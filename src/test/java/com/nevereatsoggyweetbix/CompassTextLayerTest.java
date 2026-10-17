package com.nevereatsoggyweetbix;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompassTextLayerTest
{
	private static BufferedImage render(Shape glyph, Color color, int offset, boolean aa)
	{
		BufferedImage image = new BufferedImage(180, 180, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.setClip(0, 0, 180, 180);
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				aa ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
			new CompassTextLayer().draw(graphics, new Shape[]{glyph}, new Color[]{color}, new Color(0, 0, 0, 160), offset);
		}
		finally
		{
			graphics.dispose();
		}
		return image;
	}

	@Test
	public void translucentForegroundReplacesShadowInsteadOfDarkeningOrIncreasingAlpha()
	{
		Color color = new Color(40, 180, 240, 96);
		BufferedImage image = render(new Rectangle(10, 10, 20, 20), color, 1, false);
		Color foreground = new Color(image.getRGB(15, 15), true);
		assertEquals(96, foreground.getAlpha());
		assertEquals(color.getRed(), foreground.getRed(), 2);
		assertEquals(color.getGreen(), foreground.getGreen(), 2);
		assertEquals(color.getBlue(), foreground.getBlue(), 2);
		assertEquals(160, image.getRGB(30, 20) >>> 24);
	}

	@Test
	public void antialiasedEdgesContainOnlyForegroundRgbAndCoverageAlpha()
	{
		Shape glyph = new Ellipse2D.Double(10.25, 10.25, 25.5, 25.5);
		Color color = new Color(80, 180, 240, 128);
		BufferedImage actual = render(glyph, color, 1, true);
		BufferedImage coverage = new BufferedImage(180, 180, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = coverage.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(Color.WHITE);
		graphics.fill(glyph);
		graphics.dispose();
		int edges = 0;
		for (int y = 0; y < 60; y++)
		{
			for (int x = 0; x < 60; x++)
			{
				int amount = coverage.getRGB(x, y) >>> 24;
				if (amount > 0)
				{
					Color pixel = new Color(actual.getRGB(x, y), true);
					assertEquals((128 * amount + 127) / 255, pixel.getAlpha());
					if (pixel.getAlpha() >= 20)
					{
						assertEquals(color.getRed(), pixel.getRed(), 6);
						assertEquals(color.getGreen(), pixel.getGreen(), 6);
						assertEquals(color.getBlue(), pixel.getBlue(), 6);
					}
					if (amount < 255)
					{
						edges++;
					}
				}
			}
		}
		assertTrue(edges > 10);
	}

	@Test
	public void zeroOffsetDoesNotLeaveShadowUnderTransparentText()
	{
		Color color = new Color(60, 180, 240, 64);
		BufferedImage image = render(new Rectangle(10, 10, 20, 20), color, 0, false);
		assertEquals(64, image.getRGB(15, 15) >>> 24);
		assertEquals(0, image.getRGB(30, 20) >>> 24);
		BufferedImage invisible = render(new Rectangle(10, 10, 20, 20), new Color(0, 0, 0, 0), 0, false);
		assertEquals(0, invisible.getRGB(15, 15) >>> 24);
	}

	@Test
	public void maximumOffsetAndRotatedGlyphStayInScreenCoordinates()
	{
		Shape glyph = AffineTransform.getRotateInstance(Math.PI / 4, 20, 20)
			.createTransformedShape(new Rectangle(15, 15, 10, 10));
		BufferedImage image = render(glyph, Color.WHITE, 128, false);
		assertEquals(Color.WHITE.getRGB(), image.getRGB(20, 20));
		assertEquals(160, image.getRGB(148, 148) >>> 24);
		assertEquals(0, image.getRGB(148, 20) >>> 24);
		Rectangle bounds = CompassTextLayer.bounds(new Shape[]{glyph}, 128, new Rectangle(0, 0, 100, 100));
		assertTrue(bounds.getMaxX() <= 100);
		assertTrue(bounds.getMaxY() <= 100);
	}

	@Test
	public void finishedLayerPreservesTheSceneAndCanBeReusedWithoutStalePixels()
	{
		BufferedImage image = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setClip(0, 0, 80, 80);
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 80, 80);
		CompassTextLayer layer = new CompassTextLayer();
		layer.draw(graphics, new Shape[]{new Rectangle(10, 10, 40, 40)}, new Color[]{new Color(255, 0, 0, 128)}, Color.BLACK, 1);
		assertEquals(255, image.getRGB(20, 20) >>> 24);
		assertEquals(Color.WHITE.getRGB(), image.getRGB(5, 5));
		graphics.setComposite(AlphaComposite.Clear);
		graphics.fillRect(0, 0, 80, 80);
		graphics.setComposite(AlphaComposite.SrcOver);
		layer.draw(graphics, new Shape[]{new Rectangle(10, 10, 10, 10)}, new Color[]{Color.WHITE}, Color.BLACK, 1);
		assertEquals(0, image.getRGB(40, 40) >>> 24);
		assertEquals(Color.WHITE.getRGB(), image.getRGB(15, 15));
		layer.clear();
		graphics.dispose();
	}

	@Test
	public void shadowBoundsIncludeGlyphsOutsideViewportWhoseShadowsAreVisible()
	{
		Rectangle bounds = CompassTextLayer.bounds(new Shape[]{new Rectangle2D.Double(-20, -20, 10, 10)},
			32, new Rectangle(0, 0, 100, 100));
		assertTrue(bounds.intersects(12, 12, 10, 10));
		assertEquals(0, bounds.x);
		assertEquals(0, bounds.y);
	}
}
