/*
 * File: CameraTools.java
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

import au.edu.jcu.v4l4j.*;
import au.edu.jcu.v4l4j.exceptions.ControlException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import java.io.File;
import java.util.*;

/**
 * V4L helper methods and diagnostics functions.
 * Created by kelemenb on 6/23/17.
 */
public class CameraTools {
    private static String v4lSysfsPath = "/sys/class/video4linux/";

    public static List<String> listV4LDeviceFiles() {
        List<String> devices = new ArrayList<>();
        File dir = new File(v4lSysfsPath);
        String[] files = dir.list();

        if (files == null) {
            return null;
        }

        for (String file : files) {
            if (file.contains("video"))
                devices.add("/dev/" + file);
        }

        return devices;
    }

    public static Map<String, DeviceInfo> listV4KDevicesWithInfo() {
        List<String> devices = listV4LDeviceFiles();
        if ((devices == null) || devices.isEmpty()) {
            return null;
        }

        Map<String, DeviceInfo> result = new LinkedHashMap<>(devices.size());
        for (String device : devices) {
            try {
                VideoDevice vd = new VideoDevice(device);
                DeviceInfo di = vd.getDeviceInfo();
                result.put(device, di);
            } catch (V4L4JException e) {
                result.put(device, null);
            }
        }
        return result;
    }

    public static void dumpDevicesMiniInfo() {
        Map<String, DeviceInfo> devicesWithInfo = listV4KDevicesWithInfo();
        if ((devicesWithInfo == null) || (devicesWithInfo.isEmpty())) {
            System.out.println("No video devices were found.");
            return;
        }

        System.out.println("Found video devices:");
        for (String deviceName : devicesWithInfo.keySet()) {
            DeviceInfo deviceInfo = devicesWithInfo.get(deviceName);
            System.out.print("  " + deviceName + " - ");
            if (deviceInfo == null) {
                System.out.println("<device not compatible>");
            } else {
                System.out.println(deviceInfo.getName());
            }
        }
    }

    public static void dumpDeviceInfo(String device) {
        try {
            VideoDevice vd = new VideoDevice(device);
            DeviceInfo di = vd.getDeviceInfo();

            System.out.println("Info for device " + device + ":");
            System.out.println("  Name: " + di.getName());
            System.out.println("  Resolutions: " + getResolutions(di));
            System.out.println("  Attributes: " + getAttributes(vd.getControlList()));
        } catch (V4L4JException e) {
            e.printStackTrace();
        }
    }

    private static String getAttributes(ControlList controlList) {
        StringBuilder sb = new StringBuilder();
        List<Control> controls = new ArrayList<>(controlList.getList());
        Collections.sort(controls, new Comparator<Control>() {
            @Override
            public int compare(Control o1, Control o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Control control : controls) {
            try {
                sb.append(dumpControl(control));
            } catch (ControlException e) {
                // ignore this
            }
        }
        return sb.toString();
    }

    private static String dumpControl(Control c) throws ControlException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n    \"").append(c.getName()).append("\" = ");
        if (c.getType() == V4L4JConstants.CTRL_TYPE_LONG) {
            sb.append(c.getLongValue());
        } else if (c.getType() == V4L4JConstants.CTRL_TYPE_DISCRETE) {
            sb.append(c.getValue()).append(" (");
            Map<String, Integer> valueByNameMap = c.getDiscreteValuesMap();
            boolean first = true;
            for (String name : valueByNameMap.keySet()) {
                if (!first) {
                    sb.append(", ");
                } else {
                    first = false;
                }
                sb.append(valueByNameMap.get(name)).append(": ").append(name);
            }
            sb.append(")");
        } else {
            sb.append(c.getValue()).append(" (");
            sb.append(c.getMinValue()).append("..").append(c.getMaxValue()).append("/").append(c.getStepValue());
            sb.append(")");
        }
        return sb.toString();
    }


    private static String getResolutions(DeviceInfo di) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (ImageFormat imageFormat : di.getFormatList().getJPEGEncodableFormats()) {
            if (imageFormat.getResolutionInfo().getType() == ResolutionInfo.Type.DISCRETE) {
                sb.append("    ").append(imageFormat.getName()).append(" -");
                for (ResolutionInfo.DiscreteResolution discreteResolution : imageFormat.getResolutionInfo().getDiscreteResolutions()) {
                    sb.append(" ");
                    sb.append(discreteResolution.getWidth()).append("x").append(discreteResolution.getHeight());
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static ImageFormat getImageFormat(DeviceInfo di, String type) {
        for (ImageFormat imageFormat : di.getFormatList().getJPEGEncodableFormats()) {
            if (imageFormat.getResolutionInfo().getType() == ResolutionInfo.Type.DISCRETE) {
                if (type.equals(imageFormat.getName())) {
                    return imageFormat;
                }
            }
        }
        throw new IllegalStateException("No image format with type '" + type + "'.");
    }
}
