package cavern.miner.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.world.World;

public class CavenicArrowEntity extends ArrowEntity
{
	public CavenicArrowEntity(EntityType<? extends ArrowEntity> type, World world)
	{
		super(type, world);
	}

	public CavenicArrowEntity(World world, double x, double y, double z)
	{
		super(world, x, y, z);
	}

	public CavenicArrowEntity(World world, LivingEntity shooter)
	{
		super(world, shooter);
	}

	@Override
	public void tick()
	{
		if (inGround && timeInGround >= 100)
		{
			setNoClip(true);
		}

		super.tick();
	}
}