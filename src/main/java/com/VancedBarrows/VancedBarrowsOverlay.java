package com.VancedBarrows;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;

import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class VancedBarrowsOverlay extends Overlay
{
    private BufferedImage image;
    private boolean visible = false;
    private java.awt.Point overlayLocation = new java.awt.Point(150, 150);
    private float alpha = 1.0f; // Opacity

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

    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    public void setAlpha(float alpha)
    {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
    }

    public void setOverlayLocation(Point widgetLocation)
    {
        if (widgetLocation != null)
        {
            this.overlayLocation = new java.awt.Point(widgetLocation.getX(), widgetLocation.getY());
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!visible || image == null || overlayLocation == null)
        {
            return null;
        }

        Composite original = graphics.getComposite();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        graphics.drawImage(image, overlayLocation.x, overlayLocation.y, null);
        graphics.setComposite(original);

        return new Dimension(image.getWidth(), image.getHeight());
    }
}
