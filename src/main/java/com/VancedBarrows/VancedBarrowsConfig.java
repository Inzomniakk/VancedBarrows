package com.VancedBarrows;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("vancedbarrows")
public interface VancedBarrowsConfig extends Config
{
    @ConfigItem(
            keyName = "showBarrowsFaces",
            name = "Show Barrows Faces",
            description = "Whether to show the default brothers' face popup in Barrows tunnels",
            position = 1
    )
    default boolean showBarrowsFaces()
    {
        return true;
    }

    @ConfigItem(
            keyName = "showJD",
            name = "Show JD Vance",
            description = "Whether to show JD Vance.",
            position = 2
    )
    default boolean showJD()
    {
        return true;
    }
}
