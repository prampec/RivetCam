/*
 * File: ConfigurationManager.java
 * Description:
 *    RivetCam is an open source photographic software, where you
 *    can capture still images, potentially use to create stop-motion videos.
 *    Documentation: https://doc.csipa.hu/?page_id=415
 *
 * Author: Balazs Kelemen
 * Contact: prampec+rivetcam@gmail.com
 * Copyright: 2017 Balazs Kelemen
 * Copying permission statement:
 *     This file is part of RivetCam.
 *     RivetCam is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.prampec.rivetcam;

import com.prampec.util.KeyEventWrapper;
import com.prampec.util.PropertyHelper;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * This class manages configuration for the application.
 * Created by kelemenb on 6/23/17.
 */
public class ConfigurationManager {
    private final Properties properties;
    final String videoDevice;
    List<ManualControl> manualList;
    List<String> preserveList;
    List<ControlKey> keyList;
    int playbackFps;
    int osdFontSize;
    Dimension liveViewResolution;
    Dimension stillImageResolution;
    long delayMsBeforeSnapshot;
    int imageCacheSize;
    float onionAlpha;
    String directoryPrefix;
    int directoryIndexDigits;
    String filePrefix;
    String filePostfix;
    int fileIndexDigits;
    String baseDirectoryPath;
    boolean restartFileIndexWithNewDirectory;
    boolean enableBeep;

    public ConfigurationManager() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("setup.properties"));
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
        }
        videoDevice = properties.getProperty("videoDevice", "/dev/video0");
        playbackFps = Integer.parseInt(properties.getProperty("playbackFps", "20"));
        osdFontSize = Integer.parseInt(properties.getProperty("osdFontSize", "50"));
        liveViewResolution = parseDimension(properties.getProperty("liveView.resolution", "960x544"));
        stillImageResolution = parseDimension(properties.getProperty("stillImage.resolution", "1280x720"));
        delayMsBeforeSnapshot = Integer.parseInt(properties.getProperty("stillImage.delayMs", "1000"));
        imageCacheSize = Integer.parseInt(properties.getProperty("imageCacheSize", "10"));
        onionAlpha = Float.parseFloat(properties.getProperty("onionAlpha", "0.6"));
        readManualList();
        readPreserveList();
        readKeysList();

        directoryPrefix = properties.getProperty("directoryPrefix", "batch-");
        directoryIndexDigits = Integer.parseInt(properties.getProperty("directoryIndexDigits", "2"));
        filePrefix = properties.getProperty("filePrefix", "img-");
        filePostfix = properties.getProperty("filePostfix", ".jpg");
        fileIndexDigits = Integer.parseInt(properties.getProperty("fileIndexDigits", "4"));
        baseDirectoryPath = properties.getProperty("baseDirectoryPath", ".");
        restartFileIndexWithNewDirectory = Boolean.parseBoolean(
                properties.getProperty("restartFileIndexWithNewDirectory", "True"));
        enableBeep = Boolean.parseBoolean(
                properties.getProperty("enableBeep", "True"));
    }

    private static Dimension parseDimension(String value) {
        String[] split = value.split("x");
        Dimension d = new Dimension(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        return d;
    }

    protected void readManualList() {
        manualList = PropertyHelper.readList(
                properties, "manual",
                new PropertyHelper.PropertyReader<ManualControl>() {
            @Override
            public ManualControl readItem(Properties properties, String prefix, String id) {
                ManualControl mc = new ManualControl();
                mc.name = properties.getProperty(prefix + "name");
                mc.value = properties.getProperty(prefix + "value");
                return mc;
            }
        });
    }

    protected void readPreserveList() {
        preserveList = PropertyHelper.readList(
                properties, "preserve",
                new PropertyHelper.PropertyReader<String>() {
            @Override
            public String readItem(Properties properties, String prefix, String id) {
                return properties.getProperty(prefix + "name");
            }
        });
    }

    protected void readKeysList() {
        keyList = PropertyHelper.readList(
                properties, "keys",
                new PropertyHelper.PropertyReader<ControlKey>() {
                    @Override
                    public ControlKey readItem(Properties properties, String prefix, String id) {
                        ControlKey key = new ControlKey();
                        key.name = properties.getProperty(prefix + "name");
                        key.inc = KeyEventWrapper.valueOf(properties.getProperty(prefix + "inc"));
                        key.dec = KeyEventWrapper.valueOf(properties.getProperty(prefix + "dec"));
                        return key;
                    }
                });
    }

    static class ManualControl {
        String name;
        String value;
    }

    static class ControlKey {
        String name;
        KeyEventWrapper inc;
        KeyEventWrapper dec;
    }
}
