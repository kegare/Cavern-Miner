package cavern.miner.config;

import java.io.File;
import java.io.Reader;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import cavern.miner.block.RandomiteDrop;
import cavern.miner.config.json.RandomiteDropSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.Tags;

public class RandomiteDropConfig extends AbstractEntryConfig
{
	private final NonNullList<RandomiteDrop.DropEntry> entries = NonNullList.create();

	public RandomiteDropConfig()
	{
		super(new File(CavernModConfig.getConfigDir(), "randomite_drops.json"));
	}

	public NonNullList<RandomiteDrop.DropEntry> getEntries()
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

		for (RandomiteDrop.DropEntry entry : entries)
		{
			JsonElement e = RandomiteDropSerializer.INSTANCE.serialize(entry, entry.getClass(), null);

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

			RandomiteDrop.DropEntry entry = RandomiteDropSerializer.INSTANCE.deserialize(e, e.getClass(), null);

			if (entry != RandomiteDrop.EMPTY)
			{
				entries.add(entry);
			}
		}
	}

	@Override
	public void setToDefault()
	{
		entries.clear();
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.INGOTS, 20, 1, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.NUGGETS, 20, 1, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.GEMS, 10, 1, 2));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.DUSTS, 15, 3, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.RODS, 15, 1, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.ENDER_PEARLS, 15, 1, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.BONES, 20, 2, 3));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.GUNPOWDER, 20, 2, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.STRING, 20, 2, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.SEEDS, 20, 3, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.CROPS, 20, 3, 5));
		entries.add(new RandomiteDrop.TagEntry(Tags.Items.DYES, 10, 2, 5));
	}

	public ItemStack getRandomDropItem(Random random)
	{
		if (entries.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		return WeightedRandom.getRandomItem(random, entries).getDropItem();
	}
}