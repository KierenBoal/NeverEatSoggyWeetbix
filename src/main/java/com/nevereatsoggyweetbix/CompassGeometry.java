package com.nevereatsoggyweetbix;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Arrays;

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
		inner.reset();
		outer.reset();
		Arrays.fill(labels, null);
		Arrays.fill(rotations, 0);
		for (Path2D.Double spoke : spokes)
		{
			spoke.reset();
		}
		Point2D.Double center = anchor.project(0, 0);
		double pixelScale = anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
		double worldGap = bandWidth / pixelScale;
		if (center == null || !Double.isFinite(radius) || radius < 0
			|| !Double.isFinite(worldGap) || worldGap < 0)
		{
			return;
		}
		// One world-space radius for each ring: no per-point screen-space offsets.
		double outerRadius = radius + worldGap;
		double labelRadius = radius + worldGap / 2;
		boolean innerPenDown = false;
		boolean outerPenDown = false;
		for (int i = 0; i <= SAMPLES; i++)
		{
			Point2D.Double point = anchor.project(CIRCLE_X[i] * radius, CIRCLE_Y[i] * radius);
			Point2D.Double outside = anchor.project(CIRCLE_X[i] * outerRadius, CIRCLE_Y[i] * outerRadius);
			if (radius > 0)
			{
				innerPenDown = append(inner, point, innerPenDown);
			}
			outerPenDown = append(outer, outside, outerPenDown);
		}
		for (CompassDirection direction : CompassDirection.ALL)
		{
			int index = direction.ordinal();
			Point2D.Double label = anchor.project(direction.x * labelRadius, direction.y * labelRadius);
			labels[index] = label;
			rotations[index] = label == null ? 0 : Math.atan2(label.y - center.y, label.x - center.x) + Math.PI / 2;
			Path2D.Double spoke = spokes[index];
			Point2D.Double start = anchor.project(direction.x * radius * 0.35, direction.y * radius * 0.35);
			Point2D.Double end = anchor.project(direction.x * radius * 0.85, direction.y * radius * 0.85);
			if (radius > 0 && start != null && end != null)
			{
				spoke.moveTo(start.x, start.y);
				spoke.lineTo(end.x, end.y);
			}
		}
	}

	private static boolean append(Path2D.Double path, Point2D.Double point, boolean penDown)
	{
		if (point == null)
		{
			return false;
		}
		if (penDown)
		{
			path.lineTo(point.x, point.y);
		}
		else
		{
			path.moveTo(point.x, point.y);
		}
		return true;
	}
}
