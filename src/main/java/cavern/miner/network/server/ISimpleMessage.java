package cavern.miner.network.server;

import cavern.miner.network.CaveNetworkRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface ISimpleMessage<REQ extends ISimpleMessage<REQ, REPLY>, REPLY extends IMessage> extends IMessage, IMessageHandler<REQ, REPLY>
{
	@Override
	default void fromBytes(ByteBuf buf) {}

	@Override
	default void toBytes(ByteBuf buf) {}

	REPLY process();

	@Override
	default REPLY onMessage(final REQ message, final MessageContext ctx)
	{
		IThreadListener thread = FMLCommonHandler.instance().getWorldThread(ctx.netHandler);
		EntityPlayerMP player = ctx.getServerHandler().player;

		if (thread.isCallingFromMinecraftThread())
		{
			return message.process();
		}

		thread.addScheduledTask(() ->
		{
			final REPLY result = message.process();

			if (result != null)
			{
				CaveNetworkRegistry.sendTo(() -> result, player);
			}
		});

		return null;
	}
}