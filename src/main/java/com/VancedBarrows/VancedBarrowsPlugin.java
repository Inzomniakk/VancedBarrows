package com.VancedBarrows;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
import net.runelite.api.Skill;
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
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@PluginDescriptor(
		name = "Vanced Barrows",
		description = "Replaces the Barrows Brothers' faces popup with images of JD Vance!",
		tags = {"barrows", "prayer", "overlay", "JD", "Vance"}
)
public class VancedBarrowsPlugin extends Plugin
{
	private static final int BARROWS_REGION = 14231;

	@Inject private Client client;
	@Inject private VancedBarrowsOverlay overlay;
	@Inject private OverlayManager overlayManager;
	@Inject private VancedBarrowsConfig config;

	private BufferedImage ghostFace;
	private boolean inBarrows = false;
	private int lastPrayerPoints = -1;
	private int animationTick = -1;

	@Provides
	VancedBarrowsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VancedBarrowsConfig.class);
	}

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
		overlay.setVisible(false);
		ghostFace = null;
		lastPrayerPoints = -1;
		animationTick = -1;
		log.info("Vanced Barrows stopped");
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
		Widget faceWidget = client.getWidget(24, 1); // Barrows faces

		if (faceWidget != null)
		{
			faceWidget.setHidden(!config.showBarrowsFaces());
		}

		if (lastPrayerPoints != -1 && currentPrayer < lastPrayerPoints)
		{
			if (config.showJD())
			{
				if (faceWidget != null && faceWidget.getCanvasLocation() != null)
				{
					Point loc = faceWidget.getCanvasLocation();
					int scaledWidth = (int)(faceWidget.getWidth() * 0.5);
					int scaledHeight = (int)(faceWidget.getHeight() * 0.5);
					int offsetX = loc.getX() + (faceWidget.getWidth() - scaledWidth) / 2;
					int offsetY = loc.getY() + (faceWidget.getHeight() - scaledHeight) / 2;

					overlay.setOverlayLocation(new Point(offsetX, offsetY));
					overlay.setSize(scaledWidth, scaledHeight);
				}
				else
				{
					Point randLoc = getRandomOnScreenLocation(128, 128);
					overlay.setOverlayLocation(randLoc);
					overlay.setSize(128, 128);
				}

				overlay.setAlpha(0.0f);
				overlay.setVisible(true);
				animationTick = 0;
			}
		}
		else if (animationTick >= 0 && config.showJD())
		{
			float alpha;
			if (animationTick < 2)
			{
				alpha = (animationTick + 1) / 2.0f;
			}
			else if (animationTick < 5)
			{
				alpha = 1.0f;
			}
			else if (animationTick < 8)
			{
				alpha = 1.0f - ((animationTick - 4) / 3.0f);
			}
			else
			{
				overlay.setVisible(false);
				animationTick = -1;
				return;
			}

			overlay.setAlpha(alpha * 0.75f);
			animationTick++;
		}
		else
		{
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
