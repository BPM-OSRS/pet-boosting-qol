# Corp Boosting QOL

A suite of quality-of-life tools designed for Corporeal Beast boosting and multi-logging.

## Features

### Combat Overlay
Fills the screen with a configurable colour when you are inside the Corp Cave but not in combat. Makes it immediately obvious which accounts are idle when managing multiple clients. Displays alongside alert overlays — all active indicators are visible at once.

### Vengeance Indicator
Shows a fullscreen colour overlay and/or the Vengeance spell icon centered on screen when Vengeance is ready to cast. Both the overlay and icon can be independently toggled, and the overlay colour is configurable. Suppressed once Vengeance is cast.

### Quick Prayer Indicator
Shows a fullscreen colour overlay and/or the Prayer skill icon centered on screen when quick prayers are not active inside the Corp Cave. Both the overlay and icon can be independently toggled with a configurable colour.

### Lunar Spellbook Indicator
Shows a fullscreen colour overlay and/or the Lunar spellbook icon centered on screen when you are not on the Lunar spellbook. Both the overlay and icon can be independently toggled with a configurable colour.

### Movement Lock
Prevents accidental walk clicks inside the Corp Cave. Hold a configurable key (default: `0`) to temporarily allow movement.

### Blood Fury Tracking
Tracks Amulet of Blood Fury charges by decrementing one charge per attack animation (Osmumten's Fang, Noxious Halberd, Elder Maul). Automatically updates when you recharge the amulet with a blood shard. Warns when charges drop below a configurable threshold. Charge counts persist across logouts per account. Right-click the amulet and choose **Check** at any time to resync. Warning shows **outside** the Corp Cave only.

### Rune Pouch Warning
Warns when any rune in your pouch drops below a configurable threshold. Shows a separate warning for each low rune. Visible inside the Corp Cave.

### Supply Warning
Warns when a tracked supply drops below a configurable count. Checks your bank and noted items in your inventory. Supports Super Restore, Sanfew Serum, Super Combat, Divine Super Combat, and Anglerfish. Warning shows **outside** the Corp Cave only. Count persists across logouts.

### House Teleport Tab Warning
Warns when your Teleport to House tablet count (bank + inventory) falls below a configurable threshold. Warning shows **outside** the Corp Cave only. Count persists across logouts.

### Splasher Charge Tracking
Tracks charges for items used by splasher accounts. All values persist across logouts. Right-click each item and choose **Check** to sync at any time.

- **Tome of Water** — decrements one charge per Water Strike cast
- **Serpentine Helm** — supports regular, magma, and tanzanite variants; tracks scales via right-click Check
- **Toxic Staff of the Dead** — supports standard and DMM variants; tracks scales via right-click Check
- **Zulrah Scales** — warns when bank + inventory scales fall below a configurable threshold (default 10,000)

## Notes
- Combat detection supports Osmumten's Fang, Noxious Halberd (both attack styles), and Elder Maul.
- Multiple alert overlays (combat, vengeance, prayer, spellbook) are all visible simultaneously — they layer on top of each other rather than suppressing one another.
- All charge and supply counts persist across logouts per account.
- Supply and house tab warnings update when you open your bank or when your inventory changes.
- Serp helm and toxic staff both show the same "Scales: X" message on Check — the plugin distinguishes them by which item you right-clicked.
- The warning overlay colour is fully configurable via the Corp Cave settings panel.
