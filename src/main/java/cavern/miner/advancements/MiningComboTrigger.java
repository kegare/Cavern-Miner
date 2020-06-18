package cavern.miner.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class MiningComboTrigger extends AbstractCriterionTrigger<MiningComboTrigger.Instance>
{
	private static final ResourceLocation ID = new ResourceLocation("cavern", "mining_combo");

	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	public MiningComboTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
	{
		return new MiningComboTrigger.Instance(MinMaxBounds.IntBound.fromJson(json.get("count")));
	}

	public void trigger(ServerPlayerEntity player, int count)
	{
		func_227070_a_(player.getAdvancements(), o -> o.test(count));
	}

	public static class Instance extends CriterionInstance
	{
		private final MinMaxBounds.IntBound count;

		public Instance(MinMaxBounds.IntBound count)
		{
			super(ID);
			this.count = count;
		}

		public boolean test(int value)
		{
			return count.test(value);
		}

		@Override
		public JsonElement serialize()
		{
			JsonObject object = new JsonObject();

			object.add("count", count.serialize());

			return object;
		}
	}
}