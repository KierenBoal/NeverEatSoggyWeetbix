package com.nevereatsoggyweetbix;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/** Event-maintained registry and bounded screen-overlap selection, independent of model loading. */
final class ActorMaskSelector<T>
{
	private static final class Entry<T>
	{
		private final int worldView;
		private final int index;
		private final T actor;
		private double overlap;

		private Entry(int worldView, int index, T actor)
		{
			this.worldView = worldView;
			this.index = index;
			this.actor = actor;
		}
	}

	private final Map<Long, Entry<T>> tracked = new HashMap<>();
	private final List<Entry<T>> candidates = new ArrayList<>();
	private final Comparator<Entry<T>> priority = Comparator
		.<Entry<T>>comparingDouble(entry -> entry.overlap).reversed()
		.thenComparingInt(entry -> entry.worldView).thenComparingInt(entry -> entry.index);

	void spawn(int worldView, int index, T actor)
	{
		tracked.put(key(worldView, index), new Entry<>(worldView, index, actor));
	}

	void despawn(int worldView, int index, T actor)
	{
		long key = key(worldView, index);
		Entry<T> current = tracked.get(key);
		if (current != null && current.actor == actor)
		{
			tracked.remove(key);
		}
		candidates.clear();
	}

	void removeWorld(int worldView)
	{
		tracked.values().removeIf(entry -> entry.worldView == worldView);
		candidates.clear();
	}

	int mask(boolean drawAbove, int limit, Rectangle2D drawingBounds,
		Function<T, Rectangle2D> boundsLookup, Consumer<T> maskActor)
	{
		return mask(drawAbove, limit, drawingBounds, null, boundsLookup, maskActor);
	}

	int mask(boolean drawAbove, int limit, Rectangle2D drawingBounds, T excludedActor,
		Function<T, Rectangle2D> boundsLookup, Consumer<T> maskActor)
	{
		candidates.clear();
		if (drawAbove || limit <= 0 || drawingBounds.isEmpty())
		{
			return 0;
		}
		for (Entry<T> entry : tracked.values())
		{
			if (entry.actor == excludedActor)
			{
				continue;
			}
			Rectangle2D actorBounds = boundsLookup.apply(entry.actor);
			Rectangle2D overlap = actorBounds == null ? drawingBounds : drawingBounds.createIntersection(actorBounds);
			if (!overlap.isEmpty())
			{
				entry.overlap = overlap.getWidth() * overlap.getHeight();
				candidates.add(entry);
			}
		}
		candidates.sort(priority);
		int count = Math.min(limit, candidates.size());
		for (int i = 0; i < count; i++)
		{
			// Each selected attempt consumes a slot, even when its model is unavailable.
			maskActor.accept(candidates.get(i).actor);
		}
		candidates.clear();
		return count;
	}

	int size()
	{
		return tracked.size();
	}

	void clear()
	{
		tracked.clear();
		candidates.clear();
	}

	private static long key(int worldView, int index)
	{
		return ((long) worldView << 32) | (index & 0xffffffffL);
	}
}
