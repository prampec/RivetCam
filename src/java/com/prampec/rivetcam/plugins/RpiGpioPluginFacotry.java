package com.prampec.rivetcam.plugins;

import java.util.Properties;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.prampec.rivetcam.AppController;
import com.prampec.rivetcam.RivetCamPlugin;
import com.prampec.rivetcam.RivetCamPluginFactory;

/**
 * TODO: write javadoc.
 */
public class RpiGpioPluginFacotry implements RivetCamPluginFactory
{
    @Override
    public RivetCamPlugin create(Properties properties, AppController appController)
    {
        return new RpiGpioPlugin(properties, appController);
    }
}