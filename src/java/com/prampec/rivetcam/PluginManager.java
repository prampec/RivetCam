package com.prampec.rivetcam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages plugins.
 */
public class PluginManager
{
    private static final Logger logger =
        LogManager.getLogger(PluginManager.class);

    protected static PluginManager instance;
    private List<RivetCamPlugin> plugins = new ArrayList<>();

    public static PluginManager getInstance()
    {
        if (instance == null)
        {
            instance = new PluginManager();
        }
        return instance;
    }

    public void createPlugins(
        ConfigurationManager config, AppController appController)
    {
        for (String pluginName : config.plugins.keySet())
        {
            Properties pluginProperties = config.plugins.get(pluginName);
            String factoryClassName =
                pluginProperties.getProperty("factory");
            if (factoryClassName == null)
            {
                throw new IllegalStateException(
                    "Factory is not set for plugin '" + pluginName + "'");
            }
            try
            {
                Class<?> aClass = Class.forName(factoryClassName);
                Object o = aClass.newInstance();
                RivetCamPluginFactory factory = (RivetCamPluginFactory) o;
                RivetCamPlugin rivetCamPlugin =
                    factory.create(
                        config,
                        pluginProperties,
                        appController);
                plugins.add(rivetCamPlugin);
                logger.info("Plugin '" + pluginName + "' created.");
            }
            catch (ClassNotFoundException e)
            {
                logger.error(e);
                throw new IllegalStateException(
                    "Factory '" + factoryClassName +
                        "' not found for plugin '" + pluginName + "'", e);
            }
            catch (IllegalAccessException | InstantiationException e)
            {
                logger.error(e);
                throw new IllegalStateException(
                    "Factory '" + factoryClassName +
                        "' cannot be created for plugin '" + pluginName + "'", e);
            }
        }
    }

    public void shutdownPlugins()
    {
        for (RivetCamPlugin plugin : plugins)
        {
            plugin.shutdown();
        }
    }

    public void batchFinished(File workingDirectory)
    {
        for (RivetCamPlugin plugin : plugins)
        {
            plugin.batchFinished(workingDirectory);
        }
    }
}
