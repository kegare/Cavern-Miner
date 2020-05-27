package cavern.miner.block;

import java.util.Random;

import cavern.miner.api.block.CaveOre;
import cavern.miner.api.event.RandomiteDropEvent;
import cavern.miner.block.RandomiteHelper.Category;
import cavern.miner.config.GeneralConfig;
import cavern.miner.core.CavernMod;
import cavern.miner.item.CaveItems;
import cavern.miner.item.ItemCave;
import cavern.miner.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCave extends Block implements CaveOre
{
	public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);

	public BlockCave()
	{
		super(Material.ROCK);
		this.setDefaultState(blockState.getBaseState().withProperty(VARIANT, EnumType.AQUAMARINE_ORE));
		this.setUnlocalizedName("blockCave");
		this.setCreativeTab(CavernMod.TAB_CAVERN);
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, VARIANT);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
	}

	public EnumType getType(IBlockState state)
	{
		if (state == null || state.getBlock() != this || state.getPropertyKeys().isEmpty())
		{
			state = getDefaultState();
		}

		return state.getValue(VARIANT);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return getType(state).getMetadata();
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess blockAccess, BlockPos pos)
	{
		return getType(state).getMapColor();
	}

	@Override
	public Material getMaterial(IBlockState state)
	{
		return getType(state).getMaterial();
	}

	@Override
	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity)
	{
		return getType(state).getSoundType();
	}

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos)
	{
		return getType(state).getBlockHardness();
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion)
	{
		return getType(world.getBlockState(pos)).getBlockHardness() * 5.0F;
	}

	@Override
	public String getHarvestTool(IBlockState state)
	{
		return "pickaxe";
	}

	@Override
	public int getHarvestLevel(IBlockState state)
	{
		return getType(state).getHarvestLevel();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		for (EnumType type : EnumType.VALUES)
		{
			list.add(type.getItemStack());
		}
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		if (getType(state) == EnumType.RANDOMITE_ORE)
		{
			if (world instanceof World && ((World)world).isRemote)
			{
				return;
			}

			ItemStack drop = ItemStack.EMPTY;

			if (GeneralConfig.generousRandomite && fortune > 0)
			{
				double d = fortune / Enchantments.FORTUNE.getMaxLevel();

				if (d >= 1.0D || RANDOM.nextDouble() < d)
				{
					drop = RandomiteHelper.getRandomItem();
				}
			}

			if (drop.isEmpty())
			{
				Category category = Category.COMMON;

				if (RANDOM.nextInt(20) == 0)
				{
					category = Category.FOOD;
				}

				ItemStack stack = RandomiteHelper.getDropItem(category);

				if (!stack.isEmpty())
				{
					if (fortune > 0 && stack.isStackable())
					{
						stack.grow(RANDOM.nextInt(fortune) + 1);
					}

					drop = stack;
				}
			}

			RandomiteDropEvent event = new RandomiteDropEvent(world, pos, state, drop);

			MinecraftForge.EVENT_BUS.post(event);

			if (!event.getDropItem().isEmpty())
			{
				drops.add(event.getDropItem());
			}

			return;
		}

		super.getDrops(drops, world, pos, state, fortune);
	}

	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune)
	{
		if (!world.isRemote && !world.restoringBlockSnapshots)
		{
			EnumType type = getType(state);

			if (type == EnumType.FISSURED_STONE)
			{
				EntityPlayer player = harvesters.get();

				if (fortune > 0 && player != null)
				{
					FissureHelper.fireIntensiveEffect(player, fortune);
				}
				else if (RANDOM.nextDouble() < 0.1D)
				{
					FissureHelper.fireExplosion(world, pos);
				}
				else
				{
					FissureHelper.fireAreaEffect(world, pos, player);
				}

				return;
			}
		}

		super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player)
	{
		super.onBlockHarvested(world, pos, state, player);

		if (world.isRemote || player.capabilities.isCreativeMode)
		{
			return;
		}

		switch (getType(state))
		{
			case RANDOMITE_ORE:
				PlayerUtils.grantAdvancement(player, "mine_randomite");
				break;
			case FISSURED_STONE:
				PlayerUtils.grantAdvancement(player, "mine_fissure");
				break;
			default:
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		switch (getType(state))
		{
			case AQUAMARINE_ORE:
			case HEXCITE_ORE:
				return CaveItems.CAVE_ITEM;
			case RANDOMITE_ORE:
			case FISSURED_STONE:
				return Items.AIR;
			default:
		}

		return Item.getItemFromBlock(this);
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		switch (getType(state))
		{
			case AQUAMARINE_ORE:
				return ItemCave.EnumType.AQUAMARINE.getMetadata();
			case HEXCITE_ORE:
				return ItemCave.EnumType.HEXCITE.getMetadata();
			default:
		}

		return getMetaFromState(state);
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random)
	{
		int amount = quantityDropped(random);

		switch (getType(state))
		{
			case AQUAMARINE_ORE:
			case HEXCITE_ORE:
				if (fortune > 0)
				{
					return amount * (Math.max(random.nextInt(fortune + 2) - 1, 0) + 1);
				}

				break;
			case RANDOMITE_ORE:
			case FISSURED_STONE:
				return 0;
			default:
		}

		return amount;
	}

	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune)
	{
		switch (getType(state))
		{
			case AQUAMARINE_ORE:
			case RANDOMITE_ORE:
				return MathHelper.getInt(RANDOM, 1, 3);
			case HEXCITE_ORE:
			case FISSURED_STONE:
				return MathHelper.getInt(RANDOM, 3, 5);
			default:
		}

		return 0;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return new ItemStack(this, 1, getMetaFromState(state));
	}

	@Override
	public int getMiningPoint(IBlockState state)
	{
		switch (getType(state))
		{
			case AQUAMARINE_ORE:
			case RANDOMITE_ORE:
			case FISSURED_STONE:
				return 2;
			case MAGNITE_ORE:
				return 1;
			case HEXCITE_ORE:
				return 3;
			default:
		}

		return 0;
	}

	@Override
	public int getMiningPoint(World world, BlockPos pos, IBlockState state, EntityPlayer player, int fortune)
	{
		switch (getType(state))
		{
			case RANDOMITE_ORE:
			case FISSURED_STONE:
				return MathHelper.getInt(RANDOM, 1, 3);
			default:
		}

		return getMiningPoint(state);
	}

	public enum EnumType implements IStringSerializable
	{
		AQUAMARINE_ORE(0, "aquamarine_ore", "oreAquamarine", MapColor.DIAMOND, Material.ROCK, SoundType.STONE, 3.0F, 1),
		AQUAMARINE_BLOCK(1, "aquamarine_block", "blockAquamarine", MapColor.DIAMOND, Material.IRON, SoundType.METAL, 3.5F, 1),
		MAGNITE_ORE(2, "magnite_ore", "oreMagnite", MapColor.RED, Material.ROCK, SoundType.STONE, 3.0F, 2),
		MAGNITE_BLOCK(3, "magnite_block", "blockMagnite", MapColor.RED, Material.IRON, SoundType.METAL, 2.5F, 2),
		RANDOMITE_ORE(4, "randomite_ore", "oreRandomite", MapColor.PURPLE, Material.ROCK, SoundType.STONE, 4.0F, 1),
		HEXCITE_ORE(5, "hexcite_ore", "oreHexcite", MapColor.SNOW, Material.ROCK, SoundType.STONE, 3.0F, 2),
		HEXCITE_BLOCK(6, "hexcite_block", "blockHexcite", MapColor.SNOW, Material.IRON, SoundType.METAL, 3.5F, 2),
		FISSURED_STONE(7, "fissured_stone", "stone.stone", MapColor.STONE, Material.ROCK, SoundType.STONE, 1.0F, 0);

		public static final EnumType[] VALUES = new EnumType[values().length];

		private final int meta;
		private final String name;
		private final String translationKey;
		private final MapColor mapColor;
		private final Material material;
		private final SoundType soundType;
		private final float blockHardness;
		private final int harvestLevel;

		private EnumType(int meta, String name, String key, MapColor color, Material material, SoundType soundType, float hardness, int harvestLevel)
		{
			this.meta = meta;
			this.name = name;
			this.translationKey = key;
			this.mapColor = color;
			this.material = material;
			this.soundType = soundType;
			this.blockHardness = hardness;
			this.harvestLevel = harvestLevel;
		}

		public int getMetadata()
		{
			return meta;
		}

		public MapColor getMapColor()
		{
			return mapColor;
		}

		public Material getMaterial()
		{
			return material;
		}

		public SoundType getSoundType()
		{
			return soundType;
		}

		public float getBlockHardness()
		{
			return blockHardness;
		}

		public int getHarvestLevel()
		{
			return harvestLevel;
		}

		@Override
		public String toString()
		{
			return name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		public String getTranslationKey()
		{
			return translationKey;
		}

		public ItemStack getItemStack()
		{
			return getItemStack(1);
		}

		public ItemStack getItemStack(int amount)
		{
			return new ItemStack(CaveBlocks.CAVE_BLOCK, amount, getMetadata());
		}

		public static EnumType byMetadata(int meta)
		{
			if (meta < 0 || meta >= VALUES.length)
			{
				meta = 0;
			}

			return VALUES[meta];
		}

		public static EnumType byItemStack(ItemStack stack)
		{
			return byMetadata(stack.isEmpty() ? 0 : stack.getMetadata());
		}

		static
		{
			for (EnumType type : values())
			{
				VALUES[type.getMetadata()] = type;
			}
		}
	}
}