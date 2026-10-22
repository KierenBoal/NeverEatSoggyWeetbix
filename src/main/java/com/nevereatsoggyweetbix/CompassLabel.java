package com.nevereatsoggyweetbix;

import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** Font outlines cached until the text or font changes; placement follows the current camera. */
final class CompassLabel
{
	final Rectangle2D bounds;
	private final Shape outline;
	private final Shape[] glyphs;
	private final double[] offsets;

	CompassLabel(GlyphVector vector)
	{
		outline = vector.getOutline();
		bounds = outline.getBounds2D();
		glyphs = new Shape[vector.getNumGlyphs()];
		offsets = new double[glyphs.length];
		for (int i = 0; i < glyphs.length; i++)
		{
			Rectangle2D glyphBounds = vector.getGlyphVisualBounds(i).getBounds2D();
			if (!glyphBounds.isEmpty())
			{
				offsets[i] = glyphBounds.getCenterX() - bounds.getCenterX();
				glyphs[i] = AffineTransform.getTranslateInstance(-glyphBounds.getCenterX(), -bounds.getCenterY())
					.createTransformedShape(vector.getGlyphOutline(i));
			}
		}
	}

	Shape straight(Point2D.Double position, double rotation)
	{
		AffineTransform transform = AffineTransform.getTranslateInstance(position.x, position.y);
		transform.rotate(rotation);
		transform.translate(-bounds.getCenterX(), -bounds.getCenterY());
		return transform.createTransformedShape(outline);
	}

	Shape curved(CompassGeometry geometry, CompassDirection direction)
	{
		Path2D.Double word = new Path2D.Double();
		AffineTransform transform = new AffineTransform();
		for (int i = 0; i < glyphs.length; i++)
		{
			if (glyphs[i] == null)
			{
				continue;
			}
			if (!geometry.labelTransform(direction, offsets[i], transform))
			{
				// Do not bridge a clipped section of the ring or wrap a word around a full turn.
				return null;
			}
			word.append(glyphs[i].getPathIterator(transform), false);
		}
		return word;
	}
}
