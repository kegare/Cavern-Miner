package cavern.miner.config;

import java.io.File;
import java.io.Reader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import cavern.miner.config.json.CavemanTradeSerializer;
import cavern.miner.entity.CavemanTrade;
import cavern.miner.init.CaveEnchantments;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.NonNullList;

public class CavemanTradeConfig extends AbstractEntryConfig
{
	private final NonNullList<CavemanTrade.TradeEntry> entries = NonNullList.create();

	public CavemanTradeConfig()
	{
		super(new File(CavernModConfig.getConfigDir(), "caveman_trades.json"));
	}

	public NonNullList<CavemanTrade.TradeEntry> getEntries()
	{
		return entries;
	}

	@Override
	public boolean isEmpty()
	{
		return entries.isEmpty();
	}

	@Override
	public String toJson() throws JsonParseException
	{
		if (entries.isEmpty())
		{
			return null;
		}

		JsonArray array = new JsonArray();

		for (CavemanTrade.TradeEntry entry : entries)
		{
			JsonElement e = CavemanTradeSerializer.INSTANCE.serialize(entry, entry.getClass(), null);

			if (e.isJsonNull() || e.toString().isEmpty())
			{
				continue;
			}

			array.add(e);
		}

		return getGson().toJson(array);
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonArray array = getGson().fromJson(json, JsonArray.class);

		if (array.size() == 0)
		{
			return;
		}

		entries.clear();

		for (JsonElement e : array)
		{
			if (e.isJsonNull() || !e.isJsonObject() || e.toString().isEmpty())
			{
				continue;
			}

			CavemanTrade.TradeEntry entry = CavemanTradeSerializer.INSTANCE.deserialize(e, e.getClass(), null);

			if (entry != CavemanTrade.EMPTY)
			{
				entries.add(entry);
			}
		}
	}

	@Override
	public void setToDefault()
	{
		entries.clear();

		entries.add(new CavemanTrade.ItemStackEntry(new ItemStack(Items.TORCH, 64), 100, 50, null));

		entries.add(new CavemanTrade.EnchantedBookEntry(new EnchantmentData(CaveEnchantments.VEIN_MINER.get(), 1), 50, 100, null));
		entries.add(new CavemanTrade.EnchantedBookEntry(new EnchantmentData(CaveEnchantments.VEIN_MINER.get(), 2), 30, 200, "STONE"));
		entries.add(new CavemanTrade.EnchantedBookEntry(new EnchantmentData(CaveEnchantments.VEIN_MINER.get(), 3), 20, 300, "IRON"));
		entries.add(new CavemanTrade.EnchantedBookEntry(new EnchantmentData(CaveEnchantments.VEIN_MINER.get(), 4), 5, 350, "GOLD"));
		entries.add(new CavemanTrade.EnchantedBookEntry(new EnchantmentData(CaveEnchantments.AREA_MINER.get(), 1), 50, 100, null));
		entries.add(new CavemanTrade.EnchantedBookEntry(new EnchantmentData(CaveEnchantments.AREA_MINER.get(), 2), 30, 200, "STONE"));

		entries.add(new CavemanTrade.EffectEntry(new EffectInstance(Effects.REGENERATION, 30 * 20, 1), 30, 50, "STONE"));
		entries.add(new CavemanTrade.EffectEntry(new EffectInstance(Effects.ABSORPTION, 30 * 20, 1), 30, 50, "STONE"));
		entries.add(new CavemanTrade.EffectEntry(new EffectInstance(Effects.MINING_FATIGUE, 300 * 20, 2), 50, 50, "STONE"));
		entries.add(new CavemanTrade.EffectEntry(new EffectInstance(Effects.STRENGTH, 300 * 20, 2), 50, 50, "STONE"));
		entries.add(new CavemanTrade.EffectEntry(new EffectInstance(Effects.RESISTANCE, 300 * 20, 2), 50, 50, "STONE"));
		entries.add(new CavemanTrade.EffectEntry(new EffectInstance(Effects.NIGHT_VISION, 180 * 20), 50, 50, "STONE"));
	}
}