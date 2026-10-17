package com.nevereatsoggyweetbix;

import java.awt.Font;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.FontManager;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompassFontsTest
{
	@Test
	public void builtInFontsUseTwentyFourAndTwentyPixels()
	{
		PlayerCompassConfig config = new PlayerCompassConfig() {};
		CompassFonts fonts = CompassFonts.fromConfig(config);
		assertEquals(24f, fonts.cardinal.getSize2D(), 0f);
		assertEquals(20f, fonts.diagonal.getSize2D(), 0f);
		assertEquals(FontManager.getRunescapeBoldFont().getFamily(), fonts.cardinal.getFamily());
		assertEquals(FontManager.getRunescapeSmallFont().getFamily(), fonts.diagonal.getFamily());
		assertEquals(24, config.customFont().getSize());
		assertEquals(20, config.subDirectionFont().getSize());
	}

	@Test
	public void customFontsPreserveTheirIndependentSizesAndStyles()
	{
		Font selectedCardinal = new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 25).deriveFont(25.5f);
		Font selectedSubDirection = new Font(Font.MONOSPACED, Font.PLAIN, 17).deriveFont(17.25f);
		PlayerCompassConfig config = new PlayerCompassConfig()
		{
			@Override public boolean useCustomFont() { return true; }
			@Override public FontType customFont() { return new FontType().withFont(selectedCardinal); }
			@Override public FontType subDirectionFont() { return new FontType().withFont(selectedSubDirection); }
		};
		CompassFonts fonts = CompassFonts.fromConfig(config);
		assertEquals(25.5f, fonts.cardinal.getSize2D(), 0f);
		assertEquals(17.25f, fonts.diagonal.getSize2D(), 0f);
		assertEquals(selectedCardinal.getFamily(), fonts.cardinal.getFamily());
		assertEquals(selectedSubDirection.getFamily(), fonts.diagonal.getFamily());
		assertEquals(selectedCardinal.getStyle(), fonts.cardinal.getStyle());
		assertEquals(selectedSubDirection.getStyle(), fonts.diagonal.getStyle());
		assertEquals(25.5f, selectedCardinal.getSize2D(), 0f);
		assertEquals(17.25f, selectedSubDirection.getSize2D(), 0f);
	}

	@Test
	public void missingCustomSelectionFallsBackToTwentyFourAndTwentyPixels()
	{
		PlayerCompassConfig config = new PlayerCompassConfig()
		{
			@Override public boolean useCustomFont() { return true; }
			@Override public FontType customFont() { return null; }
		};
		CompassFonts fonts = CompassFonts.fromConfig(config);
		assertEquals(24f, fonts.cardinal.getSize2D(), 0f);
		assertEquals(20f, fonts.diagonal.getSize2D(), 0f);
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
		assertEquals(20f, fonts.diagonal.getSize2D(), 0f);
		assertEquals(40, config.customFont().getSize());
	}
}
