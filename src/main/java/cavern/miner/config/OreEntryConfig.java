package cavern.miner.config;

import java.io.File;
import java.io.Reader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import cavern.miner.config.json.OreEntrySerializer;
import cavern.miner.init.CaveBlocks;
import cavern.miner.init.CaveTags;
import cavern.miner.world.vein.OreRegistry;
import cavern.miner.world.vein.VeinProvider;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.Tags;

public class OreEntryConfig extends AbstractEntryConfig
{
	private final NonNullList<OreRegistry.OreEntry> entries = NonNullList.create();

	public OreEntryConfig()
	{
		super(new File(CavernModConfig.getConfigDir(), "ore_entries.json"));
	}

	public NonNullList<OreRegistry.OreEntry> getEntries()
	{
		return entries;
	}

	@Override
	public boolean isEmpty()
	{
		return entries.isEmpty();
	}

	@Override
	public String toJson() throws JsonParseException
	{
		if (entries.isEmpty())
		{
			return null;
		}

		JsonArray array = new JsonArray();

		for (OreRegistry.OreEntry entry : entries)
		{
			JsonElement e = OreEntrySerializer.INSTANCE.serialize(entry, entry.getClass(), null);

			if (e.isJsonNull() || e.toString().isEmpty())
			{
				continue;
			}

			array.add(e);
		}

		return getGson().toJson(array);
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonArray array = getGson().fromJson(json, JsonArray.class);

		if (array.size() == 0)
		{
			return;
		}

		entries.clear();

		for (JsonElement e : array)
		{
			if (e.isJsonNull() || !e.isJsonObject() || e.toString().isEmpty())
			{
				continue;
			}

			OreRegistry.OreEntry entry = OreEntrySerializer.INSTANCE.deserialize(e, e.getClass(), null);

			if (entry != OreRegistry.OreEntry.EMPTY)
			{
				entries.add(entry);
			}
		}
	}

	@Override
	public void setToDefault()
	{
		entries.clear();

		entries.add(new OreRegistry.TagEntry(Tags.Blocks.ORES_COAL, VeinProvider.Rarity.COMMON, 1));
		entries.add(new OreRegistry.TagEntry(Tags.Blocks.ORES_IRON, VeinProvider.Rarity.COMMON, 1));
		entries.add(new OreRegistry.TagEntry(Tags.Blocks.ORES_GOLD, VeinProvider.Rarity.RARE, 2));
		entries.add(new OreRegistry.TagEntry(Tags.Blocks.ORES_REDSTONE, VeinProvider.Rarity.UNCOMMON, 2));
		entries.add(new OreRegistry.TagEntry(Tags.Blocks.ORES_LAPIS, VeinProvider.Rarity.RARE, 2));
		entries.add(new OreRegistry.TagEntry(Tags.Blocks.ORES_EMERALD, VeinProvider.Rarity.EMERALD, 3));
		entries.add(new OreRegistry.TagEntry(Tags.Blocks.ORES_DIAMOND, VeinProvider.Rarity.DIAMOND, 5));

		entries.add(new OreRegistry.TagEntry(CaveTags.Blocks.ORES_MAGNITE, VeinProvider.Rarity.COMMON, 1));
		entries.add(new OreRegistry.TagEntry(CaveTags.Blocks.ORES_AQUAMARINE, VeinProvider.Rarity.AQUA, 2));
		entries.add(new OreRegistry.TagEntry(CaveTags.Blocks.ORES_RANDOMITE, VeinProvider.Rarity.RANDOMITE, 2));

		entries.add(new OreRegistry.BlockEntry(CaveBlocks.CRACKED_STONE.get(), VeinProvider.Rarity.RANDOMITE, 2));
	}

	@Override
	public void load()
	{
		super.load();

		OreRegistry.clear();

		for (OreRegistry.OreEntry entry : entries)
		{
			if (entry instanceof OreRegistry.BlockEntry)
			{
				OreRegistry.registerBlock((OreRegistry.BlockEntry)entry);
			}

			if (entry instanceof OreRegistry.BlockStateEntry)
			{
				OreRegistry.registerBlockState((OreRegistry.BlockStateEntry)entry);
			}

			if (entry instanceof OreRegistry.TagEntry)
			{
				OreRegistry.registerTag((OreRegistry.TagEntry)entry);
			}
		}
	}
}