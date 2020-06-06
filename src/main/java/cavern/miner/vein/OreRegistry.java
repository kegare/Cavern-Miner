package cavern.miner.vein;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import cavern.miner.util.BlockStateHelper;
import cavern.miner.world.VeinProvider;
import cavern.miner.world.VeinProvider.Rarity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public final class OreRegistry
{
	private static final Map<Block, BlockEntry> BLOCK_ENTRIES = Maps.newHashMap();
	private static final Map<BlockState, BlockStateEntry> BLOCK_STATE_ENTRIES = Maps.newHashMap();
	private static final Map<ResourceLocation, TagEntry> TAG_ENTRIES = Maps.newHashMap();

	private OreRegistry() {}

	public static void registerBlock(BlockEntry entry)
	{
		BLOCK_ENTRIES.put(entry.getBlock(), entry);
	}

	public static void registerBlockState(BlockStateEntry entry)
	{
		BLOCK_STATE_ENTRIES.put(entry.getBlockState(), entry);
	}

	public static void registerTag(TagEntry entry)
	{
		TAG_ENTRIES.put(entry.getTag().getId(), entry);
	}

	public static OreEntry getEntry(Object obj)
	{
		return getEntry(obj, false);
	}

	public static OreEntry getEntry(Object obj, boolean strict)
	{
		if (obj == null)
		{
			return null;
		}

		if (obj instanceof OreEntry)
		{
			OreEntry entry = (OreEntry)obj;
			OreEntry parent = getEntry(entry.getParent(), true);

			if (parent != null)
			{
				return parent;
			}

			return entry;
		}

		if (obj instanceof Block)
		{
			Block block = (Block)obj;
			OreEntry entry = BLOCK_ENTRIES.get(block);

			if (entry != null)
			{
				return getEntry(entry, true);
			}

			if (strict)
			{
				return OreEntry.EMPTY;
			}

			for (BlockStateEntry blockStateEntry : BLOCK_STATE_ENTRIES.values())
			{
				if (blockStateEntry.getBlockState().getBlock() == block)
				{
					return getEntry(blockStateEntry, true);
				}
			}

			for (TagEntry tagEntry : TAG_ENTRIES.values())
			{
				if (block.isIn(tagEntry.getTag()))
				{
					return getEntry(tagEntry, true);
				}
			}
		}

		if (obj instanceof BlockState)
		{
			BlockState state = (BlockState)obj;
			OreEntry entry = BLOCK_STATE_ENTRIES.get(obj);

			if (entry != null)
			{
				return getEntry(entry, true);
			}

			if (strict)
			{
				return OreEntry.EMPTY;
			}

			for (BlockStateEntry blockStateEntry : BLOCK_STATE_ENTRIES.values())
			{
				if (BlockStateHelper.equals(blockStateEntry.getBlockState(), state))
				{
					return getEntry(blockStateEntry, true);
				}
			}

			for (BlockEntry blockEntry : BLOCK_ENTRIES.values())
			{
				if (blockEntry.getBlock() == state.getBlock())
				{
					return getEntry(blockEntry, true);
				}
			}

			for (TagEntry tagEntry : TAG_ENTRIES.values())
			{
				if (state.getBlock().isIn(tagEntry.getTag()))
				{
					return getEntry(tagEntry, true);
				}
			}
		}

		if (obj instanceof Tag<?>)
		{
			Tag<?> tag = (Tag<?>)obj;
			OreEntry entry = TAG_ENTRIES.get(tag.getId());

			if (entry != null)
			{
				return getEntry(entry, true);
			}

			if (strict)
			{
				return OreEntry.EMPTY;
			}

			for (BlockEntry blockEntry : BLOCK_ENTRIES.values())
			{
				if (tag.getAllElements().contains(blockEntry.getBlock()))
				{
					return getEntry(blockEntry, true);
				}
			}

			for (BlockStateEntry blockStateEntry : BLOCK_STATE_ENTRIES.values())
			{
				if (tag.getAllElements().contains(blockStateEntry.getBlockState().getBlock()))
				{
					return getEntry(blockStateEntry, true);
				}
			}
		}

		return OreEntry.EMPTY;
	}

	public interface OreEntry
	{
		static final OreEntry EMPTY = new EmptyEntry();

		@Nullable
		Object getParent();

		@Nullable
		VeinProvider.Rarity getRarity();

		@Nullable
		Integer getPoint();
	}

	private static class EmptyEntry implements OreEntry
	{
		@Override
		public Object getParent()
		{
			return null;
		}

		@Override
		public Rarity getRarity()
		{
			return null;
		}

		@Override
		public Integer getPoint()
		{
			return null;
		}
	}

	public static class BlockEntry implements OreEntry
	{
		private final Block block;
		private final Object parentEntry;

		private final VeinProvider.Rarity rarity;
		private final Integer point;

		public BlockEntry(Block block, Object parent)
		{
			this.block = block;
			this.parentEntry = parent;
			this.rarity = null;
			this.point = null;
		}

		public BlockEntry(Block block, VeinProvider.Rarity rarity, Integer point)
		{
			this.block = block;
			this.parentEntry = null;
			this.rarity = rarity;
			this.point = point;
		}

		public Block getBlock()
		{
			return block;
		}

		@Override
		public Object getParent()
		{
			return parentEntry;
		}

		@Override
		public VeinProvider.Rarity getRarity()
		{
			return rarity;
		}

		@Override
		public Integer getPoint()
		{
			return point;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
			{
				return false;
			}

			if (obj == this)
			{
				return true;
			}

			if (obj instanceof BlockEntry)
			{
				BlockEntry entry = (BlockEntry)obj;

				return getBlock() == entry.getBlock();
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return getBlock().hashCode();
		}
	}

	public static class BlockStateEntry implements OreEntry
	{
		private final BlockState blockState;
		private final OreEntry parentEntry;

		private final VeinProvider.Rarity rarity;
		private final Integer point;

		public BlockStateEntry(BlockState state, OreEntry parent)
		{
			this.blockState = state;
			this.parentEntry = parent;
			this.rarity = null;
			this.point = null;
		}

		public BlockStateEntry(BlockState state, VeinProvider.Rarity rarity, Integer point)
		{
			this.blockState = state;
			this.parentEntry = null;
			this.rarity = rarity;
			this.point = point;
		}

		public BlockState getBlockState()
		{
			return blockState;
		}

		@Override
		public OreEntry getParent()
		{
			return parentEntry;
		}

		@Override
		public VeinProvider.Rarity getRarity()
		{
			return rarity;
		}

		@Override
		public Integer getPoint()
		{
			return point;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
			{
				return false;
			}

			if (obj == this)
			{
				return true;
			}

			if (obj instanceof BlockStateEntry)
			{
				BlockStateEntry entry = (BlockStateEntry)obj;

				return BlockStateHelper.equals(getBlockState(), entry.getBlockState());
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(getBlockState().getBlock(), getBlockState().getValues().hashCode());
		}
	}

	public static class TagEntry implements OreEntry
	{
		private final Tag<Block> tag;
		private final OreEntry parentEntry;

		private final VeinProvider.Rarity rarity;
		private final Integer point;

		public TagEntry(Tag<Block> tag, OreEntry parent)
		{
			this.tag = tag;
			this.parentEntry = parent;
			this.rarity = null;
			this.point = null;
		}

		public TagEntry(Tag<Block> tag, VeinProvider.Rarity rarity, Integer point)
		{
			this.tag = tag;
			this.parentEntry = null;
			this.rarity = rarity;
			this.point = point;
		}

		public Tag<Block> getTag()
		{
			return tag;
		}

		@Override
		public OreEntry getParent()
		{
			return parentEntry;
		}

		@Override
		public VeinProvider.Rarity getRarity()
		{
			return rarity;
		}

		@Override
		public Integer getPoint()
		{
			return point;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
			{
				return false;
			}

			if (obj == this)
			{
				return true;
			}

			if (obj instanceof TagEntry)
			{
				TagEntry entry = (TagEntry)obj;

				return getTag().getId().equals(entry.getTag().getId());
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return getTag().getId().hashCode();
		}
	}
}