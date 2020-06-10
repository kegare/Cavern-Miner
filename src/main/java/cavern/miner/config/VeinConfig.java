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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.CavernMod;
import cavern.miner.config.json.BlockStateTagListSerializer;
import cavern.miner.config.json.VeinSerializer;
import cavern.miner.util.BlockStateTagList;
import cavern.miner.vein.Vein;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.Tags;

public class VeinConfig
{
	private final NonNullList<Vein> veins = NonNullList.create();

	private final BlockStateTagList whitelist = BlockStateTagList.create();
	private final BlockStateTagList blacklist = BlockStateTagList.create();

	private final File file;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public VeinConfig(File dir)
	{
		this.file = new File(dir, "veins.json");
	}

	public VeinConfig(File dir, String name)
	{
		this.file = new File(dir, name + "_veins.json");
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

	public File getFile()
	{
		return file;
	}

	public boolean loadFromFile()
	{
		try
		{
			if (file.getParentFile() != null)
			{
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return false;
			}

			if (file.canRead() && file.length() > 0L)
			{
				FileInputStream fis = new FileInputStream(file);
				BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));

				fromJson(buffer);

				buffer.close();
				fis.close();

				return true;
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to load veins", e);
		}

		return false;
	}

	public boolean saveToFile()
	{
		try
		{
			if (file.getParentFile() != null)
			{
				file.getParentFile().mkdirs();
			}

			if (!file.exists() && !file.createNewFile())
			{
				return false;
			}

			if (file.canWrite())
			{
				FileOutputStream fos = new FileOutputStream(file);
				BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos));

				buffer.write(Strings.nullToEmpty(toJson()));

				buffer.close();
				fos.close();

				return true;
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to save veins", e);
		}

		return false;
	}

	@Nullable
	public String toJson()
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

		return gson.toJson(object);
	}

	public void fromJson(Reader json)
	{
		try
		{
			JsonObject object = gson.fromJson(json, JsonObject.class);

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

				if (e != null && e.isJsonObject())
				{
					BlockStateTagList entries = BlockStateTagListSerializer.INSTANCE.deserialize(e, BlockState.class, null);

					if (!entries.isEmpty())
					{
						setWhitelist(entries);
					}
				}

				e = o.get("blacklist");

				if (e != null && e.isJsonObject())
				{
					BlockStateTagList entries = BlockStateTagListSerializer.INSTANCE.deserialize(e, BlockState.class, null);

					if (!entries.isEmpty())
					{
						setBlacklist(entries);
					}
				}
			}
		}
		catch (JsonParseException e)
		{
			CavernMod.LOG.error("Failed to read from json", e);
		}
	}

	public void setDefault()
	{
		veins.clear();

		whitelist.clear();
		whitelist.add(Tags.Blocks.ORES).add(Tags.Blocks.STONE).add(Tags.Blocks.GRAVEL).add(Blocks.DIRT);

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