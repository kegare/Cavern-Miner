package cavern.miner.world.gen.feature;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import cavern.miner.world.vein.CavernVeinProvider;
import cavern.miner.world.vein.HugeCavernVeinProvider;
import cavern.miner.world.vein.VeinProvider;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class VeinFeatureConfig implements IFeatureConfig
{
	private final ProviderType type;

	public VeinFeatureConfig(ProviderType type)
	{
		this.type = type;
	}

	public VeinProvider getProvider()
	{
		return type.getProvider();
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> ops)
	{
		return new Dynamic<>(ops, ops.createString(type.getName()));
	}

	public static VeinFeatureConfig deserialize(Dynamic<?> data)
	{
		return new VeinFeatureConfig(ProviderType.byName(data.asString("")));
	}

	public enum ProviderType
	{
		CAVERN("cavern", new CavernVeinProvider()),
		HUGE_CAVERN("huge_cavern", new HugeCavernVeinProvider());

		private final String name;
		private final VeinProvider provider;

		private static final Map<String, ProviderType> VALUES_MAP = Arrays.stream(values()).collect(Collectors.toMap(ProviderType::getName, o -> o));

		private ProviderType(String name, VeinProvider provider)
		{
			this.name = name;
			this.provider = provider;
		}

		public String getName()
		{
			return name;
		}

		public VeinProvider getProvider()
		{
			return provider;
		}

		public VeinFeatureConfig createConfig()
		{
			return new VeinFeatureConfig(this);
		}

		public static ProviderType byName(String name)
		{
			return VALUES_MAP.get(name);
		}
	}
}