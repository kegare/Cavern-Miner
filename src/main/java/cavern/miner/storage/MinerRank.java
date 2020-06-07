package cavern.miner.storage;

import java.util.function.Supplier;

import cavern.miner.init.CaveItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.LazyValue;

public enum MinerRank
{
	BEGINNER(0, () -> new ItemStack(Items.WOODEN_PICKAXE)),
	STONE(300, () -> new ItemStack(Items.STONE_PICKAXE)),
	IRON(1000, () -> new ItemStack(Items.IRON_PICKAXE)),
	MAGNITE(3000, () -> new ItemStack(CaveItems.MAGNITE_PICKAXE.get())),
	GOLD(5000, () -> new ItemStack(Items.GOLDEN_PICKAXE)),
	AQUA(10000, () -> new ItemStack(CaveItems.AQUAMARINE_PICKAXE.get())),
	DIAMOND(50000, () -> new ItemStack(Items.DIAMOND_PICKAXE));

	private final int phase;
	private final LazyValue<ItemStack> iconItem;

	private String translationKey;

	private MinerRank(int phase, Supplier<ItemStack> icon)
	{
		this.phase = phase;
		this.iconItem = new LazyValue<>(icon);
	}

	public int getPhase()
	{
		return phase;
	}

	public ItemStack getIconItem()
	{
		return iconItem.getValue();
	}

	public String getTranslationKey()
	{
		if (translationKey == null)
		{
			translationKey = "cavern.miner." + toString().toLowerCase();
		}

		return translationKey;
	}

	public MinerRank next()
	{
		MinerRank[] values = values();
		int max = values.length - 1;
		int i = ordinal();

		if (i == max)
		{
			return this;
		}

		return values[++i];
	}

	public MinerRank prev()
	{
		MinerRank[] values = values();
		int i = ordinal();

		if (i == 0)
		{
			return this;
		}

		return values[--i];
	}
}