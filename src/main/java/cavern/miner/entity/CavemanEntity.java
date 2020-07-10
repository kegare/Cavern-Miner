package cavern.miner.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import cavern.miner.block.RandomiteDrop;
import cavern.miner.config.GeneralConfig;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveItems;
import cavern.miner.init.CaveNetworkConstants;
import cavern.miner.network.CavemanTradeMessage;
import cavern.miner.storage.Miner;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.FleeSunGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.RestrictSunGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

public class CavemanEntity extends CreatureEntity
{
	private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(CavemanEntity.class, DataSerializers.BOOLEAN);

	private final List<CavemanTrade.TradeEntry> tradeEntries = new ArrayList<>();

	private PlayerEntity tradingPlayer;

	public CavemanEntity(EntityType<? extends CreatureEntity> type, World world)
	{
		super(type, world);
	}

	@Override
	protected void registerData()
	{
		super.registerData();

		dataManager.register(SITTING, false);
	}

	@Override
	protected void registerGoals()
	{
		goalSelector.addGoal(1, new AvoidEntityGoal<>(this, PlayerEntity.class, 5.0F, 1.0D, 1.1D));
		goalSelector.addGoal(2, new RestrictSunGoal(this));
		goalSelector.addGoal(3, new FleeSunGoal(this, 1.0D));
		goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
		goalSelector.addGoal(6, new LookRandomlyGoal(this));
	}

	@Override
	protected void registerAttributes()
	{
		super.registerAttributes();

		getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
		getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2875D);
	}

	public boolean isSitting()
	{
		return dataManager.get(SITTING);
	}

	public void setSitting(boolean sit)
	{
		dataManager.set(SITTING, sit);

		if (sit)
		{
			updateMovementGoalFlags();

			goalSelector.getRunningGoals().forEach(PrioritizedGoal::resetTask);

			getNavigator().clearPath();
		}
	}

	@Override
	protected void updateMovementGoalFlags()
	{
		super.updateMovementGoalFlags();

		goalSelector.setFlag(Goal.Flag.MOVE, !isSitting());
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntitySize size)
	{
		return isSitting() ? super.getEyeHeight() * 0.6F : super.getStandingEyeHeight(pose, size);
	}

	public List<CavemanTrade.TradeEntry> getTradeEntries()
	{
		if (world.isRemote)
		{
			return Collections.emptyList();
		}

		if (tradeEntries.isEmpty())
		{
			refreshTradeEntries();
		}

		return tradeEntries;
	}

	public void refreshTradeEntries()
	{
		if (world.isRemote)
		{
			return;
		}

		tradeEntries.clear();

		List<CavemanTrade.TradeEntry> list = GeneralConfig.INSTANCE.cavemanTrades.getEntries();

		for (int i = 0, count = 5 + rand.nextInt(5); i < count; ++i)
		{
			tradeEntries.add(WeightedRandom.getRandomItem(rand, list));
		}

		if (GeneralConfig.INSTANCE.includeRandomite.get())
		{
			for (int i = 0, count = 5 + rand.nextInt(5); i < count; ++i)
			{
				RandomiteDrop.DropEntry entry = GeneralConfig.INSTANCE.randomiteDrops.getRandomDrop(rand);

				if (entry != RandomiteDrop.EMPTY)
				{
					tradeEntries.add(new CavemanTrade.ItemStackEntry(entry.getDropItem(), entry.itemWeight, 30 + 10 * rand.nextInt(3), "STONE"));
				}
			}
		}

		Collections.sort(tradeEntries);
	}

	@Override
	public ILivingEntityData onInitialSpawn(IWorld world, DifficultyInstance difficulty, SpawnReason reason, ILivingEntityData spawnData, CompoundNBT dataTag)
	{
		ILivingEntityData data = super.onInitialSpawn(world, difficulty, reason, spawnData, dataTag);

		refreshTradeEntries();

		return data;
	}

	@Override
	protected boolean processInteract(PlayerEntity player, Hand hand)
	{
		if (world.isRemote || tradingPlayer != null)
		{
			return false;
		}

		tradingPlayer = player;

		setSitting(true);

		final List<CavemanTrade.TradeEntry> list = getTradeEntries();

		if (list.isEmpty())
		{
			return false;
		}

		Collections.sort(list);

		if (player instanceof ServerPlayerEntity)
		{
			final IntList inactived = new IntArrayList();

			player.getCapability(CaveCapabilities.MINER).map(Miner::getRank).ifPresent(o ->
			{
				for (int i = 0; i < list.size(); ++i)
				{
					if (o.getIndex() < list.get(i).getRank().getIndex())
					{
						inactived.add(i);
					}
				}
			});

			CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new CavemanTradeMessage(getEntityId(), list, inactived.toIntArray()));
		}

		return true;
	}

	@Nullable
	public PlayerEntity getTradingPlayer()
	{
		return tradingPlayer;
	}

	public void resetTradingPlayer()
	{
		tradingPlayer = null;
	}

	@Override
	public void tick()
	{
		super.tick();

		if (!world.isRemote && ticksExisted % 20 == 0)
		{
			setSitting(tradingPlayer != null);
		}
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return 1;
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target)
	{
		return new ItemStack(CaveItems.CAVEMAN_SPAWN_EGG.get());
	}

	@Override
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);

		ListNBT list = new ListNBT();

		tradeEntries.stream().map(CavemanTrade::write).forEach(list::add);

		compound.put("TradeEntries", list);
	}

	@Override
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);

		tradeEntries.clear();

		ListNBT list = compound.getList("TradeEntries", Constants.NBT.TAG_COMPOUND);

		list.stream().map(o -> CavemanTrade.read((CompoundNBT)o)).forEach(tradeEntries::add);
	}

	public static boolean canSpawnInLight(EntityType<? extends CreatureEntity> type, IWorld world, SpawnReason reason, BlockPos pos, Random random)
	{
		return MonsterEntity.isValidLightLevel(world, pos, random) && canSpawnOn(type, world, reason, pos, random);
	}
}