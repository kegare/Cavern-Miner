package cavern.miner.entity.monster;

import cavern.miner.util.CaveUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCavenicSpider extends EntitySpider
{
	private static final ResourceLocation LOOT_CAVENIC_SPIDER = LootTableList.register(CaveUtils.getKey("entities/cavenic_spider"));

	public EntityCavenicSpider(World world)
	{
		super(world);
		this.experienceValue = 12;
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		applyMobAttributes();
	}

	protected void applyMobAttributes()
	{
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
		getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.60000001192092896D);
	}

	@Override
	protected ResourceLocation getLootTable()
	{
		return LOOT_CAVENIC_SPIDER;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block)
	{
		playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.05F, 1.0F);
	}

	protected int getBlindnessAttackPower()
	{
		switch (world.getDifficulty())
		{
			case NORMAL:
				return 5;
			case HARD:
				return 10;
			default:
				return 3;
		}
	}

	protected int getPoisonAttackPower()
	{
		return 0;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity)
	{
		if (super.attackEntityAsMob(entity))
		{
			if (entity instanceof EntityLivingBase)
			{
				EntityLivingBase target = (EntityLivingBase)entity;
				int sec = getBlindnessAttackPower();

				if (sec > 0 && !target.isPotionActive(MobEffects.BLINDNESS))
				{
					target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, sec * 20));
				}

				sec = getPoisonAttackPower();

				if (sec > 0 && !target.isPotionActive(MobEffects.POISON))
				{
					target.addPotionEffect(new PotionEffect(MobEffects.POISON, sec * 20));
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage)
	{
		if (source == DamageSource.FALL)
		{
			damage *= 0.35F;
		}

		return !source.isFireDamage() && super.attackEntityFrom(source, damage);
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return 1;
	}
}