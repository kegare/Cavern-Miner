package cavern.miner.network.server;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cavern.miner.data.PortalCache;
import cavern.miner.network.CaveNetworkRegistry;
import cavern.miner.network.client.RegenerationGuiMessage;
import cavern.miner.network.client.RegenerationGuiMessage.EnumType;
import cavern.miner.util.CaveLog;
import cavern.miner.world.CaveDimensions;
import cavern.miner.world.CustomSeed;
import cavern.miner.world.CustomSeedProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RegenerationMessage implements ISimpleMessage<RegenerationMessage, IMessage>
{
	public boolean backup;

	public final List<DimensionType> dimensions = Lists.newArrayList();

	@Override
	public void fromBytes(ByteBuf buf)
	{
		backup = buf.readBoolean();

		int i = buf.readInt();

		if (i <= 0)
		{
			return;
		}

		while (i-- > 0)
		{
			int dim = buf.readInt();

			try
			{
				dimensions.add(DimensionType.getById(dim));
			}
			catch (IllegalArgumentException e)
			{
				continue;
			}
		}
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(backup);
		buf.writeInt(dimensions.size());

		for (DimensionType type : dimensions)
		{
			if (type != null)
			{
				buf.writeInt(type.getId());
			}
		}
	}

	@Override
	public IMessage process()
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		File rootDir = DimensionManager.getCurrentSaveRootDirectory();

		if (rootDir == null || !rootDir.exists())
		{
			return new RegenerationGuiMessage(EnumType.FAILED);
		}

		server.getPlayerList().saveAllPlayerData();

		for (DimensionType type : dimensions)
		{
			WorldServer world = DimensionManager.getWorld(type.getId());

			if (world != null)
			{
				for (EntityPlayer player : world.playerEntities)
				{
					if (player != null && player instanceof EntityPlayerMP)
					{
						EntityPlayerMP playerMP = (EntityPlayerMP)player;

						if (playerMP.connection != null)
						{
							playerMP.connection.disconnect(new TextComponentTranslation("cavern.message.disconnect.unload"));
						}
					}
				}

				try
				{
					world.saveAllChunks(true, null);
				}
				catch (MinecraftException e)
				{
					return new RegenerationGuiMessage(EnumType.FAILED);
				}

				world.flush();

				MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));

				DimensionManager.setWorld(world.provider.getDimension(), null, server);

				world.getWorldInfo().setDimensionData(world.provider.getDimension(), null);

				if (world.provider instanceof CustomSeedProvider)
				{
					CustomSeed seed = ((CustomSeedProvider)world.provider).getSeedData();

					if (seed != null)
					{
						seed.refreshSeed();
					}
				}
			}

			for (EntityPlayerMP player : server.getPlayerList().getPlayers())
			{
				if (player != null)
				{
					PortalCache.get(player).clearLastPos(null, type);

					player.setSpawnChunk(null, false, type.getId());
				}
			}

			File dimDir = new File(rootDir, "DIM" + type.getId());

			if (!dimDir.exists())
			{
				return new RegenerationGuiMessage(EnumType.FAILED);
			}

			ITextComponent name = new TextComponentString(CaveDimensions.getLocalizedName(type));
			name.getStyle().setBold(true);

			sendProgress(EnumType.START);

			ITextComponent message = new TextComponentTranslation("cavern.regeneration.regenerating", name);
			message.getStyle().setColor(TextFormatting.GRAY);

			server.getPlayerList().sendMessage(message);

			if (backup)
			{
				message = new TextComponentTranslation("cavern.regeneration.backup", name);
				message.getStyle().setColor(TextFormatting.GRAY);

				server.getPlayerList().sendMessage(message);

				sendProgress(EnumType.BACKUP);

				Calendar calendar = Calendar.getInstance();
				String year = Integer.toString(calendar.get(Calendar.YEAR));
				String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
				String day = String.format("%02d", calendar.get(Calendar.DATE));
				String hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
				String minute = String.format("%02d", calendar.get(Calendar.MINUTE));
				String second = String.format("%02d", calendar.get(Calendar.SECOND));
				File bak = new File(rootDir, type.getName() + "_bak-" + String.join("", year, month, day) + "-" + String.join("", hour, minute, second) + ".zip");

				if (archiveDirectory(dimDir, bak))
				{
					message = new TextComponentTranslation("cavern.regeneration.backup.success", name);
					message.getStyle().setColor(TextFormatting.GRAY);

					server.getPlayerList().sendMessage(message);
				}
				else
				{
					message = new TextComponentTranslation("cavern.regeneration.backup.failed", name);
					message.getStyle().setColor(TextFormatting.RED);

					server.getPlayerList().sendMessage(message);

					return new RegenerationGuiMessage(EnumType.FAILED);
				}
			}

			try
			{
				FileUtils.deleteDirectory(dimDir);
			}
			catch (IOException e)
			{
				return new RegenerationGuiMessage(EnumType.FAILED);
			}

			message = new TextComponentTranslation("cavern.regeneration.regenerated", name);
			message.getStyle().setColor(TextFormatting.GRAY);

			server.getPlayerList().sendMessage(message);

			if (type.shouldLoadSpawn())
			{
				world = server.getWorld(type.getId());

				try
				{
					world.saveAllChunks(true, null);
				}
				catch (MinecraftException e) {}

				world.flush();
			}
		}

		return new RegenerationGuiMessage(EnumType.REGENERATED);
	}

	private void sendProgress(EnumType type)
	{
		CaveNetworkRegistry.NETWORK.sendToAll(new RegenerationGuiMessage(type));
	}

	private boolean archiveDirectory(File dir, File dest)
	{
		Path dirPath = dir.toPath();
		String parent = dir.getName();
		Map<String, String> env = Maps.newHashMap();
		env.put("create", "true");
		URI uri = dest.toURI();

		try
		{
			uri = new URI("jar:" + uri.getScheme(), uri.getPath(), null);
		}
		catch (URISyntaxException e)
		{
			return false;
		}

		try (FileSystem zipfs = FileSystems.newFileSystem(uri, env))
		{
			Files.createDirectory(zipfs.getPath(parent));

			for (File file : dir.listFiles())
			{
				if (file.isDirectory())
				{
					Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>()
					{
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
						{
							Files.copy(file, zipfs.getPath(parent, dirPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);

							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
						{
							Files.createDirectory(zipfs.getPath(parent, dirPath.relativize(dir).toString()));

							return FileVisitResult.CONTINUE;
						}
					});
				}
				else
				{
					Files.copy(file.toPath(), zipfs.getPath(parent, file.getName()), StandardCopyOption.REPLACE_EXISTING);
				}
			}

			return true;
		}
		catch (IOException e)
		{
			CaveLog.log(Level.WARN, e, "An error occurred archiving the " + parent + "directory.");
		}

		return false;
	}
}