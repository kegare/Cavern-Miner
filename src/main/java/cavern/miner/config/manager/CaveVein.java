package cavern.miner.config.manager;

import java.util.Comparator;
import java.util.Set;

import com.google.common.collect.Sets;

import cavern.miner.util.BlockMeta;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CaveVein
{
	private BlockMeta blockMeta;
	private BlockMeta targetBlockMeta;
	private int weight;
	private int size;
	private int minHeight;
	private int maxHeight;

	private final Set<Biome> biomes = Sets.newTreeSet(Comparator.comparing(Biome::getRegistryName));
	private final Set<BiomeDictionary.Type> biomeTypes = Sets.newHashSet();

	public CaveVein(BlockMeta block, BlockMeta target, int weight, int size, int min, int max)
	{
		this.blockMeta = block;
		this.targetBlockMeta = target;
		this.weight = weight;
		this.size = size;
		this.minHeight = min;
		this.maxHeight = max;
	}

	public CaveVein(BlockMeta block, int weight, int size, int min, int max)
	{
		this(block, new BlockMeta(Blocks.STONE.getDefaultState()), weight, size, min, max);
	}

	public CaveVein(CaveVein vein)
	{
		this(vein.blockMeta, vein.targetBlockMeta, vein.weight, vein.size, vein.minHeight, vein.maxHeight);
		this.biomes.addAll(vein.biomes);
		this.biomeTypes.addAll(vein.biomeTypes);
	}

	public BlockMeta getBlockMeta()
	{
		return blockMeta;
	}

	public void setBlockMeta(BlockMeta block)
	{
		blockMeta = block;
	}

	public BlockMeta getTarget()
	{
		return targetBlockMeta;
	}

	public void setTarget(BlockMeta block)
	{
		targetBlockMeta = block;
	}

	public int getWeight()
	{
		return weight;
	}

	public void setWeight(int value)
	{
		weight = value;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int value)
	{
		size = value;
	}

	public int getMinHeight()
	{
		return minHeight;
	}

	public void setMinHeight(int y)
	{
		minHeight = y;
	}

	public int getMaxHeight()
	{
		return maxHeight;
	}

	public void setMaxHeight(int height)
	{
		maxHeight = height;
	}

	public Set<Biome> getBiomes()
	{
		return biomes;
	}

	public Set<BiomeDictionary.Type> getBiomeTypes()
	{
		return biomeTypes;
	}

	public CaveVein setBiomes(Object obj)
	{
		if (obj == null)
		{
			return this;
		}

		if (obj instanceof Object[])
		{
			for (Object o : (Object[])obj)
			{
				setBiomes(o);
			}
		}
		else if (obj instanceof BiomeDictionary.Type)
		{
			biomeTypes.add((BiomeDictionary.Type)obj);
		}
		else if (obj instanceof Biome)
		{
			biomes.add((Biome)obj);
		}
		else
		{
			ResourceLocation key = null;

			if (obj instanceof ResourceLocation)
			{
				key = (ResourceLocation)obj;
			}
			else if (obj instanceof String)
			{
				key = new ResourceLocation((String)obj);
			}

			if (key != null)
			{
				Biome biome = ForgeRegistries.BIOMES.getValue(key);

				if (biome != null)
				{
					biomes.add(biome);
				}
			}
		}

		return this;
	}

	public boolean containsBiome(Biome biome)
	{
		return biomes.contains(biome) || biomeTypes.stream().anyMatch(type -> BiomeDictionary.hasType(biome, type));
	}
}