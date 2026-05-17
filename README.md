# Corp Boosting QOL

A suite of quality-of-life tools designed for Corporeal Beast boosting and multi-logging.

![Corp Boosting QOL Demo](https://i.imgur.com/OwZxHSZ.gif)

## Features

### Combat Overlay
Fills the screen with a configurable colour when you are inside the Corp Cave but not in combat. Makes it immediately obvious which accounts are idle when managing multiple clients.

### Vengeance Tracker
Shows a warning when Vengeance is not active and the cooldown has fully expired, prompting you to recast.

### Lunar Spellbook Warning
Warns when you are not on the Lunar spellbook while inside the Corp Cave.

### Quick Prayer Warning
Warns when quick prayers are not active while inside the Corp Cave.

### Movement Lock
Prevents accidental walk clicks inside the Corp Cave. Hold a configurable key (default: `0`) to temporarily allow movement.

### Blood Fury Tracking
Tracks Amulet of Blood Fury charges by counting hitsplats landed. Warns when charges drop below a configurable threshold. Charge counts persist across logouts per account. Right-click the amulet and choose **Check** at any time to resync.

### Rune Pouch Warning
Warns when any rune in your pouch drops below a configurable threshold. Shows a separate warning for each low rune.

### Supply Warning
Warns when a tracked potion (Super Restore, Sanfew Serum, or Super Combat) drops below a configurable count in your bank. Updates each time you open the bank.

### Splasher Charge Tracking
Tracks charges for the three items used by splasher accounts. All values persist across logouts. Right-click each item and choose **Check** to sync at any time.

- **Tome of Water** — decrements one charge per Water Strike cast
- **Serpentine Helm** — tracks scales via right-click Check
- **Toxic Staff of the Dead** — tracks scales via right-click Check

## Notes
- Combat detection supports Osmumten's Fang, Noxious Halberd, and Elder Maul.
- Blood fury, tome, serp helm, and toxic staff charge counts all persist across logouts per account.
- Supply warnings only update when the bank is opened.
- Serp helm and toxic staff both show the same "Scales: X" message on Check — the plugin distinguishes them by which item you right-clicked.
- - Warning overlay background colour is fully configurable via the Corp Cave settings panel.
