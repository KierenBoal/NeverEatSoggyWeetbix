package com.nevereatsoggyweetbix;

import java.awt.Color;
import java.util.EnumSet;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompassDirectionTest
{
	private final NeverEatSoggyWeetbixConfig config = new NeverEatSoggyWeetbixConfig() {};

	@Test
	public void cameraColorsMoveWhileGeographicLabelsStayFixed()
	{
		assertEquals(config.currentColor(), CompassDirection.N.color(config, 0));
		assertEquals(config.oppositeColor(), CompassDirection.S.color(config, 0));
		assertEquals(config.currentSubColor(), CompassDirection.NE.color(config, 0));
		assertEquals(config.oppositeSubColor(), CompassDirection.SW.color(config, 0));
		assertEquals(config.currentColor(), CompassDirection.E.color(config, Math.PI / 2));
		assertEquals(config.oppositeColor(), CompassDirection.W.color(config, Math.PI / 2));
		assertEquals(config.currentSubColor(), CompassDirection.SE.color(config, Math.PI / 2));
		assertEquals(0, CompassDirection.N.x, 1e-9);
		assertEquals(1, CompassDirection.N.y, 1e-9);
	}

	@Test
	public void adjacentRolesAreIndependentInBothGroups()
	{
		NeverEatSoggyWeetbixConfig distinct = new NeverEatSoggyWeetbixConfig()
		{
			@Override public Color leftColor() { return Color.BLUE; }
			@Override public Color rightColor() { return Color.YELLOW; }
			@Override public Color leftSubColor() { return Color.CYAN; }
			@Override public Color rightSubColor() { return Color.MAGENTA; }
		};
		assertEquals(Color.BLUE, CompassDirection.W.color(distinct, 0));
		assertEquals(Color.YELLOW, CompassDirection.E.color(distinct, 0));
		assertEquals(Color.CYAN, CompassDirection.NW.color(distinct, 0));
		assertEquals(Color.MAGENTA, CompassDirection.SE.color(distinct, 0));
	}

	@Test
	public void exactTiesChooseClockwiseAndHeadingWraps()
	{
		assertEquals(CompassDirection.N.ordinal(), CompassDirection.current(Math.PI / 4 - 1e-6, false));
		assertEquals(CompassDirection.E.ordinal(), CompassDirection.current(Math.PI / 4, false));
		assertEquals(CompassDirection.NE.ordinal(), CompassDirection.current(0, true));
		assertEquals(CompassDirection.NW.ordinal(), CompassDirection.current(-1e-6, true));
		assertEquals(CompassDirection.NE.ordinal(), CompassDirection.current(Math.PI * 2, true));
		assertEquals(CompassDirection.N.ordinal(), CompassDirection.current(-Math.PI / 4, false));
	}

	@Test
	public void opacityMultipliesRatherThanReplacesColorAlpha()
	{
		Color half = CompassDirection.withOpacity(new Color(100, 120, 140, 128), 50);
		assertEquals(new Color(100, 120, 140, 64), half);
		assertEquals(128, CompassDirection.withOpacity(Color.WHITE, 50).getAlpha());
		assertEquals(191, CompassDirection.withOpacity(config.currentSubColor(), 75).getAlpha());
		assertEquals(0, CompassDirection.withOpacity(Color.RED, -1).getAlpha());
		assertEquals(255, CompassDirection.withOpacity(Color.RED, 101).getAlpha());
	}

	@Test
	public void letterAndConnectorSwitchesAreIndependent()
	{
		NeverEatSoggyWeetbixConfig cardinalOnly = new NeverEatSoggyWeetbixConfig()
		{
			@Override public boolean hideCardinals() { return true; }
			@Override public boolean showCardinalConnectors() { return true; }
			@Override public boolean showDiagonalConnectors() { return false; }
		};
		assertFalse(CompassDirection.N.lettersVisible(cardinalOnly));
		assertTrue(CompassDirection.N.connectorVisible(cardinalOnly, 0));
		assertTrue(CompassDirection.NE.lettersVisible(cardinalOnly));
		assertFalse(CompassDirection.NE.connectorVisible(cardinalOnly, 0));
		NeverEatSoggyWeetbixConfig diagonalOnly = new NeverEatSoggyWeetbixConfig()
		{
			@Override public boolean hideDiagonals() { return true; }
			@Override public boolean showDiagonalConnectors() { return true; }
			@Override public boolean showCardinalConnectors() { return false; }
		};
		assertTrue(CompassDirection.N.lettersVisible(diagonalOnly));
		assertFalse(CompassDirection.N.connectorVisible(diagonalOnly, 0));
		assertFalse(CompassDirection.NE.lettersVisible(diagonalOnly));
		assertTrue(CompassDirection.NE.connectorVisible(diagonalOnly, 0));
		for (CompassDirection direction : CompassDirection.ALL)
		{
			assertEquals(direction == CompassDirection.N || direction == CompassDirection.NE,
				direction.connectorVisible(config, 0));
			assertTrue(direction.lettersVisible(config));
		}
		for (int turn = 0; turn < 2; turn++)
		{
			EnumSet<CompassDirection> current = turn == 0 ? EnumSet.of(CompassDirection.N, CompassDirection.NE)
				: EnumSet.of(CompassDirection.E, CompassDirection.SE);
			EnumSet<CompassDirection> opposite = turn == 0 ? EnumSet.of(CompassDirection.S, CompassDirection.SW)
				: EnumSet.of(CompassDirection.W, CompassDirection.NW);
			for (int mask = 0; mask < 16; mask++)
			{
				final int switches = mask;
				NeverEatSoggyWeetbixConfig filtered = new NeverEatSoggyWeetbixConfig()
				{
					@Override public boolean onlyCurrentDirectionConnectors() { return (switches & 1) != 0; }
					@Override public boolean onlyOppositeDirectionConnectors() { return (switches & 2) != 0; }
					@Override public boolean showCardinalConnectors() { return (switches & 4) != 0; }
					@Override public boolean showDiagonalConnectors() { return (switches & 8) != 0; }
				};
				for (CompassDirection direction : CompassDirection.ALL)
				{
					boolean groupEnabled = direction.diagonal() ? (mask & 8) != 0 : (mask & 4) != 0;
					boolean selected = (mask & 3) == 0 || ((mask & 1) != 0 && current.contains(direction))
						|| ((mask & 2) != 0 && opposite.contains(direction));
					assertEquals("Switches " + mask + ", turn " + turn + ", direction " + direction,
						groupEnabled && selected, direction.connectorVisible(filtered, turn * Math.PI / 2));
				}
			}
		}
	}
}
