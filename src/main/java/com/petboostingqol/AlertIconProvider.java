package com.petboostingqol;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ItemID;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;

/**
 * Loads, caches, and scales the sprite/item icons used by alerts.
 */
@Singleton
class AlertIconProvider
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

	// Scaled-icon cache keyed by type+size, since most renders reuse the same size.
	private final Map<AlertType, CachedIcon> iconCache = new EnumMap<>(AlertType.class);

	@Inject
	AlertIconProvider(SpriteManager spriteManager, ItemManager itemManager)
	{
		this.spriteManager = spriteManager;
		this.itemManager = itemManager;
	}

	BufferedImage getCachedIcon(AlertType type, int size)
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
			case SARA_PROT_MAGE:
				if (prayerIcon == null) prayerIcon = spriteManager.getSprite(SPRITE_PRAYER, 0);
				return scaleIcon(prayerIcon, size);

			case KQ_SATURATED:
			case MOLE_SATURATED:
			case ZAMMY_SATURATED:
			case SARACHNIS_SATURATED:
			case SARA_SATURATED:
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
			case SARA_SPEC:
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
			case SARA_PRAYER_REGEN:
				if (prayerRegenIcon == null)
					prayerRegenIcon = itemManager.getImage(ITEM_PRAYER_REGEN_1, 1, false);
				return scaleIcon(prayerRegenIcon, size);

			case KQ_POISON:
				if (cureMeIcon == null) cureMeIcon = spriteManager.getSprite(SPRITE_CURE_ME, 0);
				return scaleIcon(cureMeIcon, size);

			case KQ_LOW_PRAYER:
			case SCORPIA_LOW_PRAYER:
			case SARACHNIS_LOW_PRAYER:
			case SARA_LOW_PRAYER:
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
}
