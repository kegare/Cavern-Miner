package cavern.miner.core;

import org.apache.logging.log4j.Level;

import cavern.miner.api.CavernAPI;
import cavern.miner.block.CaveBlocks;
import cavern.miner.block.RandomiteHelper;
import cavern.miner.capability.CaveCapabilities;
import cavern.miner.client.CaveKeyBindings;
import cavern.miner.client.CaveRenderingRegistry;
import cavern.miner.client.config.CaveConfigEntries;
import cavern.miner.client.handler.ClientEventHooks;
import cavern.miner.client.handler.MinerHUDEventHooks;
import cavern.miner.config.CavelandConfig;
import cavern.miner.config.CavernConfig;
import cavern.miner.config.EntryListHelper;
import cavern.miner.config.GeneralConfig;
import cavern.miner.config.HugeCavernConfig;
import cavern.miner.config.MiningConfig;
import cavern.miner.config.MiningPointHelper;
import cavern.miner.enchantment.CaveEnchantments;
import cavern.miner.entity.CaveEntityRegistry;
import cavern.miner.handler.CaveEventHooks;
import cavern.miner.handler.CavebornEventHooks;
import cavern.miner.handler.MinerEventHooks;
import cavern.miner.handler.MiningEventHooks;
import cavern.miner.handler.api.DataHandler;
import cavern.miner.handler.api.DimensionHandler;
import cavern.miner.item.CaveItems;
import cavern.miner.network.CaveNetworkRegistry;
import cavern.miner.plugin.HaCPlugin;
import cavern.miner.util.CaveLog;
import cavern.miner.world.CaveDimensions;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod
(
	modid = CavernMod.MODID,
	dependencies = "required:forge@[14.23.5.2854,);",
	guiFactory = "cavern.miner.client.config.CaveGuiFactory"
)
public final class CavernMod
{
	public static final String MODID = "cavern";

	@Instance(MODID)
	public static CavernMod instance;

	@Metadata(MODID)
	public static ModMetadata metadata;

	@SidedProxy(clientSide = "cavern.miner.client.ClientProxy", serverSide = "cavern.miner.core.CommonProxy")
	public static CommonProxy proxy;

	public static final CreativeTabCavern TAB_CAVERN = new CreativeTabCavern();

	@EventHandler
	public void construct(FMLConstructionEvent event)
	{
		CavernAPI.dimension = new DimensionHandler();
		CavernAPI.data = new DataHandler();

		if (event.getSide().isClient())
		{
			clientConstruct();
		}

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SideOnly(Side.CLIENT)
	public void clientConstruct()
	{
		CaveConfigEntries.initEntries();
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		GeneralConfig.syncConfig();

		MiningConfig.syncConfig();

		if (event.getSide().isClient())
		{
			CaveRenderingRegistry.registerRenderers();
		}
	}

	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event)
	{
		CaveBlocks.registerBlocks(event.getRegistry());
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();

		CaveBlocks.registerItemBlocks(registry);
		CaveItems.registerItems(registry);
	}

	@SubscribeEvent
	public void registerEntityEntries(RegistryEvent.Register<EntityEntry> event)
	{
		CaveEntityRegistry.registerEntities(event.getRegistry());
	}

	@SubscribeEvent
	public void registerEnchantments(RegistryEvent.Register<Enchantment> event)
	{
		CaveEnchantments.registerEnchantments(event.getRegistry());
	}

	@SubscribeEvent
	public void registerSounds(RegistryEvent.Register<SoundEvent> event)
	{
		CaveSounds.registerSounds(event.getRegistry());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerModels(ModelRegistryEvent event)
	{
		CaveBlocks.registerModels();
		CaveItems.registerModels();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerBlockColors(ColorHandlerEvent.Block event)
	{
		CaveBlocks.registerBlockColors(event.getBlockColors());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void registerItemColors(ColorHandlerEvent.Item event)
	{
		ItemColors itemColors = event.getItemColors();
		BlockColors blockColors = event.getBlockColors();

		CaveBlocks.registerItemBlockColors(blockColors, itemColors);
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		CavernConfig.syncConfig();
		CavernConfig.syncBiomesConfig();
		CavernConfig.syncVeinsConfig();

		HugeCavernConfig.syncConfig();
		HugeCavernConfig.syncBiomesConfig();
		HugeCavernConfig.syncVeinsConfig();

		CavelandConfig.syncConfig();
		CavelandConfig.syncVeinsConfig();

		CaveNetworkRegistry.registerMessages();

		CaveCapabilities.registerCapabilities();

		CaveBlocks.registerOreDicts();
		CaveItems.registerOreDicts();

		CaveItems.registerEquipments();

		CaveBlocks.registerSmeltingRecipes();

		CaveEntityRegistry.regsiterSpawns();

		CaveDimensions.registerDimensions();

		if (event.getSide().isClient())
		{
			CaveRenderingRegistry.registerRenderBlocks();

			CaveKeyBindings.registerKeyBindings();

			MinecraftForge.EVENT_BUS.register(new ClientEventHooks());
			MinecraftForge.EVENT_BUS.register(new MinerHUDEventHooks());
		}

		MinecraftForge.EVENT_BUS.register(new CaveEventHooks());
		MinecraftForge.EVENT_BUS.register(new CavebornEventHooks());
		MinecraftForge.EVENT_BUS.register(new MinerEventHooks());
		MinecraftForge.EVENT_BUS.register(new MiningEventHooks());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		EntryListHelper.setupBlocks();
		EntryListHelper.setupItems();

		if (Loader.isModLoaded(HaCPlugin.LIB_MODID))
		{
			try
			{
				HaCPlugin.load();
			}
			catch (Exception e)
			{
				CaveLog.log(Level.WARN, e, "Failed to load the Heat&Climate mod plugin.");
			}
		}

		MiningPointHelper.setupPoints();
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandCavern());

		GeneralConfig.cavebornBonusItems.refreshItems();
		GeneralConfig.randomiteBlacklist.refreshItems();

		MiningConfig.miningPoints.refreshPoints();
		MiningConfig.veinTargetBlocks.refreshBlocks();
		MiningConfig.areaTargetBlocks.refreshBlocks();

		CavernConfig.triggerItems.refreshItems();
		CavernConfig.dungeonMobs.refreshEntities();
		CavernConfig.towerDungeonMobs.refreshEntities();
		CavernConfig.autoVeinBlacklist.refreshBlocks();

		HugeCavernConfig.triggerItems.refreshItems();
		HugeCavernConfig.autoVeinBlacklist.refreshBlocks();

		CavelandConfig.triggerItems.refreshItems();
		CavelandConfig.autoVeinBlacklist.refreshBlocks();

		RandomiteHelper.refreshItems();
	}
}