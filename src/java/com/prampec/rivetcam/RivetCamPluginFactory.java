package com.prampec.rivetcam;

import java.util.Properties;

/**
 * Creates a RivetCam plugin.
 */
public interface RivetCamPluginFactory
{
    RivetCamPlugin create(Properties properties, AppController appController);
}
