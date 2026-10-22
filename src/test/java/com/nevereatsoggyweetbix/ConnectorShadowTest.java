package com.nevereatsoggyweetbix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConnectorShadowTest
{
	private static class ConnectorConfig implements NeverEatSoggyWeetbixConfig
	{
		boolean shadow = true;
		boolean cardinals = true;
		boolean diagonals = false;
		boolean onlyCurrent;
		boolean onlyOpposite;
		int offset = 16;
		@Override public boolean enableDropShadow() { return shadow; }
		@Override public int dropShadowOffset() { return offset; }
		@Override public Color dropShadowColor() { return new Color(0, 0, 0, 128); }
		@Override public boolean showCardinalConnectors() { return cardinals; }
		@Override public boolean showDiagonalConnectors() { return diagonals; }
		@Override public boolean onlyCurrentDirectionConnectors() { return onlyCurrent; }
		@Override public boolean onlyOppositeDirectionConnectors() { return onlyOpposite; }
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
		geometry.spokes[CompassDirection.S.ordinal()].moveTo(20, 120);
		geometry.spokes[CompassDirection.S.ordinal()].lineTo(50, 120);
		return geometry;
	}

	private static BufferedImage render(NeverEatSoggyWeetbixConfig config, CompassGeometry geometry)
	{
		BufferedImage image = new BufferedImage(240, 240, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			NeverEatSoggyWeetbixOverlay overlay = new NeverEatSoggyWeetbixOverlay(null, config);
			graphics.clip(overlay.drawingBounds(new Rectangle(0, 0, 240, 240), geometry, 0));
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
		config.cardinals = true;
		config.onlyCurrent = true;
		image = render(config, geometry);
		assertEquals(64, image.getRGB(30, 20) >>> 24);
		assertEquals(64, image.getRGB(30, 70) >>> 24);
		assertEquals(0, image.getRGB(30, 120));
		assertEquals(0, image.getRGB(46, 136));
		NeverEatSoggyWeetbixOverlay overlay = new NeverEatSoggyWeetbixOverlay(null, config);
		assertTrue(overlay.drawingBounds(new Rectangle(18, 115, 35, 5), geometry, 0).isEmpty());
		config.onlyCurrent = false;
		config.onlyOpposite = true;
		image = render(config, geometry);
		assertEquals(0, image.getRGB(30, 20));
		assertEquals(0, image.getRGB(46, 36));
		assertEquals(0, image.getRGB(30, 70));
		assertEquals(64, image.getRGB(30, 120) >>> 24);
		assertEquals(64, image.getRGB(46, 136) >>> 24);
		config.onlyCurrent = true;
		image = render(config, geometry);
		assertEquals(64, image.getRGB(30, 20) >>> 24);
		assertEquals(64, image.getRGB(30, 120) >>> 24);
	}
}
