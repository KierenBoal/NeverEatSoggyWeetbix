package com.nevereatsoggyweetbix;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Model;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class NeverEatSoggyWeetbixOverlay extends Overlay
{
	private static final BasicStroke[] STROKES = new BasicStroke[12];

	static
	{
		for (int i = 0; i < STROKES.length; i++)
		{
			STROKES[i] = new BasicStroke(i + 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		}
	}

	private final Client client;
	private final NeverEatSoggyWeetbixConfig config;
	private final CompassGeometry geometry = new CompassGeometry();
	private final ActorCompassMask actorMask = new ActorCompassMask();
	private final NpcOcclusion npcOcclusion;
	private final PlayerOcclusion playerOcclusion;
	private final CompassTextLayer textLayer = new CompassTextLayer();
	private final Shape[] placedGlyphs = new Shape[8];
	private final Color[] letterColors = new Color[8];
	private final CompassLabel[] labels = new CompassLabel[8];
	private volatile boolean fontsDirty = true;
	private volatile boolean visible = true;
	private FontRenderContext fontContext;
	private double letterFootprint;
	private double labelClearance;
	private BufferedImage buffer;
	private int viewportWidth;
	private int viewportHeight;

	@Inject
	NeverEatSoggyWeetbixOverlay(Client client, NeverEatSoggyWeetbixConfig config)
	{
		this.client = client;
		this.config = config;
		npcOcclusion = new NpcOcclusion(client);
		playerOcclusion = new PlayerOcclusion(client);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!visible)
		{
			return null;
		}
		Player player = client.getLocalPlayer();
		if (client.getGameState() != GameState.LOGGED_IN || player == null || config.overallOpacity() <= 0)
		{
			return null;
		}
		Rectangle viewport = new Rectangle(client.getViewportXOffset(), client.getViewportYOffset(),
			client.getViewportWidth(), client.getViewportHeight());
		if (viewport.isEmpty())
		{
			return null;
		}
		if (viewport.width != viewportWidth || viewport.height != viewportHeight)
		{
			if (buffer != null)
			{
				buffer.flush();
				buffer = null;
			}
			textLayer.clear();
			viewportWidth = viewport.width;
			viewportHeight = viewport.height;
		}
		Model model = !config.drawAbovePlayer() || config.anchorMode() == AnchorMode.PLAYER_HEAD ? player.getModel() : null;
		CompassAnchor anchor = CompassAnchor.resolve(client, player, model, config, viewport);
		if (anchor == null)
		{
			return null;
		}
		updateFonts(graphics);
		updateGeometry(geometry, anchor);
		HighlightCalculation highlight = config.highlightCalculation();
		CompassProjection playerProjection = highlight == HighlightCalculation.FACING_PLAYER
			|| highlight == HighlightCalculation.FROM_PLAYER_TO_MOUSE
			? CompassProjection.create(client, player.getWorldView()) : null;
		double heading = highlight.heading(anchor.projection, player.getCurrentOrientation(), playerProjection);
		if (highlight == HighlightCalculation.FROM_PLAYER_TO_MOUSE)
		{
			Point mouse = client.getMouseCanvasPosition();
			WorldView worldView = player.getWorldView();
			LocalPoint location = player.getLocalLocation();
			if (mouse != null && viewport.contains(mouse.getX(), mouse.getY()) && !client.isMenuOpen()
				&& worldView != null && location != null && worldView.contains(location))
			{
				Integer ground = CompassAnchor.groundHeight(client, worldView, location);
				if (ground != null)
				{
					heading = HighlightCalculation.mouseHeading(anchor.projection, playerProjection,
						location.getX(), location.getY(), (double) ground - player.getAnimationHeightOffset(),
						mouse.getX(), mouse.getY());
				}
			}
		}
		if (highlight == HighlightCalculation.DESTINATION_TILE)
		{
			heading = HighlightCalculation.destinationHeading(client, player, anchor.projection, viewport);
		}
		geometry.updateIndicators(anchor, heading, config);
		prepareLabels(geometry);
		Rectangle bounds = drawingBounds(viewport, geometry, heading);
		if (bounds.isEmpty())
		{
			return null;
		}
		Graphics2D target;
		boolean maskPlayer = !config.drawAbovePlayer() && model != null;
		int npcsToMask = npcOcclusion.select(bounds, config);
		int playersToMask = playerOcclusion.select(bounds, config);
		boolean masked = maskPlayer || npcsToMask > 0 || playersToMask > 0;
		if (masked)
		{
			ensureBuffer(bounds.width, bounds.height);
			target = buffer.createGraphics();
			target.setComposite(AlphaComposite.Clear);
			target.fillRect(0, 0, bounds.width, bounds.height);
			target.setComposite(AlphaComposite.SrcOver);
			target.translate(-bounds.x, -bounds.y);
		}
		else
		{
			target = (Graphics2D) graphics.create();
		}
		try
		{
			target.clip(bounds);
			target.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			drawCompass(target, heading, geometry);
			if (maskPlayer)
			{
				actorMask.erase(target, client, player, model, bounds, CompassProjection.create(client, player.getWorldView()));
			}
			if (npcsToMask > 0)
			{
				npcOcclusion.eraseSelected(target, bounds, actorMask);
			}
			if (playersToMask > 0)
			{
				playerOcclusion.eraseSelected(target, bounds, actorMask);
			}
		}
		finally
		{
			npcOcclusion.discardSelection();
			playerOcclusion.discardSelection();
			target.dispose();
		}
		if (masked)
		{
			// Draw only the cleared region when a previously larger buffer is reused.
			graphics.drawImage(buffer, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height,
				0, 0, bounds.width, bounds.height, null);
		}
		return null;
	}

	void drawCompass(Graphics2D graphics, double heading, CompassGeometry geometry)
	{
		drawCircles(graphics, geometry);
		drawConnectors(graphics, heading, geometry);
		drawLetters(graphics, heading);
		if (config.directionalIndicatorEnabled())
		{
			drawIndicator(graphics, geometry.indicator, config.directionalIndicatorFillColor(), config.directionalIndicatorLineColor());
		}
		if (config.oppositeIndicator())
		{
			drawIndicator(graphics, geometry.oppositeIndicator, config.oppositeIndicatorFillColor(), config.oppositeIndicatorLineColor());
		}
	}

	private void drawIndicator(Graphics2D graphics, DirectionalIndicatorGeometry indicator, Color fill, Color line)
	{
		if (indicator.path.getCurrentPoint() == null)
		{
			return;
		}
		drawIndicatorDropShadow(graphics, indicator, fill, line);
		graphics.setColor(opacity(fill));
		graphics.fill(indicator.path);
		graphics.setStroke(stroke(config.directionalIndicatorLineThickness()));
		graphics.setColor(opacity(line));
		graphics.draw(indicator.path);
	}

	private void drawIndicatorDropShadow(Graphics2D graphics, DirectionalIndicatorGeometry indicator, Color fill, Color line)
	{
		int offset = shadowOffset();
		Color color = opacity(config.dropShadowColor());
		if (!config.enableDropShadow() || offset == 0 || color.getAlpha() == 0)
		{
			return;
		}
		Area foreground = new Area();
		if (fill.getAlpha() > 0)
		{
			foreground.add(new Area(indicator.path));
		}
		if (line.getAlpha() > 0)
		{
			foreground.add(new Area(stroke(config.directionalIndicatorLineThickness()).createStrokedShape(indicator.path)));
		}
		Area shadow = foreground.createTransformedArea(AffineTransform.getTranslateInstance(offset, offset));
		// Keep shadows out of translucent foreground, and leave outline-only centers clear.
		shadow.subtract(foreground);
		graphics.setColor(color);
		graphics.fill(shadow);
	}

	private void drawCircles(Graphics2D graphics, CompassGeometry geometry)
	{
		drawRingDropShadows(graphics, geometry);
		if (config.showInnerRing())
		{
			graphics.setStroke(stroke(config.innerThickness()));
			graphics.setColor(opacity(config.innerColor()));
			graphics.draw(geometry.inner);
		}
		if (config.showOuterRing())
		{
			graphics.setStroke(stroke(config.outerThickness()));
			graphics.setColor(opacity(config.outerColor()));
			graphics.draw(geometry.outer);
		}
	}

	private void drawRingDropShadows(Graphics2D graphics, CompassGeometry geometry)
	{
		if (!config.enableDropShadow())
		{
			return;
		}
		int offset = shadowOffset();
		Graphics2D shadow = (Graphics2D) graphics.create();
		try
		{
			shadow.translate(offset, offset);
			if (config.showInnerRing())
			{
				shadow.setStroke(stroke(config.innerThickness()));
				shadow.setColor(opacity(config.dropShadowColor()));
				shadow.draw(geometry.inner);
			}
			if (config.showOuterRing())
			{
				shadow.setStroke(stroke(config.outerThickness()));
				shadow.setColor(opacity(config.dropShadowColor()));
				shadow.draw(geometry.outer);
			}
		}
		finally
		{
			shadow.dispose();
		}
	}

	private void drawConnectors(Graphics2D graphics, double heading, CompassGeometry geometry)
	{
		if (config.enableDropShadow())
		{
			Graphics2D shadow = (Graphics2D) graphics.create();
			try
			{
				int offset = shadowOffset();
				shadow.translate(offset, offset);
				shadow.setStroke(stroke(config.connectorThickness()));
				shadow.setColor(opacity(config.dropShadowColor()));
				for (CompassDirection direction : CompassDirection.ALL)
				{
					if (direction.connectorVisible(config, heading))
					{
						shadow.draw(geometry.spokes[direction.ordinal()]);
					}
				}
			}
			finally
			{
				shadow.dispose();
			}
		}
		graphics.setStroke(stroke(config.connectorThickness()));
		for (CompassDirection direction : CompassDirection.ALL)
		{
			if (direction.connectorVisible(config, heading))
			{
				graphics.setColor(opacity(config.connectorDirectionColors() ? direction.color(config, heading) : config.baseColor()));
				graphics.draw(geometry.spokes[direction.ordinal()]);
			}
		}
	}

	void prepareLabels(CompassGeometry geometry)
	{
		Arrays.fill(placedGlyphs, null);
		boolean outward = config.letterRotation() == LetterRotation.ROTATION;
		boolean curved = outward && geometry.prepareLabelRing();
		for (CompassDirection direction : CompassDirection.ALL)
		{
			int index = direction.ordinal();
			Point2D.Double label = geometry.labels[index];
			if (label == null || !direction.lettersVisible(config) || labels[index].bounds.isEmpty())
			{
				continue;
			}
			placedGlyphs[index] = curved ? labels[index].curved(geometry, direction)
				: labels[index].straight(label, outward ? geometry.rotations[index] : 0);
		}
	}

	private void drawLetters(Graphics2D graphics, double heading)
	{
		for (CompassDirection direction : CompassDirection.ALL)
		{
			letterColors[direction.ordinal()] = opacity(direction.color(config, heading));
		}
		if (config.enableDropShadow())
		{
			textLayer.draw(graphics, placedGlyphs, letterColors, opacity(config.dropShadowColor()), shadowOffset());
		}
		else
		{
			for (int i = 0; i < placedGlyphs.length; i++)
			{
				if (placedGlyphs[i] != null)
				{
					graphics.setColor(letterColors[i]);
					graphics.fill(placedGlyphs[i]);
				}
			}
		}
	}

	void updateFonts(Graphics2D graphics)
	{
		FontRenderContext context = graphics.getFontRenderContext();
		if (!fontsDirty && context.equals(fontContext))
		{
			return;
		}
		fontsDirty = false;
		fontContext = context;
		CompassFonts fonts = CompassFonts.fromConfig(config);
		String[] text = DirectionLabels.resolve(config);
		letterFootprint = 0;
		labelClearance = 0;
		for (CompassDirection direction : CompassDirection.ALL)
		{
			int index = direction.ordinal();
			Font font = direction.diagonal() ? fonts.diagonal : fonts.cardinal;
			char[] characters = text[index].toCharArray();
			labels[index] = new CompassLabel(font.layoutGlyphVector(context, characters, 0, characters.length, Font.LAYOUT_LEFT_TO_RIGHT));
			Rectangle2D bounds = labels[index].bounds;
			if (direction.lettersVisible(config) && !bounds.isEmpty())
			{
				labelClearance = Math.max(labelClearance, Math.hypot(bounds.getWidth(), bounds.getHeight()));
				letterFootprint = Math.max(letterFootprint,
					config.letterRotation() == LetterRotation.ROTATION ? bounds.getHeight()
						: Math.hypot(bounds.getWidth(), bounds.getHeight()));
			}
		}
	}

	double labelBandWidth()
	{
		return letterFootprint + Math.max(0, Math.min(128, config.ringPadding())) * 2
			+ (strokeWidth(config.innerThickness()) + strokeWidth(config.outerThickness())) / 2.0;
	}

	void updateGeometry(CompassGeometry geometry, CompassAnchor anchor)
	{
		double radius = Math.max(0, Math.min(128, config.renderDistance())) * Perspective.LOCAL_TILE_SIZE;
		if (config.letterRotation() == LetterRotation.ROTATION)
		{
			double clearance = labelClearance + labelBandWidth() - letterFootprint;
			geometry.updateCurvedLabels(anchor, radius, clearance, labelBandWidth());
		}
		else
		{
			geometry.update(anchor, radius, labelBandWidth());
		}
	}

	Rectangle drawingBounds(Rectangle viewport, CompassGeometry geometry, double heading)
	{
		Rectangle2D bounds = null;
		if (config.directionalIndicatorEnabled() && geometry.indicator.path.getCurrentPoint() != null)
		{
			bounds = indicatorBounds(geometry.indicator);
		}
		if (config.oppositeIndicator() && geometry.oppositeIndicator.path.getCurrentPoint() != null)
		{
			bounds = union(bounds, indicatorBounds(geometry.oppositeIndicator));
		}
		if (config.showInnerRing() && geometry.inner.getCurrentPoint() != null)
		{
			bounds = union(bounds, geometry.inner.getBounds2D());
		}
		if (config.showOuterRing() && geometry.outer.getCurrentPoint() != null)
		{
			bounds = union(bounds, geometry.outer.getBounds2D());
		}
		if (config.enableDropShadow())
		{
			int offset = shadowOffset();
			if (config.showInnerRing() && geometry.inner.getCurrentPoint() != null)
			{
				bounds = union(bounds, translateBounds(geometry.inner.getBounds2D(), offset));
			}
			if (config.showOuterRing() && geometry.outer.getCurrentPoint() != null)
			{
				bounds = union(bounds, translateBounds(geometry.outer.getBounds2D(), offset));
			}
		}
		for (CompassDirection direction : CompassDirection.ALL)
		{
			int index = direction.ordinal();
			if (direction.connectorVisible(config, heading) && geometry.spokes[index].getCurrentPoint() != null)
			{
				bounds = union(bounds, geometry.spokes[index].getBounds2D());
				if (config.enableDropShadow())
				{
					bounds = union(bounds, translateBounds(geometry.spokes[index].getBounds2D(), shadowOffset()));
				}
			}
			Shape label = placedGlyphs[index];
			if (label != null)
			{
				Rectangle2D labelBounds = label.getBounds2D();
				bounds = union(bounds, labelBounds);
				if (config.enableDropShadow())
				{
					bounds = union(bounds, translateBounds(labelBounds, shadowOffset()));
				}
			}
		}
		if (bounds == null)
		{
			return new Rectangle();
		}
		// Intersect doubles first so extreme projections cannot overflow integer bounds.
		double minX = Math.max(viewport.getMinX(), Math.floor(bounds.getMinX() - 7));
		double minY = Math.max(viewport.getMinY(), Math.floor(bounds.getMinY() - 7));
		double maxX = Math.min(viewport.getMaxX(), Math.ceil(bounds.getMaxX() + 7));
		double maxY = Math.min(viewport.getMaxY(), Math.ceil(bounds.getMaxY() + 7));
		return maxX > minX && maxY > minY
			? new Rectangle((int) minX, (int) minY, (int) (maxX - minX), (int) (maxY - minY)) : new Rectangle();
	}

	private Rectangle2D indicatorBounds(DirectionalIndicatorGeometry indicator)
	{
		Rectangle2D shapeBounds = indicator.path.getBounds2D();
		double halfStroke = strokeWidth(config.directionalIndicatorLineThickness()) / 2.0;
		Rectangle2D bounds = new Rectangle2D.Double(shapeBounds.getX() - halfStroke, shapeBounds.getY() - halfStroke,
			shapeBounds.getWidth() + halfStroke * 2, shapeBounds.getHeight() + halfStroke * 2);
		return config.enableDropShadow() ? union(bounds, translateBounds(bounds, shadowOffset())) : bounds;
	}

	private static Rectangle2D union(Rectangle2D first, Rectangle2D second)
	{
		return first == null ? second : first.createUnion(second);
	}

	private static Rectangle2D translateBounds(Rectangle2D bounds, int offset)
	{
		return new Rectangle2D.Double(bounds.getX() + offset, bounds.getY() + offset,
			bounds.getWidth(), bounds.getHeight());
	}

	private void ensureBuffer(int width, int height)
	{
		if (buffer == null || buffer.getWidth() < width || buffer.getHeight() < height)
		{
			int newWidth = buffer == null ? width : Math.max(width, buffer.getWidth());
			int newHeight = buffer == null ? height : Math.max(height, buffer.getHeight());
			if (buffer != null)
			{
				buffer.flush();
			}
			buffer = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB_PRE);
		}
	}

	private Color opacity(Color color)
	{
		return CompassDirection.withOpacity(color, config.overallOpacity());
	}

	private static int strokeWidth(int width)
	{
		return Math.max(1, Math.min(12, width));
	}

	private static BasicStroke stroke(int width)
	{
		return STROKES[strokeWidth(width) - 1];
	}

	void invalidateFonts()
	{
		fontsDirty = true;
	}

	private int shadowOffset()
	{
		return Math.max(0, Math.min(128, config.dropShadowOffset()));
	}

	void toggleVisibility()
	{
		visible = !visible;
	}

	void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	void start()
	{
		npcOcclusion.start();
		playerOcclusion.start();
	}

	NpcOcclusion npcOcclusion()
	{
		return npcOcclusion;
	}

	PlayerOcclusion playerOcclusion()
	{
		return playerOcclusion;
	}

	void clear()
	{
		geometry.indicator.path.reset();
		geometry.oppositeIndicator.path.reset();
		if (buffer != null)
		{
			buffer.flush();
			buffer = null;
		}
		actorMask.clear();
		npcOcclusion.clear();
		playerOcclusion.clear();
		textLayer.clear();
		viewportWidth = 0;
		viewportHeight = 0;
		Arrays.fill(placedGlyphs, null);
		fontContext = null;
		fontsDirty = true;
	}
}
