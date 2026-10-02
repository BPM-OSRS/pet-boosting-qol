package com.petboostingqol;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

/**
 * Unused now; combat idle alerts are drawn by PetBoostingQOLAlertOverlay instead.
 * Kept so Guice injection still resolves.
 */
public class PetBoostingQOLCombatOverlay extends Overlay
{
	@Inject
	public PetBoostingQOLCombatOverlay()
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
