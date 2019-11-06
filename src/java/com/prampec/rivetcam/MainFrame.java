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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The full screen application window.
 * Created by kelemenb on 6/17/17.
 */
public class MainFrame extends JFrame {
    JPanel imageContainer;
    private AppController appController;
    private ConfigurationManager configurationManager;

    public static void main(String[] args) {
        if (args.length > 0) {
            String arg = args[0];
            if ("--devices".equals(arg)) {
                CameraTools.dumpDevicesMiniInfo();
            }
            else if ("--info".equals(arg)) {
                CameraTools.dumpDeviceInfo(args[1]);
            }
            else {
                System.err.println("Unknown argument '" + arg + "'");
            }
            System.exit(0);
        }
        ConfigurationManager configurationManager = new ConfigurationManager();
        MainFrame dialog = new MainFrame(configurationManager);
        try {
            if (configurationManager.fixedWindowSize == null) {
                GraphicsDevice device =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
                device.setFullScreenWindow(dialog);
            }
            else {
                dialog.setPreferredSize(configurationManager.fixedWindowSize);
            }
            dialog.pack();
            dialog.setVisible(true);
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

        imageContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                appController.paint(g);
            }
        };

        setContentPane(imageContainer);
//        setModal(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setUndecorated(true);
        setTitle("RivetCam");

        appController = new AppControllerImpl(this, configurationManager);
        appController.startCapture();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                MainFrame.this.handleKeyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                MainFrame.this.handleKeyReleased(e);
            }
        });

        this.setFocusable(true);
        this.requestFocusInWindow();

    }

    private void handleKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_K) {
            appController.showKeyInfo();
        }
        else if (e.getKeyCode() == KeyEvent.VK_L) {
            appController.liveViewMode();
        } else if (e.getKeyCode() == KeyEvent.VK_P) {
            appController.playbackMode();
        } else if (e.getKeyCode() == KeyEvent.VK_O) {
            appController.changeOnionSkinLevel();
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            appController.removeLastImage();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            appController.snapshot();
        } else if (e.getKeyCode() == KeyEvent.VK_B) {
            appController.createNewBatch();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            appController.showNextImage();
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            appController.showPreviousImage();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            appController.exit();
        } else {
            processControls(e.getKeyCode());
        }
    }

    private void processControls(int keyCode) {
        for (ConfigurationManager.ControlKey controlKey : configurationManager.keyList) {
            if (keyCode == controlKey.inc.getKeyEvent()) {
                appController.adjustCameraControl(controlKey.name, 1);
                return;
            }
            else if (keyCode == controlKey.dec.getKeyEvent()) {
                appController.adjustCameraControl(controlKey.name, -1);
                return;
            }
        }
    }

    private void handleKeyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_K) {
            appController.hideKeyInfo();
        }
    }


    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            appController.startWelcomeMessage();
        }
    }
    @Override
    public void dispose() {
        appController.shutdown();
        super.dispose();
    }

    void repaintImage()
    {
        SwingUtilities.invokeLater(() -> imageContainer.repaint());
    }

    public void repaintImage(boolean invalidate)
    {
        if (!invalidate)
        {
            repaintImage();
        }
        else
        {
            SwingUtilities.invokeLater(() -> {
                imageContainer.invalidate();
                imageContainer.repaint();
            });
        }
    }


    public Dimension getImageDimension()
    {
        return new Dimension(imageContainer.getWidth(), imageContainer.getHeight());
    }
}
