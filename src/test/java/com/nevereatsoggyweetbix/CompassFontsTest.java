package com.nevereatsoggyweetbix;

import java.awt.Font;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.FontManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompassFontsTest
{
	@Test
	public void builtInFontsUseTwentyFourAndEighteenPixels()
	{
		PlayerCompassConfig config = new PlayerCompassConfig() {};
		CompassFonts fonts = CompassFonts.fromConfig(config);
		assertEquals(24f, fonts.cardinal.getSize2D(), 0f);
		assertEquals(18f, fonts.diagonal.getSize2D(), 0f);
		assertEquals(FontManager.getRunescapeBoldFont().getFamily(), fonts.cardinal.getFamily());
		assertEquals(FontManager.getRunescapeSmallFont().getFamily(), fonts.diagonal.getFamily());
		assertEquals(24, config.customFont().getSize());
	}

	@Test
	public void customFontPreservesSelectedMainSizeAndStyleWithFractionalDiagonalSize()
	{
		Font selected = new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 25).deriveFont(25.5f);
		PlayerCompassConfig config = new PlayerCompassConfig()
		{
			@Override public boolean useCustomFont() { return true; }
			@Override public FontType customFont() { return new FontType().withFont(selected); }
		};
		CompassFonts fonts = CompassFonts.fromConfig(config);
		assertEquals(25.5f, fonts.cardinal.getSize2D(), 0f);
		assertEquals(19.125f, fonts.diagonal.getSize2D(), 0f);
		assertEquals(selected.getFamily(), fonts.cardinal.getFamily());
		assertEquals(selected.getFamily(), fonts.diagonal.getFamily());
		assertEquals(selected.getStyle(), fonts.cardinal.getStyle());
		assertEquals(selected.getStyle(), fonts.diagonal.getStyle());
		assertEquals(25.5f, selected.getSize2D(), 0f);
	}

	@Test
	public void missingCustomSelectionFallsBackToTwentyFourAndEighteenPixels()
	{
		PlayerCompassConfig config = new PlayerCompassConfig()
		{
			@Override public boolean useCustomFont() { return true; }
			@Override public FontType customFont() { return null; }
		};
		CompassFonts fonts = CompassFonts.fromConfig(config);
		assertEquals(24f, fonts.cardinal.getSize2D(), 0f);
		assertEquals(18f, fonts.diagonal.getSize2D(), 0f);
	}

	@Test
	public void disablingCustomFontsIgnoresSavedCustomSelection()
	{
		PlayerCompassConfig config = new PlayerCompassConfig()
		{
			@Override public FontType customFont() { return new FontType().withFamily("Serif").withSize(40); }
		};
		CompassFonts fonts = CompassFonts.fromConfig(config);
		assertEquals(24f, fonts.cardinal.getSize2D(), 0f);
		assertEquals(18f, fonts.diagonal.getSize2D(), 0f);
		assertEquals(40, config.customFont().getSize());
	}
}
