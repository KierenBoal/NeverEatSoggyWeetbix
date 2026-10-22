package com.nevereatsoggyweetbix;

import java.awt.Font;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.FontManager;

final class CompassFonts
{
	static final int DEFAULT_MAIN_SIZE = 24;
	static final int DEFAULT_SUB_SIZE = 20;

	final Font cardinal;
	final Font diagonal;

	private CompassFonts(Font cardinal, Font diagonal)
	{
		this.cardinal = cardinal;
		this.diagonal = diagonal;
	}

	static CompassFonts fromConfig(NeverEatSoggyWeetbixConfig config)
	{
		if (!config.useCustomFont())
		{
			return new CompassFonts(
				FontManager.getRunescapeBoldFont().deriveFont((float) DEFAULT_MAIN_SIZE),
				FontManager.getRunescapeSmallFont().deriveFont((float) DEFAULT_SUB_SIZE));
		}
		return new CompassFonts(
			configuredFont(config.customFont(), DEFAULT_MAIN_SIZE),
			configuredFont(config.subDirectionFont(), DEFAULT_SUB_SIZE));
	}

	private static Font configuredFont(FontType selected, int defaultSize)
	{
		Font font = selected == null ? null : selected.getFont();
		if (font == null)
		{
			font = new Font(Font.SANS_SERIF, Font.PLAIN, defaultSize);
		}
		// Bound damaged/hand-edited settings before allocating glyph outlines.
		float size = Float.isFinite(font.getSize2D())
			? Math.max(1f, Math.min(512f, font.getSize2D())) : defaultSize;
		return font.deriveFont(size);
	}
}
