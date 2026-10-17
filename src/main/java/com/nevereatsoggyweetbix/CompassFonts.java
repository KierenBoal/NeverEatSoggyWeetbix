package com.nevereatsoggyweetbix;

import java.awt.Font;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.FontManager;

final class CompassFonts
{
	static final int DEFAULT_MAIN_SIZE = 24;
	private static final float DIAGONAL_SCALE = 0.75f;

	final Font cardinal;
	final Font diagonal;

	private CompassFonts(Font cardinal, Font diagonal)
	{
		this.cardinal = cardinal;
		this.diagonal = diagonal;
	}

	static CompassFonts fromConfig(PlayerCompassConfig config)
	{
		if (!config.useCustomFont())
		{
			return new CompassFonts(
				FontManager.getRunescapeBoldFont().deriveFont((float) DEFAULT_MAIN_SIZE),
				FontManager.getRunescapeSmallFont().deriveFont(DEFAULT_MAIN_SIZE * DIAGONAL_SCALE));
		}
		FontType selected = config.customFont();
		Font main = selected == null ? new Font(Font.SANS_SERIF, Font.PLAIN, DEFAULT_MAIN_SIZE) : selected.getFont();
		// Bound damaged/hand-edited settings before allocating glyph outlines.
		main = main.deriveFont(Math.max(1f, Math.min(512f, main.getSize2D())));
		return new CompassFonts(main, main.deriveFont(main.getSize2D() * DIAGONAL_SCALE));
	}
}
