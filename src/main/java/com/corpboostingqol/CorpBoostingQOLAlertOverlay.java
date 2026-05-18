package com.corpboostingqol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class CorpBoostingQOLAlertOverlay extends Overlay
{
	// Sprite IDs confirmed from runelite2 fork SpriteID.java
	private static final int SPRITE_VENGEANCE = SpriteID.SPELL_VENGEANCE;            // 564
	private static final int SPRITE_PRAYER    = SpriteID.SKILL_PRAYER;                // 201
	private static final int SPRITE_LUNAR     = SpriteID.TAB_MAGIC_SPELLBOOK_LUNAR;   // 1584
	private static final int SPRITE_ATTACK    = SpriteID.SKILL_ATTACK;                // 197 — used for idle combat alert

	// Icon size as fraction of screen height
	private static final float ICON_FRACTION = 0.10f;

	private final CorpBoostingQOLPlugin plugin;
	private final CorpBoostingQOLConfig config;
	private final SpriteManager spriteManager;

	private BufferedImage vengIcon;
	private BufferedImage prayerIcon;
	private BufferedImage lunarIcon;
	private BufferedImage combatIcon;

	@Inject
	public CorpBoostingQOLAlertOverlay(CorpBoostingQOLPlugin plugin, CorpBoostingQOLConfig config, SpriteManager spriteManager)
	{
		this.plugin        = plugin;
		this.config        = config;
		this.spriteManager = spriteManager;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!plugin.inCorpCave) return null;

		// Collect active alerts — combat idle is now part of this system
		List<AlertType> active = new ArrayList<>();
		if (config.combatOverlayEnabled() && !plugin.inCombat) active.add(AlertType.COMBAT_IDLE);
		if (config.vengEnabled() && plugin.vengReady)               active.add(AlertType.VENG);
		if (config.quickPrayerEnabled() && plugin.quickPrayerWarn)  active.add(AlertType.PRAYER);
		if (config.lunarsEnabled() && plugin.lunarsWarn)             active.add(AlertType.LUNAR);

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
			AlertType type = active.get(i);
			int sliceX = i * sliceW;

			if (shouldDrawOverlay(type))
			{
				g.setColor(getOverlayColor(type));
				g.fillRect(sliceX, 0, sliceW, screenH);
			}

			if (shouldDrawIcon(type))
			{
				BufferedImage icon = getIcon(type, iconSize);
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

	private boolean shouldDrawOverlay(AlertType type)
	{
		switch (type)
		{
			case COMBAT_IDLE: return config.combatOverlayEnabled();
			case VENG:        return config.vengOverlayEnabled();
			case PRAYER:      return config.prayerOverlayEnabled();
			case LUNAR:       return config.lunarOverlayEnabled();
			default:          return false;
		}
	}

	private boolean shouldDrawIcon(AlertType type)
	{
		switch (type)
		{
			case COMBAT_IDLE: return config.combatIconEnabled();
			case VENG:        return config.vengIconEnabled();
			case PRAYER:      return config.prayerIconEnabled();
			case LUNAR:       return config.lunarIconEnabled();
			default:          return false;
		}
	}

	private Color getOverlayColor(AlertType type)
	{
		switch (type)
		{
			case COMBAT_IDLE: return config.combatOverlayColor();
			case VENG:        return config.vengOverlayColor();
			case PRAYER:      return config.prayerOverlayColor();
			case LUNAR:       return config.lunarOverlayColor();
			default:          return new Color(255, 0, 0, 100);
		}
	}

	private BufferedImage getIcon(AlertType type, int size)
	{
		switch (type)
		{
			case COMBAT_IDLE:
				if (combatIcon == null || combatIcon.getHeight() != size)
					combatIcon = spriteManager.getSprite(SPRITE_ATTACK, 0);
				return scaleIcon(combatIcon, size);
			case VENG:
				if (vengIcon == null || vengIcon.getHeight() != size)
					vengIcon = spriteManager.getSprite(SPRITE_VENGEANCE, 0);
				return scaleIcon(vengIcon, size);
			case PRAYER:
				if (prayerIcon == null || prayerIcon.getHeight() != size)
					prayerIcon = spriteManager.getSprite(SPRITE_PRAYER, 0);
				return scaleIcon(prayerIcon, size);
			case LUNAR:
				if (lunarIcon == null || lunarIcon.getHeight() != size)
					lunarIcon = spriteManager.getSprite(SPRITE_LUNAR, 0);
				return scaleIcon(lunarIcon, size);
			default:
				return null;
		}
	}

	private BufferedImage scaleIcon(BufferedImage src, int targetSize)
	{
		if (src == null) return null;
		if (src.getHeight() == targetSize) return src;
		BufferedImage scaled = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
		scaled.createGraphics().drawImage(src, 0, 0, targetSize, targetSize, null);
		return scaled;
	}

	private enum AlertType { COMBAT_IDLE, VENG, PRAYER, LUNAR }
}
