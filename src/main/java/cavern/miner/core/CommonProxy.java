package cavern.miner.core;

import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;

@SuppressWarnings("deprecation")
public class CommonProxy
{
	public String translate(String key)
	{
		return I18n.translateToLocal(key);
	}

	public String translateFormat(String key, Object... format)
	{
		return I18n.translateToLocalFormatted(key, format);
	}

	public boolean isSinglePlayer()
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer();
	}
}