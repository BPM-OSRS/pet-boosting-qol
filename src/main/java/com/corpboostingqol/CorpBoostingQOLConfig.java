package com.corpboostingqol;

import java.awt.Color;
import java.awt.event.KeyEvent;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;

@ConfigGroup("corpboostingqol")
public interface CorpBoostingQOLConfig extends Config
{
	// -------------------------------------------------------------------------
	// Sections
	// -------------------------------------------------------------------------

	@ConfigSection(name = "Corp Cave",    description = "Features that only apply inside the Corporeal Beast cave", position = 0)
	String corpCaveSection = "corpcave";

	@ConfigSection(name = "Vengeance",    description = "Vengeance ready indicator settings",   position = 1)
	String vengSection = "vengeance";

	@ConfigSection(name = "Quick Prayer", description = "Quick prayer down indicator settings",  position = 2)
	String prayerSection = "prayer";

	@ConfigSection(name = "Lunar Spellbook", description = "Wrong spellbook indicator settings", position = 3)
	String lunarSection = "lunar";

	@ConfigSection(name = "Supplies",     description = "Bank supply warnings and rune pouch tracking", position = 4)
	String suppliesSection = "supplies";

	@ConfigSection(name = "Splasher",     description = "Charge tracking for Tome of Water, Serpentine Helm, and Toxic Staff of the Dead", position = 5)
	String splasherSection = "splasher";

	// -------------------------------------------------------------------------
	// Warning Overlay Colour
	// -------------------------------------------------------------------------

	@Alpha
	@ConfigItem(keyName = "warningOverlayColor", name = "Warning box colour",
			description = "Background colour of the warning text boxes", section = corpCaveSection, position = 0)
	default Color warningOverlayColor() { return new Color(150, 0, 0, 230); }

	// -------------------------------------------------------------------------
	// Corp Cave — Combat Overlay
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "combatOverlayEnabled", name = "Combat overlay",
			description = "Show a fullscreen colour overlay when idle inside the Corp Cave",
			section = corpCaveSection, position = 1)
	default boolean combatOverlayEnabled() { return true; }

	@ConfigItem(keyName = "combatIconEnabled", name = "Combat idle icon",
			description = "Show the Attack skill icon when idle inside the Corp Cave",
			section = corpCaveSection, position = 2)
	default boolean combatIconEnabled() { return true; }

	@Alpha
	@ConfigItem(keyName = "combatOverlayColor", name = "Combat overlay colour",
			description = "Colour and opacity of the combat overlay",
			section = corpCaveSection, position = 3)
	default Color combatOverlayColor() { return new Color(255, 0, 0, 120); }

	// -------------------------------------------------------------------------
	// Corp Cave — Movement Lock
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "movementLockEnabled", name = "Movement lock",
			description = "Consume walk clicks inside the Corp Cave unless the key below is held",
			section = corpCaveSection, position = 4)
	default boolean movementLockEnabled() { return true; }

	@ConfigItem(keyName = "movementHoldKey", name = "Hold key to walk",
			description = "Hold this key to allow walking while movement lock is active",
			section = corpCaveSection, position = 5)
	default Keybind movementHoldKey() { return new Keybind(KeyEvent.VK_0, 0); }

	// -------------------------------------------------------------------------
	// Vengeance Section
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "vengEnabled", name = "Vengeance indicator enabled",
			description = "Show an indicator when Vengeance is ready to cast",
			section = vengSection, position = 0)
	default boolean vengEnabled() { return true; }

	@ConfigItem(keyName = "vengIconEnabled", name = "Show icon",
			description = "Show the Vengeance spell icon in the center of the screen",
			section = vengSection, position = 1)
	default boolean vengIconEnabled() { return true; }

	@ConfigItem(keyName = "vengOverlayEnabled", name = "Show overlay",
			description = "Show a fullscreen colour overlay when Vengeance is ready",
			section = vengSection, position = 2)
	default boolean vengOverlayEnabled() { return true; }

	@Alpha
	@ConfigItem(keyName = "vengOverlayColor", name = "Overlay colour",
			description = "Colour of the Vengeance fullscreen overlay",
			section = vengSection, position = 3)
	default Color vengOverlayColor() { return new Color(255, 80, 80, 100); }

	// -------------------------------------------------------------------------
	// Quick Prayer Section
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "quickPrayerEnabled", name = "Quick prayer indicator enabled",
			description = "Show an indicator when quick prayers are not active inside the Corp Cave",
			section = prayerSection, position = 0)
	default boolean quickPrayerEnabled() { return true; }

	@ConfigItem(keyName = "prayerIconEnabled", name = "Show icon",
			description = "Show the Prayer skill icon in the center of the screen",
			section = prayerSection, position = 1)
	default boolean prayerIconEnabled() { return true; }

	@ConfigItem(keyName = "prayerOverlayEnabled", name = "Show overlay",
			description = "Show a fullscreen colour overlay when quick prayers are off",
			section = prayerSection, position = 2)
	default boolean prayerOverlayEnabled() { return true; }

	@Alpha
	@ConfigItem(keyName = "prayerOverlayColor", name = "Overlay colour",
			description = "Colour of the Prayer fullscreen overlay",
			section = prayerSection, position = 3)
	default Color prayerOverlayColor() { return new Color(255, 255, 255, 80); }

	// -------------------------------------------------------------------------
	// Lunar Spellbook Section
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "lunarsEnabled", name = "Lunar spellbook indicator enabled",
			description = "Show an indicator when you are not on the Lunar spellbook inside the Corp Cave",
			section = lunarSection, position = 0)
	default boolean lunarsEnabled() { return true; }

	@ConfigItem(keyName = "lunarIconEnabled", name = "Show icon",
			description = "Show the Lunar spellbook icon in the center of the screen",
			section = lunarSection, position = 1)
	default boolean lunarIconEnabled() { return true; }

	@ConfigItem(keyName = "lunarOverlayEnabled", name = "Show overlay",
			description = "Show a fullscreen colour overlay when not on Lunar spellbook",
			section = lunarSection, position = 2)
	default boolean lunarOverlayEnabled() { return true; }

	@Alpha
	@ConfigItem(keyName = "lunarOverlayColor", name = "Overlay colour",
			description = "Colour of the Lunar spellbook fullscreen overlay",
			section = lunarSection, position = 3)
	default Color lunarOverlayColor() { return new Color(255, 220, 0, 100); }

	// -------------------------------------------------------------------------
	// Supplies — Blood Fury
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "bloodFuryEnabled", name = "Blood fury tracking",
			description = "Decrements a charge for every attack animation (Fang, Nox Halberd, Elder Maul). Right-click your amulet and choose Check to resync. Warning shows outside Corp Cave only.",
			section = suppliesSection, position = 0)
	default boolean bloodFuryEnabled() { return true; }

	@ConfigItem(keyName = "bloodFuryThreshold", name = "Blood fury warn threshold",
			description = "Show a warning when charges fall below this number",
			section = suppliesSection, position = 1)
	default int bloodFuryThreshold() { return 1000; }

	// -------------------------------------------------------------------------
	// Supplies — Rune Pouch
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "runePouchEnabled", name = "Rune pouch warning",
			description = "Warn when any rune in your pouch drops below the threshold",
			section = suppliesSection, position = 2)
	default boolean runePouchEnabled() { return true; }

	@ConfigItem(keyName = "runePouchThreshold", name = "Rune pouch warn threshold",
			description = "Show a warning when a rune count falls below this number",
			section = suppliesSection, position = 3)
	default int runePouchThreshold() { return 1000; }

	// -------------------------------------------------------------------------
	// Supplies — Bank Supplies
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "suppliesEnabled", name = "Bank supply warning",
			description = "Warn when your supply count falls below the threshold. Checks bank and noted items in inventory. Warning shows outside Corp Cave only.",
			section = suppliesSection, position = 4)
	default boolean suppliesEnabled() { return true; }

	@ConfigItem(keyName = "supplyType", name = "Supply type",
			description = "Which potion to track in your bank",
			section = suppliesSection, position = 5)
	default SupplyType supplyType() { return SupplyType.SUPER_RESTORE; }

	@ConfigItem(keyName = "supplyThreshold", name = "Bank supply warn threshold",
			description = "Show a warning when the supply count falls below this number",
			section = suppliesSection, position = 6)
	default int supplyThreshold() { return 100; }

	// -------------------------------------------------------------------------
	// Supplies — House Tabs
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "houseTabEnabled", name = "House tab warning",
			description = "Warn when your Teleport to House tablet count falls below the threshold. Checks bank and inventory. Warning shows outside Corp Cave only.",
			section = suppliesSection, position = 7)
	default boolean houseTabEnabled() { return false; }

	@ConfigItem(keyName = "houseTabThreshold", name = "House tab warn threshold",
			description = "Show a warning when house tab count falls below this number",
			section = suppliesSection, position = 8)
	default int houseTabThreshold() { return 10; }

	// -------------------------------------------------------------------------
	// Splasher — Tome of Water
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "tomeOfWaterEnabled", name = "Tome of Water tracking",
			description = "Track Tome of Water charges. Right-click the tome and choose Check to sync.",
			section = splasherSection, position = 0)
	default boolean tomeOfWaterEnabled() { return false; }

	@ConfigItem(keyName = "tomeOfWaterThreshold", name = "Tome of Water warn threshold",
			description = "Show a warning when Tome of Water charges fall below this number",
			section = splasherSection, position = 1)
	default int tomeOfWaterThreshold() { return 100; }

	// -------------------------------------------------------------------------
	// Splasher — Serpentine Helm
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "serpHelmEnabled", name = "Serpentine helm tracking",
			description = "Track Serpentine helm charges. Right-click the helm and choose Check to sync.",
			section = splasherSection, position = 2)
	default boolean serpHelmEnabled() { return false; }

	@ConfigItem(keyName = "serpHelmThreshold", name = "Serpentine helm warn threshold",
			description = "Show a warning when Serpentine helm scales fall below this number (max 11,000). e.g. 2200 = ~20%",
			section = splasherSection, position = 3)
	default int serpHelmThreshold() { return 500; }

	// -------------------------------------------------------------------------
	// Splasher — Toxic Staff of the Dead
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "toxicStaffEnabled", name = "Toxic staff tracking",
			description = "Track Toxic Staff of the Dead charges. Right-click the staff and choose Check to sync.",
			section = splasherSection, position = 4)
	default boolean toxicStaffEnabled() { return false; }

	@ConfigItem(keyName = "toxicStaffThreshold", name = "Toxic staff warn threshold",
			description = "Show a warning when Toxic Staff of the Dead scales fall below this number (max 11,000). e.g. 2200 = ~20%",
			section = splasherSection, position = 5)
	default int toxicStaffThreshold() { return 500; }

	// -------------------------------------------------------------------------
	// Splasher — Zulrah Scales
	// -------------------------------------------------------------------------

	@ConfigItem(keyName = "zulrahScalesEnabled", name = "Zulrah scales warning",
			description = "Warn when your Zulrah scales count (bank + noted inventory) falls below the threshold.",
			section = splasherSection, position = 6)
	default boolean zulrahScalesEnabled() { return false; }

	@ConfigItem(keyName = "zulrahScalesThreshold", name = "Zulrah scales warn threshold",
			description = "Show a warning when Zulrah scales fall below this number",
			section = splasherSection, position = 7)
	default int zulrahScalesThreshold() { return 10000; }
}
