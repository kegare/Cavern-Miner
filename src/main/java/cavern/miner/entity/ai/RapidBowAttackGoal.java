package cavern.miner.entity.ai;

import java.util.EnumSet;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;

public class RapidBowAttackGoal<T extends MonsterEntity & IRangedAttackMob> extends RangedBowAttackGoal<T>
{
	private final T attacker;
	private final double moveSpeedAmp;
	private final float maxAttackDistance;
	private final int attackSpeed;

	private int seeTime;
	private int attackTime;
	private int attackCooldown;
	private int attackRapid;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime = -1;

	public RapidBowAttackGoal(T attacker, double speedAmplifier, float maxDistance, int attackSpeed)
	{
		super(attacker, speedAmplifier, 0, maxDistance);
		this.attacker = attacker;
		this.moveSpeedAmp = speedAmplifier;
		this.maxAttackDistance = maxDistance * maxDistance;
		this.attackSpeed = attackSpeed;
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public void setAttackCooldown(int time)
	{
		attackCooldown = time;
	}

	@Override
	public boolean shouldExecute()
	{
		return attacker.getAttackTarget() != null && isBowInMainhand();
	}

	@Override
	protected boolean isBowInMainhand()
	{
		return attacker.getHeldItemMainhand().getItem() instanceof BowItem || attacker.getHeldItemOffhand().getItem() instanceof BowItem;
	}

	@Override
	public boolean shouldContinueExecuting()
	{
		return (shouldExecute() || !attacker.getNavigator().noPath()) && isBowInMainhand();
	}

	@Override
	public void startExecuting()
	{
		super.startExecuting();

		attacker.setAggroed(true);
	}

	@Override
	public void resetTask()
	{
		super.resetTask();

		attacker.setAggroed(false);
		seeTime = 0;
		attackTime = 0;
		attacker.resetActiveHand();
	}

	@Override
	public void tick()
	{
		LivingEntity target = attacker.getAttackTarget();

		if (target != null)
		{
			double dist = attacker.getDistanceSq(target.getPosX(), target.getPosY(), target.getPosZ());
			boolean canSee = attacker.getEntitySenses().canSee(target);
			boolean seeing = seeTime > 0;

			if (canSee != seeing)
			{
				seeTime = 0;
			}

			if (canSee)
			{
				++seeTime;
			}
			else
			{
				--seeTime;
			}

			if (dist <= maxAttackDistance && seeTime >= 15)
			{
				attacker.getNavigator().clearPath();
				++strafingTime;
			}
			else
			{
				attacker.getNavigator().tryMoveToEntityLiving(target, moveSpeedAmp);
				strafingTime = -1;
			}

			if (strafingTime >= 5)
			{
				if (attacker.getRNG().nextFloat() < 0.3D)
				{
					strafingClockwise = !strafingClockwise;
				}

				if (attacker.getRNG().nextFloat() < 0.3D)
				{
					strafingBackwards = !strafingBackwards;
				}

				strafingTime = 0;
			}

			if (strafingTime > -1)
			{
				if (dist > maxAttackDistance * 0.75F)
				{
					strafingBackwards = false;
				}
				else if (dist < maxAttackDistance * 0.25F)
				{
					strafingBackwards = true;
				}

				attacker.getMoveHelper().strafe(strafingBackwards ? -0.5F : 0.5F, strafingClockwise ? 0.5F : -0.5F);
				attacker.faceEntity(target, 30.0F, 30.0F);
			}
			else
			{
				attacker.getLookController().setLookPositionWithEntity(target, 30.0F, 30.0F);
			}

			if (attacker.isHandActive())
			{
				if (!canSee && seeTime < -20 || attackTime > 200)
				{
					attacker.resetActiveHand();

					attackTime = 0;
					attackCooldown = 50;
				}
				else if (canSee && --attackCooldown <= 0)
				{
					if (++attackRapid >= attackSpeed)
					{
						attacker.attackEntityWithRangedAttack(target, BowItem.getArrowVelocity(5));

						attackRapid = 0;
					}

					++attackTime;
				}
			}
			else if (seeTime >= -20)
			{
				attacker.setActiveHand(ProjectileHelper.getHandWith(attacker, Items.BOW));

				attackTime = 0;
			}
		}
	}
}