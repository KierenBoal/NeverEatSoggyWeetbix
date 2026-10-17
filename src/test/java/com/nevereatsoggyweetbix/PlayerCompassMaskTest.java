package com.nevereatsoggyweetbix;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerCompassMaskTest
{
	private static BufferedImage filledImage()
	{
		BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(new Color(255, 0, 0, 128));
		graphics.fillRect(0, 0, 40, 40);
		graphics.dispose();
		return image;
	}

	@Test
	public void projectedFacesRemoveCoverageButPreserveGapsInSilhouette()
	{
		BufferedImage image = filledImage();
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.setColor(new Color(255, 0, 0, 128));
			new PlayerCompassMask().eraseFaces(graphics, new Rectangle(0, 0, 40, 40),
				new int[]{2, 14, 2, 24, 36, 36}, new int[]{2, 2, 36, 2, 2, 36},
				2, new int[]{0, 3}, new int[]{1, 4}, new int[]{2, 5}, null, null);
			assertEquals(0, image.getRGB(4, 8) >>> 24);
			assertEquals(0, image.getRGB(34, 8) >>> 24);
			assertEquals(128, image.getRGB(20, 8) >>> 24);
			assertEquals(128, image.getRGB(20, 38) >>> 24);
			// Masking must not leak its composite or color back into later overlays.
			graphics.fillRect(20, 20, 1, 1);
			assertEquals(192, image.getRGB(20, 20) >>> 24);
		}
		finally
		{
			graphics.dispose();
		}
	}

	@Test
	public void transparentHiddenInvalidAndOffscreenFacesDoNotMask()
	{
		for (int variant = 0; variant < 4; variant++)
		{
			BufferedImage image = filledImage();
			Graphics2D graphics = image.createGraphics();
			try
			{
				int[] x = {2, 36, variant == 2 ? Integer.MIN_VALUE : 2};
				new PlayerCompassMask().eraseFaces(graphics,
					variant == 3 ? new Rectangle(50, 50, 10, 10) : new Rectangle(0, 0, 40, 40),
					x, new int[]{2, 2, 36}, 1, new int[]{0}, new int[]{1}, new int[]{2},
					variant == 1 ? new int[]{-2} : null, variant == 0 ? new byte[]{(byte) 255} : null);
				assertEquals(128, image.getRGB(4, 8) >>> 24);
			}
			finally
			{
				graphics.dispose();
			}
		}
	}

	@Test
	public void translatedBufferUsesCanvasCoordinatesAndClearsSharedEdges()
	{
		BufferedImage image = filledImage();
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.translate(-100, -200);
			new PlayerCompassMask().eraseFaces(graphics, new Rectangle(100, 200, 40, 40),
				new int[]{104, 136, 136, 104}, new int[]{204, 204, 236, 236},
				2, new int[]{0, 0}, new int[]{1, 2}, new int[]{2, 3}, null, null);
			for (int y = 5; y < 35; y++)
			{
				for (int x = 5; x < 35; x++)
				{
					assertEquals(0, image.getRGB(x, y) >>> 24);
				}
			}
			assertEquals(128, image.getRGB(1, 1) >>> 24);
		}
		finally
		{
			graphics.dispose();
		}
	}
}
