package cavern.miner.world.gen.carver;

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
	public ExtremeCanyonWorldCarver(Function<Dynamic<?>, ? extends ProbabilityConfig> factory)
	{
		super(factory);
	}

	@Override
	public boolean carveRegion(IChunk chunk, Function<BlockPos, Biome> biomePos, Random rand, int seaLevel, int chunkXOffset, int chunkZOffset, int chunkX, int chunkZ, BitSet carvingMask, ProbabilityConfig config)
	{
		int i = (func_222704_c() * 2 - 1) * 16;
		double x = chunkXOffset * 16 + rand.nextInt(16);
		double y = rand.nextInt(rand.nextInt(10) + 8) + 70;
		double z = chunkZOffset * 16 + rand.nextInt(16);
		float yaw = rand.nextFloat() * ((float)Math.PI * 7.0F);
		float pitch = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
		float width = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 8.0F;
		int branchCount = i - rand.nextInt(i / 4);

		carveCanyon(chunk, biomePos, rand.nextLong(), seaLevel, chunkX, chunkZ, x, y, z, width, yaw, pitch, 0, branchCount, 9.0D, carvingMask);

		return true;
	}
}