package cavern.miner.world;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public class TeleporterCache implements INBTSerializable<CompoundNBT>
{
	private final Map<ResourceLocation, DimensionType> lastDim = Maps.newHashMap();
	private final Table<ResourceLocation, DimensionType, BlockPos> lastPos = HashBasedTable.create();

	private Vec3d lastPortalVec;
	private Direction teleportDirection;

	public DimensionType getLastDim(ResourceLocation key)
	{
		return getLastDim(key, DimensionType.OVERWORLD);
	}

	public DimensionType getLastDim(ResourceLocation key, DimensionType nullDefault)
	{
		return lastDim.getOrDefault(key, nullDefault);
	}

	public void setLastDim(ResourceLocation key, DimensionType type)
	{
		lastDim.put(key, type);
	}

	public BlockPos getLastPos(ResourceLocation key, DimensionType type)
	{
		return lastPos.get(key, type);
	}

	public BlockPos getLastPos(ResourceLocation key, DimensionType type, BlockPos nullDefault)
	{
		return ObjectUtils.defaultIfNull(getLastPos(key, type), ObjectUtils.defaultIfNull(nullDefault, BlockPos.ZERO));
	}

	public boolean hasLastPos(ResourceLocation key, DimensionType type)
	{
		return lastPos.contains(key, type);
	}

	public void setLastPos(ResourceLocation key, DimensionType type, BlockPos pos)
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

	public void clearLastPos(ResourceLocation key, DimensionType type)
	{
		for (Cell<ResourceLocation, DimensionType, BlockPos> entry : lastPos.cellSet())
		{
			if ((key == null || entry.getRowKey().equals(key)) && entry.getColumnKey() == type)
			{
				lastPos.remove(entry.getRowKey(), entry.getColumnKey());
			}
		}
	}

	public Vec3d getLastPortalVec()
	{
		return lastPortalVec;
	}

	public void setLastPortalVec(Vec3d vec)
	{
		lastPortalVec = vec;
	}

	public Direction getTeleportDirection()
	{
		return teleportDirection;
	}

	public void setTeleportDirection(Direction direction)
	{
		teleportDirection = direction;
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