package cavern.miner.config.json;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class JsonHelper
{
	public static JsonObject serializeRegistryEntry(IForgeRegistryEntry<?> entry)
	{
		JsonObject object = new JsonObject();

		object.addProperty("name", entry.getRegistryName().toString());

		return object;
	}

	public static JsonObject serializeBlockState(BlockState state)
	{
		return BlockStateSerializer.INSTANCE.serialize(state, state.getClass(), null).getAsJsonObject();
	}

	public static JsonObject serializeTag(Tag<?> tag)
	{
		JsonObject object = new JsonObject();

		object.addProperty("tag", tag.getId().toString());

		return object;
	}

	@Nullable
	public static ResourceLocation deserializeKey(JsonElement e)
	{
		if (e.isJsonNull())
		{
			return null;
		}

		if (e.isJsonObject())
		{
			JsonObject object = e.getAsJsonObject();

			return new ResourceLocation(object.get("name").getAsString());
		}

		if (e.isJsonPrimitive())
		{
			return new ResourceLocation(e.getAsString());
		}

		return null;
	}

	public static Block deserializeBlock(JsonElement e)
	{
		ResourceLocation key = deserializeKey(e);

		return ObjectUtils.defaultIfNull(key == null ? null : ForgeRegistries.BLOCKS.getValue(key), Blocks.AIR);
	}

	public static BlockState deserializeBlockState(JsonObject object)
	{
		return BlockStateSerializer.INSTANCE.deserialize(object, object.getClass(), null);
	}

	public static Tag<Block> deserializeBlockTag(JsonElement e)
	{
		ResourceLocation key = null;

		if (e.isJsonObject())
		{
			JsonObject object = e.getAsJsonObject();

			key = new ResourceLocation(object.get("tag").getAsString());
		}

		if (key == null)
		{
			key = new ResourceLocation(e.getAsString());
		}

		return new BlockTags.Wrapper(key);
	}

	public static JsonObject serializeItemStack(ItemStack stack)
	{
		return ItemStackSerializer.INSTANCE.serialize(stack, stack.getClass(), null).getAsJsonObject();
	}

	public static Item deserializeItem(JsonElement e)
	{
		ResourceLocation key = deserializeKey(e);

		return ObjectUtils.defaultIfNull(key == null ? null : ForgeRegistries.ITEMS.getValue(key), Items.AIR);
	}

	public static ItemStack deserializeItemStack(JsonObject object)
	{
		return ItemStackSerializer.INSTANCE.deserialize(object, object.getClass(), null);
	}

	public static Tag<Item> deserializeItemTag(JsonElement e)
	{
		ResourceLocation key = null;

		if (e.isJsonObject())
		{
			JsonObject object = e.getAsJsonObject();

			key = new ResourceLocation(object.get("tag").getAsString());
		}

		if (key == null)
		{
			key = new ResourceLocation(e.getAsString());
		}

		return new ItemTags.Wrapper(key);
	}

	@Nullable
	public static EntityType<?> deserializeEntityType(JsonElement e)
	{
		ResourceLocation key = deserializeKey(e);

		return key == null ? null : ForgeRegistries.ENTITIES.getValue(key);
	}
}