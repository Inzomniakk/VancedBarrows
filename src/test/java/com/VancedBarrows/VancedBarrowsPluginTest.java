package com.VancedBarrows;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VancedBarrowsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VancedBarrowsPlugin.class);
		RuneLite.main(args);
	}
}