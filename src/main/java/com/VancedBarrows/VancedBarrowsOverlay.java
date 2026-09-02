package com.VancedBarrows;

import javax.imageio.ImageReader;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;

import lombok.Setter;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class VancedBarrowsOverlay extends Overlay
{
    @Setter
    private ImageReader reader;
    private long startTime = 0;

    @Setter
    private boolean visible = false;
    private java.awt.Point overlayLocation = new java.awt.Point(150, 150);
    private float alpha = 1.0f;
    private int width = 128;
    private int height = 128;

    @Inject
    public VancedBarrowsOverlay()
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void setAlpha(float alpha)
    {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
    }

    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
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
        if (!visible || reader == null || overlayLocation == null)
        {
            startTime = System.nanoTime();
            return null;
        }

        try
        {
            int numFrames = reader.getNumImages(true);

            // spread it out over 3s
            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
            int idx = (int)((elapsedMs % 3000L) / 3000.0 * numFrames);

            BufferedImage image = reader.read(idx);
            Composite original = graphics.getComposite();
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            graphics.drawImage(image, overlayLocation.x, overlayLocation.y, width, height, null);
            graphics.setComposite(original);
        }
        catch (java.io.IOException e)
        {
            throw new RuntimeException(e);
        }

        return new Dimension(width, height);
    }
}