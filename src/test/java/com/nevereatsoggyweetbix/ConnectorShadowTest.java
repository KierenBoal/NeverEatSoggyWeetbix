package com.nevereatsoggyweetbix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConnectorShadowTest
{
	private static class ConnectorConfig implements PlayerCompassConfig
	{
		boolean shadow = true;
		boolean cardinals = true;
		boolean diagonals = false;
		int offset = 16;
		@Override public boolean enableConnectorDropShadow() { return shadow; }
		@Override public int connectorDropShadowOffset() { return offset; }
		@Override public Color connectorDropShadowColor() { return new Color(0, 0, 0, 128); }
		@Override public boolean showCardinalConnectors() { return cardinals; }
		@Override public boolean showDiagonalConnectors() { return diagonals; }
		@Override public boolean connectorDirectionColors() { return false; }
		@Override public Color baseColor() { return new Color(255, 255, 255, 128); }
		@Override public int connectorThickness() { return 2; }
		@Override public int overallOpacity() { return 50; }
		@Override public boolean showInnerRing() { return false; }
		@Override public boolean showOuterRing() { return false; }
		@Override public boolean hideCardinals() { return true; }
		@Override public boolean hideDiagonals() { return true; }
		@Override public boolean directionalIndicatorEnabled() { return false; }
	}

	private static CompassGeometry geometry()
	{
		CompassGeometry geometry = new CompassGeometry();
		geometry.spokes[CompassDirection.N.ordinal()].moveTo(20, 20);
		geometry.spokes[CompassDirection.N.ordinal()].lineTo(50, 20);
		geometry.spokes[CompassDirection.NE.ordinal()].moveTo(20, 70);
		geometry.spokes[CompassDirection.NE.ordinal()].lineTo(50, 70);
		return geometry;
	}

	private static BufferedImage render(PlayerCompassConfig config, CompassGeometry geometry)
	{
		BufferedImage image = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			PlayerCompassOverlay overlay = new PlayerCompassOverlay(null, config);
			graphics.clip(overlay.drawingBounds(new Rectangle(0, 0, 240, 240), geometry));
			overlay.drawCompass(graphics, 0, geometry);
		}
		finally
		{
			graphics.dispose();
		}
		return image;
	}

	@Test
	public void shadowsRespectToggleVisibilityColorAndOpacity()
	{
		ConnectorConfig config = new ConnectorConfig();
		CompassGeometry geometry = geometry();
		BufferedImage image = render(config, geometry);
		assertEquals(new Color(255, 255, 255, 64).getRGB(), image.getRGB(30, 20));
		assertEquals(new Color(0, 0, 0, 64).getRGB(), image.getRGB(46, 36));
		assertEquals(0, image.getRGB(30, 70));
		assertEquals(0, image.getRGB(46, 86));
		config.shadow = false;
		image = render(config, geometry);
		assertEquals(64, image.getRGB(30, 20) >>> 24);
		assertEquals(0, image.getRGB(46, 36));
		config.shadow = true;
		config.cardinals = false;
		config.diagonals = true;
		image = render(config, geometry);
		assertEquals(0, image.getRGB(30, 20));
		assertEquals(0, image.getRGB(46, 36));
		assertEquals(64, image.getRGB(30, 70) >>> 24);
		assertEquals(64, image.getRGB(46, 86) >>> 24);
	}

	@Test
	public void allShadowsAreBehindAllForegroundConnectors()
	{
		ConnectorConfig config = new ConnectorConfig()
		{
			@Override public int overallOpacity() { return 100; }
			@Override public Color baseColor() { return Color.WHITE; }
			@Override public Color connectorDropShadowColor() { return Color.BLACK; }
		};
		CompassGeometry geometry = geometry();
		// The later E shadow crosses N's foreground at (40, 20).
		geometry.spokes[CompassDirection.E.ordinal()].moveTo(24, 0);
		geometry.spokes[CompassDirection.E.ordinal()].lineTo(24, 40);
		assertEquals(Color.WHITE.getRGB(), render(config, geometry).getRGB(40, 20));
	}

	@Test
	public void boundsAndRenderingIncludeClampedLargeOffsetsWithoutRingsOrLabels()
	{
		ConnectorConfig config = new ConnectorConfig();
		config.offset = 500;
		CompassGeometry geometry = geometry();
		PlayerCompassOverlay overlay = new PlayerCompassOverlay(null, config);
		Rectangle bounds = overlay.drawingBounds(new Rectangle(0, 0, 240, 240), geometry);
		assertTrue(bounds.contains(178, 149));
		assertEquals(64, render(config, geometry).getRGB(158, 148) >>> 24);
		assertEquals(new Rectangle(150, 145, 20, 10),
			overlay.drawingBounds(new Rectangle(150, 145, 20, 10), geometry));
		config.shadow = false;
		assertTrue(overlay.drawingBounds(new Rectangle(150, 145, 20, 10), geometry).isEmpty());
		config.shadow = true;
		config.offset = -10;
		BufferedImage image = render(config, geometry);
		assertEquals(0, image.getRGB(46, 36));
		assertTrue((image.getRGB(30, 20) >>> 24) > 64);
	}
}
