package cavern.miner.config.json;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class JsonHelper
{
	public static JsonObject serializeBlock(Block block)
	{
		JsonObject object = new JsonObject();

		object.addProperty("name", block.getRegistryName().toString());

		return object;
	}

	public static JsonObject serializeBlockState(BlockState state)
	{
		return BlockStateSerializer.INSTANCE.serialize(state, BlockState.class, null).getAsJsonObject();
	}

	public static JsonObject serializeTag(Tag<?> tag)
	{
		JsonObject object = new JsonObject();

		object.addProperty("tag", tag.getId().toString());

		return object;
	}

	public static Block deserializeBlock(JsonObject object)
	{
		return ObjectUtils.defaultIfNull(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(object.get("name").getAsString())), Blocks.AIR);
	}

	public static BlockState deserializeBlockState(JsonObject object)
	{
		return BlockStateSerializer.INSTANCE.deserialize(object, BlockState.class, null);
	}

	@Nullable
	public static Tag<Block> deserializeBlockTag(JsonObject object)
	{
		return BlockTags.getCollection().get(new ResourceLocation(object.get("tag").getAsString()));
	}

	public static JsonObject serializeItem(IItemProvider item)
	{
		JsonObject object = new JsonObject();

		object.addProperty("name", item.asItem().getRegistryName().toString());

		return object;
	}

	public static JsonObject serializeItemStack(ItemStack stack)
	{
		return ItemStackSerializer.INSTANCE.serialize(stack, ItemStack.class, null).getAsJsonObject();
	}

	public static Item deserializeItem(JsonObject object)
	{
		return ObjectUtils.defaultIfNull(ForgeRegistries.ITEMS.getValue(new ResourceLocation(object.get("name").getAsString())), Items.AIR);
	}

	public static ItemStack deserializeItemStack(JsonObject object)
	{
		return ItemStackSerializer.INSTANCE.deserialize(object, ItemStack.class, null);
	}

	@Nullable
	public static Tag<Item> deserializeItemTag(JsonObject object)
	{
		return ItemTags.getCollection().get(new ResourceLocation(object.get("tag").getAsString()));
	}
}