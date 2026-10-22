package com.nevereatsoggyweetbix;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import org.junit.Test;
import static org.junit.Assert.*;

public class ScreenBoundsTest
{
	private static CompassProjection projection()
	{
		return new CompassProjection(CompassProjection.cameraMatrix(0, 0, 0, 0, 0), 400, 300, 800);
	}

	@Test
	public void projectedBoxesSupportCheapOverlapRejection()
	{
		Rectangle2D visible = ScreenBounds.projectBox(projection(), -10, 10, 1000, 1020, -10, 10);
		assertNotNull(visible);
		assertTrue(visible.intersects(new Rectangle(390, 290, 20, 20)));
		Rectangle2D far = ScreenBounds.projectBox(projection(), 10000, 10100, 1000, 1020, -10, 10);
		assertFalse(far.intersects(new Rectangle(0, 0, 800, 600)));
	}

	@Test
	public void nearPlaneCrossingsRemainCandidatesButFullyBehindBoxesAreRejected()
	{
		assertNull(ScreenBounds.projectBox(projection(), -10, 10, 40, 100, -10, 10));
		assertTrue(ScreenBounds.projectBox(projection(), -10, 10, -200, -100, -10, 10).isEmpty());
		assertNull(ScreenBounds.projectBox(null, -10, 10, 100, 200, -10, 10));
	}
}
