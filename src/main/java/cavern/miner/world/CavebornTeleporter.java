package cavern.miner.world;

import java.util.function.Function;

import cavern.miner.network.CaveNetworkConstants;
import cavern.miner.network.LoadingScreenMessage;
import cavern.miner.util.BlockPosHelper;
import cavern.miner.world.dimension.CavernDimension;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.PacketDistributor;

public class CavebornTeleporter implements ITeleporter
{
	@Override
	public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity)
	{
		Entity teleported = repositionEntity.apply(false);

		BlockPos resultPos = BlockPosHelper.findPos(destWorld, teleported.getPosition(), 128, pos ->
		{
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			if (destWorld.isAirBlock(pos) && destWorld.isAirBlock(pos.setPos(x, y + 1, z)))
			{
				BlockState state = destWorld.getBlockState(pos.setPos(x, y - 1, z));

				return state.isNormalCube(destWorld, pos.setPos(x, y, z));
			}

			return false;
		});

		if (resultPos == null)
		{
			return teleported;
		}

		double posX = resultPos.getX() + 0.5D;
		double posY = resultPos.getY();
		double posZ = resultPos.getZ() + 0.5D;

		teleported.moveForced(posX, posY, posZ);

		if (teleported instanceof ServerPlayerEntity && destWorld.getDimension() instanceof CavernDimension && destWorld.getServer().isSinglePlayer())
		{
			CaveNetworkConstants.PLAY.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)teleported), new LoadingScreenMessage(LoadingScreenMessage.Stage.DONE));
		}

		return teleported;
	}
}