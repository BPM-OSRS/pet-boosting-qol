package com.corpboostingqol;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

@PluginDescriptor(
		name = "Corp Boosting QOL",
		description = "Boosting QOL for Corp: combat overlay, vengeance/lunars indicator, movement lock, blood fury/rune pouch/supply/splasher charge tracking.",
		tags = {"corp", "boost", "vengeance", "combat", "overlay", "blood fury", "rune pouch", "corporeal beast", "splash", "tome", "serp", "toxic staff"}
)
public class CorpBoostingQOLPlugin extends Plugin
{

	static final int CORP_CAVE_REGION        = 11844;
	private static final int VENG_COOLDOWN_TICKS = 50;
	private static final int VENG_GRAPHIC        = 726;
	private static final int FANG_ATTACK_ANIM    = 9471;
	private static final int COMBAT_IDLE_TIMEOUT = 10;
	private static final int SPELLBOOK_VARBIT    = 4070;
	private static final int LUNAR_SPELLBOOK     = 2;

	// Water Strike cast animation — this is the Toxic Staff of the Dead cast animation (11423),
	// distinct from the standard Water Strike animation (711). Used for splasher tome tracking.
	private static final int WATER_STRIKE_ANIM  = 11423;
	private static final int NOX_HALBERD_ANIM   = 440; // slash/swipe style
	private static final int NOX_HALBERD_ANIM2  = 428; // jab/stab style
	private static final int ELDER_MAUL_ANIM    = 7516;

	// Serp helm: 10 scales on combat enter, then 10 more every 90 ticks.
	// Toxic staff: 10 scales on combat enter, then 10 more every 100 ticks.
	private static final int SERP_DRAIN_INTERVAL    = 90;
	private static final int STAFF_DRAIN_INTERVAL   = 100;
	private static final int SPLASHER_IDLE_TIMEOUT  = 6;

	// Varbit IDs for serp helm and toxic staff charges.
	// Primary source of truth if correct; Check message parsing is the fallback.
	private static final int SERP_HELM_CHARGES_VARBIT   = 6668;
	private static final int TOXIC_STAFF_CHARGES_VARBIT = 6669;

	// Equipment item IDs
	private static final int ITEM_SERP_HELM          = 12931; // charged serpentine helm
	private static final int ITEM_SERP_HELM_MAGMA    = 13197;
	private static final int ITEM_SERP_HELM_TANZANITE = 13199;
	private static final int ITEM_TOXIC_STAFF        = 12904; // toxic staff of the dead (charged)
	private static final int ITEM_TOXIC_STAFF_DMM    = 33036; // DMM toxic staff
	private static final int ITEM_TOME_OF_WATER      = 25616; // charged tome of water
	private static final int ITEM_HOUSE_TAB          = 8013;
	private static final int ITEM_ZULRAH_SCALES      = 12934;

	// Equipment panel widget IDs for the Check menu option (param1 in MenuOptionClicked).
	// These are the packed widget IDs for interface 387 (equipment panel):
	//   component 15 = head slot  → (387 << 16) | 15 = 25362447
	//   component 18 = weapon slot → (387 << 16) | 18 = 25362450
	// If Check detection ever breaks after an RS update, re-confirm with:
	//   log.info("Check param1={}", event.getParam1()) after right-clicking the item.
	private static final int WIDGET_EQUIP_SERP_HELM  = 25362447; // interface 387, component 15
	private static final int WIDGET_EQUIP_TOXIC_STAFF = 25362450; // interface 387, component 18

	private static final Pattern BLOOD_FURY_CHECK_PATTERN =
			Pattern.compile("Your Amulet of blood fury will work for ([\\d,]+) more hits\\.");
	private static final Pattern BLOOD_FURY_RECHARGE_PATTERN =
			Pattern.compile("It will now work for ([\\d,]+) more hits\\.");

	// Case-insensitive, trailing punctuation optional — covers likely game message variants.
	// "Your tome currently holds 3,520 charges." (exact game message)
	private static final Pattern TOME_OF_WATER_CHECK_PATTERN =
			Pattern.compile("Your tome currently holds ([\\d,]+) charges?");
	// "Scales: 10,410 (94.6%)" — right-click Check on serp helm or toxic staff
	private static final Pattern SCALES_CHECK_PATTERN =
			Pattern.compile("Scales: ([\\d,]+) \\(");

	private static final String CONFIG_GROUP          = "corpboostingqol";
	private static final String BLOOD_FURY_KEY_PREFIX = "bloodfury_charges_";
	private static final String TOME_KEY_PREFIX       = "tome_charges_";
	private static final String SERP_KEY_PREFIX       = "serp_charges_";
	private static final String STAFF_KEY_PREFIX      = "staff_charges_";
	private static final String SUPPLY_KEY_PREFIX     = "supply_count_";
	private static final String HOUSE_TAB_KEY_PREFIX  = "housetab_count_";
	private static final String SCALES_KEY_PREFIX     = "scales_count_";

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CorpBoostingQOLCombatOverlay combatOverlay;

	@Inject
	private CorpBoostingQOLWarningOverlay warningOverlay;

	@Inject
	private CorpBoostingQOLAlertOverlay alertOverlay;

	@Inject
	private CorpBoostingQOLConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private KeyManager keyManager;

	boolean inCorpCave   = false;
	boolean inCombat     = false;
	boolean vengReady    = false;
	boolean lunarsWarn   = false;

	int     bloodFuryCharges = -1;
	boolean bloodFuryWarn    = false;
	boolean bloodFuryNoData  = false;

	List<String> runePouchWarnings = new ArrayList<>();

	boolean supplyWarn  = false;
	int     supplyCount = -1;
	private int cachedBankSupplyCount = -1; // last known bank quantity; survives bank close

	boolean houseTabWarn  = false;
	int     houseTabCount = -1;
	private int cachedBankHouseTabCount = -1;

	boolean zulrahScalesWarn  = false;
	int     zulrahScalesCount = -1;
	private int cachedBankScalesCount = -1;

	boolean quickPrayerWarn = false;

	// Splasher
	int     tomeOfWaterCharges = -1;
	boolean tomeOfWaterWarn    = false;
	boolean tomeOfWaterNoData  = true;

	int     serpHelmCharges = -1;
	boolean serpHelmWarn    = false;
	boolean serpHelmNoData  = true;

	int     toxicStaffCharges = -1;
	boolean toxicStaffWarn    = false;
	boolean toxicStaffNoData  = true;

	private boolean vengActive        = false;
	private int     vengCooldownTicks = 0;
	private int     combatIdleTicks   = 0;
	private boolean runePouchDirty    = false;
	private boolean bloodFuryLoaded   = false;
	private boolean isHotkeyHeld      = false;
	private HotkeyListener hotkeyListener;

	private boolean supplyLoaded            = false;
	private boolean houseTabLoaded          = false;
	private boolean scalesLoaded            = false;

	private boolean splasherInCombat        = false;
	private int     splasherIdleTicks       = 0;
	private int     serpDrainTicks          = 0;
	private int     staffDrainTicks         = 0;
	private boolean serpCombatEnterDrained  = false;
	private boolean staffCombatEnterDrained = false;
	private boolean splasherLoaded          = false;
	private boolean bankDataLoaded          = false;

	private enum ScalesItem { NONE, SERP, STAFF }
	private ScalesItem pendingScalesCheck = ScalesItem.NONE;

	@Override
	protected void startUp()
	{
		overlayManager.add(combatOverlay);
		overlayManager.add(warningOverlay);
		overlayManager.add(alertOverlay);

		hotkeyListener = new HotkeyListener(() -> config.movementHoldKey())
		{
			@Override public void hotkeyPressed()  { isHotkeyHeld = true;  }
			@Override public void hotkeyReleased() { isHotkeyHeld = false; }
		};
		keyManager.registerKeyListener(hotkeyListener);

		loadBloodFuryCharges();
		loadTomeCharges();
		loadSerpCharges();
		loadStaffCharges();
		loadSupplyCount();
		loadHouseTabCount();
		loadScalesCount();
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(combatOverlay);
		overlayManager.remove(warningOverlay);
		overlayManager.remove(alertOverlay);
		keyManager.unregisterKeyListener(hotkeyListener);
		hotkeyListener = null;
		resetState();
	}

	private void resetState()
	{
		inCorpCave        = false;
		inCombat          = false;
		vengActive        = false;
		vengReady         = false;
		lunarsWarn        = false;
		vengCooldownTicks = 0;
		combatIdleTicks   = 0;

		bloodFuryCharges  = -1;
		bloodFuryWarn     = false;
		// Intentionally false here (not true): on login the overlay check
		// bloodFuryNoData || bloodFuryCharges == -1 still shows the "inspect" prompt
		// because charges are -1. Setting noData=false avoids a double-trigger on startup
		// before loadBloodFuryCharges() has had a chance to run.
		bloodFuryNoData   = true;
		bloodFuryLoaded   = false;

		runePouchWarnings = new ArrayList<>();
		supplyWarn        = false;
		supplyCount       = -1;
		cachedBankSupplyCount   = -1;
		houseTabWarn      = false;
		houseTabCount     = -1;
		cachedBankHouseTabCount = -1;
		zulrahScalesWarn  = false;
		zulrahScalesCount = -1;
		cachedBankScalesCount   = -1;
		quickPrayerWarn   = false;
		runePouchDirty    = false;
		isHotkeyHeld      = false;

		tomeOfWaterCharges      = -1;
		tomeOfWaterWarn         = false;
		tomeOfWaterNoData       = true;
		serpHelmCharges         = -1;
		serpHelmWarn            = false;
		serpHelmNoData          = true;
		toxicStaffCharges       = -1;
		toxicStaffWarn          = false;
		toxicStaffNoData        = true;
		splasherInCombat        = false;
		splasherIdleTicks       = 0;
		serpDrainTicks          = 0;
		staffDrainTicks         = 0;
		serpCombatEnterDrained  = false;
		staffCombatEnterDrained = false;
		splasherLoaded          = false;
		bankDataLoaded          = false;
		supplyLoaded            = false;
		houseTabLoaded          = false;
		scalesLoaded            = false;
		pendingScalesCheck      = ScalesItem.NONE;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}
		String key = event.getKey();

		if (key.equals("bloodFuryThreshold") || key.equals("bloodFuryEnabled"))
		{
			if (!bloodFuryNoData && bloodFuryCharges >= 0)
			{
				bloodFuryWarn = config.bloodFuryEnabled() && bloodFuryCharges < config.bloodFuryThreshold();
			}
			else if (bloodFuryCharges == -1)
			{
				bloodFuryNoData = true;
				bloodFuryWarn   = false;
			}
		}

		if (key.equals("runePouchThreshold") || key.equals("runePouchEnabled"))
		{
			runePouchDirty = true;
		}

		if (key.equals("supplyThreshold") || key.equals("suppliesEnabled") || key.equals("supplyType"))
		{
			checkSupplies();
		}

		if (key.equals("quickPrayerEnabled"))
		{
			quickPrayerWarn = config.quickPrayerEnabled()
					&& client.getVarbitValue(Varbits.QUICK_PRAYER) == 0;
		}

		if (key.equals("lunarsEnabled"))
		{
			lunarsWarn = config.lunarsEnabled()
					&& client.getVarbitValue(SPELLBOOK_VARBIT) != LUNAR_SPELLBOOK;
		}

		if (key.equals("tomeOfWaterThreshold") || key.equals("tomeOfWaterEnabled"))
		{
			reevaluateTomeWarn();
		}
		if (key.equals("serpHelmThreshold") || key.equals("serpHelmEnabled"))
		{
			reevaluateSerpWarn();
		}
		if (key.equals("toxicStaffThreshold") || key.equals("toxicStaffEnabled"))
		{
			reevaluateStaffWarn();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGGED_IN:
				loadBloodFuryCharges();
				loadTomeCharges();
				loadSerpCharges();
				loadStaffCharges();
				loadSupplyCount();
				loadHouseTabCount();
				loadScalesCount();
				break;
			case LOGIN_SCREEN:
			case HOPPING:
				resetState();
				break;
			default:
				break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		WorldPoint loc = client.getLocalPlayer().getWorldLocation();
		inCorpCave = loc.getRegionID() == CORP_CAVE_REGION;

		if (!bloodFuryLoaded && config.bloodFuryEnabled())
		{
			loadBloodFuryCharges();
		}
		if (!splasherLoaded)
		{
			Player local = client.getLocalPlayer();
			if (local != null && local.getName() != null)
			{
				loadTomeCharges();
				loadSerpCharges();
				loadStaffCharges();
				splasherLoaded = true;
			}
		}
		if (!supplyLoaded)
		{
			Player local = client.getLocalPlayer();
			if (local != null && local.getName() != null)
			{
				loadSupplyCount();
				supplyLoaded = true;
			}
		}
		if (!houseTabLoaded)
		{
			Player local = client.getLocalPlayer();
			if (local != null && local.getName() != null)
			{
				loadHouseTabCount();
				houseTabLoaded = true;
			}
		}
		if (!scalesLoaded)
		{
			Player local = client.getLocalPlayer();
			if (local != null && local.getName() != null)
			{
				loadScalesCount();
				scalesLoaded = true;
			}
		}

		// Veng cooldown
		if (vengCooldownTicks > 0) vengCooldownTicks--;
		vengReady = !vengActive && vengCooldownTicks == 0;

		// Corp combat idle
		if (combatIdleTicks > 0) combatIdleTicks--;
		else inCombat = false;

		// Quick prayer warning — re-evaluate every tick inside corp cave so it
		// appears immediately on first teleport in (not only after a varbit change).
		if (config.quickPrayerEnabled())
		{
			quickPrayerWarn = client.getVarbitValue(Varbits.QUICK_PRAYER) == 0;
		}

		// Lunars warning — only inside corp cave
		lunarsWarn = inCorpCave && config.lunarsEnabled()
				&& client.getVarbitValue(SPELLBOOK_VARBIT) != LUNAR_SPELLBOOK;

		// Rune pouch
		if (runePouchDirty && config.runePouchEnabled())
		{
			checkRunePouch();
			runePouchDirty = false;
		}

		// ---- Splasher ----

		// Serp helm and toxic staff: read from varbits every tick while equipped.
		// This is the primary tracking method — completely automatic if varbit IDs
		// are correct. The Check message fallback handles wrong IDs gracefully.
		readSerpHelmVarbit();
		readToxicStaffVarbit();

		// Splasher combat idle countdown
		if (splasherIdleTicks > 0)
		{
			splasherIdleTicks--;
		}
		else if (splasherInCombat)
		{
			// Left combat — reset everything for next combat session
			splasherInCombat        = false;
			serpDrainTicks          = 0;
			staffDrainTicks         = 0;
			serpCombatEnterDrained  = false;
			staffCombatEnterDrained = false;
		}

		// Tick up interval drain counters while in combat
		if (splasherInCombat)
		{
			serpDrainTicks++;
			staffDrainTicks++;

			if (serpDrainTicks >= SERP_DRAIN_INTERVAL && serpHelmEquipped())
			{
				drainSerpHelm(10);
				serpDrainTicks = 0;
			}
			if (staffDrainTicks >= STAFF_DRAIN_INTERVAL && toxicStaffEquipped())
			{
				drainToxicStaff(10);
				staffDrainTicks = 0;
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int id = event.getVarbitId();

		if (id == Varbits.QUICK_PRAYER && config.quickPrayerEnabled())
		{
			// onGameTick handles this every tick; this fires immediately on change
			// for responsiveness between ticks.
			quickPrayerWarn = event.getValue() == 0;
		}

		if (id == SPELLBOOK_VARBIT && config.lunarsEnabled() && inCorpCave)
		{
			lunarsWarn = event.getValue() != LUNAR_SPELLBOOK;
		}

		if (id == Varbits.RUNE_POUCH_RUNE1   || id == Varbits.RUNE_POUCH_RUNE2
				|| id == Varbits.RUNE_POUCH_RUNE3   || id == Varbits.RUNE_POUCH_RUNE4
				|| id == Varbits.RUNE_POUCH_AMOUNT1  || id == Varbits.RUNE_POUCH_AMOUNT2
				|| id == Varbits.RUNE_POUCH_AMOUNT3  || id == Varbits.RUNE_POUCH_AMOUNT4)
		{
			runePouchDirty = true;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int containerId = event.getContainerId();

		if (containerId == InventoryID.BANK.getId())
		{
			// Only update when bank actually has items — fires with empty container on close
			ItemContainer bank = event.getItemContainer();
			if (bank != null && bank.getItems().length > 0)
			{
				bankDataLoaded = true;
				if (config.suppliesEnabled()) checkSupplies();
				if (config.houseTabEnabled()) checkHouseTabs();
				if (config.zulrahScalesEnabled()) checkZulrahScales();
			}
		}

		if (containerId == InventoryID.INVENTORY.getId() && bankDataLoaded)
		{
			if (config.suppliesEnabled()) checkSupplies();
			if (config.houseTabEnabled()) checkHouseTabs();
			if (config.zulrahScalesEnabled()) checkZulrahScales();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		// Detect right-click Check on serp helm or toxic staff so we know
		// which item the upcoming "Scales: X" chat message belongs to.
		// Use slot index from the event and verify against what's actually equipped.
		if (event.getMenuOption().equals("Check"))
		{
			int param1 = event.getParam1();
			if (param1 == WIDGET_EQUIP_SERP_HELM)
			{
				pendingScalesCheck = ScalesItem.SERP;
			}
			else if (param1 == WIDGET_EQUIP_TOXIC_STAFF)
			{
				pendingScalesCheck = ScalesItem.STAFF;
			}
		}

		if (!inCorpCave || !config.movementLockEnabled() || isHotkeyHeld)
		{
			return;
		}
		if (event.getMenuAction() == MenuAction.WALK)
		{
			event.consume();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() != client.getLocalPlayer())
		{
			return;
		}

		int anim = event.getActor().getAnimation();

		// Corp alt — Fang attack
		if (anim == FANG_ATTACK_ANIM && inCorpCave)
		{
			inCombat        = true;
			combatIdleTicks = COMBAT_IDLE_TIMEOUT;
			decrementBloodFury();
		}

		// Corp alt — Noxious halberd / Elder maul
		if ((anim == NOX_HALBERD_ANIM || anim == NOX_HALBERD_ANIM2 || anim == ELDER_MAUL_ANIM) && inCorpCave)
		{
			inCombat        = true;
			combatIdleTicks = COMBAT_IDLE_TIMEOUT;
			decrementBloodFury();
		}

		// Splasher — Water Strike
		if (anim == WATER_STRIKE_ANIM)
		{
			splasherIdleTicks = SPLASHER_IDLE_TIMEOUT;

			if (!splasherInCombat)
			{
				// Entering combat — immediate 10-scale drain on serp and staff
				splasherInCombat = true;
				serpDrainTicks   = 0;
				staffDrainTicks  = 0;

				if (!serpCombatEnterDrained && serpHelmEquipped())
				{
					drainSerpHelm(10);
					serpCombatEnterDrained = true;
				}
				if (!staffCombatEnterDrained && toxicStaffEquipped())
				{
					drainToxicStaff(10);
					staffCombatEnterDrained = true;
				}
			}

			// Tome of water: 1 charge per Water Strike cast
			if (config.tomeOfWaterEnabled() && tomeOfWaterEquipped()
					&& !tomeOfWaterNoData && tomeOfWaterCharges > 0)
			{
				tomeOfWaterCharges--;
				saveTomeCharges();
				reevaluateTomeWarn();
			}
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		if (event.getActor() != client.getLocalPlayer())
		{
			return;
		}
		if (event.getActor().getGraphic() == VENG_GRAPHIC)
		{
			vengActive        = true;
			vengCooldownTicks = VENG_COOLDOWN_TICKS;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String msg = event.getMessage().replaceAll("<[^>]+>", "");

		if (msg.contains("Taste vengeance!"))
		{
			vengActive = false;
		}

		ChatMessageType type = event.getType();
		if (type != ChatMessageType.SPAM
				&& type != ChatMessageType.GAMEMESSAGE
				&& type != ChatMessageType.PLAYERRELATED
				&& type != ChatMessageType.MESBOX)
		{
			return;
		}

		// Blood fury
		if (config.bloodFuryEnabled())
		{
			Matcher m = BLOOD_FURY_CHECK_PATTERN.matcher(msg);
			if (m.find())
			{
				try
				{
					bloodFuryCharges = Integer.parseInt(m.group(1).replace(",", ""));
					saveBloodFuryCharges();
					bloodFuryWarn   = bloodFuryCharges < config.bloodFuryThreshold();
					bloodFuryNoData = false;
				}
				catch (NumberFormatException e) {}
			}

			// Blood shard recharge: "It will now work for X more hits."
			Matcher rm = BLOOD_FURY_RECHARGE_PATTERN.matcher(msg);
			if (rm.find())
			{
				try
				{
					bloodFuryCharges = Integer.parseInt(rm.group(1).replace(",", ""));
					saveBloodFuryCharges();
					bloodFuryWarn   = bloodFuryCharges < config.bloodFuryThreshold();
					bloodFuryNoData = false;
				}
				catch (NumberFormatException e) {}
			}
		}

		// Tome of water — right-click Check syncs the running count
		if (config.tomeOfWaterEnabled())
		{
			Matcher m = TOME_OF_WATER_CHECK_PATTERN.matcher(msg);
			if (m.find())
			{
				try
				{
					tomeOfWaterCharges = Integer.parseInt(m.group(1).replace(",", ""));
					saveTomeCharges();
					tomeOfWaterNoData = false;
					reevaluateTomeWarn();
				}
				catch (NumberFormatException e)
				{
				}
			}
		}

		// Serp helm and toxic staff both show "Scales: X (Y%)" on right-click Check.
		// Use pendingScalesCheck to know which item was actually checked.
		Matcher scalesMatcher = SCALES_CHECK_PATTERN.matcher(msg);
		if (scalesMatcher.find())
		{
			if (pendingScalesCheck != ScalesItem.NONE)
			{
				try
				{
					int checked = Integer.parseInt(scalesMatcher.group(1).replace(",", ""));
					if (pendingScalesCheck == ScalesItem.SERP && config.serpHelmEnabled())
					{
						serpHelmCharges = checked;
						serpHelmNoData  = false;
						saveSerpCharges();
						reevaluateSerpWarn();
					}
					else if (pendingScalesCheck == ScalesItem.STAFF && config.toxicStaffEnabled())
					{
						toxicStaffCharges = checked;
						toxicStaffNoData  = false;
						saveStaffCharges();
						reevaluateStaffWarn();
					}
				}
				catch (NumberFormatException e)
				{
				}
				pendingScalesCheck = ScalesItem.NONE;
			}
		}
	}

	private void readSerpHelmVarbit()
	{
		if (!config.serpHelmEnabled())
		{
			return;
		}
		if (!serpHelmEquipped())
		{
			return;
		}
		int val = client.getVarbitValue(SERP_HELM_CHARGES_VARBIT);
		// Only trust the varbit if it returns a positive value. A zero reading
		// most likely means the varbit ID is wrong, not that the helm is at 0 charges.
		// The right-click Check message is the fallback source of truth.
		if (val > 0)
		{
			if (val != serpHelmCharges)
			{
				serpHelmCharges = val;
				serpHelmNoData  = false;
				saveSerpCharges();
				reevaluateSerpWarn();
			}
		}
	}

	private void readToxicStaffVarbit()
	{
		if (!config.toxicStaffEnabled())
		{
			return;
		}
		if (!toxicStaffEquipped())
		{
			return;
		}
		int val = client.getVarbitValue(TOXIC_STAFF_CHARGES_VARBIT);
		// Only trust the varbit if it returns a positive value. A zero reading
		// most likely means the varbit ID is wrong, not that the staff is at 0 charges.
		// The right-click Check message is the fallback source of truth.
		if (val > 0)
		{
			if (val != toxicStaffCharges)
			{
				toxicStaffCharges = val;
				toxicStaffNoData  = false;
				saveStaffCharges();
				reevaluateStaffWarn();
			}
		}
	}

	/**
	 * Apply a drain to serp helm charges.
	 * If varbits are working this is redundant (varbit read corrects it next tick),
	 * but it keeps the display accurate between ticks when varbits are wrong.
	 */
	private void drainSerpHelm(int amount)
	{
		if (serpHelmNoData || serpHelmCharges < 0)
		{
			return;
		}
		serpHelmCharges = Math.max(0, serpHelmCharges - amount);
		reevaluateSerpWarn();
	}

	private void drainToxicStaff(int amount)
	{
		if (toxicStaffNoData || toxicStaffCharges < 0)
		{
			return;
		}
		toxicStaffCharges = Math.max(0, toxicStaffCharges - amount);
		reevaluateStaffWarn();
	}

	private boolean serpHelmEquipped()
	{
		ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equip == null) return false;
		Item head = equip.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
		if (head == null) return false;
		int id = head.getId();
		return id == ITEM_SERP_HELM || id == ITEM_SERP_HELM_MAGMA || id == ITEM_SERP_HELM_TANZANITE;
	}

	private boolean toxicStaffEquipped()
	{
		ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equip == null) return false;
		Item weapon = equip.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if (weapon == null) return false;
		int id = weapon.getId();
		return id == ITEM_TOXIC_STAFF || id == ITEM_TOXIC_STAFF_DMM;
	}

	private boolean tomeOfWaterEquipped()
	{
		ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equip == null) return false;
		Item shield = equip.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
		return shield != null && shield.getId() == ITEM_TOME_OF_WATER;
	}

	private void reevaluateTomeWarn()
	{
		tomeOfWaterWarn = config.tomeOfWaterEnabled()
				&& !tomeOfWaterNoData
				&& tomeOfWaterCharges >= 0
				&& tomeOfWaterCharges < config.tomeOfWaterThreshold();
	}

	private void reevaluateSerpWarn()
	{
		serpHelmWarn = config.serpHelmEnabled()
				&& !serpHelmNoData
				&& serpHelmCharges >= 0
				&& serpHelmCharges < config.serpHelmThreshold();
	}

	private void reevaluateStaffWarn()
	{
		toxicStaffWarn = config.toxicStaffEnabled()
				&& !toxicStaffNoData
				&& toxicStaffCharges >= 0
				&& toxicStaffCharges < config.toxicStaffThreshold();
	}

	private void checkRunePouch()
	{
		int[] runeVarbits   = { Varbits.RUNE_POUCH_RUNE1,   Varbits.RUNE_POUCH_RUNE2,   Varbits.RUNE_POUCH_RUNE3,   Varbits.RUNE_POUCH_RUNE4   };
		int[] amountVarbits = { Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3, Varbits.RUNE_POUCH_AMOUNT4 };

		runePouchWarnings = new ArrayList<>();
		for (int i = 0; i < runeVarbits.length; i++)
		{
			int runeId = client.getVarbitValue(runeVarbits[i]);
			int amount = client.getVarbitValue(amountVarbits[i]);
			if (runeId > 0 && amount > 0 && amount < config.runePouchThreshold())
			{
				runePouchWarnings.add(getRuneName(runeId) + ": " + amount + " left");
			}
		}
	}

	private void checkSupplies()
	{
		if (!config.suppliesEnabled())
		{
			supplyWarn = false;
			return;
		}
		int targetId = config.supplyType().itemId;
		int notedId  = targetId + 1; // noted form is always itemId + 1 in OSRS

		// Update cached bank count only when we actually have bank data.
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null && bank.getItems().length > 0)
		{
			int bankCount = 0;
			for (Item item : bank.getItems())
			{
				if (item.getId() == targetId || item.getId() == notedId)
					bankCount += item.getQuantity();
			}
			cachedBankSupplyCount = bankCount;
		}

		// Count inventory: both unnoted and noted forms are valid in inventory.
		int inventoryCount = 0;
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null)
		{
			for (Item item : inventory.getItems())
			{
				if (item.getId() == targetId || item.getId() == notedId)
					inventoryCount += item.getQuantity();
			}
		}

		// Total = cached bank + current inventory.
		// If bank has not been loaded this session, preserve the persisted count and only
		// warn based on inventory alone — avoids overwriting the saved total with a partial value.
		int bankPart = Math.max(0, cachedBankSupplyCount);
		if (bankDataLoaded)
		{
			supplyCount = bankPart + inventoryCount;
			supplyWarn  = supplyCount < config.supplyThreshold();
			saveSupplyCount();
		}
		else
		{
			supplyWarn = inventoryCount < config.supplyThreshold();
		}
	}

	private void checkHouseTabs() {
		if (!config.houseTabEnabled()) {
			houseTabWarn = false;
			return;
		}
		int notedId = ITEM_HOUSE_TAB + 1;

		// Update cached bank count only when we actually have live bank data.
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null && bank.getItems().length > 0) {
			int bankCount = 0;
			for (Item item : bank.getItems()) {
				if (item.getId() == ITEM_HOUSE_TAB || item.getId() == notedId)
					bankCount += item.getQuantity();
			}
			cachedBankHouseTabCount = bankCount;
		}

		int inventoryCount = 0;
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null) {
			for (Item item : inventory.getItems()) {
				if (item.getId() == ITEM_HOUSE_TAB || item.getId() == notedId)
					inventoryCount += item.getQuantity();
			}
		}

		int bankPart = Math.max(0, cachedBankHouseTabCount);
		if (bankDataLoaded)
		{
			houseTabCount = bankPart + inventoryCount;
			houseTabWarn  = houseTabCount < config.houseTabThreshold();
			saveHouseTabCount();
		}
		else
		{
			houseTabWarn = inventoryCount < config.houseTabThreshold();
		}
	}
	private void checkZulrahScales()
	{
		if (!config.zulrahScalesEnabled())
		{
			zulrahScalesWarn = false;
			return;
		}

		// Zulrah's scales are stackable — they have no noted form, so only check ITEM_ZULRAH_SCALES.
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null && bank.getItems().length > 0)
		{
			int bankCount = 0;
			for (Item item : bank.getItems())
			{
				if (item.getId() == ITEM_ZULRAH_SCALES)
					bankCount += item.getQuantity();
			}
			cachedBankScalesCount = bankCount;
		}

		int inventoryCount = 0;
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null)
		{
			for (Item item : inventory.getItems())
			{
				if (item.getId() == ITEM_ZULRAH_SCALES)
					inventoryCount += item.getQuantity();
			}
		}

		int bankPart = Math.max(0, cachedBankScalesCount);
		if (bankDataLoaded)
		{
			zulrahScalesCount = bankPart + inventoryCount;
			zulrahScalesWarn  = zulrahScalesCount < config.zulrahScalesThreshold();
			saveScalesCount();
		}
		else
		{
			zulrahScalesWarn = inventoryCount < config.zulrahScalesThreshold();
		}
	}

	private void decrementBloodFury()
	{
		if (!config.bloodFuryEnabled() || bloodFuryNoData || bloodFuryCharges <= 0)
		{
			return;
		}
		bloodFuryCharges--;
		saveBloodFuryCharges();
		bloodFuryWarn = bloodFuryCharges < config.bloodFuryThreshold();
	}

	private String getBloodFuryConfigKey()
	{
		Player local    = client.getLocalPlayer();
		String username = (local != null && local.getName() != null) ? local.getName() : "unknown";
		return BLOOD_FURY_KEY_PREFIX + username;
	}

	private void saveBloodFuryCharges()
	{
		String key = getBloodFuryConfigKey();
		if (!key.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, key, String.valueOf(bloodFuryCharges));
		}
	}

	private void loadBloodFuryCharges()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null)
		{
			bloodFuryNoData = true;
			return;
		}
		String val = configManager.getConfiguration(CONFIG_GROUP, getBloodFuryConfigKey());
		if (val != null)
		{
			try
			{
				bloodFuryCharges = Integer.parseInt(val);
				bloodFuryWarn    = config.bloodFuryEnabled() && bloodFuryCharges >= 0
						&& bloodFuryCharges < config.bloodFuryThreshold();
				bloodFuryNoData  = false;
				bloodFuryLoaded  = true;
			}
			catch (NumberFormatException e)
			{
				bloodFuryCharges = -1;
				bloodFuryNoData  = true;
				bloodFuryLoaded  = true;
			}
		}
		else
		{
			bloodFuryCharges = -1;
			bloodFuryNoData  = true;
			bloodFuryLoaded  = true;
		}
	}

	private String getTomeConfigKey()
	{
		Player local    = client.getLocalPlayer();
		String username = (local != null && local.getName() != null) ? local.getName() : "unknown";
		return TOME_KEY_PREFIX + username;
	}

	private void saveTomeCharges()
	{
		String key = getTomeConfigKey();
		if (!key.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, key, String.valueOf(tomeOfWaterCharges));
		}
	}

	private void loadTomeCharges()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null) return;

		String val = configManager.getConfiguration(CONFIG_GROUP, getTomeConfigKey());
		if (val != null)
		{
			try
			{
				tomeOfWaterCharges = Integer.parseInt(val);
				tomeOfWaterNoData  = tomeOfWaterCharges < 0;
				reevaluateTomeWarn();
			}
			catch (NumberFormatException e)
			{
				tomeOfWaterCharges = -1;
				tomeOfWaterNoData  = true;
			}
		}
		else
		{
			tomeOfWaterCharges = -1;
			tomeOfWaterNoData  = true;
		}
	}

	private String getSerpConfigKey()
	{
		Player local    = client.getLocalPlayer();
		String username = (local != null && local.getName() != null) ? local.getName() : "unknown";
		return SERP_KEY_PREFIX + username;
	}

	private void saveSerpCharges()
	{
		String key = getSerpConfigKey();
		if (!key.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, key, String.valueOf(serpHelmCharges));
		}
	}

	private void loadSerpCharges()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null) return;

		String val = configManager.getConfiguration(CONFIG_GROUP, getSerpConfigKey());
		if (val != null)
		{
			try
			{
				serpHelmCharges = Integer.parseInt(val);
				serpHelmNoData  = serpHelmCharges < 0;
				reevaluateSerpWarn();
			}
			catch (NumberFormatException e)
			{
				serpHelmCharges = -1;
				serpHelmNoData  = true;
			}
		}
		else
		{
			serpHelmCharges = -1;
			serpHelmNoData  = true;
		}
	}

	private String getStaffConfigKey()
	{
		Player local    = client.getLocalPlayer();
		String username = (local != null && local.getName() != null) ? local.getName() : "unknown";
		return STAFF_KEY_PREFIX + username;
	}

	private void saveStaffCharges()
	{
		String key = getStaffConfigKey();
		if (!key.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, key, String.valueOf(toxicStaffCharges));
		}
	}

	private void loadStaffCharges()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null) return;

		String val = configManager.getConfiguration(CONFIG_GROUP, getStaffConfigKey());
		if (val != null)
		{
			try
			{
				toxicStaffCharges = Integer.parseInt(val);
				toxicStaffNoData  = toxicStaffCharges < 0;
				reevaluateStaffWarn();
			}
			catch (NumberFormatException e)
			{
				toxicStaffCharges = -1;
				toxicStaffNoData  = true;
			}
		}
		else
		{
			toxicStaffCharges = -1;
			toxicStaffNoData  = true;
		}
	}

	private String getSupplyConfigKey()
	{
		Player local    = client.getLocalPlayer();
		String username = (local != null && local.getName() != null) ? local.getName() : "unknown";
		return SUPPLY_KEY_PREFIX + username;
	}

	private void saveSupplyCount()
	{
		String key = getSupplyConfigKey();
		if (!key.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, key, String.valueOf(supplyCount));
		}
	}

	private void loadSupplyCount()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null) return;

		String val = configManager.getConfiguration(CONFIG_GROUP, getSupplyConfigKey());
		if (val != null)
		{
			try
			{
				supplyCount = Integer.parseInt(val);
				supplyWarn  = config.suppliesEnabled() && supplyCount >= 0
						&& supplyCount < config.supplyThreshold();
			}
			catch (NumberFormatException e)
			{
				supplyCount = -1;
			}
		}
		else
		{
			supplyCount = -1;
		}
	}

	private String getHouseTabConfigKey()
	{
		Player local    = client.getLocalPlayer();
		String username = (local != null && local.getName() != null) ? local.getName() : "unknown";
		return HOUSE_TAB_KEY_PREFIX + username;
	}

	private void saveHouseTabCount()
	{
		String key = getHouseTabConfigKey();
		if (!key.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, key, String.valueOf(houseTabCount));
		}
	}

	private void loadHouseTabCount()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null) return;

		String val = configManager.getConfiguration(CONFIG_GROUP, getHouseTabConfigKey());
		if (val != null)
		{
			try
			{
				houseTabCount = Integer.parseInt(val);
				houseTabWarn  = config.houseTabEnabled() && houseTabCount >= 0
						&& houseTabCount < config.houseTabThreshold();
			}
			catch (NumberFormatException e)
			{
				houseTabCount = -1;
			}
		}
		else
		{
			houseTabCount = -1;
		}
	}

	private String getScalesConfigKey()
	{
		Player local    = client.getLocalPlayer();
		String username = (local != null && local.getName() != null) ? local.getName() : "unknown";
		return SCALES_KEY_PREFIX + username;
	}

	private void saveScalesCount()
	{
		String key = getScalesConfigKey();
		if (!key.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, key, String.valueOf(zulrahScalesCount));
		}
	}

	private void loadScalesCount()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null) return;

		String val = configManager.getConfiguration(CONFIG_GROUP, getScalesConfigKey());
		if (val != null)
		{
			try
			{
				zulrahScalesCount = Integer.parseInt(val);
				zulrahScalesWarn  = config.zulrahScalesEnabled() && zulrahScalesCount >= 0
						&& zulrahScalesCount < config.zulrahScalesThreshold();
			}
			catch (NumberFormatException e)
			{
				zulrahScalesCount = -1;
			}
		}
		else
		{
			zulrahScalesCount = -1;
		}
	}

	private String getRuneName(int runeId)
	{
		switch (runeId)
		{
			case 1:  return "Air rune";
			case 2:  return "Water rune";
			case 3:  return "Earth rune";
			case 4:  return "Fire rune";
			case 5:  return "Mind rune";
			case 6:  return "Chaos rune";
			case 7:  return "Death rune";
			case 8:  return "Blood rune";
			case 9:  return "Cosmic rune";
			case 10: return "Nature rune";
			case 11: return "Law rune";
			case 12: return "Soul rune";
			case 13: return "Wrath rune";
			case 14: return "Astral rune";
			case 15: return "Mist rune";
			case 16: return "Mud rune";
			case 17: return "Dust rune";
			case 18: return "Lava rune";
			case 19: return "Steam rune";
			case 20: return "Smoke rune";
			case 21: return "Aether rune";
			default: return "Unknown rune (id=" + runeId + ")";
		}
	}

	@Provides
	CorpBoostingQOLConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CorpBoostingQOLConfig.class);
	}
}