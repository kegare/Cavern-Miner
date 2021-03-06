package cavern.miner.entity;

import javax.annotation.Nullable;

import cavern.miner.entity.ai.RapidBowAttackGoal;
import cavern.miner.init.CaveItems;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class CavenicSkeletonEntity extends AbstractSkeletonEntity
{
	protected RangedBowAttackGoal<AbstractSkeletonEntity> bowAttackGoal;
	protected MeleeAttackGoal meleeAttackGoal;

	public CavenicSkeletonEntity(EntityType<? extends CavenicSkeletonEntity> type, World world)
	{
		super(type, world);
	}

	@Override
	protected void registerAttributes()
	{
		super.registerAttributes();

		getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
		getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.75D);
		getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.215D);
		getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
	}

	protected void initGoals()
	{
		bowAttackGoal = new RapidBowAttackGoal<>(this, 0.975D, 5.0F, 4);
		meleeAttackGoal = new MeleeAttackGoal(this, 1.25D, false)
		{
			@Override
			public void resetTask()
			{
				super.resetTask();

				CavenicSkeletonEntity.this.setAggroed(false);
			}

			@Override
			public void startExecuting()
			{
				super.startExecuting();

				CavenicSkeletonEntity.this.setAggroed(true);
			}
		};
	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.ENTITY_SKELETON_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return SoundEvents.ENTITY_SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.ENTITY_SKELETON_DEATH;
	}

	@Override
	protected SoundEvent getStepSound()
	{
		return SoundEvents.ENTITY_SKELETON_STEP;
	}

	@Nullable
	protected Hand getBowHand()
	{
		if (getHeldItemMainhand().getItem() instanceof BowItem)
		{
			return Hand.MAIN_HAND;
		}

		if (getHeldItemOffhand().getItem() instanceof BowItem)
		{
			return Hand.OFF_HAND;
		}

		return null;
	}

	@Override
	public void setCombatTask()
	{
		if (world != null && !world.isRemote)
		{
			if (bowAttackGoal == null || meleeAttackGoal == null)
			{
				initGoals();
			}

			goalSelector.removeGoal(meleeAttackGoal);
			goalSelector.removeGoal(bowAttackGoal);

			if (getBowHand() != null)
			{
				bowAttackGoal.setAttackCooldown(world.getDifficulty() != Difficulty.HARD ? 40 : 20);

				goalSelector.addGoal(4, bowAttackGoal);
			}
			else
			{
				goalSelector.addGoal(4, meleeAttackGoal);
			}
		}
	}

	@Override
	protected AbstractArrowEntity fireArrow(ItemStack arrowStack, float distanceFactor)
	{
		AbstractArrowEntity arrow = super.fireArrow(arrowStack, distanceFactor);

		if (arrow instanceof ArrowEntity)
		{
			CavenicArrowEntity cavenicArrow = new CavenicArrowEntity(world, this);

			cavenicArrow.setPotionEffect(arrowStack);
			cavenicArrow.setEnchantmentEffectsFromEntity(this, distanceFactor);

			return cavenicArrow;
		}

		return arrow;
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntitySize size)
	{
		return 1.95F;
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return 1;
	}

	@Override
	public ItemStack getPickedResult(RayTraceResult target)
	{
		return new ItemStack(CaveItems.CAVENIC_SKELETON_SPAWN_EGG.get());
	}
}