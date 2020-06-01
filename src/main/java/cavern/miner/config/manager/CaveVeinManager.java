package cavern.miner.config.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import cavern.miner.block.BlockCave;
import cavern.miner.block.CaveBlocks;
import cavern.miner.config.Config;
import cavern.miner.core.CavernMod;
import cavern.miner.util.BlockMeta;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.oredict.OreDictionary;

public class CaveVeinManager implements JsonSerializer<CaveVein>, JsonDeserializer<CaveVein>
{
	private final List<CaveVein> veins = Lists.newArrayList();

	private final File file;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(CaveVein.class, this).create();

	public CaveVeinManager(String name)
	{
		this.file = new File(Config.getConfigDir(), name + "_veins.json");
	}

	public boolean setCaveVeins(Collection<CaveVein> vein)
	{
		veins.clear();

		return veins.addAll(vein);
	}

	public List<CaveVein> getCaveVeins()
	{
		return veins;
	}

	public void addCaveVein(CaveVein vein)
	{
		veins.add(vein);
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
				veins.clear();

				FileInputStream fis = new FileInputStream(file);
				BufferedReader buffer = new BufferedReader(new InputStreamReader(fis));

				fromJson(buffer);

				buffer.close();
				fis.close();
			}
		}
		catch (IOException e)
		{
			CavernMod.LOG.error("Failed to load veins", e);
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
			CavernMod.LOG.error("Failed to save veins", e);
		}
	}

	@Nullable
	public String toJson()
	{
		if (veins.isEmpty())
		{
			return null;
		}

		return gson.toJson(veins);
	}

	public boolean fromJson(Reader json)
	{
		try
		{
			Collection<CaveVein> entries = gson.fromJson(json, new TypeToken<Collection<CaveVein>>(){}.getType());

			if (entries == null || entries.isEmpty())
			{
				return false;
			}

			return veins.addAll(entries);
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Failed to read from json", e);

			return false;
		}
	}

	@Override
	public JsonElement serialize(CaveVein src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject entry = new JsonObject();

		JsonObject sub = new JsonObject();
		BlockMeta blockMeta = src.getBlockMeta();
		sub.addProperty("name", blockMeta.getBlock().getRegistryName().toString());
		sub.addProperty("meta", blockMeta.getMeta());
		entry.add("block", sub);

		sub = new JsonObject();
		blockMeta = src.getTarget();
		sub.addProperty("name", blockMeta.getBlock().getRegistryName().toString());
		sub.addProperty("meta", blockMeta.getMeta());
		entry.add("target_block", sub);

		entry.addProperty("weight", src.getWeight());
		entry.addProperty("size", src.getSize());

		sub = new JsonObject();
		sub.addProperty("min", src.getMinHeight());
		sub.addProperty("max", src.getMaxHeight());
		entry.add("height", sub);

		sub = new JsonObject();
		JsonArray array = new JsonArray();
		src.getBiomes().stream().map(biome -> biome.getRegistryName().toString()).forEach(array::add);
		sub.add("name", array);
		array = new JsonArray();
		src.getBiomeTypes().stream().map(BiomeDictionary.Type::getName).forEach(array::add);
		sub.add("type", array);
		entry.add("target_biomes", sub);

		return entry;
	}

	@Override
	public CaveVein deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject entry = json.getAsJsonObject();

		JsonObject sub = entry.get("block").getAsJsonObject();
		BlockMeta block = new BlockMeta(sub.get("name").getAsString(), sub.get("meta").getAsInt());

		sub = entry.get("target_block").getAsJsonObject();
		BlockMeta target = new BlockMeta(sub.get("name").getAsString(), sub.get("meta").getAsInt());

		int weight = entry.get("weight").getAsInt();
		int size = entry.get("size").getAsInt();

		sub = entry.get("height").getAsJsonObject();
		int min = sub.get("min").getAsInt();
		int max = sub.get("max").getAsInt();

		CaveVein vein = new CaveVein(block, target, weight, size, min, max);

		sub = entry.get("target_biome").getAsJsonObject();
		JsonArray array = sub.get("name").getAsJsonArray();

		Stream<String> name = Stream.empty();

		if (array.size() > 0)
		{
			 name = Streams.stream(array).map(JsonElement::getAsString);
		}

		array = sub.get("type").getAsJsonArray();

		Stream<BiomeDictionary.Type> type = Stream.empty();

		if (array.size() > 0)
		{
			Set<String> set = Streams.stream(array).map(JsonElement::getAsString).collect(Collectors.toSet());

			type = BiomeDictionary.Type.getAll().stream().filter(o -> set.contains(o.getName()));
		}

		return vein.setBiomes(Stream.concat(name, type).toArray());
	}

	public static void createExample()
	{
		CaveVeinManager manager = new CaveVeinManager("example");

		manager.addCaveVein(new CaveVein(new BlockMeta(Blocks.COAL_ORE, 0), 20, 10, 1, 127));

		BlockMeta block = new BlockMeta(CaveBlocks.CAVE_BLOCK, BlockCave.EnumType.RANDOMITE_ORE.getMetadata());
		BlockMeta target = new BlockMeta(Blocks.STONE, OreDictionary.WILDCARD_VALUE);

		manager.addCaveVein(new CaveVein(block, target, 10, 5, 1, 127).setBiomes(new Object[] {Biomes.PLAINS, BiomeDictionary.Type.getAll().toArray()}));

		manager.saveToFile();
	}
}