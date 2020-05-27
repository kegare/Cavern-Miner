package cavern.miner.client.handler;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Strings;

import cavern.miner.api.CavernAPI;
import cavern.miner.block.RandomiteHelper;
import cavern.miner.client.CaveKeyBindings;
import cavern.miner.client.gui.GuiMiningRecords;
import cavern.miner.config.CavelandConfig;
import cavern.miner.config.CavernConfig;
import cavern.miner.config.GeneralConfig;
import cavern.miner.config.HugeCavernConfig;
import cavern.miner.config.MiningConfig;
import cavern.miner.core.CavernMod;
import cavern.miner.item.ItemCavenicBow;
import cavern.miner.util.Version;
import cavern.miner.world.CaveDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientEventHooks
{
	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		String mod = event.getModID();
		String type = event.getConfigID();

		if (mod.equals(CavernMod.MODID))
		{
			if (Strings.isNullOrEmpty(type))
			{
				GeneralConfig.syncConfig();
				MiningConfig.syncConfig();
				CavernConfig.syncConfig();
				HugeCavernConfig.syncConfig();
				CavelandConfig.syncConfig();
			}
			else switch (type)
			{
				case "general":
					GeneralConfig.syncConfig();

					if (event.isWorldRunning())
					{
						GeneralConfig.cavebornBonusItems.refreshItems();
						GeneralConfig.randomiteBlacklist.refreshItems();

						RandomiteHelper.refreshItems();
					}

					break;
				case "mining":
					MiningConfig.syncConfig();

					if (event.isWorldRunning())
					{
						MiningConfig.miningPoints.refreshPoints();
						MiningConfig.veinTargetBlocks.refreshBlocks();
						MiningConfig.areaTargetBlocks.refreshBlocks();
					}

					break;
				case "dimension.cavern":
					CavernConfig.syncConfig();

					if (event.isWorldRunning())
					{
						CavernConfig.triggerItems.refreshItems();
						CavernConfig.dungeonMobs.refreshEntities();
						CavernConfig.towerDungeonMobs.refreshEntities();
						CavernConfig.autoVeinBlacklist.refreshBlocks();
					}

					break;
				case "dimension.hugeCavern":
					HugeCavernConfig.syncConfig();

					if (event.isWorldRunning())
					{
						HugeCavernConfig.triggerItems.refreshItems();
						HugeCavernConfig.autoVeinBlacklist.refreshBlocks();
					}

					break;
				case "dimension.caveland":
					CavelandConfig.syncConfig();

					if (event.isWorldRunning())
					{
						CavelandConfig.triggerItems.refreshItems();
						CavelandConfig.autoVeinBlacklist.refreshBlocks();
					}

					break;
			}
		}
	}

	@SubscribeEvent
	public void onRenderGameTextOverlay(RenderGameOverlayEvent.Text event)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (mc.gameSettings.showDebugInfo && CavernAPI.dimension.isInCaverns(mc.player))
		{
			String name = CaveDimensions.getLocalizedName(mc.world.provider.getDimensionType());

			if (!Strings.isNullOrEmpty(name))
			{
				event.getLeft().add("Cave Dim: " + name);
			}
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event)
	{
		if (!Keyboard.getEventKeyState())
		{
			return;
		}

		Minecraft mc = FMLClientHandler.instance().getClient();

		if (!CavernAPI.dimension.isInCaverns(mc.player))
		{
			return;
		}

		int key = Keyboard.getEventKey();

		if (CaveKeyBindings.KEY_MINING_RECORDS.isActiveAndMatches(key))
		{
			mc.displayGuiScreen(new GuiMiningRecords());
		}
	}

	@SubscribeEvent
	public void onConnected(ClientConnectedToServerEvent event)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (GeneralConfig.versionNotify)
		{
			ITextComponent message;
			ITextComponent name = new TextComponentString(CavernMod.metadata.name);
			name.getStyle().setColor(TextFormatting.AQUA);

			if (Version.isOutdated())
			{
				ITextComponent latest = new TextComponentString(Version.getLatest().toString());
				latest.getStyle().setColor(TextFormatting.YELLOW);

				message = new TextComponentTranslation("cavern.version.message", name);
				message.appendText(" : ").appendSibling(latest);
				message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, CavernMod.metadata.url));

				mc.ingameGUI.getChatGUI().printChatMessage(message);
			}

			message = null;

			if (Version.isDev())
			{
				message = new TextComponentTranslation("cavern.version.message.dev", name);
			}
			else if (Version.isBeta())
			{
				message = new TextComponentTranslation("cavern.version.message.beta", name);
			}
			else if (Version.isAlpha())
			{
				message = new TextComponentTranslation("cavern.version.message.alpha", name);
			}

			if (message != null)
			{
				mc.ingameGUI.getChatGUI().printChatMessage(message);
			}
		}
	}

	@SubscribeEvent
	public void onFogDensity(FogDensity event)
	{
		Entity entity = event.getEntity();

		if (CavernAPI.dimension.isInCaveland(entity))
		{
			GlStateManager.setFog(GlStateManager.FogMode.EXP);

			event.setDensity((float)Math.abs(Math.pow((Math.min(entity.posY, 20) - 63) / (255 - 63), 4)));
			event.setCanceled(true);
		}
		else if (CavernAPI.dimension.isInHugeCavern(entity))
		{
			GlStateManager.setFog(GlStateManager.FogMode.EXP);

			event.setDensity(0.005F);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onFogColors(FogColors event)
	{
		Entity entity = event.getEntity();
		float var1 = 0.0F;

		if (CavernAPI.dimension.isInHugeCavern(entity))
		{
			var1 = 0.8F;
		}
		else if (CavernAPI.dimension.isInCaveland(entity))
		{
			var1 = 0.7F;
		}

		if (var1 > 0.0F)
		{
			float red = event.getRed();
			float green = event.getGreen();
			float blue = event.getBlue();
			float var2 = 1.0F / red;

			if (var2 > 1.0F / green)
			{
				var2 = 1.0F / green;
			}

			if (var2 > 1.0F / blue)
			{
				var2 = 1.0F / blue;
			}

			event.setRed(red * (1.0F - var1) + red * var2 * var1);
			event.setGreen(green * (1.0F - var1) + green * var2 * var1);
			event.setBlue(blue * (1.0F - var1) + blue * var2 * var1);
		}
	}

	@SubscribeEvent
	public void onFOVUpdate(FOVUpdateEvent event)
	{
		EntityPlayer player = event.getEntity();

		if (!player.isHandActive())
		{
			return;
		}

		ItemStack using = player.getActiveItemStack();

		if (using.isEmpty())
		{
			return;
		}

		if (using.getItem() instanceof ItemCavenicBow)
		{
			ItemCavenicBow.BowMode mode = ItemCavenicBow.BowMode.byItemStack(using);
			float zoom = mode.getZoomScale();

			if (zoom <= 0.0F)
			{
				return;
			}

			float f = player.getItemInUseMaxCount() / mode.getPullingSpeed();

			if (f > 1.0F)
			{
				f = 1.0F;
			}
			else
			{
				f *= f;
			}

			event.setNewfov(event.getFov() * (1.0F - f * zoom));
		}
	}
}