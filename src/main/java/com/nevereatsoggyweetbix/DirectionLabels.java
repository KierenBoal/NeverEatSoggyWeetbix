package com.nevereatsoggyweetbix;

import java.util.Locale;

final class DirectionLabels
{
	private static final String[] FULL_NAMES = {
		"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest"
	};

	private DirectionLabels()
	{
	}

	static String[] resolve(NeverEatSoggyWeetbixConfig config)
	{
		if (config.labelMode() == DirectionLabelMode.FULL_NAMES)
		{
			return FULL_NAMES.clone();
		}
		String[] labels = new String[CompassDirection.ALL.length];
		for (CompassDirection direction : CompassDirection.ALL)
		{
			labels[direction.ordinal()] = direction.name();
		}
		String definitions = config.customLabels();
		if (config.labelMode() != DirectionLabelMode.CUSTOM || definitions == null)
		{
			return labels;
		}
		for (String line : definitions.split("\\R"))
		{
			int separator = line.indexOf('=');
			if (separator < 0)
			{
				continue;
			}
			String key = line.substring(0, separator).trim().toUpperCase(Locale.ROOT);
			for (CompassDirection direction : CompassDirection.ALL)
			{
				if (direction.name().equals(key))
				{
					labels[direction.ordinal()] = line.substring(separator + 1).trim();
					break;
				}
			}
		}
		return labels;
	}
}
