package cavern.miner.entity.monster;

import cavern.miner.util.CaveUtils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCavenicCreeper extends EntityCreeper
{
	private static final ResourceLocation LOOT_CAVENIC_CREEPER = LootTableList.register(CaveUtils.getKey("entities/cavenic_creeper"));

	public EntityCavenicCreeper(World world)
	{
		super(world);
		this.experienceValue = 13;
		this.fuseTime = 15;
		this.explosionRadius = 5;
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		applyMobAttributes();
	}

	protected void applyMobAttributes()
	{
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
		getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.85D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
	}

	@Override
	protected ResourceLocation getLootTable()
	{
		return LOOT_CAVENIC_CREEPER;
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