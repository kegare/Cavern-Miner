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

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import cavern.miner.CavernMod;
import cavern.miner.config.json.BlockStateTagListSerializer;
import cavern.miner.util.BlockStateTagList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.Tags;

public class VeinBlacklistConfig
{
	private final BlockStateTagList list = BlockStateTagList.create();

	private final File file;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public VeinBlacklistConfig(String name)
	{
		this.file = new File(CavernModConfig.getConfigDir(), name + "_veins_blacklist.json");
	}

	public boolean setBlacklist(BlockStateTagList entries)
	{
		list.clear();

		return list.addEntries(entries.getEntryList()) && list.addTags(entries.getTagList());
	}

	public BlockStateTagList getBlacklist()
	{
		return list;
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
			CavernMod.LOG.error("Failed to load veins blacklist", e);
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
			CavernMod.LOG.error("Failed to save veins blacklist", e);
		}
	}

	@Nullable
	public String toJson()
	{
		if (list.isEmpty())
		{
			return null;
		}

		return gson.toJson(BlockStateTagListSerializer.INSTANCE.serialize(list, BlockState.class, null));
	}

	public boolean fromJson(Reader json)
	{
		try
		{
			BlockStateTagList entries = BlockStateTagListSerializer.INSTANCE.deserialize(gson.fromJson(json, JsonElement.class), BlockState.class, null);

			if (entries == null || entries.isEmpty())
			{
				return false;
			}

			return setBlacklist(entries);
		}
		catch (Exception e)
		{
			CavernMod.LOG.error("Failed to read from json", e);

			return false;
		}
	}

	public void setDefault()
	{
		list.clear();
		list.add(Blocks.STONE).add(Blocks.POLISHED_ANDESITE).add(Blocks.POLISHED_DIORITE).add(Blocks.POLISHED_GRANITE);
		list.add(Tags.Blocks.ORES_QUARTZ);
	}
}