package com.nevereatsoggyweetbix;

import org.junit.Test;
import static org.junit.Assert.*;

public class DirectionLabelsTest
{
	@Test
	public void modesSelectAbbreviationsFullNamesOrTheMnemonic()
	{
		assertArrayEquals(new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"},
			DirectionLabels.resolve(new NeverEatSoggyWeetbixConfig() {}));
		assertArrayEquals(new String[]{"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest"},
			DirectionLabels.resolve(new NeverEatSoggyWeetbixConfig()
			{
				@Override public DirectionLabelMode labelMode() { return DirectionLabelMode.FULL_NAMES; }
			}));
		assertArrayEquals(new String[]{"Never", "NE", "Eat", "SE", "Soggy", "SW", "WeetBix", "NW"},
			DirectionLabels.resolve(new NeverEatSoggyWeetbixConfig()
			{
				@Override public DirectionLabelMode labelMode() { return DirectionLabelMode.CUSTOM; }
			}));
	}

	@Test
	public void customDefinitionsAllowPartialMappingsBlankValuesAndTolerateEditingMistakes()
	{
		assertArrayEquals(new String[]{"North=Up", "NE", "Eat", "SE", "", "SW", "W", "NW"},
			DirectionLabels.resolve(new NeverEatSoggyWeetbixConfig()
			{
				@Override public DirectionLabelMode labelMode() { return DirectionLabelMode.CUSTOM; }
				@Override public String customLabels()
				{
					return "\r\n n = First \r\nE=Eat\nnot a definition\nUNKNOWN=ignored\n=ignored\n s = \nN= North=Up \r\n";
				}
			}));
	}
}
