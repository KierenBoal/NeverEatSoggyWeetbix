package com.nevereatsoggyweetbix;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
}
