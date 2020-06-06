package cavern.miner.init;

import cavern.miner.capability.NBTSerializableCapability;
import cavern.miner.miner.Miner;
import cavern.miner.world.CavePortalList;
import cavern.miner.world.TeleporterCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "cavern")
public final class CaveCapabilities
{
	private static final ResourceLocation TELEPORTER_CACHE_ID = new ResourceLocation("cavern", "teleporter_cache");
	private static final ResourceLocation MINER_ID = new ResourceLocation("cavern", "miner");

	private static final ResourceLocation CAVE_PORTAL_LIST_ID = new ResourceLocation("cavern", "cave_portal_list");

	@CapabilityInject(TeleporterCache.class)
	public static final Capability<TeleporterCache> TELEPORTER_CACHE = null;
	@CapabilityInject(Miner.class)
	public static final Capability<Miner> MINER = null;

	@CapabilityInject(CavePortalList.class)
	public static final Capability<CavePortalList> CAVE_PORTAL_LIST = null;

	public static void registerCapabilities()
	{
		CapabilityManager registry = CapabilityManager.INSTANCE;

		registry.register(TeleporterCache.class, NBTSerializableCapability.createStorage(), TeleporterCache::new);
		registry.register(Miner.class, NBTSerializableCapability.createStorage(), () -> new Miner(null));

		registry.register(CavePortalList.class, NBTSerializableCapability.createStorage(), CavePortalList::new);
	}

	@SubscribeEvent
	public static void onAttachEntityCapabilities(final AttachCapabilitiesEvent<Entity> event)
	{
		event.addCapability(TELEPORTER_CACHE_ID, new NBTSerializableCapability<>(TELEPORTER_CACHE, TeleporterCache::new));

		if (event.getObject() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)event.getObject();

			event.addCapability(MINER_ID, new NBTSerializableCapability<>(MINER, () -> new Miner(player)));
		}
	}

	@SubscribeEvent
	public static void onAttachWorldCapabilities(final AttachCapabilitiesEvent<World> event)
	{
		event.addCapability(CAVE_PORTAL_LIST_ID, new NBTSerializableCapability<>(CAVE_PORTAL_LIST, CavePortalList::new));
	}

	@SubscribeEvent
	public static void onPlayerClone(final PlayerEvent.Clone event)
	{
		PlayerEntity original = event.getOriginal();
		PlayerEntity player = event.getPlayer();

		cloneCapability(TELEPORTER_CACHE, original, player);
		cloneCapability(MINER, original, player);
	}

	public static <T extends INBTSerializable<NBT>, NBT extends INBT> void cloneCapability(Capability<T> cap, ICapabilityProvider from, ICapabilityProvider to)
	{
		T fromCap = from.getCapability(cap).orElse(null);
		T toCap = to.getCapability(cap).orElse(null);

		if (fromCap != null && toCap != null)
		{
			NBT nbt = fromCap.serializeNBT();

			if (nbt != null)
			{
				toCap.deserializeNBT(nbt);
			}
		}
	}
}