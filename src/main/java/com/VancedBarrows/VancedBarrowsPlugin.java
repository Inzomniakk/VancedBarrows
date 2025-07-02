package com.VancedBarrows;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
	protected void startUp() throws Exception
	{
		ghostFace = ImageUtil.loadImageResource(getClass(), "/vance.png");
		overlay.setImage(ghostFace);
		overlay.setActive(false); // Start inactive
		overlayManager.add(overlay);
		log.info("Vanced Barrows started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		ghostFace = null;
		inBarrows = false;
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
	}

	private void updateBarrowsState()
	{
		if (client.getMapRegions() == null)
		{
			return;
		}

		boolean nowInBarrows = Arrays.stream(client.getMapRegions()).anyMatch(id -> id == BARROWS_REGION);
		if (nowInBarrows != inBarrows)
		{
			inBarrows = nowInBarrows;
			overlay.setActive(inBarrows);
			log.debug("Barrows region state changed: {}", inBarrows);
		}
	}
}
