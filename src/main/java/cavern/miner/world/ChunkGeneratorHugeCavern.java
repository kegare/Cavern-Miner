package cavern.miner.world;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import cavern.miner.block.CaveBlocks;
import cavern.miner.config.HugeCavernConfig;
import cavern.miner.config.manager.CaveBiome;
import cavern.miner.world.gen.MapGenHugeCaves;
import cavern.miner.world.gen.VeinGenerator;
import cavern.miner.world.gen.WorldGenCaveBush;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ChunkGeneratorHugeCavern implements IChunkGenerator
{
	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
	protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();

	private final World world;
	private final Random rand;

	private Biome[] biomesForGeneration;

	private final MapGenHugeCaves caveGenerator = new MapGenHugeCaves();

	private final VeinGenerator veinGenerator;

	private final WorldGenerator lakeWaterGen = new WorldGenLakes(Blocks.WATER);
	private final WorldGenerator lakeLavaGen = new WorldGenLakes(Blocks.LAVA);
	private final WorldGenerator mushroomBrownGen = new WorldGenCaveBush(Blocks.BROWN_MUSHROOM);
	private final WorldGenerator mushroomRedGen = new WorldGenCaveBush(Blocks.RED_MUSHROOM);
	private final WorldGenerator cavenicShroomGen = new WorldGenBush(CaveBlocks.CAVENIC_SHROOM);
	private final WorldGenerator liquidWaterGen = new WorldGenLiquids(Blocks.FLOWING_WATER);
	private final WorldGenerator liquidLavaGen = new WorldGenLiquids(Blocks.FLOWING_LAVA);

	public ChunkGeneratorHugeCavern(World world)
	{
		this.world = world;
		this.rand = new Random(world.getSeed());
		this.veinGenerator = new VeinGenerator(WorldProviderHugeCavern.VEINS);
	}

	public void setBlocksInChunk(ChunkPrimer primer)
	{
		for (int x = 0; x < 16; ++x)
		{
			for (int z = 0; z < 16; ++z)
			{
				for (int y = 255; y >= 0; --y)
				{
					primer.setBlockState(x, y, z, STONE);
				}
			}
		}
	}

	public void replaceBiomeBlocks(int chunkX, int chunkZ, ChunkPrimer primer)
	{
		if (!ForgeEventFactory.onReplaceBiomeBlocks(this, chunkX, chunkZ, primer, world))
		{
			return;
		}

		int max = world.provider.getActualHeight() - 1;

		for (int x = 0; x < 16; ++x)
		{
			for (int z = 0; z < 16; ++z)
			{
				Biome biome = biomesForGeneration[x * 16 + z];
				CaveBiome caveBiome = HugeCavernConfig.BIOMES.getCaveBiome(biome);
				IBlockState top = caveBiome == null ? STONE : caveBiome.getTopBlock().getBlockState();
				IBlockState filter = caveBiome == null ? top : caveBiome.getFillerBlock().getBlockState();

				primer.setBlockState(x, 0, z, BEDROCK);
				primer.setBlockState(x, max, z, BEDROCK);

				for (int y = 1; y <= max - 1; ++y)
				{
					if (primer.getBlockState(x, y, z).getMaterial().isSolid() && primer.getBlockState(x, y + 1, z).getBlock() == Blocks.AIR)
					{
						primer.setBlockState(x, y, z, top);
					}
					else if (primer.getBlockState(x, y, z).getBlock() == Blocks.STONE)
					{
						primer.setBlockState(x, y, z, filter);
					}
				}

				if (max < 255)
				{
					for (int y = max + 1; y < 256; ++y)
					{
						primer.setBlockState(x, y, z, AIR);
					}
				}
			}
		}
	}

	@Override
	public Chunk generateChunk(int chunkX, int chunkZ)
	{
		rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

		biomesForGeneration = world.getBiomeProvider().getBiomes(biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);

		ChunkPrimer primer = new ChunkPrimer();

		setBlocksInChunk(primer);

		if (HugeCavernConfig.generateCaves)
		{
			caveGenerator.generate(world, chunkX, chunkZ, primer, biomesForGeneration);
		}

		replaceBiomeBlocks(chunkX, chunkZ, primer);

		veinGenerator.generate(world, rand, biomesForGeneration, chunkX, chunkZ, primer);

		Chunk chunk = new Chunk(world, primer, chunkX, chunkZ);
		byte[] biomeArray = chunk.getBiomeArray();

		for (int i = 0; i < biomeArray.length; ++i)
		{
			biomeArray[i] = (byte)Biome.getIdForBiome(biomesForGeneration[i]);
		}

		return chunk;
	}

	@Override
	public void populate(int chunkX, int chunkZ)
	{
		BlockFalling.fallInstantly = true;

		BlockPos pos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
		ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
		Biome biome = world.getBiome(pos.add(16, 0, 16));
		int max = world.provider.getActualHeight() - 1;

		ForgeEventFactory.onChunkPopulate(true, this, world, rand, chunkX, chunkZ, false);

		int x, y, z;

		if (BiomeDictionary.hasType(biome, Type.NETHER))
		{
			if (HugeCavernConfig.generateLakes && rand.nextInt(4) == 0 && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.LAVA))
			{
				x = rand.nextInt(16) + 8;
				y = rand.nextInt(max - 32) + 16;
				z = rand.nextInt(16) + 8;

				lakeLavaGen.generate(world, rand, pos.add(x, y, z));
			}
		}
		else if (!BiomeDictionary.hasType(biome, Type.END))
		{
			if (HugeCavernConfig.generateLakes)
			{
				if (!BiomeDictionary.hasType(biome, Type.SANDY) && rand.nextInt(4) == 0 && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.LAKE))
				{
					x = rand.nextInt(16) + 8;
					y = rand.nextInt(max - 16);
					z = rand.nextInt(16) + 8;

					lakeWaterGen.generate(world, rand, pos.add(x, y, z));
				}

				if (rand.nextInt(10) == 0 && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.LAVA))
				{
					x = rand.nextInt(16) + 8;
					y = rand.nextInt(max / 2 - 16) + 32;
					z = rand.nextInt(16) + 8;

					lakeLavaGen.generate(world, rand, pos.add(x, y, z));
				}
			}
		}

		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, rand, chunkPos));

		MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(world, rand, pos));

		if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.SHROOM))
		{
			int i = 0;

			if (BiomeDictionary.hasType(biome, Type.MUSHROOM))
			{
				i += 2;
			}
			else if (BiomeDictionary.hasType(biome, Type.NETHER))
			{
				i += 1;
			}

			if (rand.nextInt(7) <= i)
			{
				mushroomBrownGen.generate(world, rand, pos.add(16, 0, 16));
			}

			if (rand.nextInt(10) <= i)
			{
				mushroomRedGen.generate(world, rand, pos.add(16, 0, 16));
			}

			if (rand.nextInt(5) <= i)
			{
				cavenicShroomGen.generate(world, rand, pos.add(16, 0, 16));
			}
		}

		if (biome.decorator.generateFalls)
		{
			if (BiomeDictionary.hasType(biome, Type.NETHER))
			{
				if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.LAKE_LAVA))
				{
					for (int i = 0; i < 70; ++i)
					{
						x = rand.nextInt(16) + 8;
						y = rand.nextInt(max - 22) + 20;
						z = rand.nextInt(16) + 8;

						liquidLavaGen.generate(world, rand, pos.add(x, y, z));
					}
				}
			}
			else if (BiomeDictionary.hasType(biome, Type.WATER))
			{
				if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.LAKE_WATER))
				{
					for (int i = 0; i < 50; ++i)
					{
						x = rand.nextInt(16) + 8;
						y = rand.nextInt(rand.nextInt(max - 16) + 10);
						z = rand.nextInt(16) + 8;

						liquidWaterGen.generate(world, rand, pos.add(x, y, z));
					}
				}
			}
			else if (!BiomeDictionary.hasType(biome, Type.END))
			{
				if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.LAKE_WATER))
				{
					for (int i = 0; i < 50; ++i)
					{
						x = rand.nextInt(16) + 8;
						y = rand.nextInt(rand.nextInt(max - 16) + 10);
						z = rand.nextInt(16) + 8;

						liquidWaterGen.generate(world, rand, pos.add(x, y, z));
					}
				}

				if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.LAKE_LAVA))
				{
					for (int i = 0; i < 50; ++i)
					{
						x = rand.nextInt(16) + 8;
						y = rand.nextInt(max / 2 - 32) + 20;
						z = rand.nextInt(16) + 8;

						liquidLavaGen.generate(world, rand, pos.add(x, y, z));
					}
				}
			}
		}

		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Post(world, rand, chunkPos));

		ForgeEventFactory.onChunkPopulate(false, this, world, rand, chunkX, chunkZ, false);

		BlockFalling.fallInstantly = false;
	}

	@Override
	public boolean generateStructures(Chunk chunk, int x, int z)
	{
		return false;
	}

	@Override
	public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
	{
		return Collections.emptyList();
	}

	@Override
	public boolean isInsideStructure(World world, String structureName, BlockPos pos)
	{
		return false;
	}

	@Override
	public BlockPos getNearestStructurePos(World world, String structureName, BlockPos pos, boolean findUnexplored)
	{
		return null;
	}

	@Override
	public void recreateStructures(Chunk chunk, int x, int z) {}
}