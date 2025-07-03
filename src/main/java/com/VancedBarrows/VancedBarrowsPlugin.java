package com.VancedBarrows;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.widgets.Widget;
import net.runelite.api.Point;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Arrays;

@Slf4j
@PluginDescriptor(
		name = "Vanced Barrows",
		description = "Displays a custom image when prayer is drained in the Barrows tunnels",
		tags = {"barrows", "prayer", "overlay"}
)
public class VancedBarrowsPlugin extends Plugin
{
	private static final int BARROWS_REGION = 14231;
	private static final int WIDGET_GROUP = 24;
	private static final int WIDGET_CHILD = 1;

	@Inject
	private Client client;

	@Inject
	private VancedBarrowsOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	private BufferedImage ghostFace;
	private boolean inBarrows = false;
	private int lastPrayerPoints = -1;
	private int animationTick = -1;

	@Override
	protected void startUp()
	{
		ghostFace = ImageUtil.loadImageResource(getClass(), "/vance.png"); //vance and vanceOld
		if (ghostFace == null)
		{
			log.error("Failed to load vance.png!");
		}
		else
		{
			log.info("Loaded vance.png successfully.");
		}

		overlay.setImage(ghostFace);
		overlay.setVisible(false);
		overlayManager.add(overlay);
		log.info("Vanced Barrows started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlay.setVisible(false);
		ghostFace = null;
		lastPrayerPoints = -1;
		animationTick = -1;
		log.info("Vanced Barrows stopped");
	}

	@Provides
	VancedBarrowsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VancedBarrowsConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			updateBarrowsState();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		updateBarrowsState();

		if (!inBarrows)
		{
			overlay.setVisible(false);
			lastPrayerPoints = -1;
			animationTick = -1;
			return;
		}

		int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);

		if (lastPrayerPoints != -1 && currentPrayer < lastPrayerPoints)
		{
			// Try to match face widget, not doing anything yet.
			Widget faceWidget = client.getWidget(WIDGET_GROUP, WIDGET_CHILD);
			if (faceWidget != null && faceWidget.getCanvasLocation() != null)
			{
				Point loc = faceWidget.getCanvasLocation();

				faceWidget.setHidden(true); // Set false to show brothers.

				// Position Vance
				overlay.setOverlayLocation(new Point(150, 150));
				overlay.setSize(512, 512);
				overlay.setVisible(true);
				animationTick = 0;
				log.warn("Showing Vance.");
			}
		}
		else if (animationTick >= 0)
		{
			// Handle animation phases
			float alpha;
			if (animationTick < 2)
			{
				// Fade in
				alpha = (animationTick + 1) / 2.0f;
			}
			else if (animationTick < 5)
			{
				// Hold
				alpha = 1.0f;
			}
			else if (animationTick < 8)
			{
				// Fade out
				alpha = 1.0f - ((animationTick - 5 + 1) / 3.0f);
			}
			else
			{
				// Done
				overlay.setVisible(false);
				animationTick = -1;
				return;
			}

			overlay.setAlpha(alpha * 0.50f);  // Cap alpha at 50% opacity
			animationTick++;
		}
		else
		{
			// No animation active
			overlay.setVisible(false);
		}

		lastPrayerPoints = currentPrayer;
	}

	private void updateBarrowsState()
	{
		if (client.getMapRegions() == null)
		{
			inBarrows = false;
			return;
		}

		boolean nowInBarrows = Arrays.stream(client.getMapRegions())
				.anyMatch(region -> region == BARROWS_REGION);

		if (nowInBarrows != inBarrows)
		{
			log.info("Barrows region state changed: inBarrows={}", nowInBarrows);
		}

		inBarrows = nowInBarrows;
	}
}
