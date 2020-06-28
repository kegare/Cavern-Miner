package cavern.miner.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public class TeleporterCache implements INBTSerializable<CompoundNBT>
{
	private final Map<ResourceLocation, DimensionType> lastDim = new HashMap<>();
	private final Table<ResourceLocation, DimensionType, BlockPos> lastPos = HashBasedTable.create();

	public Optional<DimensionType> getLastDim(ResourceLocation key)
	{
		return Optional.ofNullable(lastDim.get(key));
	}

	public void setLastDim(ResourceLocation key, @Nullable DimensionType type)
	{
		if (type == null)
		{
			lastDim.remove(key);
		}
		else
		{
			lastDim.put(key, type);
		}
	}

	public Optional<BlockPos> getLastPos(ResourceLocation key, DimensionType type)
	{
		return Optional.ofNullable(lastPos.get(key, type));
	}

	public void setLastPos(ResourceLocation key, DimensionType type, @Nullable BlockPos pos)
	{
		if (pos == null)
		{
			lastPos.remove(key, type);
		}
		else
		{
			lastPos.put(key, type, pos);
		}
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();
		ListNBT list = new ListNBT();

		for (Entry<ResourceLocation, DimensionType> entry : lastDim.entrySet())
		{
			ResourceLocation key = entry.getKey();
			DimensionType type = entry.getValue();

			if (key != null && type != null)
			{
				CompoundNBT tag = new CompoundNBT();

				tag.putString("Key", key.toString());
				tag.putString("Dim", type.getRegistryName().toString());

				list.add(tag);
			}
		}

		nbt.put("LastDim", list);

		list = new ListNBT();

		for (Cell<ResourceLocation, DimensionType, BlockPos> entry : lastPos.cellSet())
		{
			ResourceLocation key = entry.getRowKey();
			DimensionType type = entry.getColumnKey();
			BlockPos pos = entry.getValue();

			if (key != null && type != null && pos != null)
			{
				CompoundNBT tag = NBTUtil.writeBlockPos(pos);

				tag.putString("Key", key.toString());
				tag.putString("Dim", type.getRegistryName().toString());

				list.add(tag);
			}
		}

		nbt.put("LastPos", list);

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		ListNBT list = nbt.getList("LastDim", NBT.TAG_COMPOUND);

		for (INBT entry : list)
		{
			CompoundNBT tag = (CompoundNBT)entry;
			DimensionType type = DimensionType.byName(new ResourceLocation(tag.getString("Dim")));

			if (type != null && tag.contains("Key", NBT.TAG_STRING))
			{
				lastDim.put(new ResourceLocation(tag.getString("Key")), type);
			}
		}

		list = nbt.getList("LastPos", NBT.TAG_COMPOUND);

		for (INBT entry : list)
		{
			CompoundNBT tag = (CompoundNBT)entry;
			DimensionType type = DimensionType.byName(new ResourceLocation(tag.getString("Dim")));

			if (type != null && tag.contains("Key", NBT.TAG_STRING))
			{
				lastPos.put(new ResourceLocation(tag.getString("Key")), type, NBTUtil.readBlockPos(tag));
			}
		}
	}
}