package cavern.miner.client;

import cavern.miner.core.CaveSounds;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.EnumHelperClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CaveMusics
{
	public static final MusicType CAVES = create("CAVERN_CAVES", CaveSounds.MUSIC_CAVES);
	public static final MusicType CAVELAND = create("CAVELAND", CaveSounds.MUSIC_CAVELAND);

	private static MusicType create(String name, SoundEvent sound)
	{
		return create(name, sound, 12000, 24000);
	}

	private static MusicType create(String name, SoundEvent sound, int minDelay, int maxDelay)
	{
		return EnumHelperClient.addMusicType(name, sound, minDelay, maxDelay);
	}
}