/*
 * File: MainFrame.java
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

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * The full screen application window.
 * Created by kelemenb on 6/17/17.
 */
public class MainFrame extends JFrame implements CaptureCallback {
    private int lastImagesCacheSize = 10;
    private enum Mode {
        LIVE_VIEW,
        CAPTURING,
        PREVIEW,
        PLAYBACK,
    }

    private final static long snapshotDelayMs = 3000;
    private JPanel imageContainer;

    private final CameraManager cameraManager;
    private final OnScreenDisplay onScreenDisplay;
    private BufferedImage image = null;
    private boolean snapshotInProgress = false;
    private Date snapshotEffectTime;
    private int onion = 2;
    private FileManager fileManager;
    private Map<String, Integer> controls;
    private int activePreviewImage = 0;
    private Mode mode = Mode.LIVE_VIEW;
    private ConfigurationManager configurationManager;

    private LinkedList<BufferedImage> lastImagesCache = new LinkedList<BufferedImage>() {
        @Override
        public boolean add(BufferedImage bufferedImage) {
            if (size() > lastImagesCacheSize) {
                removeFirst();
            }
            return super.add(bufferedImage);
        }
    };

    public static void main(String[] args) {
        if (args.length > 0) {
            if ("--devices".equals(args[0])) {
                CameraTools.dumpDevicesMiniInfo();
            }
            else if ("--info".equals(args[0])) {
                CameraTools.dumpDeviceInfo(args[1]);
            }
            System.exit(0);
        }
        ConfigurationManager configurationManager = new ConfigurationManager();
        MainFrame dialog = new MainFrame(configurationManager);
        try {
            GraphicsDevice device = GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getScreenDevices()[0];
            device.setFullScreenWindow(dialog);
            dialog.pack();
            dialog.setVisible(true);
            dialog.createNewBatch();
            while (dialog.isVisible()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        finally {
//            dialog.setVisible(false);
//            dialog.dispose();
        }
    }


    public MainFrame(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        lastImagesCacheSize = configurationManager.imageCacheSize;
        fileManager = new FileManager(configurationManager);

        imageContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage imageToShow = null;
                if (cameraManager.isCapturing()) {
                    imageToShow = image;
                    MainFrame.this.showImages((Graphics2D) g, imageToShow);
                } else if (lastImagesCache.size() > 0) {
                    imageToShow = lastImagesCache.get(lastImagesCache.size() - activePreviewImage - 1);
                    MainFrame.this.showImage(g, imageToShow);
                }
                MainFrame.this.paintOsd(g);
            }
        };

        setContentPane(imageContainer);
//        setModal(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        setTitle("FullScreen Capture");
        cameraManager = new CameraManager(
                configurationManager.videoDevice,
                configurationManager.preserveList,
                configurationManager.manualList
                );
        cameraManager.disableAuto();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeypressed(e);
            }
        });

        this.setFocusable(true);
        this.requestFocusInWindow();

        this.startCapture();

        onScreenDisplay = new OnScreenDisplay(new OnScreenDisplay.OsdEventListener() {
            @Override
            public void newMessageArrived(List<String> messages) {
                imageContainer.repaint();
            }

            @Override
            public void noMessagesToDisplay() {
                imageContainer.repaint();
            }
        });

    }

    private void handleKeypressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_L) {
            MainFrame.this.setLiveView(!cameraManager.isCapturing());
        } else if (e.getKeyCode() == KeyEvent.VK_P) {
            MainFrame.this.setLiveView(false);
            MainFrame.this.startPlayback();
        } else if (e.getKeyCode() == KeyEvent.VK_O) {
            onion += 1;
            if (onion > 2) {
                onion = 0;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            String removed = fileManager.removeLast();
            if (!lastImagesCache.isEmpty()) {
                lastImagesCache.removeLast(); // TODO: might want to load images to cache.
            }
            onScreenDisplay.add("Last image (" + removed + ") removed.");
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (mode != Mode.CAPTURING) {
                MainFrame.this.doSnapshot();
            }
        } else if (e.getKeyCode() == KeyEvent.VK_B) {
            createNewBatch();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            activePreviewImage = activePreviewImage > 0 ? activePreviewImage - 1 : 0;
            imageContainer.repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            activePreviewImage = activePreviewImage < (lastImagesCache.size()-1) ? activePreviewImage + 1 : lastImagesCache.size()-1;
            imageContainer.repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            MainFrame.this.setVisible(false);
            MainFrame.this.dispose();
//            System.exit(0);
        } else {
            processControls(e.getKeyCode());
        }
    }

    private void processControls(int keyCode) {
        for (ConfigurationManager.ControlKey controlKey : configurationManager.keyList) {
            if (keyCode == controlKey.inc.getKeyEvent()) {
                int value = cameraManager.setControl(controlKey.name, 1);
                onScreenDisplay.add(controlKey.name, controlKey.name + " set to: " + value);
                return;
            }
            else if (keyCode == controlKey.dec.getKeyEvent()) {
                int value = cameraManager.setControl(controlKey.name, -1);
                onScreenDisplay.add(controlKey.name, controlKey.name + " set to: " + value);
                return;
            }
        }
    }

    private void startPlayback() {
        mode = Mode.PLAYBACK;
        this.activePreviewImage = lastImagesCache.size()-1;
        imageContainer.repaint();
        final Timer tm = new Timer();
        tm.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (activePreviewImage == 0) {
                    tm.cancel();
                } else {
                    activePreviewImage -= 1;
                    imageContainer.revalidate(); // TODO: real time paint
                    imageContainer.repaint();
//                    imageContainer.paintImmediately(0, 0, imageContainer.getWidth(), imageContainer.getHeight());
                }
            }
        }, 0, 1000/configurationManager.playbackFps);
    }

    private void createNewBatch() {
        String batchName = fileManager.createNewWorkingDirectory();
        onScreenDisplay.add("New batch: " + batchName);
        activePreviewImage = 0;
        lastImagesCache.clear();
    }

    private void paintOsd(Graphics g) {
        List<String> messages = onScreenDisplay.messagesToDisplay();
        if (messages != null) {
            int fontSize = configurationManager.osdFontSize;
            g.setFont(new Font("Helvetia", Font.PLAIN, fontSize));
            int y = 2 * fontSize;
            for (String message : messages) {
                g.setColor(Color.black);
                g.drawString(message, fontSize + 2, y + 2);
                g.setColor(Color.magenta);
                g.drawString(message, fontSize, y);
                y += fontSize + fontSize /2;
            }
        }
    }

    private void showImage(Graphics g, BufferedImage image) {
        if (image != null) {
            int height = image.getHeight();
            float factor = (float) imageContainer.getWidth() / image.getWidth();
            height = (int) ((float) height * factor);
            g.drawImage(image, 0, (imageContainer.getHeight() - height) / 2, imageContainer.getWidth(), height, this);
        }
    }

    private void showImages(Graphics2D g, BufferedImage liveImage) {
        float alpha = configurationManager.onionAlpha;
        int compositeRule = AlphaComposite.SRC_OVER;
        AlphaComposite composite = AlphaComposite.getInstance(compositeRule, alpha);
        int s = lastImagesCache.size();
        for (int i = s - onion; i < s; i++) {
            if (i >= 0) {
                showImage(g, lastImagesCache.get(i));
                g.setComposite(composite);
            }
        }
        showImage(g, liveImage);
    }

    private Thread th;
    private void doSnapshot() {
        onScreenDisplay.add("capture", "Capturing...");
        mode = Mode.CAPTURING;
        final boolean liveViewWasOn;
        if (cameraManager.isCapturing()) {
            liveViewWasOn = true;
            controls = cameraManager.saveControls();
            cameraManager.stop();
            snapshotEffectTime = new Date(new Date().getTime() + configurationManager.delayMsBeforeSnapshot);
        } else {
            snapshotEffectTime = new Date(new Date().getTime() + snapshotDelayMs);
            controls = null;
            liveViewWasOn = false;
        }

        snapshotInProgress = true;
        cameraManager.startStill(this, configurationManager.stillImageResolution);
        if (controls != null) {
            cameraManager.loadControls(controls);
        }
        th = new Thread()
        {
            @Override
            public void run() {
                while (snapshotInProgress) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                cameraManager.stop();

                if (liveViewWasOn) {
                    startCapture();
                    cameraManager.loadControls(controls);
                    mode = Mode.LIVE_VIEW;
                } else {
                    mode = Mode.PLAYBACK;
                    imageContainer.repaint();
                }

            }
        };
        th.start();
//        this.requestFocusInWindow();
    }

    public void setLiveView(boolean liveView) {
        if (liveView) {
            startCapture();
        } else if (cameraManager.isCapturing()) {
            controls = cameraManager.saveControls();
            cameraManager.stop();
            activePreviewImage = 0;
        }
        this.requestFocusInWindow();
    }

    private void startCapture() {
        cameraManager.start(this, configurationManager.liveViewResolution);
        if (controls != null) {
            cameraManager.loadControls(controls);
        }
    }

    public void nextFrame(VideoFrame frame) {
        BufferedImage bufferedImage = frame.getBufferedImage();
        if (snapshotInProgress) {
            if (new Date().after(snapshotEffectTime)) {
                try {
                    File outputfile = fileManager.getNextFile();
                    ImageIO.write(bufferedImage, "jpg", outputfile);
                    lastImagesCache.add(bufferedImage);
                    onScreenDisplay.replace("capture", "Frame saved to: " + fileManager.formatName(outputfile) );
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    snapshotInProgress = false;
                }
            }
        } else {
//            System.out.print(".");
            image = bufferedImage;
            imageContainer.repaint();
        }
        frame.recycle();
    }

    public void exceptionReceived(V4L4JException e) {
        e.printStackTrace();
    }

    @Override
    public void dispose() {
        onScreenDisplay.dispose();
        if (cameraManager.isCapturing()) {
            cameraManager.stop();
        }
        cameraManager.dispose();
        super.dispose();
    }
}
