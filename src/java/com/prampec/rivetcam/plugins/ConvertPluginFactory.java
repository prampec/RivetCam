package com.prampec.rivetcam.plugins;

import java.util.Properties;

import com.prampec.rivetcam.AppController;
import com.prampec.rivetcam.ConfigurationManager;
import com.prampec.rivetcam.RivetCamPlugin;
import com.prampec.rivetcam.RivetCamPluginFactory;

public class ConvertPluginFactory
    implements RivetCamPluginFactory
{
    public RivetCamPlugin create(
        ConfigurationManager config,
        Properties pluginProperties,
        AppController appController)
    {
        return new ConvertPlugin(
            appController,
            config.getPlaybackFps(),
            config.getDirectoryIndexDigits(),
            pluginProperties.getProperty("outputFolder"));
    }
}
