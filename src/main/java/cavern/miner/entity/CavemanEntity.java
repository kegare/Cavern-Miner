package cavern.miner.entity;

import java.util.Random;

import cavern.miner.config.GeneralConfig;
import cavern.miner.init.CaveNetworkConstants;
import cavern.miner.network.CavemanTradeMessage;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
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
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

public class CavemanEntity extends CreatureEntity
{
	private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(CavemanEntity.class, DataSerializers.BOOLEAN);

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

			getNavigator().setPath(null, 0.0D);
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

	public NonNullList<CavemanTrade.TradeEntry> getTradeEntries()
	{
		return GeneralConfig.INSTANCE.cavemanTrades.getEntries();
	}

	@Override
	protected boolean processInteract(PlayerEntity player, Hand hand)
	{
		boolean sitting = !isSitting();

		setSitting(sitting);

		if (sitting && player instanceof ServerPlayerEntity)
		{
			CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new CavemanTradeMessage(getTradeEntries()));
		}

		return true;
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return 1;
	}

	public static boolean canSpawnInLight(EntityType<? extends CreatureEntity> type, IWorld world, SpawnReason reason, BlockPos pos, Random random)
	{
		return MonsterEntity.isValidLightLevel(world, pos, random) && canSpawnOn(type, world, reason, pos, random);
	}
}