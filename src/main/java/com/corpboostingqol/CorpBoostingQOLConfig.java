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

	@ConfigSection(
			name = "Corp Cave",
			description = "Features that only apply inside the Corporeal Beast cave",
			position = 0
	)
	String corpCaveSection = "corpcave";

	@ConfigSection(
			name = "Supplies",
			description = "Bank supply warnings and rune pouch tracking",
			position = 1
	)
	String suppliesSection = "supplies";

	@ConfigSection(
			name = "Splasher",
			description = "Charge tracking for Tome of Water, Serpentine Helm, and Toxic Staff of the Dead",
			position = 2
	)
	String splasherSection = "splasher";

	// -------------------------------------------------------------------------
	// Warning Overlay Color
	// -------------------------------------------------------------------------

	@Alpha
	@ConfigItem(
			keyName = "warningOverlayColor",
			name = "Warning overlay colour",
			description = "Background colour of the warning notification boxes",
			section = corpCaveSection,
			position = 0
	)
	default Color warningOverlayColor()
	{
		return new Color(150, 0, 0, 230);
	}

	// -------------------------------------------------------------------------
	// Corp Cave — Combat Overlay
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "combatOverlayEnabled",
			name = "Combat overlay",
			description = "Fills the screen with the colour below when you are not in combat inside the Corp Cave",
			section = corpCaveSection,
			position = 0
	)
	default boolean combatOverlayEnabled()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "combatOverlayColor",
			name = "Combat overlay colour",
			description = "Colour and opacity of the combat overlay",
			section = corpCaveSection,
			position = 1
	)
	default Color combatOverlayColor()
	{
		return new Color(255, 0, 0, 120);
	}

	// -------------------------------------------------------------------------
	// Corp Cave — Vengeance
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "vengEnabled",
			name = "Vengeance indicator",
			description = "Show a warning when Vengeance is ready to cast",
			section = corpCaveSection,
			position = 2
	)
	default boolean vengEnabled()
	{
		return true;
	}

	// -------------------------------------------------------------------------
	// Corp Cave — Movement Lock
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "movementLockEnabled",
			name = "Movement lock",
			description = "Consume walk clicks inside the Corp Cave unless the key below is held",
			section = corpCaveSection,
			position = 3
	)
	default boolean movementLockEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "movementHoldKey",
			name = "Hold key to walk",
			description = "Hold this key to allow walking while movement lock is active",
			section = corpCaveSection,
			position = 4
	)
	default Keybind movementHoldKey()
	{
		return new Keybind(KeyEvent.VK_0, 0);
	}

	// -------------------------------------------------------------------------
	// Corp Cave — Quick Prayers
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "quickPrayerEnabled",
			name = "Quick prayer warning",
			description = "Warn when quick prayers are not active inside the Corp Cave",
			section = corpCaveSection,
			position = 5
	)
	default boolean quickPrayerEnabled()
	{
		return true;
	}

	// -------------------------------------------------------------------------
	// Corp Cave — Lunar Spellbook
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "lunarsEnabled",
			name = "Lunar spellbook warning",
			description = "Warn when you are not on the Lunar spellbook inside the Corp Cave",
			section = corpCaveSection,
			position = 6
	)
	default boolean lunarsEnabled()
	{
		return true;
	}

	// -------------------------------------------------------------------------
	// Supplies — Blood Fury
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "bloodFuryEnabled",
			name = "Blood fury tracking",
			description = "Decrements a charge for every attack animation (Fang, Nox Halberd, Elder Maul). Right-click your amulet and choose Check to resync.",
			section = suppliesSection,
			position = 0
	)
	default boolean bloodFuryEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "bloodFuryThreshold",
			name = "Blood fury warn threshold",
			description = "Show a warning when charges fall below this number",
			section = suppliesSection,
			position = 1
	)
	default int bloodFuryThreshold()
	{
		return 1000;
	}

	// -------------------------------------------------------------------------
	// Supplies — Rune Pouch
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "runePouchEnabled",
			name = "Rune pouch warning",
			description = "Warn when any rune in your pouch drops below the threshold",
			section = suppliesSection,
			position = 2
	)
	default boolean runePouchEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "runePouchThreshold",
			name = "Rune pouch warn threshold",
			description = "Show a warning when a rune count falls below this number",
			section = suppliesSection,
			position = 3
	)
	default int runePouchThreshold()
	{
		return 1000;
	}

	// -------------------------------------------------------------------------
	// Supplies — Bank Supplies
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "suppliesEnabled",
			name = "Bank supply warning",
			description = "Warn when your bank supply count falls below the threshold. Updates each time you open the bank.",
			section = suppliesSection,
			position = 4
	)
	default boolean suppliesEnabled()
	{
		return true;
	}

	@ConfigItem(
			keyName = "supplyType",
			name = "Supply type",
			description = "Which potion to track in your bank",
			section = suppliesSection,
			position = 5
	)
	default SupplyType supplyType()
	{
		return SupplyType.SUPER_RESTORE;
	}

	@ConfigItem(
			keyName = "supplyThreshold",
			name = "Bank supply warn threshold",
			description = "Show a warning when the supply count falls below this number",
			section = suppliesSection,
			position = 6
	)
	default int supplyThreshold()
	{
		return 100;
	}

	// -------------------------------------------------------------------------
	// Splasher — Tome of Water
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "tomeOfWaterEnabled",
			name = "Tome of Water tracking",
			description = "Track Tome of Water charges. Right-click the tome and choose Check to sync.",
			section = splasherSection,
			position = 0
	)
	default boolean tomeOfWaterEnabled()
	{
		return false;
	}

	@ConfigItem(
			keyName = "tomeOfWaterThreshold",
			name = "Tome of Water warn threshold",
			description = "Show a warning when Tome of Water charges fall below this number",
			section = splasherSection,
			position = 1
	)
	default int tomeOfWaterThreshold()
	{
		return 100;
	}

	// -------------------------------------------------------------------------
	// Splasher — Serpentine Helm
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "serpHelmEnabled",
			name = "Serpentine helm tracking",
			description = "Track Serpentine helm charges. Right-click the helm and choose Check to sync.",
			section = splasherSection,
			position = 2
	)
	default boolean serpHelmEnabled()
	{
		return false;
	}

	@ConfigItem(
			keyName = "serpHelmThreshold",
			name = "Serpentine helm warn threshold",
			description = "Show a warning when Serpentine helm scales fall below this number (max 11,000). e.g. 2200 = ~20%",
			section = splasherSection,
			position = 3
	)
	default int serpHelmThreshold()
	{
		return 500;
	}

	// -------------------------------------------------------------------------
	// Splasher — Toxic Staff of the Dead
	// -------------------------------------------------------------------------

	@ConfigItem(
			keyName = "toxicStaffEnabled",
			name = "Toxic staff tracking",
			description = "Track Toxic Staff of the Dead charges. Right-click the staff and choose Check to sync.",
			section = splasherSection,
			position = 4
	)
	default boolean toxicStaffEnabled()
	{
		return false;
	}

	@ConfigItem(
			keyName = "toxicStaffThreshold",
			name = "Toxic staff warn threshold",
			description = "Show a warning when Toxic Staff of the Dead scales fall below this number (max 11,000). e.g. 2200 = ~20%",
			section = splasherSection,
			position = 5
	)
	default int toxicStaffThreshold()
	{
		return 500;
	}
}
