package com.nevereatsoggyweetbix;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.runelite.client.config.FontType;
import org.junit.Test;
import static org.junit.Assert.*;

public class NeverEatSoggyWeetbixDrawOrderTest
{
	@Test
	public void sharedShadowSwitchAndOpacityApplyToAllCompassParts()
	{
		for (boolean enabled : new boolean[]{false, true})
		{
			NeverEatSoggyWeetbixConfig config = new NeverEatSoggyWeetbixConfig()
			{
				@Override public boolean useCustomFont() { return true; }
				@Override public boolean onlyCurrentDirectionConnectors() { return false; }
				@Override public FontType customFont() { return new FontType().withFont(new Font(Font.SANS_SERIF, Font.BOLD, 24)); }
				@Override public int overallOpacity() { return 50; }
				@Override public Color currentColor() { return new Color(86, 180, 233, 128); }
				@Override public boolean enableDropShadow() { return enabled; }
				@Override public int dropShadowOffset() { return 32; }
			};
			CompassGeometry geometry = new CompassGeometry();
			geometry.labels[CompassDirection.N.ordinal()] = new Point2D.Double(35, 35);
			geometry.inner.moveTo(130, 20);
			geometry.inner.lineTo(160, 20);
			geometry.outer.moveTo(130, 80);
			geometry.outer.lineTo(160, 80);
			geometry.spokes[CompassDirection.E.ordinal()].moveTo(130, 140);
			geometry.spokes[CompassDirection.E.ordinal()].lineTo(160, 140);
			geometry.indicator.path.moveTo(180, 200);
			geometry.indicator.path.lineTo(200, 200);
			geometry.indicator.path.lineTo(200, 220);
			geometry.indicator.path.lineTo(180, 220);
			geometry.indicator.path.closePath();
			BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = image.createGraphics();
			try
			{
				graphics.setClip(0, 0, 300, 300);
				NeverEatSoggyWeetbixOverlay overlay = new NeverEatSoggyWeetbixOverlay(null, config);
				overlay.updateFonts(graphics);
				overlay.prepareLabels(geometry);
				overlay.drawCompass(graphics, 0, geometry);
				assertEquals(64, maxAlpha(image, 20, 20, 30, 30));
				assertEquals(enabled ? 90 : 0, maxAlpha(image, 52, 52, 30, 30));
				assertEquals(enabled ? 90 : 0, image.getRGB(175, 52) >>> 24);
				assertEquals(enabled ? 90 : 0, image.getRGB(175, 112) >>> 24);
				assertEquals(enabled ? 90 : 0, image.getRGB(175, 172) >>> 24);
				assertEquals(enabled ? 90 : 0, image.getRGB(225, 245) >>> 24);
			}
			finally
			{
				graphics.dispose();
			}
		}
	}

	private static int maxAlpha(BufferedImage image, int x0, int y0, int width, int height)
	{
		int max = 0;
		for (int y = y0; y < y0 + height; y++)
		{
			for (int x = x0; x < x0 + width; x++)
			{
				max = Math.max(max, image.getRGB(x, y) >>> 24);
			}
		}
		return max;
	}

	@Test
	public void lettersCoverBothCirclesAndLaterDirectionConnectors()
	{
		for (LetterRotation rotation : LetterRotation.values())
		{
			NeverEatSoggyWeetbixConfig config = new NeverEatSoggyWeetbixConfig()
			{
				@Override public boolean useCustomFont() { return true; }
				@Override public boolean onlyCurrentDirectionConnectors() { return false; }
				@Override public FontType customFont() { return new FontType().withFont(new Font(Font.SANS_SERIF, Font.BOLD, 24)); }
				@Override public LetterRotation letterRotation() { return rotation; }
				@Override public int overallOpacity() { return 100; }
				@Override public boolean showCardinalConnectors() { return true; }
				@Override public boolean connectorDirectionColors() { return false; }
				@Override public Color baseColor() { return Color.RED; }
				@Override public int connectorThickness() { return 12; }
				@Override public int innerThickness() { return 12; }
				@Override public int outerThickness() { return 12; }
			};
			CompassGeometry geometry = new CompassGeometry();
			geometry.inner.moveTo(10, 40);
			geometry.inner.lineTo(70, 40);
			geometry.outer.moveTo(40, 10);
			geometry.outer.lineTo(40, 70);
			geometry.labels[CompassDirection.N.ordinal()] = new Point2D.Double(40, 40);
			geometry.rotations[CompassDirection.N.ordinal()] = Math.PI / 4;
			// E is visited after N. The old interleaved loop would paint this over N.
			geometry.spokes[CompassDirection.E.ordinal()].moveTo(10, 40);
			geometry.spokes[CompassDirection.E.ordinal()].lineTo(70, 40);
			BufferedImage actual = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = actual.createGraphics();
			try
			{
				NeverEatSoggyWeetbixOverlay overlay = new NeverEatSoggyWeetbixOverlay(null, config);
				overlay.updateFonts(graphics);
				overlay.prepareLabels(geometry);
				overlay.drawCompass(graphics, 0, geometry);
				Shape glyph = CompassFonts.fromConfig(config).cardinal
					.createGlyphVector(graphics.getFontRenderContext(), "N").getOutline();
				Rectangle2D bounds = glyph.getBounds2D();
				AffineTransform placement = AffineTransform.getTranslateInstance(40, 40);
				if (rotation == LetterRotation.ROTATION)
				{
					placement.rotate(Math.PI / 4);
				}
				placement.translate(-bounds.getCenterX(), -bounds.getCenterY());
				Shape letter = placement.createTransformedShape(glyph);
				int checked = 0;
				for (int y = 35; y < 45; y++)
				{
					for (int x = 20; x < 60; x++)
					{
						// Check interior coverage rather than rasterizer-specific edge rounding.
						if (letter.contains(x - 0.25, y - 0.25, 1.5, 1.5))
						{
							assertEquals(rotation + " letter at " + x + "," + y,
								config.currentColor().getRGB(), actual.getRGB(x, y));
							checked++;
						}
					}
				}
				assertTrue("Fixture must overlap the connector with visible letter pixels", checked > 10);
				assertEquals(Color.RED.getRGB(), actual.getRGB(15, 40));
				assertEquals(Color.WHITE.getRGB(), actual.getRGB(40, 15));
			}
			finally
			{
				graphics.dispose();
			}
		}
	}
}
