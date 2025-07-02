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
    private boolean active = false;

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

    public void setActive(boolean active)
    {
        this.active = active;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!active || image == null)
        {
            return null;
        }

        graphics.drawImage(image, 260, 140, null);
        return new Dimension(image.getWidth(), image.getHeight());
    }
}
