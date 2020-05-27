package cavern.miner.entity.monster;

import cavern.miner.util.CaveUtils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCavenicZombie extends EntityZombie
{
	private static final ResourceLocation LOOT_CAVENIC_ZOMBIE = LootTableList.register(CaveUtils.getKey("entities/cavenic_zombie"));

	public EntityCavenicZombie(World world)
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
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0D);
		getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
		getEntityAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0D);
	}

	@Override
	protected ResourceLocation getLootTable()
	{
		return LOOT_CAVENIC_ZOMBIE;
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