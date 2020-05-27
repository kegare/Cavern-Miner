package cavern.miner.network;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import cavern.miner.core.CavernMod;
import cavern.miner.network.client.CustomSeedMessage;
import cavern.miner.network.client.MinerMessage;
import cavern.miner.network.client.MiningMessage;
import cavern.miner.network.client.MiningRecordsMessage;
import cavern.miner.network.client.RegenerationGuiMessage;
import cavern.miner.network.server.RegenerationMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class CaveNetworkRegistry
{
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(CavernMod.MODID);

	private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, int id, Side side)
	{
		NETWORK.registerMessage(messageHandler, requestMessageType, id, side);
	}

	public static void sendTo(Supplier<IMessage> message, @Nullable EntityPlayer player)
	{
		if (player != null && player instanceof EntityPlayerMP && !(player instanceof FakePlayer))
		{
			NETWORK.sendTo(message.get(), (EntityPlayerMP)player);
		}
	}

	public static void registerMessages()
	{
		int id = 0;

		registerMessage(CustomSeedMessage.class, CustomSeedMessage.class, id++, Side.CLIENT);
		registerMessage(MinerMessage.class, MinerMessage.class, id++, Side.CLIENT);
		registerMessage(MiningRecordsMessage.class, MiningRecordsMessage.class, id++, Side.CLIENT);
		registerMessage(MiningMessage.class, MiningMessage.class, id++, Side.CLIENT);
		registerMessage(RegenerationGuiMessage.class, RegenerationGuiMessage.class, id++, Side.CLIENT);

		registerMessage(RegenerationMessage.class, RegenerationMessage.class, id++, Side.SERVER);
	}
}