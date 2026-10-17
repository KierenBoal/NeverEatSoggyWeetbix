package com.nevereatsoggyweetbix;

import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompassProjectionTest
{
	@Test
	public void groundProjectionRoundTripsAcrossCameraAnglesAndHeights()
	{
		for (double yaw : new double[]{0, 0.63, Math.PI / 2, Math.PI, -1.71, Math.PI * 2})
		{
			for (double pitch : new double[]{0.25, 0.8, 1.4})
			{
				CompassProjection projection = new CompassProjection(
					CompassProjection.cameraMatrix(yaw, pitch,
						6400 + Math.sin(yaw) * 1500, 6400 - Math.cos(yaw) * 1500, -1200),
					517, 363, 800);
				for (double height : new double[]{-512, 0, 512})
				{
					for (int offset = -128; offset <= 128; offset += 64)
					{
						double x = 6400 + offset, y = 6400 - offset;
						Point2D.Double screen = projection.project(x, y, height);
						assertNotNull(screen);
						Point2D.Double ground = projection.unproject(screen.x, screen.y, height);
						assertNotNull(ground);
						assertEquals(x, ground.x, 1e-6);
						assertEquals(y, ground.y, 1e-6);
					}
				}
			}
		}
	}

	@Test
	public void nearPlaneHorizonAndNonFiniteInputsAreRejected()
	{
		CompassProjection projection = new CompassProjection(
			CompassProjection.cameraMatrix(0, 0, 0, 0, 0), 400, 300, 800);
		assertNull(projection.project(0, 49, 0));
		assertNotNull(projection.project(0, 50, 0));
		assertNull(projection.project(0, -1000, 0));
		assertNull(projection.unproject(400, 300, 0));
		assertNull(projection.project(Double.NaN, 100, 0));
		assertNull(projection.unproject(Double.POSITIVE_INFINITY, 300, 0));
		assertNull(new CompassProjection(new double[3][4], 0, 0, 0).unproject(0, 0, 0));
	}

	@Test
	public void positiveHeightOffsetMovesAlongWorldUpRatherThanScreenYOnly()
	{
		CompassProjection projection = new CompassProjection(
			CompassProjection.cameraMatrix(0.4, 0.7, 6000, 5000, -1000), 400, 300, 800);
		Point2D.Double floor = projection.project(6400, 6400, 0);
		Point2D.Double raised = projection.project(6400, 6400, -256);
		assertNotNull(raised);
		assertTrue(raised.y < floor.y);
		assertTrue(Math.abs(raised.x - floor.x) > 1);
		Point2D.Double original = projection.unproject(raised.x, raised.y, -256);
		assertEquals(6400, original.x, 1e-6);
		assertEquals(6400, original.y, 1e-6);
	}

	@Test
	public void cameraFacingUsesClockwiseWorldHeadingAndIgnoresPitch()
	{
		for (double pitch : new double[]{0, 0.7, Math.PI / 2})
		{
			CompassProjection projection = new CompassProjection(
				CompassProjection.cameraMatrix(Math.PI / 2, pitch, 0, 0, 0), 0, 0, 800);
			assertEquals(-Math.PI / 2, projection.heading(), 1e-9);
		}
	}

	@Test
	public void continuousMouseRayUsesOneFeetPlaneWithoutTerrainOrSceneBoundarySnapping()
	{
		CompassProjection projection = new CompassProjection(
			CompassProjection.cameraMatrix(0.3, 0.7, 6000, 5000, -1200), 400, 300, 800);
		double feetHeight = -120;
		Point2D.Double previous = null;
		for (int x = 200; x <= 600; x++)
		{
			Point2D.Double ground = projection.unproject(x, 400, feetHeight);
			assertNotNull(ground);
			Point2D.Double screen = projection.project(ground.x, ground.y, feetHeight);
			assertEquals(x, screen.x, 1e-6);
			assertEquals(400, screen.y, 1e-6);
			if (previous != null)
			{
				assertTrue(ground.distance(previous) < 10);
			}
			previous = ground;
		}
		Point2D.Double outsideScene = projection.project(20000, 20000, feetHeight);
		Point2D.Double world = projection.unproject(outsideScene.x, outsideScene.y, feetHeight);
		assertEquals(20000, world.x, 1e-6);
		assertEquals(20000, world.y, 1e-6);
	}
}
