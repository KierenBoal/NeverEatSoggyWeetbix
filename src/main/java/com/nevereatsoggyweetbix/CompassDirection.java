package com.nevereatsoggyweetbix;

import java.awt.Color;

enum CompassDirection
{
	N, NE, E, SE, S, SW, W, NW;

	static final CompassDirection[] ALL = values();
	final double x = Math.sin(ordinal() * Math.PI / 4);
	final double y = Math.cos(ordinal() * Math.PI / 4);

	boolean diagonal()
	{
		return (ordinal() & 1) != 0;
	}

	boolean lettersVisible(NeverEatSoggyWeetbixConfig config)
	{
		return diagonal() ? !config.hideDiagonals() : !config.hideCardinals();
	}

	boolean connectorVisible(NeverEatSoggyWeetbixConfig config, double heading)
	{
		if (!(diagonal() ? config.showDiagonalConnectors() : config.showCardinalConnectors()))
		{
			return false;
		}
		boolean onlyCurrent = config.onlyCurrentDirectionConnectors();
		boolean onlyOpposite = config.onlyOppositeDirectionConnectors();
		if (!onlyCurrent && !onlyOpposite)
		{
			return true;
		}
		int offset = Math.floorMod(ordinal() - current(heading, diagonal()), 8);
		return (onlyCurrent && offset == 0) || (onlyOpposite && offset == 4);
	}

	static int current(double heading, boolean diagonal)
	{
		double turns = heading / (Math.PI * 2);
		turns -= Math.floor(turns);
		int group = (int) Math.floor(turns * 4 + (diagonal ? 0 : 0.5));
		return (group * 2 + (diagonal ? 1 : 0)) & 7;
	}

	Color color(NeverEatSoggyWeetbixConfig config, double heading)
	{
		int role = Math.floorMod(ordinal() - current(heading, diagonal()), 8) / 2;
		if (diagonal())
		{
			switch (role)
			{
				case 0: return config.currentSubColor();
				case 1: return config.rightSubColor();
				case 2: return config.oppositeSubColor();
				default: return config.leftSubColor();
			}
		}
		switch (role)
		{
			case 0: return config.currentColor();
			case 1: return config.rightColor();
			case 2: return config.oppositeColor();
			default: return config.leftColor();
		}
	}

	static Color withOpacity(Color color, int percent)
	{
		int alpha = (int) Math.round(color.getAlpha() * Math.max(0, Math.min(100, percent)) / 100.0);
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}
}
