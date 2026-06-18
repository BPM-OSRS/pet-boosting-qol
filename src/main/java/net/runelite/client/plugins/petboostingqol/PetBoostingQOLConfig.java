package net.runelite.client.plugins.petboostingqol;

import java.awt.Color;
import java.awt.event.KeyEvent;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;

@ConfigGroup("petboostingqol")
public interface PetBoostingQOLConfig extends Config
{
	@ConfigSection(name = "Corporeal Beast", description = "All Corp settings", position = 0, closedByDefault = true)
	String corpSection = "corp";

	@ConfigSection(name = "Kalphite Queen", description = "All KQ settings", position = 1, closedByDefault = true)
	String kqSection = "kq";

	@ConfigSection(name = "Giant Mole", description = "All Mole settings", position = 2, closedByDefault = true)
	String moleSection = "mole";

	@ConfigSection(name = "King Black Dragon", description = "All KBD settings", position = 3, closedByDefault = true)
	String kbdSection = "kbd";

	// CORP

	@Alpha
	@ConfigItem(keyName = "warningOverlayColor", name = "Warning box colour",
		description = "Background colour of the Corp warning text boxes",
		section = corpSection, position = 0)
	default Color warningOverlayColor()
	{
		return new Color(150, 0, 0, 230);
	}

	@ConfigItem(keyName = "combatOverlayEnabled", name = "Combat idle overlay",
		description = "Show a fullscreen colour overlay when idle inside the Corp Cave",
		section = corpSection, position = 1)
	default boolean combatOverlayEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "combatIconEnabled", name = "Combat idle icon",
		description = "Show the Attack skill icon when idle inside the Corp Cave",
		section = corpSection, position = 2)
	default boolean combatIconEnabled()
	{
		return true;
	}

	@Alpha
	@ConfigItem(keyName = "combatOverlayColor", name = "Combat idle overlay colour",
		description = "Colour and opacity of the combat idle overlay",
		section = corpSection, position = 3)
	default Color combatOverlayColor()
	{
		return new Color(255, 0, 0, 120);
	}

	@ConfigItem(keyName = "movementLockEnabled", name = "Movement lock",
		description = "Consume walk clicks inside the Corp Cave unless the key below is held",
		section = corpSection, position = 4)
	default boolean movementLockEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "movementHoldKey", name = "Hold key to walk",
		description = "Hold this key to allow walking while movement lock is active",
		section = corpSection, position = 5)
	default Keybind movementHoldKey()
	{
		return new Keybind(KeyEvent.VK_0, 0);
	}

	@ConfigItem(keyName = "vengEnabled", name = "Vengeance indicator",
		description = "Show an indicator when Vengeance is ready to cast",
		section = corpSection, position = 6)
	default boolean vengEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "vengIconEnabled", name = "Vengeance icon",
		description = "Show the Vengeance spell icon",
		section = corpSection, position = 7)
	default boolean vengIconEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "vengOverlayEnabled", name = "Vengeance overlay",
		description = "Show a fullscreen colour overlay when Vengeance is ready",
		section = corpSection, position = 8)
	default boolean vengOverlayEnabled()
	{
		return true;
	}

	@Alpha
	@ConfigItem(keyName = "vengOverlayColor", name = "Vengeance overlay colour",
		description = "Colour of the Vengeance fullscreen overlay",
		section = corpSection, position = 9)
	default Color vengOverlayColor()
	{
		return new Color(255, 80, 80, 100);
	}

	@ConfigItem(keyName = "quickPrayerEnabled", name = "Quick prayer indicator",
		description = "Show an indicator when quick prayers are not active inside the Corp Cave",
		section = corpSection, position = 10)
	default boolean quickPrayerEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "prayerIconEnabled", name = "Quick prayer icon",
		description = "Show the Prayer skill icon",
		section = corpSection, position = 11)
	default boolean prayerIconEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "prayerOverlayEnabled", name = "Quick prayer overlay",
		description = "Show a fullscreen colour overlay when quick prayers are off",
		section = corpSection, position = 12)
	default boolean prayerOverlayEnabled()
	{
		return true;
	}

	@Alpha
	@ConfigItem(keyName = "prayerOverlayColor", name = "Quick prayer overlay colour",
		description = "Colour of the quick prayer fullscreen overlay",
		section = corpSection, position = 13)
	default Color prayerOverlayColor()
	{
		return new Color(255, 255, 255, 80);
	}

	@ConfigItem(keyName = "lunarsEnabled", name = "Lunar spellbook indicator",
		description = "Show an indicator when you are not on the Lunar spellbook inside the Corp Cave",
		section = corpSection, position = 14)
	default boolean lunarsEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "lunarIconEnabled", name = "Lunar spellbook icon",
		description = "Show the Lunar spellbook icon",
		section = corpSection, position = 15)
	default boolean lunarIconEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "lunarOverlayEnabled", name = "Lunar spellbook overlay",
		description = "Show a fullscreen colour overlay when not on Lunar spellbook",
		section = corpSection, position = 16)
	default boolean lunarOverlayEnabled()
	{
		return true;
	}

	@Alpha
	@ConfigItem(keyName = "lunarOverlayColor", name = "Lunar spellbook overlay colour",
		description = "Colour of the Lunar spellbook fullscreen overlay",
		section = corpSection, position = 17)
	default Color lunarOverlayColor()
	{
		return new Color(255, 220, 0, 100);
	}

	@ConfigItem(keyName = "bloodFuryEnabled", name = "Blood fury tracking",
		description = "Decrements a charge per attack animation. Right-click amulet and choose Check to resync.",
		section = corpSection, position = 18)
	default boolean bloodFuryEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "bloodFuryThreshold", name = "Blood fury warn threshold",
		description = "Show a warning when charges fall below this number",
		section = corpSection, position = 19)
	default int bloodFuryThreshold()
	{
		return 1000;
	}

	@ConfigItem(keyName = "runePouchEnabled", name = "Rune pouch warning",
		description = "Warn when any rune in your pouch drops below the threshold",
		section = corpSection, position = 20)
	default boolean runePouchEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "runePouchThreshold", name = "Rune pouch warn threshold",
		description = "Show a warning when a rune count falls below this number",
		section = corpSection, position = 21)
	default int runePouchThreshold()
	{
		return 1000;
	}

	@ConfigItem(keyName = "suppliesEnabled", name = "Bank supply warning",
		description = "Warn when your supply count falls below the threshold",
		section = corpSection, position = 22)
	default boolean suppliesEnabled()
	{
		return true;
	}

	@ConfigItem(keyName = "supplyType", name = "Supply type",
		description = "Which potion to track in your bank",
		section = corpSection, position = 23)
	default SupplyType supplyType()
	{
		return SupplyType.SUPER_RESTORE;
	}

	@ConfigItem(keyName = "supplyThreshold", name = "Bank supply warn threshold",
		description = "Show a warning when the supply count falls below this number",
		section = corpSection, position = 24)
	default int supplyThreshold()
	{
		return 100;
	}

	@ConfigItem(keyName = "houseTabEnabled", name = "House tab warning",
		description = "Warn when your Teleport to House tablet count falls below the threshold",
		section = corpSection, position = 25)
	default boolean houseTabEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "houseTabThreshold", name = "House tab warn threshold",
		description = "Show a warning when house tab count falls below this number",
		section = corpSection, position = 26)
	default int houseTabThreshold()
	{
		return 10;
	}

	@ConfigItem(keyName = "zulrahScalesEnabled", name = "Zulrah scales warning",
		description = "Warn when your Zulrah scales count falls below the threshold",
		section = corpSection, position = 27)
	default boolean zulrahScalesEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "zulrahScalesThreshold", name = "Zulrah scales warn threshold",
		description = "Show a warning when Zulrah scales fall below this number",
		section = corpSection, position = 28)
	default int zulrahScalesThreshold()
	{
		return 10000;
	}

	@ConfigItem(keyName = "tomeOfWaterEnabled", name = "Tome of Water tracking",
		description = "Track Tome of Water charges. Right-click the tome and choose Check to sync.",
		section = corpSection, position = 29)
	default boolean tomeOfWaterEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "tomeOfWaterThreshold", name = "Tome of Water warn threshold",
		description = "Show a warning when Tome of Water charges fall below this number",
		section = corpSection, position = 30)
	default int tomeOfWaterThreshold()
	{
		return 100;
	}

	@ConfigItem(keyName = "serpHelmEnabled", name = "Serpentine helm tracking",
		description = "Track Serpentine helm charges. Right-click the helm and choose Check to sync.",
		section = corpSection, position = 31)
	default boolean serpHelmEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "serpHelmThreshold", name = "Serpentine helm warn threshold",
		description = "Show a warning when Serpentine helm scales fall below this number (max 11,000)",
		section = corpSection, position = 32)
	default int serpHelmThreshold()
	{
		return 500;
	}

	@ConfigItem(keyName = "toxicStaffEnabled", name = "Toxic staff tracking",
		description = "Track Toxic Staff of the Dead charges. Right-click the staff and choose Check to sync.",
		section = corpSection, position = 33)
	default boolean toxicStaffEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "toxicStaffThreshold", name = "Toxic staff warn threshold",
		description = "Show a warning when Toxic Staff of the Dead scales fall below this number (max 11,000)",
		section = corpSection, position = 34)
	default int toxicStaffThreshold()
	{
		return 500;
	}

	// KQ

	@ConfigItem(keyName = "kqMovementLockEnabled", name = "Movement lock",
		description = "Consume walk clicks inside the KQ Cave unless the hold key is held (uses same key as Corp setting)",
		section = kqSection, position = 0)
	default boolean kqMovementLockEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqVengEnabled", name = "Vengeance indicator",
		description = "Show an indicator when Vengeance is ready to cast at KQ",
		section = kqSection, position = 1)
	default boolean kqVengEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqVengIconEnabled", name = "Vengeance icon",
		description = "Show the Vengeance spell icon",
		section = kqSection, position = 2)
	default boolean kqVengIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqVengOverlayEnabled", name = "Vengeance overlay",
		description = "Show a fullscreen colour overlay when Vengeance is ready",
		section = kqSection, position = 3)
	default boolean kqVengOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kqVengOverlayColor", name = "Vengeance overlay colour",
		description = "Colour of the KQ Vengeance overlay",
		section = kqSection, position = 4)
	default Color kqVengOverlayColor()
	{
		return new Color(255, 80, 80, 100);
	}

	@ConfigItem(keyName = "kqSaturatedHeartEnabled", name = "Saturated Heart indicator",
		description = "Show an alert when the Saturated Heart buff is not active (fires on entry and when buff expires)",
		section = kqSection, position = 5)
	default boolean kqSaturatedHeartEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqSaturatedIconEnabled", name = "Saturated Heart icon",
		description = "Show the Saturated Heart item icon",
		section = kqSection, position = 6)
	default boolean kqSaturatedIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqSaturatedOverlayEnabled", name = "Saturated Heart overlay",
		description = "Show a fullscreen colour overlay when the buff is not active",
		section = kqSection, position = 7)
	default boolean kqSaturatedOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kqSaturatedOverlayColor", name = "Saturated Heart overlay colour",
		description = "Colour of the Saturated Heart overlay",
		section = kqSection, position = 8)
	default Color kqSaturatedOverlayColor()
	{
		return new Color(180, 100, 255, 100);
	}

	@ConfigItem(keyName = "kqProtMageEnabled", name = "Protect from Magic indicator",
		description = "Show an alert when Protect from Magic is not active at KQ",
		section = kqSection, position = 9)
	default boolean kqProtMageEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqProtMageIconEnabled", name = "Protect from Magic icon",
		description = "Show the Prayer skill icon when Protect from Magic is off",
		section = kqSection, position = 10)
	default boolean kqProtMageIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqProtMageOverlayEnabled", name = "Protect from Magic overlay",
		description = "Show a fullscreen colour overlay when Protect from Magic is off",
		section = kqSection, position = 11)
	default boolean kqProtMageOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kqProtMageOverlayColor", name = "Protect from Magic overlay colour",
		description = "Colour of the Protect from Magic overlay",
		section = kqSection, position = 12)
	default Color kqProtMageOverlayColor()
	{
		return new Color(255, 255, 255, 80);
	}

	@ConfigItem(keyName = "kqSpecEnabled", name = "Special attack 100% indicator",
		description = "Show an alert when special attack energy is at 100%",
		section = kqSection, position = 13)
	default boolean kqSpecEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqSpecIconEnabled", name = "Special attack icon",
		description = "Show the Energy Transfer spell icon when spec is at 100%",
		section = kqSection, position = 14)
	default boolean kqSpecIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqSpecOverlayEnabled", name = "Special attack overlay",
		description = "Show a fullscreen colour overlay when spec is at 100%",
		section = kqSection, position = 15)
	default boolean kqSpecOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kqSpecOverlayColor", name = "Special attack overlay colour",
		description = "Colour of the 100% spec overlay",
		section = kqSection, position = 16)
	default Color kqSpecOverlayColor()
	{
		return new Color(255, 255, 100, 100);
	}

	@ConfigItem(keyName = "kqPrayerRegenEnabled", name = "Prayer Regen Potion indicator",
		description = "Show an alert when Prayer Regen Potion buff is not active (fires on entry and when buff expires)",
		section = kqSection, position = 17)
	default boolean kqPrayerRegenEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqPrayerRegenIconEnabled", name = "Prayer Regen Potion icon",
		description = "Show a Prayer Regen Potion icon",
		section = kqSection, position = 18)
	default boolean kqPrayerRegenIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqPrayerRegenOverlayEnabled", name = "Prayer Regen Potion overlay",
		description = "Show a fullscreen colour overlay when the buff is not active",
		section = kqSection, position = 19)
	default boolean kqPrayerRegenOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kqPrayerRegenOverlayColor", name = "Prayer Regen Potion overlay colour",
		description = "Colour of the Prayer Regen overlay",
		section = kqSection, position = 20)
	default Color kqPrayerRegenOverlayColor()
	{
		return new Color(110, 60, 51, 100);
	}

	@ConfigItem(keyName = "kqPoisonEnabled", name = "Poison indicator",
		description = "Show an alert when you are poisoned at KQ",
		section = kqSection, position = 21)
	default boolean kqPoisonEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqPoisonIconEnabled", name = "Poison icon",
		description = "Show the Cure Me spell icon when poisoned",
		section = kqSection, position = 22)
	default boolean kqPoisonIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqPoisonOverlayEnabled", name = "Poison overlay",
		description = "Show a fullscreen colour overlay when poisoned",
		section = kqSection, position = 23)
	default boolean kqPoisonOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kqPoisonOverlayColor", name = "Poison overlay colour",
		description = "Colour of the poison overlay",
		section = kqSection, position = 24)
	default Color kqPoisonOverlayColor()
	{
		return new Color(60, 200, 60, 100);
	}

	@ConfigItem(keyName = "kqLowPrayerEnabled", name = "Low prayer indicator",
		description = "Show an alert when your prayer points fall below the threshold at KQ",
		section = kqSection, position = 25)
	default boolean kqLowPrayerEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqLowPrayerIconEnabled", name = "Low prayer icon",
		description = "Show a Prayer Potion icon when prayer is low",
		section = kqSection, position = 26)
	default boolean kqLowPrayerIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kqLowPrayerOverlayEnabled", name = "Low prayer overlay",
		description = "Show a fullscreen colour overlay when prayer is low",
		section = kqSection, position = 27)
	default boolean kqLowPrayerOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kqLowPrayerOverlayColor", name = "Low prayer overlay colour",
		description = "Colour of the low prayer overlay",
		section = kqSection, position = 28)
	default Color kqLowPrayerOverlayColor()
	{
		return new Color(180, 180, 255, 100);
	}

	@ConfigItem(keyName = "kqPrayerThreshold", name = "Prayer threshold",
		description = "Show a prayer warning when prayer points fall below this number",
		section = kqSection, position = 29)
	default int kqPrayerThreshold()
	{
		return 5;
	}

	// MOLE

	@ConfigItem(keyName = "moleSaturatedHeartEnabled", name = "Saturated Heart indicator",
		description = "Show an alert when the Saturated Heart buff is not active (fires on entry and when buff expires)",
		section = moleSection, position = 0)
	default boolean moleSaturatedHeartEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "moleSaturatedIconEnabled", name = "Saturated Heart icon",
		description = "Show the Saturated Heart item icon",
		section = moleSection, position = 1)
	default boolean moleSaturatedIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "moleSaturatedOverlayEnabled", name = "Saturated Heart overlay",
		description = "Show a fullscreen colour overlay when the buff is not active",
		section = moleSection, position = 2)
	default boolean moleSaturatedOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "moleSaturatedOverlayColor", name = "Saturated Heart overlay colour",
		description = "Colour of the Saturated Heart overlay at Mole",
		section = moleSection, position = 3)
	default Color moleSaturatedOverlayColor()
	{
		return new Color(180, 100, 255, 100);
	}

	@ConfigItem(keyName = "moleSpecEnabled", name = "Special attack 100% indicator",
		description = "Show an alert when special attack energy is at 100% at Mole",
		section = moleSection, position = 4)
	default boolean moleSpecEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "moleSpecIconEnabled", name = "Special attack icon",
		description = "Show the Energy Transfer spell icon when spec is at 100%",
		section = moleSection, position = 5)
	default boolean moleSpecIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "moleSpecOverlayEnabled", name = "Special attack overlay",
		description = "Show a fullscreen colour overlay when spec is at 100% at Mole",
		section = moleSection, position = 6)
	default boolean moleSpecOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "moleSpecOverlayColor", name = "Special attack overlay colour",
		description = "Colour of the 100% spec overlay at Mole",
		section = moleSection, position = 7)
	default Color moleSpecOverlayColor()
	{
		return new Color(255, 255, 100, 100);
	}

	// KBD

	@ConfigItem(keyName = "kbdAntifireEnabled", name = "Extended Antifire indicator",
		description = "Show an alert when Extended Antifire buff is not active (fires on entry and when buff expires)",
		section = kbdSection, position = 0)
	default boolean kbdAntifireEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kbdAntifireIconEnabled", name = "Extended Antifire icon",
		description = "Show an Extended Antifire Potion icon",
		section = kbdSection, position = 1)
	default boolean kbdAntifireIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kbdAntifireOverlayEnabled", name = "Extended Antifire overlay",
		description = "Show a fullscreen colour overlay when the antifire buff is not active",
		section = kbdSection, position = 2)
	default boolean kbdAntifireOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kbdAntifireOverlayColor", name = "Extended Antifire overlay colour",
		description = "Colour of the antifire overlay",
		section = kbdSection, position = 3)
	default Color kbdAntifireOverlayColor()
	{
		return new Color(180, 80, 220, 100);
	}

	@ConfigItem(keyName = "kbdPoisonEnabled", name = "Poison indicator",
		description = "Show an alert when you are poisoned at KBD",
		section = kbdSection, position = 4)
	default boolean kbdPoisonEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kbdPoisonIconEnabled", name = "Poison icon",
		description = "Show an Araxyte Venom Sack icon when poisoned at KBD",
		section = kbdSection, position = 5)
	default boolean kbdPoisonIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kbdPoisonOverlayEnabled", name = "Poison overlay",
		description = "Show a fullscreen colour overlay when poisoned at KBD",
		section = kbdSection, position = 6)
	default boolean kbdPoisonOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kbdPoisonOverlayColor", name = "Poison overlay colour",
		description = "Colour of the poison overlay at KBD",
		section = kbdSection, position = 7)
	default Color kbdPoisonOverlayColor()
	{
		return new Color(60, 200, 60, 100);
	}

	@ConfigItem(keyName = "kbdSpecEnabled", name = "Special attack 100% indicator",
		description = "Show an alert when special attack energy is at 100% at KBD",
		section = kbdSection, position = 8)
	default boolean kbdSpecEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kbdSpecIconEnabled", name = "Special attack icon",
		description = "Show the Energy Transfer spell icon when spec is at 100%",
		section = kbdSection, position = 9)
	default boolean kbdSpecIconEnabled()
	{
		return false;
	}

	@ConfigItem(keyName = "kbdSpecOverlayEnabled", name = "Special attack overlay",
		description = "Show a fullscreen colour overlay when spec is at 100% at KBD",
		section = kbdSection, position = 10)
	default boolean kbdSpecOverlayEnabled()
	{
		return false;
	}

	@Alpha
	@ConfigItem(keyName = "kbdSpecOverlayColor", name = "Special attack overlay colour",
		description = "Colour of the 100% spec overlay at KBD",
		section = kbdSection, position = 11)
	default Color kbdSpecOverlayColor()
	{
		return new Color(255, 255, 100, 100);
	}
}
