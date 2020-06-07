package cavern.miner.config;

import net.minecraftforge.common.Tags;

public class RandomiteDropConfig extends ItemStackTagListConfig
{
	public RandomiteDropConfig()
	{
		super(CavernModConfig.getConfigDir(), "randomite_drops");
	}

	@Override
	public void setDefault()
	{
		list.clear();
		list.add(Tags.Items.INGOTS).add(Tags.Items.NUGGETS).add(Tags.Items.GEMS).add(Tags.Items.DUSTS).add(Tags.Items.RODS);
		list.add(Tags.Items.ENDER_PEARLS).add(Tags.Items.BONES).add(Tags.Items.GUNPOWDER).add(Tags.Items.STRING);
		list.add(Tags.Items.SEEDS).add(Tags.Items.CROPS).add(Tags.Items.DYES);
	}
}