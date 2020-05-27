package cavern.miner.handler.api;

import javax.annotation.Nullable;

import cavern.miner.api.DimensionWrapper;
import cavern.miner.world.CaveDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.world.DimensionType;

public class DimensionHandler implements DimensionWrapper
{
	private boolean isInDimension(@Nullable Entity entity, @Nullable DimensionType type)
	{
		return entity != null && type != null && entity.dimension == type.getId();
	}

	@Override
	public DimensionType getCavern()
	{
		return CaveDimensions.CAVERN;
	}

	@Override
	public boolean isInCavern(Entity entity)
	{
		return isInDimension(entity, getCavern());
	}

	@Override
	public DimensionType getHugeCavern()
	{
		return CaveDimensions.HUGE_CAVERN;
	}

	@Override
	public boolean isInHugeCavern(Entity entity)
	{
		return isInDimension(entity, getHugeCavern());
	}

	@Override
	public DimensionType getCaveland()
	{
		return CaveDimensions.CAVELAND;
	}

	@Override
	public boolean isInCaveland(Entity entity)
	{
		return isInDimension(entity, getCaveland());
	}

	@Override
	public boolean isInCaverns(Entity entity)
	{
		return isInCavern(entity) || isInHugeCavern(entity) || isInCaveland(entity);
	}

	@Override
	public boolean isCaverns(DimensionType type)
	{
		return type != null && (type == getCavern() || type == getHugeCavern() || type == getCaveland());
	}
}