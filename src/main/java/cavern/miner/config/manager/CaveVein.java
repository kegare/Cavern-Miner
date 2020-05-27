package cavern.miner.config.manager;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
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
	private int veinWeight;
	private int veinSize;
	private int minHeight;
	private int maxHeight;
	private String[] biomes;

	public CaveVein() {}

	public CaveVein(BlockMeta block, BlockMeta target, int weight, int size, int min, int max)
	{
		this.blockMeta = block;
		this.targetBlockMeta = target;
		this.veinWeight = weight;
		this.veinSize = size;
		this.minHeight = min;
		this.maxHeight = max;
		this.biomes = new String[0];
	}

	public CaveVein(BlockMeta block, int weight, int size, int min, int max, Object... biomes)
	{
		this(block, new BlockMeta(Blocks.STONE.getDefaultState()), weight, size, min, max);
		this.biomes = biomes == null || biomes.length <= 0 ? null : getBiomes(biomes);
	}

	public CaveVein(CaveVein vein)
	{
		this(vein.blockMeta, vein.targetBlockMeta, vein.veinWeight, vein.veinSize, vein.minHeight, vein.maxHeight);
		this.biomes = vein.biomes;
	}

	private String[] getBiomes(Object... objects)
	{
		Set<String> biomes = Sets.newTreeSet();

		for (Object e : objects)
		{
			if (e instanceof Biome)
			{
				biomes.add(((Biome)e).getRegistryName().toString());
			}
			else if (e instanceof String)
			{
				Biome biome = Biome.REGISTRY.getObject(new ResourceLocation((String)e));

				if (biome != null)
				{
					biomes.add((String)e);
				}
			}
			else if (e instanceof Integer)
			{
				Biome biome = Biome.getBiome((Integer)e);

				if (biome != null)
				{
					biomes.add(biome.getRegistryName().toString());
				}
			}
			else if (e instanceof BiomeDictionary.Type)
			{
				BiomeDictionary.getBiomes((BiomeDictionary.Type)e).stream().map(Biome::getRegistryName).map(ResourceLocation::toString).forEach(biomes::add);
			}
		}

		return biomes.toArray(new String[biomes.size()]);
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
		return veinWeight;
	}

	public void setWeight(int weight)
	{
		veinWeight = weight;
	}

	public int getSize()
	{
		return veinSize;
	}

	public void setSize(int size)
	{
		veinSize = size;
	}

	public int getMinHeight()
	{
		return minHeight;
	}

	public void setMinHeight(int height)
	{
		minHeight = height;
	}

	public int getMaxHeight()
	{
		return maxHeight;
	}

	public void setMaxHeight(int height)
	{
		maxHeight = height;
	}

	public String[] getBiomes()
	{
		return biomes;
	}

	public void setBiomes(String[] target)
	{
		biomes = target;
	}

	public boolean containsBiome(@Nullable Biome biome)
	{
		if (biomes == null || biomes.length <= 0 || biome == null)
		{
			return false;
		}

		for (String key : biomes)
		{
			if (key.equals(biome.getRegistryName().toString()))
			{
				return true;
			}
		}

		return false;
	}

	public List<Biome> getBiomeList()
	{
		if (biomes == null || biomes.length <= 0)
		{
			return Collections.emptyList();
		}

		List<Biome> list = Lists.newArrayList();

		for (String key : biomes)
		{
			Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(key));

			if (biome != null)
			{
				list.add(biome);
			}
		}

		return list;
	}
}