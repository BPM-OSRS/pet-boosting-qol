package com.petboostingqol;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared alert lookups used by both overlay classes.
 */
final class AlertRegistry
{
	private AlertRegistry() { }

	static List<AlertType> collectActive(PetBoostingQOLPlugin plugin, PetBoostingQOLConfig config)
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

		if (plugin.inSaraRoom)
		{
			if (config.saraSaturatedHeartEnabled() && plugin.saraSaturatedWarn)   active.add(AlertType.SARA_SATURATED);
			if (config.saraPrayerRegenEnabled() && plugin.saraPrayerRegenWarn)    active.add(AlertType.SARA_PRAYER_REGEN);
			if (config.saraLowPrayerEnabled() && plugin.saraLowPrayerWarn)        active.add(AlertType.SARA_LOW_PRAYER);
			if (config.saraSpecEnabled() && plugin.saraSpecWarn)                  active.add(AlertType.SARA_SPEC);
			if (config.saraProtMageEnabled() && plugin.saraProtMageWarn)          active.add(AlertType.SARA_PROT_MAGE);
		}

		if (config.everywhereSaturatedHeartEnabled() && plugin.everywhereSaturatedWarn)   active.add(AlertType.EVERYWHERE_SATURATED);
		if (config.everywherePrayerRegenEnabled() && plugin.everywherePrayerRegenWarn)   active.add(AlertType.EVERYWHERE_PRAYER_REGEN);
		if (config.everywhereLowPrayerEnabled() && plugin.everywhereLowPrayerWarn)       active.add(AlertType.EVERYWHERE_LOW_PRAYER);
		if (config.everywhereSpecEnabled() && plugin.everywhereSpecWarn)                 active.add(AlertType.EVERYWHERE_SPEC);
		if (config.everywhereAntifireEnabled() && plugin.everywhereAntifireWarn)         active.add(AlertType.EVERYWHERE_ANTIFIRE);

		return active;
	}

	static boolean shouldDrawOverlay(AlertType type, PetBoostingQOLConfig config)
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
			case SARA_SATURATED:      return config.saraSaturatedOverlayEnabled();
			case SARA_PRAYER_REGEN:   return config.saraPrayerRegenOverlayEnabled();
			case SARA_LOW_PRAYER:     return config.saraLowPrayerOverlayEnabled();
			case SARA_SPEC:           return config.saraSpecOverlayEnabled();
			case SARA_PROT_MAGE:      return config.saraProtMageOverlayEnabled();
			case EVERYWHERE_SATURATED:    return config.everywhereSaturatedOverlayEnabled();
			case EVERYWHERE_PRAYER_REGEN: return config.everywherePrayerRegenOverlayEnabled();
			case EVERYWHERE_LOW_PRAYER:   return config.everywhereLowPrayerOverlayEnabled();
			case EVERYWHERE_SPEC:         return config.everywhereSpecOverlayEnabled();
			case EVERYWHERE_ANTIFIRE:     return config.everywhereAntifireOverlayEnabled();
			default:               return false;
		}
	}

	static boolean shouldDrawIcon(AlertType type, PetBoostingQOLConfig config)
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
			case SARA_SATURATED:      return config.saraSaturatedIconEnabled();
			case SARA_PRAYER_REGEN:   return config.saraPrayerRegenIconEnabled();
			case SARA_LOW_PRAYER:     return config.saraLowPrayerIconEnabled();
			case SARA_SPEC:           return config.saraSpecIconEnabled();
			case SARA_PROT_MAGE:      return config.saraProtMageIconEnabled();
			case EVERYWHERE_SATURATED:    return config.everywhereSaturatedIconEnabled();
			case EVERYWHERE_PRAYER_REGEN: return config.everywherePrayerRegenIconEnabled();
			case EVERYWHERE_LOW_PRAYER:   return config.everywhereLowPrayerIconEnabled();
			case EVERYWHERE_SPEC:         return config.everywhereSpecIconEnabled();
			case EVERYWHERE_ANTIFIRE:     return config.everywhereAntifireIconEnabled();
			default:               return false;
		}
	}

	static Color getOverlayColor(AlertType type, PetBoostingQOLConfig config)
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
			case SARA_SATURATED:      return config.saraSaturatedOverlayColor();
			case SARA_PRAYER_REGEN:   return config.saraPrayerRegenOverlayColor();
			case SARA_LOW_PRAYER:     return config.saraLowPrayerOverlayColor();
			case SARA_SPEC:           return config.saraSpecOverlayColor();
			case SARA_PROT_MAGE:      return config.saraProtMageOverlayColor();
			case EVERYWHERE_SATURATED:    return config.everywhereSaturatedOverlayColor();
			case EVERYWHERE_PRAYER_REGEN: return config.everywherePrayerRegenOverlayColor();
			case EVERYWHERE_LOW_PRAYER:   return config.everywhereLowPrayerOverlayColor();
			case EVERYWHERE_SPEC:         return config.everywhereSpecOverlayColor();
			case EVERYWHERE_ANTIFIRE:     return config.everywhereAntifireOverlayColor();
			default:               return new Color(255, 0, 0, 100);
		}
	}
}
