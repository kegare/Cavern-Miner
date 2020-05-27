package cavern.miner.core;

import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

import cavern.miner.data.Miner;
import cavern.miner.data.MinerRank;
import cavern.miner.network.CaveNetworkRegistry;
import cavern.miner.network.client.RegenerationGuiMessage;
import cavern.miner.network.client.RegenerationGuiMessage.EnumType;
import cavern.miner.util.Version;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandCavern extends CommandBase
{
	@Override
	public String getName()
	{
		return "cavern";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return String.format("/%s <%s>", getName(), Joiner.on('|').join(getCommands()));
	}

	public String[] getCommands()
	{
		return Version.isDev() ? new String[] {"regenerate", "miner"} : new String[] {"regenerate"};
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (args.length <= 0)
		{
			throw new WrongUsageException(getUsage(sender));
		}

		boolean isPlayer = sender instanceof EntityPlayerMP;

		if (args[0].equalsIgnoreCase("regenerate") && isPlayer)
		{
			EntityPlayerMP player = (EntityPlayerMP)sender;

			if (server.isSinglePlayer() || server.getPlayerList().canSendCommands(player.getGameProfile()))
			{
				CaveNetworkRegistry.sendTo(() -> new RegenerationGuiMessage(EnumType.OPEN), player);
			}
			else throw new CommandException("commands.generic.permission");
		}
		else if (args[0].equalsIgnoreCase("miner") && isPlayer && Version.isDev())
		{
			EntityPlayerMP player = (EntityPlayerMP)sender;

			if (server.isSinglePlayer() || server.getPlayerList().canSendCommands(player.getGameProfile()))
			{
				Miner miner = Miner.get(player);
				MinerRank nextRank = MinerRank.get(miner.getRank() + 1);

				if (nextRank.getRank() < MinerRank.VALUES.length - 1)
				{
					miner.setPoint(0, false);
					miner.addPoint(nextRank.getPhase());
				}
			}
			else throw new CommandException("commands.generic.permission");
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender instanceof MinecraftServer || sender instanceof EntityPlayerMP;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
	{
		return args.length == 1 ? CommandBase.getListOfStringsMatchingLastWord(args, getCommands()) : Collections.emptyList();
	}
}