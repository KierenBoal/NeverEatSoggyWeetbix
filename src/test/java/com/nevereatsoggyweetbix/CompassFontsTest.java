package com.nevereatsoggyweetbix;

import java.awt.Font;
import net.runelite.client.config.FontType;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompassFontsTest
{
	@Test
	public void customFontsPreserveTheirIndependentSizesAndStyles()
	{
		Font selectedCardinal = new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 25).deriveFont(25.5f);
		Font selectedSubDirection = new Font(Font.MONOSPACED, Font.PLAIN, 17).deriveFont(17.25f);
		NeverEatSoggyWeetbixConfig config = new NeverEatSoggyWeetbixConfig()
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
}
