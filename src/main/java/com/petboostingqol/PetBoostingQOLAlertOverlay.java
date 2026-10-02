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

public class PetBoostingQOLAlertOverlay extends Overlay
{
	private static final float ICON_FRACTION = 0.10f;

	private final PetBoostingQOLPlugin plugin;
	private final PetBoostingQOLConfig config;
	private final AlertIconProvider iconProvider;

	// Cached per alert type so we're not re-filling a rect every frame.
	private final Map<AlertType, CachedSlice> sliceCache = new EnumMap<>(AlertType.class);

	@Inject
	public PetBoostingQOLAlertOverlay(PetBoostingQOLPlugin plugin, PetBoostingQOLConfig config,
			AlertIconProvider iconProvider)
	{
		this.plugin       = plugin;
		this.config       = config;
		this.iconProvider = iconProvider;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		// Compact overlay handles rendering instead when enabled.
		if (config.compactOverlayModeEnabled())
		{
			return null;
		}

		List<AlertType> active = AlertRegistry.collectActive(plugin, config);
		if (active.isEmpty()) return null;

		Rectangle bounds = g.getClipBounds();
		if (bounds == null) return null;

		int screenW  = bounds.width;
		int screenH  = bounds.height;
		int iconSize = (int)(screenH * ICON_FRACTION);
		int count    = active.size();
		int sliceW   = screenW / count;

		for (int i = 0; i < count; i++)
		{
			AlertType type  = active.get(i);
			int sliceX = i * sliceW;

			if (AlertRegistry.shouldDrawOverlay(type, config))
			{
				Color color = AlertRegistry.getOverlayColor(type, config);
				BufferedImage slice = getCachedSlice(type, sliceW, screenH, color);
				if (slice != null)
				{
					g.drawImage(slice, sliceX, 0, null);
				}
			}

			if (AlertRegistry.shouldDrawIcon(type, config))
			{
				BufferedImage icon = iconProvider.getCachedIcon(type, iconSize);
				if (icon != null)
				{
					int ix = sliceX + (sliceW - icon.getWidth()) / 2;
					int iy = (screenH - icon.getHeight()) / 2;
					g.drawImage(icon, ix, iy, null);
				}
			}
		}

		return null;
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
