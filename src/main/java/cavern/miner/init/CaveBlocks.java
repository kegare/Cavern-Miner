package cavern.miner.init;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import cavern.miner.block.CaveOreBlock;
import cavern.miner.block.CavernPortalBlock;
import cavern.miner.block.CrackedStoneBlock;
import cavern.miner.block.HugeCavernPortalBlock;
import cavern.miner.block.RandomiteOreBlock;
import net.minecraft.block.Block;
import net.minecraft.block.OreBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class CaveBlocks
{
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, "cavern");

	public static final RegistryObject<CavernPortalBlock> CAVERN_PORTAL = REGISTRY.register("cavern_portal", () -> new CavernPortalBlock(createPortalProperties()));
	public static final RegistryObject<CavernPortalBlock> HUGE_CAVERN_PORTAL = REGISTRY.register("huge_cavern_portal", () -> new HugeCavernPortalBlock(createPortalProperties()));

	public static final Supplier<List<CavernPortalBlock>> CAVE_PORTALS = () -> ImmutableList.of(CAVERN_PORTAL.get(), HUGE_CAVERN_PORTAL.get());

	public static final RegistryObject<Block> MAGNITE_ORE = REGISTRY.register("magnite_ore", () -> new OreBlock(createOreProperties(1)));
	public static final RegistryObject<Block> MAGNITE_BLOCK = REGISTRY.register("magnite_block",
		() -> new Block(Block.Properties.create(Material.IRON, MaterialColor.ORANGE_TERRACOTTA).hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> AQUAMARINE_ORE = REGISTRY.register("aquamarine_ore", () -> new CaveOreBlock(createOreProperties(1), rand -> MathHelper.nextInt(rand, 1, 3)));
	public static final RegistryObject<Block> AQUAMARINE_BLOCK = REGISTRY.register("aquamarine_block",
		() -> new Block(Block.Properties.create(Material.IRON, MaterialColor.LIGHT_BLUE).hardnessAndResistance(5.0F, 6.0F).sound(SoundType.METAL)));
	public static final RegistryObject<Block> RANDOMITE_ORE = REGISTRY.register("randomite_ore", () -> new RandomiteOreBlock(createOreProperties(2)));
	public static final RegistryObject<Block> CRACKED_STONE = REGISTRY.register("cracked_stone", () -> new CrackedStoneBlock(createOreProperties(0)));

	private static Block.Properties createPortalProperties()
	{
		return Block.Properties.create(Material.PORTAL).doesNotBlockMovement().hardnessAndResistance(-1.0F).sound(SoundType.GLASS).lightValue(5).noDrops();
	}

	private static Block.Properties createOreProperties(int level)
	{
		return Block.Properties.create(Material.ROCK).hardnessAndResistance(3.0F, 3.0F).harvestLevel(level).harvestTool(ToolType.PICKAXE);
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderType()
	{
		CAVE_PORTALS.get().forEach(o -> RenderTypeLookup.setRenderLayer(o, RenderType.getTranslucent()));

		RenderTypeLookup.setRenderLayer(MAGNITE_ORE.get(), RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(AQUAMARINE_ORE.get(), RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(RANDOMITE_ORE.get(), RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(CRACKED_STONE.get(), RenderType.getCutoutMipped());
	}
}