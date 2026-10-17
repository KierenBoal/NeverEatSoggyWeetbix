package com.nevereatsoggyweetbix;

import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompassGeometryTest
{
	private static CompassAnchor anchor(double cameraY, double cameraZ, double pitch, double height)
	{
		return new CompassAnchor(new CompassProjection(
			CompassProjection.cameraMatrix(0, pitch, 6400, cameraY, cameraZ), 400, 300, 800),
			6400, 6400, height, 0, 0);
	}

	@Test
	public void labelsOccupyAllEightDirectionsAndSpokesLeaveTheCenterClear()
	{
		CompassAnchor anchor = anchor(5000, -1200, 0.7, 0);
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(anchor, 256, 40);
		Point2D.Double center = anchor.project(0, 0);
		assertTrue(geometry.labels[CompassDirection.N.ordinal()].y < center.y);
		assertTrue(geometry.labels[CompassDirection.S.ordinal()].y > center.y);
		assertTrue(geometry.labels[CompassDirection.E.ordinal()].x > center.x);
		assertTrue(geometry.labels[CompassDirection.W.ordinal()].x < center.x);
		for (CompassDirection direction : CompassDirection.ALL)
		{
			Point2D.Double edge = anchor.project(direction.x * 256, direction.y * 256);
			assertEquals(20, edge.distance(geometry.labels[direction.ordinal()]), 1e-6);
			PathIterator iterator = geometry.spokes[direction.ordinal()].getPathIterator(null);
			double[] coordinates = new double[6];
			assertEquals(PathIterator.SEG_MOVETO, iterator.currentSegment(coordinates));
			Point2D.Double expectedStart = anchor.project(direction.x * 256 * 0.35, direction.y * 256 * 0.35);
			assertEquals(expectedStart.x, coordinates[0], 1e-6);
			assertEquals(expectedStart.y, coordinates[1], 1e-6);
			iterator.next();
			assertEquals(PathIterator.SEG_LINETO, iterator.currentSegment(coordinates));
			Point2D.Double expectedEnd = anchor.project(direction.x * 256 * 0.85, direction.y * 256 * 0.85);
			assertEquals(expectedEnd.x, coordinates[0], 1e-6);
			assertEquals(expectedEnd.y, coordinates[1], 1e-6);
			iterator.next();
			assertTrue(iterator.isDone());
		}
	}

	@Test
	public void ringGapUsesScreenPixelsRegardlessOfZoomOrAngle()
	{
		for (double pitch : new double[]{0.3, 0.7, 1.3})
		{
			CompassGeometry geometry = new CompassGeometry();
			geometry.update(anchor(5000, -1200, pitch, 0), 256, 40);
			PathIterator inner = geometry.inner.getPathIterator(null);
			PathIterator outer = geometry.outer.getPathIterator(null);
			double[] a = new double[6], b = new double[6];
			while (!inner.isDone())
			{
				assertEquals(inner.currentSegment(a), outer.currentSegment(b));
				assertEquals(40, Point2D.distance(a[0], a[1], b[0], b[1]), 1e-6);
				inner.next();
				outer.next();
			}
			assertTrue(outer.isDone());
		}
	}

	@Test
	public void behindCameraRingSectionsStartNewPathsInsteadOfBridging()
	{
		CompassGeometry geometry = new CompassGeometry();
		CompassAnchor anchor = anchor(6300, -100, 0.2, 0);
		geometry.update(anchor, 256, 40);
		int moves = 0;
		PathIterator path = geometry.inner.getPathIterator(null);
		double[] coordinates = new double[6];
		while (!path.isDone())
		{
			int type = path.currentSegment(coordinates);
			assertTrue(Double.isFinite(coordinates[0]));
			assertTrue(Double.isFinite(coordinates[1]));
			assertNotEquals(PathIterator.SEG_CLOSE, type);
			if (type == PathIterator.SEG_MOVETO)
			{
				moves++;
			}
			path.next();
		}
		assertEquals(2, moves);
		assertNull(geometry.labels[CompassDirection.S.ordinal()]);
		assertNull(geometry.spokes[CompassDirection.S.ordinal()].getCurrentPoint());
	}

	@Test
	public void maximumRadiusAndHeightOffsetsRemainFinite()
	{
		for (double height : new double[]{-512, 0, 512})
		{
			CompassGeometry geometry = new CompassGeometry();
			geometry.update(anchor(5000, -1200, 0.7, height), 128 * 128, 300);
			PathIterator path = geometry.outer.getPathIterator(null);
			double[] coordinates = new double[6];
			int samples = 0;
			while (!path.isDone())
			{
				path.currentSegment(coordinates);
				assertTrue(Double.isFinite(coordinates[0]));
				assertTrue(Double.isFinite(coordinates[1]));
				path.next();
				samples++;
			}
			assertTrue(samples > 0 && samples <= CompassGeometry.SAMPLES + 1);
		}
	}

	@Test
	public void screenTranslationPreservesShapeAndSetsCenter()
	{
		CompassAnchor original = anchor(5000, -1200, 0.7, -256);
		Point2D.Double center = original.project(0, 0);
		CompassAnchor translated = new CompassAnchor(original.projection, original.x, original.y,
			original.height, 500 - center.x, 400 - center.y);
		assertEquals(500, translated.project(0, 0).x, 1e-6);
		assertEquals(400, translated.project(0, 0).y, 1e-6);
		assertEquals(original.project(128, 64).distance(center),
			translated.project(128, 64).distance(translated.project(0, 0)), 1e-6);
	}

	@Test
	public void missingModelAndDegenerateRadialPointsHaveSafeFallbacks()
	{
		assertEquals(180, CompassAnchor.modelTop(null, 180), 0);
		assertEquals(0, CompassAnchor.modelTop(null, -1), 0);
		assertNull(CompassGeometry.expand(null, new Point2D.Double(), 40));
		assertNull(CompassGeometry.expand(new Point2D.Double(), null, 40));
		assertNull(CompassGeometry.expand(new Point2D.Double(), new Point2D.Double(), 40));
	}
}
