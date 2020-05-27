package cavern.miner.capability;

import javax.annotation.Nullable;

import cavern.miner.data.Miner;
import cavern.miner.data.MiningCache;
import cavern.miner.data.PlayerData;
import cavern.miner.data.PortalCache;
import cavern.miner.data.WorldData;
import cavern.miner.enchantment.MiningUnit;
import cavern.miner.util.CaveUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class CaveCapabilities
{
	@CapabilityInject(WorldData.class)
	public static final Capability<WorldData> WORLD_DATA = null;
	@CapabilityInject(PortalCache.class)
	public static final Capability<PortalCache> PORTAL_CACHE = null;
	@CapabilityInject(PlayerData.class)
	public static final Capability<PlayerData> PLAYER_DATA = null;
	@CapabilityInject(Miner.class)
	public static final Capability<Miner> MINER = null;
	@CapabilityInject(MiningCache.class)
	public static final Capability<MiningCache> MINING_CACHE = null;
	@CapabilityInject(MiningUnit.class)
	public static final Capability<MiningUnit> MINING_UNIT = null;

	public static void registerCapabilities()
	{
		CapabilityWorldData.register();
		CapabilityPortalCache.register();
		CapabilityPlayerData.register();
		CapabilityMiner.register();
		CapabilityMiningCache.register();
		CapabilityMiningUnit.register();

		MinecraftForge.EVENT_BUS.register(new CaveCapabilities());
	}

	public static <T> boolean hasCapability(@Nullable ICapabilityProvider entry, @Nullable Capability<T> capability)
	{
		return entry != null && capability != null && entry.hasCapability(capability, null);
	}

	@Nullable
	public static <T> T getCapability(@Nullable ICapabilityProvider entry, @Nullable Capability<T> capability)
	{
		return hasCapability(entry, capability) ? entry.getCapability(capability, null) : null;
	}

	@SubscribeEvent
	public void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event)
	{
		event.addCapability(CaveUtils.getKey("world_data"), new CapabilityWorldData());
	}

	@SubscribeEvent
	public void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
	{
		event.addCapability(CaveUtils.getKey("portal_cache"), new CapabilityPortalCache());

		if (event.getObject() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getObject();

			event.addCapability(CaveUtils.getKey("player_data"), new CapabilityPlayerData());
			event.addCapability(CaveUtils.getKey("miner"), new CapabilityMiner(player));
			event.addCapability(CaveUtils.getKey("mining_cache"), new CapabilityMiningCache(player));
			event.addCapability(CaveUtils.getKey("mining_unit"), new CapabilityMiningUnit(player));
		}
	}

	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event)
	{
		EntityPlayer player = event.getEntityPlayer();
		EntityPlayer original = event.getOriginal();

		PortalCache originalPortalCache = getCapability(original, PORTAL_CACHE);
		PortalCache portalCache = getCapability(player, PORTAL_CACHE);

		if (originalPortalCache != null && portalCache != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();

			originalPortalCache.writeToNBT(nbt);
			portalCache.readFromNBT(nbt);
		}

		PlayerData originalPlayerData = getCapability(original, PLAYER_DATA);
		PlayerData playerData = getCapability(player, PLAYER_DATA);

		if (originalPlayerData != null && playerData != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();

			originalPlayerData.writeToNBT(nbt);
			playerData.readFromNBT(nbt);
		}

		Miner originalMiner = getCapability(original, MINER);
		Miner miner = getCapability(player, MINER);

		if (originalMiner != null && miner != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();

			originalMiner.writeToNBT(nbt);
			miner.readFromNBT(nbt);
		}
	}
}