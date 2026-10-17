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

	@ConfigSection(name = "Placement", description = "Placement settings", position = 0, closedByDefault = true)
	String placement = "placement";

	@ConfigSection(name = "Labels", description = "Labels settings", position = 1, closedByDefault = true)
	String labels = "labels";

	@ConfigSection(name = "Connectors", description = "Connectors settings", position = 2, closedByDefault = true)
	String connectors = "connectors";

	@ConfigSection(name = "Rings", description = "Rings settings", position = 3, closedByDefault = true)
	String rings = "rings";

	@ConfigSection(name = "Appearance", description = "Appearance settings", position = 4, closedByDefault = true)
	String appearance = "appearance";

	@ConfigSection(name = "Colors", description = "Colors settings", position = 5, closedByDefault = true)
	String colors = "colors";

	@ConfigItem(keyName = "anchorMode", name = "Anchor mode", description = "Where the compass is anchored.", section = placement, position = 0)
	default AnchorMode anchorMode()
	{
		return AnchorMode.PLAYER_FEET;
	}

	@Range(min = 0, max = 128)
	@ConfigItem(keyName = "renderDistance", name = "Ring size in tiles", description = "Inner ring radius in world tiles, including fractional values from 0 to 128.", section = placement, position = 1)
	default double renderDistance()
	{
		return 1.5;
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

	@ConfigItem(keyName = "useCustomFont", name = "Use custom fonts", description = "Use the custom cardinal and sub direction fonts below instead of the RuneScape fonts.", section = labels, position = 6)
	default boolean useCustomFont()
	{
		return false;
	}

	@ConfigItem(keyName = "customFont", name = "Cardinal letters font", description = "Font family, size and style for cardinal letters. Used only when Use Custom font is enabled.", section = labels, position = 7)
	default FontType customFont()
	{
		return new FontType().withFamily("SansSerif").withSize(CompassFonts.DEFAULT_MAIN_SIZE);
	}

	@ConfigItem(keyName = "subDirectionFont", name = "Sub direction letters font", description = "Font family, size and style for sub direction letters. Used only when Use Custom font is enabled.", section = labels, position = 8)
	default FontType subDirectionFont()
	{
		return new FontType().withFamily("SansSerif").withSize(CompassFonts.DEFAULT_SUB_SIZE);
	}

	@ConfigItem(keyName = "showCardinalConnectors", name = "Show cardinal connectors", description = "Show the four short N/E/S/W spokes.", section = connectors, position = 8)
	default boolean showCardinalConnectors()
	{
		return true;
	}

	@ConfigItem(keyName = "showDiagonalConnectors", name = "Show diagonal connectors", description = "Show the four short NE/SE/SW/NW spokes.", section = connectors, position = 9)
	default boolean showDiagonalConnectors()
	{
		return false;
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
		return 1;
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
		return 1;
	}

	@Range(min = 1, max = 12)
	@Units("px")
	@ConfigItem(keyName = "outerThickness", name = "Outer circle thickness", description = "Screen-pixel width of the outer circle.", section = rings, position = 15)
	default int outerThickness()
	{
		return 1;
	}

	@Range(min = 0, max = 128)
	@Units("px")
	@ConfigItem(keyName = "ringPadding", name = "Padding between inner and outer circle", description = "Extra padding on each side of the letter band, measured in pixels at the anchor's horizontal scale. Foreshortens with the floor.", section = rings, position = 16)
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

	@ConfigItem(keyName = "drawAboveNpcs", name = "Draw above NPCs", description = "Draw over NPCs. Turn off to mask the compass under NPC silhouettes, using additional CPU.", section = appearance, position = 19)
	default boolean drawAboveNpcs()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(keyName = "maxNpcs", name = "Max NPCs to calculate", description = "Maximum NPC model calculations per frame when drawing under NPCs. Largest screen overlaps are prioritized; 0 skips NPC masking.", section = appearance, position = 20)
	default int maxNpcs()
	{
		return 12;
	}

	@ConfigItem(keyName = "drawAbovePlayers", name = "Draw above players", description = "Draw over other players. Turn off to mask under their silhouettes, using additional CPU. Your own player is controlled separately.", section = appearance, position = 21)
	default boolean drawAbovePlayers()
	{
		return true;
	}

	@Range(min = 0)
	@ConfigItem(keyName = "maxPlayers", name = "Max players to calculate", description = "Maximum other-player model calculations per frame. Largest screen overlaps are prioritized; 0 skips masking. Independent of the NPC limit.", section = appearance, position = 22)
	default int maxPlayers()
	{
		return 12;
	}

	@ConfigItem(keyName = "highlightCalculation", name = "Highlight calculation", description = "Choose camera heading, the player's current yaw, or fixed north to assign direction colors.", section = appearance, position = 23)
	default HighlightCalculation highlightCalculation()
	{
		return HighlightCalculation.FACING_CAMERA;
	}

	@Alpha
	@ConfigItem(keyName = "currentColor", name = "Current direction color", description = "Nearest cardinal to the selected highlight heading.", section = colors, position = 19)
	default Color currentColor()
	{
		return new Color(65, 135, 175, 255);
	}

	@Alpha
	@ConfigItem(keyName = "currentSubColor", name = "Current sub direction color", description = "Nearest diagonal to the selected highlight heading; exact ties choose clockwise.", section = colors, position = 20)
	default Color currentSubColor()
	{
		return new Color(65, 135, 175, 255);
	}

	@Alpha
	@ConfigItem(keyName = "oppositeColor", name = "Opposite direction color", description = "Cardinal opposite the current cardinal.", section = colors, position = 21)
	default Color oppositeColor()
	{
		return new Color(173, 39, 0, 255);
	}

	@Alpha
	@ConfigItem(keyName = "oppositeSubColor", name = "Opposite sub direction color", description = "Diagonal opposite the current diagonal.", section = colors, position = 22)
	default Color oppositeSubColor()
	{
		return new Color(194, 44, 0, 255);
	}

	@Alpha
	@ConfigItem(keyName = "leftColor", name = "Left adjacent direction color", description = "Cardinal 90 degrees counterclockwise from the current cardinal.", section = colors, position = 23)
	default Color leftColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "rightColor", name = "Right adjacent direction color", description = "Cardinal 90 degrees clockwise from the current cardinal.", section = colors, position = 24)
	default Color rightColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "leftSubColor", name = "Left adjacent sub direction color", description = "Diagonal 90 degrees counterclockwise from the current diagonal.", section = colors, position = 25)
	default Color leftSubColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(keyName = "rightSubColor", name = "Right adjacent sub direction color", description = "Diagonal 90 degrees clockwise from the current diagonal.", section = colors, position = 26)
	default Color rightSubColor()
	{
		return Color.WHITE;
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

	@ConfigItem(keyName = "enableDropShadow", name = "Enable drop shadow", description = "Draw a shadow behind letters while keeping shadow pixels out of the foreground text.", section = colors, position = 30)
	default boolean enableDropShadow()
	{
		return true;
	}

	@Range(min = 0, max = 128)
	@Units("px")
	@ConfigItem(keyName = "dropShadowOffset", name = "Drop shadow offset", description = "Screen-pixel offset down and right, independent of letter rotation.", section = colors, position = 31)
	default int dropShadowOffset()
	{
		return 1;
	}

	@Alpha
	@ConfigItem(keyName = "dropShadowColor", name = "Drop shadow color", description = "Letter shadow color. Its alpha is multiplied by overall opacity.", section = colors, position = 32)
	default Color dropShadowColor()
	{
		return Color.BLACK;
	}

	@ConfigItem(keyName = "enableRingDropShadow", name = "Enable ring drop shadow", description = "Draw shadows behind the inner and outer rings.", section = colors, position = 33)
	default boolean enableRingDropShadow()
	{
		return true;
	}

	@Range(min = 0, max = 128)
	@Units("px")
	@ConfigItem(keyName = "ringDropShadowOffset", name = "Ring drop shadow offset", description = "Screen-pixel offset down and right from each ring.", section = colors, position = 34)
	default int ringDropShadowOffset()
	{
		return 1;
	}

	@Alpha
	@ConfigItem(keyName = "innerRingDropShadowColor", name = "Inner ring drop shadow color", description = "Shadow color for the inner ring. Its alpha is multiplied by overall opacity.", section = colors, position = 35)
	default Color innerRingDropShadowColor()
	{
		return Color.BLACK;
	}

	@Alpha
	@ConfigItem(keyName = "outerRingDropShadowColor", name = "Outer ring drop shadow color", description = "Shadow color for the outer ring. Its alpha is multiplied by overall opacity.", section = colors, position = 36)
	default Color outerRingDropShadowColor()
	{
		return Color.BLACK;
	}
}
