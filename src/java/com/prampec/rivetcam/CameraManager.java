/*
 * File: CameraManager.java
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
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Helper class to access video camera functions.
 * <p>
 * Created by kelemenb on 6/17/17.
 */
public class CameraManager {
    private final DeviceInfo di;
    private VideoDevice videoDevice;
    private List<String> preserve;
    private List<ConfigurationManager.ManualControl> manualList;
    private FrameGrabber grabber = null;

    public CameraManager(
            String deviceFile,
            List<String> preserve,
            List<ConfigurationManager.ManualControl> manualList) {
        System.out.println("Opening video device: " + deviceFile);
        videoDevice = getVideoDevice(new File(deviceFile));
        this.preserve = preserve;
        this.manualList = manualList;
        try {
            di = videoDevice.getDeviceInfo();
        } catch (V4L4JException e) {
            throw new IllegalStateException("Cannot get video device information.", e);
        }
        System.out.println("Video device name: " + di.getName());
    }

    private static VideoDevice getVideoDevice(File file) {
        try {
            return new VideoDevice(file.getAbsolutePath());
        } catch (V4L4JException e) {
            throw new IllegalStateException("Cannot instantiate V4L4J device from " + file, e);
        }
    }

    public void start(CaptureCallback captureCallback, Dimension d) {
        try {
            // TODO: also define image format
            grabber = videoDevice.getJPEGFrameGrabber(d.width, d.height, 0, 0, 80, null);
        } catch (V4L4JException e) {
            throw new IllegalStateException(e);
        }

        grabber.setCaptureCallback(captureCallback);

        try {
            grabber.startCapture();
        } catch (V4L4JException e) {
            throw new IllegalStateException(e);
        }
    }

    public void startStill(CaptureCallback captureCallback, Dimension d) {
        try {
            // TODO: also define image format
            grabber = videoDevice.getJPEGFrameGrabber(d.width, d.height, 0, 0, 98, null);
        } catch (V4L4JException e) {
            throw new IllegalStateException(e);
        }

        grabber.setCaptureCallback(captureCallback);

        try {
            grabber.startCapture();
        } catch (V4L4JException e) {
            throw new IllegalStateException(e);
        }
    }

    public synchronized void stop() {
        try {
            if (grabber != null) {
                grabber.stopCapture();
            }
        } catch (StateException e) {
            e.printStackTrace();
        }

        grabber = null;
        videoDevice.releaseFrameGrabber();
    }

    public boolean isCapturing() {
        return grabber != null;
    }

    public void dispose() {
        try {
            videoDevice.releaseControlList();
            videoDevice.release();
        } catch (au.edu.jcu.v4l4j.exceptions.StateException e) {
            // ignore this
        }
    }

    /**
     * Disables all features of the camera, that is changed over time by the camera itself.
     */
    public void disableAuto() {
        ControlList controlList = videoDevice.getControlList();
        for (ConfigurationManager.ManualControl manualControl : manualList) {
            try {
                Control control = controlList.getControl(manualControl.name);
                if (control != null) {
                    control.setValue(Integer.parseInt(manualControl.value));
                }
                else {
                    System.err.println(
                            "'" + manualControl.name + "' is defined for manual control, but camera '"
                            + getCameraName() + "' does not provide this control. Try running diagnostics!");
                }
            } catch (ControlException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Integer> saveControls() {
        Map<String, Integer> controlsToSave = new HashMap<>();
        ControlList controlList = videoDevice.getControlList();

        for (String controlName : preserve) {
            try {
                addControlValueToMap(controlList, controlsToSave, controlName);

            } catch (ControlException e) {
                e.printStackTrace();
            }
        }

        return controlsToSave;
    }

    public void loadControls(Map<String, Integer> save) {
        ControlList controlList = videoDevice.getControlList();
        try {
            for (String controlName : save.keySet()) {
                Control control = controlList.getControl(controlName);
                if (control == null) {
                    System.err.println(
                            "'" + controlName + "' is defined as a persistable control, but camera '"
                                    + getCameraName() + "' does not provide this control. Try running diagnostics!");
                    continue;
                }
                // TODO: handle non-integer values
                Integer value = save.get(controlName);
                {
                    // -- Not-changing-value workaround
                    if (value < control.getMaxValue()) {
                        control.setValue(value + control.getStepValue());
                    } else {
                        control.setValue(value - control.getStepValue());
                    }
                }
                control.setValue(value);
            }
        } catch (ControlException e) {
            e.printStackTrace();
        }
    }

    private void addControlValueToMap(
            ControlList controlList, Map<String, Integer> controlsToSave, String controlName) throws ControlException {
        Control control = controlList.getControl(controlName);
        if (control != null) {
            // TODO: handle non-integer values
            controlsToSave.put(controlName, control.getValue());
        }
        else {
            System.err.println(
                    "'" + controlName + "' is defined as a persistable control, but camera '"
                            + getCameraName() + "' does not provide this control. Try running diagnostics!");
        }
    }

    public int setControl(String controlName, int increment) {
        ControlList controlList = videoDevice.getControlList();
        try {
            Control control = controlList.getControl(controlName);
            if (control == null) {
                System.err.println(
                        "Trying to set control value for '" + controlName + "', but camera '"
                                + getCameraName() + "' does not provide this control. Try running diagnostics!");
                return -1;
            }
            // TODO: handle non-integer values
            int value = control.getValue();
            value += increment * control.getStepValue();
            if (value < control.getMinValue()) {
                value = control.getMinValue();
            } else if (value > control.getMaxValue()) {
                value = control.getMaxValue();
            }
            control.setValue(value);
            return value / control.getStepValue();
        } catch (ControlException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getCameraName() {
        return di.getName();
    }
}
