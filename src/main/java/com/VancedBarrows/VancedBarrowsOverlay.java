package com.VancedBarrows;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class VancedBarrowsOverlay extends Overlay
{
    private BufferedImage image;

    @Inject
    public VancedBarrowsOverlay()
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void setImage(BufferedImage image)
    {
        this.image = image;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (image == null)
        {
            return null;
        }

        // Render at static coordinates (e.g., center-ish of game window)
        graphics.drawImage(image, 260, 140, null);
        return new Dimension(image.getWidth(), image.getHeight());
    }
}
