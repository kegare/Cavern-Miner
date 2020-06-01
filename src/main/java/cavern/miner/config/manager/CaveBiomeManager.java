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
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import cavern.miner.block.CaveBlocks;
import cavern.miner.config.Config;
import cavern.miner.core.CavernMod;
import cavern.miner.util.BlockMeta;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CaveBiomeManager implements JsonSerializer<CaveBiome>, JsonDeserializer<CaveBiome>
{
	private final Map<Biome, CaveBiome> biomes = Maps.newHashMap();

	private final File file;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(CaveBiome.class, this).create();

	public CaveBiomeManager(String name)
	{
		this.file = new File(Config.getConfigDir(), name + "_biomes.json");
	}

	public void setCaveBiomes(Collection<CaveBiome> entries)
	{
		biomes.clear();

		for (CaveBiome entry : entries)
		{
			biomes.put(entry.getBiome(), entry);
		}
	}

	@Nullable
	public CaveBiome getCaveBiome(Biome biome)
	{
		return biomes.get(biome);
	}

	public Map<Biome, CaveBiome> getCaveBiomes()
	{
		return biomes;
	}

	public void addCaveBiome(CaveBiome entry)
	{
		biomes.put(entry.getBiome(), entry);
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
				biomes.clear();

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
		if (biomes.isEmpty())
		{
			return null;
		}

		return gson.toJson(biomes.values().stream().sorted().toArray(CaveBiome[]::new));
	}

	public boolean fromJson(Reader json)
	{
		try
		{
			Collection<CaveBiome> entries = gson.fromJson(json, new TypeToken<Collection<CaveBiome>>(){}.getType());

			if (entries == null || entries.isEmpty())
			{
				return false;
			}

			setCaveBiomes(entries);

			return !biomes.isEmpty();
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Failed to read from json", e);

			return false;
		}
	}

	@Override
	public JsonElement serialize(CaveBiome src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject entry = new JsonObject();

		entry.addProperty("name", src.getBiome().getRegistryName().toString());

		JsonObject sub = new JsonObject();
		BlockMeta blockMeta = src.getTopBlock();
		sub.addProperty("name", blockMeta.getBlock().getRegistryName().toString());
		sub.addProperty("meta", blockMeta.getMeta());
		entry.add("top_block", sub);

		sub = new JsonObject();
		blockMeta = src.getFillerBlock();
		sub.addProperty("name", blockMeta.getBlock().getRegistryName().toString());
		sub.addProperty("meta", blockMeta.getMeta());
		entry.add("filler_block", sub);

		return entry;
	}

	@Override
	public CaveBiome deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject entry = json.getAsJsonObject();

		String name = entry.get("name").getAsString();

		JsonObject sub = entry.get("top_block").getAsJsonObject();
		BlockMeta top = new BlockMeta(sub.get("name").getAsString(), sub.get("meta").getAsInt());

		sub = entry.get("filler_block").getAsJsonObject();
		BlockMeta filler = new BlockMeta(sub.get("name").getAsString(), sub.get("meta").getAsInt());

		return new CaveBiome(ForgeRegistries.BIOMES.getValue(new ResourceLocation(name)), top, filler);
	}

	public static void createExample()
	{
		CaveBiomeManager manager = new CaveBiomeManager("example");

		manager.addCaveBiome(new CaveBiome(Biomes.PLAINS, new BlockMeta(Blocks.GRASS, 0), new BlockMeta(Blocks.DIRT, 0)));
		manager.addCaveBiome(new CaveBiome(Biomes.TAIGA, new BlockMeta(CaveBlocks.PERVERTED_LOG, 0), new BlockMeta(Blocks.PLANKS, 0)));

		manager.saveToFile();
	}
}