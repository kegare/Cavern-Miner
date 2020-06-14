package cavern.miner.world.vein;

import java.util.Random;

import com.google.common.collect.ImmutableList;

import cavern.miner.config.HugeCavernConfig;
import cavern.miner.util.BlockStateTagList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;

public class HugeCavernVeinProvider extends VeinProvider
{
	@Override
	public ImmutableList<Vein> getVeins()
	{
		return ImmutableList.copyOf(HugeCavernConfig.VEINS.getVeins());
	}

	@Override
	public BlockStateTagList getWhitelist()
	{
		return HugeCavernConfig.VEINS.getWhitelist();
	}

	@Override
	public BlockStateTagList getBlacklist()
	{
		return HugeCavernConfig.VEINS.getBlacklist();
	}

	@Override
	protected Vein createVein(BlockState state, Rarity rarity, IWorld world, Random rand)
	{
		Vein.Properties properties = new Vein.Properties().max(world.getMaxHeight() - 1);

		switch (rarity)
		{
			case COMMON:
				properties.count(MathHelper.nextInt(rand, 10, 15));
				properties.size(MathHelper.nextInt(rand, 20, 25));
				break;
			case UNCOMMON:
				properties.count(MathHelper.nextInt(rand, 7, 10));
				properties.size(MathHelper.nextInt(rand, 15, 20));
				break;
			case RARE:
				properties.count(MathHelper.nextInt(rand, 5, 7));
				properties.size(MathHelper.nextInt(rand, 7, 10));
				break;
			case EPIC:
				properties.count(1);
				properties.size(MathHelper.nextInt(rand, 5, 7));
				properties.max(30);
				break;
			case EMERALD:
				properties.count(2);
				properties.size(MathHelper.nextInt(rand, 3, 7));
				properties.max(70);
				break;
			case DIAMOND:
				properties.count(1);
				properties.size(5);
				properties.max(20);
				break;
			case AQUA:
				properties.count(MathHelper.nextInt(rand, 2, 5));
				properties.size(MathHelper.nextInt(rand, 5, 10));
				properties.max(70);
				break;
			case RANDOMITE:
				properties.count(MathHelper.nextInt(rand, 2, 5));
				properties.size(MathHelper.nextInt(rand, 3, 5));
				properties.min(20);
				break;
			default:
		}

		return new Vein(state, properties);
	}
}