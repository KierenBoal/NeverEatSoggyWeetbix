package com.nevereatsoggyweetbix;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/** A reusable closed silhouette projected from the compass's horizontal plane. */
final class DirectionalIndicatorGeometry
{
	private static final double[][] TRIANGLE = {{-0.5, 0}, {0.5, 0}, {0, 1}};
	private static final double[][] ARROW = {
		{-0.18, 0}, {0.18, 0}, {0.18, 0.5}, {0.5, 0.5},
		{0, 1}, {-0.5, 0.5}, {-0.18, 0.5}
	};
	private static final double[][] SQUARE = {{-0.5, 0}, {0.5, 0}, {0.5, 1}, {-0.5, 1}};
	private static final double[][] CIRCLE = new double[64][2];

	static
	{
		for (int i = 0; i < CIRCLE.length; i++)
		{
			double angle = i * Math.PI * 2 / CIRCLE.length;
			CIRCLE[i][0] = Math.sin(angle) / 2;
			CIRCLE[i][1] = (1 - Math.cos(angle)) / 2;
		}
	}

	final Path2D.Double path = new Path2D.Double();

	static double rotationHeading(double heading, NeverEatSoggyWeetbixConfig config)
	{
		if (!config.limitRotationSteps() || !Double.isFinite(heading))
		{
			return heading;
		}
		int steps = Math.max(1, Math.min(360, config.rotationSteps()));
		double turn = Math.PI * 2;
		double step = turn / steps;
		// Round signed angles first: adding a turn before rounding can lose negative halfway ties.
		double rounded = Math.floor((heading % turn) / step + 0.5);
		return ((rounded % steps + steps) % steps) * step;
	}

	void update(CompassAnchor anchor, double outerRadius, double heading, NeverEatSoggyWeetbixConfig config)
	{
		path.reset();
		if (!Double.isFinite(outerRadius) || outerRadius < 0
			|| !Double.isFinite(heading))
		{
			return;
		}
		double scale = anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
		double dx = Math.sin(heading), dy = Math.cos(heading);
		Point2D.Double edge = anchor.project(dx * outerRadius, dy * outerRadius);
		Point2D.Double radial = anchor.project(dx * (outerRadius + 1), dy * (outerRadius + 1));
		Point2D.Double tangent = anchor.project(dx * outerRadius + dy, dy * outerRadius - dx);
		if (!(scale > 0) || !Double.isFinite(scale) || edge == null || radial == null || tangent == null)
		{
			return;
		}
		// Stroke widths are screen pixels. Use the projected normal scale for their clearance,
		// so a low camera pitch does not collapse the stroke clearance with the ground gap.
		double tx = tangent.x - edge.x, ty = tangent.y - edge.y;
		double normalScale = Math.abs((radial.x - edge.x) * ty - (radial.y - edge.y) * tx) / Math.hypot(tx, ty);
		if (!Double.isFinite(normalScale) || normalScale < 1e-6)
		{
			return;
		}
		double strokeClearance = (thickness(config.directionalIndicatorLineThickness())
			+ (config.showOuterRing() ? thickness(config.outerThickness()) : 0)) / 2.0;
		double margin = Math.max(0, Math.min(128, config.directionalIndicatorMargin()));
		double start = outerRadius + margin / scale + strokeClearance / normalScale;
		double width = Math.max(1, Math.min(128, config.directionalIndicatorWidth())) / scale;
		double height = Math.max(1, Math.min(128, config.directionalIndicatorHeight())) / scale;
		double[][] vertices;
		switch (config.directionalIndicatorShape())
		{
			case ARROW: vertices = ARROW; break;
			case SQUARE: vertices = SQUARE; break;
			case CIRCLE: vertices = CIRCLE; break;
			default: vertices = TRIANGLE; break;
		}
		for (int i = 0; i < vertices.length; i++)
		{
			double across = vertices[i][0] * width;
			double outward = start + vertices[i][1] * height;
			Point2D.Double point = anchor.project(dx * outward + dy * across, dy * outward - dx * across);
			if (point == null)
			{
				path.reset();
				return;
			}
			if (i == 0)
			{
				path.moveTo(point.x, point.y);
			}
			else
			{
				path.lineTo(point.x, point.y);
			}
		}
		path.closePath();
	}

	private static int thickness(int value)
	{
		return Math.max(1, Math.min(12, value));
	}
}
