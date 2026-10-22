package com.nevereatsoggyweetbix;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class DirectionLabelRenderingTest
{
	private static final class LabelConfig implements NeverEatSoggyWeetbixConfig
	{
		DirectionLabelMode mode = DirectionLabelMode.ABBREVIATIONS;
		String definitions = "N=Never eat soggy WeetBix";
		boolean hidden;
		boolean shadow;
		int padding = 4;
		LetterRotation rotation;

		@Override public DirectionLabelMode labelMode() { return mode; }
		@Override public String customLabels() { return definitions; }
		@Override public boolean hideCardinals() { return hidden; }
		@Override public boolean hideDiagonals() { return true; }
		@Override public boolean showInnerRing() { return false; }
		@Override public boolean showOuterRing() { return false; }
		@Override public boolean showCardinalConnectors() { return false; }
		@Override public boolean showDiagonalConnectors() { return false; }
		@Override public boolean directionalIndicatorEnabled() { return false; }
		@Override public LetterRotation letterRotation() { return rotation; }
		@Override public boolean enableDropShadow() { return shadow; }
		@Override public int dropShadowOffset() { return 16; }
		@Override public int ringPadding() { return padding; }
	}

	@Test
	public void editedLabelsRefreshTheirRenderingAndBoundsWithRotationAndShadows()
	{
		for (LetterRotation rotation : LetterRotation.values())
		{
			for (boolean shadow : new boolean[]{false, true})
			{
				LabelConfig config = new LabelConfig();
				config.rotation = rotation;
				config.shadow = shadow;
				NeverEatSoggyWeetbixOverlay overlay = new NeverEatSoggyWeetbixOverlay(null, config);
				CompassGeometry geometry = new CompassGeometry();
				CompassAnchor anchor = new CompassAnchor(new CompassProjection(
					CompassProjection.cameraMatrix(0, 0.8, 0, -1400, -1200), 300, 300, 800),
					0, 0, 0, 0, 0);
				Rectangle abbreviation = render(overlay, geometry, anchor, true);
				config.mode = DirectionLabelMode.FULL_NAMES;
				Rectangle fullName = render(overlay, geometry, anchor, true);
				assertTrue(fullName.width > abbreviation.width);
				config.mode = DirectionLabelMode.CUSTOM;
				Rectangle custom = render(overlay, geometry, anchor, true);
				assertTrue(custom.width > fullName.width);
				config.definitions += "\nNE=" + "hidden".repeat(100);
				assertEquals("Hidden diagonals must not inflate ring spacing", custom, render(overlay, geometry, anchor, true));
				config.hidden = true;
				assertTrue(render(overlay, geometry, anchor, false).isEmpty());
				config.hidden = false;
				config.definitions = "N=";
				assertTrue(render(overlay, geometry, anchor, false).isEmpty());
				if (rotation == LetterRotation.ROTATION)
				{
					config.definitions = "N=W";
					render(overlay, geometry, anchor, true);
					double shortBand = overlay.labelBandWidth();
					config.definitions = "N=WWWWWWWWWWWW";
					render(overlay, geometry, anchor, true);
					assertEquals("Long curved words must not widen the band", shortBand, overlay.labelBandWidth(), 1e-9);
					config.padding = 8;
					render(overlay, geometry, anchor, true);
					assertEquals("Padding applies to both sides", shortBand + 8, overlay.labelBandWidth(), 1e-9);
				}
			}
		}
	}

	private static Rectangle render(NeverEatSoggyWeetbixOverlay overlay, CompassGeometry geometry, CompassAnchor anchor, boolean visible)
	{
		BufferedImage image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			overlay.invalidateFonts();
			overlay.updateFonts(graphics);
			overlay.updateGeometry(geometry, anchor);
			for (CompassDirection direction : CompassDirection.ALL)
			{
				if (direction != CompassDirection.N)
				{
					geometry.labels[direction.ordinal()] = null;
				}
			}
			overlay.prepareLabels(geometry);
			Rectangle bounds = overlay.drawingBounds(new Rectangle(0, 0, 600, 600), geometry, 0);
			overlay.drawCompass(graphics, 0, geometry);
			int pixels = 0;
			for (int y = 0; y < image.getHeight(); y++)
			{
				for (int x = 0; x < image.getWidth(); x++)
				{
					if ((image.getRGB(x, y) >>> 24) != 0)
					{
						assertTrue("Text and shadows must fit the clipping bounds", bounds.contains(x, y));
						pixels++;
					}
				}
			}
			assertEquals(visible, pixels > 0);
			return bounds;
		}
		finally
		{
			graphics.dispose();
		}
	}
}
