package cavern.miner.world;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cavern.miner.block.BlockCave;
import cavern.miner.block.CaveBlocks;
import cavern.miner.config.CavernConfig;
import cavern.miner.config.manager.CaveVein;
import cavern.miner.config.manager.CaveVeinManager;
import cavern.miner.config.property.ConfigBlocks;
import cavern.miner.util.BlockMeta;
import cavern.miner.util.CaveLog;
import cavern.miner.util.CaveUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

public class VeinProviderCavern
{
	private static final Map<BlockMeta, Rarity> RARITY_MAP = Maps.newHashMap();

	static
	{
		setRarity(Blocks.COAL_ORE.getDefaultState(), Rarity.COMMON);
		setRarity(Blocks.IRON_ORE.getDefaultState(), Rarity.COMMON);
		setRarity(Blocks.GOLD_ORE.getDefaultState(), Rarity.RARE);
		setRarity(Blocks.REDSTONE_ORE.getDefaultState(), Rarity.UNCOMMON);
		setRarity(Blocks.LAPIS_ORE.getDefaultState(), Rarity.RARE);
		setRarity(Blocks.EMERALD_ORE.getDefaultState(), Rarity.EMERALD);
		setRarity(Blocks.DIAMOND_ORE.getDefaultState(), Rarity.DIAMOND);
		setRarity(CaveBlocks.CAVE_BLOCK.getStateFromMeta(BlockCave.EnumType.AQUAMARINE_ORE.getMetadata()), Rarity.AQUA);
		setRarity(CaveBlocks.CAVE_BLOCK.getStateFromMeta(BlockCave.EnumType.MAGNITE_ORE.getMetadata()), Rarity.COMMON);
		setRarity(CaveBlocks.CAVE_BLOCK.getStateFromMeta(BlockCave.EnumType.RANDOMITE_ORE.getMetadata()), Rarity.RANDOMITE);
		setRarity(CaveBlocks.CAVE_BLOCK.getStateFromMeta(BlockCave.EnumType.HEXCITE_ORE.getMetadata()), Rarity.EPIC);
		setRarity(CaveBlocks.CAVE_BLOCK.getStateFromMeta(BlockCave.EnumType.FISSURED_STONE.getMetadata()), Rarity.RANDOMITE);
	}

	public static void setRarity(IBlockState state, Rarity rarity)
	{
		RARITY_MAP.put(new BlockMeta(state), rarity);
	}

	protected final World world;
	protected final Random rand;
	protected final Supplier<CaveVeinManager> veinManager;

	protected Pair<ChunkPos, List<CaveVein>> cachedVeins;

	public VeinProviderCavern(World world, Supplier<CaveVeinManager> manager)
	{
		this.world = world;
		this.rand = new Random();
		this.veinManager = manager;
	}

	@Nullable
	public ConfigBlocks getExemptBlocks()
	{
		return CavernConfig.autoVeinBlacklist;
	}

	@SuppressWarnings("deprecation")
	public final List<CaveVein> getVeins(int chunkX, int chunkZ)
	{
		CaveVeinManager manager = veinManager.get();

		if (manager != null)
		{
			return manager.getCaveVeins();
		}

		List<CaveVein> list;
		ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

		if (cachedVeins == null || CaveUtils.getChunkDistance(cachedVeins.getLeft(), chunkPos) > 3.0D)
		{
			list = Lists.newArrayList();
		}
		else
		{
			return cachedVeins.getRight();
		}

		List<String> oreNames = Arrays.stream(OreDictionary.getOreNames())
			.filter(name -> name.startsWith("ore") && name.length() > 3 && Character.isUpperCase(name.charAt(3))).sorted().collect(Collectors.toList());

		for (String name : oreNames)
		{
			for (ItemStack stack : OreDictionary.getOres(name, false))
			{
				try
				{
					if (stack.isEmpty() || stack.getItem() == Items.AIR || !(stack.getItem() instanceof ItemBlock))
					{
						continue;
					}

					Block block = ((ItemBlock)stack.getItem()).getBlock();

					if (block == null || block instanceof BlockAir)
					{
						continue;
					}

					IBlockState state = block.getStateFromMeta(stack.getItemDamage());

					if (getExemptBlocks() != null && getExemptBlocks().hasBlockState(state))
					{
						continue;
					}

					BlockMeta blockMeta = new BlockMeta(state);
					Rarity rarity = RARITY_MAP.get(blockMeta);

					if (rarity == null)
					{
						for (Rarity type : Rarity.values())
						{
							if (type.hasOreDict() && name.equals(type.getOreDictName()))
							{
								rarity = type;

								break;
							}
						}
					}

					if (rarity == null)
					{
						String variant = name.substring(3).toLowerCase();

						int harvestLevel = block.getHarvestLevel(state);
						int level = harvestLevel;

						Item pickaxe = ForgeRegistries.ITEMS.getValue(new ResourceLocation(block.getRegistryName().getResourceDomain(), variant + "_pickaxe"));
						double toolRarity = 1.0D;

						if (pickaxe != null)
						{
							ItemStack dummy = new ItemStack(pickaxe);
							int max = pickaxe.getMaxDamage(dummy);
							float destroy = pickaxe.getDestroySpeed(dummy, Blocks.COAL_ORE.getDefaultState());
							int enchant = pickaxe.getItemEnchantability(dummy);
							int harvest = pickaxe.getHarvestLevel(dummy, "pickaxe", null, null);

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

						rarity = Rarity.COMMON;

						RARITY_MAP.put(blockMeta, rarity);
					}

					List<CaveVein> veins = createVeins(name, blockMeta, rarity);

					if (veins == null || veins.isEmpty())
					{
						CaveVein vein = createVein(name, blockMeta, rarity);

						if (vein != null)
						{
							list.add(vein);
						}
					}
					else
					{
						list.addAll(veins);
					}
				}
				catch (Exception e)
				{
					CaveLog.log(Level.ERROR, e, "An error occurred while setup. Skip: {%s} %s", name, stack.toString());
				}
			}
		}

		getSubVeins(list);

		cachedVeins = Pair.of(chunkPos, list);

		return list;
	}

	@SuppressWarnings("deprecation")
	protected void getSubVeins(List<CaveVein> list)
	{
		List<String> oreNames = Arrays.stream(OreDictionary.getOreNames())
			.filter(name -> name.startsWith("stone") && name.length() > 5 && Character.isUpperCase(name.charAt(5))).sorted().collect(Collectors.toList());

		oreNames.add("dirt");
		oreNames.add("gravel");

		int max = world.getActualHeight() - 1;

		for (String name : oreNames)
		{
			for (ItemStack stack : OreDictionary.getOres(name, false))
			{
				try
				{
					if (stack.isEmpty() || stack.getItem() == Items.AIR || !(stack.getItem() instanceof ItemBlock))
					{
						continue;
					}

					Block block = ((ItemBlock)stack.getItem()).getBlock();

					if (block == null || block instanceof BlockAir)
					{
						continue;
					}

					IBlockState state = block.getStateFromMeta(stack.getItemDamage());

					if (getExemptBlocks() != null && getExemptBlocks().hasBlockState(state))
					{
						continue;
					}

					int weight = MathHelper.getInt(rand, 25, 40);
					int size = MathHelper.getInt(rand, 10, 30);

					list.add(new CaveVein(new BlockMeta(state), weight, size, 1, max));
				}
				catch (Exception e)
				{
					CaveLog.log(Level.ERROR, e, "An error occurred while setup. Skip: {%s} %s", name, stack.toString());
				}
			}
		}
	}

	@Nullable
	protected CaveVein createVein(String name, BlockMeta blockMeta, Rarity rarity)
	{
		int weight = 20;
		int size = 5;
		int min = 1;
		int max = world.getActualHeight() - 1;
		Object[] biome = null;

		switch (rarity)
		{
			case COMMON:
				weight = MathHelper.getInt(rand, 15, 20);
				size = MathHelper.getInt(rand, 10, 20);
				break;
			case UNCOMMON:
				weight = MathHelper.getInt(rand, 12, 15);
				size = MathHelper.getInt(rand, 7, 10);
				break;
			case RARE:
				weight = MathHelper.getInt(rand, 10, 12);
				size = MathHelper.getInt(rand, 4, 7);
				break;
			case EPIC:
				weight = MathHelper.getInt(rand, 1, 3);
				size = MathHelper.getInt(rand, 2, 7);
				max = 30;
				break;
			case EMERALD:
				weight = MathHelper.getInt(rand, 1, 5);
				size = MathHelper.getInt(rand, 1, 5);
				min = 50;
				biome = ArrayUtils.toArray(BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.HILLS);
				break;
			case DIAMOND:
				weight = MathHelper.getInt(rand, 1, 2);
				size = MathHelper.getInt(rand, 2, 6);
				max = 20;
				break;
			case AQUA:
				weight = MathHelper.getInt(rand, 5, 7);
				size = MathHelper.getInt(rand, 2, 7);
				max = 70;
				biome = ArrayUtils.toArray(BiomeDictionary.Type.COLD, BiomeDictionary.Type.WET, BiomeDictionary.Type.OCEAN, BiomeDictionary.Type.RIVER);
				break;
			case RANDOMITE:
				weight = MathHelper.getInt(rand, 5, 10);
				size = MathHelper.getInt(rand, 1, 4);
				min = 20;
				break;
		}

		if (weight <= 0 || size <= 0)
		{
			return null;
		}

		return new CaveVein(blockMeta, weight, size, min, max, biome);
	}

	@Nullable
	protected List<CaveVein> createVeins(String name, BlockMeta blockMeta, Rarity rarity)
	{
		return null;
	}

	public enum Rarity
	{
		COMMON,
		UNCOMMON,
		RARE,
		EPIC,
		EMERALD("oreEmerald"),
		DIAMOND("oreDiamond"),
		AQUA("oreAquamarine"),
		RANDOMITE("oreRandomite");

		private final String oreName;

		private Rarity()
		{
			this.oreName = null;
		}

		private Rarity(String oredict)
		{
			this.oreName = oredict;
		}

		public boolean hasOreDict()
		{
			return !Strings.isNullOrEmpty(oreName);
		}

		public String getOreDictName()
		{
			return oreName;
		}
	}
}