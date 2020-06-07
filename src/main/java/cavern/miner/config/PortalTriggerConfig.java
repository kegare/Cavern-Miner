package cavern.miner.config;

import java.io.File;

import net.minecraftforge.common.Tags;

public class PortalTriggerConfig extends ItemStackTagListConfig
{
	public PortalTriggerConfig(File dir, String name)
	{
		super(dir, name + "_portal_triggers");
	}

	@Override
	public void setDefault()
	{
		list.clear();
		list.add(Tags.Items.GEMS_EMERALD);
	}
}