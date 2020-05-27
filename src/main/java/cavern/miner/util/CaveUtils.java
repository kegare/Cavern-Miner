package cavern.miner.util;

import java.util.List;

import javax.annotation.Nullable;

import cavern.miner.core.CavernMod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.oredict.OreDictionary;

public final class CaveUtils
{
	public static ModContainer getModContainer()
	{
		ModContainer mod = Loader.instance().getIndexedModList().get(CavernMod.MODID);

		if (mod == null)
		{
			mod = Loader.instance().activeModContainer();

			if (mod == null || !CavernMod.MODID.equals(mod.getModId()))
			{
				return new DummyModContainer(CavernMod.metadata);
			}
		}

		return mod;
	}

	public static ResourceLocation getKey(String key)
	{
		return new ResourceLocation(CavernMod.MODID, key);
	}

	public static int compareWithNull(@Nullable Object o1, @Nullable Object o2)
	{
		return (o1 == null ? 1 : 0) - (o2 == null ? 1 : 0);
	}

	@Nullable
	public static <E> E getRandomObject(@Nullable List<E> list)
	{
		return getRandomObject(list, null);
	}

	public static <E> E getRandomObject(@Nullable List<E> list, @Nullable E nullDefault)
	{
		if (list == null || list.isEmpty())
		{
			return nullDefault;
		}

		if (list.size() == 1)
		{
			return list.get(0);
		}

		return list.get(MathHelper.floor(Math.random() * list.size()));
	}

	public static boolean isBlockEqual(@Nullable IBlockState stateA, @Nullable IBlockState stateB)
	{
		if (stateA == null || stateB == null)
		{
			return false;
		}

		if (stateA == stateB)
		{
			return true;
		}

		return stateA.getBlock() == stateB.getBlock() && stateA.getBlock().getMetaFromState(stateA) == stateB.getBlock().getMetaFromState(stateB);
	}

	public static boolean isItemEqual(ItemStack target, ItemStack input)
	{
		if (target.getHasSubtypes())
		{
			return OreDictionary.itemMatches(target, input, false);
		}

		return target.getItem() == input.getItem();
	}

	public static boolean isPickaxe(ItemStack stack)
	{
		if (stack.isEmpty())
		{
			return false;
		}

		Item item = stack.getItem();

		if (item instanceof ItemPickaxe)
		{
			return true;
		}

		if (item.getToolClasses(stack).contains("pickaxe"))
		{
			return true;
		}

		return false;
	}

	public static double getChunkDistance(ChunkPos pos1, ChunkPos pos2)
	{
		double dx = pos1.x - pos2.x;
		double dz = pos1.z - pos2.z;

		return Math.sqrt(dx * dx + dz * dz);
	}
}