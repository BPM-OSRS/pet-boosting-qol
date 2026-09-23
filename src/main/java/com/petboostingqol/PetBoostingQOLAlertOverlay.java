package com.petboostingqol;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
	private static final int SPRITE_HITPOINTS = SpriteID.SKILL_HITPOINTS;
	private static final int SPRITE_CURE_ME   = SpriteID.SPELL_CURE_ME;
	private static final int SPRITE_SPEC      = SpriteID.SPELL_ENERGY_TRANSFER;

	private static final int ITEM_SATURATED_HEART = ItemID.SATURATED_HEART;
	private static final int ITEM_PRAYER_REGEN_1  = ItemID.PRAYER_REGENERATION_POTION1;
	private static final int ITEM_ARAXYTE_SACK    = ItemID.ARAXYTE_VENOM_SAC;
	private static final int ITEM_EXT_ANTIFIRE_1  = ItemID.EXTENDED_ANTIFIRE1;
	private static final int ITEM_PRAYER_POTION_1 = ItemID.PRAYER_POTION1;

	private static final float ICON_FRACTION = 0.10f;

	// Low CPU mode: blink the rect overlay instead of drawing it every frame.
	// Icons are unaffected. Only the full-height color fill gets skipped on
	// "off" frames.
	private static final int BLINK_HALF_PERIOD_FRAMES = 25; // ~0.5s on, ~0.5s off at 50fps
	private long blinkFrameCounter = 0;

	private final PetBoostingQOLPlugin plugin;
	private final PetBoostingQOLConfig config;
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;

	private BufferedImage vengIcon;
	private BufferedImage prayerIcon;
	private BufferedImage lunarIcon;
	private BufferedImage combatIcon;
	private BufferedImage cureMeIcon;
	private BufferedImage specIcon;
	private BufferedImage hpIcon;
	private BufferedImage saturatedHeartIcon;
	private BufferedImage prayerRegenIcon;
	private BufferedImage araxyteSackIcon;
	private BufferedImage extAntifireIcon;
	private BufferedImage prayerPotionIcon;

	// Cached rect slice and scaled icon per alert type, so we're not
	// re-rendering a fillRect or re-scaling an icon on every single frame.
	private final Map<AlertType, CachedSlice> sliceCache = new EnumMap<>(AlertType.class);
	private final Map<AlertType, CachedIcon> iconCache = new EnumMap<>(AlertType.class);

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
			if (config.kqHpEnabled() && plugin.kqHpWarn)                        active.add(AlertType.KQ_HP);
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

		if (plugin.inZammyRoom)
		{
			if (config.zammySaturatedHeartEnabled() && plugin.zammySaturatedWarn)     active.add(AlertType.ZAMMY_SATURATED);
			if (config.zammyPrayerRegenEnabled() && plugin.zammyPrayerRegenWarn)      active.add(AlertType.ZAMMY_PRAYER_REGEN);
			if (config.zammyProtMeleeEnabled() && plugin.zammyProtMeleeWarn)          active.add(AlertType.ZAMMY_PROT_MELEE);
			if (config.zammyPoisonEnabled() && plugin.zammyPoisoned)                  active.add(AlertType.ZAMMY_POISON);
			if (config.zammyHpEnabled() && plugin.zammyHpWarn)                        active.add(AlertType.ZAMMY_HP);
		}

		if (plugin.inScorpiaLair)
		{
			if (config.scorpiaPrayerRegenEnabled() && plugin.scorpiaPrayerRegenWarn) active.add(AlertType.SCORPIA_PRAYER_REGEN);
			if (config.scorpiaLowPrayerEnabled() && plugin.scorpiaLowPrayerWarn)     active.add(AlertType.SCORPIA_LOW_PRAYER);
			if (config.scorpiaPoisonEnabled() && plugin.scorpiaPoisoned)             active.add(AlertType.SCORPIA_POISON);
			if (config.scorpiaSpecEnabled() && plugin.scorpiaSpecWarn)               active.add(AlertType.SCORPIA_SPEC);
		}

		if (plugin.inSarachnisLair)
		{
			if (config.sarachnisSaturatedHeartEnabled() && plugin.sarachnisSaturatedWarn)   active.add(AlertType.SARACHNIS_SATURATED);
			if (config.sarachnisPrayerRegenEnabled() && plugin.sarachnisPrayerRegenWarn)    active.add(AlertType.SARACHNIS_PRAYER_REGEN);
			if (config.sarachnisLowPrayerEnabled() && plugin.sarachnisLowPrayerWarn)        active.add(AlertType.SARACHNIS_LOW_PRAYER);
			if (config.sarachnisSpecEnabled() && plugin.sarachnisSpecWarn)                  active.add(AlertType.SARACHNIS_SPEC);
			if (config.sarachnisProtRangeEnabled() && plugin.sarachnisProtRangeWarn)         active.add(AlertType.SARACHNIS_PROT_RANGE);
		}

		if (active.isEmpty()) return null;

		blinkFrameCounter++;

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

			if (shouldDrawOverlay(type) && shouldDrawRectThisFrame())
			{
				Color color = getOverlayColor(type);
				BufferedImage slice = getCachedSlice(type, sliceW, screenH, color);
				if (slice != null)
				{
					g.drawImage(slice, sliceX, 0, null);
				}
			}

			if (shouldDrawIcon(type))
			{
				BufferedImage icon = getCachedIcon(type, iconSize);
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

	private BufferedImage getCachedIcon(AlertType type, int size)
	{
		CachedIcon cached = iconCache.get(type);
		if (cached != null && cached.size == size)
		{
			return cached.image;
		}

		BufferedImage icon = getIcon(type, size);
		if (icon != null)
		{
			iconCache.put(type, new CachedIcon(size, icon));
		}
		return icon;
	}

	private static final class CachedIcon
	{
		final int size;
		final BufferedImage image;

		CachedIcon(int size, BufferedImage image)
		{
			this.size = size;
			this.image = image;
		}
	}

	private boolean shouldDrawRectThisFrame()
	{
		if (!config.lowCpuMode())
		{
			return true;
		}
		return (blinkFrameCounter / BLINK_HALF_PERIOD_FRAMES) % 2 == 0;
	}

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
			case KQ_HP:            return config.kqHpOverlayEnabled();
			case MOLE_SATURATED:   return config.moleSaturatedOverlayEnabled();
			case MOLE_SPEC:        return config.moleSpecOverlayEnabled();
			case KBD_ANTIFIRE:     return config.kbdAntifireOverlayEnabled();
			case KBD_POISON:       return config.kbdPoisonOverlayEnabled();
			case KBD_SPEC:         return config.kbdSpecOverlayEnabled();
			case SIRE_SPEC:        return config.sireSpecOverlayEnabled();
			case SMOKE_SPEC:       return config.smokeSpecOverlayEnabled();
			case SCORPIA_PRAYER_REGEN: return config.scorpiaPrayerRegenOverlayEnabled();
			case SCORPIA_LOW_PRAYER:   return config.scorpiaLowPrayerOverlayEnabled();
			case SCORPIA_POISON:       return config.scorpiaPoisonOverlayEnabled();
			case SCORPIA_SPEC:         return config.scorpiaSpecOverlayEnabled();
			case ZAMMY_SATURATED:      return config.zammySaturatedOverlayEnabled();
			case ZAMMY_PRAYER_REGEN:   return config.zammyPrayerRegenOverlayEnabled();
			case ZAMMY_PROT_MELEE:     return config.zammyProtMeleeOverlayEnabled();
			case ZAMMY_POISON:         return config.zammyPoisonOverlayEnabled();
			case ZAMMY_HP:             return config.zammyHpOverlayEnabled();
			case SARACHNIS_SATURATED:      return config.sarachnisSaturatedOverlayEnabled();
			case SARACHNIS_PRAYER_REGEN:   return config.sarachnisPrayerRegenOverlayEnabled();
			case SARACHNIS_LOW_PRAYER:     return config.sarachnisLowPrayerOverlayEnabled();
			case SARACHNIS_SPEC:           return config.sarachnisSpecOverlayEnabled();
			case SARACHNIS_PROT_RANGE:     return config.sarachnisProtRangeOverlayEnabled();
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
			case KQ_HP:            return config.kqHpIconEnabled();
			case MOLE_SATURATED:   return config.moleSaturatedIconEnabled();
			case MOLE_SPEC:        return config.moleSpecIconEnabled();
			case KBD_ANTIFIRE:     return config.kbdAntifireIconEnabled();
			case KBD_POISON:       return config.kbdPoisonIconEnabled();
			case KBD_SPEC:         return config.kbdSpecIconEnabled();
			case SIRE_SPEC:        return config.sireSpecIconEnabled();
			case SMOKE_SPEC:       return config.smokeSpecIconEnabled();
			case SCORPIA_PRAYER_REGEN: return config.scorpiaPrayerRegenIconEnabled();
			case SCORPIA_LOW_PRAYER:   return config.scorpiaLowPrayerIconEnabled();
			case SCORPIA_POISON:       return config.scorpiaPoisonIconEnabled();
			case SCORPIA_SPEC:         return config.scorpiaSpecIconEnabled();
			case ZAMMY_SATURATED:      return config.zammySaturatedIconEnabled();
			case ZAMMY_PRAYER_REGEN:   return config.zammyPrayerRegenIconEnabled();
			case ZAMMY_PROT_MELEE:     return config.zammyProtMeleeIconEnabled();
			case ZAMMY_POISON:         return config.zammyPoisonIconEnabled();
			case ZAMMY_HP:             return config.zammyHpIconEnabled();
			case SARACHNIS_SATURATED:      return config.sarachnisSaturatedIconEnabled();
			case SARACHNIS_PRAYER_REGEN:   return config.sarachnisPrayerRegenIconEnabled();
			case SARACHNIS_LOW_PRAYER:     return config.sarachnisLowPrayerIconEnabled();
			case SARACHNIS_SPEC:           return config.sarachnisSpecIconEnabled();
			case SARACHNIS_PROT_RANGE:     return config.sarachnisProtRangeIconEnabled();
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
			case KQ_HP:            return config.kqHpOverlayColor();
			case MOLE_SATURATED:   return config.moleSaturatedOverlayColor();
			case MOLE_SPEC:        return config.moleSpecOverlayColor();
			case KBD_ANTIFIRE:     return config.kbdAntifireOverlayColor();
			case KBD_POISON:       return config.kbdPoisonOverlayColor();
			case KBD_SPEC:         return config.kbdSpecOverlayColor();
			case SIRE_SPEC:        return config.sireSpecOverlayColor();
			case SMOKE_SPEC:       return config.smokeSpecOverlayColor();
			case SCORPIA_PRAYER_REGEN: return config.scorpiaPrayerRegenOverlayColor();
			case SCORPIA_LOW_PRAYER:   return config.scorpiaLowPrayerOverlayColor();
			case SCORPIA_POISON:       return config.scorpiaPoisonOverlayColor();
			case SCORPIA_SPEC:         return config.scorpiaSpecOverlayColor();
			case ZAMMY_SATURATED:      return config.zammySaturatedOverlayColor();
			case ZAMMY_PRAYER_REGEN:   return config.zammyPrayerRegenOverlayColor();
			case ZAMMY_PROT_MELEE:     return config.zammyProtMeleeOverlayColor();
			case ZAMMY_POISON:         return config.zammyPoisonOverlayColor();
			case ZAMMY_HP:             return config.zammyHpOverlayColor();
			case SARACHNIS_SATURATED:      return config.sarachnisSaturatedOverlayColor();
			case SARACHNIS_PRAYER_REGEN:   return config.sarachnisPrayerRegenOverlayColor();
			case SARACHNIS_LOW_PRAYER:     return config.sarachnisLowPrayerOverlayColor();
			case SARACHNIS_SPEC:           return config.sarachnisSpecOverlayColor();
			case SARACHNIS_PROT_RANGE:     return config.sarachnisProtRangeOverlayColor();
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
			case ZAMMY_PROT_MELEE:
			case SARACHNIS_PROT_RANGE:
				if (prayerIcon == null) prayerIcon = spriteManager.getSprite(SPRITE_PRAYER, 0);
				return scaleIcon(prayerIcon, size);

			case KQ_SATURATED:
			case MOLE_SATURATED:
			case ZAMMY_SATURATED:
			case SARACHNIS_SATURATED:
				if (saturatedHeartIcon == null)
					saturatedHeartIcon = itemManager.getImage(ITEM_SATURATED_HEART, 1, false);
				return scaleIcon(saturatedHeartIcon, size);

			case KQ_SPEC:
			case MOLE_SPEC:
			case KBD_SPEC:
			case SIRE_SPEC:
			case SMOKE_SPEC:
			case SCORPIA_SPEC:
			case SARACHNIS_SPEC:
				if (specIcon == null) specIcon = spriteManager.getSprite(SPRITE_SPEC, 0);
				return scaleIcon(specIcon, size);

			case KQ_HP:
			case ZAMMY_HP:
				if (hpIcon == null) hpIcon = spriteManager.getSprite(SPRITE_HITPOINTS, 0);
				return scaleIcon(hpIcon, size);

			case KQ_PRAYER_REGEN:
			case SCORPIA_PRAYER_REGEN:
			case ZAMMY_PRAYER_REGEN:
			case SARACHNIS_PRAYER_REGEN:
				if (prayerRegenIcon == null)
					prayerRegenIcon = itemManager.getImage(ITEM_PRAYER_REGEN_1, 1, false);
				return scaleIcon(prayerRegenIcon, size);

			case KQ_POISON:
				if (cureMeIcon == null) cureMeIcon = spriteManager.getSprite(SPRITE_CURE_ME, 0);
				return scaleIcon(cureMeIcon, size);

			case KQ_LOW_PRAYER:
			case SCORPIA_LOW_PRAYER:
			case SARACHNIS_LOW_PRAYER:
				if (prayerPotionIcon == null)
					prayerPotionIcon = itemManager.getImage(ITEM_PRAYER_POTION_1, 1, false);
				return scaleIcon(prayerPotionIcon, size);

			case KBD_ANTIFIRE:
				if (extAntifireIcon == null)
					extAntifireIcon = itemManager.getImage(ITEM_EXT_ANTIFIRE_1, 1, false);
				return scaleIcon(extAntifireIcon, size);

			case KBD_POISON:
			case SCORPIA_POISON:
			case ZAMMY_POISON:
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
		CORP_COMBAT_IDLE, CORP_VENG, CORP_PRAYER, CORP_LUNAR,
		KQ_VENG, KQ_SATURATED, KQ_PROT_MAGE, KQ_SPEC, KQ_PRAYER_REGEN, KQ_POISON, KQ_LOW_PRAYER, KQ_HP,
		MOLE_SATURATED, MOLE_SPEC,
		KBD_ANTIFIRE, KBD_POISON, KBD_SPEC,
		SIRE_SPEC,
		SCORPIA_PRAYER_REGEN, SCORPIA_LOW_PRAYER, SCORPIA_POISON, SCORPIA_SPEC,
		SMOKE_SPEC,
		ZAMMY_SATURATED, ZAMMY_PRAYER_REGEN, ZAMMY_PROT_MELEE, ZAMMY_POISON, ZAMMY_HP,
		SARACHNIS_SATURATED, SARACHNIS_PRAYER_REGEN, SARACHNIS_LOW_PRAYER, SARACHNIS_SPEC, SARACHNIS_PROT_RANGE
	}
}
