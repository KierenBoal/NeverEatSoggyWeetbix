package com.nevereatsoggyweetbix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class DirectionalIndicatorRenderingTest
{
	private static final class IndicatorOnlyConfig implements NeverEatSoggyWeetbixConfig
	{
		boolean enabled = true;
		boolean opposite;
		Color fill = new Color(255, 0, 0, 128);
		Color line = Color.WHITE;
		boolean shadow;
		int shadowOffset = 16;
		@Override public boolean directionalIndicatorEnabled() { return enabled; }
		@Override public boolean oppositeIndicator() { return opposite; }
		@Override public DirectionalIndicatorShape directionalIndicatorShape() { return DirectionalIndicatorShape.SQUARE; }
		@Override public int directionalIndicatorLineThickness() { return 4; }
		@Override public Color directionalIndicatorFillColor() { return fill; }
		@Override public Color directionalIndicatorLineColor() { return line; }
		@Override public boolean enableDropShadow() { return shadow; }
		@Override public int dropShadowOffset() { return shadowOffset; }
		@Override public Color dropShadowColor() { return new Color(0, 0, 0, 128); }
		@Override public int overallOpacity() { return 50; }
		@Override public boolean showInnerRing() { return false; }
		@Override public boolean showOuterRing() { return false; }
		@Override public boolean hideCardinals() { return true; }
		@Override public boolean hideDiagonals() { return true; }
		@Override public boolean showCardinalConnectors() { return false; }
		@Override public boolean showDiagonalConnectors() { return false; }
	}

	private static CompassGeometry geometry(NeverEatSoggyWeetbixConfig config)
	{
		CompassAnchor anchor = new CompassAnchor(new CompassProjection(new double[][]{
			{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 1000}
		}, 100, 100, 1000), 0, 0, 0, 0, 0);
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(anchor, 10, 10);
		geometry.updateIndicators(anchor, 0, config);
		return geometry;
	}

	private static BufferedImage render(NeverEatSoggyWeetbixConfig config, CompassGeometry geometry, Rectangle viewport)
	{
		BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			NeverEatSoggyWeetbixOverlay overlay = new NeverEatSoggyWeetbixOverlay(null, config);
			graphics.clip(overlay.drawingBounds(viewport, geometry, 0));
			overlay.drawCompass(graphics, 0, geometry);
		}
		finally
		{
			graphics.dispose();
		}
		return image;
	}

	@Test
	public void indicatorOnlyRenderingAppliesIndependentFillAndLineAlphaAndOverallOpacity()
	{
		IndicatorOnlyConfig config = new IndicatorOnlyConfig();
		CompassGeometry geometry = geometry(config);
		BufferedImage image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		assertEquals(64, image.getRGB(100, 134) >>> 24);
		assertEquals(128, image.getRGB(83, 134) >>> 24);
		assertEquals(0x00ff0000, image.getRGB(100, 134) & 0x00ffffff);
		assertEquals(0, image.getRGB(130, 166));
		config.shadow = true;
		image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		assertEquals(new Color(0, 0, 0, 64).getRGB(), image.getRGB(130, 166));
		assertEquals("Shadow must not change translucent foreground", new Color(255, 0, 0, 64).getRGB(), image.getRGB(110, 150));
		config.fill = new Color(255, 0, 0, 0);
		image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		assertEquals(0, image.getRGB(100, 134) >>> 24);
		assertEquals(128, image.getRGB(83, 134) >>> 24);
		assertEquals("Outline-only shadows must leave their centers clear", 0, image.getRGB(110, 166));
		assertEquals(64, image.getRGB(132, 150) >>> 24);
		config.line = new Color(255, 255, 255, 0);
		config.fill = Color.RED;
		image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		assertEquals(128, image.getRGB(100, 134) >>> 24);
		assertEquals(0, image.getRGB(83, 134) >>> 24);
		config.fill = new Color(255, 0, 0, 0);
		image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		assertEquals("Invisible indicators must not leave a shadow", 0, image.getRGB(130, 166));
		config.enabled = false;
		image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		for (int y = 0; y < 200; y++)
		{
			for (int x = 0; x < 200; x++)
			{
				assertEquals(0, image.getRGB(x, y));
			}
		}
	}

	@Test
	public void boundsIncludeStrokeBeyondHiddenRingAndClipToViewport()
	{
		IndicatorOnlyConfig config = new IndicatorOnlyConfig();
		CompassGeometry geometry = geometry(config);
		NeverEatSoggyWeetbixOverlay overlay = new NeverEatSoggyWeetbixOverlay(null, config);
		Rectangle bounds = overlay.drawingBounds(new Rectangle(0, 0, 200, 200), geometry, 0);
		assertTrue(bounds.contains(82, 124));
		assertTrue(bounds.contains(118, 144));
		Rectangle viewport = new Rectangle(90, 130, 15, 10);
		assertEquals(viewport, overlay.drawingBounds(viewport, geometry, 0));
		BufferedImage image = render(config, geometry, viewport);
		assertEquals(64, image.getRGB(100, 134) >>> 24);
		assertEquals(0, image.getRGB(89, 134));
		assertEquals(0, image.getRGB(105, 134));
		assertTrue(overlay.drawingBounds(new Rectangle(0, 0, 50, 50), geometry, 0).isEmpty());
		config.opposite = true;
		geometry = geometry(config);
		assertTrue(overlay.drawingBounds(new Rectangle(90, 60, 20, 10), geometry, 0).contains(100, 66));
		Color opposite = new Color(render(config, geometry, new Rectangle(0, 0, 200, 200)).getRGB(100, 66), true);
		assertEquals(128, opposite.getAlpha());
		assertEquals(230, opposite.getRed(), 1);
		assertEquals(159, opposite.getGreen(), 1);
		assertEquals(0, opposite.getBlue());
		config.opposite = false;
		assertEquals(0, render(config, geometry, new Rectangle(0, 0, 200, 200)).getRGB(100, 66));
		config.opposite = true;
		config.shadow = true;
		config.shadowOffset = 64;
		Rectangle shadowViewport = new Rectangle(150, 190, 30, 10);
		assertEquals(shadowViewport, overlay.drawingBounds(shadowViewport, geometry, 0));
		assertEquals(64, render(config, geometry, shadowViewport).getRGB(164, 198) >>> 24);
		config.enabled = false;
		geometry = geometry(config);
		image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		assertEquals(0, image.getRGB(100, 134));
		assertEquals(0, image.getRGB(164, 198));
		assertEquals(128, image.getRGB(100, 66) >>> 24);
		assertEquals(64, image.getRGB(164, 130) >>> 24);
		config.opposite = false;
		assertTrue(overlay.drawingBounds(new Rectangle(0, 0, 200, 200), geometry, 0).isEmpty());
	}
}
