package cavern.miner.config.manager;

import javax.annotation.Nonnull;

import cavern.miner.util.BlockMeta;
import cavern.miner.util.CaveUtils;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;

public class CaveBiome implements Comparable<CaveBiome>
{
	private static final BlockMeta STONE = new BlockMeta(Blocks.STONE, 0);

	private final Biome biome;

	private BlockMeta topBlock;
	private BlockMeta fillerBlock;

	public CaveBiome(Biome biome, BlockMeta top, BlockMeta filler)
	{
		this.biome = biome;
		this.topBlock = top;
		this.fillerBlock = filler;
	}

	public CaveBiome(Biome biome)
	{
		this(biome, STONE, STONE);
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
		int i = CaveUtils.compareWithNull(this, o);

		if (i == 0)
		{
			i = biome.getRegistryName().compareTo(o.biome.getRegistryName());

			if (i == 0)
			{
				i = topBlock.compareTo(o.topBlock);

				if (i == 0)
				{
					i = fillerBlock.compareTo(o.fillerBlock);
				}
			}
		}

		return i;
	}

	public Biome getBiome()
	{
		return biome;
	}

	@Nonnull
	public BlockMeta getTopBlock()
	{
		return topBlock == null ? getFillerBlock() : topBlock;
	}

	public CaveBiome setTopBlock(BlockMeta block)
	{
		topBlock = block;

		return this;
	}

	@Nonnull
	public BlockMeta getFillerBlock()
	{
		if (fillerBlock == null)
		{
			fillerBlock = new BlockMeta(Blocks.STONE.getDefaultState());
		}

		return fillerBlock;
	}

	public CaveBiome setFillerBlock(BlockMeta block)
	{
		fillerBlock = block;

		return this;
	}
}