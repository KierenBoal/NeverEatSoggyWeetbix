package com.nevereatsoggyweetbix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class DirectionalIndicatorRenderingTest
{
	private static final class IndicatorOnlyConfig implements PlayerCompassConfig
	{
		boolean enabled = true;
		Color fill = new Color(255, 0, 0, 128);
		Color line = Color.WHITE;
		@Override public boolean directionalIndicatorEnabled() { return enabled; }
		@Override public DirectionalIndicatorShape directionalIndicatorShape() { return DirectionalIndicatorShape.SQUARE; }
		@Override public int directionalIndicatorLineThickness() { return 4; }
		@Override public Color directionalIndicatorFillColor() { return fill; }
		@Override public Color directionalIndicatorLineColor() { return line; }
		@Override public int overallOpacity() { return 50; }
		@Override public boolean showInnerRing() { return false; }
		@Override public boolean showOuterRing() { return false; }
		@Override public boolean hideCardinals() { return true; }
		@Override public boolean hideDiagonals() { return true; }
		@Override public boolean showCardinalConnectors() { return false; }
		@Override public boolean showDiagonalConnectors() { return false; }
	}

	private static CompassGeometry geometry(PlayerCompassConfig config)
	{
		CompassAnchor anchor = new CompassAnchor(new CompassProjection(new double[][]{
			{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 1000}
		}, 100, 100, 1000), 0, 0, 0, 0, 0);
		CompassGeometry geometry = new CompassGeometry();
		geometry.update(anchor, 10, 10);
		geometry.indicator.update(anchor, geometry.outerRadius, 0, config);
		return geometry;
	}

	private static BufferedImage render(PlayerCompassConfig config, CompassGeometry geometry, Rectangle viewport)
	{
		BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			PlayerCompassOverlay overlay = new PlayerCompassOverlay(null, config);
			graphics.clip(overlay.drawingBounds(viewport, geometry));
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
		config.fill = new Color(255, 0, 0, 0);
		image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		assertEquals(0, image.getRGB(100, 134) >>> 24);
		assertEquals(128, image.getRGB(83, 134) >>> 24);
		config.line = new Color(255, 255, 255, 0);
		config.fill = Color.RED;
		image = render(config, geometry, new Rectangle(0, 0, 200, 200));
		assertEquals(128, image.getRGB(100, 134) >>> 24);
		assertEquals(0, image.getRGB(83, 134) >>> 24);
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
		PlayerCompassOverlay overlay = new PlayerCompassOverlay(null, config);
		Rectangle bounds = overlay.drawingBounds(new Rectangle(0, 0, 200, 200), geometry);
		assertTrue(bounds.contains(82, 124));
		assertTrue(bounds.contains(118, 144));
		Rectangle viewport = new Rectangle(90, 130, 15, 10);
		assertEquals(viewport, overlay.drawingBounds(viewport, geometry));
		BufferedImage image = render(config, geometry, viewport);
		assertEquals(64, image.getRGB(100, 134) >>> 24);
		assertEquals(0, image.getRGB(89, 134));
		assertEquals(0, image.getRGB(105, 134));
		assertTrue(overlay.drawingBounds(new Rectangle(0, 0, 50, 50), geometry).isEmpty());
		config.enabled = false;
		assertTrue(overlay.drawingBounds(new Rectangle(0, 0, 200, 200), geometry).isEmpty());
	}
}
