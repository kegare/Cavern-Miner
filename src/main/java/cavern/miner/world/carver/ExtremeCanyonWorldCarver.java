package cavern.miner.world.carver;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.ProbabilityConfig;

public class ExtremeCanyonWorldCarver extends CavernCanyonWorldCarver
{
	public ExtremeCanyonWorldCarver(Function<Dynamic<?>, ? extends ProbabilityConfig> config)
	{
		super(config);
	}

	@Override
	public boolean func_225555_a_(IChunk chunk, Function<BlockPos, Biome> biomes, Random rand, int seaLevel, int chunkX, int chunkZ, int globalX, int globalZ, BitSet carvingMask, ProbabilityConfig config)
	{
		int i = (func_222704_c() * 2 - 1) * 16;
		double blockX = chunkX * 16 + rand.nextInt(16);
		double blockY = rand.nextInt(rand.nextInt(10) + 8) + 70;
		double blockZ = chunkZ * 16 + rand.nextInt(16);
		float leftRightRadian = rand.nextFloat() * ((float)Math.PI * 7.0F);
		float upDownRadian = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
		float scale = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 8.0F;
		int targetY = i - rand.nextInt(i / 4);

		genCanyon(chunk, biomes, rand.nextLong(), seaLevel, globalX, globalZ, blockX, blockY, blockZ, scale, leftRightRadian, upDownRadian, 0, targetY, 9.0D, carvingMask);

		return true;
	}
}