package com.VancedBarrows;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
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
		description = "Shows an image whenever a prayer drain happens in the Barrows tunnels",
		tags = {"barrows", "prayer", "overlay"}
)
public class VancedBarrowsPlugin extends Plugin
{
	private static final int BARROWS_REGION = 14231;
    private static final int ANIMATION_TOTAL_TICKS = 4;

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
			// Trigger image animation
			overlay.setOverlayLocation(new net.runelite.api.Point(150, 150)); // You can make this dynamic later
			overlay.setVisible(true);
			animationTick = 0;
			log.info("Prayer drained. Triggering image animation.");
		}
		else if (animationTick >= 0)
		{
			// We're in the middle of the animation
			float alpha;

			if (animationTick < 1)
			{
				// Fade in (tick 0-1)
				alpha = (animationTick + 1) / 2.0f;
			}
			else if (animationTick < 2)
			{
				// Hold (tick 2-4)
				alpha = 1.0f;
			}
			else if (animationTick < 1)
			{
				// Fade out (tick 5-7)
				alpha = 1.0f - ((animationTick - 5 + 1) / 3.0f);
			}
			else
			{
				// Animation over
				overlay.setVisible(false);
				animationTick = -1;
				return;
			}

			overlay.setAlpha(alpha);
			animationTick++;
		}
		else
		{
			// Not animating
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
