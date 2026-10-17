package com.nevereatsoggyweetbix;

import java.awt.Color;
import org.junit.Test;
import static org.junit.Assert.*;

public class CompassDirectionTest
{
	private final PlayerCompassConfig config = new PlayerCompassConfig() {};

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
		PlayerCompassConfig distinct = new PlayerCompassConfig()
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
		PlayerCompassConfig cardinalOnly = new PlayerCompassConfig()
		{
			@Override public boolean hideCardinals() { return true; }
			@Override public boolean showCardinalConnectors() { return true; }
			@Override public boolean showDiagonalConnectors() { return false; }
		};
		assertFalse(CompassDirection.N.lettersVisible(cardinalOnly));
		assertTrue(CompassDirection.N.connectorVisible(cardinalOnly));
		assertTrue(CompassDirection.NE.lettersVisible(cardinalOnly));
		assertFalse(CompassDirection.NE.connectorVisible(cardinalOnly));
		PlayerCompassConfig diagonalOnly = new PlayerCompassConfig()
		{
			@Override public boolean hideDiagonals() { return true; }
			@Override public boolean showDiagonalConnectors() { return true; }
			@Override public boolean showCardinalConnectors() { return false; }
		};
		assertTrue(CompassDirection.N.lettersVisible(diagonalOnly));
		assertFalse(CompassDirection.N.connectorVisible(diagonalOnly));
		assertFalse(CompassDirection.NE.lettersVisible(diagonalOnly));
		assertTrue(CompassDirection.NE.connectorVisible(diagonalOnly));
		for (CompassDirection direction : CompassDirection.ALL)
		{
			assertEquals(!direction.diagonal(), direction.connectorVisible(config));
			assertTrue(direction.lettersVisible(config));
		}
	}

	@Test
	public void freshSettingsUseWhiteRingsAccessibleHighlightsAndThinLines()
	{
		assertEquals(1, config.connectorThickness());
		assertEquals(1, config.innerThickness());
		assertEquals(1, config.outerThickness());
		Color adjacent = Color.WHITE;
		assertEquals(Color.WHITE, config.leftColor());
		assertEquals(Color.WHITE, config.rightColor());
		assertEquals(adjacent, config.leftSubColor());
		assertEquals(adjacent, config.rightSubColor());
		assertEquals(Color.WHITE, config.baseColor());
		assertEquals(Color.WHITE, config.innerColor());
		assertEquals(Color.WHITE, config.outerColor());
		assertEquals(new Color(0x4187AF), config.currentColor());
		assertEquals(new Color(0xAD2700), config.oppositeColor());
		assertEquals(new Color(65, 135, 175, 255), config.currentSubColor());
		assertEquals(new Color(0xC22C00), config.oppositeSubColor());
		assertEquals(191, CompassDirection.withOpacity(adjacent, config.overallOpacity()).getAlpha());
		assertEquals(HighlightCalculation.FACING_CAMERA, config.highlightCalculation());
		assertTrue(config.drawAboveNpcs());
		assertEquals(12, config.maxNpcs());
		assertTrue(config.drawAbovePlayers());
		assertEquals(12, config.maxPlayers());
		assertFalse(config.drawAbovePlayer());
		assertTrue(config.enableDropShadow());
		assertEquals(Color.BLACK, config.dropShadowColor());
		assertEquals(1, config.dropShadowOffset());
		assertEquals(1.5, config.renderDistance(), 0);
	}
}
