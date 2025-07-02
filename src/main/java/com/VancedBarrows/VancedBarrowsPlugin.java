package com.VancedBarrows;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
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
		description = "Replaces the Barrows ghost popup with your own image",
		tags = {"barrows", "ghost", "face", "overlay"}
)
public class VancedBarrowsPlugin extends Plugin
{
	private static final int BARROWS_REGION = 14231;

	@Inject
	private Client client;

	@Inject
	private VancedBarrowsOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	private BufferedImage ghostFace;
	private boolean inBarrows = false;

	@Override
	protected void startUp()
	{
		ghostFace = ImageUtil.loadImageResource(getClass(), "/vance.png");
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
		ghostFace = null;
		overlay.setVisible(false);
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
			return;
		}

		Widget ghostWidget = client.getWidget(24, 1);

		if (ghostWidget != null && ghostWidget.getSpriteId() != -1)
		{
			// Hide original popup
			ghostWidget.setHidden(true);

			// Set overlay position based on widget location
			Point widgetLocation = ghostWidget.getCanvasLocation();
			if (widgetLocation != null)
			{
				overlay.setOverlayLocation(widgetLocation);
				overlay.setVisible(true);
				log.debug("Showing overlay at {}", widgetLocation);
			}
			else
			{
				// Fallback to visible but no specific position
				overlay.setVisible(true);
				log.debug("Showing overlay but widget location null");
			}
		}
		else
		{
			overlay.setVisible(false);
		}
	}

	private void updateBarrowsState()
	{
		if (client.getMapRegions() == null)
		{
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
