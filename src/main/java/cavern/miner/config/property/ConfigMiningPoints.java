package cavern.miner.config.property;

import java.util.Arrays;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import cavern.miner.config.MiningPointHelper;
import cavern.miner.util.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class ConfigMiningPoints
{
	private final Table<Block, Integer, Integer> points = HashBasedTable.create();

	private String[] values;

	public String[] getValues()
	{
		if (values == null)
		{
			values = new String[0];
		}

		return values;
	}

	public void setValues(String[] entries)
	{
		values = entries;
	}

	public boolean isEmpty()
	{
		return points.isEmpty();
	}

	public void refreshPoints()
	{
		points.clear();

		Arrays.stream(values).map(String::trim).filter(value -> value.contains(",") && value.length() > 3).forEach(value ->
		{
			int i = value.indexOf(',');
			String str = value.substring(0, i).trim();
			int point = NumberUtils.toInt(value.substring(i + 1));

			if (OreDictionary.doesOreNameExist(str))
			{
				setPoint(str, point);
			}
			else
			{
				if (!str.contains(":"))
				{
					str = "minecraft:" + str;
				}

				BlockMeta blockMeta;

				if (str.indexOf(':') != str.lastIndexOf(':'))
				{
					i = str.lastIndexOf(':');

					blockMeta = new BlockMeta(str.substring(0, i), str.substring(i + 1));
				}
				else
				{
					blockMeta = new BlockMeta(str, 0);
				}

				if (!(blockMeta.getBlock() instanceof BlockAir))
				{
					setPoint(blockMeta, point);
				}
			}
		});
	}

	public void setPoint(Block block, int meta, int amount)
	{
		if (meta == OreDictionary.WILDCARD_VALUE)
		{
			for (int i = 0; i < 16; ++i)
			{
				points.put(block, i, amount);
			}
		}
		else
		{
			points.put(block, meta, amount);
		}
	}

	public void setPoint(BlockMeta blockMeta, int amount)
	{
		setPoint(blockMeta.getBlock(), blockMeta.getMeta(), amount);
	}

	public void setPoint(String name, int amount)
	{
		NonNullList<ItemStack> ores = OreDictionary.getOres(name, false);

		if (ores.isEmpty())
		{
			return;
		}

		for (ItemStack stack : ores)
		{
			Block block = Block.getBlockFromItem(stack.getItem());

			if (block != null && !(block instanceof BlockAir))
			{
				setPoint(block, stack.getMetadata(), amount);
			}
		}
	}

	public int getPoint(IBlockState state)
	{
		Block block = state.getBlock();
		int meta = state.getBlock().getMetaFromState(state);

		if (!points.isEmpty())
		{
			Integer ret = points.get(block, meta);

			return ret == null ? 0 : ret;
		}

		return MiningPointHelper.getPoint(state);
	}
}