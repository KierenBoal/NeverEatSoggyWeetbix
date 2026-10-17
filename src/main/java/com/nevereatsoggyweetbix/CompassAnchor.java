package com.nevereatsoggyweetbix;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

final class CompassAnchor
{
	final CompassProjection projection;
	final double x;
	final double y;
	final double height;
	final double translateX;
	final double translateY;

	CompassAnchor(CompassProjection projection, double x, double y, double height,
		double translateX, double translateY)
	{
		this.projection = projection;
		this.x = x;
		this.y = y;
		this.height = height;
		this.translateX = translateX;
		this.translateY = translateY;
	}

	static CompassAnchor resolve(Client client, Player player, Model model, PlayerCompassConfig config,
		Rectangle viewport)
	{
		WorldView worldView = player.getWorldView();
		LocalPoint location = player.getLocalLocation();
		if (worldView == null || location == null)
		{
			return null;
		}
		AnchorMode mode = config.anchorMode();
		boolean mouseAnchor = mode == AnchorMode.MOUSE_POSITION || mode == AnchorMode.MOUSE_TILE;
		Point mouse = client.getMouseCanvasPosition();
		if (mouseAnchor)
		{
			if (mouse == null || !viewport.contains(mouse.getX(), mouse.getY()) || client.isMenuOpen())
			{
				return null;
			}
			if (mode == AnchorMode.MOUSE_TILE)
			{
				Tile tile = worldView.getSelectedSceneTile();
				if (tile == null && !worldView.isTopLevel())
				{
					worldView = client.getTopLevelWorldView();
					tile = worldView == null ? null : worldView.getSelectedSceneTile();
				}
				if (tile == null || tile.getPlane() != worldView.getPlane())
				{
					return null;
				}
				location = tile.getLocalLocation();
			}
		}
		else if (mode == AnchorMode.PLAYER_TRUE_TILE)
		{
			location = LocalPoint.fromWorld(worldView, player.getWorldLocation());
		}
		if (location == null || !worldView.contains(location))
		{
			return null;
		}
		CompassProjection projection = CompassProjection.create(client, worldView);
		if (projection == null)
		{
			return null;
		}
		double x = location.getX(), y = location.getY();
		Integer groundHeight = groundHeight(client, worldView, location);
		if (groundHeight == null)
		{
			return null;
		}
		double height = groundHeight;
		if (mode != AnchorMode.MOUSE_TILE && mode != AnchorMode.PLAYER_TRUE_TILE)
		{
			height -= player.getAnimationHeightOffset();
		}
		if (mode == AnchorMode.PLAYER_HEAD)
		{
			height -= modelTop(model, player.getLogicalHeight());
		}
		if (mode == AnchorMode.MOUSE_POSITION)
		{
			Point2D.Double ground = projection.unproject(mouse.getX(), mouse.getY(), height);
			if (ground == null)
			{
				return null;
			}
			x = ground.x;
			y = ground.y;
		}
		// RuneScape height coordinates increase downward: positive config offsets rise.
		height -= Math.max(-512, Math.min(512, config.heightOffset()));
		Point2D.Double center = projection.project(x, y, height);
		if (center == null)
		{
			return null;
		}
		double translateX = 0, translateY = 0;
		if (mode == AnchorMode.SCREEN_CENTER)
		{
			translateX = client.getCanvasWidth() / 2.0 - center.x;
			translateY = client.getCanvasHeight() / 2.0 - center.y;
		}
		return new CompassAnchor(projection, x, y, height, translateX, translateY);
	}

	Point2D.Double project(double dx, double dy)
	{
		Point2D.Double point = projection.project(x + dx, y + dy, height);
		if (point != null)
		{
			point.x += translateX;
			point.y += translateY;
		}
		return point;
	}

	static double modelTop(Model model, int fallback)
	{
		double top = 0;
		if (model != null)
		{
			float[] heights = model.getVerticesY();
			for (int i = 0; i < model.getVerticesCount(); i++)
			{
				top = Math.max(top, -heights[i]);
			}
		}
		return top > 0 ? top : Math.max(0, fallback);
	}

	static Integer groundHeight(Client client, WorldView worldView, LocalPoint location)
	{
		if (worldView.isTopLevel())
		{
			return Perspective.getTileHeight(client, location, worldView.getPlane());
		}
		WorldView top = client.getTopLevelWorldView();
		WorldEntity entity = top == null ? null : top.worldEntities().byIndex(worldView.getId());
		if (entity == null || entity.getLocalLocation() == null)
		{
			return null;
		}
		// Match Perspective.localToCanvas: subview height includes the entity's base elevation.
		LocalPoint base = entity.getLocalLocation();
		return worldView.getTileHeight(location.getX(), location.getY(), worldView.getPlane())
			+ top.getTileHeight(base.getX(), base.getY(), top.getPlane());
	}
}
