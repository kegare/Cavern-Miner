package cavern.miner.world;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import cavern.miner.config.CavelandConfig;
import cavern.miner.world.gen.MapGenCavelandCaves;
import cavern.miner.world.gen.MapGenCavelandRavine;
import cavern.miner.world.gen.VeinGenerator;
import cavern.miner.world.gen.WorldGenAcresia;
import cavern.miner.world.gen.WorldGenCaveBush;
import cavern.miner.world.gen.WorldGenPervertedBirchTree;
import cavern.miner.world.gen.WorldGenPervertedSpruceTree;
import cavern.miner.world.gen.WorldGenPervertedTrees;
import cavern.miner.world.gen.WorldGenRuinedHouse;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
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

public class ChunkGeneratorCaveland implements IChunkGenerator
{
	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	protected static final IBlockState DIRT = Blocks.DIRT.getDefaultState();
	protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
	protected static final IBlockState SANDSTONE = Blocks.SANDSTONE.getDefaultState();

	private final World world;
	private final Random rand;

	private Biome[] biomesForGeneration;

	private final MapGenBase caveGenerator = new MapGenCavelandCaves();
	private final MapGenBase ravineGenerator = new MapGenCavelandRavine();

	private final VeinGenerator veinGenerator;

	private final WorldGenerator lakeWaterGen = new WorldGenLakes(Blocks.WATER);
	private final WorldGenerator lakeLavaGen = new WorldGenLakes(Blocks.LAVA);
	private final WorldGenerator mushroomBrownGen = new WorldGenCaveBush(Blocks.BROWN_MUSHROOM);
	private final WorldGenerator mushroomRedGen = new WorldGenCaveBush(Blocks.RED_MUSHROOM);
	private final WorldGenerator liquidWaterGen = new WorldGenLiquids(Blocks.FLOWING_WATER);
	private final WorldGenerator liquidLavaGen = new WorldGenLiquids(Blocks.FLOWING_LAVA);
	private final WorldGenerator deadBushGen = new WorldGenDeadBush();
	private final WorldGenerator acresiaGen = new WorldGenAcresia();
	private final WorldGenerator ruinedHouseGen = new WorldGenRuinedHouse();

	public ChunkGeneratorCaveland(World world)
	{
		this.world = world;
		this.rand = new Random(world.getSeed());
		this.veinGenerator = new VeinGenerator(WorldProviderCaveland.VEINS);
	}

	public void setBlocksInChunk(ChunkPrimer primer)
	{
		for (int x = 0; x < 16; ++x)
		{
			for (int z = 0; z < 16; ++z)
			{
				for (int y = 255; y >= 0; --y)
				{
					primer.setBlockState(x, y, z, DIRT);
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
				IBlockState top = biome.topBlock;
				IBlockState filter = biome.fillerBlock;

				if (biome.isSnowyBiome())
				{
					top = Blocks.PACKED_ICE.getDefaultState();
					filter = Blocks.PACKED_ICE.getDefaultState();
				}

				if (filter.getBlock() == Blocks.SAND)
				{
					filter = SANDSTONE;
				}

				primer.setBlockState(x, 0, z, BEDROCK);
				primer.setBlockState(x, max, z, BEDROCK);
				primer.setBlockState(x, 1, z, primer.getBlockState(x, 2, z));

				for (int y = 1; y <= max - 1; ++y)
				{
					if (primer.getBlockState(x, y, z).getBlock() == Blocks.GRASS ||
						primer.getBlockState(x, y, z).getMaterial().isSolid() && primer.getBlockState(x, y + 1, z).getBlock() == Blocks.AIR)
					{
						primer.setBlockState(x, y, z, top);
					}
					else if (primer.getBlockState(x, y, z).getBlock() == Blocks.DIRT)
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

		caveGenerator.generate(world, chunkX, chunkZ, primer);

		if (CavelandConfig.generateRiver)
		{
			ravineGenerator.generate(world, chunkX, chunkZ, primer);
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
		BiomeDecorator decorator = biome.decorator;
		int ground = world.provider.getAverageGroundLevel();
		int max = world.provider.getActualHeight() - 1;

		ForgeEventFactory.onChunkPopulate(true, this, world, rand, chunkX, chunkZ, false);

		int x, y, z;

		if (CavelandConfig.generateLakes)
		{
			if (TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.LAKE))
			{
				x = rand.nextInt(16) + 8;
				y = rand.nextInt(max - 16);
				z = rand.nextInt(16) + 8;

				lakeWaterGen.generate(world, rand, pos.add(x, y, z));
			}

			if (rand.nextInt(30) == 0 && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.LAVA))
			{
				x = rand.nextInt(16) + 8;
				y = rand.nextInt(max / 2);
				z = rand.nextInt(16) + 8;

				lakeLavaGen.generate(world, rand, pos.add(x, y, z));
			}
		}

		MinecraftForge.EVENT_BUS.post(new DecorateBiomeEvent.Pre(world, rand, chunkPos));

		if (rand.nextInt(30) == 0 && BiomeDictionary.hasType(biome, Type.FOREST))
		{
			x = rand.nextInt(16) + 8;
			y = rand.nextInt(max / 2);
			z = rand.nextInt(16) + 8;

			ruinedHouseGen.generate(world, rand, pos.add(x, y, z));
		}

		MinecraftForge.ORE_GEN_BUS.post(new OreGenEvent.Post(world, rand, pos));

		for (int i = 0; i < 10; ++i)
		{
			x = rand.nextInt(16) + 8;
			y = rand.nextInt(max - 10);
			z = rand.nextInt(16) + 8;

			acresiaGen.generate(world, rand, pos.add(x, y, z));
		}

		for (int i = 0; i < 15; ++i)
		{
			x = rand.nextInt(16) + 8;
			y = rand.nextInt(max / 2 - 10) + max / 2;
			z = rand.nextInt(16) + 8;

			acresiaGen.generate(world, rand, pos.add(x, y, z));
		}

		if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.SHROOM))
		{
			mushroomBrownGen.generate(world, rand, pos.add(16, 0, 16));

			mushroomRedGen.generate(world, rand, pos.add(16, 0, 16));
		}

		if (BiomeDictionary.hasType(biome, Type.SANDY))
		{
			if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.CACTUS))
			{
				for (int i = 0; i < 80; ++i)
				{
					x = rand.nextInt(16) + 8;
					y = rand.nextInt(max - 5);
					z = rand.nextInt(16) + 8;

					decorator.cactusGen.generate(world, rand, pos.add(x, y, z));
				}
			}

			if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.DEAD_BUSH))
			{
				for (int i = 0; i < 10; ++i)
				{
					x = rand.nextInt(16) + 8;
					y = rand.nextInt(max - 5);
					z = rand.nextInt(16) + 8;

					deadBushGen.generate(world, rand, pos.add(x, y, z));
				}
			}
		}
		else
		{
			if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.FLOWERS))
			{
				for (int i = 0; i < 8; ++i)
				{
					x = rand.nextInt(16) + 8;
					y = rand.nextInt(max - 5);
					z = rand.nextInt(16) + 8;

					decorator.flowerGen.generate(world, rand, pos.add(x, y, z));
				}
			}

			for (int i = 0; i < 18; ++i)
			{
				x = rand.nextInt(16) + 8;
				y = rand.nextInt(max - 5);
				z = rand.nextInt(16) + 8;

				biome.getRandomWorldGenForGrass(rand).generate(world, rand, pos.add(x, y, z));
			}

			if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.TREE))
			{
				WorldGenAbstractTree treeGen = null;

				if (BiomeDictionary.hasType(biome, Type.JUNGLE))
				{
					treeGen = new WorldGenPervertedTrees(false, 4 + rand.nextInt(7), BlockPlanks.EnumType.JUNGLE, true);
				}
				else if (BiomeDictionary.hasType(biome, Type.FOREST) || !BiomeDictionary.hasType(biome, Type.PLAINS) || rand.nextInt(10) == 0)
				{
					if (BiomeDictionary.hasType(biome, Type.COLD))
					{
						treeGen = new WorldGenPervertedSpruceTree(false);
					}
					else if (rand.nextInt(3) == 0)
					{
						treeGen = new WorldGenPervertedBirchTree(false, false);
					}
					else
					{
						treeGen = new WorldGenPervertedTrees(false, 3, BlockPlanks.EnumType.OAK, true);
					}
				}

				if (treeGen != null)
				{
					for (int i = 0; i < 80; ++i)
					{
						x = rand.nextInt(16) + 8;
						y = rand.nextInt(max - ground - 10) + ground;
						z = rand.nextInt(16) + 8;

						BlockPos blockpos = pos.add(x, y, z);

						if (treeGen.generate(world, rand, blockpos))
						{
							treeGen.generateSaplings(world, rand, blockpos);
						}
					}

					for (int i = 0; i < 50; ++i)
					{
						x = rand.nextInt(16) + 8;
						y = ground - 2 + rand.nextInt(6);
						z = rand.nextInt(16) + 8;

						BlockPos blockpos = pos.add(x, y, z);

						if (treeGen.generate(world, rand, blockpos))
						{
							treeGen.generateSaplings(world, rand, blockpos);
						}
					}
				}
			}

			if (decorator.generateFalls)
			{
				if (BiomeDictionary.hasType(biome, Type.WATER))
				{
					if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.LAKE_WATER))
					{
						for (int i = 0; i < 150; ++i)
						{
							x = rand.nextInt(16) + 8;
							y = rand.nextInt(rand.nextInt(max - 16) + 10);
							z = rand.nextInt(16) + 8;

							liquidWaterGen.generate(world, rand, pos.add(x, y, z));
						}
					}
				}
				else
				{
					if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.LAKE_WATER))
					{
						for (int i = 0; i < 100; ++i)
						{
							x = rand.nextInt(16) + 8;
							y = rand.nextInt(rand.nextInt(max - 16) + 10);
							z = rand.nextInt(16) + 8;

							liquidWaterGen.generate(world, rand, pos.add(x, y, z));
						}
					}

					if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.LAKE_LAVA))
					{
						for (int i = 0; i < 20; ++i)
						{
							x = rand.nextInt(16) + 8;
							y = rand.nextInt(max / 2);
							z = rand.nextInt(16) + 8;

							liquidLavaGen.generate(world, rand, pos.add(x, y, z));
						}
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