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
			double labelRadius = 256 + 20 / anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
			Point2D.Double label = unproject(anchor, geometry.labels[direction.ordinal()]);
			assertEquals(anchor.x + direction.x * labelRadius, label.x, 1e-6);
			assertEquals(anchor.y + direction.y * labelRadius, label.y, 1e-6);
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
	public void ringsAndLabelsRemainWorldSpaceCirclesAcrossPitchYawAndZoom()
	{
		for (double pitch : new double[]{0.05, 0.3, 0.7, 1.3, Math.PI / 2})
		{
			for (double yaw : new double[]{0, 0.63, Math.PI / 2, Math.PI})
			{
				for (double scale : new double[]{400, 800, 1600})
				{
					CompassAnchor anchor = new CompassAnchor(new CompassProjection(
						CompassProjection.cameraMatrix(yaw, pitch,
							6400 + Math.sin(yaw) * 1400, 6400 - Math.cos(yaw) * 1400, -1200),
						400, 300, scale), 6400, 6400, 0, 0, 0);
					assertWorldRings(anchor, 256, 40);
				}
			}
		}
	}

	@Test
	public void ringsStayFlatAtTranslatedFractionalTileAndHeadAnchors()
	{
		CompassProjection projection = anchor(5000, -1200, 0.7, 0).projection;
		// Feet/mouse positions may be fractional; tile modes use tile centers.
		for (double[] position : new double[][]{{6400.5, 6400.25}, {6464, 6464}, {6500.75, 6330.5}})
		{
			for (double height : new double[]{-692, -512, 0, 512})
			{
				assertWorldRings(new CompassAnchor(projection, position[0], position[1], height, 100, -50), 256, 40);
			}
		}
	}

	private static void assertWorldRings(CompassAnchor anchor, double radius, double bandPixels)
	{
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(anchor, radius, bandPixels);
		double worldGap = bandPixels / anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
		PathIterator inner = geometry.inner.getPathIterator(null);
		PathIterator outer = geometry.outer.getPathIterator(null);
		double[] a = new double[6], b = new double[6];
		int points = 0;
		while (!inner.isDone())
		{
			assertFalse(outer.isDone());
			assertEquals(inner.currentSegment(a), outer.currentSegment(b));
			Point2D.Double groundInner = unproject(anchor, new Point2D.Double(a[0], a[1]));
			Point2D.Double groundOuter = unproject(anchor, new Point2D.Double(b[0], b[1]));
			assertEquals(radius, groundInner.distance(anchor.x, anchor.y), 1e-6);
			assertEquals(radius + worldGap, groundOuter.distance(anchor.x, anchor.y), 1e-6);
			inner.next();
			outer.next();
			points++;
		}
		assertEquals(CompassGeometry.SAMPLES + 1, points);
		assertTrue(outer.isDone());
		for (CompassDirection direction : CompassDirection.ALL)
		{
			Point2D.Double ground = unproject(anchor, geometry.labels[direction.ordinal()]);
			assertEquals(anchor.x + direction.x * (radius + worldGap / 2), ground.x, 1e-6);
			assertEquals(anchor.y + direction.y * (radius + worldGap / 2), ground.y, 1e-6);
		}
	}

	private static Point2D.Double unproject(CompassAnchor anchor, Point2D.Double point)
	{
		assertNotNull(point);
		Point2D.Double ground = anchor.projection.unproject(point.x - anchor.translateX,
			point.y - anchor.translateY, anchor.height);
		assertNotNull(ground);
		return ground;
	}

	@Test
	public void lowPitchGapForeshortensInsteadOfExpandingVerticallyOnScreen()
	{
		CompassAnchor anchor = anchor(5000, -100, 0.05, 0);
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(anchor, 256, 40);
		double gap = 40 / anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
		Point2D.Double northInner = anchor.project(0, 256);
		Point2D.Double northOuter = anchor.project(0, 256 + gap);
		Point2D.Double eastInner = anchor.project(256, 0);
		Point2D.Double eastOuter = anchor.project(256 + gap, 0);
		assertEquals(40, eastInner.distance(eastOuter), 1e-6);
		assertTrue(northInner.distance(northOuter) < 5);
		assertWorldRings(anchor, 256, 40);
	}

	@Test
	public void outerRingClippingDoesNotRemoveValidInnerRingSections()
	{
		CompassAnchor anchor = anchor(6090, -100, 0.2, 0);
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(anchor, 256, 400);
		assertEquals(CompassGeometry.SAMPLES + 1, pathPointCount(geometry.inner));
		assertTrue(pathPointCount(geometry.outer) < CompassGeometry.SAMPLES + 1);
		assertNotNull(anchor.project(0, -256));
		assertNotNull(geometry.spokes[CompassDirection.S.ordinal()].getCurrentPoint());
		assertNull(geometry.labels[CompassDirection.S.ordinal()]);
	}

	private static int pathPointCount(java.awt.geom.Path2D path)
	{
		int count = 0;
		for (PathIterator iterator = path.getPathIterator(null); !iterator.isDone(); iterator.next())
		{
			count++;
		}
		return count;
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
	public void missingModelAndInvalidProjectionHaveSafeFallbacks()
	{
		assertEquals(180, CompassAnchor.modelTop(null, 180), 0);
		assertEquals(0, CompassAnchor.modelTop(null, -1), 0);
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(anchor(5000, -1200, 0.7, 0), 256, 40);
		assertNotNull(geometry.inner.getCurrentPoint());
		geometry.update(anchor(9000, 0, 0, 0), 256, 40);
		assertNull(geometry.inner.getCurrentPoint());
		assertNull(geometry.outer.getCurrentPoint());
		for (CompassDirection direction : CompassDirection.ALL)
		{
			assertNull(geometry.labels[direction.ordinal()]);
			assertNull(geometry.spokes[direction.ordinal()].getCurrentPoint());
		}
	}
}
