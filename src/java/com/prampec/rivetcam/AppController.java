package com.prampec.rivetcam;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Controls application lifetime actions.
 */
public interface AppController
{
    void liveViewMode();

    void playbackMode();

    void changeOnionSkinLevel();

    void switchOnionSkin(boolean on);

    void paint(Graphics g);

    void startCapture();

    void shutdown();

    void startWelcomeMessage();

    void showKeyInfo();

    void hideKeyInfo();

    void removeLastImage();

    void snapshot();

    void createNewBatch();

    void showNextImage();

    void showPreviousImage();

    void exit();

    void adjustCameraControl(String controlName, int value);
}
