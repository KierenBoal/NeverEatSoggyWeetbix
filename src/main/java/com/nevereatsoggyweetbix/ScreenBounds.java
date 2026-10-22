package com.nevereatsoggyweetbix;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

final class ScreenBounds
{
	private ScreenBounds()
	{
	}

	/** Null means uncertain/near-plane crossing; an empty box is definitely invisible. */
	static Rectangle2D projectBox(CompassProjection projection, double minX, double maxX,
		double minY, double maxY, double minHeight, double maxHeight)
	{
		return projectBox(projection, minX, maxX, minY, maxY, minHeight, maxHeight,
			new Rectangle2D.Double(), new Point2D.Double());
	}

	/** The returned box aliases result; consume it before reusing the scratch objects. */
	static Rectangle2D projectBox(CompassProjection projection, double minX, double maxX,
		double minY, double maxY, double minHeight, double maxHeight,
		Rectangle2D.Double result, Point2D.Double point)
	{
		if (projection == null)
		{
			return null;
		}
		result.setRect(0, 0, 0, 0);
		int visible = 0;
		for (int corner = 0; corner < 8; corner++)
		{
			if (projection.project((corner & 1) == 0 ? minX : maxX,
				(corner & 2) == 0 ? minY : maxY, (corner & 4) == 0 ? minHeight : maxHeight, point))
			{
				visible++;
				if (visible == 1)
				{
					result.setRect(point.x, point.y, 0, 0);
				}
				else
				{
					result.add(point);
				}
			}
		}
		return visible == 0 || visible == 8 ? result : null;
	}
}
