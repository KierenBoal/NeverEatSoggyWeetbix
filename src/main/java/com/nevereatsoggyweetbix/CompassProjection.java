package com.nevereatsoggyweetbix;

import java.awt.geom.Point2D;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Projection;
import net.runelite.api.WorldView;

/** Camera-space affine transform followed by RuneLite's perspective divide. */
final class CompassProjection
{
	private static final double NEAR_PLANE = 50;
	private final double[][] matrix;
	private final double centerX;
	private final double centerY;
	private final double scale;

	CompassProjection(double[][] matrix, double centerX, double centerY, double scale)
	{
		this.matrix = matrix;
		this.centerX = centerX;
		this.centerY = centerY;
		this.scale = scale;
	}

	static CompassProjection create(Client client, WorldView worldView)
	{
		if (client.getScale() <= 0)
		{
			return null;
		}
		double[][] matrix;
		if (worldView.isTopLevel())
		{
			boolean gpu = client.isGpu();
			double yaw = gpu ? client.getCameraFpYaw() : client.getCameraYaw() * Perspective.UNIT14;
			double pitch = gpu ? client.getCameraFpPitch() : client.getCameraPitch() * Perspective.UNIT14;
			matrix = cameraMatrix(yaw, pitch,
				gpu ? client.getCameraFpX() : client.getCameraX(),
				gpu ? client.getCameraFpY() : client.getCameraY(),
				gpu ? client.getCameraFpZ() : client.getCameraZ());
		}
		else
		{
			Projection projection = worldView.getCanvasProjection();
			if (projection == null)
			{
				return null;
			}
			// Sample the affine transform in local X, local Y, then world height.
			// A tile-sized basis avoids losing precision to float translations.
			float[] origin = projection.project(0, 0, 0);
			float[][] basis = {
				projection.project(Perspective.LOCAL_TILE_SIZE, 0, 0),
				projection.project(0, 0, Perspective.LOCAL_TILE_SIZE),
				projection.project(0, Perspective.LOCAL_TILE_SIZE, 0)
			};
			matrix = new double[3][4];
			for (int row = 0; row < 3; row++)
			{
				for (int col = 0; col < 3; col++)
				{
					matrix[row][col] = (basis[col][row] - origin[row]) / Perspective.LOCAL_TILE_SIZE;
				}
				matrix[row][3] = origin[row];
			}
		}
		return new CompassProjection(matrix,
			client.getViewportXOffset() + client.getViewportWidth() / 2.0,
			client.getViewportYOffset() + client.getViewportHeight() / 2.0, client.getScale());
	}

	static double[][] cameraMatrix(double yaw, double pitch, double x, double y, double z)
	{
		double sy = Math.sin(yaw), cy = Math.cos(yaw);
		double sp = Math.sin(pitch), cp = Math.cos(pitch);
		double[][] m = {
			{cy, sy, 0, 0},
			{sy * sp, -cy * sp, cp, 0},
			{-sy * cp, cy * cp, sp, 0}
		};
		for (double[] row : m)
		{
			row[3] = -row[0] * x - row[1] * y - row[2] * z;
		}
		return m;
	}

	Point2D.Double project(double x, double y, double height)
	{
		Point2D.Double result = new Point2D.Double();
		return project(x, y, height, result) ? result : null;
	}

	boolean project(double x, double y, double height, Point2D.Double result)
	{
		double depth = transform(2, x, y, height);
		if (!Double.isFinite(depth) || depth < NEAR_PLANE)
		{
			return false;
		}
		double screenX = centerX + transform(0, x, y, height) * scale / depth;
		double screenY = centerY + transform(1, x, y, height) * scale / depth;
		if (!Double.isFinite(screenX) || !Double.isFinite(screenY))
		{
			return false;
		}
		result.setLocation(screenX, screenY);
		return true;
	}

	/** Intersect a screen ray with the horizontal plane at the supplied height. */
	Point2D.Double unproject(double screenX, double screenY, double height)
	{
		if (!(scale > 0))
		{
			return null;
		}
		double u = (screenX - centerX) / scale;
		double v = (screenY - centerY) / scale;
		double a = matrix[0][0] - u * matrix[2][0];
		double b = matrix[0][1] - u * matrix[2][1];
		double c = matrix[1][0] - v * matrix[2][0];
		double d = matrix[1][1] - v * matrix[2][1];
		double e = -(matrix[0][2] - u * matrix[2][2]) * height - matrix[0][3] + u * matrix[2][3];
		double f = -(matrix[1][2] - v * matrix[2][2]) * height - matrix[1][3] + v * matrix[2][3];
		double determinant = a * d - b * c;
		if (!Double.isFinite(determinant) || Math.abs(determinant) < 1e-9)
		{
			return null;
		}
		double x = (e * d - b * f) / determinant;
		double y = (a * f - e * c) / determinant;
		return project(x, y, height) == null ? null : new Point2D.Double(x, y);
	}

	double heading()
	{
		// Camera-right is independent of pitch, including a near-vertical camera.
		return Math.atan2(-matrix[0][1], matrix[0][0]);
	}

	/** Pixel scale along camera-right on the horizontal plane, measured at the anchor. */
	double horizontalScaleAt(double x, double y, double height)
	{
		double depth = transform(2, x, y, height);
		double horizontalScale = scale * Math.hypot(matrix[0][0], matrix[0][1]) / depth;
		return depth >= NEAR_PLANE && Double.isFinite(horizontalScale) && horizontalScale > 0
			? horizontalScale : Double.NaN;
	}

	private double transform(int row, double x, double y, double z)
	{
		return matrix[row][0] * x + matrix[row][1] * y + matrix[row][2] * z + matrix[row][3];
	}
}
