package cavern.miner.config;

import java.io.File;
import java.io.Reader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import cavern.miner.config.json.MinerRankSerializer;
import cavern.miner.init.CaveItems;
import cavern.miner.storage.MinerRank;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;

public class MinerRankConfig extends AbstractEntryConfig
{
	private final NonNullList<MinerRank.RankEntry> entries = NonNullList.create();

	public MinerRankConfig()
	{
		super(new File(CavernModConfig.getConfigDir(), "miner_ranks.json"));
	}

	public NonNullList<MinerRank.RankEntry> getEntries()
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

		for (MinerRank.RankEntry entry : entries)
		{
			JsonElement e = MinerRankSerializer.INSTANCE.serialize(entry, entry.getClass(), null);

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

			MinerRank.RankEntry entry = MinerRankSerializer.INSTANCE.deserialize(e, e.getClass(), null);

			entries.add(entry);
		}
	}

	@Override
	public void setToDefault()
	{
		entries.clear();
		entries.add(new MinerRank.RankEntry("STONE", 300, new ItemStack(Items.STONE_PICKAXE)));
		entries.add(new MinerRank.RankEntry("IRON", 1000, new ItemStack(Items.IRON_PICKAXE)));
		entries.add(new MinerRank.RankEntry("MAGNITE", 3000, new ItemStack(CaveItems.MAGNITE_PICKAXE.get())));
		entries.add(new MinerRank.RankEntry("GOLD", 5000, new ItemStack(Items.GOLDEN_PICKAXE)));
		entries.add(new MinerRank.RankEntry("AQUA", 10000, new ItemStack(CaveItems.AQUAMARINE_PICKAXE.get())));
		entries.add(new MinerRank.RankEntry("DIAMOND", 50000, new ItemStack(Items.DIAMOND_PICKAXE)));
	}

	@Override
	public void load()
	{
		super.load();

		MinerRank.load(entries);
	}
}