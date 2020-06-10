package cavern.miner.world.vein;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

import cavern.miner.util.BlockStateTagList;
import cavern.miner.vein.OreRegistry;
import cavern.miner.vein.Vein;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.OreBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class VeinProvider
{
	private static final Map<Block, Rarity> RARITY_CACHE = new HashMap<>();

	protected Pair<ChunkPos, NonNullList<Vein>> cachedVeins;

	@Nullable
	public NonNullList<Vein> getVeins()
	{
		return null;
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

	protected Rarity getOreRarity(BlockState state)
	{
		Rarity rarity = OreRegistry.getEntry(state).getRarity();

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

	public NonNullList<Vein> getVeins(IWorld world, IChunk chunk, Random rand)
	{
		ChunkPos chunkPos = chunk.getPos();

		if (cachedVeins != null && chunkPos.getChessboardDistance(cachedVeins.getLeft()) <= 3)
		{
			return cachedVeins.getRight();
		}

		NonNullList<Vein> list = ObjectUtils.defaultIfNull(getVeins(), NonNullList.create());

		if (getWhitelist() != null)
		{
			for (BlockState state : getWhitelist())
			{
				if (getBlacklist() != null && getBlacklist().contains(state))
				{
					continue;
				}

				Block block = state.getBlock();

				if (block.isIn(Tags.Blocks.ORES) || block instanceof OreBlock || block instanceof RedstoneOreBlock)
				{
					Rarity rarity = getOreRarity(state);

					if (rarity == Rarity.UNKNOWN)
					{
						continue;
					}

					List<Vein> veins = createVeins(state, rarity, world, rand);

					if (veins.isEmpty())
					{
						Vein vein = createVein(state, rarity, world, rand);

						if (vein.getCount() > 0 && vein.getSize() > 0)
						{
							list.add(vein);
						}
					}
					else for (Vein vein : veins)
					{
						if (vein.getCount() > 0 && vein.getSize() > 0)
						{
							list.add(vein);
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

					List<Vein> veins = createVariousVeins(state, rarity, world, rand);

					if (veins.isEmpty())
					{
						Vein vein = createVariousVein(state, rarity, world, rand);

						if (vein.getCount() > 0 && vein.getSize() > 0)
						{
							list.add(vein);
						}
					}
					else for (Vein vein : veins)
					{
						if (vein.getCount() > 0 && vein.getSize() > 0)
						{
							list.add(vein);
						}
					}
				}
			}
		}

		cachedVeins = Pair.of(chunkPos, list);

		return list;
	}

	protected Vein createVein(BlockState state, Rarity rarity, IWorld world, Random rand)
	{
		Vein.Properties properties = new Vein.Properties().max(world.getMaxHeight() - 1);

		switch (rarity)
		{
			case COMMON:
				properties.count(MathHelper.nextInt(rand, 15, 20));
				properties.size(MathHelper.nextInt(rand, 10, 20));
				break;
			case UNCOMMON:
				properties.count(MathHelper.nextInt(rand, 12, 15));
				properties.size(MathHelper.nextInt(rand, 7, 10));
				break;
			case RARE:
				properties.count(MathHelper.nextInt(rand, 10, 12));
				properties.size(MathHelper.nextInt(rand, 4, 7));
				break;
			case EPIC:
				properties.count(MathHelper.nextInt(rand, 1, 3));
				properties.size(MathHelper.nextInt(rand, 2, 7));
				properties.max(30);
				break;
			case EMERALD:
				properties.count(MathHelper.nextInt(rand, 1, 5));
				properties.size(MathHelper.nextInt(rand, 1, 5));
				properties.max(50);
				break;
			case DIAMOND:
				properties.count(MathHelper.nextInt(rand, 1, 2));
				properties.size(MathHelper.nextInt(rand, 2, 6));
				properties.max(20);
				break;
			case AQUA:
				properties.count(MathHelper.nextInt(rand, 5, 7));
				properties.size(MathHelper.nextInt(rand, 2, 7));
				properties.max(70);
				break;
			case RANDOMITE:
				properties.count(MathHelper.nextInt(rand, 5, 10));
				properties.size(MathHelper.nextInt(rand, 1, 4));
				properties.min(20);
				break;
			default:
		}

		return new Vein(state, properties);
	}

	protected List<Vein> createVeins(BlockState state, Rarity rarity, IWorld world, Random rand)
	{
		return Collections.emptyList();
	}

	protected Vein createVariousVein(BlockState state, Rarity rarity, IWorld world, Random rand)
	{
		switch (rarity)
		{
			case COMMON:
				return new Vein(state, new Vein.Properties().max(world.getMaxHeight() - 1).count(MathHelper.nextInt(rand, 25, 40)).size(MathHelper.nextInt(rand, 10, 30)));
			case UNCOMMON:
				return new Vein(state, new Vein.Properties().max(world.getMaxHeight() - 1).count(MathHelper.nextInt(rand, 20, 30)).size(MathHelper.nextInt(rand, 10, 20)));
			default:
				return createVein(state, rarity, world, rand);
		}
	}

	protected List<Vein> createVariousVeins(BlockState state, Rarity rarity, IWorld world, Random rand)
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