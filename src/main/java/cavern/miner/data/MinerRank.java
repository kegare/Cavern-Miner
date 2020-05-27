package cavern.miner.data;

import javax.annotation.Nullable;

import cavern.miner.item.CaveItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public enum MinerRank
{
	BEGINNER(0, 0, 1.0F, "beginner", new ItemStack(Items.WOODEN_PICKAXE)),
	STONE_MINER(1, 100, 1.0F, "stoneMiner", new ItemStack(Items.STONE_PICKAXE)),
	IRON_MINER(2, 500, 1.0F, "ironMiner", new ItemStack(Items.IRON_PICKAXE)),
	MAGNITE_MINER(3, 1000, 1.1F, "magniteMiner", new ItemStack(CaveItems.MAGNITE_PICKAXE)),
	GOLD_MINER(4, 3000, 1.2F, "goldMiner", new ItemStack(Items.GOLDEN_PICKAXE)),
	AQUA_MINER(5, 5000, 1.25F, "aquaMiner", new ItemStack(CaveItems.AQUAMARINE_PICKAXE)),
	HEXCITE_MINER(6, 10000, 1.5F, "hexciteMiner", new ItemStack(CaveItems.HEXCITE_PICKAXE)),
	DIAMOND_MINER(7, 30000, 1.75F, "diamondMiner", new ItemStack(Items.DIAMOND_PICKAXE));

	public static final MinerRank[] VALUES = new MinerRank[values().length];

	private final int rank;
	private final int phase;
	private final float bonus;
	private final String name;
	private final ItemStack iconItem;

	private MinerRank(int rank, int phase, float bonus, String name, @Nullable ItemStack stack)
	{
		this.rank = rank;
		this.phase = phase;
		this.bonus = bonus;
		this.name = name;
		this.iconItem = stack;
	}

	public int getRank()
	{
		return rank;
	}

	public int getPhase()
	{
		return phase;
	}

	public float getBonusChance()
	{
		return bonus;
	}

	public String getName()
	{
		return name;
	}

	public String getUnlocalizedName()
	{
		return "cavern.minerrank." + name;
	}

	public ItemStack getIconItem()
	{
		return iconItem == null ? ItemStack.EMPTY : iconItem;
	}

	public static MinerRank get(int rank)
	{
		if (rank < 0)
		{
			rank = 0;
		}

		int max = VALUES.length - 1;

		if (rank > max)
		{
			rank = max;
		}

		return VALUES[rank];
	}

	static
	{
		for (MinerRank rank : values())
		{
			VALUES[rank.getRank()] = rank;
		}
	}
}