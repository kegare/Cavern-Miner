package cavern.miner.world.vein;

import com.google.common.collect.ImmutableList;

import cavern.miner.config.dimension.HugeCavernConfig;
import cavern.miner.util.BlockStateTagList;
import net.minecraft.block.BlockState;

public class HugeCavernVeinProvider extends VeinProvider
{
	@Override
	public ImmutableList<Vein> getVeins()
	{
		return ImmutableList.copyOf(HugeCavernConfig.INSTANCE.veins.getVeins());
	}

	@Override
	public BlockStateTagList getWhitelist()
	{
		return HugeCavernConfig.INSTANCE.veins.getWhitelist();
	}

	@Override
	public BlockStateTagList getBlacklist()
	{
		return HugeCavernConfig.INSTANCE.veins.getBlacklist();
	}

	@Override
	protected Vein createVein(BlockState state, Rarity rarity)
	{
		Vein.Properties properties = new Vein.Properties();

		switch (rarity)
		{
			case COMMON:
				properties.count(10);
				properties.size(15);
				break;
			case UNCOMMON:
				properties.count(5);
				properties.size(12);
				break;
			case RARE:
				properties.count(3);
				properties.size(7);
				break;
			case EPIC:
				properties.count(1);
				properties.size(5);
				properties.max(30);
				break;
			case EMERALD:
				properties.count(2);
				properties.size(5);
				properties.max(70);
				break;
			case DIAMOND:
				properties.count(1);
				properties.size(5);
				properties.max(20);
				break;
			case AQUA:
				properties.count(3);
				properties.size(5);
				properties.max(70);
				break;
			case RANDOMITE:
				properties.count(5);
				properties.size(5);
				properties.min(20);
				break;
			default:
		}

		return new Vein(state, properties);
	}

	@Override
	protected Vein createVariousVein(BlockState state, Rarity rarity)
	{
		switch (rarity)
		{
			case COMMON:
				return new Vein(state, new Vein.Properties().count(12).size(25));
			case UNCOMMON:
				return new Vein(state, new Vein.Properties().count(10).size(20));
			default:
				return createVein(state, rarity);
		}
	}
}