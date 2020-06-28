package cavern.miner.world.vein;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import cavern.miner.util.BlockStateTagList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.OreBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class VeinProvider
{
	private static final Map<Block, Rarity> RARITY_CACHE = new HashMap<>();

	protected final List<Vein> autoEntries = new ArrayList<>();

	public List<Vein> getVeins()
	{
		return Collections.emptyList();
	}

	@Nullable
	public BlockStateTagList getWhitelist()
	{
		return null;
	}

	@Nullable
	public BlockStateTagList getBlacklist()
	{
		return null;
	}

	@Nullable
	public List<String> getBlacklistMods()
	{
		return null;
	}

	protected Rarity getOreRarity(BlockState state)
	{
		Rarity rarity = OreRegistry.getEntry(state).getRarity().orElse(null);

		if (rarity != null)
		{
			return rarity;
		}

		Block block = state.getBlock();

		if (RARITY_CACHE.containsKey(block))
		{
			return RARITY_CACHE.get(block);
		}

		int harvestLevel = state.getHarvestLevel();

		if (harvestLevel < 0 || state.getHarvestTool() != ToolType.PICKAXE)
		{
			return Rarity.UNKNOWN;
		}

		int level = harvestLevel;

		String name = block.getRegistryName().getPath();

		if (name.contains("_"))
		{
			name = name.substring(0, name.lastIndexOf('_'));
		}

		Item pickaxe = ForgeRegistries.ITEMS.getValue(new ResourceLocation(block.getRegistryName().getNamespace(), name + "_pickaxe"));
		double toolRarity = 1.0D;

		if (pickaxe != null)
		{
			ItemStack dummy = new ItemStack(pickaxe);
			int max = pickaxe.getMaxDamage(dummy);
			float destroy = pickaxe.getDestroySpeed(dummy, Blocks.COAL_ORE.getDefaultState());
			int enchant = pickaxe.getItemEnchantability(dummy);
			int harvest = pickaxe.getHarvestLevel(dummy, ToolType.PICKAXE, null, null);

			toolRarity = max * 0.01D + destroy * 0.01D + enchant * 0.01D + harvest * 1.0D;
		}
		else if (harvestLevel > 0)
		{
			++level;
		}

		if (toolRarity >= 12.0D)
		{
			level += MathHelper.ceil(toolRarity * 0.3D) - 3;
		}

		if (level > 3)
		{
			rarity = Rarity.EPIC;
		}
		else if (level > 2)
		{
			rarity = Rarity.RARE;
		}
		else if (level > 1)
		{
			rarity = Rarity.UNCOMMON;
		}
		else
		{
			rarity = Rarity.COMMON;
		}

		RARITY_CACHE.put(block, rarity);

		return rarity;
	}

	protected Rarity getVariousRarity(BlockState state)
	{
		if (state.hasTileEntity() || !state.isSolid())
		{
			return Rarity.UNKNOWN;
		}

		Block block = state.getBlock();

		if (block instanceof IGrowable)
		{
			return Rarity.UNKNOWN;
		}

		if (block.isIn(Tags.Blocks.STONE) || block.isIn(Tags.Blocks.DIRT))
		{
			return Rarity.COMMON;
		}

		if (block.isIn(Tags.Blocks.GRAVEL) || block.isIn(Tags.Blocks.SAND))
		{
			return Rarity.UNCOMMON;
		}

		return Rarity.RARE;
	}

	public List<Vein> getAutoEntries()
	{
		if (getWhitelist() == null)
		{
			return Collections.emptyList();
		}

		if (autoEntries.isEmpty())
		{
			setupAutoEntries();
		}

		return autoEntries;
	}

	protected void setupAutoEntries()
	{
		autoEntries.clear();

		if (getWhitelist() == null)
		{
			return;
		}

		for (BlockState state : getWhitelist())
		{
			if (getBlacklist() != null && getBlacklist().contains(state))
			{
				continue;
			}

			Block block = state.getBlock();

			if (getBlacklistMods() != null && getBlacklistMods().contains(block.getRegistryName().getNamespace()))
			{
				continue;
			}

			if (block.isIn(Tags.Blocks.ORES) || block instanceof OreBlock || block instanceof RedstoneOreBlock)
			{
				Rarity rarity = getOreRarity(state);

				if (rarity == Rarity.UNKNOWN)
				{
					continue;
				}

				List<Vein> veins = createVeins(state, rarity);

				if (veins.isEmpty())
				{
					Vein vein = createVein(state, rarity);

					if (vein.getCount() > 0 && vein.getSize() > 0)
					{
						autoEntries.add(vein);
					}
				}
				else for (Vein vein : veins)
				{
					if (vein.getCount() > 0 && vein.getSize() > 0)
					{
						autoEntries.add(vein);
					}
				}
			}
			else
			{
				Rarity rarity = getVariousRarity(state);

				if (rarity == Rarity.UNKNOWN)
				{
					continue;
				}

				List<Vein> veins = createVariousVeins(state, rarity);

				if (veins.isEmpty())
				{
					Vein vein = createVariousVein(state, rarity);

					if (vein.getCount() > 0 && vein.getSize() > 0)
					{
						autoEntries.add(vein);
					}
				}
				else for (Vein vein : veins)
				{
					if (vein.getCount() > 0 && vein.getSize() > 0)
					{
						autoEntries.add(vein);
					}
				}
			}
		}
	}

	protected Vein createVein(BlockState state, Rarity rarity)
	{
		Vein.Properties properties = new Vein.Properties();

		switch (rarity)
		{
			case COMMON:
				properties.count(20);
				properties.size(10);
				break;
			case UNCOMMON:
				properties.count(15);
				properties.size(7);
				break;
			case RARE:
				properties.count(10);
				properties.size(5);
				break;
			case EPIC:
				properties.count(2);
				properties.size(5);
				properties.max(30);
				break;
			case EMERALD:
				properties.count(5);
				properties.size(3);
				properties.max(50);
				break;
			case DIAMOND:
				properties.count(1);
				properties.size(5);
				properties.max(20);
				break;
			case AQUA:
				properties.count(5);
				properties.size(7);
				properties.max(70);
				break;
			case RANDOMITE:
				properties.count(5);
				properties.size(3);
				properties.min(20);
				break;
			default:
		}

		return new Vein(state, properties);
	}

	protected List<Vein> createVeins(BlockState state, Rarity rarity)
	{
		return Collections.emptyList();
	}

	protected Vein createVariousVein(BlockState state, Rarity rarity)
	{
		switch (rarity)
		{
			case COMMON:
				return new Vein(state, new Vein.Properties().count(30).size(20));
			case UNCOMMON:
				return new Vein(state, new Vein.Properties().count(20).size(10));
			default:
				return createVein(state, rarity);
		}
	}

	protected List<Vein> createVariousVeins(BlockState state, Rarity rarity)
	{
		return Collections.emptyList();
	}

	public enum Rarity
	{
		UNKNOWN,
		COMMON,
		UNCOMMON,
		RARE,
		EPIC,
		EMERALD,
		DIAMOND,
		AQUA,
		RANDOMITE;
	}
}