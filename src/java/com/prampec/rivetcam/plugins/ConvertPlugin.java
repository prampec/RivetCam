package com.prampec.rivetcam.plugins;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.prampec.rivetcam.AppController;
import com.prampec.rivetcam.RivetCamPlugin;

public class ConvertPlugin
    implements RivetCamPlugin
{
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
        stopBackgroundProcesses();
    }

    public void batchFinished(File workingDirectory)
    {
        createAnimation(
            workingDirectory);
    }

    protected void onConversionDone(File outFile)
    {
        String message = "Animation " + outFile.getName() + " was composed.";
        System.out.println(message);
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

        runCmdWBackground(
            workingDirectory, callback,
            "ffmpeg -r " + fps + " -start_number 0 -i img-%04d.jpg " +
                outFile.getAbsolutePath());
    }

    static List<Thread> backgroundProcesses =
        Collections.synchronizedList(new ArrayList<>());

    private static void runCmdWBackground(
        File workingDirectory, Callback callback, String cmdLine)
    {
        Thread thread = new Thread(() -> runCmd(
            workingDirectory, callback, cmdLine));
        thread.start();
        backgroundProcesses.add(thread);
    }

    public static void stopBackgroundProcesses()
    {
        for (Thread thread : backgroundProcesses)
        {
            System.out.println("Interrupting background process "
                + thread.getName());
            thread.interrupt();
        }
        while (!backgroundProcesses.isEmpty())
        {
            backgroundProcesses.removeIf(thread -> !thread.isAlive());
        }
        System.out.println("All background processes are finished.");
    }

    private static void runCmd(
        File workingDirectory, Callback callback, String cmdLine)
    {
        runCmd(workingDirectory, callback, cmdLine.split("\\s+"));
    }
    private static void runCmd(
        File workingDirectory, Callback callback, String... cmdLine)
    {
        System.out.println("Starting command in " +
            workingDirectory + Arrays.toString(cmdLine));
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(workingDirectory);
        pb.command(cmdLine);
        try
        {
            Process process = pb.start();

            Thread thStd = startStreamReadingThread(
                process, process.getInputStream(), System.out);
            Thread thErr = startStreamReadingThread(
                process, process.getErrorStream(), System.err);

            int exitCode = process.waitFor();
            thStd.interrupt();
            thErr.interrupt();
            thStd.join(1000);
            thErr.join(1000);

            // TODO: handle errors
            System.out.println("Command exit code = " + exitCode);

            backgroundProcesses.remove(Thread.currentThread());

            callback.perform(exitCode);
        }
        catch (IOException | InterruptedException ex)
        {
            // TODO: handle errors
            ex.printStackTrace(System.out);
        }
    }

    private static Thread startStreamReadingThread(
        Process process, InputStream reader, PrintStream out)
    {
        BufferedReader br =
            new BufferedReader(
                new InputStreamReader(reader));

        return new Thread()
        {
            @Override
            public void run()
            {
                String line;
                try
                {
                    while ((line = br.readLine()) != null)
                    {
                        out.println(line);
                        if (!process.isAlive())
                        {
                            break;
                        }
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    public interface Callback
    {
        void perform(int exitCode);
    }
}
