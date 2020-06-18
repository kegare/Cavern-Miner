package cavern.miner.config.dimension;

import java.io.File;
import java.io.Reader;
import java.util.Collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.config.AbstractEntryConfig;
import cavern.miner.config.CavernModConfig;
import cavern.miner.config.json.BlockStateTagListSerializer;
import cavern.miner.config.json.VeinSerializer;
import cavern.miner.util.BlockStateTagList;
import cavern.miner.world.vein.Vein;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.Tags;

public class VeinConfig extends AbstractEntryConfig
{
	private final NonNullList<Vein> veins = NonNullList.create();

	private final BlockStateTagList whitelist = BlockStateTagList.create();
	private final BlockStateTagList blacklist = BlockStateTagList.create();

	public VeinConfig(File dir)
	{
		super(new File(dir, "veins.json"));
	}

	public VeinConfig(File dir, String name)
	{
		super(new File(dir, name + "_veins.json"));
	}

	public boolean setVeins(Collection<Vein> entries)
	{
		veins.clear();

		return veins.addAll(entries);
	}

	public NonNullList<Vein> getVeins()
	{
		return veins;
	}

	public boolean setWhitelist(BlockStateTagList entries)
	{
		whitelist.clear();

		return whitelist.addEntries(entries.getEntryList()) && whitelist.addTags(entries.getTagList());
	}

	public BlockStateTagList getWhitelist()
	{
		return whitelist;
	}

	public boolean setBlacklist(BlockStateTagList entries)
	{
		blacklist.clear();

		return blacklist.addEntries(entries.getEntryList()) && blacklist.addTags(entries.getTagList());
	}

	public BlockStateTagList getBlacklist()
	{
		return blacklist;
	}

	@Override
	public String toJson() throws JsonParseException
	{
		JsonObject object = new JsonObject();

		JsonArray array = new JsonArray();

		for (Vein vein : veins)
		{
			JsonElement e = VeinSerializer.INSTANCE.serialize(vein, vein.getClass(), null);

			if (e.isJsonNull() || e.toString().isEmpty())
			{
				continue;
			}

			array.add(e);
		}

		object.add("veins", array);

		JsonObject o = new JsonObject();

		o.add("whitelist", BlockStateTagListSerializer.INSTANCE.serialize(whitelist, BlockState.class, null));
		o.add("blacklist", BlockStateTagListSerializer.INSTANCE.serialize(blacklist, BlockState.class, null));

		object.add("auto_entries", o);

		return getGson().toJson(object);
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonObject object = getGson().fromJson(json, JsonObject.class);

		if (object.size() == 0)
		{
			return;
		}

		veins.clear();
		whitelist.clear();
		blacklist.clear();

		JsonElement e = object.get("veins");

		if (e != null && e.isJsonArray())
		{
			JsonArray array = e.getAsJsonArray();

			for (JsonElement o : array)
			{
				if (o.isJsonNull() || !o.isJsonObject() || o.toString().isEmpty())
				{
					continue;
				}

				Vein vein = VeinSerializer.INSTANCE.deserialize(o, Vein.class, null);

				if (vein.getBlockState().getBlock() instanceof AirBlock)
				{
					continue;
				}

				if (vein.getCount() <= 0 || vein.getSize() <= 0)
				{
					continue;
				}

				veins.add(vein);
			}

		}

		e = object.get("auto_entries");

		if (e != null && e.isJsonObject())
		{
			JsonObject o = e.getAsJsonObject();

			if (o.size() == 0)
			{
				return;
			}

			e = o.get("whitelist");

			whitelist.clear();

			if (e != null && e.isJsonObject())
			{
				BlockStateTagList entries = BlockStateTagListSerializer.INSTANCE.deserialize(e, e.getClass(), null);

				if (!entries.getEntryList().isEmpty())
				{
					whitelist.addEntries(entries.getEntryList());
				}

				if (!entries.getTagList().isEmpty())
				{
					whitelist.addTags(entries.getTagList());
				}
			}

			e = o.get("blacklist");

			blacklist.clear();

			if (e != null && e.isJsonObject())
			{
				BlockStateTagList entries = BlockStateTagListSerializer.INSTANCE.deserialize(e, e.getClass(), null);

				if (!entries.getEntryList().isEmpty())
				{
					blacklist.addEntries(entries.getEntryList());
				}

				if (!entries.getTagList().isEmpty())
				{
					blacklist.addTags(entries.getTagList());
				}
			}
		}
	}

	public void setDefault()
	{
		veins.clear();

		whitelist.clear();
		whitelist.add(Tags.Blocks.ORES).add(Tags.Blocks.STONE).add(Blocks.DIRT).add(Blocks.GRAVEL);

		blacklist.clear();
		blacklist.add(Blocks.STONE).add(Blocks.POLISHED_ANDESITE).add(Blocks.POLISHED_DIORITE).add(Blocks.POLISHED_GRANITE);
		blacklist.add(Tags.Blocks.ORES_QUARTZ);
	}

	public static void createExampleConfig()
	{
		VeinConfig config = new VeinConfig(CavernModConfig.getConfigDir(), "example");

		config.setDefault();

		config.getVeins().add(new Vein(Blocks.COAL_ORE.getDefaultState(), new Vein.Properties().count(20).size(10)));
		config.getVeins().add(new Vein(Blocks.SAND.getDefaultState(), new Vein.Properties().target(Blocks.DIRT.getDefaultState()).count(30).size(15).min(30)));
		config.getVeins().add(new Vein(Blocks.ACACIA_LOG.getDefaultState().with(RotatedPillarBlock.AXIS, Direction.Axis.Z), new Vein.Properties().target(Blocks.ACACIA_PLANKS.getDefaultState())));

		config.saveToFile();
	}
}