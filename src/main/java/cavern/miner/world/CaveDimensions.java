package cavern.miner.world;

import javax.annotation.Nullable;

import cavern.miner.block.BlockCavernPortal;
import cavern.miner.block.CaveBlocks;
import cavern.miner.config.CavelandConfig;
import cavern.miner.config.CavernConfig;
import cavern.miner.config.HugeCavernConfig;
import cavern.miner.core.CavernMod;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

public final class CaveDimensions
{
	public static DimensionType CAVERN;
	public static DimensionType HUGE_CAVERN;
	public static DimensionType CAVELAND;

	@Nullable
	private static DimensionType register(String name, int id, Class<? extends WorldProvider> provider)
	{
		if (id == 0 || DimensionManager.isDimensionRegistered(id))
		{
			return null;
		}

		DimensionType type = DimensionType.register(name, "_" + name, id, provider, false);

		DimensionManager.registerDimension(id, type);

		return type;
	}

	public static void registerDimensions()
	{
		CAVERN = register("cavern", CavernConfig.dimensionId, WorldProviderCavern.class);
		HUGE_CAVERN = register("huge_cavern", HugeCavernConfig.dimensionId, WorldProviderHugeCavern.class);
		CAVELAND = register("caveland", CavelandConfig.dimensionId, WorldProviderCaveland.class);
	}

	@Nullable
	public static BlockCavernPortal getPortalBlock(@Nullable DimensionType type)
	{
		if (type == null)
		{
			return null;
		}

		if (type == CaveDimensions.CAVERN)
		{
			return CaveBlocks.CAVERN_PORTAL;
		}

		if (type == CaveDimensions.HUGE_CAVERN)
		{
			return CaveBlocks.HUGE_CAVERN_PORTAL;
		}

		if (type == CaveDimensions.CAVELAND)
		{
			return CaveBlocks.CAVELAND_PORTAL;
		}

		return null;
	}

	public static String getLocalizedName(@Nullable DimensionType type)
	{
		if (type == null)
		{
			return null;
		}

		if (type == CAVERN)
		{
			return CavernMod.proxy.translate("dimension.cavern.name");
		}

		if (type == HUGE_CAVERN)
		{
			return CavernMod.proxy.translate("dimension.hugeCavern.name");
		}

		if (type == CAVELAND)
		{
			return CavernMod.proxy.translate("dimension.caveland.name");
		}

		return type.getName();
	}
}