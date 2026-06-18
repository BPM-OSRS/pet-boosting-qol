package net.runelite.client.plugins.petboostingqol;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class PetBoostingQOLWarningOverlay extends Overlay
{
	private static final Color TEXT_COLOR = Color.WHITE;
	private static final int   BOX_WIDTH  = 380;
	private static final int   ROW_HEIGHT = 36;
	private static final int   PADDING_X  = 12;
	private static final int   ARC        = 4;

	private final PetBoostingQOLPlugin plugin;
	private final PetBoostingQOLConfig config;

	@Inject
	public PetBoostingQOLWarningOverlay(PetBoostingQOLPlugin plugin, PetBoostingQOLConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		List<String> rows = new ArrayList<>();

		// Rune pouch — show always
		if (config.runePouchEnabled() && plugin.runePouchWarnings != null)
			for (String warning : plugin.runePouchWarnings) rows.add(warning);

		// Corp cave only
		if (plugin.inCorpCave)
		{
			if (config.tomeOfWaterEnabled())
			{
				if (plugin.tomeOfWaterNoData) rows.add("Tome of Water: check tome");
				else if (plugin.tomeOfWaterWarn) rows.add("Tome of Water: " + plugin.tomeOfWaterCharges + " charges");
			}
			if (config.serpHelmEnabled())
			{
				if (plugin.serpHelmNoData) rows.add("Serp Helm: check helm");
				else if (plugin.serpHelmWarn) rows.add("Serp Helm: " + plugin.serpHelmCharges + " charges");
			}
			if (config.toxicStaffEnabled())
			{
				if (plugin.toxicStaffNoData) rows.add("Toxic Staff: check staff");
				else if (plugin.toxicStaffWarn) rows.add("Toxic Staff: " + plugin.toxicStaffCharges + " charges");
			}
		}

		// Outside all boss caves (bank/supply warnings)
		if (!plugin.inCorpCave && !plugin.inKqCave && !plugin.inMoleLair && !plugin.inKbdLair)
		{
			if (config.zulrahScalesEnabled() && plugin.zulrahScalesWarn)
				rows.add("Zulrah scales: " + plugin.zulrahScalesCount + " left");
			if (config.bloodFuryEnabled())
			{
				if (plugin.bloodFuryNoData || plugin.bloodFuryCharges == -1)
					rows.add("Blood Fury: inspect amulet");
				else if (plugin.bloodFuryWarn)
					rows.add("Blood Fury: " + plugin.bloodFuryCharges + " charges");
			}
			if (config.suppliesEnabled() && plugin.supplyWarn)
				rows.add(config.supplyType().displayName + ": " + plugin.supplyCount + " left");
			if (config.houseTabEnabled() && plugin.houseTabWarn)
				rows.add("House tabs: " + plugin.houseTabCount + " left");
		}

		if (rows.isEmpty()) return null;

		Font font = FontManager.getRunescapeBoldFont().deriveFont(Font.BOLD, 24f);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();

		int totalHeight = rows.size() * ROW_HEIGHT;

		g.setColor(config.warningOverlayColor());
		g.fillRoundRect(0, 0, BOX_WIDTH, totalHeight, ARC, ARC);
		g.setColor(config.warningOverlayColor().darker());
		g.drawRoundRect(0, 0, BOX_WIDTH - 1, totalHeight - 1, ARC, ARC);

		for (int i = 0; i < rows.size(); i++)
		{
			String text = rows.get(i);
			int rowY = i * ROW_HEIGHT;
			if (i > 0)
			{
				g.setColor(config.warningOverlayColor().darker());
				g.drawLine(0, rowY, BOX_WIDTH, rowY);
			}
			int maxWidth = BOX_WIDTH - PADDING_X * 2;
			while (fm.stringWidth(text) > maxWidth && text.length() > 4)
				text = text.substring(0, text.length() - 4) + "...";
			g.setColor(TEXT_COLOR);
			int ty = rowY + (ROW_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();
			g.drawString(text, PADDING_X, ty);
		}

		return new Dimension(BOX_WIDTH, totalHeight);
	}
}
