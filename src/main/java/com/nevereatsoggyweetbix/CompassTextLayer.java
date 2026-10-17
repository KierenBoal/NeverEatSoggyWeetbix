package com.nevereatsoggyweetbix;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/** Isolated text RGBA layer: foreground coverage replaces shadows rather than blending with them. */
final class CompassTextLayer
{
	private BufferedImage layer;
	private BufferedImage coverage;

	void draw(Graphics2D target, Shape[] glyphs, Color[] colors, Color shadow, int offset)
	{
		Rectangle bounds = bounds(glyphs, offset, target.getClipBounds());
		if (bounds.isEmpty())
		{
			return;
		}
		ensureBuffers(bounds.width, bounds.height);
		int stride = layer.getWidth();
		int[] pixels = ((DataBufferInt) layer.getRaster().getDataBuffer()).getData();
		int[] mask = ((DataBufferInt) coverage.getRaster().getDataBuffer()).getData();
		for (int y = 0; y < bounds.height; y++)
		{
			Arrays.fill(pixels, y * stride, y * stride + bounds.width, 0);
		}
		Graphics2D shadows = layer.createGraphics();
		Graphics2D foreground = coverage.createGraphics();
		try
		{
			shadows.setRenderingHints(target.getRenderingHints());
			shadows.translate(-bounds.x, -bounds.y);
			shadows.clip(bounds);
			shadows.setColor(shadow);
			AffineTransform shift = AffineTransform.getTranslateInstance(offset, offset);
			for (Shape glyph : glyphs)
			{
				if (glyph != null)
				{
					shadows.fill(shift.createTransformedShape(glyph));
				}
			}
			foreground.setRenderingHints(target.getRenderingHints());
			foreground.translate(-bounds.x, -bounds.y);
			foreground.clip(bounds);
			foreground.setColor(Color.WHITE);
			foreground.setComposite(AlphaComposite.Src);
			for (int i = 0; i < glyphs.length; i++)
			{
				Shape glyph = glyphs[i];
				if (glyph == null)
				{
					continue;
				}
				Rectangle patch = glyph.getBounds();
				patch.grow(1, 1);
				patch = patch.intersection(bounds);
				if (patch.isEmpty())
				{
					continue;
				}
				int startX = patch.x - bounds.x, startY = patch.y - bounds.y;
				for (int y = startY; y < startY + patch.height; y++)
				{
					Arrays.fill(mask, y * stride + startX, y * stride + startX + patch.width, 0);
				}
				foreground.fill(glyph);
				Color color = colors[i];
				for (int y = startY; y < startY + patch.height; y++)
				{
					for (int x = startX; x < startX + patch.width; x++)
					{
						int index = y * stride + x;
						int amount = mask[index] >>> 24;
						if (amount != 0)
						{
							int alpha = (color.getAlpha() * amount + 127) / 255;
							pixels[index] = (alpha << 24) | (color.getRGB() & 0xffffff);
						}
					}
				}
			}
		}
		finally
		{
			shadows.dispose();
			foreground.dispose();
		}
		// Src is confined to our coverage buffer; the finished text blends normally with the scene.
		target.drawImage(layer, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
			0, 0, bounds.width, bounds.height, null);
	}

	static Rectangle bounds(Shape[] glyphs, int offset, Rectangle clip)
	{
		Rectangle2D result = null;
		for (Shape glyph : glyphs)
		{
			if (glyph != null)
			{
				Rectangle2D box = glyph.getBounds2D();
				Rectangle2D combined = box.createUnion(new Rectangle2D.Double(
					box.getX() + offset, box.getY() + offset, box.getWidth(), box.getHeight()));
				result = result == null ? combined : result.createUnion(combined);
			}
		}
		if (result == null)
		{
			return new Rectangle();
		}
		double minX = Math.floor(result.getMinX() - 1), minY = Math.floor(result.getMinY() - 1);
		double maxX = Math.ceil(result.getMaxX() + 1), maxY = Math.ceil(result.getMaxY() + 1);
		if (clip != null)
		{
			minX = Math.max(minX, clip.getMinX()); minY = Math.max(minY, clip.getMinY());
			maxX = Math.min(maxX, clip.getMaxX()); maxY = Math.min(maxY, clip.getMaxY());
		}
		return maxX > minX && maxY > minY
			? new Rectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY)) : new Rectangle();
	}

	private void ensureBuffers(int width, int height)
	{
		if (layer == null || layer.getWidth() < width || layer.getHeight() < height)
		{
			int nextWidth = layer == null ? width : Math.max(width, layer.getWidth());
			int nextHeight = layer == null ? height : Math.max(height, layer.getHeight());
			clear();
			layer = new BufferedImage(nextWidth, nextHeight, BufferedImage.TYPE_INT_ARGB);
			coverage = new BufferedImage(nextWidth, nextHeight, BufferedImage.TYPE_INT_ARGB);
		}
	}

	void clear()
	{
		if (layer != null)
		{
			layer.flush(); coverage.flush();
			layer = null; coverage = null;
		}
	}
}
