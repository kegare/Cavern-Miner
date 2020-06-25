package cavern.miner.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveSounds
{
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "cavern");

	public static final RegistryObject<SoundEvent> MUSIC_CAVERN = REGISTRY.register("music_cavern", () -> new SoundEvent(new ResourceLocation("cavern", "music_cavern")));

	public static final RegistryObject<SoundEvent> MINER_RANKUP = REGISTRY.register("miner_rankup", () -> new SoundEvent(new ResourceLocation("cavern", "miner_rankup")));
}