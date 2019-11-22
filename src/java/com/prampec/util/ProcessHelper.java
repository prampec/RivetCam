package com.prampec.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.prampec.rivetcam.plugins.ConvertPlugin;

public class ProcessHelper
{
    private static final Logger logger =
        LogManager.getLogger(ProcessHelper.class);

    static List<Thread> backgroundProcesses =
        Collections.synchronizedList(new ArrayList<>());

    public static void runCmdWBackground(
        File workingDirectory, ConvertPlugin.Callback callback, String cmdLine)
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
            logger.info("Interrupting background process "
                + thread.getName());
            thread.interrupt();
        }
        while (!backgroundProcesses.isEmpty())
        {
            backgroundProcesses.removeIf(thread -> !thread.isAlive());
        }
        logger.info("All background processes are finished.");
    }

    private static void runCmd(
        File workingDirectory, ConvertPlugin.Callback callback, String cmdLine)
    {
        runCmd(workingDirectory, callback, cmdLine.split("\\s+"));
    }
    private static void runCmd(
        File workingDirectory, ConvertPlugin.Callback callback, String... cmdLine)
    {
        logger.info("Starting command in " +
            workingDirectory + Arrays.toString(cmdLine));
        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(workingDirectory);
        pb.command(cmdLine);
        try
        {
            Process process = pb.start();

            // TODO: dump stdOut, and stdErr to log4j
            Thread thOut = startStreamReadingThread(
                process, process.getInputStream(), System.out);
            Thread thErr = startStreamReadingThread(
                process, process.getErrorStream(), System.err);

            int exitCode = process.waitFor();
            thOut.interrupt();
            thErr.interrupt();
            thOut.join(1000);
            thErr.join(1000);

            // TODO: handle errors
            logger.info("Command exit code = " + exitCode);

            backgroundProcesses.remove(Thread.currentThread());

            if (callback != null)
            {
                callback.perform(exitCode);
            }
        }
        catch (IOException | InterruptedException e)
        {
            // TODO: handle errors
            logger.error(e);
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
                    logger.error(e);
                    throw new IllegalStateException(e);
                }
            }
        };
    }


}
