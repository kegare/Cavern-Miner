package cavern.miner.util;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.base.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;

public class BlockMeta implements Comparable<BlockMeta>
{
	private final Block block;
	private final int meta;

	private IBlockState stateCache;

	public BlockMeta(Block block, int meta)
	{
		this.block = block;
		this.meta = meta;
	}

	public BlockMeta(IBlockState state)
	{
		this(state.getBlock(), state.getBlock().getMetaFromState(state));
		this.stateCache = state;
	}

	public BlockMeta(String name, int meta)
	{
		this(name, Blocks.AIR, meta);
	}

	public BlockMeta(String name, Block defaultValue, int meta)
	{
		this(ObjectUtils.defaultIfNull(Block.getBlockFromName(name), defaultValue), meta);
	}

	@Nonnull
	public Block getBlock()
	{
		return block;
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

	public String getBlockName()
	{
		return block.getRegistryName().toString();
	}

	@Override
	public String toString()
	{
		String name = getBlockName();

		if (meta < 0 || meta == OreDictionary.WILDCARD_VALUE || !Item.getItemFromBlock(block).getHasSubtypes())
		{
			return name;
		}

		return name + ":" + meta;
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
			i = toString().compareTo(blockMeta.toString());
		}

		return i;
	}
}