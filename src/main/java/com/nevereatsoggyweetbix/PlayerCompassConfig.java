package com.nevereatsoggyweetbix;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.FontType;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(PlayerCompassConfig.GROUP)
public interface PlayerCompassConfig extends Config
{
	String GROUP = "player-compass";

	@ConfigSection(name = "Placement", description = "Placement settings", position = 0)
	String placement = "placement";

	@ConfigSection(name = "Labels", description = "Labels settings", position = 1)
	String labels = "labels";

	@ConfigSection(name = "Connectors", description = "Connectors settings", position = 2)
	String connectors = "connectors";

	@ConfigSection(name = "Rings", description = "Rings settings", position = 3)
	String rings = "rings";

	@ConfigSection(name = "Appearance", description = "Appearance settings", position = 4)
	String appearance = "appearance";

	@ConfigSection(name = "Colors", description = "Colors settings", position = 5)
	String colors = "colors";

	@ConfigItem(keyName = "anchorMode", name = "Anchor mode", description = "Where the compass is anchored.", section = placement, position = 0)
	default AnchorMode anchorMode()
	{
		return AnchorMode.PLAYER_FEET;
	}

	@Range(min = 1, max = 128)
	@Units("tiles")
	@ConfigItem(keyName = "renderDistance", name = "Render distance", description = "Inner ring radius in world tiles.", section = placement, position = 1)
	default int renderDistance()
	{
		return 2;
	}

	@Range(min = -512, max = 512)
	@ConfigItem(keyName = "heightOffset", name = "Render height offset", description = "World units above the anchor; negative values lower the compass.", section = placement, position = 2)
	default int heightOffset()
	{
		return 0;
	}

	@ConfigItem(keyName = "hideCardinals", name = "Hide cardinal letters", description = "Hide N/E/S/W letters without hiding their connectors.", section = labels, position = 3)
	default boolean hideCardinals()
	{
		return false;
	}

	@ConfigItem(keyName = "hideDiagonals", name = "Hide diagonal letters", description = "Hide NE/SE/SW/NW letters without hiding their connectors.", section = labels, position = 4)
	default boolean hideDiagonals()
	{
		return false;
	}

	@ConfigItem(keyName = "letterRotation", name = "Letter rotation mode", description = "Keep text upright or point the top of each letter outward.", section = labels, position = 5)
	default LetterRotation letterRotation()
	{
		return LetterRotation.ALWAYS_UP;
	}

	@ConfigItem(keyName = "useCustomFont", name = "Use Custom font", description = "Use the custom font below instead of RuneScape big and small fonts.", section = labels, position = 6)
	default boolean useCustomFont()
	{
		return false;
	}

	@ConfigItem(keyName = "customFont", name = "Custom font", description = "Font family, main-letter size and style. Used only when Use Custom font is enabled; diagonals are half-size.", section = labels, position = 7)
	default FontType customFont()
	{
		return new FontType().withFamily("SansSerif").withSize(32);
	}

	@ConfigItem(keyName = "showCardinalConnectors", name = "Show cardinal connectors", description = "Show the four short N/E/S/W spokes.", section = connectors, position = 8)
	default boolean showCardinalConnectors()
	{
		return true;
	}

	@ConfigItem(keyName = "showDiagonalConnectors", name = "Show diagonal connectors", description = "Show the four short NE/SE/SW/NW spokes.", section = connectors, position = 9)
	default boolean showDiagonalConnectors()
	{
		return true;
	}

	@ConfigItem(keyName = "connectorDirectionColors", name = "Connector lines use direction colors", description = "Use matching direction colors; otherwise use Base color.", section = connectors, position = 10)
	default boolean connectorDirectionColors()
	{
		return true;
	}

	@Range(min = 1, max = 12)
	@Units("px")
	@ConfigItem(keyName = "connectorThickness", name = "Connector line thickness", description = "Screen-pixel width of all connector lines.", section = connectors, position = 11)
	default int connectorThickness()
	{
		return 3;
	}

	@ConfigItem(keyName = "showInnerRing", name = "Show inner ring", description = "Draw the inner ring at the render distance.", section = rings, position = 12)
	default boolean showInnerRing()
	{
		return true;
	}

	@ConfigItem(keyName = "showOuterRing", name = "Show outer ring", description = "Draw the outer boundary around the letter band.", section = rings, position = 13)
	default boolean showOuterRing()
	{
		return true;
	}

	@Range(min = 1, max = 12)
	@Units("px")
	@ConfigItem(keyName = "innerThickness", name = "Inner circle thickness", description = "Screen-pixel width of the inner circle.", section = rings, position = 14)
	default int innerThickness()
	{
		return 3;
	}

	@Range(min = 1, max = 12)
	@Units("px")
	@ConfigItem(keyName = "outerThickness", name = "Outer circle thickness", description = "Screen-pixel width of the outer circle.", section = rings, position = 15)
	default int outerThickness()
	{
		return 3;
	}

	@Range(min = 0, max = 128)
	@Units("px")
	@ConfigItem(keyName = "ringPadding", name = "Padding between inner and outer circle", description = "Extra screen-pixel padding on each side of the letter band.", section = rings, position = 16)
	default int ringPadding()
	{
		return 4;
	}

	@ConfigItem(keyName = "drawAbovePlayer", name = "Draw above player", description = "Allow the compass to overlap your player. Off masks drawing out of your model silhouette.", section = appearance, position = 17)
	default boolean drawAbovePlayer()
	{
		return false;
	}

	@Range(min = 0, max = 100)
	@Units("%")
	@ConfigItem(keyName = "overallOpacity", name = "Overall opacity", description = "Multiply each configured color's alpha by this percentage.", section = appearance, position = 18)
	default int overallOpacity()
	{
		return 75;
	}

	@Alpha
	@ConfigItem(keyName = "currentColor", name = "Current direction color", description = "Nearest cardinal to the direction the camera looks toward.", section = colors, position = 19)
	default Color currentColor()
	{
		return new Color(255, 0, 0, 255);
	}

	@Alpha
	@ConfigItem(keyName = "oppositeColor", name = "Opposite direction color", description = "Cardinal opposite the current cardinal.", section = colors, position = 20)
	default Color oppositeColor()
	{
		return new Color(0, 255, 0, 255);
	}

	@Alpha
	@ConfigItem(keyName = "leftColor", name = "Left adjacent direction color", description = "Cardinal 90 degrees counterclockwise from the current cardinal.", section = colors, position = 21)
	default Color leftColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "rightColor", name = "Right adjacent direction color", description = "Cardinal 90 degrees clockwise from the current cardinal.", section = colors, position = 22)
	default Color rightColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "currentSubColor", name = "Current subdirection color", description = "Nearest diagonal to the camera heading; exact ties choose clockwise.", section = colors, position = 23)
	default Color currentSubColor()
	{
		return new Color(255, 128, 128, 128);
	}

	@Alpha
	@ConfigItem(keyName = "oppositeSubColor", name = "Opposite subdirection color", description = "Diagonal opposite the current diagonal.", section = colors, position = 24)
	default Color oppositeSubColor()
	{
		return new Color(128, 255, 128, 128);
	}

	@Alpha
	@ConfigItem(keyName = "leftSubColor", name = "Left adjacent subdirection color", description = "Diagonal 90 degrees counterclockwise from the current diagonal.", section = colors, position = 25)
	default Color leftSubColor()
	{
		return new Color(255, 255, 255, 128);
	}

	@Alpha
	@ConfigItem(keyName = "rightSubColor", name = "Right adjacent subdirection color", description = "Diagonal 90 degrees clockwise from the current diagonal.", section = colors, position = 26)
	default Color rightSubColor()
	{
		return new Color(255, 255, 255, 128);
	}

	@Alpha
	@ConfigItem(keyName = "baseColor", name = "Base color", description = "Connector color when direction colors are disabled.", section = colors, position = 27)
	default Color baseColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "innerColor", name = "Inner circle color", description = "Color of the inner ring.", section = colors, position = 28)
	default Color innerColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "outerColor", name = "Outer circle color", description = "Color of the outer ring.", section = colors, position = 29)
	default Color outerColor()
	{
		return Color.WHITE;
	}
}
