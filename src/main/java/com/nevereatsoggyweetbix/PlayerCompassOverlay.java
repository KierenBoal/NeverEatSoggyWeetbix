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
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class PlayerCompassOverlay extends Overlay
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
	private final PlayerCompassConfig config;
	private final CompassGeometry geometry = new CompassGeometry();
	private final ActorCompassMask actorMask = new ActorCompassMask();
	private final NpcOcclusion npcOcclusion;
	private final PlayerOcclusion playerOcclusion;
	private final CompassTextLayer textLayer = new CompassTextLayer();
	private final Shape[] placedGlyphs = new Shape[8];
	private final Color[] letterColors = new Color[8];
	private final Shape[] glyphs = new Shape[8];
	private final Rectangle2D[] glyphBounds = new Rectangle2D[8];
	private volatile boolean fontsDirty = true;
	private FontRenderContext fontContext;
	private double letterFootprint;
	private BufferedImage buffer;
	private int viewportWidth;
	private int viewportHeight;

	@Inject
	PlayerCompassOverlay(Client client, PlayerCompassConfig config)
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
		double bandWidth = letterFootprint + Math.max(0, Math.min(128, config.ringPadding())) * 2
			+ (strokeWidth(config.innerThickness()) + strokeWidth(config.outerThickness())) / 2.0;
		double radius = Math.max(0, Math.min(128, config.renderDistance())) * Perspective.LOCAL_TILE_SIZE;
		geometry.update(anchor, radius, bandWidth);
		Rectangle bounds = drawingBounds(viewport);
		if (bounds.isEmpty())
		{
			return null;
		}
		HighlightCalculation highlight = config.highlightCalculation();
		CompassProjection playerProjection = highlight == HighlightCalculation.FACING_PLAYER
			? CompassProjection.create(client, player.getWorldView()) : null;
		double heading = highlight.heading(anchor.projection, player.getCurrentOrientation(), playerProjection);
		Graphics2D target;
		boolean maskPlayer = !config.drawAbovePlayer() && model != null;
		boolean masked = maskPlayer || (!config.drawAboveNpcs() && config.maxNpcs() > 0)
			|| (!config.drawAbovePlayers() && config.maxPlayers() > 0);
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
			npcOcclusion.erase(target, bounds, config, actorMask);
			playerOcclusion.erase(target, bounds, config, actorMask);
		}
		finally
		{
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
		drawLetters(graphics, heading, geometry);
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
		if (!config.enableRingDropShadow())
		{
			return;
		}
		int offset = ringShadowOffset();
		Graphics2D shadow = (Graphics2D) graphics.create();
		try
		{
			shadow.translate(offset, offset);
			if (config.showInnerRing())
			{
				shadow.setStroke(stroke(config.innerThickness()));
				shadow.setColor(opacity(config.innerRingDropShadowColor()));
				shadow.draw(geometry.inner);
			}
			if (config.showOuterRing())
			{
				shadow.setStroke(stroke(config.outerThickness()));
				shadow.setColor(opacity(config.outerRingDropShadowColor()));
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
		graphics.setStroke(stroke(config.connectorThickness()));
		for (CompassDirection direction : CompassDirection.ALL)
		{
			if (direction.connectorVisible(config))
			{
				graphics.setColor(opacity(config.connectorDirectionColors() ? direction.color(config, heading) : config.baseColor()));
				graphics.draw(geometry.spokes[direction.ordinal()]);
			}
		}
	}

	private void drawLetters(Graphics2D graphics, double heading, CompassGeometry geometry)
	{
		Arrays.fill(placedGlyphs, null);
		for (CompassDirection direction : CompassDirection.ALL)
		{
			int index = direction.ordinal();
			Point2D.Double label = geometry.labels[index];
			if (label == null || !direction.lettersVisible(config))
			{
				continue;
			}
			AffineTransform transform = AffineTransform.getTranslateInstance(label.x, label.y);
			if (config.letterRotation() == LetterRotation.ROTATION)
			{
				transform.rotate(geometry.rotations[index]);
			}
			Rectangle2D textBounds = glyphBounds[index];
			transform.translate(-textBounds.getCenterX(), -textBounds.getCenterY());
			placedGlyphs[index] = transform.createTransformedShape(glyphs[index]);
			letterColors[index] = opacity(direction.color(config, heading));
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
		letterFootprint = 0;
		for (CompassDirection direction : CompassDirection.ALL)
		{
			int index = direction.ordinal();
			Font font = direction.diagonal() ? fonts.diagonal : fonts.cardinal;
			glyphs[index] = font.createGlyphVector(context, direction.name()).getOutline();
			glyphBounds[index] = glyphs[index].getBounds2D();
			letterFootprint = Math.max(letterFootprint,
				Math.hypot(glyphBounds[index].getWidth(), glyphBounds[index].getHeight()));
		}
	}

	private Rectangle drawingBounds(Rectangle viewport)
	{
		Rectangle2D bounds = null;
		if (config.showInnerRing() && geometry.inner.getCurrentPoint() != null)
		{
			bounds = geometry.inner.getBounds2D();
		}
		if (config.showOuterRing() && geometry.outer.getCurrentPoint() != null)
		{
			bounds = union(bounds, geometry.outer.getBounds2D());
		}
		if (config.enableRingDropShadow())
		{
			int offset = ringShadowOffset();
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
			if (direction.connectorVisible(config) && geometry.spokes[index].getCurrentPoint() != null)
			{
				bounds = union(bounds, geometry.spokes[index].getBounds2D());
			}
			Point2D.Double label = geometry.labels[index];
			if (direction.lettersVisible(config) && label != null)
			{
				bounds = union(bounds, new Rectangle2D.Double(label.x - letterFootprint / 2,
					label.y - letterFootprint / 2, letterFootprint, letterFootprint));
				if (config.enableDropShadow())
				{
					bounds = union(bounds, new Rectangle2D.Double(label.x - letterFootprint / 2 + shadowOffset(),
						label.y - letterFootprint / 2 + shadowOffset(), letterFootprint, letterFootprint));
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

	private int ringShadowOffset()
	{
		return Math.max(0, Math.min(128, config.ringDropShadowOffset()));
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
