package cavern.miner.config;

import java.io.File;
import java.io.Reader;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import cavern.miner.config.json.ItemStackTagListSerializer;
import cavern.miner.init.CaveDimensions;
import cavern.miner.util.ItemStackTagList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.dimension.DimensionType;

public class CavebornConfig extends AbstractEntryConfig
{
	private final ItemStackTagList items = ItemStackTagList.create();

	public CavebornConfig()
	{
		super(new File(CavernModConfig.getConfigDir(), "caveborn_items.json"));
	}

	public ItemStackTagList getItems()
	{
		return items;
	}

	@Override
	public String toJson() throws JsonParseException
	{
		if (items.isEmpty())
		{
			return null;
		}

		return getGson().toJson(ItemStackTagListSerializer.INSTANCE.serialize(items, items.getClass(), null));
	}

	@Override
	public void fromJson(Reader json) throws JsonParseException
	{
		JsonObject object = getGson().fromJson(json, JsonObject.class);
		ItemStackTagList list = ItemStackTagListSerializer.INSTANCE.deserialize(object, object.getClass(), null);

		items.clear();

		if (!list.getEntryList().isEmpty())
		{
			items.addEntries(list.getEntryList());
		}

		if (!list.getTagList().isEmpty())
		{
			items.addTags(list.getTagList());
		}
	}

	public void setDefault()
	{
		items.clear();
		items.add(new ItemStack(Items.TORCH, 64));
		items.add(new ItemStack(Items.BREAD, 32));
		items.add(Items.STONE_SWORD);
		items.add(Items.STONE_PICKAXE);
		items.add(Items.STONE_AXE);
		items.add(Items.STONE_SHOVEL);
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