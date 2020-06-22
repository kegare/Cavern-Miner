package cavern.miner.world;

import java.util.function.Function;

import cavern.miner.network.CaveNetworkConstants;
import cavern.miner.network.LoadingScreenMessage;
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
		int radius = 128;
		int max = destWorld.getMaxHeight();
		BlockPos originPos = teleported.getPosition();
		BlockPos.Mutable findPos = new BlockPos.Mutable();
		BlockPos resultPos = null;

		outside: for (int i = 1; i < radius; ++i)
		{
			for (int j = -i; j <= i; ++j)
			{
				for (int k = -i; k <= i; ++k)
				{
					if (Math.abs(j) < i && Math.abs(k) < i) continue;

					int x = originPos.getX() + j;
					int z = originPos.getZ() + k;

					for (int y = max - 1; y > 1; --y)
					{
						if (destWorld.isAirBlock(findPos.setPos(x, y, z)) && destWorld.isAirBlock(findPos.setPos(x, y + 1, z)))
						{
							BlockState state = destWorld.getBlockState(findPos.setPos(x, y - 1, z));

							if (state.isNormalCube(destWorld, findPos))
							{
								resultPos = findPos.setPos(x, y, z).toImmutable();

								break outside;
							}
						}
					}
				}
			}
		}

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