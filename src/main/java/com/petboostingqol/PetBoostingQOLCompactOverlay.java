package com.petboostingqol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

/**
 * Small draggable/resizable alternative to the fullscreen overlay. Splits into
 * up to 4 quadrants when multiple alerts are active, reusing the colour/icon
 * settings already configured per alert type.
 */
public class PetBoostingQOLCompactOverlay extends Overlay
{
	private static final int DEFAULT_SIZE = 160;
	private static final float ICON_FRACTION = 0.6f; // fraction of the smaller cell dimension
	private static final int MAX_CELLS = 4;
	private static final Color GRID_LINE_COLOR = new Color(0, 0, 0, 140);

	private final PetBoostingQOLPlugin plugin;
	private final PetBoostingQOLConfig config;
	private final AlertIconProvider iconProvider;

	private final Map<AlertType, CachedSlice> sliceCache = new EnumMap<>(AlertType.class);

	@Inject
	public PetBoostingQOLCompactOverlay(PetBoostingQOLPlugin plugin, PetBoostingQOLConfig config,
			AlertIconProvider iconProvider)
	{
		this.plugin       = plugin;
		this.config       = config;
		this.iconProvider = iconProvider;
		// Free-floating, drag/resizable box (unlike DYNAMIC, used by the fullscreen overlay).
		setPosition(OverlayPosition.DETACHED);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGHEST);
		setResizable(true);
		setPreferredSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!config.compactOverlayModeEnabled())
		{
			return null;
		}

		// getBounds() reflects the current drag/resize state; g.getClipBounds() would not.
		Rectangle bounds = getBounds();
		int boxW = (bounds != null && bounds.width  > 0) ? bounds.width  : DEFAULT_SIZE;
		int boxH = (bounds != null && bounds.height > 0) ? bounds.height : DEFAULT_SIZE;

		// Report our size even with nothing active, so the box doesn't disappear.
		List<AlertType> active = AlertRegistry.collectActive(plugin, config);
		if (active.isEmpty())
		{
			return new Dimension(boxW, boxH);
		}

		int count = Math.min(active.size(), MAX_CELLS);
		int cols  = count <= 1 ? 1 : 2;
		int rows  = count <= 2 ? 1 : 2;
		int cellW = boxW / cols;
		int cellH = boxH / rows;
		int iconSize = (int) (Math.min(cellW, cellH) * ICON_FRACTION);

		for (int i = 0; i < count; i++)
		{
			AlertType type = active.get(i);
			int col = i % cols;
			int row = i / cols;
			int cellX = col * cellW;
			int cellY = row * cellH;

			if (AlertRegistry.shouldDrawOverlay(type, config))
			{
				Color color = AlertRegistry.getOverlayColor(type, config);
				BufferedImage slice = getCachedSlice(type, cellW, cellH, color);
				if (slice != null)
				{
					g.drawImage(slice, cellX, cellY, null);
				}
			}

			if (AlertRegistry.shouldDrawIcon(type, config))
			{
				BufferedImage icon = iconProvider.getCachedIcon(type, Math.max(1, iconSize));
				if (icon != null)
				{
					int ix = cellX + (cellW - icon.getWidth()) / 2;
					int iy = cellY + (cellH - icon.getHeight()) / 2;
					g.drawImage(icon, ix, iy, null);
				}
			}
		}

		// Divider lines between quadrants when more than one is active.
		if (count > 1)
		{
			g.setColor(GRID_LINE_COLOR);
			if (cols > 1)
			{
				g.drawLine(cellW, 0, cellW, boxH);
			}
			if (rows > 1)
			{
				g.drawLine(0, cellH, boxW, cellH);
			}
		}

		return new Dimension(boxW, boxH);
	}

	private BufferedImage getCachedSlice(AlertType type, int width, int height, Color color)
	{
		if (width <= 0 || height <= 0)
		{
			return null;
		}

		CachedSlice cached = sliceCache.get(type);
		if (cached != null && cached.matches(width, height, color))
		{
			return cached.image;
		}

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ig = img.createGraphics();
		try
		{
			ig.setColor(color);
			ig.fillRect(0, 0, width, height);
		}
		finally
		{
			ig.dispose();
		}

		sliceCache.put(type, new CachedSlice(width, height, color, img));
		return img;
	}

	private static final class CachedSlice
	{
		final int width;
		final int height;
		final Color color;
		final BufferedImage image;

		CachedSlice(int width, int height, Color color, BufferedImage image)
		{
			this.width = width;
			this.height = height;
			this.color = color;
			this.image = image;
		}

		boolean matches(int width, int height, Color color)
		{
			return this.width == width && this.height == height && this.color.equals(color);
		}
	}
}
