package com.nevereatsoggyweetbix;

import java.awt.geom.AffineTransform;
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
	final DirectionalIndicatorGeometry indicator = new DirectionalIndicatorGeometry();
	final DirectionalIndicatorGeometry oppositeIndicator = new DirectionalIndicatorGeometry();
	final Path2D.Double[] spokes = new Path2D.Double[8];
	final Point2D.Double[] labels = new Point2D.Double[8];
	final double[] rotations = new double[8];
	double outerRadius = Double.NaN;
	private final Point2D.Double[] labelRing = new Point2D.Double[SAMPLES];
	private final double[] labelLengths = new double[SAMPLES];
	private CompassAnchor labelAnchor;
	private double labelRadius;
	private boolean labelRingReady;

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
		indicator.path.reset();
		oppositeIndicator.path.reset();
		outerRadius = Double.NaN;
		labelAnchor = null;
		labelRingReady = false;
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
		outerRadius = radius + worldGap;
		labelRadius = radius + worldGap / 2;
		labelAnchor = anchor;
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

	void updateCurvedLabels(CompassAnchor anchor, double minimumRadius, double labelClearance, double bandWidth)
	{
		double scale = anchor.projection.horizontalScaleAt(anchor.x, anchor.y, anchor.height);
		// Preserve the circle used to lay out the words; their length is not radial padding.
		double centerRadius = minimumRadius + labelClearance / (2 * scale);
		double gap = bandWidth / scale;
		for (int iteration = 0; iteration < 3; iteration++)
		{
			double normalScale = scale;
			for (int sample = 0; sample < SAMPLES; sample += 8)
			{
				double dx = CIRCLE_X[sample], dy = CIRCLE_Y[sample];
				Point2D.Double point = anchor.project(dx * centerRadius, dy * centerRadius);
				Point2D.Double radial = anchor.project(dx * (centerRadius + 1), dy * (centerRadius + 1));
				Point2D.Double tangent = anchor.project(dx * centerRadius + dy, dy * centerRadius - dx);
				if (point == null || radial == null || tangent == null)
				{
					continue;
				}
				double tx = tangent.x - point.x, ty = tangent.y - point.y;
				double normal = Math.abs((radial.x - point.x) * ty - (radial.y - point.y) * tx) / Math.hypot(tx, ty);
				if (Double.isFinite(normal) && normal > 1e-6)
				{
					normalScale = Math.min(normalScale, normal);
				}
			}
			// Text keeps its screen height, so account for the floor's foreshortening.
			gap = bandWidth / normalScale;
			double requiredCenter = minimumRadius + gap / 2;
			if (centerRadius >= requiredCenter)
			{
				break;
			}
			centerRadius = requiredCenter;
		}
		update(anchor, Math.max(minimumRadius, centerRadius - gap / 2), gap * scale);
	}

	boolean prepareLabelRing()
	{
		if (labelAnchor == null || !(labelRadius > 0))
		{
			return false;
		}
		if (!labelRingReady)
		{
			for (int i = 0; i < SAMPLES; i++)
			{
				labelRing[i] = labelAnchor.project(CIRCLE_X[i] * labelRadius, CIRCLE_Y[i] * labelRadius);
			}
			for (int i = 0; i < SAMPLES; i++)
			{
				Point2D.Double point = labelRing[i], next = labelRing[(i + 1) % SAMPLES];
				labelLengths[i] = point == null || next == null ? Double.NaN : point.distance(next);
			}
			labelRingReady = true;
		}
		return true;
	}

	/** Place a character by screen arc length, with its baseline tangent to the projected ring. */
	boolean labelTransform(CompassDirection direction, double offset, AffineTransform transform)
	{
		if (!labelRingReady || !Double.isFinite(offset))
		{
			return false;
		}
		int index = direction.ordinal() * SAMPLES / CompassDirection.ALL.length;
		int step = offset < 0 ? -1 : 1;
		double remaining = Math.abs(offset);
		for (int segment = 0; segment < SAMPLES; segment++)
		{
			int next = Math.floorMod(index + step, SAMPLES);
			int start = step > 0 ? index : next;
			double length = labelLengths[start];
			if (!Double.isFinite(length))
			{
				return false;
			}
			if (length > 1e-9 && remaining <= length)
			{
				Point2D.Double a = labelRing[start], b = labelRing[(start + 1) % SAMPLES];
				double fraction = step > 0 ? remaining / length : 1 - remaining / length;
				double dx = b.x - a.x, dy = b.y - a.y;
				Point2D.Double before = labelRing[Math.floorMod(start - 1, SAMPLES)];
				Point2D.Double after = labelRing[(start + 2) % SAMPLES];
				if (before != null && after != null)
				{
					// Interpolate tangents to avoid visible angle changes at sample boundaries.
					dx = (1 - fraction) * (b.x - before.x) + fraction * (after.x - a.x);
					dy = (1 - fraction) * (b.y - before.y) + fraction * (after.y - a.y);
				}
				transform.setToTranslation(a.x + (b.x - a.x) * fraction, a.y + (b.y - a.y) * fraction);
				transform.rotate(Math.atan2(dy, dx));
				return true;
			}
			remaining -= length;
			index = next;
		}
		return false;
	}

	void updateIndicators(CompassAnchor anchor, double heading, NeverEatSoggyWeetbixConfig config)
	{
		indicator.path.reset();
		oppositeIndicator.path.reset();
		double indicatorHeading = DirectionalIndicatorGeometry.rotationHeading(heading, config);
		if (config.directionalIndicatorEnabled())
		{
			indicator.update(anchor, outerRadius, indicatorHeading, config);
		}
		if (config.oppositeIndicator())
		{
			// Keep an exact opposite even with odd rotation step counts.
			oppositeIndicator.update(anchor, outerRadius, indicatorHeading + Math.PI, config);
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
