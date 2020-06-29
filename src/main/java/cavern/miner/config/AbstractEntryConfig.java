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
import com.google.gson.JsonParseException;

import cavern.miner.CavernMod;

public abstract class AbstractEntryConfig
{
	private final File file;
	private final Gson gson;

	public AbstractEntryConfig(File file)
	{
		this(file, new GsonBuilder().setPrettyPrinting().create());
	}

	public AbstractEntryConfig(File file, Gson gson)
	{
		this.file = file;
		this.gson = gson;
	}

	public File getFile()
	{
		return file;
	}

	public Gson getGson()
	{
		return gson;
	}

	public abstract boolean isEmpty();

	public boolean isAllowEmpty()
	{
		return false;
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
				try (FileInputStream fis = new FileInputStream(file); BufferedReader buffer = new BufferedReader(new InputStreamReader(fis)))
				{
					fromJson(buffer);
				}

				return true;
			}
		}
		catch (IOException | JsonParseException e)
		{
			CavernMod.LOG.error("Failed to load {}", file.getName(), e);
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
				try (FileOutputStream fos = new FileOutputStream(file); BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos)))
				{
					buffer.write(Strings.nullToEmpty(toJson()));
				}

				return true;
			}
		}
		catch (IOException | JsonParseException e)
		{
			CavernMod.LOG.error("Failed to save {}", file.getName(), e);
		}

		return false;
	}

	@Nullable
	public abstract String toJson() throws JsonParseException;

	public abstract void fromJson(Reader json) throws JsonParseException;

	public void setToDefault() {}

	public void load()
	{
		if (!loadFromFile() || (!isAllowEmpty() && isEmpty()))
		{
			setToDefault();
			saveToFile();
		}
	}
}