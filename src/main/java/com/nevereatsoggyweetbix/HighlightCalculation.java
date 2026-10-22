package com.nevereatsoggyweetbix;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Projection;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;

public enum HighlightCalculation
{
	FACING_CAMERA("Camera"),
	FACING_PLAYER("Player"),
	STATIC("North"),
	FROM_PLAYER_TO_MOUSE("Mouse"),
	DESTINATION_TILE("Destination");

	private final String label;

	HighlightCalculation(String label)
	{
		this.label = label;
	}

	double heading(CompassProjection anchorProjection, int playerOrientation, CompassProjection playerProjection)
	{
		switch (this)
		{
			case STATIC:
				return 0;
			case FACING_PLAYER:
				if (playerProjection != null)
				{
					// Actor angles start south; compass headings start north.
					// Use the full table length rather than the rounded Perspective.UNIT constant.
					int fullTurn = Perspective.SINE.length;
					int northBased = (Math.floorMod(playerOrientation, fullTurn) + fullTurn / 2) % fullTurn;
					double playerHeading = northBased * (Math.PI * 2 / fullTurn);
					// The same camera expressed in two views gives the rotation between their axes.
					return playerHeading + (anchorProjection.heading() - playerProjection.heading());
				}
				// A missing world-view projection cannot provide a reliable player heading.
				return anchorProjection.heading();
			default:
				return anchorProjection.heading();
		}
	}

	@Override
	public String toString()
	{
		return label;
	}

	/** Resolve the cursor on the player's feet plane, then express its heading in the anchor's axes. */
	static double mouseHeading(CompassProjection anchorProjection, CompassProjection playerProjection,
		double playerX, double playerY, double feetHeight, double mouseX, double mouseY)
	{
		if (playerProjection != null)
		{
			Point2D.Double target = playerProjection.unproject(mouseX, mouseY, feetHeight);
			if (target != null)
			{
				return pointHeading(anchorProjection, playerProjection, playerX, playerY, target.x, target.y);
			}
		}
		// No direction exists at the feet, outside the scene viewport, or beyond the ground horizon.
		return anchorProjection.heading();
	}

	static double destinationHeading(Client client, Player player, CompassProjection anchorProjection, Rectangle viewport)
	{
		WorldView playerView = player.getWorldView();
		LocalPoint playerLocation = player.getLocalLocation();
		if (playerView == null || playerLocation == null || !playerView.contains(playerLocation))
		{
			return anchorProjection.heading();
		}
		LocalPoint target = client.getLocalDestinationLocation();
		WorldView targetView;
		if (target != null)
		{
			targetView = client.getWorldView(target.getWorldView());
		}
		else
		{
			Point mouse = client.getMouseCanvasPosition();
			if (mouse == null || !viewport.contains(mouse.getX(), mouse.getY()) || client.isMenuOpen())
			{
				return anchorProjection.heading();
			}
			targetView = playerView;
			Tile tile = targetView.getSelectedSceneTile();
			if (tile == null && !targetView.isTopLevel())
			{
				targetView = client.getTopLevelWorldView();
				tile = targetView == null ? null : targetView.getSelectedSceneTile();
			}
			if (tile == null || tile.getPlane() != targetView.getPlane())
			{
				return anchorProjection.heading();
			}
			target = tile.getLocalLocation();
		}
		if (target == null || targetView == null || !targetView.contains(target))
		{
			return anchorProjection.heading();
		}
		if (targetView.getId() == playerView.getId())
		{
			return tileHeading(anchorProjection, CompassProjection.create(client, playerView),
				playerLocation.getX(), playerLocation.getY(), target);
		}
		target = LocalPoint.fromScene(target.getSceneX(), target.getSceneY(), targetView);
		// Compare different world views in top-level coordinates (e.g. a boat and the surrounding world).
		WorldView top = client.getTopLevelWorldView();
		Point2D.Double origin = mainWorldLocation(playerView, playerLocation);
		Point2D.Double destination = mainWorldLocation(targetView, target);
		if (top == null || origin == null || destination == null)
		{
			return anchorProjection.heading();
		}
		return pointHeading(anchorProjection, CompassProjection.create(client, top),
			origin.x, origin.y, destination.x, destination.y);
	}

	static double tileHeading(CompassProjection anchorProjection, CompassProjection playerProjection,
		double playerX, double playerY, LocalPoint target)
	{
		if (target == null)
		{
			return anchorProjection.heading();
		}
		// Aim at the center, never the cursor's position within the tile.
		double x = (target.getSceneX() + 0.5) * Perspective.LOCAL_TILE_SIZE;
		double y = (target.getSceneY() + 0.5) * Perspective.LOCAL_TILE_SIZE;
		return pointHeading(anchorProjection, playerProjection, playerX, playerY, x, y);
	}

	private static double pointHeading(CompassProjection anchorProjection, CompassProjection pointProjection,
		double playerX, double playerY, double targetX, double targetY)
	{
		double dx = targetX - playerX, dy = targetY - playerY;
		return pointProjection != null && Double.isFinite(dx) && Double.isFinite(dy) && Math.hypot(dx, dy) > 1e-6
			? Math.atan2(dx, dy) + anchorProjection.heading() - pointProjection.heading()
			: anchorProjection.heading();
	}

	private static Point2D.Double mainWorldLocation(WorldView worldView, LocalPoint location)
	{
		if (worldView.isTopLevel())
		{
			return new Point2D.Double(location.getX(), location.getY());
		}
		Projection projection = worldView.getMainWorldProjection();
		if (projection == null)
		{
			return null;
		}
		float[] point = projection.project(location.getX(), 0, location.getY());
		return new Point2D.Double(point[0], point[2]);
	}
}
