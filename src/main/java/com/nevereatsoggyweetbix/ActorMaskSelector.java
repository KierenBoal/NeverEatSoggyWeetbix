package com.nevereatsoggyweetbix;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
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
	private final PriorityQueue<Entry<T>> best = new PriorityQueue<>(priority.reversed());

	void spawn(int worldView, int index, T actor)
	{
		tracked.put(key(worldView, index), new Entry<>(worldView, index, actor));
		clearSelection();
	}

	void despawn(int worldView, int index, T actor)
	{
		long key = key(worldView, index);
		Entry<T> current = tracked.get(key);
		if (current != null && current.actor == actor)
		{
			tracked.remove(key);
		}
		clearSelection();
	}

	void removeWorld(int worldView)
	{
		tracked.values().removeIf(entry -> entry.worldView == worldView);
		clearSelection();
	}

	int select(boolean drawAbove, int limit, Rectangle2D drawingBounds, T excludedActor,
		Function<T, Rectangle2D> boundsLookup)
	{
		clearSelection();
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
			double width = actorBounds == null ? drawingBounds.getWidth()
				: Math.min(drawingBounds.getMaxX(), actorBounds.getMaxX())
					- Math.max(drawingBounds.getMinX(), actorBounds.getMinX());
			double height = actorBounds == null ? drawingBounds.getHeight()
				: Math.min(drawingBounds.getMaxY(), actorBounds.getMaxY())
					- Math.max(drawingBounds.getMinY(), actorBounds.getMinY());
			if (width > 0 && height > 0)
			{
				entry.overlap = width * height;
				if (best.size() < limit)
				{
					best.offer(entry);
				}
				else if (priority.compare(entry, best.peek()) < 0)
				{
					best.poll();
					best.offer(entry);
				}
			}
		}
		// Drain worst-first, then reverse without copying the queue into a temporary array.
		while (!best.isEmpty())
		{
			candidates.add(best.poll());
		}
		Collections.reverse(candidates);
		return candidates.size();
	}

	void maskSelected(Consumer<T> maskActor)
	{
		try
		{
			for (Entry<T> entry : candidates)
			{
				// Each selected attempt consumes a slot, even when its model is unavailable.
				maskActor.accept(entry.actor);
			}
		}
		finally
		{
			clearSelection();
		}
	}

	void clearSelection()
	{
		candidates.clear();
		best.clear();
	}

	int size()
	{
		return tracked.size();
	}

	void clear()
	{
		tracked.clear();
		clearSelection();
	}

	private static long key(int worldView, int index)
	{
		return ((long) worldView << 32) | (index & 0xffffffffL);
	}
}
