package cavern.miner.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.Dimension;

public class PlacedCache
{
	private final Cache<BlockPos, BlockState> placedCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(10, TimeUnit.MINUTES).build();

	public void addCache(BlockPos pos, BlockState state)
	{
		placedCache.put(pos, state);
	}

	@Nullable
	public BlockState getCache(BlockPos pos)
	{
		BlockState cache = placedCache.getIfPresent(pos);

		if (cache == null)
		{
			return null;
		}

		placedCache.invalidate(pos);

		return cache;
	}

	private static final Map<Pair<String, Dimension>, PlacedCache> CACHES = new HashMap<>();

	public static PlacedCache get(@Nullable String name, Dimension dim)
	{
		if (Strings.isNullOrEmpty(name))
		{
			name = "Cache";
		}

		return CACHES.computeIfAbsent(Pair.of(name, dim), o -> new PlacedCache());
	}
}