package cavern.miner.world;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import cavern.miner.block.CaveBlocks;
import cavern.miner.config.CavernConfig;
import cavern.miner.config.manager.CaveBiome;
import cavern.miner.world.gen.MapGenCavernCaves;
import cavern.miner.world.gen.MapGenCavernRavine;
import cavern.miner.world.gen.MapGenExtremeCaves;
import cavern.miner.world.gen.MapGenExtremeRavine;
import cavern.miner.world.gen.VeinGenerator;
import cavern.miner.world.gen.WorldGenCaveBush;
import cavern.miner.world.gen.WorldGenCaveDungeons;
import cavern.miner.world.gen.WorldGenTowerDungeons;
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
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ChunkGeneratorCavern implements IChunkGenerator
{
	protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
	protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
	protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();

	private final World world;
	private final Random rand;

	private Biome[] biomesForGeneration;

	private final MapGenBase caveGenerator = new MapGenCavernCaves();
	private final MapGenBase ravineGenerator = new MapGenCavernRavine();
	private final MapGenBase extremeCaveGenerator = new MapGenExtremeCaves();
	private final MapGenBase extremeRavineGenerator = new MapGenExtremeRavine();
	private final MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();

	private final VeinGenerator veinGenerator;

	private final WorldGenerator lakeWaterGen = new WorldGenLakes(Blocks.WATER);
	private final WorldGenerator lakeLavaGen = new WorldGenLakes(Blocks.LAVA);
	private final WorldGenerator mushroomBrownGen = new WorldGenCaveBush(Blocks.BROWN_MUSHROOM);
	private final WorldGenerator mushroomRedGen = new WorldGenCaveBush(Blocks.RED_MUSHROOM);
	private final WorldGenerator cavenicShroomGen = new WorldGenCaveBush(CaveBlocks.CAVENIC_SHROOM);
	private final WorldGenerator dungeonGen = new WorldGenCaveDungeons(CavernConfig.dungeonMobs.getKeys());
	private final WorldGenerator towerDungeonGen = new WorldGenTowerDungeons(CavernConfig.towerDungeonMobs.getKeys());
	private final WorldGenerator liquidWaterGen = new WorldGenLiquids(Blocks.FLOWING_WATER);
	private final WorldGenerator liquidLavaGen = new WorldGenLiquids(Blocks.FLOWING_LAVA);

	public ChunkGeneratorCavern(World world)
	{
		this.world = world;
		this.rand = new Random(world.getSeed());
		this.veinGenerator = new VeinGenerator(WorldProviderCavern.VEINS);
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
				CaveBiome caveBiome = CavernConfig.BIOMES.getCaveBiome(biome);
				IBlockState top = caveBiome == null ? STONE : caveBiome.getTopBlock().getBlockState();
				IBlockState filter = caveBiome == null ? top : caveBiome.getTerrainBlock().getBlockState();

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

	public Biome[] replaceBiomes(Biome[] biomes)
	{
		Biome[] newBiomes = biomes.clone();

		for (int i = 0, j = biomes.length; i < j; ++i)
		{
			Biome biome = biomes[i];

			if (biome.isMutation())
			{
				Biome newBiome = Biome.getMutationForBiome(biome);

				if (newBiome != null && !biome.isMutation())
				{
					newBiomes[i] = newBiome;
				}
			}
		}

		return newBiomes;
	}

	@Override
	public Chunk generateChunk(int chunkX, int chunkZ)
	{
		rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

		biomesForGeneration = world.getBiomeProvider().getBiomes(biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
		biomesForGeneration = replaceBiomes(biomesForGeneration);

		ChunkPrimer primer = new ChunkPrimer();

		setBlocksInChunk(primer);

		if (CavernConfig.generateCaves)
		{
			caveGenerator.generate(world, chunkX, chunkZ, primer);
		}

		if (CavernConfig.generateRavine)
		{
			ravineGenerator.generate(world, chunkX, chunkZ, primer);
		}

		if (CavernConfig.generateExtremeCaves)
		{
			extremeCaveGenerator.generate(world, chunkX, chunkZ, primer);
		}

		if (CavernConfig.generateExtremeRavine)
		{
			extremeRavineGenerator.generate(world, chunkX, chunkZ, primer);
		}

		if (CavernConfig.generateMineshaft)
		{
			mineshaftGenerator.generate(world, chunkX, chunkZ, primer);
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

		if (CavernConfig.generateMineshaft)
		{
			mineshaftGenerator.generateStructure(world, rand, chunkPos);
		}

		if (BiomeDictionary.hasType(biome, Type.NETHER))
		{
			if (CavernConfig.generateLakes && rand.nextInt(4) == 0 && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.LAVA))
			{
				x = rand.nextInt(16) + 8;
				y = rand.nextInt(max - 16);
				z = rand.nextInt(16) + 8;

				lakeLavaGen.generate(world, rand, pos.add(x, y, z));
			}
		}
		else if (!BiomeDictionary.hasType(biome, Type.END))
		{
			if (CavernConfig.generateLakes)
			{
				if (!BiomeDictionary.hasType(biome, Type.SANDY) && rand.nextInt(4) == 0 && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.LAKE))
				{
					x = rand.nextInt(16) + 8;
					y = rand.nextInt(max - 16);
					z = rand.nextInt(16) + 8;

					lakeWaterGen.generate(world, rand, pos.add(x, y, z));
				}

				if (rand.nextInt(20) == 0 && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.LAVA))
				{
					x = rand.nextInt(16) + 8;
					y = rand.nextInt(max / 2);
					z = rand.nextInt(16) + 8;

					lakeLavaGen.generate(world, rand, pos.add(x, y, z));
				}
			}
		}

		if (CavernConfig.generateDungeons && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.DUNGEON))
		{
			for (int i = 0; i < 20; ++i)
			{
				x = rand.nextInt(16) + 8;
				y = rand.nextInt(max - 30) + 5;
				z = rand.nextInt(16) + 8;

				dungeonGen.generate(world, rand, pos.add(x, y, z));
			}
		}

		if (CavernConfig.generateTowerDungeons && TerrainGen.populate(this, world, rand, chunkX, chunkZ, false, EventType.DUNGEON))
		{
			if (rand.nextInt(100) == 0)
			{
				y = rand.nextInt(16) + 8;

				towerDungeonGen.generate(world, rand, pos.add(16, y, 16));
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

			if (rand.nextInt(5) <= i)
			{
				mushroomBrownGen.generate(world, rand, pos.add(16, 0, 16));
			}

			if (rand.nextInt(10) <= i)
			{
				mushroomRedGen.generate(world, rand, pos.add(16, 0, 16));
			}

			if (rand.nextInt(7) <= i)
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
					for (int i = 0; i < 40; ++i)
					{
						x = rand.nextInt(16) + 8;
						y = rand.nextInt(max - 12) + 10;
						z = rand.nextInt(16) + 8;

						liquidLavaGen.generate(world, rand, pos.add(x, y, z));
					}
				}
			}
			else if (BiomeDictionary.hasType(biome, Type.WATER))
			{
				if (TerrainGen.decorate(world, rand, chunkPos, Decorate.EventType.LAKE_WATER))
				{
					for (int i = 0; i < 65; ++i)
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
		if ("Mineshaft".equals(structureName) && mineshaftGenerator != null)
		{
			return mineshaftGenerator.isInsideStructure(pos);
		}

		return false;
	}

	@Override
	public BlockPos getNearestStructurePos(World world, String structureName, BlockPos pos, boolean findUnexplored)
	{
		if ("Mineshaft".equals(structureName) && mineshaftGenerator != null)
		{
			return mineshaftGenerator.getNearestStructurePos(world, pos, findUnexplored);
		}

		return null;
	}

	@Override
	public void recreateStructures(Chunk chunk, int x, int z)
	{
		if (CavernConfig.generateMineshaft)
		{
			mineshaftGenerator.generate(world, x, z, null);
		}
	}
}