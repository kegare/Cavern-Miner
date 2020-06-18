package cavern.miner.item;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.RegistryObject;

public class CaveSpawnEggItem<T extends EntityType<?>> extends SpawnEggItem
{
	private final RegistryObject<T> entry;

	public CaveSpawnEggItem(RegistryObject<T> type, EntityType<?> parentType, int primaryColor, int secondaryColor, Properties builder)
	{
		super(parentType, primaryColor, secondaryColor, builder);
		this.entry = type;
	}

	@Override
	public EntityType<?> getType(@Nullable CompoundNBT nbt)
	{
		T type = entry.orElse(null);

		if (type == null)
		{
			return super.getType(nbt);
		}

		if (nbt != null && nbt.contains("EntityTag", Constants.NBT.TAG_COMPOUND))
		{
			CompoundNBT tag = nbt.getCompound("EntityTag");

			if (tag.contains("id", Constants.NBT.TAG_STRING))
			{
				return EntityType.byKey(tag.getString("id")).orElse(type);
			}
		}

		return type;
	}
}