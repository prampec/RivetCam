package com.prampec.rivetcam;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.imageio.ImageIO;
import javax.swing.*;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

/**
 * Controls application lifetime actions.
 */
public class AppControllerImpl implements AppController, CaptureCallback
{
    private static final String[] KEY_INFO = {
        "Esc - Quit", "L - Live view",
        "Space - Capture", "P - Playback",
        "Backspace - Remove", "Arrows - Prev/Next",
        "O - Onion skin", "B - New batch",
    };

    private int lastImagesCacheSize;

    private enum Mode {
        LIVE_VIEW,
        CAPTURING,
        PREVIEW,
        PLAYBACK,
    }

    private int onion = 2;
    private boolean keyInfoOn = false;

    private final static long snapshotDelayMs = 3000;
    private final MainFrame mainFrame;
    private final CameraManager cameraManager;
    private final OnScreenDisplay onScreenDisplay;
    private BufferedImage image = null;
    private boolean snapshotInProgress = false;
    private Date snapshotEffectTime;
    private FileManager fileManager;
    private Map<String, Integer> savedControls;
    private int activePreviewImage = 0;
    private Mode mode = Mode.LIVE_VIEW;
    private ConfigurationManager configurationManager;
    private java.util.Timer welcomeTimer = new Timer();

    private LinkedList<BufferedImage> lastImagesCache = new LinkedList<BufferedImage>() {
        @Override
        public boolean add(BufferedImage bufferedImage) {
            if (size() > lastImagesCacheSize) {
                removeFirst();
            }
            return super.add(bufferedImage);
        }
    };

    AppControllerImpl(
        MainFrame mainFrame, ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
        lastImagesCacheSize = this.configurationManager.imageCacheSize;
        fileManager = new FileManager(this.configurationManager);

        this.mainFrame = mainFrame;
        cameraManager = new CameraManager(
            this.configurationManager.videoDevice,
            this.configurationManager.preserveList,
            this.configurationManager.manualList
        );
        cameraManager.disableAuto();

        onScreenDisplay = new OnScreenDisplay(new OnScreenDisplay.OsdEventListener() {
            @Override
            public void newMessageArrived(List<String> messages) {
                mainFrame.imageContainer.repaint();
            }

            @Override
            public void noMessagesToDisplay() {
                mainFrame.imageContainer.repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g)
    {
        BufferedImage imageToShow;
        if (cameraManager.isCapturing()) {
            imageToShow = image;
            showImages((Graphics2D) g, imageToShow);
        } else if (lastImagesCache.size() > 0) {
            imageToShow = lastImagesCache.get(lastImagesCache.size() - activePreviewImage - 1);
            showImage(g, imageToShow);
        }
        paintOsd(g);
    }

    @Override
    public void showKeyInfo()
    {
        keyInfoOn = true;
    }

    @Override
    public void hideKeyInfo()
    {
        keyInfoOn = false;
    }

    public void liveViewMode()
    {
        SwingUtilities.invokeLater(
            () -> setLiveView(!cameraManager.isCapturing()));
    }

    @Override
    public void playbackMode()
    {
        SwingUtilities.invokeLater(() ->
        {
            setLiveView(false);
            startPlayback();
        });
    }

    @Override
    public void changeOnionSkinLevel()
    {
        onion += 1;
        if (onion > 2) {
            onion = 0;
        }
    }

    @Override
    public void removeLastImage()
    {
        String removed = fileManager.removeLast();
        if (!lastImagesCache.isEmpty()) {
            lastImagesCache.removeLast(); // TODO: might want to load images to cache.
        }
        onScreenDisplay.add("Last image (" + removed + ") removed.");
    }

    @Override
    public void snapshot()
    {
        if (mode != AppControllerImpl.Mode.CAPTURING) {
            doSnapshot();
        }
    }

    private void startPlayback() {
        mode = Mode.PLAYBACK;
        this.activePreviewImage = lastImagesCache.size()-1;
        mainFrame.repaintImage();
        final Timer tm = new Timer();
        tm.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (activePreviewImage == 0) {
                    tm.cancel();
                    if (configurationManager.returnToLiveViewAfterPlayback)
                    {
                        liveViewMode();
                    }
                } else {
                    activePreviewImage -= 1;
                    mainFrame.repaintImage(true); // TODO: real time paint
//                    imageContainer.paintImmediately(0, 0, imageContainer.getWidth(), imageContainer.getHeight());
                }
            }
        }, 0, 1000/configurationManager.playbackFps);
    }

    private void createNewBatchIfNone() {
        if (!fileManager.hasBatch()) {
            createNewBatch();
        }
    }

    @Override
    public void createNewBatch() {
        String batchName = fileManager.createNewWorkingDirectory();
        onScreenDisplay.add("New batch: " + batchName);
        activePreviewImage = 0;
        lastImagesCache.clear();
    }

    @Override
    public void showNextImage()
    {
        activePreviewImage = activePreviewImage > 0 ? activePreviewImage - 1 : 0;
        mainFrame.repaintImage();
    }

    @Override
    public void showPreviousImage()
    {
        activePreviewImage = activePreviewImage < (lastImagesCache.size()-1) ? activePreviewImage + 1 : lastImagesCache.size()-1;
        mainFrame.repaintImage();
    }

    @Override
    public void exit()
    {
        SwingUtilities.invokeLater( () -> {
            mainFrame.setVisible(false);
            mainFrame.dispose();
        });
        //            System.exit(0);
    }

    @Override
    public void adjustCameraControl(String controlName, int increment)
    {
        int value = cameraManager.setControl(controlName, increment);
        onScreenDisplay.add(controlName, controlName + " set to: " + value);
    }

    private void paintOsd(Graphics g) {
        int fontSize = configurationManager.osdFontSize;
        g.setFont(new Font("Helvetia", Font.PLAIN, fontSize));

        List<String> messages = onScreenDisplay.messagesToDisplay();
        if (messages != null) {
            int y = 2 * fontSize;
            for (String message : messages) {
                drawText(g, fontSize, y, message, Color.magenta, 0);
                y += fontSize + fontSize /2;
            }
        }
        if (keyInfoOn) {
            int y = 2 * fontSize;
            Color blue = new Color(130, 130, 255);
            for (int i = 0; i < KEY_INFO.length; i += 2) {
                String message1 = KEY_INFO[i];
                String message2 = KEY_INFO[i + 1];
                drawText(g, fontSize, y, message1,blue, 0);
                drawText(g, fontSize, y, message2, blue, mainFrame.getWidth()/2);
                y += fontSize + fontSize / 2;
            }
            for (ConfigurationManager.ControlKey controlKey : configurationManager.keyList) {
                String message = controlKey.dec + "/" + controlKey.inc + " - " + controlKey.name;
                drawText(g, fontSize, y, message, blue, 0);
                y += fontSize + fontSize / 2;
            }
        }
    }

    private void drawText(
        Graphics g,
        int fontSize,
        int y,
        String message,
        Color textColor,
        int xOffset) {
        g.setColor(Color.black);
        g.drawString(message, fontSize + 2 + xOffset, y + 2);
        g.setColor(textColor);
        g.drawString(message, fontSize + xOffset    , y);
    }

    private void showImage(Graphics g, BufferedImage image) {
        if (image != null) {
            int height = image.getHeight();
            Dimension imageContainer = mainFrame.getImageDimension();
            double factor = imageContainer.getWidth() / image.getWidth();
            height = (int) ((double) height * factor);
            g.drawImage(image, 0, (imageContainer.height - height) / 2, imageContainer.width, height, mainFrame);
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

    private void doSnapshot() {
        onScreenDisplay.add("capture", "Capturing...");
        mode = Mode.CAPTURING;
        final boolean liveViewWasOn;
        if (cameraManager.isCapturing()) {
            liveViewWasOn = true;
            savedControls = cameraManager.saveControls();
            cameraManager.stop();
            snapshotEffectTime = new Date(new Date().getTime() + configurationManager.delayMsBeforeSnapshot);
        } else {
            snapshotEffectTime = new Date(new Date().getTime() + snapshotDelayMs);
            savedControls = null;
            liveViewWasOn = false;
        }

        snapshotInProgress = true;
        cameraManager.startStill(this, configurationManager.stillImageResolution);
        if (savedControls != null) {
            cameraManager.loadControls(savedControls);
        }
        Thread th = new Thread(() ->
        {
            while (snapshotInProgress)
            {
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
            cameraManager.stop();

            if (liveViewWasOn)
            {
                startCapture();
                cameraManager.loadControls(savedControls);
                mode = Mode.LIVE_VIEW;
            }
            else
            {
                mode = Mode.PLAYBACK;
                mainFrame.repaintImage();
            }
            if (configurationManager.enableBeep)
            {
                Toolkit.getDefaultToolkit().beep();
            }
        });
        th.start();
        //        this.requestFocusInWindow();
    }

    private void setLiveView(boolean liveView) {
        if (liveView) {
            startCapture();
        } else if (cameraManager.isCapturing()) {
            savedControls = cameraManager.saveControls();
            cameraManager.stop();
            activePreviewImage = 0;
        }
        SwingUtilities.invokeLater(() -> mainFrame.requestFocusInWindow());
    }

    public void startCapture() {
        cameraManager.start(this, configurationManager.liveViewResolution);
        if (savedControls != null) {
            cameraManager.loadControls(savedControls);
        }
    }

    @Override
    public void shutdown()
    {
        welcomeTimer.cancel();
        onScreenDisplay.dispose();
        if (cameraManager.isCapturing()) {
            cameraManager.stop();
        }
        cameraManager.dispose();
    }

    public void nextFrame(VideoFrame frame) {
        BufferedImage bufferedImage = frame.getBufferedImage();
        if (snapshotInProgress) {
            if (new Date().after(snapshotEffectTime)) {
                try {
                    createNewBatchIfNone();
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
            mainFrame.repaintImage();
        }
        frame.recycle();
    }

    public void exceptionReceived(V4L4JException e) {
        e.printStackTrace();
    }

    @Override
    public void startWelcomeMessage()
    {
        welcomeTimer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                String message = null;
                switch (count) {
                    case 0:
                        message = "Welcome!";
                        break;
                    case 1:
                        String cameraName = cameraManager.getCameraName();
                        message = "Camera: " + cameraName;
                        break;
                    case 2:
                        message = "Press 'K' for keys info.";
                        break;
                }
                if (message != null) {
                    onScreenDisplay.add("welcome", message);
                }
                count += 1;
                if (count > 2) {
                    welcomeTimer.cancel();
                }
            }
        }, 1000, 3000);
    }
}
