package com.petboostingqol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.ItemID;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class PetBoostingQOLAlertOverlay extends Overlay
{
	private static final int SPRITE_VENGEANCE = SpriteID.SPELL_VENGEANCE;
	private static final int SPRITE_PRAYER    = SpriteID.SKILL_PRAYER;
	private static final int SPRITE_LUNAR     = SpriteID.TAB_MAGIC_SPELLBOOK_LUNAR;
	private static final int SPRITE_ATTACK    = SpriteID.SKILL_ATTACK;

	private static final int ITEM_SATURATED_HEART   = ItemID.SATURATED_HEART;
	private static final int ITEM_PRAYER_REGEN_1    = ItemID.PRAYER_REGENERATION_POTION1;
	private static final int SPRITE_CURE_ME         = SpriteID.SPELL_CURE_ME;
	private static final int ITEM_ARAXYTE_SACK      = ItemID.ARAXYTE_VENOM_SACK;
	private static final int ITEM_EXT_ANTIFIRE_1    = ItemID.EXTENDED_ANTIFIRE1;
	private static final int ITEM_PRAYER_POTION_1   = ItemID.PRAYER_POTION1;     // 143
	private static final int SPRITE_SPEC            = SpriteID.SPELL_ENERGY_TRANSFER;

	private static final float ICON_FRACTION = 0.10f;

	private final PetBoostingQOLPlugin plugin;
	private final PetBoostingQOLConfig config;
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;

	// Sprite-based icons (Corp)
	private BufferedImage vengIcon;
	private BufferedImage prayerIcon;
	private BufferedImage lunarIcon;
	private BufferedImage combatIcon;
	private BufferedImage cureMeIcon;
	private BufferedImage specIcon;

	// Item-based icons (new bosses)
	private BufferedImage saturatedHeartIcon;
	private BufferedImage prayerRegenIcon;
	private BufferedImage araxyteSackIcon;
	private BufferedImage extAntifireIcon;
	private BufferedImage prayerPotionIcon;

	@Inject
	public PetBoostingQOLAlertOverlay(PetBoostingQOLPlugin plugin, PetBoostingQOLConfig config,
			SpriteManager spriteManager, ItemManager itemManager)
	{
		this.plugin        = plugin;
		this.config        = config;
		this.spriteManager = spriteManager;
		this.itemManager   = itemManager;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGHEST);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		List<AlertType> active = new ArrayList<>();

		if (plugin.inCorpCave)
		{
			if (config.combatOverlayEnabled() && !plugin.inCombat)      active.add(AlertType.CORP_COMBAT_IDLE);
			if (config.vengEnabled() && plugin.vengReady)                active.add(AlertType.CORP_VENG);
			if (config.quickPrayerEnabled() && plugin.quickPrayerWarn)   active.add(AlertType.CORP_PRAYER);
			if (config.lunarsEnabled() && plugin.lunarsWarn)             active.add(AlertType.CORP_LUNAR);
		}

		if (plugin.inKqCave)
		{
			if (config.kqVengEnabled() && plugin.kqVengReady)                   active.add(AlertType.KQ_VENG);
			if (config.kqSaturatedHeartEnabled() && plugin.kqSaturatedWarn)     active.add(AlertType.KQ_SATURATED);
			if (config.kqProtMageEnabled() && plugin.kqProtMageWarn)            active.add(AlertType.KQ_PROT_MAGE);
			if (config.kqSpecEnabled() && plugin.kqSpecWarn)                    active.add(AlertType.KQ_SPEC);
			if (config.kqPrayerRegenEnabled() && plugin.kqPrayerRegenWarn)      active.add(AlertType.KQ_PRAYER_REGEN);
			if (config.kqPoisonEnabled() && plugin.kqPoisoned)                  active.add(AlertType.KQ_POISON);
			if (config.kqLowPrayerEnabled() && plugin.kqLowPrayerWarn)          active.add(AlertType.KQ_LOW_PRAYER);
		}

		if (plugin.inMoleLair)
		{
			if (config.moleSaturatedHeartEnabled() && plugin.moleSaturatedWarn) active.add(AlertType.MOLE_SATURATED);
			if (config.moleSpecEnabled() && plugin.moleSpecWarn)                active.add(AlertType.MOLE_SPEC);
		}

		if (plugin.inKbdLair)
		{
			if (config.kbdAntifireEnabled() && plugin.kbdAntifireWarn)  active.add(AlertType.KBD_ANTIFIRE);
			if (config.kbdPoisonEnabled() && plugin.kbdPoisoned)        active.add(AlertType.KBD_POISON);
			if (config.kbdSpecEnabled() && plugin.kbdSpecWarn)          active.add(AlertType.KBD_SPEC);
		}

		if (plugin.inSireLair)
		{
			if (config.sireSpecEnabled() && plugin.sireSpecWarn)        active.add(AlertType.SIRE_SPEC);
		}

		if (plugin.inSmokeLair)
		{
			if (config.smokeSpecEnabled() && plugin.smokeSpecWarn)      active.add(AlertType.SMOKE_SPEC);
		}

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

	// Dispatch helpers
	private boolean shouldDrawOverlay(AlertType type)
	{
		switch (type)
		{
			case CORP_COMBAT_IDLE: return config.combatOverlayEnabled();
			case CORP_VENG:        return config.vengOverlayEnabled();
			case CORP_PRAYER:      return config.prayerOverlayEnabled();
			case CORP_LUNAR:       return config.lunarOverlayEnabled();
			case KQ_VENG:          return config.kqVengOverlayEnabled();
			case KQ_SATURATED:     return config.kqSaturatedOverlayEnabled();
			case KQ_PROT_MAGE:     return config.kqProtMageOverlayEnabled();
			case KQ_SPEC:          return config.kqSpecOverlayEnabled();
			case KQ_PRAYER_REGEN:  return config.kqPrayerRegenOverlayEnabled();
			case KQ_POISON:        return config.kqPoisonOverlayEnabled();
			case KQ_LOW_PRAYER:    return config.kqLowPrayerOverlayEnabled();
			case MOLE_SATURATED:   return config.moleSaturatedOverlayEnabled();
			case MOLE_SPEC:        return config.moleSpecOverlayEnabled();
			case KBD_ANTIFIRE:     return config.kbdAntifireOverlayEnabled();
			case KBD_POISON:       return config.kbdPoisonOverlayEnabled();
			case KBD_SPEC:         return config.kbdSpecOverlayEnabled();
			case SIRE_SPEC:        return config.sireSpecOverlayEnabled();
			case SMOKE_SPEC:       return config.smokeSpecOverlayEnabled();
			default:               return false;
		}
	}

	private boolean shouldDrawIcon(AlertType type)
	{
		switch (type)
		{
			case CORP_COMBAT_IDLE: return config.combatIconEnabled();
			case CORP_VENG:        return config.vengIconEnabled();
			case CORP_PRAYER:      return config.prayerIconEnabled();
			case CORP_LUNAR:       return config.lunarIconEnabled();
			case KQ_VENG:          return config.kqVengIconEnabled();
			case KQ_SATURATED:     return config.kqSaturatedIconEnabled();
			case KQ_PROT_MAGE:     return config.kqProtMageIconEnabled();
			case KQ_SPEC:          return config.kqSpecIconEnabled();
			case KQ_PRAYER_REGEN:  return config.kqPrayerRegenIconEnabled();
			case KQ_POISON:        return config.kqPoisonIconEnabled();
			case KQ_LOW_PRAYER:    return config.kqLowPrayerIconEnabled();
			case MOLE_SATURATED:   return config.moleSaturatedIconEnabled();
			case MOLE_SPEC:        return config.moleSpecIconEnabled();
			case KBD_ANTIFIRE:     return config.kbdAntifireIconEnabled();
			case KBD_POISON:       return config.kbdPoisonIconEnabled();
			case KBD_SPEC:         return config.kbdSpecIconEnabled();
			case SIRE_SPEC:        return config.sireSpecIconEnabled();
			case SMOKE_SPEC:       return config.smokeSpecIconEnabled();
			default:               return false;
		}
	}

	private Color getOverlayColor(AlertType type)
	{
		switch (type)
		{
			case CORP_COMBAT_IDLE: return config.combatOverlayColor();
			case CORP_VENG:        return config.vengOverlayColor();
			case CORP_PRAYER:      return config.prayerOverlayColor();
			case CORP_LUNAR:       return config.lunarOverlayColor();
			case KQ_VENG:          return config.kqVengOverlayColor();
			case KQ_SATURATED:     return config.kqSaturatedOverlayColor();
			case KQ_PROT_MAGE:     return config.kqProtMageOverlayColor();
			case KQ_SPEC:          return config.kqSpecOverlayColor();
			case KQ_PRAYER_REGEN:  return config.kqPrayerRegenOverlayColor();
			case KQ_POISON:        return config.kqPoisonOverlayColor();
			case KQ_LOW_PRAYER:    return config.kqLowPrayerOverlayColor();
			case MOLE_SATURATED:   return config.moleSaturatedOverlayColor();
			case MOLE_SPEC:        return config.moleSpecOverlayColor();
			case KBD_ANTIFIRE:     return config.kbdAntifireOverlayColor();
			case KBD_POISON:       return config.kbdPoisonOverlayColor();
			case KBD_SPEC:         return config.kbdSpecOverlayColor();
			case SIRE_SPEC:        return config.sireSpecOverlayColor();
			case SMOKE_SPEC:       return config.smokeSpecOverlayColor();
			default:               return new Color(255, 0, 0, 100);
		}
	}

	private BufferedImage getIcon(AlertType type, int size)
	{
		switch (type)
		{
			case CORP_COMBAT_IDLE:
				if (combatIcon == null) combatIcon = spriteManager.getSprite(SPRITE_ATTACK, 0);
				return scaleIcon(combatIcon, size);
			case CORP_VENG:
			case KQ_VENG:
				if (vengIcon == null) vengIcon = spriteManager.getSprite(SPRITE_VENGEANCE, 0);
				return scaleIcon(vengIcon, size);
			case CORP_PRAYER:
				if (prayerIcon == null) prayerIcon = spriteManager.getSprite(SPRITE_PRAYER, 0);
				return scaleIcon(prayerIcon, size);
			case CORP_LUNAR:
				if (lunarIcon == null) lunarIcon = spriteManager.getSprite(SPRITE_LUNAR, 0);
				return scaleIcon(lunarIcon, size);

			case KQ_PROT_MAGE:
				if (prayerIcon == null) prayerIcon = spriteManager.getSprite(SPRITE_PRAYER, 0);
				return scaleIcon(prayerIcon, size);

			case KQ_SATURATED:
			case MOLE_SATURATED:
				if (saturatedHeartIcon == null)
					saturatedHeartIcon = itemManager.getImage(ITEM_SATURATED_HEART, 1, false);
				return scaleIcon(saturatedHeartIcon, size);

			case KQ_SPEC:
			case MOLE_SPEC:
			case KBD_SPEC:
			case SIRE_SPEC:
			case SMOKE_SPEC:
				if (specIcon == null) specIcon = spriteManager.getSprite(SPRITE_SPEC, 0);
				return scaleIcon(specIcon, size);

			case KQ_PRAYER_REGEN:
				if (prayerRegenIcon == null)
					prayerRegenIcon = itemManager.getImage(ITEM_PRAYER_REGEN_1, 1, false);
				return scaleIcon(prayerRegenIcon, size);

			case KQ_POISON:
				if (cureMeIcon == null) cureMeIcon = spriteManager.getSprite(SPRITE_CURE_ME, 0);
				return scaleIcon(cureMeIcon, size);

			case KQ_LOW_PRAYER:
				if (prayerPotionIcon == null)
					prayerPotionIcon = itemManager.getImage(ITEM_PRAYER_POTION_1, 1, false);
				return scaleIcon(prayerPotionIcon, size);

			case KBD_ANTIFIRE:
				if (extAntifireIcon == null)
					extAntifireIcon = itemManager.getImage(ITEM_EXT_ANTIFIRE_1, 1, false);
				return scaleIcon(extAntifireIcon, size);

			case KBD_POISON:
				if (araxyteSackIcon == null)
					araxyteSackIcon = itemManager.getImage(ITEM_ARAXYTE_SACK, 1, false);
				return scaleIcon(araxyteSackIcon, size);

			default:
				return null;
		}
	}

	private BufferedImage scaleIcon(BufferedImage src, int targetSize)
	{
		if (src == null) return null;
		if (src.getHeight() == targetSize && src.getWidth() == targetSize) return src;
		BufferedImage scaled = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
		scaled.createGraphics().drawImage(src, 0, 0, targetSize, targetSize, null);
		return scaled;
	}

	enum AlertType
	{
		// Corp
		CORP_COMBAT_IDLE, CORP_VENG, CORP_PRAYER, CORP_LUNAR,
		// KQ
		KQ_VENG, KQ_SATURATED, KQ_PROT_MAGE, KQ_SPEC, KQ_PRAYER_REGEN, KQ_POISON, KQ_LOW_PRAYER,
		// Mole
		MOLE_SATURATED, MOLE_SPEC,
		// KBD
		KBD_ANTIFIRE, KBD_POISON, KBD_SPEC,
		// Sire
		SIRE_SPEC,
		// Smoke Devil
		SMOKE_SPEC
	}
}
