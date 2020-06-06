package cavern.miner.world;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import cavern.miner.block.CavernPortalBlock;
import cavern.miner.init.CaveBlocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public class CavePortalList implements INBTSerializable<CompoundNBT>
{
	private final SetMultimap<CavernPortalBlock, BlockPos> cavePortals = HashMultimap.create();

	public boolean addPortal(CavernPortalBlock portal, BlockPos pos)
	{
		return !hasPortal(portal, pos.getX() << 4, pos.getZ() << 4) && cavePortals.put(portal, pos);
	}

	public boolean removePortal(CavernPortalBlock portal, BlockPos pos)
	{
		return cavePortals.remove(portal, pos);
	}

	public ImmutableSet<BlockPos> getPortalPositions()
	{
		return ImmutableSet.copyOf(cavePortals.values());
	}

	public ImmutableSet<BlockPos> getPortalPositions(CavernPortalBlock portal)
	{
		return ImmutableSet.copyOf(cavePortals.get(portal));
	}

	public boolean hasPortal(int chunkX, int chunkZ)
	{
		return cavePortals.values().stream().anyMatch(pos -> pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ);
	}

	public boolean hasPortal(CavernPortalBlock portal, int chunkX, int chunkZ)
	{
		return cavePortals.get(portal).stream().anyMatch(pos -> pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ);
	}

	public boolean isPortalEmpty()
	{
		return cavePortals.values().isEmpty();
	}

	public boolean isPortalEmpty(CavernPortalBlock portal)
	{
		return cavePortals.get(portal).isEmpty();
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		CompoundNBT nbt = new CompoundNBT();

		for (CavernPortalBlock key : CaveBlocks.CAVE_PORTALS.get())
		{
			ListNBT list = new ListNBT();

			for (BlockPos pos : cavePortals.get(key))
			{
				list.add(NBTUtil.writeBlockPos(pos));
			}

			nbt.put(key.getRegistryName().toString(), list);
		}

		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt)
	{
		for (CavernPortalBlock key : CaveBlocks.CAVE_PORTALS.get())
		{
			ListNBT list = nbt.getList(key.getRegistryName().toString(), NBT.TAG_COMPOUND);

			for (INBT entry : list)
			{
				cavePortals.put(key, NBTUtil.readBlockPos((CompoundNBT)entry));
			}
		}
	}
}