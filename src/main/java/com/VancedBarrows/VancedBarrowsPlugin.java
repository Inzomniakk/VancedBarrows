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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@PluginDescriptor(
		name = "Vanced Barrows",
		description = "Replaces the Barrows Brothers' faces popup with images of JD Vance every 18 seconds!",
		tags = {"barrows", "overlay", "JD", "Vance"}
)
public class VancedBarrowsPlugin extends Plugin
{
	private static final int BARROWS_REGION = 14231;
	private static final String[] VANCE_IMAGE_PATHS = {"/vance.png", "/vanceTwo.png", "/vanceThree.png"};

	@Inject private Client client;
	@Inject private VancedBarrowsOverlay overlay;
	@Inject private OverlayManager overlayManager;
	@Inject private VancedBarrowsConfig config;

	private final List<BufferedImage> vanceFaces = new ArrayList<>();
	private int currentFaceIndex = 0;
	private boolean inBarrows = false;
	private int animationTick = -1;
	private int tickCounter = 0;

	@Provides
	VancedBarrowsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VancedBarrowsConfig.class);
	}

	@Override
	protected void startUp()
	{
		vanceFaces.clear();
		for (String path : VANCE_IMAGE_PATHS)
		{
			final BufferedImage image = ImageUtil.loadImageResource(getClass(), path);
			if (image != null)
			{
				vanceFaces.add(image);
				log.debug("Loaded {} successfully.", path);
			}
			else
			{
				log.error("Failed to load image: {}", path);
			}
		}

		if (vanceFaces.isEmpty())
		{
			log.error("No Vance images were loaded. The plugin will not display images.");
		}

		overlay.setVisible(false);
		overlayManager.add(overlay);
		log.debug("Vanced Barrows started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlay.setVisible(false);
		vanceFaces.clear();
		currentFaceIndex = 0;
		animationTick = -1;
		tickCounter = 0;
		log.debug("Vanced Barrows stopped");
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

		Widget faceWidget = client.getWidget(24, 1); // Barrows faces widget
		if (faceWidget != null)
		{
			faceWidget.setHidden(!config.showBarrowsFaces());
		}

		if (!inBarrows)
		{
			overlay.setVisible(false);
			animationTick = -1;
			tickCounter = 0;
			return;
		}

		tickCounter++;

		if (tickCounter >= 30 && config.showJD()) // Every 18 seconds (30 ticks)
		{
			if (vanceFaces.isEmpty())
			{
				return;
			}

			tickCounter = 0;

			overlay.setImage(vanceFaces.get(currentFaceIndex));
			currentFaceIndex = (currentFaceIndex + 1) % vanceFaces.size();

			Point randLoc = getRandomOnScreenLocation(128, 128);
			overlay.setOverlayLocation(randLoc);
			overlay.setSize(256, 256);
			overlay.setAlpha(0.0f);
			overlay.setVisible(true);
			animationTick = 0;
		}

		if (animationTick >= 0 && config.showJD())
		{
			float alpha;
			switch (animationTick)
			{
				case 0:
					alpha = 0.325f;
					break;
				case 1:
				case 2:
					alpha = 0.65f;
					break;
				case 3:
					alpha = 0.325f;
					break;
				case 4:
					alpha = 0.0f;
					break;
				default:
					overlay.setVisible(false);
					animationTick = -1;
					return;
			}
			overlay.setAlpha(alpha);
			animationTick++;
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
			inBarrows = false;
			return;
		}

		boolean nowInBarrows = Arrays.stream(client.getMapRegions())
				.anyMatch(region -> region == BARROWS_REGION);

		if (nowInBarrows != inBarrows)
		{
			log.debug("Barrows region state changed: inBarrows={}", nowInBarrows);
		}

		inBarrows = nowInBarrows;
	}

	private Point getRandomOnScreenLocation(int imageWidth, int imageHeight)
	{
		int canvasWidth = client.getCanvasWidth();
		int canvasHeight = client.getCanvasHeight();

		int maxX = canvasWidth - imageWidth;
		int maxY = canvasHeight - imageHeight;

		int x = ThreadLocalRandom.current().nextInt(0, Math.max(1, maxX));
		int y = ThreadLocalRandom.current().nextInt(0, Math.max(1, maxY));

		return new Point(x, y);
	}
}
