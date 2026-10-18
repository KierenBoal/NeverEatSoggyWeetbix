package com.nevereatsoggyweetbix;

import java.awt.BasicStroke;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class DirectionalIndicatorGeometryTest
{
	private final PlayerCompassConfig config = new PlayerCompassConfig()
	{
		@Override public DirectionalIndicatorShape directionalIndicatorShape() { return DirectionalIndicatorShape.TRIANGLE; }
		@Override public int directionalIndicatorHeight() { return 16; }
	};

	static CompassAnchor anchor(double yaw, double pitch, double scale)
	{
		return new CompassAnchor(new CompassProjection(CompassProjection.cameraMatrix(yaw, pitch,
			6400 + Math.sin(yaw) * 1400, 6400 - Math.cos(yaw) * 1400, -1200),
			400, 300, scale), 6400, 6400, 0, 0, 0);
	}

	@Test
	public void triangleTracksContinuousHeadingAndKeepsDimensionsOnItsPlane()
	{
		for (double yaw : new double[]{0, 0.7, -2.1})
		{
			for (double pitch : new double[]{0.1, 0.7, 1.4})
			{
				for (double zoom : new double[]{400, 1200})
				{
					CompassAnchor base = anchor(yaw, pitch, zoom);
					CompassAnchor anchor = new CompassAnchor(base.projection, 6400.5, 6400.25, -120, 37, -22);
					double scale = anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
					for (double heading = 0; heading < Math.PI * 2; heading += 0.137)
					{
						CompassGeometry geometry = new CompassGeometry();
						geometry.update(anchor, 192, 40);
						geometry.indicator.update(anchor, geometry.outerRadius, heading, config);
						List<Point2D.Double> points = groundPoints(anchor, geometry.indicator);
						assertEquals(3, points.size());
						Point2D.Double a = points.get(0), b = points.get(1), tip = points.get(2);
						double x = (a.x + b.x) / 2, y = (a.y + b.y) / 2;
						assertEquals(32, a.distance(b) * scale, 1e-6);
						assertEquals(16 * Math.sin(heading), (tip.x - x) * scale, 1e-6);
						assertEquals(16 * Math.cos(heading), (tip.y - y) * scale, 1e-6);
						assertEquals(Math.sin(heading), (x - anchor.x) / Point2D.distance(x, y, anchor.x, anchor.y), 1e-8);
						assertTrue(Point2D.distance(x, y, anchor.x, anchor.y) > geometry.outerRadius + 4 / scale);
						Area overlap = new Area(new BasicStroke(1).createStrokedShape(geometry.outer));
						Area silhouette = new Area(geometry.indicator.path);
						silhouette.add(new Area(new BasicStroke(1).createStrokedShape(geometry.indicator.path)));
						overlap.intersect(silhouette);
						assertTrue("Indicator must clear the ring", overlap.isEmpty());
					}
				}
			}
		}
	}

	@Test
	public void allShapesFitConfiguredWidthAndHeightAndRemainClosed()
	{
		CompassAnchor anchor = anchor(0.4, 0.8, 800);
		double scale = anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
		for (DirectionalIndicatorShape shape : DirectionalIndicatorShape.values())
		{
			PlayerCompassConfig config = new PlayerCompassConfig()
			{
				@Override public DirectionalIndicatorShape directionalIndicatorShape() { return shape; }
				@Override public int directionalIndicatorWidth() { return 48; }
				@Override public int directionalIndicatorHeight() { return 24; }
			};
			DirectionalIndicatorGeometry indicator = new DirectionalIndicatorGeometry();
			indicator.update(anchor, 200, 0, config);
			List<Point2D.Double> points = groundPoints(anchor, indicator);
			double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
			double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
			for (Point2D.Double point : points)
			{
				minX = Math.min(minX, point.x); maxX = Math.max(maxX, point.x);
				minY = Math.min(minY, point.y); maxY = Math.max(maxY, point.y);
			}
			assertEquals(48, (maxX - minX) * scale, 1e-6);
			assertEquals(24, (maxY - minY) * scale, 1e-6);
			assertEquals(shape == DirectionalIndicatorShape.CIRCLE ? 64
				: shape == DirectionalIndicatorShape.ARROW ? 7
				: shape == DirectionalIndicatorShape.SQUARE ? 4 : 3, points.size());
		}
	}

	@Test
	public void disabledInvalidAndPartlyClippedShapesClearPreviousGeometry()
	{
		DirectionalIndicatorGeometry indicator = new DirectionalIndicatorGeometry();
		CompassAnchor valid = anchor(0, 0.7, 800);
		indicator.update(valid, 200, 0, config);
		assertNotNull(indicator.path.getCurrentPoint());
		indicator.update(valid, 200, 0, new PlayerCompassConfig()
		{
			@Override public boolean directionalIndicatorEnabled() { return false; }
		});
		assertNull(indicator.path.getCurrentPoint());
		indicator.update(valid, Double.NaN, 0, config);
		assertNull(indicator.path.getCurrentPoint());
		indicator.update(valid, 200, Double.NaN, config);
		assertNull(indicator.path.getCurrentPoint());
		// Ring edge is visible but the south-pointing tip crosses the near plane.
		CompassAnchor clipped = new CompassAnchor(new CompassProjection(
			CompassProjection.cameraMatrix(0, 0.2, 0, 0, -100), 400, 300, 800),
			0, 100, 0, 0, 0);
		assertNotNull(clipped.project(0, -60));
		indicator.update(clipped, 60, Math.PI, new PlayerCompassConfig()
		{
			@Override public int directionalIndicatorHeight() { return 128; }
		});
		assertNull(indicator.path.getCurrentPoint());
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(valid, 192, 40);
		geometry.update(clipped, Double.NaN, 40);
		assertTrue(Double.isNaN(geometry.outerRadius));
	}

	private static List<Point2D.Double> groundPoints(CompassAnchor anchor, DirectionalIndicatorGeometry indicator)
	{
		List<Point2D.Double> points = new ArrayList<>();
		double[] xy = new double[6];
		boolean closed = false;
		for (PathIterator iterator = indicator.path.getPathIterator(null); !iterator.isDone(); iterator.next())
		{
			if (iterator.currentSegment(xy) == PathIterator.SEG_CLOSE)
			{
				closed = true;
				continue;
			}
			Point2D.Double ground = anchor.projection.unproject(xy[0] - anchor.translateX,
				xy[1] - anchor.translateY, anchor.height);
			assertNotNull(ground);
			points.add(ground);
		}
		assertTrue(closed);
		return points;
	}
}
