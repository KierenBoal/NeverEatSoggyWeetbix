package com.nevereatsoggyweetbix;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import net.runelite.api.AABB;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;

/** Removes selected actor silhouettes from the compass buffer. */
final class ActorCompassMask
{
	private int[] vertexX = new int[0];
	private int[] vertexY = new int[0];
	private final int[] triangleX = new int[3];
	private final int[] triangleY = new int[3];
	private final Rectangle2D.Double projectedBounds = new Rectangle2D.Double();
	private final Point2D.Double projectedPoint = new Point2D.Double();

	void erase(Graphics2D graphics, Client client, Actor actor, Model model, Rectangle bounds, CompassProjection projection)
	{
		if (model == null || model.getVerticesCount() == 0)
		{
			return;
		}
		LocalPoint location = actor.getLocalLocation();
		if (location == null || actor.getWorldView() == null || projection == null)
		{
			return;
		}
		Integer ground = CompassAnchor.groundHeight(client, actor.getWorldView(), location);
		if (ground == null)
		{
			return;
		}
		int height = ground - actor.getAnimationHeightOffset();
		AABB box = model.getAABB(actor.getCurrentOrientation());
		if (box != null)
		{
			Rectangle2D projected = ScreenBounds.projectBox(projection,
				location.getX() + box.getCenterX() - box.getExtremeX(),
				location.getX() + box.getCenterX() + box.getExtremeX(),
				location.getY() + box.getCenterZ() - box.getExtremeZ(),
				location.getY() + box.getCenterZ() + box.getExtremeZ(),
				height + box.getCenterY() - box.getExtremeY(), height + box.getCenterY() + box.getExtremeY(),
				projectedBounds, projectedPoint);
			if (projected != null && !projected.intersects(bounds))
			{
				return;
			}
		}
		int count = model.getVerticesCount();
		if (vertexX.length < count)
		{
			vertexX = new int[count];
			vertexY = new int[count];
		}
		Arrays.fill(vertexX, 0, count, Integer.MIN_VALUE);
		Arrays.fill(vertexY, 0, count, Integer.MIN_VALUE);
		Perspective.modelToCanvas(client, actor.getWorldView(), count,
			location.getX(), location.getY(), height, actor.getCurrentOrientation(),
			model.getVerticesX(), model.getVerticesZ(), model.getVerticesY(), vertexX, vertexY);
		eraseFaces(graphics, bounds, vertexX, vertexY, model.getFaceCount(),
			model.getFaceIndices1(), model.getFaceIndices2(), model.getFaceIndices3(),
			model.getFaceColors3(), model.getFaceTransparencies());
	}

	void eraseFaces(Graphics2D graphics, Rectangle bounds, int[] x, int[] y, int faceCount,
		int[] a, int[] b, int[] c, int[] colors3, byte[] transparencies)
	{
		Graphics2D mask = (Graphics2D) graphics.create();
		try
		{
			mask.setComposite(AlphaComposite.Clear);
			// Opaque triangle coverage prevents shared-edge seams in the mask.
			mask.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			for (int face = 0; face < faceCount; face++)
			{
				if ((colors3 != null && colors3[face] == -2)
					|| (transparencies != null && (transparencies[face] & 0xff) == 255))
				{
					continue;
				}
				int first = a[face], second = b[face], third = c[face];
				if (!valid(x, y, first) || !valid(x, y, second) || !valid(x, y, third))
				{
					continue;
				}
				// RuneLite draws negative screen-space winding; back faces cannot cover the compass.
				long area = ((long) x[second] - x[first]) * ((long) y[third] - y[first])
					- ((long) y[second] - y[first]) * ((long) x[third] - x[first]);
				if (area >= 0)
				{
					continue;
				}
				int minX = Math.min(x[first], Math.min(x[second], x[third]));
				int minY = Math.min(y[first], Math.min(y[second], y[third]));
				int maxX = Math.max(x[first], Math.max(x[second], x[third]));
				int maxY = Math.max(y[first], Math.max(y[second], y[third]));
				if (maxX < bounds.x || maxY < bounds.y || minX >= bounds.getMaxX() || minY >= bounds.getMaxY())
				{
					continue;
				}
				triangleX[0] = x[first]; triangleY[0] = y[first];
				triangleX[1] = x[second]; triangleY[1] = y[second];
				triangleX[2] = x[third]; triangleY[2] = y[third];
				mask.fillPolygon(triangleX, triangleY, 3);
			}
		}
		finally
		{
			mask.dispose();
		}
	}

	private static boolean valid(int[] x, int[] y, int index)
	{
		return index >= 0 && index < x.length && index < y.length
			&& x[index] != Integer.MIN_VALUE && y[index] != Integer.MIN_VALUE;
	}

	void clear()
	{
		vertexX = new int[0];
		vertexY = new int[0];
	}
}
