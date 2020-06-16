package cavern.miner.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import cavern.miner.init.CaveCapabilities;
import cavern.miner.storage.Miner;
import cavern.miner.storage.MinerRank;
import cavern.miner.storage.MinerRank.RankEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class CavernCommand
{
	private static final SimpleCommandExceptionType INVALID_MINER = new SimpleCommandExceptionType(new LiteralMessage("Invalid miner"));

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("cavern").then(registerMiner()));
	}

	private static ArgumentBuilder<CommandSource, ?> registerMiner()
	{
		return Commands.literal("miner").requires(o -> o.hasPermissionLevel(2))
			.then(Commands.argument("target", EntityArgument.player())
				.then(Commands.literal("point")
					.then(Commands.literal("add").then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(o -> mapToZero(getMiner(o).addPoint(IntegerArgumentType.getInteger(o, "amount")).sendToClient()))))
					.then(Commands.literal("set").then(Commands.argument("amount", IntegerArgumentType.integer(0))
						.executes(o -> mapToZero(getMiner(o).setPoint(IntegerArgumentType.getInteger(o, "amount")).sendToClient()))))
				)
				.then(Commands.literal("rank")
					.then(Commands.literal("set").then(Commands.argument("name", StringArgumentType.string())
						.suggests((o, builder) -> ISuggestionProvider.suggest(MinerRank.getEntries().stream().map(RankEntry::getName), builder))
						.executes(o -> mapToZero(getMiner(o).setRank(StringArgumentType.getString(o, "name")).sendToClient()))))
					.then(Commands.literal("promote").then(Commands.argument("name", StringArgumentType.string())
							.suggests((o, builder) -> ISuggestionProvider.suggest(MinerRank.getEntries().stream().map(RankEntry::getName), builder))
							.executes(o -> mapToZero(getMiner(o).promoteRank(StringArgumentType.getString(o, "name")).sendToClient()))))
				)
			);
	}

	private static int mapToZero(Object obj)
	{
		return 0;
	}

	private static Miner getMiner(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");

		return player.getCapability(CaveCapabilities.MINER).orElseThrow(() -> INVALID_MINER.create());
	}
}