package cavern.miner.config;

import java.io.File;
import java.io.Reader;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import cavern.miner.config.json.ItemStackSerializer;
import cavern.miner.init.CaveDimensions;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.world.dimension.DimensionType;

public class CavebornConfig extends AbstractEntryConfig
{
	private final NonNullList<ItemStack> items = NonNullList.create();

	public CavebornConfig()
	{
		super(new File(CavernModConfig.getConfigDir(), "caveborn_items.json"));
	}

	public NonNullList<ItemStack> getItems()
	{
		return items;
	}

	@Override
	public boolean isEmpty()
	{
		return items.isEmpty();
	}

	@Override
	public boolean isAllowEmpty()
	{
		return true;
	}

	@Override
	public String toJson() throws JsonParseException
	{
		if (items.isEmpty())
		{
			return null;
		}

		JsonArray array = new JsonArray();

		for (ItemStack stack : items)
		{
			JsonElement e = ItemStackSerializer.INSTANCE.serialize(stack, stack.getClass(), null);

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

		items.clear();

		for (JsonElement e : array)
		{
			ItemStack stack = ItemStackSerializer.INSTANCE.deserialize(e, e.getClass(), null);

			if (!stack.isEmpty())
			{
				items.add(stack);
			}
		}
	}

	@Override
	public void setToDefault()
	{
		items.clear();
		items.add(new ItemStack(Items.TORCH, 64));
		items.add(new ItemStack(Items.BREAD, 32));
		items.add(new ItemStack(Items.STICK, 16));
		items.add(new ItemStack(Items.STONE_SWORD));
		items.add(new ItemStack(Items.STONE_PICKAXE));
		items.add(new ItemStack(Items.STONE_AXE));
		items.add(new ItemStack(Items.STONE_SHOVEL));
	}

	public enum Type
	{
		DISABLED(() -> null),
		CAVERN(() -> CaveDimensions.CAVERN_TYPE),
		HUGE_CAVERN(() -> CaveDimensions.HUGE_CAVERN_TYPE);

		private final Supplier<DimensionType> dimension;

		private Type(Supplier<DimensionType> dimension)
		{
			this.dimension = dimension;
		}

		@Nullable
		public DimensionType getDimension()
		{
			return dimension.get();
		}
	}
}