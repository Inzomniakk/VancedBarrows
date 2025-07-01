package com.VancedBarrows;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
		name = "Vanced Barrows",
		description = "Replaces the Barrows ghost popup with your own image",
		tags = {"barrows", "ghost", "face", "overlay"}
)
public class VancedBarrowsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private VancedBarrowsOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	BufferedImage ghostFace;

	@Override
	protected void startUp() throws Exception
	{
		ghostFace = ImageUtil.loadImageResource(getClass(), "/vance.png");
		overlay.setImage(ghostFace);
		overlayManager.add(overlay);
		log.info("Vanced Barrows started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		ghostFace = null;
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
			log.debug("Player has logged in");
		}
	}
}
