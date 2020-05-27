package cavern.miner.item;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.common.collect.Lists;

import cavern.miner.util.CaveUtils;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

public final class CaveItems
{
	public static final ToolMaterial AQUAMARINE = EnumHelper.addToolMaterial("AQUAMARINE", 2, 200, 8.0F, 1.5F, 15);
	public static final ToolMaterial MAGNITE = EnumHelper.addToolMaterial("MAGNITE", 3, 10, 100.0F, 11.0F, 50);
	public static final ToolMaterial HEXCITE = EnumHelper.addToolMaterial("HEXCITE", 3, 1041, 10.0F, 5.0F, 15);
	public static final ToolMaterial CAVENIC = EnumHelper.addToolMaterial("CAVENIC", 2, 278, 7.0F, 2.5F, 30);

	public static final ArmorMaterial HEXCITE_ARMOR = EnumHelper.addArmorMaterial("HEXCITE", "hexcite", 22,
		new int[] {4, 7, 9, 4}, 15, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 1.0F);

	public static final Item CAVE_ITEM = new ItemCave();
	public static final ItemAquamarinePickaxe AQUAMARINE_PICKAXE = new ItemAquamarinePickaxe();
	public static final ItemAquamarineAxe AQUAMARINE_AXE = new ItemAquamarineAxe();
	public static final ItemAquamarineShovel AQUAMARINE_SHOVEL = new ItemAquamarineShovel();
	public static final ItemCaveSword MAGNITE_SWORD = new ItemCaveSword(MAGNITE, "swordMagnite");
	public static final ItemCavePickaxe MAGNITE_PICKAXE = new ItemCavePickaxe(MAGNITE, "pickaxeMagnite");
	public static final ItemCaveAxe MAGNITE_AXE = new ItemCaveAxe(MAGNITE, 18.0F, -3.0F, "axeMagnite");
	public static final ItemCaveShovel MAGNITE_SHOVEL = new ItemCaveShovel(MAGNITE, "shovelMagnite");
	public static final ItemCaveSword HEXCITE_SWORD = new ItemCaveSword(HEXCITE, "swordHexcite");
	public static final ItemCavePickaxe HEXCITE_PICKAXE = new ItemCavePickaxe(HEXCITE, "pickaxeHexcite");
	public static final ItemCaveAxe HEXCITE_AXE = new ItemCaveAxe(HEXCITE, 10.0F, -2.8F, "axeHexcite");
	public static final ItemCaveShovel HEXCITE_SHOVEL = new ItemCaveShovel(HEXCITE, "shovelHexcite");
	public static final ItemCaveHoe HEXCITE_HOE = new ItemCaveHoe(HEXCITE, "hoeHexcite");
	public static final ItemCaveArmor HEXCITE_HELMET = new ItemCaveArmor(HEXCITE_ARMOR, "helmetHexcite", "hexcite", EntityEquipmentSlot.HEAD);
	public static final ItemCaveArmor HEXCITE_CHESTPLATE = new ItemCaveArmor(HEXCITE_ARMOR, "chestplateHexcite", "hexcite", EntityEquipmentSlot.CHEST);
	public static final ItemCaveArmor HEXCITE_LEGGINGS = new ItemCaveArmor(HEXCITE_ARMOR, "leggingsHexcite", "hexcite", EntityEquipmentSlot.LEGS);
	public static final ItemCaveArmor HEXCITE_BOOTS = new ItemCaveArmor(HEXCITE_ARMOR, "bootsHexcite", "hexcite", EntityEquipmentSlot.FEET);
	public static final ItemCavenicSword CAVENIC_SWORD = new ItemCavenicSword();
	public static final ItemCavenicAxe CAVENIC_AXE = new ItemCavenicAxe();
	public static final ItemCavenicBow CAVENIC_BOW = new ItemCavenicBow();

	public static void registerItems(IForgeRegistry<Item> registry)
	{
		registry.register(CAVE_ITEM.setRegistryName("cave_item"));
		registry.register(AQUAMARINE_PICKAXE.setRegistryName("aquamarine_pickaxe"));
		registry.register(AQUAMARINE_AXE.setRegistryName("aquamarine_axe"));
		registry.register(AQUAMARINE_SHOVEL.setRegistryName("aquamarine_shovel"));
		registry.register(MAGNITE_SWORD.setRegistryName("magnite_sword"));
		registry.register(MAGNITE_PICKAXE.setRegistryName("magnite_pickaxe"));
		registry.register(MAGNITE_AXE.setRegistryName("magnite_axe"));
		registry.register(MAGNITE_SHOVEL.setRegistryName("magnite_shovel"));
		registry.register(HEXCITE_SWORD.setRegistryName("hexcite_sword"));
		registry.register(HEXCITE_PICKAXE.setRegistryName("hexcite_pickaxe"));
		registry.register(HEXCITE_AXE.setRegistryName("hexcite_axe"));
		registry.register(HEXCITE_SHOVEL.setRegistryName("hexcite_shovel"));
		registry.register(HEXCITE_HOE.setRegistryName("hexcite_hoe"));
		registry.register(HEXCITE_HELMET.setRegistryName("hexcite_helmet"));
		registry.register(HEXCITE_CHESTPLATE.setRegistryName("hexcite_chestplate"));
		registry.register(HEXCITE_LEGGINGS.setRegistryName("hexcite_leggings"));
		registry.register(HEXCITE_BOOTS.setRegistryName("hexcite_boots"));
		registry.register(CAVENIC_SWORD.setRegistryName("cavenic_sword"));
		registry.register(CAVENIC_AXE.setRegistryName("cavenic_axe"));
		registry.register(CAVENIC_BOW.setRegistryName("cavenic_bow"));
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels()
	{
		registerModels(CAVE_ITEM, "aquamarine", "magnite_ingot", "hexcite", "cavenic_orb");
		registerModel(AQUAMARINE_PICKAXE);
		registerModel(AQUAMARINE_AXE);
		registerModel(AQUAMARINE_SHOVEL);
		registerModel(MAGNITE_SWORD);
		registerModel(MAGNITE_PICKAXE);
		registerModel(MAGNITE_AXE);
		registerModel(MAGNITE_SHOVEL);
		registerModel(HEXCITE_SWORD);
		registerModel(HEXCITE_PICKAXE);
		registerModel(HEXCITE_AXE);
		registerModel(HEXCITE_SHOVEL);
		registerModel(HEXCITE_HOE);
		registerModel(HEXCITE_HELMET);
		registerModel(HEXCITE_CHESTPLATE);
		registerModel(HEXCITE_LEGGINGS);
		registerModel(HEXCITE_BOOTS);
		registerModel(CAVENIC_SWORD);
		registerModel(CAVENIC_AXE);
		registerModel(CAVENIC_BOW);
	}

	@SideOnly(Side.CLIENT)
	public static void registerModel(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}

	@SideOnly(Side.CLIENT)
	public static void registerModel(Item item, String modelName)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(CaveUtils.getKey(modelName), "inventory"));
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels(Item item, String... modelNames)
	{
		registerModels(null, item, modelNames);
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels(@Nullable String prefix, Item item, String... modelNames)
	{
		List<ModelResourceLocation> models = Lists.newArrayList();

		for (String model : modelNames)
		{
			models.add(new ModelResourceLocation(CaveUtils.getKey(ObjectUtils.defaultIfNull(prefix, "") + model), "inventory"));
		}

		ModelBakery.registerItemVariants(item, models.toArray(new ResourceLocation[models.size()]));

		for (int i = 0, size = models.size(); i < size; ++i)
		{
			ModelLoader.setCustomModelResourceLocation(item, i, models.get(i));
		}
	}

	@SideOnly(Side.CLIENT)
	public static void registerVanillaModel(Item item, String modelName)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation("minecraft:" + modelName, "inventory"));
	}

	@SideOnly(Side.CLIENT)
	public static void registerVanillaModels(Item item, String... modelNames)
	{
		List<ModelResourceLocation> models = Lists.newArrayList();

		for (String model : modelNames)
		{
			models.add(new ModelResourceLocation("minecraft:" + model, "inventory"));
		}

		ModelBakery.registerItemVariants(item, models.toArray(new ResourceLocation[models.size()]));

		for (int i = 0, size = models.size(); i < size; ++i)
		{
			ModelLoader.setCustomModelResourceLocation(item, i, models.get(i));
		}
	}

	public static void registerOreDicts()
	{
		OreDictionary.registerOre("gemAquamarine", ItemCave.EnumType.AQUAMARINE.getItemStack());
		OreDictionary.registerOre("ingotMagnite", ItemCave.EnumType.MAGNITE_INGOT.getItemStack());
		OreDictionary.registerOre("gemHexcite", ItemCave.EnumType.HEXCITE.getItemStack());
		OreDictionary.registerOre("orbCavenic", ItemCave.EnumType.CAVENIC_ORB.getItemStack());
	}

	public static void registerEquipments()
	{
		AQUAMARINE.setRepairItem(ItemCave.EnumType.AQUAMARINE.getItemStack());
		MAGNITE.setRepairItem(ItemCave.EnumType.MAGNITE_INGOT.getItemStack());
		HEXCITE.setRepairItem(ItemCave.EnumType.HEXCITE.getItemStack());
	}
}