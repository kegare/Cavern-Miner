package cavern.miner.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class CaveNetworkConstants
{
	public static final String NET_MARKER = "CAVERN";
	public static final int NET_VERSION = 4;
	public static final String PROTOCOL_VERSION = NET_MARKER + NET_VERSION;

	public static final SimpleChannel PLAY = createPlayChannel();

	private static SimpleChannel createPlayChannel()
	{
		SimpleChannel channel = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation("cavern", "play"))
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.simpleChannel();

		int i = 0;

		channel.messageBuilder(LoadingScreenMessage.class, i++, NetworkDirection.PLAY_TO_CLIENT)
			.decoder(LoadingScreenMessage::decode).encoder(LoadingScreenMessage::encode).consumer(LoadingScreenMessage::handle).add();
		channel.messageBuilder(MinerUpdateMessage.class, i++, NetworkDirection.PLAY_TO_CLIENT)
			.decoder(MinerUpdateMessage::decode).encoder(MinerUpdateMessage::encode).consumer(MinerUpdateMessage::handle).add();
		channel.messageBuilder(MinerPointMessage.class, i++, NetworkDirection.PLAY_TO_CLIENT)
			.decoder(MinerPointMessage::decode).encoder(MinerPointMessage::encode).consumer(MinerPointMessage::handle).add();
		channel.messageBuilder(MinerRecordMessage.class, i++, NetworkDirection.PLAY_TO_CLIENT)
			.decoder(MinerRecordMessage::decode).encoder(MinerRecordMessage::encode).consumer(MinerRecordMessage::handle).add();
		channel.messageBuilder(MiningInteractMessage.class, i++, NetworkDirection.PLAY_TO_CLIENT)
			.decoder(MiningInteractMessage::decode).encoder(MiningInteractMessage::encode).consumer(MiningInteractMessage::handle).add();

		return channel;
	}

	public static String init()
	{
		return PROTOCOL_VERSION;
	}
}