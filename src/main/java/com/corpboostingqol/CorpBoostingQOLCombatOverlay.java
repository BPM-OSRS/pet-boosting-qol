package com.corpboostingqol;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

/**
 * Stub — combat idle overlay is now handled by CorpBoostingQOLAlertOverlay
 * as a slice alongside vengeance, prayer, and spellbook alerts.
 * This class is kept for injection compatibility.
 */
public class CorpBoostingQOLCombatOverlay extends Overlay
{
	@Inject
	public CorpBoostingQOLCombatOverlay()
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		return null;
	}
}
