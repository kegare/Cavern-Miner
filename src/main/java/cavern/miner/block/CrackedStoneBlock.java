package cavern.miner.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class CrackedStoneBlock extends CaveOreBlock
{
	public CrackedStoneBlock(Block.Properties properties)
	{
		super(properties);
	}

	@Override
	protected int getExperience(Random rand)
	{
		return 0;
	}

	@Override
	public int getPoint(BlockState state, IWorldReader reader, BlockPos pos, int point)
	{
		return MathHelper.nextInt(RANDOM, point - 2, point + 2);
	}

	@Override
	public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack)
	{
		super.harvestBlock(world, player, pos, state, te, stack);

		if (!world.isRemote && !world.restoringBlockSnapshots)
		{
			int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);

			if (fortune > 0)
			{
				fireIntensiveEffect(player, fortune);
			}
			else if (RANDOM.nextDouble() < 0.1D)
			{
				fireExplosion(world, pos);
			}
			else
			{
				fireAreaEffect(world, pos, player);
			}
		}
	}

	protected void fireAreaEffect(World world, BlockPos pos, @Nullable LivingEntity entity)
	{
		AreaEffectCloudEntity areaEffectCloud = new AreaEffectCloudEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);

		areaEffectCloud.setOwner(entity);
		areaEffectCloud.setRadius(2.5F);
		areaEffectCloud.setRadiusOnUse(-0.5F);
		areaEffectCloud.setWaitTime(10);
		areaEffectCloud.setDuration(20 * 30);
		areaEffectCloud.addEffect(new EffectInstance(getRandomEffect(RANDOM.nextDouble() < 0.3D), 20 * 30, RANDOM.nextInt(2)));

		world.addEntity(areaEffectCloud);
	}

	protected void fireIntensiveEffect(LivingEntity entity, int fortune)
	{
		for (int i = 0; i < Math.min(fortune, 5); ++i)
		{
			entity.addPotionEffect(new EffectInstance(getRandomEffect(false), 20 * 60, RANDOM.nextInt(2)));
		}
	}

	protected Effect getRandomEffect(boolean badEffect)
	{
		if (badEffect)
		{
			if (RANDOM.nextDouble() < 0.15D)
			{
				return Effects.WEAKNESS;
			}

			if (RANDOM.nextDouble() < 0.3D)
			{
				return Effects.POISON;
			}

			if (RANDOM.nextDouble() < 0.3D)
			{
				return Effects.SLOWNESS;
			}

			return Effects.HUNGER;
		}

		if (RANDOM.nextDouble() < 0.15D)
		{
			return Effects.NIGHT_VISION;
		}

		if (RANDOM.nextDouble() < 0.3D)
		{
			return Effects.REGENERATION;
		}

		if (RANDOM.nextDouble() < 0.3D)
		{
			return Effects.ABSORPTION;
		}

		return Effects.RESISTANCE;
	}

	protected void fireExplosion(World world, BlockPos pos)
	{
		double posX = pos.getX() + 0.5D;
		double posY = pos.getY() + 0.5D;
		double posZ = pos.getZ() + 0.5D;
		float strength = 1.45F;

		if (RANDOM.nextDouble() < 0.15D)
		{
			strength = 3.0F;
		}

		world.createExplosion(null, posX, posY, posZ, strength, false, Explosion.Mode.NONE);
	}
}