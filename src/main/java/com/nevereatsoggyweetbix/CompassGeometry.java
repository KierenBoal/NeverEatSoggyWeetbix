package com.nevereatsoggyweetbix;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

final class CompassGeometry
{
	static final int SAMPLES = 256;
	private static final double[] CIRCLE_X = new double[SAMPLES + 1];
	private static final double[] CIRCLE_Y = new double[SAMPLES + 1];

	static
	{
		for (int i = 0; i <= SAMPLES; i++)
		{
			CIRCLE_X[i] = Math.sin((i % SAMPLES) * Math.PI * 2 / SAMPLES);
			CIRCLE_Y[i] = Math.cos((i % SAMPLES) * Math.PI * 2 / SAMPLES);
		}
	}

	final Path2D.Double inner = new Path2D.Double();
	final Path2D.Double outer = new Path2D.Double();
	final Path2D.Double[] spokes = new Path2D.Double[8];
	final Point2D.Double[] labels = new Point2D.Double[8];
	final double[] rotations = new double[8];

	CompassGeometry()
	{
		for (int i = 0; i < spokes.length; i++)
		{
			spokes[i] = new Path2D.Double();
		}
	}

	void update(CompassAnchor anchor, double radius, double bandWidth)
	{
		Point2D.Double center = anchor.project(0, 0);
		inner.reset();
		outer.reset();
		boolean penDown = false;
		for (int i = 0; i <= SAMPLES; i++)
		{
			Point2D.Double point = anchor.project(CIRCLE_X[i] * radius, CIRCLE_Y[i] * radius);
			Point2D.Double outside = expand(center, point, bandWidth);
			if (outside == null)
			{
				penDown = false;
				continue;
			}
			append(inner, point, penDown);
			append(outer, outside, penDown);
			penDown = true;
		}
		for (CompassDirection direction : CompassDirection.ALL)
		{
			int index = direction.ordinal();
			Point2D.Double edge = anchor.project(direction.x * radius, direction.y * radius);
			labels[index] = expand(center, edge, bandWidth / 2);
			rotations[index] = edge == null ? 0 : Math.atan2(edge.y - center.y, edge.x - center.x) + Math.PI / 2;
			Path2D.Double spoke = spokes[index];
			spoke.reset();
			Point2D.Double start = anchor.project(direction.x * radius * 0.35, direction.y * radius * 0.35);
			Point2D.Double end = anchor.project(direction.x * radius * 0.85, direction.y * radius * 0.85);
			if (start != null && end != null)
			{
				spoke.moveTo(start.x, start.y);
				spoke.lineTo(end.x, end.y);
			}
		}
	}

	private static void append(Path2D.Double path, Point2D.Double point, boolean penDown)
	{
		if (penDown)
		{
			path.lineTo(point.x, point.y);
		}
		else
		{
			path.moveTo(point.x, point.y);
		}
	}

	static Point2D.Double expand(Point2D.Double center, Point2D.Double point, double pixels)
	{
		if (center == null || point == null)
		{
			return null;
		}
		double dx = point.x - center.x, dy = point.y - center.y;
		double length = Math.hypot(dx, dy);
		if (length < 1e-6)
		{
			return null;
		}
		return new Point2D.Double(point.x + dx / length * pixels, point.y + dy / length * pixels);
	}
}
