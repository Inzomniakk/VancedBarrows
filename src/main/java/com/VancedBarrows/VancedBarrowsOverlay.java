package com.VancedBarrows;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class VancedBarrowsOverlay extends Overlay {
    private BufferedImage image;
    private boolean visible = false;
    private Point overlayLocation = new Point(9, 9);

    @Inject
    public VancedBarrowsOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setOverlayLocation(Point location) {
        this.overlayLocation = location;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!visible || image == null) {
            return null;
        }

        graphics.drawImage(image, (int) overlayLocation.getX(), (int) overlayLocation.getY(), null);
        return new Dimension(image.getWidth(), image.getHeight());
    }

    public void setOverlayLocation(net.runelite.api.Point widgetLocation) {
    }
}