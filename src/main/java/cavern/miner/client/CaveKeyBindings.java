package cavern.miner.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class CaveKeyBindings
{
	public static final KeyBinding KEY_MINING_RECORDS = new KeyBinding("key.cavern.miningRecords", KeyConflictContext.IN_GAME, Keyboard.KEY_F12, "key.categories.cavern");

	public static void registerKeyBindings()
	{
		ClientRegistry.registerKeyBinding(KEY_MINING_RECORDS);
	}
}