package com.prampec.rivetcam.plugins;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.prampec.rivetcam.AppController;
import com.prampec.rivetcam.RivetCamPlugin;
import com.prampec.util.ProcessHelper;

public class ConvertPlugin
    implements RivetCamPlugin
{
    private static final Logger logger =
        LogManager.getLogger(ConvertPlugin.class);

    private AppController appController;
    private int fps;
    private int directoryIndexDigits;
    private String outputFolder;

    public ConvertPlugin(
        AppController appController,
        int playbackFps,
        int directoryIndexDigits,
        String outputFolder)
    {
        this.appController = appController;
        this.fps = playbackFps;
        this.directoryIndexDigits = directoryIndexDigits;
        this.outputFolder = outputFolder;
    }

    public void shutdown()
    {
        ProcessHelper.stopBackgroundProcesses();
    }

    public void batchFinished(File workingDirectory)
    {
        createAnimation(
            workingDirectory);
    }

    protected void onConversionDone(File outFile)
    {
        String message = "Animation " + outFile.getName() + " was composed.";
        logger.info(message);
        appController.getOnScreenDisplay().add(message);
    }

    protected void onConversionFailed(File outFile, int exitCode)
    {
        String message = "Failed to write " + outFile.getName();
        appController.getOnScreenDisplay().add(message);
    }

    private void createAnimation(File workingDirectory)
    {
        String name = workingDirectory.getName();
        String number =
            name.substring(name.length() - directoryIndexDigits);
        File outFile = new File(outputFolder, "anim-" + number + ".mp4");

        Callback callback = (int exitCode) ->
        {
            if (exitCode == 0)
            {
                onConversionDone(outFile);
            }
            else
            {
                onConversionDone(outFile);
            }
        };

        ProcessHelper.runCmdWBackground(
            workingDirectory, callback,
            "ffmpeg -r " + fps + " -start_number 0 -i img-%04d.jpg " +
                outFile.getAbsolutePath());
    }

    @Override
    public void frameCaptured(File imageFile)
    {
        // not interested
    }

    public interface Callback
    {
        void perform(int exitCode);
    }
}
