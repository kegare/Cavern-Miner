package cavern.miner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cavern.miner.client.ClientProxy;
import cavern.miner.config.CavernConfig;
import cavern.miner.config.CavernModConfig;
import cavern.miner.config.VeinConfig;
import cavern.miner.init.CaveBiomes;
import cavern.miner.init.CaveBlocks;
import cavern.miner.init.CaveCapabilities;
import cavern.miner.init.CaveDimensions;
import cavern.miner.init.CaveItems;
import cavern.miner.init.CaveSounds;
import cavern.miner.init.CaveWorldCarvers;
import cavern.miner.network.CaveNetworkConstants;
import cavern.miner.proxy.CommonProxy;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("cavern")
public final class CavernMod
{
	public static final Logger LOG = LogManager.getLogger("cavern");

	public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

	public CavernMod()
	{
		CavernModConfig.register(ModLoadingContext.get());

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.register(this);

		CaveBlocks.REGISTRY.register(modEventBus);
		CaveItems.REGISTRY.register(modEventBus);
		CaveBiomes.REGISTRY.register(modEventBus);
		CaveDimensions.REGISTRY.register(modEventBus);
		CaveWorldCarvers.REGISTRY.register(modEventBus);
		CaveSounds.REGISTRY.register(modEventBus);

		LOG.debug("Loading network data for cavern net version: {}", CaveNetworkConstants.init());
	}

	@SubscribeEvent
	public void doCommonStuff(final FMLCommonSetupEvent event)
	{
		VeinConfig.createExampleConfig();

		CaveCapabilities.registerCapabilities();
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void doClientStuff(final FMLClientSetupEvent event)
	{
		CaveBlocks.registerRenderType();
	}

	@SubscribeEvent
	public void onItemsRegistry(final RegistryEvent.Register<Item> event)
	{
		CaveBlocks.registerBlockItems(CaveItems.REGISTRY);
	}

	@SubscribeEvent
	public void onLoaded(final FMLLoadCompleteEvent event)
	{
		CavernConfig.loadConfig();

		CaveBlocks.registerOres();
	}
}