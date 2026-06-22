package com.petboostingqol;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
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
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
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
		name = "Pet Boosting QOL",
		description = "Boosting QOL for Corp, Kalphite Queen, Giant Mole, and King Black Dragon: combat overlays, vengeance/prayer/spec indicators, heart/antifire/poison timers, and supply tracking.",
		tags = {"corp", "kq", "kalphite", "mole", "kbd", "boost", "vengeance", "combat", "overlay", "blood fury",
				"rune pouch", "corporeal beast", "splash", "tome", "serp", "toxic staff", "saturate", "antifire", "poison"}
)
@Slf4j
public class PetBoostingQOLPlugin extends Plugin
{
	// Region IDs
	static final int CORP_CAVE_REGION = 11844;
	static final int KQ_CAVE_REGION   = 13972;
	static final int MOLE_LAIR_REGION = 6993;
	static final int KBD_LAIR_REGION  = 9033;

	// Shared constants
	private static final int VENG_COOLDOWN_TICKS  = 50;
	private static final int VENG_GRAPHIC          = 726;
	private static final int SPELLBOOK_VARBIT      = 4070;
	private static final int LUNAR_SPELLBOOK       = 2;
	private static final int SPEC_ENERGY_VARPLAYER = 300;

	private static final int SATURATED_HEART_GRAPHIC = 2287;
	private static final int SATURATED_HEART_TICKS = 500;  // 5 minutes

	// Corp-specific constants
	private static final int COMBAT_IDLE_TIMEOUT   = 10;
	private static final int FANG_ATTACK_ANIM      = 9471;
	private static final int NOX_HALBERD_ANIM      = 440;
	private static final int NOX_HALBERD_ANIM2     = 428;
	private static final int ELDER_MAUL_ANIM       = 7516;
	private static final int WATER_STRIKE_ANIM     = 11423;
	private static final int SERP_DRAIN_INTERVAL   = 90;
	private static final int STAFF_DRAIN_INTERVAL  = 100;
	private static final int SPLASHER_IDLE_TIMEOUT = 6;

	private static final int SERP_HELM_CHARGES_VARBIT  = 6668;
	private static final int TOXIC_STAFF_CHARGES_VARBIT = 6669;

	private static final int ITEM_SERP_HELM          = 12931;
	private static final int ITEM_SERP_HELM_MAGMA    = 13197;
	private static final int ITEM_SERP_HELM_TANZANITE = 13199;
	private static final int ITEM_TOXIC_STAFF        = 12904;
	private static final int ITEM_TOXIC_STAFF_DMM    = 33036;
	private static final int ITEM_TOME_OF_WATER      = 25616;
	private static final int ITEM_HOUSE_TAB          = 8013;
	private static final int ITEM_ZULRAH_SCALES      = 12934;

	private static final int WIDGET_EQUIP_SERP_HELM  = 25362447;
	private static final int WIDGET_EQUIP_TOXIC_STAFF = 25362450;

	// Patterns
	private static final Pattern BLOOD_FURY_CHECK_PATTERN =
			Pattern.compile("Your Amulet of blood fury will work for ([\\d,]+) more hits\\.");
	private static final Pattern BLOOD_FURY_RECHARGE_PATTERN =
			Pattern.compile("It will now work for ([\\d,]+) more hits\\.");
	private static final Pattern TOME_OF_WATER_CHECK_PATTERN =
			Pattern.compile("Your tome currently holds ([\\d,]+) charges?");
	private static final Pattern SCALES_CHECK_PATTERN =
			Pattern.compile("Scales: ([\\d,]+) \\(");


	// Config keys
	private static final String CONFIG_GROUP          = "petboostingqol";
	private static final String BLOOD_FURY_KEY_PREFIX = "bloodfury_charges_";
	private static final String TOME_KEY_PREFIX       = "tome_charges_";
	private static final String SERP_KEY_PREFIX       = "serp_charges_";
	private static final String STAFF_KEY_PREFIX      = "staff_charges_";
	private static final String SUPPLY_KEY_PREFIX     = "supply_count_";
	private static final String HOUSE_TAB_KEY_PREFIX  = "housetab_count_";
	private static final String SCALES_KEY_PREFIX     = "scales_count_";

	// Injected dependencies
	@Inject private Client client;
	@Inject private OverlayManager overlayManager;
	@Inject private PetBoostingQOLCombatOverlay combatOverlay;
	@Inject private PetBoostingQOLWarningOverlay warningOverlay;
	@Inject private PetBoostingQOLAlertOverlay alertOverlay;
	@Inject private PetBoostingQOLConfig config;
	@Inject private ConfigManager configManager;
	@Inject private KeyManager keyManager;
	@Inject private net.runelite.client.callback.ClientThread clientThread;

	// Boss location flags
	boolean inCorpCave = false;
	boolean inKqCave   = false;
	boolean inMoleLair = false;
	boolean inKbdLair  = false;

	// Corp state
	boolean inCombat        = false;
	boolean vengReady       = false;
	boolean lunarsWarn      = false;
	boolean quickPrayerWarn = false;

	int     bloodFuryCharges = -1;
	boolean bloodFuryWarn    = false;
	boolean bloodFuryNoData  = true;

	List<String> runePouchWarnings = new ArrayList<>();

	boolean supplyWarn  = false;
	int     supplyCount = -1;
	private int cachedBankSupplyCount = -1;

	boolean houseTabWarn  = false;
	int     houseTabCount = -1;
	private int cachedBankHouseTabCount = -1;

	boolean zulrahScalesWarn  = false;
	int     zulrahScalesCount = -1;
	private int cachedBankScalesCount = -1;

	int     tomeOfWaterCharges = -1;
	boolean tomeOfWaterWarn    = false;
	boolean tomeOfWaterNoData  = true;
	int     serpHelmCharges    = -1;
	boolean serpHelmWarn       = false;
	boolean serpHelmNoData     = true;
	int     toxicStaffCharges  = -1;
	boolean toxicStaffWarn     = false;
	boolean toxicStaffNoData   = true;

	// KQ state
	boolean kqVengReady            = false;
	boolean kqSaturatedActive      = false;
	boolean kqSaturatedWarn        = false;
	int     kqSaturatedTicksLeft   = 0;
	boolean kqProtMageWarn         = false;
	boolean kqSpecWarn             = false;
	boolean kqPrayerRegenWarn      = false;
	boolean kqPoisoned             = false;
	boolean kqLowPrayerWarn        = false;

	// Mole state
	boolean moleSaturatedActive    = false;
	boolean moleSaturatedWarn      = false;
	int     moleSaturatedTicksLeft = 0;
	boolean moleSpecWarn           = false;

	// KBD state
	boolean kbdAntifireWarn      = false;
	boolean kbdPoisoned          = false;
	boolean kbdSpecWarn          = false;

	// Private fields
	private boolean vengActive              = false;
	private int     vengCooldownTicks       = 0;
	private boolean kqVengJustUsed          = false;
	private int     kqVengCooldown          = 0;
	private int     combatIdleTicks         = 0;
	private boolean runePouchDirty          = false;
	private boolean bloodFuryLoaded         = false;
	private boolean isHotkeyHeld            = false;
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

	private enum ScalesItem
	{
		NONE, SERP, STAFF
	}
	private ScalesItem pendingScalesCheck = ScalesItem.NONE;

	// Lifecycle
	@Override
	protected void startUp()
	{
		overlayManager.add(combatOverlay);
		overlayManager.add(warningOverlay);
		overlayManager.add(alertOverlay);

		hotkeyListener = new HotkeyListener(() -> config.movementHoldKey())
		{
			@Override
			public void hotkeyPressed()
			{
				isHotkeyHeld = true;
			}
			@Override
			public void hotkeyReleased()
			{
				isHotkeyHeld = false;
			}
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
		inCorpCave = false;
		inKqCave   = false;
		inMoleLair = false;
		inKbdLair  = false;

		inCombat          = false;
		vengActive        = false;
		vengReady         = false;
		lunarsWarn        = false;
		quickPrayerWarn   = false;
		vengCooldownTicks = 0;
		combatIdleTicks   = 0;
		bloodFuryCharges  = -1;
		bloodFuryWarn     = false;
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

		kqVengReady            = false;
		kqVengCooldown         = 0;
		kqVengJustUsed         = false;
		kqSaturatedActive      = false;
		kqSaturatedWarn        = false;
		kqSaturatedTicksLeft   = 0;
		kqProtMageWarn         = false;
		kqSpecWarn             = false;
		kqPrayerRegenWarn      = false;
		kqPoisoned             = false;
		kqLowPrayerWarn        = false;

		moleSaturatedActive    = false;
		moleSaturatedWarn      = false;
		moleSaturatedTicksLeft = 0;
		moleSpecWarn           = false;

		kbdAntifireWarn      = false;
		kbdPoisoned          = false;
		kbdSpecWarn          = false;
	}

	// Events

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP)) return;
		String key = event.getKey();

		if (key.equals("bloodFuryThreshold") || key.equals("bloodFuryEnabled"))
		{
			if (!bloodFuryNoData && bloodFuryCharges >= 0)
				bloodFuryWarn = config.bloodFuryEnabled() && bloodFuryCharges < config.bloodFuryThreshold();
			else if (bloodFuryCharges == -1)
		{
			bloodFuryNoData = true;
			bloodFuryWarn = false;
		}
		}
		if (key.equals("runePouchThreshold") || key.equals("runePouchEnabled"))
			runePouchDirty = true;
		if (key.equals("supplyThreshold") || key.equals("suppliesEnabled") || key.equals("supplyType"))
			clientThread.invokeLater(this::checkSupplies);
		if (key.equals("quickPrayerEnabled"))
			clientThread.invokeLater(() -> quickPrayerWarn = config.quickPrayerEnabled()
					&& client.getVarbitValue(Varbits.QUICK_PRAYER) == 0);
		if (key.equals("lunarsEnabled"))
			clientThread.invokeLater(() -> lunarsWarn = config.lunarsEnabled()
					&& client.getVarbitValue(SPELLBOOK_VARBIT) != LUNAR_SPELLBOOK);
		if (key.equals("tomeOfWaterThreshold") || key.equals("tomeOfWaterEnabled")) reevaluateTomeWarn();
		if (key.equals("serpHelmThreshold")    || key.equals("serpHelmEnabled"))    reevaluateSerpWarn();
		if (key.equals("toxicStaffThreshold")  || key.equals("toxicStaffEnabled"))  reevaluateStaffWarn();
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
		if (client.getGameState() != GameState.LOGGED_IN) return;

		WorldPoint loc = client.getLocalPlayer().getWorldLocation();
		int region = loc.getRegionID();
		inCorpCave = region == CORP_CAVE_REGION;
		inKqCave   = region == KQ_CAVE_REGION;
		inMoleLair = region == MOLE_LAIR_REGION;
		inKbdLair  = region == KBD_LAIR_REGION;

		// Lazy loaders
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

		if (vengCooldownTicks > 0) vengCooldownTicks--;
		vengReady = !vengActive && vengCooldownTicks == 0;

		if (combatIdleTicks > 0) combatIdleTicks--;
		else inCombat = false;

		if (config.quickPrayerEnabled())
			quickPrayerWarn = client.getVarbitValue(Varbits.QUICK_PRAYER) == 0;

		lunarsWarn = inCorpCave && config.lunarsEnabled()
				&& client.getVarbitValue(SPELLBOOK_VARBIT) != LUNAR_SPELLBOOK;

		if (runePouchDirty && config.runePouchEnabled())
		{
			checkRunePouch();
			runePouchDirty = false;
		}

		readSerpHelmVarbit();
		readToxicStaffVarbit();

		if (splasherIdleTicks > 0) splasherIdleTicks--;
		else if (splasherInCombat)
		{
			splasherInCombat        = false;
			serpDrainTicks          = 0;
			staffDrainTicks         = 0;
			serpCombatEnterDrained  = false;
			staffCombatEnterDrained = false;
		}
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

		if (kqVengCooldown > 0) kqVengCooldown--;
		kqVengReady = !kqVengJustUsed && kqVengCooldown == 0 && inKqCave;

		if (inKqCave)
		{
			if (kqSaturatedActive)
			{
				if (kqSaturatedTicksLeft > 0) kqSaturatedTicksLeft--;
				else
				{
					kqSaturatedActive = false;
					kqSaturatedWarn = config.kqSaturatedHeartEnabled();
				}
			}
			if (config.kqPrayerRegenEnabled())
			{
				boolean buffActive = client.getVarbitValue(VarbitID.PRAYER_REGENERATION_POTION_TIMER) > 0;
				kqPrayerRegenWarn = !buffActive;
			}
			kqProtMageWarn  = config.kqProtMageEnabled()  && !client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC);
			if (config.kqSpecEnabled())
			{
				int spec = client.getVarpValue(SPEC_ENERGY_VARPLAYER);
				if (spec >= 1000) kqSpecWarn = true;
				else if (spec < 1000) kqSpecWarn = false;
			}
			kqPoisoned      = config.kqPoisonEnabled()    && client.getVarpValue(VarPlayerID.POISON) > 0;
			kqLowPrayerWarn = config.kqLowPrayerEnabled() && client.getBoostedSkillLevel(Skill.PRAYER) < config.kqPrayerThreshold();
		}
		else
		{
			kqProtMageWarn  = false;
			kqSpecWarn      = false;
			kqPoisoned      = false;
			kqLowPrayerWarn = false;
			kqVengReady     = false;
		}

		if (inMoleLair)
		{
			if (moleSaturatedActive)
			{
				if (moleSaturatedTicksLeft > 0) moleSaturatedTicksLeft--;
				else
				{
					moleSaturatedActive = false;
					moleSaturatedWarn = config.moleSaturatedHeartEnabled();
				}
			}
			if (config.moleSpecEnabled())
			{
				int spec = client.getVarpValue(SPEC_ENERGY_VARPLAYER);
				if (spec >= 1000) moleSpecWarn = true;
				else if (spec < 1000) moleSpecWarn = false;
			}
		}
		else
		{
			moleSpecWarn = false;
		}

		if (inKbdLair)
		{
			if (config.kbdAntifireEnabled())
			{
				boolean buffActive = client.getVarbitValue(VarbitID.ANTIFIRE_POTION) > 0;
				kbdAntifireWarn = !buffActive;
			}
			kbdPoisoned = config.kbdPoisonEnabled() && client.getVarpValue(VarPlayerID.POISON) > 0;
			if (config.kbdSpecEnabled())
			{
				int spec = client.getVarpValue(SPEC_ENERGY_VARPLAYER);
				if (spec >= 1000) kbdSpecWarn = true;
				else if (spec < 1000) kbdSpecWarn = false;
			}
		}
		else
		{
			kbdPoisoned      = false;
			kbdSpecWarn      = false;
			kbdAntifireWarn  = false;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int id = event.getVarbitId();
		if (id == Varbits.QUICK_PRAYER && config.quickPrayerEnabled())
			quickPrayerWarn = event.getValue() == 0;
		if (id == SPELLBOOK_VARBIT && config.lunarsEnabled() && inCorpCave)
			lunarsWarn = event.getValue() != LUNAR_SPELLBOOK;
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
			ItemContainer bank = event.getItemContainer();
			if (bank != null && bank.getItems().length > 0)
			{
				bankDataLoaded = true;
				if (config.suppliesEnabled())     checkSupplies();
				if (config.houseTabEnabled())     checkHouseTabs();
				if (config.zulrahScalesEnabled()) checkZulrahScales();
			}
		}
		if (containerId == InventoryID.INVENTORY.getId() && bankDataLoaded)
		{
			if (config.suppliesEnabled())     checkSupplies();
			if (config.houseTabEnabled())     checkHouseTabs();
			if (config.zulrahScalesEnabled()) checkZulrahScales();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuOption().equals("Check"))
		{
			int param1 = event.getParam1();
			if (param1 == WIDGET_EQUIP_SERP_HELM)   pendingScalesCheck = ScalesItem.SERP;
			else if (param1 == WIDGET_EQUIP_TOXIC_STAFF) pendingScalesCheck = ScalesItem.STAFF;
		}
		if (inCorpCave && config.movementLockEnabled() && !isHotkeyHeld
			&& event.getMenuAction() == MenuAction.WALK)
		{
			event.consume();
			return;
		}
		if (inKqCave && config.kqMovementLockEnabled() && !isHotkeyHeld
			&& event.getMenuAction() == MenuAction.WALK)
		{
			event.consume();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() != client.getLocalPlayer()) return;
		int anim = event.getActor().getAnimation();

		if ((anim == FANG_ATTACK_ANIM || anim == NOX_HALBERD_ANIM
				|| anim == NOX_HALBERD_ANIM2 || anim == ELDER_MAUL_ANIM) && inCorpCave)
		{
			inCombat        = true;
			combatIdleTicks = COMBAT_IDLE_TIMEOUT;
			decrementBloodFury();
		}

		if (anim == WATER_STRIKE_ANIM)
		{
			splasherIdleTicks = SPLASHER_IDLE_TIMEOUT;
			if (!splasherInCombat)
			{
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
		if (event.getActor() != client.getLocalPlayer()) return;
		int graphic = event.getActor().getGraphic();

		if (graphic == VENG_GRAPHIC)
		{
			if (inCorpCave)
			{
				vengActive = true;
				vengCooldownTicks = VENG_COOLDOWN_TICKS;
			}
			if (inKqCave)
			{
				kqVengJustUsed = true;
				kqVengCooldown = VENG_COOLDOWN_TICKS;
			}
		}

		if (graphic == SATURATED_HEART_GRAPHIC)
		{
			if (inKqCave)
			{
				kqSaturatedActive    = true;
				kqSaturatedWarn      = false;
				kqSaturatedTicksLeft = SATURATED_HEART_TICKS;
			}
			if (inMoleLair)
			{
				moleSaturatedActive    = true;
				moleSaturatedWarn      = false;
				moleSaturatedTicksLeft = SATURATED_HEART_TICKS;
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		String msg = event.getMessage().replaceAll("<[^>]+>", "");

		if (msg.contains("Taste vengeance!"))
		{
			vengActive = false;
			kqVengJustUsed = false;
		}

		ChatMessageType type = event.getType();
		if (type != ChatMessageType.SPAM && type != ChatMessageType.GAMEMESSAGE
				&& type != ChatMessageType.PLAYERRELATED && type != ChatMessageType.MESBOX)
			return;

		if (config.bloodFuryEnabled())
		{
			Matcher m = BLOOD_FURY_CHECK_PATTERN.matcher(msg);
			if (m.find())
			{
				try
				{
					bloodFuryCharges = Integer.parseInt(m.group(1).replace(",", ""));
					saveBloodFuryCharges();
					bloodFuryWarn = bloodFuryCharges < config.bloodFuryThreshold();
					bloodFuryNoData = false;
				}
				catch (NumberFormatException ignored)
				{
				}
			}
			Matcher rm = BLOOD_FURY_RECHARGE_PATTERN.matcher(msg);
			if (rm.find())
			{
				try
				{
					bloodFuryCharges = Integer.parseInt(rm.group(1).replace(",", ""));
					saveBloodFuryCharges();
					bloodFuryWarn = bloodFuryCharges < config.bloodFuryThreshold();
					bloodFuryNoData = false;
				}
				catch (NumberFormatException ignored)
				{
				}
			}
		}

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
				catch (NumberFormatException ignored)
				{
				}
			}
		}

		Matcher scalesMatcher = SCALES_CHECK_PATTERN.matcher(msg);
		if (scalesMatcher.find() && pendingScalesCheck != ScalesItem.NONE)
		{
			try
			{
				int checked = Integer.parseInt(scalesMatcher.group(1).replace(",", ""));
				if (pendingScalesCheck == ScalesItem.SERP && config.serpHelmEnabled())
				{
					serpHelmCharges = checked;
					serpHelmNoData = false;
					saveSerpCharges();
					reevaluateSerpWarn();
				}
				else if (pendingScalesCheck == ScalesItem.STAFF && config.toxicStaffEnabled())
				{
					toxicStaffCharges = checked;
					toxicStaffNoData = false;
					saveStaffCharges();
					reevaluateStaffWarn();
				}
			}
			catch (NumberFormatException ignored)
			{
			}
			pendingScalesCheck = ScalesItem.NONE;
		}

	}

	// Splasher helpers
	private void readSerpHelmVarbit()
	{
		if (!config.serpHelmEnabled() || !serpHelmEquipped()) return;
		int val = client.getVarbitValue(SERP_HELM_CHARGES_VARBIT);
		if (val > 0 && val != serpHelmCharges)
		{
			serpHelmCharges = val;
			serpHelmNoData = false;
			saveSerpCharges();
			reevaluateSerpWarn();
		}
	}

	private void readToxicStaffVarbit()
	{
		if (!config.toxicStaffEnabled() || !toxicStaffEquipped()) return;
		int val = client.getVarbitValue(TOXIC_STAFF_CHARGES_VARBIT);
		if (val > 0 && val != toxicStaffCharges)
		{
			toxicStaffCharges = val;
			toxicStaffNoData = false;
			saveStaffCharges();
			reevaluateStaffWarn();
		}
	}

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

	private static final int ITEM_BLOOD_FURY = 22947;

	private boolean bloodFuryEquipped()
	{
		ItemContainer equip = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equip == null) return false;
		Item neck = equip.getItem(EquipmentInventorySlot.AMULET.getSlotIdx());
		return neck != null && neck.getId() == ITEM_BLOOD_FURY;
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
		tomeOfWaterWarn = config.tomeOfWaterEnabled() && !tomeOfWaterNoData
			&& tomeOfWaterCharges >= 0 && tomeOfWaterCharges < config.tomeOfWaterThreshold();
	}

	private void reevaluateSerpWarn()
	{
		serpHelmWarn = config.serpHelmEnabled() && !serpHelmNoData
			&& serpHelmCharges >= 0 && serpHelmCharges < config.serpHelmThreshold();
	}

	private void reevaluateStaffWarn()
	{
		toxicStaffWarn = config.toxicStaffEnabled() && !toxicStaffNoData
			&& toxicStaffCharges >= 0 && toxicStaffCharges < config.toxicStaffThreshold();
	}

	// Supply checks
	private void checkRunePouch()
	{
		int[] runeVarbits   = { Varbits.RUNE_POUCH_RUNE1, Varbits.RUNE_POUCH_RUNE2, Varbits.RUNE_POUCH_RUNE3, Varbits.RUNE_POUCH_RUNE4 };
		int[] amountVarbits = { Varbits.RUNE_POUCH_AMOUNT1, Varbits.RUNE_POUCH_AMOUNT2, Varbits.RUNE_POUCH_AMOUNT3, Varbits.RUNE_POUCH_AMOUNT4 };
		runePouchWarnings = new ArrayList<>();
		for (int i = 0; i < runeVarbits.length; i++)
		{
			int runeId = client.getVarbitValue(runeVarbits[i]);
			int amount = client.getVarbitValue(amountVarbits[i]);
			if (runeId > 0 && amount > 0 && amount < config.runePouchThreshold())
				runePouchWarnings.add(getRuneName(runeId) + ": " + amount + " left");
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
		int notedId  = targetId + 1;
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null && bank.getItems().length > 0)
		{
			int bankCount = 0;
			for (Item item : bank.getItems())
			{
				if (item.getId() == targetId || item.getId() == notedId)
				{
					bankCount += item.getQuantity();
				}
			}
			cachedBankSupplyCount = bankCount;
		}
		int inventoryCount = 0;
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null) for (Item item : inventory.getItems()) if (item.getId() == targetId || item.getId() == notedId) inventoryCount += item.getQuantity();
		int bankPart = Math.max(0, cachedBankSupplyCount);
		if (bankDataLoaded)
		{
			supplyCount = bankPart + inventoryCount;
			supplyWarn = supplyCount < config.supplyThreshold();
			saveSupplyCount();
		}
		else supplyWarn = inventoryCount < config.supplyThreshold();
	}

	private void checkHouseTabs()
	{
		if (!config.houseTabEnabled())
		{
			houseTabWarn = false;
			return;
		}
		int notedId = ITEM_HOUSE_TAB + 1;
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null && bank.getItems().length > 0)
		{
			int bankCount = 0;
			for (Item item : bank.getItems())
			{
				if (item.getId() == ITEM_HOUSE_TAB || item.getId() == notedId)
				{
					bankCount += item.getQuantity();
				}
			}
			cachedBankHouseTabCount = bankCount;
		}
		int inventoryCount = 0;
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null) for (Item item : inventory.getItems()) if (item.getId() == ITEM_HOUSE_TAB || item.getId() == notedId) inventoryCount += item.getQuantity();
		int bankPart = Math.max(0, cachedBankHouseTabCount);
		if (bankDataLoaded)
		{
			houseTabCount = bankPart + inventoryCount;
			houseTabWarn = houseTabCount < config.houseTabThreshold();
			saveHouseTabCount();
		}
		else houseTabWarn = inventoryCount < config.houseTabThreshold();
	}

	private void checkZulrahScales()
	{
		if (!config.zulrahScalesEnabled())
		{
			zulrahScalesWarn = false;
			return;
		}
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank != null && bank.getItems().length > 0)
		{
			int bankCount = 0;
			for (Item item : bank.getItems())
			{
				if (item.getId() == ITEM_ZULRAH_SCALES)
				{
					bankCount += item.getQuantity();
				}
			}
			cachedBankScalesCount = bankCount;
		}
		int inventoryCount = 0;
		ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null) for (Item item : inventory.getItems()) if (item.getId() == ITEM_ZULRAH_SCALES) inventoryCount += item.getQuantity();
		int bankPart = Math.max(0, cachedBankScalesCount);
		if (bankDataLoaded)
		{
			zulrahScalesCount = bankPart + inventoryCount;
			zulrahScalesWarn = zulrahScalesCount < config.zulrahScalesThreshold();
			saveScalesCount();
		}
		else zulrahScalesWarn = inventoryCount < config.zulrahScalesThreshold();
	}

	private void decrementBloodFury()
	{
		if (!config.bloodFuryEnabled() || bloodFuryNoData || bloodFuryCharges <= 0) return;
		if (!bloodFuryEquipped()) return;
		bloodFuryCharges--;
		saveBloodFuryCharges();
		bloodFuryWarn = bloodFuryCharges < config.bloodFuryThreshold();
	}

	// Persistence helpers
	private String playerKey(String prefix)
	{
		Player local = client.getLocalPlayer();
		String name = (local != null && local.getName() != null) ? local.getName() : "unknown";
		return prefix + name;
	}

	private void saveBloodFuryCharges()
	{
		String k = playerKey(BLOOD_FURY_KEY_PREFIX);
		if (!k.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, k, String.valueOf(bloodFuryCharges));
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
		String val = configManager.getConfiguration(CONFIG_GROUP, playerKey(BLOOD_FURY_KEY_PREFIX));
		if (val != null)
		{
			try
			{
				bloodFuryCharges = Integer.parseInt(val);
				bloodFuryWarn = config.bloodFuryEnabled() && bloodFuryCharges >= 0
					&& bloodFuryCharges < config.bloodFuryThreshold();
				bloodFuryNoData = false;
				bloodFuryLoaded = true;
			}
			catch (NumberFormatException e)
			{
				bloodFuryCharges = -1;
				bloodFuryNoData = true;
				bloodFuryLoaded = true;
			}
		}
		else
		{
			bloodFuryCharges = -1;
			bloodFuryNoData = true;
			bloodFuryLoaded = true;
		}
	}

	private void saveTomeCharges()
	{
		String k = playerKey(TOME_KEY_PREFIX);
		if (!k.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, k, String.valueOf(tomeOfWaterCharges));
		}
	}
	private void loadTomeCharges()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null)
		{
			return;
		}
		String val = configManager.getConfiguration(CONFIG_GROUP, playerKey(TOME_KEY_PREFIX));
		if (val != null)
		{
			try
			{
				tomeOfWaterCharges = Integer.parseInt(val);
				tomeOfWaterNoData = tomeOfWaterCharges < 0;
				reevaluateTomeWarn();
			}
			catch (NumberFormatException e)
			{
				tomeOfWaterCharges = -1;
				tomeOfWaterNoData = true;
			}
		}
		else
		{
			tomeOfWaterCharges = -1;
			tomeOfWaterNoData = true;
		}
	}

	private void saveSerpCharges()
	{
		String k = playerKey(SERP_KEY_PREFIX);
		if (!k.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, k, String.valueOf(serpHelmCharges));
		}
	}
	private void loadSerpCharges()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null)
		{
			return;
		}
		String val = configManager.getConfiguration(CONFIG_GROUP, playerKey(SERP_KEY_PREFIX));
		if (val != null)
		{
			try
			{
				serpHelmCharges = Integer.parseInt(val);
				serpHelmNoData = serpHelmCharges < 0;
				reevaluateSerpWarn();
			}
			catch (NumberFormatException e)
			{
				serpHelmCharges = -1;
				serpHelmNoData = true;
			}
		}
		else
		{
			serpHelmCharges = -1;
			serpHelmNoData = true;
		}
	}

	private void saveStaffCharges()
	{
		String k = playerKey(STAFF_KEY_PREFIX);
		if (!k.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, k, String.valueOf(toxicStaffCharges));
		}
	}
	private void loadStaffCharges()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null)
		{
			return;
		}
		String val = configManager.getConfiguration(CONFIG_GROUP, playerKey(STAFF_KEY_PREFIX));
		if (val != null)
		{
			try
			{
				toxicStaffCharges = Integer.parseInt(val);
				toxicStaffNoData = toxicStaffCharges < 0;
				reevaluateStaffWarn();
			}
			catch (NumberFormatException e)
			{
				toxicStaffCharges = -1;
				toxicStaffNoData = true;
			}
		}
		else
		{
			toxicStaffCharges = -1;
			toxicStaffNoData = true;
		}
	}

	private void saveSupplyCount()
	{
		String k = playerKey(SUPPLY_KEY_PREFIX);
		if (!k.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, k, String.valueOf(supplyCount));
		}
	}
	private void loadSupplyCount()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null)
		{
			return;
		}
		String val = configManager.getConfiguration(CONFIG_GROUP, playerKey(SUPPLY_KEY_PREFIX));
		if (val != null)
		{
			try
			{
				supplyCount = Integer.parseInt(val);
				supplyWarn = config.suppliesEnabled() && supplyCount >= 0
					&& supplyCount < config.supplyThreshold();
			}
			catch (NumberFormatException e)
			{
				supplyCount = -1;
			}
		}
		else supplyCount = -1;
	}

	private void saveHouseTabCount()
	{
		String k = playerKey(HOUSE_TAB_KEY_PREFIX);
		if (!k.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, k, String.valueOf(houseTabCount));
		}
	}
	private void loadHouseTabCount()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null)
		{
			return;
		}
		String val = configManager.getConfiguration(CONFIG_GROUP, playerKey(HOUSE_TAB_KEY_PREFIX));
		if (val != null)
		{
			try
			{
				houseTabCount = Integer.parseInt(val);
				houseTabWarn = config.houseTabEnabled() && houseTabCount >= 0
					&& houseTabCount < config.houseTabThreshold();
			}
			catch (NumberFormatException e)
			{
				houseTabCount = -1;
			}
		}
		else houseTabCount = -1;
	}

	private void saveScalesCount()
	{
		String k = playerKey(SCALES_KEY_PREFIX);
		if (!k.endsWith("unknown"))
		{
			configManager.setConfiguration(CONFIG_GROUP, k, String.valueOf(zulrahScalesCount));
		}
	}
	private void loadScalesCount()
	{
		Player local = client.getLocalPlayer();
		if (local == null || local.getName() == null)
		{
			return;
		}
		String val = configManager.getConfiguration(CONFIG_GROUP, playerKey(SCALES_KEY_PREFIX));
		if (val != null)
		{
			try
			{
				zulrahScalesCount = Integer.parseInt(val);
				zulrahScalesWarn = config.zulrahScalesEnabled() && zulrahScalesCount >= 0
					&& zulrahScalesCount < config.zulrahScalesThreshold();
			}
			catch (NumberFormatException e)
			{
				zulrahScalesCount = -1;
			}
		}
		else zulrahScalesCount = -1;
	}

	private String getRuneName(int runeId)
	{
		switch (runeId)
		{
			case 1:  return "Air rune";    case 2:  return "Water rune";  case 3:  return "Earth rune";
			case 4:  return "Fire rune";   case 5:  return "Mind rune";   case 6:  return "Chaos rune";
			case 7:  return "Death rune";  case 8:  return "Blood rune";  case 9:  return "Cosmic rune";
			case 10: return "Nature rune"; case 11: return "Law rune";    case 12: return "Soul rune";
			case 13: return "Wrath rune";  case 14: return "Astral rune"; case 15: return "Mist rune";
			case 16: return "Mud rune";    case 17: return "Dust rune";   case 18: return "Lava rune";
			case 19: return "Steam rune";  case 20: return "Smoke rune";  case 21: return "Aether rune";
			default: return "Unknown rune (id=" + runeId + ")";
		}
	}

	@Provides
	PetBoostingQOLConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PetBoostingQOLConfig.class);
	}
}
