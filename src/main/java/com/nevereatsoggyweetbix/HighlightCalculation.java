package com.nevereatsoggyweetbix;

import java.awt.geom.Point2D;
import net.runelite.api.Perspective;

public enum HighlightCalculation
{
	FACING_CAMERA("Facing camera"),
	FACING_PLAYER("Facing player"),
	STATIC("Static"),
	FROM_PLAYER_TO_MOUSE("From player to mouse");

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
				double dx = target.x - playerX, dy = target.y - playerY;
				if (Double.isFinite(dx) && Double.isFinite(dy) && Math.hypot(dx, dy) > 1e-6)
				{
					return Math.atan2(dx, dy) + anchorProjection.heading() - playerProjection.heading();
				}
			}
		}
		// No direction exists at the feet, outside the scene viewport, or beyond the ground horizon.
		return anchorProjection.heading();
	}
}
