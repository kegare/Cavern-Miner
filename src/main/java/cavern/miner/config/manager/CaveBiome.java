package cavern.miner.config.manager;

import javax.annotation.Nonnull;

import cavern.miner.util.BlockMeta;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

public class CaveBiome implements Comparable<CaveBiome>
{
	private final Biome biome;

	private BlockMeta terrainBlock;
	private BlockMeta topBlock;

	public CaveBiome(Biome biome, BlockMeta terrain, BlockMeta top)
	{
		this.biome = biome;
		this.terrainBlock = terrain;
		this.topBlock = top;
	}

	public CaveBiome(Biome biome)
	{
		this(biome, null, null);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj == null || !(obj instanceof CaveBiome))
		{
			return false;
		}

		return biome == ((CaveBiome)obj).biome;
	}

	@Override
	public int hashCode()
	{
		return biome.getRegistryName().hashCode();
	}

	@Override
	public int compareTo(CaveBiome o)
	{
		return o == null ? -1 : Integer.compare(Biome.getIdForBiome(biome), Biome.getIdForBiome(o.biome));
	}

	public Biome getBiome()
	{
		return biome;
	}

	@Nonnull
	public BlockMeta getTerrainBlock()
	{
		if (terrainBlock == null)
		{
			setTerrainBlock(new BlockMeta(Blocks.STONE.getDefaultState()));
		}

		return terrainBlock;
	}

	public CaveBiome setTerrainBlock(BlockMeta terrain)
	{
		terrainBlock = terrain;

		return this;
	}

	@Nonnull
	public BlockMeta getTopBlock()
	{
		return topBlock == null ? getTerrainBlock() : topBlock;
	}

	public CaveBiome setTopBlock(BlockMeta top)
	{
		topBlock = top;

		return this;
	}
}