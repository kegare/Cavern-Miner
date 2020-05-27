package cavern.miner.plugin;

import cavern.miner.entity.monster.EntityCaveman;
import cavern.miner.entity.monster.EntityCavenicCreeper;
import cavern.miner.entity.monster.EntityCavenicSkeleton;
import cavern.miner.entity.monster.EntityCavenicSpider;
import cavern.miner.entity.monster.EntityCavenicWitch;
import cavern.miner.entity.monster.EntityCavenicZombie;
import cavern.miner.item.CaveItems;
import defeatedcrow.hac.api.damage.DamageAPI;
import net.minecraftforge.fml.common.Optional.Method;

public class HaCPlugin
{
	public static final String LIB_MODID = "dcs_climate|lib";

	@Method(modid = LIB_MODID)
	public static void load()
	{
		DamageAPI.armorRegister.registerMaterial(CaveItems.HEXCITE_ARMOR, 0.5F, 0.5F);

		DamageAPI.resistantData.registerEntityResistant(EntityCavenicSkeleton.class, 7.0F, 4.0F);
		DamageAPI.resistantData.registerEntityResistant(EntityCavenicCreeper.class, 2.0F, 5.0F);
		DamageAPI.resistantData.registerEntityResistant(EntityCavenicZombie.class, 8.0F, 5.0F);
		DamageAPI.resistantData.registerEntityResistant(EntityCavenicSpider.class, 6.0F, 4.0F);
		DamageAPI.resistantData.registerEntityResistant(EntityCavenicWitch.class, 6.0F, 6.0F);
		DamageAPI.resistantData.registerEntityResistant(EntityCaveman.class, 5.0F, 3.0F);
	}
}