package cavern.miner.advancements;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class MinerRankTrigger extends AbstractCriterionTrigger<MinerRankTrigger.Instance>
{
	private static final ResourceLocation ID = new ResourceLocation("cavern", "miner_rank");

	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	public MinerRankTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context)
	{
		return new MinerRankTrigger.Instance(json.get("name").getAsString());
	}

	public void trigger(ServerPlayerEntity player, String name)
	{
		func_227070_a_(player.getAdvancements(), o -> o.test(name));
	}

	public static class Instance extends CriterionInstance
	{
		private final String name;

		public Instance(String name)
		{
			super(ID);
			this.name = name;
		}

		public boolean test(String value)
		{
			return name.equalsIgnoreCase(value);
		}

		@Override
		public JsonElement serialize()
		{
			JsonObject object = new JsonObject();

			object.addProperty("name", name);

			return object;
		}
	}
}