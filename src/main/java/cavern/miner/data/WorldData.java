package cavern.miner.data;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import cavern.miner.block.BlockCavernPortal;
import cavern.miner.block.CaveBlocks;
import cavern.miner.capability.CaveCapabilities;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class WorldData
{
	private final SetMultimap<BlockCavernPortal, BlockPos> cavePortals = HashMultimap.create();

	public boolean addPortal(BlockCavernPortal portal, BlockPos pos)
	{
		return !hasPortal(portal, pos.getX() << 4, pos.getZ() << 4) && cavePortals.put(portal, pos);
	}

	public boolean removePortal(BlockCavernPortal portal, BlockPos pos)
	{
		return cavePortals.remove(portal, pos);
	}

	public ImmutableSet<BlockPos> getPortalPositions()
	{
		return ImmutableSet.copyOf(cavePortals.values());
	}

	public ImmutableSet<BlockPos> getPortalPositions(BlockCavernPortal portal)
	{
		return ImmutableSet.copyOf(cavePortals.get(portal));
	}

	public boolean hasPortal(int chunkX, int chunkZ)
	{
		return cavePortals.values().stream().anyMatch(pos -> pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ);
	}

	public boolean hasPortal(BlockCavernPortal portal, int chunkX, int chunkZ)
	{
		return cavePortals.get(portal).stream().anyMatch(pos -> pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ);
	}

	public boolean isPortalEmpty()
	{
		return cavePortals.isEmpty();
	}

	public boolean isPortalEmpty(BlockCavernPortal portal)
	{
		return cavePortals.get(portal).isEmpty();
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagCompound portals = new NBTTagCompound();

		for (BlockCavernPortal key : CaveBlocks.CAVE_PORTALS)
		{
			NBTTagList tagList = new NBTTagList();

			for (BlockPos pos : cavePortals.get(key))
			{
				tagList.appendTag(NBTUtil.createPosTag(pos));
			}

			portals.setTag(key.getRegistryName().toString(), tagList);
		}

		nbt.setTag("Portals", portals);
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTBase portals = nbt.getTag("Portals");

		if (portals != null && portals.getId() == NBT.TAG_COMPOUND)
		{
			for (BlockCavernPortal key : CaveBlocks.CAVE_PORTALS)
			{
				NBTTagList tagList = ((NBTTagCompound)portals).getTagList(key.getRegistryName().toString(), NBT.TAG_COMPOUND);

				for (int i = 0; i < tagList.tagCount(); ++i)
				{
					cavePortals.put(key, NBTUtil.getPosFromTag(tagList.getCompoundTagAt(i)));
				}
			}
		}
	}

	public static WorldData get(World world)
	{
		return ObjectUtils.defaultIfNull(CaveCapabilities.getCapability(world, CaveCapabilities.WORLD_DATA), new WorldData());
	}
}