package cavern.miner.util;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.base.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public class BlockMeta implements Comparable<BlockMeta>
{
	private final Block block;
	private final int meta;

	private IBlockState stateCache;

	public BlockMeta(ResourceLocation name, int meta)
	{
		this(name, Blocks.AIR, meta);
	}

	public BlockMeta(String name, int meta)
	{
		this(new ResourceLocation(name), meta);
	}

	public BlockMeta(Block block, int meta)
	{
		this.block = block;
		this.meta = meta;
	}

	public BlockMeta(ResourceLocation name, Block defaultValue, int meta)
	{
		this.block = ObjectUtils.defaultIfNull(ForgeRegistries.BLOCKS.getValue(name), defaultValue);
		this.meta = meta;
	}

	public BlockMeta(String name, Block defaultValue, int meta)
	{
		this(new ResourceLocation(name), defaultValue, meta);
	}

	public BlockMeta(IBlockState state)
	{
		this(state.getBlock(), state.getBlock().getMetaFromState(state));
		this.stateCache = state;
	}

	public Block getBlock()
	{
		return block;
	}

	public ResourceLocation getRegistryName()
	{
		return block.getRegistryName();
	}

	public int getMeta()
	{
		return meta;
	}

	@SuppressWarnings("deprecation")
	public IBlockState getBlockState()
	{
		if (stateCache == null)
		{
			stateCache = block.getStateFromMeta(meta);
		}

		return stateCache;
	}

	@Override
	public String toString()
	{
		if (meta < 0 || meta == OreDictionary.WILDCARD_VALUE || !Item.getItemFromBlock(block).getHasSubtypes())
		{
			return getRegistryName().toString();
		}

		return getRegistryName().toString() + ":" + meta;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj == null || !(obj instanceof BlockMeta))
		{
			return false;
		}

		BlockMeta blockMeta = (BlockMeta)obj;

		if (block != blockMeta.block)
		{
			return false;
		}
		else if (meta < 0 || meta == OreDictionary.WILDCARD_VALUE || blockMeta.meta < 0 || blockMeta.meta == OreDictionary.WILDCARD_VALUE)
		{
			return true;
		}
		else if (!Item.getItemFromBlock(block).getHasSubtypes() && !Item.getItemFromBlock(blockMeta.block).getHasSubtypes())
		{
			return true;
		}

		return meta == blockMeta.meta;
	}

	@Override
	public int hashCode()
	{
		if (meta < 0 || meta == OreDictionary.WILDCARD_VALUE || !Item.getItemFromBlock(block).getHasSubtypes())
		{
			return block.hashCode();
		}

		return Objects.hashCode(block, meta);
	}

	@Override
	public int compareTo(BlockMeta blockMeta)
	{
		int i = CaveUtils.compareWithNull(this, blockMeta);

		if (i == 0 && blockMeta != null)
		{
			i = getRegistryName().compareTo(blockMeta.getRegistryName());

			if (i == 0)
			{
				i = Integer.compare(meta, blockMeta.meta);
			}
		}

		return i;
	}
}