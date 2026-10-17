package com.nevereatsoggyweetbix;

import net.runelite.api.Perspective;

public enum HighlightCalculation
{
	FACING_CAMERA("Facing camera"),
	FACING_PLAYER("Facing player"),
	STATIC("Static");

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
}
