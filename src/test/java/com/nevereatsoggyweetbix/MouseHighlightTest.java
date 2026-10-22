package com.nevereatsoggyweetbix;

import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;

public class MouseHighlightTest
{
	private final NeverEatSoggyWeetbixConfig config = new NeverEatSoggyWeetbixConfig() {};

	@Test
	public void mouseWorldDirectionSurvivesCameraRotationPitchZoomAndFeetHeight()
	{
		for (double yaw : new double[]{0, 0.7, Math.PI / 2, -2.4})
		{
			for (double pitch : new double[]{0.25, 0.8, 1.4})
			{
				for (double scale : new double[]{400, 1000})
				{
					CompassProjection projection = projection(yaw, pitch, scale);
					for (double height : new double[]{-512, -120, 512})
					{
						for (CompassDirection direction : CompassDirection.ALL)
						{
							Point2D.Double mouse = projection.project(6400 + direction.x * 256,
								6400 + direction.y * 256, height);
							assertNotNull(mouse);
							double heading = HighlightCalculation.mouseHeading(projection, projection,
								6400, 6400, height, mouse.x, mouse.y);
							assertEquals(direction.x, Math.sin(heading), 1e-9);
							assertEquals(direction.y, Math.cos(heading), 1e-9);
							assertEquals(direction.diagonal() ? config.currentSubColor() : config.currentColor(),
								direction.color(config, heading));
						}
					}
				}
			}
		}
	}

	@Test
	public void cursorDirectionConvertsBetweenPlayerAndAnchorWorldViewAxes()
	{
		CompassProjection player = projection(0, 0.8, 800);
		CompassProjection anchor = projection(Math.PI / 2, 0.8, 800);
		Point2D.Double mouse = player.project(6656, 6400, -120);
		double heading = HighlightCalculation.mouseHeading(anchor, player, 6400, 6400, -120, mouse.x, mouse.y);
		assertEquals(0, heading, 1e-9);
		assertEquals(config.currentColor(), CompassDirection.N.color(config, heading));
	}

	@Test
	public void missingProjectionFeetAndInvalidRaysFallBackToCamera()
	{
		CompassProjection projection = projection(0.7, 0.8, 800);
		Point2D.Double feet = projection.project(6400, 6400, -120);
		assertEquals(projection.heading(), HighlightCalculation.mouseHeading(projection, projection,
			6400, 6400, -120, feet.x, feet.y), 0);
		assertEquals(projection.heading(), HighlightCalculation.mouseHeading(projection, null,
			6400, 6400, -120, 400, 300), 0);
		assertEquals(projection.heading(), HighlightCalculation.mouseHeading(projection, projection,
			6400, 6400, -120, Double.NaN, 300), 0);
		CompassProjection horizon = new CompassProjection(
			CompassProjection.cameraMatrix(0, 0, 0, 0, 0), 400, 300, 800);
		assertEquals(projection.heading(), HighlightCalculation.mouseHeading(projection, horizon,
			0, 100, 0, 400, 300), 0);
	}

	private static CompassProjection projection(double yaw, double pitch, double scale)
	{
		return new CompassProjection(CompassProjection.cameraMatrix(yaw, pitch,
			6400 + Math.sin(yaw) * 1500, 6400 - Math.cos(yaw) * 1500, -1200), 400, 300, scale);
	}
}
