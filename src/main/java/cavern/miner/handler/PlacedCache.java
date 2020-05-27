package cavern.miner.handler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;

public class PlacedCache
{
	private final Cache<BlockPos, IBlockState> placedCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(10, TimeUnit.MINUTES).build();

	public void addCache(BlockPos pos, IBlockState state)
	{
		placedCache.put(pos, state);
	}

	@Nullable
	public IBlockState getCache(BlockPos pos)
	{
		IBlockState cache = placedCache.getIfPresent(pos);

		if (cache == null)
		{
			return null;
		}

		placedCache.invalidate(pos);

		return cache;
	}

	private static final Map<Pair<String, DimensionType>, PlacedCache> CACHES = Maps.newHashMap();

	public static PlacedCache get(@Nullable String name, DimensionType dim)
	{
		if (Strings.isNullOrEmpty(name))
		{
			name = "Cache";
		}

		Pair<String, DimensionType> key = Pair.of(name, dim);
		PlacedCache cache = CACHES.get(key);

		if (cache == null)
		{
			cache = new PlacedCache();

			CACHES.put(key, cache);
		}

		return cache;
	}
}