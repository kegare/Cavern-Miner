package cavern.miner.core;

import cavern.miner.util.CaveUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class CaveSounds
{
	public static final CaveSoundEvent MUSIC_CAVES = new CaveSoundEvent("music.caves");
	public static final CaveSoundEvent MUSIC_CAVELAND = new CaveSoundEvent("music.caveland");

	public static final CaveSoundEvent RANKUP = new CaveSoundEvent("miner.rankup");

	public static void registerSounds(IForgeRegistry<SoundEvent> registry)
	{
		registry.register(MUSIC_CAVES);
		registry.register(MUSIC_CAVELAND);

		registry.register(RANKUP);
	}

	private static class CaveSoundEvent extends SoundEvent
	{
		public CaveSoundEvent(ResourceLocation key)
		{
			super(key);
			this.setRegistryName(key);
		}

		public CaveSoundEvent(String key)
		{
			this(CaveUtils.getKey(key));
		}
	}
}