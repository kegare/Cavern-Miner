package cavern.miner.api;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.world.DimensionType;

public interface DimensionWrapper
{
	@Nullable
	DimensionType getCavern();

	boolean isInCavern(@Nullable Entity entity);

	@Nullable
	DimensionType getHugeCavern();

	boolean isInHugeCavern(@Nullable Entity entity);

	@Nullable
	DimensionType getCaveland();

	boolean isInCaveland(@Nullable Entity entity);

	boolean isInCaverns(@Nullable Entity entity);

	boolean isCaverns(@Nullable DimensionType type);
}