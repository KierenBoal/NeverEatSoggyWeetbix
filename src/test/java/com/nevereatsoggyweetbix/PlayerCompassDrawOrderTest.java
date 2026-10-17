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

public class PlayerCompassDrawOrderTest
{
	@Test
	public void lettersCoverBothCirclesAndLaterDirectionConnectors()
	{
		for (LetterRotation rotation : LetterRotation.values())
		{
			PlayerCompassConfig config = new PlayerCompassConfig()
			{
				@Override public boolean useCustomFont() { return true; }
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
				PlayerCompassOverlay overlay = new PlayerCompassOverlay(null, config);
				overlay.updateFonts(graphics);
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
