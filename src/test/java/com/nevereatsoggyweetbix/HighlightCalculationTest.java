package com.nevereatsoggyweetbix;

import org.junit.Test;
import static org.junit.Assert.*;

public class HighlightCalculationTest
{
	private final PlayerCompassConfig config = new PlayerCompassConfig() {};

	private static CompassProjection camera(double yaw, double pitch)
	{
		return new CompassProjection(CompassProjection.cameraMatrix(yaw, pitch, 6400, 5000, -1200),
			400, 300, 800);
	}

	@Test
	public void cameraModeKeepsExistingHeadingRegardlessOfPlayerFacing()
	{
		for (double yaw : new double[]{0, 0.4, Math.PI / 2, Math.PI, -Math.PI / 2})
		{
			CompassProjection anchor = camera(yaw, 0.7);
			for (int orientation : new int[]{0, 512, 1024, 1536})
			{
				assertEquals(anchor.heading(), HighlightCalculation.FACING_CAMERA.heading(
					anchor, orientation, camera(yaw + 0.7, 0.2)), 0);
			}
		}
	}

	@Test
	public void playerModeRecognizesEveryDirectionAndIsIndependentOfCameraMovement()
	{
		for (double yaw : new double[]{0, 0.61, Math.PI, -2.2})
		{
			for (double pitch : new double[]{0.05, 0.7, Math.PI / 2})
			{
				CompassProjection projection = camera(yaw, pitch);
				for (int direction = 0; direction < 8; direction++)
				{
					int orientation = (1024 + direction * 256) % 2048;
					double heading = HighlightCalculation.FACING_PLAYER.heading(projection, orientation, projection);
					assertEquals(direction * Math.PI / 4, heading, 1e-9);
					int current = (direction & 1) == 0
						? CompassDirection.current(heading, false) : CompassDirection.current(heading, true);
					assertEquals(direction, current);
				}
			}
		}
	}

	@Test
	public void playerRotationTransitionsChooseClockwiseAtExactTies()
	{
		CompassProjection projection = camera(0.6, 0.7);
		assertEquals(CompassDirection.N.ordinal(), CompassDirection.current(
			HighlightCalculation.FACING_PLAYER.heading(projection, 1279, projection), false));
		assertEquals(CompassDirection.E.ordinal(), CompassDirection.current(
			HighlightCalculation.FACING_PLAYER.heading(projection, 1280, projection), false));
		assertEquals(CompassDirection.E.ordinal(), CompassDirection.current(
			HighlightCalculation.FACING_PLAYER.heading(projection, 1281, projection), false));
		assertEquals(CompassDirection.NW.ordinal(), CompassDirection.current(
			HighlightCalculation.FACING_PLAYER.heading(projection, 1023, projection), true));
		assertEquals(CompassDirection.NE.ordinal(), CompassDirection.current(
			HighlightCalculation.FACING_PLAYER.heading(projection, 1024, projection), true));
	}

	@Test
	public void playerFacingConvertsBetweenRotatedAnchorAndPlayerViews()
	{
		// Local north in the player's view points east in the main anchor view.
		for (double cameraRotation : new double[]{0, 0.6, -1.8})
		{
			CompassProjection main = camera(cameraRotation, 0.7);
			CompassProjection rotated = camera(cameraRotation + Math.PI / 2, 0.7);
			double mainHeading = HighlightCalculation.FACING_PLAYER.heading(main, 1024, rotated);
			assertEquals(CompassDirection.E.ordinal(), CompassDirection.current(mainHeading, false));
			assertEquals(Math.PI / 2, mainHeading, 1e-9);
			double localHeading = HighlightCalculation.FACING_PLAYER.heading(rotated, 1024, main);
			assertEquals(CompassDirection.W.ordinal(), CompassDirection.current(localHeading, false));
		}
	}

	@Test
	public void staticModeAlwaysUsesNorthAndNorthEastForCurrentColors()
	{
		for (double yaw : new double[]{0, 0.7, Math.PI, -2})
		{
			for (int orientation : new int[]{0, 512, 1024, 1536})
			{
				double heading = HighlightCalculation.STATIC.heading(camera(yaw, 0.3), orientation, null);
				assertEquals(0, heading, 0);
				assertEquals(config.currentColor(), CompassDirection.N.color(config, heading));
				assertEquals(config.oppositeColor(), CompassDirection.S.color(config, heading));
				assertEquals(config.currentSubColor(), CompassDirection.NE.color(config, heading));
				assertEquals(config.oppositeSubColor(), CompassDirection.SW.color(config, heading));
			}
		}
	}

	@Test
	public void unavailablePlayerProjectionFallsBackToCameraHeading()
	{
		CompassProjection projection = camera(0.63, 0.7);
		assertEquals(projection.heading(), HighlightCalculation.FACING_PLAYER.heading(projection, 1024, null), 0);
	}
}
