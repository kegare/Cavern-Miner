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
import java.util.Iterator;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import cavern.miner.CavernMod;
import cavern.miner.config.json.MinerRankSerializer;
import cavern.miner.init.CaveItems;
import cavern.miner.storage.MinerRank;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;

public class MinerRankConfig
{
	private final NonNullList<MinerRank.RankEntry> entries = NonNullList.create();

	private final File file = new File(CavernModConfig.getConfigDir(), "miner_ranks.json");
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public boolean setEntries(Collection<MinerRank.RankEntry> collection)
	{
		entries.clear();

		return entries.addAll(collection);
	}

	public NonNullList<MinerRank.RankEntry> getEntries()
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
			CavernMod.LOG.error("Failed to load miner ranks", e);
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
			CavernMod.LOG.error("Failed to save miner ranks", e);
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

		for (MinerRank.RankEntry entry : entries)
		{
			JsonElement e = MinerRankSerializer.INSTANCE.serialize(entry, entry.getClass(), null);

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

				MinerRank.RankEntry entry = MinerRankSerializer.INSTANCE.deserialize(e, MinerRank.RankEntry.class, null);

				entries.add(entry);
			}
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Failed to read from json", e);
		}
	}

	public void registerEntries()
	{
		Iterator<MinerRank.RankEntry> iterator = entries.iterator();

		while (iterator.hasNext())
		{
			MinerRank.RankEntry entry = iterator.next();

			if (!MinerRank.add(entry))
			{
				iterator.remove();
			}
		}
	}

	public void setDefault()
	{
		entries.clear();
		entries.add(new MinerRank.RankEntry("STONE", 300, new ItemStack(Items.STONE_PICKAXE)));
		entries.add(new MinerRank.RankEntry("IRON", 1000, new ItemStack(Items.IRON_PICKAXE)));
		entries.add(new MinerRank.RankEntry("MAGNITE", 1000, new ItemStack(CaveItems.MAGNITE_PICKAXE.get())));
		entries.add(new MinerRank.RankEntry("GOLD", 5000, new ItemStack(Items.GOLDEN_PICKAXE)));
		entries.add(new MinerRank.RankEntry("AQUA", 10000, new ItemStack(CaveItems.AQUAMARINE_PICKAXE.get())));
		entries.add(new MinerRank.RankEntry("DIAMOND", 50000, new ItemStack(Items.DIAMOND_PICKAXE)));
	}
}