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
		if (projection == null)
		{
			return null;
		}
		Rectangle2D result = null;
		int visible = 0;
		for (int corner = 0; corner < 8; corner++)
		{
			Point2D.Double point = projection.project((corner & 1) == 0 ? minX : maxX,
				(corner & 2) == 0 ? minY : maxY, (corner & 4) == 0 ? minHeight : maxHeight);
			if (point != null)
			{
				visible++;
				if (result == null)
				{
					result = new Rectangle2D.Double(point.x, point.y, 0, 0);
				}
				else
				{
					result.add(point);
				}
			}
		}
		return visible == 0 ? new Rectangle2D.Double() : visible == 8 ? result : null;
	}
}
