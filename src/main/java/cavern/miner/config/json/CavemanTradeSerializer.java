package cavern.miner.config.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import cavern.miner.entity.CavemanTrade;
import cavern.miner.storage.MinerRank;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;

public enum CavemanTradeSerializer implements JsonSerializer<CavemanTrade.TradeEntry>, JsonDeserializer<CavemanTrade.TradeEntry>
{
	INSTANCE;

	@Override
	public JsonElement serialize(CavemanTrade.TradeEntry src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();

		if (src instanceof CavemanTrade.ItemStackEntry)
		{
			object.add("item", JsonHelper.serializeItemStack(((CavemanTrade.ItemStackEntry)src).getItemStack()));
		}
		else if (src instanceof CavemanTrade.EnchantedBookEntry)
		{
			EnchantmentData data = ((CavemanTrade.EnchantedBookEntry)src).getData();
			JsonObject o = JsonHelper.serializeRegistryEntry(data.enchantment);

			o.addProperty("level", data.enchantmentLevel);

			object.add("enchanted_book", o);
		}
		else if (src instanceof CavemanTrade.EffectEntry)
		{
			object.add("effect", JsonHelper.serializeEffectInstance(((CavemanTrade.EffectEntry)src).getEffect()));
		}

		object.addProperty("cost", src.getCost());
		object.addProperty("rank", src.getRankName());

		return object;
	}

	@Override
	public CavemanTrade.TradeEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();

		int cost;

		if (object.has("cost"))
		{
			cost = object.get("cost").getAsInt();
		}
		else
		{
			cost = 0;
		}

		String rank;

		if (object.has("rank"))
		{
			rank = object.get("rank").getAsString();
		}
		else
		{
			rank = MinerRank.BEGINNER.getName();
		}

		if (object.has("item"))
		{
			ItemStack stack = JsonHelper.deserializeItemStack(object.get("item").getAsJsonObject());

			if (!stack.isEmpty())
			{
				return new CavemanTrade.ItemStackEntry(stack, cost, rank);
			}
		}
		else if (object.has("enchanted_book"))
		{
			JsonObject o = object.get("enchanted_book").getAsJsonObject();
			Enchantment ench = JsonHelper.deserializeEnchantment(o);

			if (ench != null)
			{
				int level;

				if (o.has("level"))
				{
					level = o.get("level").getAsInt();
				}
				else
				{
					level = ench.getMinLevel();
				}

				return new CavemanTrade.EnchantedBookEntry(new EnchantmentData(ench, level), cost, rank);
			}
		}
		else if (object.has("effect"))
		{
			EffectInstance effect = JsonHelper.deserializeEffectInstance(object.get("effect").getAsJsonObject());

			if (effect != null)
			{
				return new CavemanTrade.EffectEntry(effect, cost, rank);
			}
		}

		return CavemanTrade.EMPTY;
	}
}