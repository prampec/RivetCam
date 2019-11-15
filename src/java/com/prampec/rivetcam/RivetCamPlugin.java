package com.prampec.rivetcam;

import java.io.File;

/**
 * Plugins allowed to use AppController to control the application.
 */
public interface RivetCamPlugin
{
    void shutdown();

    void batchFinished(File workingDirectory);
}
