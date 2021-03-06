package com.prampec.rivetcam.plugins;

import java.util.Properties;

import com.prampec.rivetcam.AppController;
import com.prampec.rivetcam.ConfigurationManager;
import com.prampec.rivetcam.RivetCamPlugin;
import com.prampec.rivetcam.RivetCamPluginFactory;

public class RpiGpioPluginFactory implements RivetCamPluginFactory
{
    @Override
    public RivetCamPlugin create(
        ConfigurationManager config,
        Properties properties,
        AppController appController)
    {
        return new RpiGpioPlugin(properties, appController);
    }
}