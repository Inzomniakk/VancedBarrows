package com.VancedBarrows;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.RuneLite;
import java.io.File;

@ConfigGroup("vancedbarrows")
public interface VancedBarrowsConfig extends Config
{
    enum FaceMode
    {
        BARROWS_FACES,
        JD_VANCE_FACES,
        CUSTOM_FACES
    }

    @ConfigItem(
            keyName = "faceMode",
            name = "Face Mode",
            description = "Select which faces to show.",
            position = 1
    )
    default FaceMode faceMode()
    {
        return FaceMode.JD_VANCE_FACES;
    }

    @ConfigItem(
            keyName = "customFacesInfo",
            name = "Custom Faces Folder Path",
            description = "For 'Custom Faces' mode, place your images in this folder. You can copy this path.",
            position = 2
    )
    default String customFacesInfo()
    {
        return new File(RuneLite.RUNELITE_DIR, "barrowsfaces").getAbsolutePath();
    }
}
