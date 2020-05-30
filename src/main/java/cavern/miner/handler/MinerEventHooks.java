package cavern.miner.handler;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import cavern.miner.api.CavernAPI;
import cavern.miner.api.block.CaveOre;
import cavern.miner.api.event.CriticalMiningEvent;
import cavern.miner.api.event.MinerEvent;
import cavern.miner.config.MiningConfig;
import cavern.miner.data.Miner;
import cavern.miner.data.MinerRank;
import cavern.miner.data.MiningCache;
import cavern.miner.data.PlacedCache;
import cavern.miner.enchantment.MiningUnit;
import cavern.miner.network.CaveNetworkRegistry;
import cavern.miner.network.client.MiningMessage;
import cavern.miner.util.BlockMeta;
import cavern.miner.util.CaveUtils;
import cavern.miner.util.PlayerUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class MinerEventHooks
{
	private final Random rand = CaveEventHooks.RANDOM;

	@SubscribeEvent
	public void onBlockBreak(BreakEvent event)
	{
		EntityPlayer entityPlayer = event.getPlayer();

		if (entityPlayer == null || entityPlayer instanceof FakePlayer || !(entityPlayer instanceof EntityPlayerMP))
		{
			return;
		}

		EntityPlayerMP player = (EntityPlayerMP)entityPlayer;

		if (!CavernAPI.dimension.isInCaverns(player))
		{
			return;
		}

		if (MiningConfig.actualMining && MiningUnit.get(player).isBreaking())
		{
			return;
		}

		ItemStack stack = player.getHeldItemMainhand();

		if (!CaveUtils.isPickaxe(stack))
		{
			return;
		}

		IBlockState state = event.getState();
		BlockPos pos = event.getPos();
		String name = player.mcServer.getFolderName();
		DimensionType dim = player.world.provider.getDimensionType();

		if (CaveUtils.isBlockEqual(state, PlacedCache.get(name, dim).getCache(pos)))
		{
			return;
		}

		World world = event.getWorld();
		int i = 0;

		if (state.getBlock() instanceof CaveOre)
		{
			i = ((CaveOre)state.getBlock()).getMiningPoint(world, pos, state, player, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack));
		}
		else
		{
			i = MiningConfig.miningPoints.getPoint(state);
		}

		if (i <= 0)
		{
			return;
		}

		Miner miner = Miner.get(player, true);

		if (miner == null)
		{
			return;
		}

		MinerEvent.MineOre minerEvent = new MinerEvent.MineOre(player, miner, i, pos, state);

		if (MinecraftForge.EVENT_BUS.post(minerEvent))
		{
			return;
		}

		int point = minerEvent.getNewPoint();

		if (point <= 0)
		{
			return;
		}

		miner.addPoint(point, false);
		miner.addMiningRecord(new BlockMeta(state));

		CaveNetworkRegistry.sendTo(() -> new MiningMessage(miner, state, point), player);

		MiningCache cache = MiningCache.get(player);

		cache.setLastMining(state, point);

		int combo = cache.getCombo();

		if (combo > 0 && combo % 10 == 0)
		{
			world.playSound(null, player.posX, player.posY + 0.25D, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
				SoundCategory.PLAYERS, 0.1F, 0.5F * ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.8F));

			player.addExperience(combo / 10);
		}

		if (combo >= 50)
		{
			PlayerUtils.grantAdvancement(player, "good_mine");
		}
	}

	@SubscribeEvent
	public void onBlockPlace(BlockEvent.EntityPlaceEvent event)
	{
		Entity placer = event.getEntity();

		if (placer == null || placer instanceof FakePlayer || !(placer instanceof EntityPlayerMP))
		{
			return;
		}

		if (!CavernAPI.dimension.isInCaverns(placer))
		{
			return;
		}

		IBlockState state = event.getPlacedBlock();

		if (MiningConfig.miningPoints.getPoint(state) > 0)
		{
			EntityPlayerMP player = (EntityPlayerMP)placer;
			String name = player.mcServer.getFolderName();
			DimensionType dim = player.world.provider.getDimensionType();

			PlacedCache.get(name, dim).addCache(event.getPos(), state);
		}
	}

	@SubscribeEvent
	public void onHarvestDrops(HarvestDropsEvent event)
	{
		if (!MiningConfig.criticalMining || event.isSilkTouching())
		{
			return;
		}

		World world = event.getWorld();

		if (world.isRemote)
		{
			return;
		}

		EntityPlayer player = event.getHarvester();

		if (player == null || player instanceof FakePlayer || !CavernAPI.dimension.isInCaverns(player))
		{
			return;
		}

		IBlockState state = event.getState();

		if (MiningConfig.miningPoints.getPoint(state) <= 0)
		{
			return;
		}

		if (state.getMaterial() != Material.ROCK)
		{
			return;
		}

		MinerRank rank = MinerRank.get(Miner.get(player).getRank());
		float f = rank.getBonusChance();

		if (f <= 1.0F)
		{
			return;
		}

		f = (f - 1.0F) * 0.3F;

		ItemStack held = player.getHeldItemMainhand();
		String tool = state.getBlock().getHarvestTool(state);

		if (held.isEmpty() || tool == null)
		{
			return;
		}

		int toolLevel = held.getItem().getHarvestLevel(held, tool, player, state);

		if (toolLevel <= 0)
		{
			return;
		}

		f *= 1.0F + toolLevel * 0.1F;

		List<ItemStack> originalDrops = event.getDrops();
		List<ItemStack> drops = Lists.newArrayList();

		for (ItemStack stack : originalDrops)
		{
			if (!stack.isEmpty() && !(stack.getItem() instanceof ItemBlock) && rand.nextFloat() <= f)
			{
				drops.add(stack.copy());
			}
		}

		if (!drops.isEmpty())
		{
			CriticalMiningEvent criticalEvent = new CriticalMiningEvent(world, event.getPos(), state, player, event.getFortuneLevel(), originalDrops, drops);

			if (MinecraftForge.EVENT_BUS.post(criticalEvent))
			{
				return;
			}

			player.sendStatusMessage(new TextComponentTranslation("cavern.message.mining.critical"), true);

			originalDrops.addAll(criticalEvent.getBonusDrops());
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		EntityLivingBase living = event.getEntityLiving();

		if (living instanceof EntityPlayer && !(living instanceof FakePlayer))
		{
			MiningCache.get((EntityPlayer)living).onUpdate();
		}
	}
}