package com.nevereatsoggyweetbix;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.FontType;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(NeverEatSoggyWeetbixConfig.GROUP)
public interface NeverEatSoggyWeetbixConfig extends Config
{
	String GROUP = "never-eat-soggy-weetbix";

	@ConfigItem(keyName = "drawAbovePlayer", name = "Render above self", description = "Render the compass over your player. Enabled by default; turn off to let your player hide the compass behind its model.", position = 0)
	default boolean drawAbovePlayer()
	{
		return true;
	}

	@ConfigItem(keyName = "highlightCalculation", name = "Direction", description = "Camera: camera heading. Player: player facing. North: fixed north. Mouse: direction from player to cursor. Destination: destination tile. Destination tile targets the destination first, then the hovered tile center. Missing targets use camera heading.", position = 1)
	default HighlightCalculation highlightCalculation()
	{
		return HighlightCalculation.FROM_PLAYER_TO_MOUSE;
	}

	@ConfigItem(keyName = "showHideHotkey", name = "Show/hide hotkey", description = "Show or hide the whole compass using Keybind mode below. Supports RuneLite key combinations such as Ctrl+C. Unbound by default.", position = 2)
	default Keybind showHideHotkey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(keyName = "keybindMode", name = "Keybind mode", description = "Toggle: press to switch visibility. Hold: hidden unless the hotkey is held. Hold invisible: visible unless the hotkey is held. Changing the mode or hotkey resets visibility to that mode's resting state.", position = 3)
	default KeybindMode keybindMode()
	{
		return KeybindMode.TOGGLE;
	}

	@ConfigSection(name = "Placement", description = "Placement settings", position = 4, closedByDefault = true)
	String placement = "placement";

	@ConfigSection(name = "Labels", description = "Labels settings", position = 5, closedByDefault = true)
	String labels = "labels";

	@ConfigSection(name = "Directional indicator", description = "A shape outside the ring that follows the selected direction.", position = 6, closedByDefault = true)
	String directionalIndicator = "directionalIndicator";

	@ConfigSection(name = "Rings", description = "Rings settings", position = 7, closedByDefault = true)
	String rings = "rings";

	@ConfigSection(name = "Connectors", description = "Connectors settings", position = 8, closedByDefault = true)
	String connectors = "connectors";

	@ConfigSection(name = "Appearance", description = "Appearance settings", position = 9, closedByDefault = true)
	String appearance = "appearance";

	@ConfigSection(name = "Colors", description = "Colors settings", position = 10, closedByDefault = true)
	String colors = "colors";

	@ConfigItem(keyName = "anchorMode", name = "Anchor", description = "Anchor at Player feet, Player head, True tile, Screen center, Mouse position, or Hovered tile center.", section = placement, position = 0)
	default AnchorMode anchorMode()
	{
		return AnchorMode.PLAYER_FEET;
	}

	@Range(min = 0, max = 128)
	@ConfigItem(keyName = "renderDistance", name = "Radius (tiles)", description = "Minimum inner ring radius in world tiles, including fractional values from 0 to 128. Outward labels can move the ring band farther out to make room for long words.", section = placement, position = 1)
	default double renderDistance()
	{
		return 1.0;
	}

	@Range(min = -512, max = 512)
	@ConfigItem(keyName = "heightOffset", name = "Height offset", description = "World units above the anchor; negative values lower the compass.", section = placement, position = 2)
	default int heightOffset()
	{
		return 0;
	}

	@ConfigItem(keyName = "labelMode", name = "Label mode", description = "Show direction abbreviations, full names, or the custom labels below.", section = labels, position = 0)
	default DirectionLabelMode labelMode()
	{
		return DirectionLabelMode.ABBREVIATIONS;
	}

	@ConfigItem(keyName = "customLabels", name = "Custom labels", description = "Used in Custom mode. One definition per line, e.g. N=Never. Missing directions use abbreviations; N= hides North. Invalid lines are ignored; the last definition wins.", section = labels, position = 1)
	default String customLabels()
	{
		return "N=Never\nNE=NE\nE=Eat\nSE=SE\nS=Soggy\nSW=SW\nW=WeetBix\nNW=NW";
	}

	@ConfigItem(keyName = "hideCardinals", name = "Hide cardinals", description = "Hide N/E/S/W labels without hiding their connectors.", section = labels, position = 2)
	default boolean hideCardinals()
	{
		return false;
	}

	@ConfigItem(keyName = "hideDiagonals", name = "Hide diagonals", description = "Hide NE/SE/SW/NW labels without hiding their connectors.", section = labels, position = 3)
	default boolean hideDiagonals()
	{
		return false;
	}

	@ConfigItem(keyName = "letterRotation", name = "Rotation", description = "Upright keeps words horizontal. Outward curves each word around the projected ring, with characters following its tangent and their tops pointing outward.", section = labels, position = 4)
	default LetterRotation letterRotation()
	{
		return LetterRotation.ALWAYS_UP;
	}

	@ConfigItem(keyName = "useCustomFont", name = "Custom fonts", description = "Use the custom cardinal and sub direction fonts below instead of the RuneScape fonts.", section = labels, position = 5)
	default boolean useCustomFont()
	{
		return false;
	}

	@ConfigItem(keyName = "customFont", name = "Cardinal font", description = "Font family, size and style for cardinal labels. Used only when Custom fonts is enabled.", section = labels, position = 6)
	default FontType customFont()
	{
		return new FontType().withFamily("SansSerif").withSize(CompassFonts.DEFAULT_MAIN_SIZE);
	}

	@ConfigItem(keyName = "subDirectionFont", name = "Diagonal font", description = "Font family, size and style for diagonal labels. Used only when Custom fonts is enabled.", section = labels, position = 7)
	default FontType subDirectionFont()
	{
		return new FontType().withFamily("SansSerif").withSize(CompassFonts.DEFAULT_SUB_SIZE);
	}

	@ConfigItem(keyName = "directionalIndicatorEnabled", name = "Direction indicator", description = "Show the indicator pointing in the selected direction. Independent of Opposite indicator; also visible when rings are hidden.", section = directionalIndicator, position = 0)
	default boolean directionalIndicatorEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "oppositeIndicator", name = "Opposite indicator", description = "Show an indicator pointing opposite the selected direction, independently of Direction indicator. Shares shape, size, rotation, and shadows. Colors are in Colors.", section = directionalIndicator, position = 1)
	default boolean oppositeIndicator()
	{
		return false;
	}

	@ConfigItem(keyName = "limitRotationSteps", name = "Snap rotation", description = "Snap indicator rotation to the nearest step, with halfway angles rounding clockwise. Direction label colors still follow the unsnapped heading.", section = directionalIndicator, position = 3)
	default boolean limitRotationSteps()
	{
		return true;
	}

	@Range(min = 1, max = 360)
	@ConfigItem(keyName = "rotationSteps", name = "Rotation steps", description = "Number of evenly spaced indicator headings: 8 gives 45-degree steps, 16 gives 22.5-degree steps. Used only when Snap rotation is enabled.", section = directionalIndicator, position = 4)
	default int rotationSteps()
	{
		return 8;
	}

	@ConfigItem(keyName = "directionalIndicatorShape", name = "Shape", description = "Indicator silhouette. Unequal width and height stretch Square and Circle into a rectangle or ellipse.", section = directionalIndicator, position = 2)
	default DirectionalIndicatorShape directionalIndicatorShape()
	{
		return DirectionalIndicatorShape.TRIANGLE;
	}

	@Range(min = 1, max = 128)
	@Units("px")
	@ConfigItem(keyName = "directionalIndicatorWidth", name = "Width", description = "Width across the heading at the anchor's horizontal pixel scale, before ground-plane perspective.", section = directionalIndicator, position = 5)
	default int directionalIndicatorWidth()
	{
		return 32;
	}

	@Range(min = 1, max = 128)
	@Units("px")
	@ConfigItem(keyName = "directionalIndicatorHeight", name = "Height", description = "Length outward toward the tip at the anchor's horizontal pixel scale, before ground-plane perspective.", section = directionalIndicator, position = 6)
	default int directionalIndicatorHeight()
	{
		return 32;
	}

	@Range(min = 1, max = 12)
	@Units("px")
	@ConfigItem(keyName = "directionalIndicatorLineThickness", name = "Line width", description = "Screen-pixel thickness of the indicator outline.", section = directionalIndicator, position = 8)
	default int directionalIndicatorLineThickness()
	{
		return 1;
	}

	@Range(min = 0, max = 128)
	@Units("px")
	@ConfigItem(keyName = "directionalIndicatorMargin", name = "Margin", description = "Gap outside the outer ring for both indicators, measured in pixels at the anchor's horizontal scale before ground-plane perspective. Outline clearance is added automatically.", section = directionalIndicator, position = 7)
	default int directionalIndicatorMargin()
	{
		return 8;
	}

	@ConfigItem(keyName = "showInnerRing", name = "Inner ring", description = "Draw the inner edge of the label band.", section = rings, position = 0)
	default boolean showInnerRing()
	{
		return true;
	}

	@Range(min = 1, max = 12)
	@Units("px")
	@ConfigItem(keyName = "innerThickness", name = "Inner width", description = "Screen-pixel width of the inner circle.", section = rings, position = 1)
	default int innerThickness()
	{
		return 1;
	}

	@ConfigItem(keyName = "showOuterRing", name = "Outer ring", description = "Draw the outer boundary around the label band.", section = rings, position = 2)
	default boolean showOuterRing()
	{
		return true;
	}

	@Range(min = 1, max = 12)
	@Units("px")
	@ConfigItem(keyName = "outerThickness", name = "Outer width", description = "Screen-pixel width of the outer circle.", section = rings, position = 3)
	default int outerThickness()
	{
		return 1;
	}

	@Range(min = 0, max = 128)
	@Units("px")
	@ConfigItem(keyName = "ringPadding", name = "Label padding", description = "Extra space on each side of the labels. Outward fits character height with perspective clearance; word length affects circle placement, not band thickness. Upright padding is measured at the anchor's horizontal scale.", section = rings, position = 4)
	default int ringPadding()
	{
		return 4;
	}

	@ConfigItem(keyName = "showCardinalConnectors", name = "Cardinal lines", description = "Show the four short N/E/S/W spokes.", section = connectors, position = 0)
	default boolean showCardinalConnectors()
	{
		return true;
	}

	@ConfigItem(keyName = "showDiagonalConnectors", name = "Diagonal lines", description = "Show the four short NE/SE/SW/NW spokes.", section = connectors, position = 1)
	default boolean showDiagonalConnectors()
	{
		return true;
	}

	@ConfigItem(keyName = "onlyCurrentDirectionConnectors", name = "Current only", description = "Show only current highlighted connectors from the enabled cardinal/diagonal groups. Enabled by default. Enable Opposite only too to show both roles; disable both filters to show all enabled connectors.", section = connectors, position = 2)
	default boolean onlyCurrentDirectionConnectors()
	{
		return true;
	}

	@ConfigItem(keyName = "onlyOppositeDirectionConnectors", name = "Opposite only", description = "Show only opposite highlighted connectors from the enabled cardinal/diagonal groups. Enable Current only too to show both roles; disable both filters to show all enabled connectors.", section = connectors, position = 3)
	default boolean onlyOppositeDirectionConnectors()
	{
		return false;
	}

	@Range(min = 1, max = 12)
	@Units("px")
	@ConfigItem(keyName = "connectorThickness", name = "Line width", description = "Screen-pixel width of all connector lines.", section = connectors, position = 4)
	default int connectorThickness()
	{
		return 1;
	}

	@ConfigItem(keyName = "connectorDirectionColors", name = "Direction colors", description = "Use matching direction colors; otherwise use the Connectors color in Colors.", section = connectors, position = 5)
	default boolean connectorDirectionColors()
	{
		return true;
	}

	@Range(min = 0, max = 100)
	@Units("%")
	@ConfigItem(keyName = "overallOpacity", name = "Opacity", description = "Multiply each configured color's alpha by this percentage.", section = appearance, position = 0)
	default int overallOpacity()
	{
		return 100;
	}

	@ConfigItem(keyName = "enableDropShadow", name = "Drop shadow", description = "Draw shadows behind labels, rings, connectors, and the directional indicator.", section = appearance, position = 1)
	default boolean enableDropShadow()
	{
		return true;
	}

	@Alpha
	@ConfigItem(keyName = "dropShadowColor", name = "Shadow color", description = "Shared color for all compass shadows. Its alpha is multiplied by overall opacity.", section = appearance, position = 2)
	default Color dropShadowColor()
	{
		return new Color(0, 0, 0, 180);
	}

	@Range(min = 0, max = 128)
	@Units("px")
	@ConfigItem(keyName = "dropShadowOffset", name = "Shadow offset", description = "Screen-pixel offset down and right, for all compass shadows, independent of rotation.", section = appearance, position = 3)
	default int dropShadowOffset()
	{
		return 1;
	}


	@ConfigItem(keyName = "drawAbovePlayers", name = "Above players", description = "Draw over other players. Turn off to mask under their silhouettes, using additional CPU. Your own player is controlled separately.", section = appearance, position = 4)
	default boolean drawAbovePlayers()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(keyName = "maxPlayers", name = "Player limit", description = "Maximum other-player model calculations per frame. Largest screen overlaps are prioritized; 0 skips masking. Independent of the NPC limit.", section = appearance, position = 5)
	default int maxPlayers()
	{
		return 12;
	}

	@ConfigItem(keyName = "drawAboveNpcs", name = "Above NPCs", description = "Draw over NPCs. Turn off to mask the compass under NPC silhouettes, using additional CPU.", section = appearance, position = 6)
	default boolean drawAboveNpcs()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(keyName = "maxNpcs", name = "NPC limit", description = "Maximum NPC model calculations per frame when drawing under NPCs. Largest screen overlaps are prioritized; 0 skips NPC masking.", section = appearance, position = 7)
	default int maxNpcs()
	{
		return 12;
	}

	@Alpha
	@ConfigItem(keyName = "currentColor", name = "Current", description = "Nearest cardinal to the selected highlight heading.", section = colors, position = 0)
	default Color currentColor()
	{
		return new Color(86, 180, 233, 255);
	}

	@Alpha
	@ConfigItem(keyName = "currentSubColor", name = "Current diagonal", description = "Nearest diagonal to the selected highlight heading; exact ties choose clockwise.", section = colors, position = 1)
	default Color currentSubColor()
	{
		return new Color(86, 180, 233, 255);
	}

	@Alpha
	@ConfigItem(keyName = "oppositeColor", name = "Opposite", description = "Cardinal opposite the current cardinal.", section = colors, position = 2)
	default Color oppositeColor()
	{
		return new Color(230, 159, 0, 255);
	}

	@Alpha
	@ConfigItem(keyName = "oppositeSubColor", name = "Opposite diagonal", description = "Diagonal opposite the current diagonal.", section = colors, position = 3)
	default Color oppositeSubColor()
	{
		return new Color(230, 159, 0, 255);
	}

	@Alpha
	@ConfigItem(keyName = "leftColor", name = "Left", description = "Cardinal 90 degrees counterclockwise from the current cardinal.", section = colors, position = 4)
	default Color leftColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "leftSubColor", name = "Left diagonal", description = "Diagonal 90 degrees counterclockwise from the current diagonal.", section = colors, position = 5)
	default Color leftSubColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "rightColor", name = "Right", description = "Cardinal 90 degrees clockwise from the current cardinal.", section = colors, position = 6)
	default Color rightColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "rightSubColor", name = "Right diagonal", description = "Diagonal 90 degrees clockwise from the current diagonal.", section = colors, position = 7)
	default Color rightSubColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "directionalIndicatorFillColor", name = "Indicator fill", description = "Indicator fill color, multiplied by overall opacity. Set alpha to zero for an outline only.", section = colors, position = 8)
	default Color directionalIndicatorFillColor()
	{
		return new Color(86, 180, 233, 255);
	}

	@Alpha
	@ConfigItem(keyName = "directionalIndicatorLineColor", name = "Indicator outline", description = "Indicator outline color, multiplied by overall opacity.", section = colors, position = 9)
	default Color directionalIndicatorLineColor()
	{
		return new Color(86, 180, 233, 255);
	}

	@Alpha
	@ConfigItem(keyName = "oppositeIndicatorFillColor", name = "Opposite fill", description = "Opposite indicator fill color, multiplied by overall opacity. Set alpha to zero for an outline only.", section = colors, position = 10)
	default Color oppositeIndicatorFillColor()
	{
		return new Color(230, 159, 0, 255);
	}

	@Alpha
	@ConfigItem(keyName = "oppositeIndicatorLineColor", name = "Opposite outline", description = "Opposite indicator outline color, multiplied by overall opacity.", section = colors, position = 11)
	default Color oppositeIndicatorLineColor()
	{
		return new Color(230, 159, 0, 255);
	}

	@Alpha
	@ConfigItem(keyName = "innerColor", name = "Inner ring", description = "Color of the inner ring.", section = colors, position = 12)
	default Color innerColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "outerColor", name = "Outer ring", description = "Color of the outer ring.", section = colors, position = 13)
	default Color outerColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "baseColor", name = "Connectors", description = "Connector color when direction colors are disabled.", section = colors, position = 14)
	default Color baseColor()
	{
		return Color.WHITE;
	}
}
