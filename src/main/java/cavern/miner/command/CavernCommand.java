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
import cavern.miner.network.CaveNetworkConstants;
import cavern.miner.network.MinerRecordMessage;
import cavern.miner.storage.Miner;
import cavern.miner.storage.MinerRank;
import cavern.miner.storage.MinerRank.RankEntry;
import cavern.miner.storage.MinerRecord;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public final class CavernCommand
{
	private static final SimpleCommandExceptionType INVALID_MINER = new SimpleCommandExceptionType(new LiteralMessage("Invalid miner"));

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("cavern").then(registerMiner()).then(registerRecord()));
	}

	private static ArgumentBuilder<CommandSource, ?> registerMiner()
	{
		return Commands.literal("miner").requires(o -> o.hasPermissionLevel(2))
			.then(Commands.argument("target", EntityArgument.player())
				.then(Commands.literal("point")
					.then(Commands.literal("add").then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(ctx -> execute(ctx, o -> getMiner(o).addPoint(IntegerArgumentType.getInteger(o, "amount")).sendToClient()))))
					.then(Commands.literal("set").then(Commands.argument("amount", IntegerArgumentType.integer(0))
						.executes(ctx -> execute(ctx, o -> getMiner(o).setPoint(IntegerArgumentType.getInteger(o, "amount")).sendToClient()))))
				)
				.then(Commands.literal("rank")
					.then(Commands.literal("set").then(Commands.argument("name", StringArgumentType.string())
						.suggests((o, builder) -> ISuggestionProvider.suggest(MinerRank.getEntries().stream().map(RankEntry::getName), builder))
						.executes(ctx -> execute(ctx, o -> getMiner(o).setRank(StringArgumentType.getString(o, "name")).sendToClient()))))
					.then(Commands.literal("promote").then(Commands.argument("name", StringArgumentType.string())
						.suggests((o, builder) -> ISuggestionProvider.suggest(MinerRank.getEntries().stream().map(RankEntry::getName), builder))
						.executes(ctx -> execute(ctx, o -> getMiner(o).promoteRank(StringArgumentType.getString(o, "name")).sendToClient()))))
				)
			);
	}

	private static ArgumentBuilder<CommandSource, ?> registerRecord()
	{
		return Commands.literal("record").requires(o -> o.getEntity() != null && o.getEntity() instanceof ServerPlayerEntity)
			.executes(ctx -> execute(ctx, CavernCommand::displayMinerRecord));
	}

	private static int execute(CommandContext<CommandSource> context, CommandConsumer<CommandContext<CommandSource>> command) throws CommandSyntaxException
	{
		return command.run(context);
	}

	private static Miner getMiner(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");

		return player.getCapability(CaveCapabilities.MINER).orElseThrow(() -> INVALID_MINER.create());
	}

	private static void displayMinerRecord(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity player = context.getSource().asPlayer();
		MinerRecord record = player.getCapability(CaveCapabilities.MINER).map(Miner::getRecord).orElseThrow(() -> INVALID_MINER.create());

		CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> player), new MinerRecordMessage(record));
	}
}