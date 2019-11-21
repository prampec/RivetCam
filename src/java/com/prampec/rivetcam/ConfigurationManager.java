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

import au.edu.jcu.v4l4j.DeviceInfo;
import com.prampec.util.KeyEventWrapper;
import com.prampec.util.PropertiesHelper;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class manages configuration for the application.
 * Created by kelemenb on 6/23/17.
 */
public class ConfigurationManager {
    private static final Logger logger =
        LogManager.getLogger(ConfigurationManager.class);

    private final Properties properties;
    String videoDevice;
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
    Dimension fixedWindowSize;
    boolean returnToLiveViewAfterPlayback;
    Map<String, Properties> plugins = new LinkedHashMap<>();

    public ConfigurationManager() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("setup.properties"));
        } catch (IOException e) {
            logger.error("Error loading properties: ", e);
        }
        videoDevice = properties.getProperty("videoDevice");
        String videoDeviceByName = properties.getProperty("videoDeviceByName");
        if ((videoDevice != null) && (videoDeviceByName != null)) {
            logger.error(
                    "Configuration warning! Both videoDevice and videoDeviceByName are specified. Only the videoDevice will be used.");
        }
        if (videoDevice == null) {
            if (videoDeviceByName != null) {
                videoDevice = getVideoDeviceByName(videoDeviceByName);
            }
            else {
                videoDevice = "/dev/video0";
            }
        }
        fixedWindowSize = parseDimension(properties.getProperty("fixedWindowSize"));
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
                properties.getProperty("restartFileIndexWithNewDirectory", "False"));
        enableBeep = Boolean.parseBoolean(
                properties.getProperty("enableBeep", "True"));
        returnToLiveViewAfterPlayback = Boolean.parseBoolean(
                properties.getProperty("returnToLiveViewAfterPlayback", "False"));

        plugins = PropertiesHelper.readSubProperties(properties, "plugin");
    }

    private String getVideoDeviceByName(String videoDeviceName) {
        if ((videoDeviceName == null) || (videoDeviceName.isEmpty())) {
            return null;
        }
        Map<String, DeviceInfo> devices = CameraTools.listV4KDevicesWithInfo();
        if (devices != null) {
            for (DeviceInfo deviceInfo : devices.values()) {
                if (videoDeviceName.equals(deviceInfo.getName())) {
                    return deviceInfo.getDeviceFile();
                }
            }
        }
        logger.error(
                "videoDeviceByName configuration was set, but no device found with name: "
                        + videoDeviceName);
        return null;
    }

    private static Dimension parseDimension(String value) {
        if (value == null)
        {
            return null;
        }
        String[] split = value.split("x");
        Dimension d = new Dimension(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        return d;
    }

    protected void readManualList() {
        manualList = PropertiesHelper.readList(
                properties, "manual", (properties, prefix, id) ->
            {
                ManualControl mc = new ManualControl();
                mc.name = properties.getProperty(prefix + "name");
                mc.value = properties.getProperty(prefix + "value");
                return mc;
            });
    }

    protected void readPreserveList() {
        preserveList = PropertiesHelper.readList(
                properties, "preserve",
            (properties, prefix, id) -> properties.getProperty(prefix + "name"));
    }

    protected void readKeysList() {
        keyList = PropertiesHelper.readList(
                properties, "keys", (properties, prefix, id) ->
            {
                ControlKey key = new ControlKey();
                key.name = properties.getProperty(prefix + "name");
                key.inc = KeyEventWrapper.valueOf(properties.getProperty(prefix + "inc"));
                key.dec = KeyEventWrapper.valueOf(properties.getProperty(prefix + "dec"));
                return key;
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

    ///////////////////////////////////////////////////////////////////////

    public String getVideoDevice()
    {
        return videoDevice;
    }

    public List<ManualControl> getManualList()
    {
        return manualList;
    }

    public List<String> getPreserveList()
    {
        return preserveList;
    }

    public List<ControlKey> getKeyList()
    {
        return keyList;
    }

    public int getPlaybackFps()
    {
        return playbackFps;
    }

    public int getOsdFontSize()
    {
        return osdFontSize;
    }

    public Dimension getLiveViewResolution()
    {
        return liveViewResolution;
    }

    public Dimension getStillImageResolution()
    {
        return stillImageResolution;
    }

    public long getDelayMsBeforeSnapshot()
    {
        return delayMsBeforeSnapshot;
    }

    public int getImageCacheSize()
    {
        return imageCacheSize;
    }

    public float getOnionAlpha()
    {
        return onionAlpha;
    }

    public String getDirectoryPrefix()
    {
        return directoryPrefix;
    }

    public int getDirectoryIndexDigits()
    {
        return directoryIndexDigits;
    }

    public String getFilePrefix()
    {
        return filePrefix;
    }

    public String getFilePostfix()
    {
        return filePostfix;
    }

    public int getFileIndexDigits()
    {
        return fileIndexDigits;
    }

    public String getBaseDirectoryPath()
    {
        return baseDirectoryPath;
    }

    public boolean isRestartFileIndexWithNewDirectory()
    {
        return restartFileIndexWithNewDirectory;
    }

    public boolean isEnableBeep()
    {
        return enableBeep;
    }

    public Dimension getFixedWindowSize()
    {
        return fixedWindowSize;
    }

    public boolean isReturnToLiveViewAfterPlayback()
    {
        return returnToLiveViewAfterPlayback;
    }
}
