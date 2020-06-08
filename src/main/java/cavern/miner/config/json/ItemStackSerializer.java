package cavern.miner.config.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import cavern.miner.CavernMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.math.MathHelper;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack>
{
	public static final ItemStackSerializer INSTANCE = new ItemStackSerializer();

	@Override
	public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = JsonHelper.serializeRegistryEntry(src.getItem());
		int count = src.getCount();

		if (count > 1)
		{
			object.addProperty("count", count);
		}

		CompoundNBT nbt = src.getTag();

		if (nbt != null)
		{
			JsonObject nbtObject = new JsonObject();

			for (String key : nbt.keySet())
			{
				if (key.equals("Damage") && nbt.getInt("Damage") == 0)
				{
					continue;
				}

				String value = nbt.get(key).toString();

				nbtObject.addProperty(key, value);
			}

			if (nbtObject.size() > 0)
			{
				object.add("nbt", nbtObject);
			}
		}

		return object;
	}

	@Override
	public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		Item item = JsonHelper.deserializeItem(object);

		if (item == Items.AIR)
		{
			return ItemStack.EMPTY;
		}

		ItemStack stack = new ItemStack(item);

		if (object.has("nbt"))
		{
			try
			{
				CompoundNBT nbt = JsonToNBT.getTagFromJson(object.get("nbt").toString());

				if (!nbt.isEmpty())
				{
					stack.setTag(nbt);
				}
			}
			catch (CommandSyntaxException e)
			{
				CavernMod.LOG.error("Invalid nbt tag: " + e.getMessage());
			}
		}

		if (object.has("count"))
		{
			stack.setCount(MathHelper.clamp(object.get("count").getAsInt(), 1, stack.getMaxStackSize()));
		}

		return stack;
	}
}