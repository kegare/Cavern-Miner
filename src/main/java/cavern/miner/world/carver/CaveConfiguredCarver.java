package cavern.miner.world.carver;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.util.LazyValue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ICarverConfig;
import net.minecraft.world.gen.carver.WorldCarver;

public class CaveConfiguredCarver<WC extends ICarverConfig> extends ConfiguredCarver<WC>
{
	public final LazyValue<WorldCarver<WC>> carver;

	public CaveConfiguredCarver(Supplier<WorldCarver<WC>> caver, WC config)
	{
		super(null, config);
		this.carver = new LazyValue<>(caver);
	}

	@Override
	public boolean shouldCarve(Random random, int chunkX, int chunkZ)
	{
		return carver.getValue().shouldCarve(random, chunkX, chunkZ, config);
	}

	@Override
	public boolean func_227207_a_(IChunk chunk, Function<BlockPos, Biome> biome, Random random, int par, int chunkX, int chunkZ, int x, int z, BitSet bitSet)
	{
		return carver.getValue().func_225555_a_(chunk, biome, random, par, chunkX, chunkZ, x, z, bitSet, config);
	}
}