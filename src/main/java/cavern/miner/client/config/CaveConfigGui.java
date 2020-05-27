package cavern.miner.client.config;

import java.util.List;

import com.google.common.collect.Lists;

import cavern.miner.client.config.common.GeneralConfigEntry;
import cavern.miner.client.config.common.MiningConfigEntry;
import cavern.miner.client.config.dimension.DimensionConfigEntry;
import cavern.miner.config.Config;
import cavern.miner.core.CavernMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CaveConfigGui extends GuiConfig
{
	public static boolean detailInfo = true;
	public static boolean instantFilter = true;

	public CaveConfigGui(GuiScreen parent)
	{
		super(parent, getConfigElements(), CavernMod.MODID, false, false, I18n.format(Config.LANG_KEY + "title"));
	}

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = Lists.newArrayList();

		list.add(new DummyCategoryElement("cavern:generalConfig", Config.LANG_KEY + "general", GeneralConfigEntry.class));
		list.add(new DummyCategoryElement("cavern:miningConfig", Config.LANG_KEY + "mining", MiningConfigEntry.class));
		list.add(new DummyCategoryElement("cavern:dimensionConfig", Config.LANG_KEY + "dimension", DimensionConfigEntry.class));

		return list;
	}

	@Override
	public void initGui()
	{
		if (entryList == null || needsRefresh)
		{
			entryList = new CaveConfigGuiEntries(this, mc);
			needsRefresh = false;
		}

		super.initGui();
	}
}