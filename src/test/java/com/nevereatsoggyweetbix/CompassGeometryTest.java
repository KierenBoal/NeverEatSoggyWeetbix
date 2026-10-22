package com.nevereatsoggyweetbix;

import java.awt.geom.AffineTransform;
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
		assertTrue(geometry.prepareLabelRing());
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
			for (double offset : new double[]{-60, 0, 60})
			{
				AffineTransform placement = new AffineTransform();
				assertTrue(geometry.labelTransform(direction, offset, placement));
				Point2D.Double character = unproject(anchor,
					new Point2D.Double(placement.getTranslateX(), placement.getTranslateY()));
				assertEquals("Character centers must follow the ring", labelRadius,
					character.distance(anchor.x, anchor.y), 0.1);
				double angle = Math.atan2(character.x - anchor.x, character.y - anchor.y);
				Point2D.Double before = anchor.project(Math.sin(angle - 1e-4) * labelRadius, Math.cos(angle - 1e-4) * labelRadius);
				Point2D.Double after = anchor.project(Math.sin(angle + 1e-4) * labelRadius, Math.cos(angle + 1e-4) * labelRadius);
				double length = before.distance(after);
				assertEquals("Character baselines must follow the tangent", (after.x - before.x) / length, placement.getScaleX(), 0.005);
				assertEquals((after.y - before.y) / length, placement.getShearY(), 0.005);
			}
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
	public void zeroRadiusCollapsesInnerRingAndSpokesButRetainsOuterBandAndLabels()
	{
		CompassAnchor anchor = anchor(5000, -1200, 0.7, 0);
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(anchor, 0, 40);
		assertNull(geometry.inner.getCurrentPoint());
		assertNotNull(geometry.outer.getCurrentPoint());
		double radius = 20 / anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
		for (CompassDirection direction : CompassDirection.ALL)
		{
			assertNull(geometry.spokes[direction.ordinal()].getCurrentPoint());
			Point2D.Double point = unproject(anchor, geometry.labels[direction.ordinal()]);
			assertEquals(radius, point.distance(anchor.x, anchor.y), 1e-6);
		}
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
