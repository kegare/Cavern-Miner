package cavern.miner.init;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public final class CaveTags
{
	public static class Blocks
	{
		public static final Tag<Block> ORES_MAGNITE = tag("ores/magnite");
		public static final Tag<Block> ORES_AQUAMARINE = tag("ores/aquamarine");
		public static final Tag<Block> ORES_RANDOMITE = tag("ores/randomite");

		public static final Tag<Block> STORAGE_BLOCKS_MAGNITE = tag("storage_blocks/magnite");
		public static final Tag<Block> STORAGE_BLOCKS_AQUAMARINE = tag("storage_blocks/aquamarine");

		private static Tag<Block> tag(String name)
		{
			return new BlockTags.Wrapper(new ResourceLocation("forge", name));
		}
	}

	public static class Items
	{
		public static final Tag<Item> ORES_MAGNITE = tag("ores/magnite");
		public static final Tag<Item> ORES_AQUAMARINE = tag("ores/aquamarine");
		public static final Tag<Item> ORES_RANDOMITE = tag("ores/randomite");

		public static final Tag<Item> STORAGE_BLOCKS_MAGNITE = tag("storage_blocks/magnite");
		public static final Tag<Item> STORAGE_BLOCKS_AQUAMARINE = tag("storage_blocks/aquamarine");

		public static final Tag<Item> INGOTS_MAGNITE = tag("ingots/magnite");

		public static final Tag<Item> NUGGETS_MAGNITE = tag("nuggets/magnite");

		public static final Tag<Item> GEMS_AQUAMARINE = tag("gems/aquamarine");

		private static Tag<Item> tag(String name)
		{
			return new ItemTags.Wrapper(new ResourceLocation("forge", name));
		}
	}
}