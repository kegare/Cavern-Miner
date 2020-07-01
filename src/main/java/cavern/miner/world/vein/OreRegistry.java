package cavern.miner.world.vein;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import com.google.common.base.Objects;

import cavern.miner.util.BlockStateHelper;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public final class OreRegistry
{
	private static final Map<Block, BlockEntry> BLOCK_ENTRIES = new HashMap<>();
	private static final Map<BlockState, BlockStateEntry> BLOCK_STATE_ENTRIES = new HashMap<>();
	private static final Map<ResourceLocation, TagEntry> TAG_ENTRIES = new HashMap<>();

	private OreRegistry() {}

	public static void registerBlock(BlockEntry entry)
	{
		if (entry.getBlock() instanceof AirBlock)
		{
			return;
		}

		BLOCK_ENTRIES.put(entry.getBlock(), entry);
	}

	public static void registerBlockState(BlockStateEntry entry)
	{
		if (entry.getBlockState().getBlock() instanceof AirBlock)
		{
			return;
		}

		BLOCK_STATE_ENTRIES.put(entry.getBlockState(), entry);
	}

	public static void registerTag(TagEntry entry)
	{
		TAG_ENTRIES.put(entry.getTag().getId(), entry);
	}

	public static void clear()
	{
		BLOCK_ENTRIES.clear();
		BLOCK_STATE_ENTRIES.clear();
		TAG_ENTRIES.clear();
	}

	public static OreEntry getEntry(Object obj)
	{
		return getEntry(obj, false);
	}

	public static OreEntry getEntry(Object obj, boolean strict)
	{
		if (obj == null || obj == OreEntry.EMPTY)
		{
			return OreEntry.EMPTY;
		}

		if (obj instanceof OreEntry)
		{
			OreEntry entry = (OreEntry)obj;
			OreEntry parent = getEntry(entry.getParent().orElse(OreEntry.EMPTY), true);

			if (parent != OreEntry.EMPTY)
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

		Optional<Object> getParent();

		Optional<OreRarity> getRarity();

		OptionalInt getPoint();
	}

	private static class EmptyEntry implements OreEntry
	{
		@Override
		public Optional<Object> getParent()
		{
			return Optional.empty();
		}

		@Override
		public Optional<OreRarity> getRarity()
		{
			return Optional.empty();
		}

		@Override
		public OptionalInt getPoint()
		{
			return OptionalInt.empty();
		}
	}

	public static class BlockEntry implements OreEntry
	{
		private final Block block;
		private final Object parentEntry;

		private final OreRarity rarity;
		private final int point;

		public BlockEntry(Block block, Object parent)
		{
			this.block = block;
			this.parentEntry = parent;
			this.rarity = null;
			this.point = 0;
		}

		public BlockEntry(Block block, OreRarity rarity, int point)
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
		public Optional<Object> getParent()
		{
			return Optional.ofNullable(parentEntry);
		}

		@Override
		public Optional<OreRarity> getRarity()
		{
			return Optional.ofNullable(rarity);
		}

		@Override
		public OptionalInt getPoint()
		{
			return point == 0 ? OptionalInt.empty() : OptionalInt.of(point);
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

		private final OreRarity rarity;
		private final int point;

		public BlockStateEntry(BlockState state, OreEntry parent)
		{
			this.blockState = state;
			this.parentEntry = parent;
			this.rarity = null;
			this.point = 0;
		}

		public BlockStateEntry(BlockState state, OreRarity rarity, int point)
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
		public Optional<Object> getParent()
		{
			return Optional.ofNullable(parentEntry);
		}

		@Override
		public Optional<OreRarity> getRarity()
		{
			return Optional.ofNullable(rarity);
		}

		@Override
		public OptionalInt getPoint()
		{
			return point == 0 ? OptionalInt.empty() : OptionalInt.of(point);
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

		private final OreRarity rarity;
		private final int point;

		public TagEntry(Tag<Block> tag, OreEntry parent)
		{
			this.tag = tag;
			this.parentEntry = parent;
			this.rarity = null;
			this.point = 0;
		}

		public TagEntry(Tag<Block> tag, OreRarity rarity, int point)
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
		public Optional<Object> getParent()
		{
			return Optional.ofNullable(parentEntry);
		}

		@Override
		public Optional<OreRarity> getRarity()
		{
			return Optional.ofNullable(rarity);
		}

		@Override
		public OptionalInt getPoint()
		{
			return point == 0 ? OptionalInt.empty() : OptionalInt.of(point);
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