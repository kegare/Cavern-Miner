package cavern.miner.command;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import cavern.miner.config.GeneralConfig;
import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.config.dimension.HugeCavernConfig;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveDimensions;
import cavern.miner.storage.Miner;
import cavern.miner.storage.MinerRank;
import cavern.miner.storage.MinerRank.RankEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;

public final class CavernCommand
{
	private static final SimpleCommandExceptionType INVALID_MINER = new SimpleCommandExceptionType(new TranslationTextComponent("cavern.message.miner.invalid"));

	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal("cavern").then(registerRegenerate()).then(registerReload()).then(registerMiner()).then(registerRecord()));
	}

	private static ArgumentBuilder<CommandSource, ?> registerRegenerate()
	{
		return Commands.literal("regenerate").requires(o -> o.getServer().isSinglePlayer() || o.hasPermissionLevel(4))
			.then(Commands.argument("name", StringArgumentType.string())
				.suggests((ctx, builder) -> ISuggestionProvider.suggest(Arrays.asList("CAVERN", "HUGE_CAVERN"), builder))
				.executes(ctx -> execute(ctx, CavernCommand::regenerateDimension))
			);
	}

	private static ArgumentBuilder<CommandSource, ?> registerReload()
	{
		return Commands.literal("reload").requires(o -> o.getServer().isSinglePlayer() || o.hasPermissionLevel(4)).executes(ctx -> execute(ctx, CavernCommand::reloadConfig));
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
		return Commands.literal("record").requires(o -> o.getEntity() != null && o.getEntity() instanceof ServerPlayerEntity).executes(ctx -> execute(ctx, CavernCommand::displayMinerRecord));
	}

	private static int execute(CommandContext<CommandSource> context, CommandConsumer<CommandContext<CommandSource>> command) throws CommandSyntaxException
	{
		return command.run(context);
	}

	private static void regenerateDimension(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		String name = StringArgumentType.getString(context, "name").toUpperCase();
		DimensionType dim = null;

		if (name.equals("CAVERN"))
		{
			dim = CaveDimensions.CAVERN_TYPE;
		}
		else if (name.equals("HUGE_CAVERN"))
		{
			dim = CaveDimensions.HUGE_CAVERN_TYPE;
		}

		if (dim == null)
		{
			context.getSource().sendErrorMessage(new TranslationTextComponent("cavern.message.regenerate.invalid").appendText(" : " + name));

			return;
		}

		MinecraftServer server = context.getSource().getServer();
		ServerWorld world = DimensionManager.getWorld(server, dim, false, false);

		if (world != null && !world.getPlayers().isEmpty())
		{
			context.getSource().sendErrorMessage(new TranslationTextComponent("cavern.message.regenerate.error.player").appendText(" : " + name));

			return;
		}

		File folder = CaveDimensions.getSaveFolder(server, dim);

		if (folder == null)
		{
			context.getSource().sendErrorMessage(new TranslationTextComponent("cavern.message.regenerate.error.folder").appendText(" : " + name));

			return;
		}

		DimensionManager.unloadWorld(world);
		DimensionManager.unloadWorlds(server, false);

		try
		{
			FileUtils.deleteDirectory(folder);
		}
		catch (Exception e)
		{
			context.getSource().sendErrorMessage(new TranslationTextComponent("cavern.message.regenerate.error.unknown").appendText(" : " + name));

			return;
		}

		DimensionManager.initWorld(server, dim);

		context.getSource().sendFeedback(new TranslationTextComponent("cavern.message.regenerate.success").appendText(" : " + name), true);
	}

	private static void reloadConfig(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		GeneralConfig.INSTANCE.load();
		CavernConfig.INSTANCE.load();
		HugeCavernConfig.INSTANCE.load();

		context.getSource().sendFeedback(new TranslationTextComponent("cavern.message.reload"), true);
	}

	private static Miner getMiner(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity player = EntityArgument.getPlayer(context, "target");

		return player.getCapability(CaveCapabilities.MINER).orElseThrow(INVALID_MINER::create);
	}

	private static void displayMinerRecord(CommandContext<CommandSource> context) throws CommandSyntaxException
	{
		ServerPlayerEntity player = context.getSource().asPlayer();

		player.getCapability(CaveCapabilities.MINER).orElseThrow(INVALID_MINER::create).displayRecord();
	}
}