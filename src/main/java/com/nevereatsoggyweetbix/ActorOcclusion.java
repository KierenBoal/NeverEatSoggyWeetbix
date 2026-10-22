package com.nevereatsoggyweetbix;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

abstract class ActorOcclusion<T extends Actor>
{
	protected final Client client;
	private final ActorMaskSelector<T> selector = new ActorMaskSelector<>();
	private final Map<Integer, CompassProjection> projections = new HashMap<>();
	private final Rectangle2D.Double projectedBounds = new Rectangle2D.Double();
	private final Point2D.Double projectedPoint = new Point2D.Double();
	private boolean active;

	ActorOcclusion(Client client)
	{
		this.client = client;
	}

	protected abstract Iterable<? extends T> actors(WorldView worldView);

	protected abstract int index(T actor);

	protected T excludedActor()
	{
		return null;
	}

	void start()
	{
		active = true;
		refresh();
	}

	void refresh()
	{
		reset();
		if (active)
		{
			seed(client.getTopLevelWorldView(), new HashSet<>());
		}
	}

	void worldLoaded(WorldView worldView)
	{
		if (active)
		{
			seed(worldView, new HashSet<>());
		}
	}

	private void seed(WorldView worldView, Set<Integer> visited)
	{
		if (worldView == null || !visited.add(worldView.getId()))
		{
			return;
		}
		for (T actor : actors(worldView))
		{
			spawn(actor);
		}
		for (WorldView child : worldView.worldViews())
		{
			seed(child, visited);
		}
	}

	void spawn(T actor)
	{
		if (active && actor != null && actor != excludedActor() && actor.getWorldView() != null)
		{
			selector.spawn(actor.getWorldView().getId(), index(actor), actor);
		}
	}

	void despawn(T actor)
	{
		if (actor != null && actor.getWorldView() != null)
		{
			selector.despawn(actor.getWorldView().getId(), index(actor), actor);
		}
	}

	void worldUnloaded(WorldView worldView)
	{
		removeWorld(worldView, new HashSet<>());
	}

	private void removeWorld(WorldView worldView, Set<Integer> visited)
	{
		if (worldView == null || !visited.add(worldView.getId()))
		{
			return;
		}
		selector.removeWorld(worldView.getId());
		projections.remove(worldView.getId());
		for (WorldView child : worldView.worldViews())
		{
			removeWorld(child, visited);
		}
	}

	int select(Rectangle bounds, boolean drawAbove, int limit)
	{
		discardSelection();
		if (!active || drawAbove || limit <= 0 || selector.size() == 0)
		{
			return 0;
		}
		return selector.select(false, limit, bounds, excludedActor(), this::coarseBounds);
	}

	void eraseSelected(Graphics2D graphics, Rectangle bounds, ActorCompassMask mask)
	{
		try
		{
			selector.maskSelected(actor ->
			{
				Model model = actor.getModel();
				if (model != null)
				{
					mask.erase(graphics, client, actor, model, bounds, projection(actor.getWorldView()));
				}
			});
		}
		finally
		{
			discardSelection();
		}
	}

	void discardSelection()
	{
		selector.clearSelection();
		projections.clear();
	}

	private Rectangle2D coarseBounds(T actor)
	{
		WorldView worldView = actor.getWorldView();
		LocalPoint location = actor.getLocalLocation();
		if (worldView == null || client.getWorldView(worldView.getId()) != worldView
			|| location == null || !worldView.contains(location))
		{
			projectedBounds.setRect(0, 0, 0, 0);
			return projectedBounds;
		}
		int footprint = actor.getFootprintSize();
		int height = Math.max(actor.getLogicalHeight(), actor.getModelHeight());
		Integer ground = CompassAnchor.groundHeight(client, worldView, location);
		if (footprint <= 0 || height <= 0 || ground == null)
		{
			return null;
		}
		// Cached actor fields only: requesting the animated model is reserved for selected slots.
		double radius = footprint * (double) Perspective.LOCAL_HALF_TILE_SIZE
			+ Perspective.LOCAL_TILE_SIZE;
		double base = ground - actor.getAnimationHeightOffset();
		return ScreenBounds.projectBox(projection(worldView), location.getX() - radius, location.getX() + radius,
			location.getY() - radius, location.getY() + radius,
			base - height - Perspective.LOCAL_TILE_SIZE, base + Perspective.LOCAL_HALF_TILE_SIZE,
			projectedBounds, projectedPoint);
	}

	private CompassProjection projection(WorldView worldView)
	{
		if (worldView == null)
		{
			return null;
		}
		int id = worldView.getId();
		if (!projections.containsKey(id))
		{
			projections.put(id, CompassProjection.create(client, worldView));
		}
		return projections.get(id);
	}

	void clear()
	{
		active = false;
		reset();
	}

	void reset()
	{
		selector.clear();
		projections.clear();
	}
}
