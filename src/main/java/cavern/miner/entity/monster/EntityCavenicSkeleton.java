package cavern.miner.entity.monster;

import javax.annotation.Nullable;

import cavern.miner.entity.ai.EntityAIAttackCavenicBow;
import cavern.miner.entity.projectile.EntityCavenicArrow;
import cavern.miner.item.CaveItems;
import cavern.miner.util.CaveUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCavenicSkeleton extends EntitySkeleton
{
	private static final ResourceLocation LOOT_CAVENIC_SKELETON = LootTableList.register(CaveUtils.getKey("entities/cavenic_skeleton"));

	protected EntityAIAttackRangedBow<EntityCavenicSkeleton> aiArrowAttack;
	protected EntityAIAttackMelee aiAttackOnCollide;

	public EntityCavenicSkeleton(World world)
	{
		super(world);
		this.experienceValue = 13;
		this.setSize(0.68F, 2.0F);
	}

	protected void initCustomAI()
	{
		aiArrowAttack = new EntityAIAttackCavenicBow<>(this, 0.975D, 5.0F, 4);
		aiAttackOnCollide = new EntityAIAttackMelee(this, 1.25D, false)
		{
			@Override
			public void resetTask()
			{
				super.resetTask();

				EntityCavenicSkeleton.this.setSwingingArms(false);
			}

			@Override
			public void startExecuting()
			{
				super.startExecuting();

				EntityCavenicSkeleton.this.setSwingingArms(true);
			}
		};
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		applyMobAttributes();
	}

	protected void applyMobAttributes()
	{
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
		getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(21.0D);
	}

	@Override
	protected ResourceLocation getLootTable()
	{
		return LOOT_CAVENIC_SKELETON;
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
	{
		super.setEquipmentBasedOnDifficulty(difficulty);

		if (rand.nextDouble() < 0.45D)
		{
			setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(CaveItems.CAVENIC_BOW));
		}
	}

	@Override
	public void setCombatTask()
	{
		if (aiArrowAttack == null || aiAttackOnCollide == null)
		{
			initCustomAI();
		}

		if (world != null && !world.isRemote)
		{
			tasks.removeTask(aiAttackOnCollide);
			tasks.removeTask(aiArrowAttack);

			ItemStack heldMain = getHeldItemMainhand();

			if (!heldMain.isEmpty() && heldMain.getItem() instanceof ItemBow)
			{
				tasks.addTask(4, aiArrowAttack);
			}
			else
			{
				tasks.addTask(4, aiAttackOnCollide);
			}
		}
	}

	@Override
	protected EntityArrow getArrow(float dist)
	{
		ItemStack heldOff = getItemStackFromSlot(EntityEquipmentSlot.OFFHAND);

		if (heldOff.getItem() == Items.SPECTRAL_ARROW)
		{
			EntitySpectralArrow arrow = new EntitySpectralArrow(this.world, this);
			arrow.setEnchantmentEffectsFromEntity(this, dist);

			return arrow;
		}
		else
		{
			EntityArrow arrow = new EntityCavenicArrow(world, this);
			arrow.setEnchantmentEffectsFromEntity(this, dist);

			if (heldOff.getItem() == Items.TIPPED_ARROW && arrow instanceof EntityTippedArrow)
			{
				((EntityTippedArrow)arrow).setPotionEffect(heldOff);
			}

			return arrow;
		}
	}

	public boolean isFriends(@Nullable Entity entity)
	{
		return entity != null && entity instanceof EntityCavenicSkeleton;
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source)
	{
		if (super.isEntityInvulnerable(source))
		{
			return true;
		}

		if (source.getTrueSource() == this || source.getImmediateSource() == this)
		{
			return true;
		}

		if (isFriends(source.getTrueSource()) || isFriends(source.getImmediateSource()))
		{
			return true;
		}

		return false;
	}

	@Override
	public void setAttackTarget(EntityLivingBase entity)
	{
		if (isFriends(entity))
		{
			return;
		}

		super.setAttackTarget(entity);
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