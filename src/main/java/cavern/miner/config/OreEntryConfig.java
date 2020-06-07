package cavern.miner.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import cavern.miner.CavernMod;
import cavern.miner.config.json.OreEntrySerializer;
import cavern.miner.init.CaveBlocks;
import cavern.miner.init.CaveTags;
import cavern.miner.vein.OreRegistry;
import cavern.miner.world.VeinProvider;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.Tags;

public class OreEntryConfig
{
	public static final OreEntryConfig INSTANCE = new OreEntryConfig();

	private final NonNullList<OreRegistry.OreEntry> entries = NonNullList.create();

	private final File file = new File(CavernModConfig.getConfigDir(), "ore_entries.json");
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public boolean setEntries(Collection<OreRegistry.OreEntry> collection)
	{
		entries.clear();

		return entries.addAll(collection);
	}

	public NonNullList<OreRegistry.OreEntry> getEntries()
	{
		return entries;
	}

	public File getFile()
	{
		return file;
	}

	public void loadFromFile()
	{
		try
		{
			if (file.getParentFile() != null)
			{
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return;
			}

			if (file.canRead())
			{
				FileInputStream fis = new FileInputStream(file);
				BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));

				fromJson(buffer);

				buffer.close();
				fis.close();
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to load ore entries", e);
		}
	}

	public void saveToFile()
	{
		try
		{
			if (file.getParentFile() != null)
			{
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return;
			}

			if (file.canWrite())
			{
				FileOutputStream fos = new FileOutputStream(file);
				BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos));

				buffer.write(Strings.nullToEmpty(toJson()));

				buffer.close();
				fos.close();
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to save ore entries", e);
		}
	}

	@Nullable
	public String toJson()
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

		return gson.toJson(array);
	}

	public void fromJson(Reader json)
	{
		try
		{
			JsonArray array = gson.fromJson(json, JsonArray.class);

			if (array.size() == 0)
			{
				return;
			}

			entries.clear();

			for (JsonElement e : array)
			{
				if (e.isJsonNull() || e.toString().isEmpty())
				{
					continue;
				}

				OreRegistry.OreEntry entry = OreEntrySerializer.INSTANCE.deserialize(e, OreRegistry.OreEntry.class, null);

				if (entry != OreRegistry.OreEntry.EMPTY)
				{
					entries.add(entry);
				}
			}
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Failed to read from json", e);
		}
	}

	public void registerEntries()
	{
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

	public void setDefault()
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

	public static void loadConfig()
	{
		INSTANCE.loadFromFile();

		if (INSTANCE.getEntries().isEmpty())
		{
			INSTANCE.setDefault();
			INSTANCE.saveToFile();
		}

		INSTANCE.registerEntries();
	}
}