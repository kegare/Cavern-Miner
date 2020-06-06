package cavern.miner.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveSounds
{
	public static final DeferredRegister<SoundEvent> REGISTRY = new DeferredRegister<>(ForgeRegistries.SOUND_EVENTS, "cavern");

	public static final RegistryObject<SoundEvent> MINER_RANKUP = REGISTRY.register("miner_rankup", () -> new SoundEvent(new ResourceLocation("cavern", "miner_rankup")));
}