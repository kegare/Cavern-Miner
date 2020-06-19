package cavern.miner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cavern.miner.command.CavernCommand;
import cavern.miner.config.CavernModConfig;
import cavern.miner.config.GeneralConfig;
import cavern.miner.config.dimension.CavernConfig;
import cavern.miner.config.dimension.HugeCavernConfig;
import cavern.miner.config.dimension.VeinConfig;
import cavern.miner.init.CaveBiomes;
import cavern.miner.init.CaveBlocks;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveCriteriaTriggers;
import cavern.miner.init.CaveDimensions;
import cavern.miner.init.CaveEnchantments;
import cavern.miner.init.CaveEntities;
import cavern.miner.init.CaveFeatures;
import cavern.miner.init.CaveItems;
import cavern.miner.init.CavePlacements;
import cavern.miner.init.CaveSounds;
import cavern.miner.init.CaveWorldCarvers;
import cavern.miner.network.CaveNetworkConstants;
import net.minecraft.command.ICommandSource;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;

@Mod("cavern")
public final class CavernMod
{
	public static final Logger LOG = LogManager.getLogger("cavern");

	public CavernMod()
	{
		CavernModConfig.check();
		CavernModConfig.register(ModLoadingContext.get());

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.register(this);

		CaveBlocks.REGISTRY.register(modEventBus);
		CaveItems.REGISTRY.register(modEventBus);
		CaveEntities.REGISTRY.register(modEventBus);
		CaveEnchantments.REGISTRY.register(modEventBus);
		CaveBiomes.REGISTRY.register(modEventBus);
		CaveDimensions.REGISTRY.register(modEventBus);
		CaveWorldCarvers.REGISTRY.register(modEventBus);
		CaveFeatures.REGISTRY.register(modEventBus);
		CavePlacements.REGISTRY.register(modEventBus);
		CaveSounds.REGISTRY.register(modEventBus);

		MinecraftForge.EVENT_BUS.register(this);

		CaveCriteriaTriggers.registerTriggers();

		LOG.debug("Loading network data for cavern net version: {}", CaveNetworkConstants.init());
	}

	@SubscribeEvent
	public void doCommonStuff(final FMLCommonSetupEvent event)
	{
		VeinConfig.createExampleConfig();

		CaveEntities.registerSpawnPlacements();

		CaveBiomes.init();

		CaveCapabilities.registerCapabilities();
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void doClientStuff(final FMLClientSetupEvent event)
	{
		CaveBlocks.registerRenderType();

		CaveEntities.registerRenderers();
	}

	@SubscribeEvent
	public void onItemsRegistry(final RegistryEvent.Register<Item> event)
	{
		CaveBlocks.registerBlockItems(CaveItems.REGISTRY);
	}

	@SubscribeEvent
	public void onLoaded(final FMLLoadCompleteEvent event)
	{
		GeneralConfig.loadConfig();

		CavernConfig.loadConfig();
		HugeCavernConfig.loadConfig();
	}

	@SubscribeEvent
	public void onServerStarting(final FMLServerStartingEvent event)
	{
		sendVersionNotification(event.getServer());

		CavernCommand.register(event.getCommandDispatcher());
	}

	public static IModInfo getModInfo()
	{
		return ModList.get().getModFileById("cavern").getMods().get(0);
	}

	public static void sendVersionNotification(ICommandSource source)
	{
		if (!GeneralConfig.INSTANCE.updateNotification.get())
		{
			return;
		}

		VersionChecker.CheckResult result = VersionChecker.getResult(getModInfo());

		if (result.status.shouldDraw() && result.target != null)
		{
			ITextComponent version = new StringTextComponent(result.target.toString());
			version.getStyle().setColor(TextFormatting.YELLOW);

			ITextComponent message = new TranslationTextComponent("cavern.message.update_version");
			message.appendText(" : ").appendSibling(version);
			message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result.url));

			source.sendMessage(message);
		}
	}
}