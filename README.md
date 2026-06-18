# Pet Boosting QOL

A suite of quality-of-life tools for pet boosting at Corporeal Beast, Kalphite Queen, Giant Mole, and King Black Dragon.

## Corporeal Beast

### Combat Overlay
Fills the screen with a configurable colour when you are inside the Corp Cave but not in combat. Makes it immediately obvious which accounts are idle when managing multiple clients.

### Movement Lock
Prevents accidental walk clicks inside the Corp Cave. Hold a configurable key (default: `0`) to temporarily allow movement.

### Vengeance Indicator
Shows a fullscreen colour overlay and/or the Vengeance spell icon when Vengeance is ready to cast. Suppressed once Vengeance is cast.

### Quick Prayer Indicator
Shows a fullscreen colour overlay and/or the Prayer skill icon when quick prayers are not active inside the Corp Cave.

### Lunar Spellbook Indicator
Shows a fullscreen colour overlay and/or the Lunar spellbook icon when you are not on the Lunar spellbook inside the Corp Cave.

### Blood Fury Tracking
Tracks Amulet of Blood Fury charges by decrementing one charge per attack animation (Osmumten's Fang, Noxious Halberd, Elder Maul). Warns when charges drop below a configurable threshold. Right-click the amulet and choose **Check** to resync at any time. Charge counts persist across logouts per account.

### Rune Pouch Warning
Warns when any rune in your pouch drops below a configurable threshold.

### Supply Warning
Warns when a tracked supply drops below a configurable count. Checks your bank and noted items in your inventory. Supports Super Restore, Sanfew Serum, Super Combat, Divine Super Combat, and Anglerfish.

### House Teleport Tab Warning
Warns when your Teleport to House tablet count (bank + inventory) falls below a configurable threshold.

### Splasher Charge Tracking
Tracks charges for Tome of Water, Serpentine Helm, Toxic Staff of the Dead, and Zulrah Scales. All values persist across logouts. Right-click each item and choose **Check** to sync.

## Kalphite Queen

### Movement Lock
Prevents accidental walk clicks inside the KQ Cave. Uses the same hold key as the Corp movement lock setting.

### Vengeance Indicator
Shows a fullscreen colour overlay and/or the Vengeance spell icon when Vengeance is ready to cast at KQ.

### Saturated Heart Indicator
Shows an alert when the Saturated Heart buff is not active. Fires on entry if the buff is inactive, and again when the buff expires.

### Protect from Magic Indicator
Shows an alert when Protect from Magic is not active at KQ.

### Special Attack Indicator
Shows an alert when special attack energy reaches 100%.

### Prayer Regen Potion Indicator
Shows an alert when the Prayer Regeneration Potion buff is not active. Fires on entry if the buff is inactive, and again when the buff expires.

### Poison Indicator
Shows an alert when you are poisoned at KQ.

### Low Prayer Indicator
Shows an alert when your prayer points fall below a configurable threshold at KQ.

## Giant Mole

### Saturated Heart Indicator
Shows an alert when the Saturated Heart buff is not active at Mole. Fires on entry if the buff is inactive, and again when the buff expires.

### Special Attack Indicator
Shows an alert when special attack energy reaches 100% at Mole.

## King Black Dragon

### Extended Antifire Indicator
Shows an alert when the Extended Antifire buff is not active at KBD. Fires on entry if the buff is inactive, and again when the buff expires.

### Poison Indicator
Shows an alert when you are poisoned at KBD.

### Special Attack Indicator
Shows an alert when special attack energy reaches 100% at KBD.

## Notes

- All KQ, Mole, and KBD features default to off.
- All indicators have individually toggleable icon, fullscreen overlay, and custom overlay colour.
- Multiple alert overlays are all visible simultaneously — they layer rather than suppress each other.
- All charge and supply counts persist across logouts per account.
