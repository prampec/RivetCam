package com.prampec.rivetcam.plugins;

import java.io.File;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.SoftTone;
import com.prampec.rivetcam.AppController;
import com.prampec.rivetcam.RivetCamPlugin;
import com.prampec.util.ProcessHelper;
import com.prampec.util.PropertiesHelper;

/**
 * Use Raspberry Pi (or compatible) GPIOs as inputs.
 */
public class RpiGpioPlugin implements RivetCamPlugin
{
    private static final Logger logger =
        LogManager.getLogger(RpiGpioPlugin.class);

    private static final String ACTION_SNAPSHOT = "snapshot";
    private static final String ACTION_REMOVE_LAST_IMAGE =
        "removeLastImage";
    private static final String ACTION_PLAYBACK = "playback";
    private static final String ACTION_NEW_BATCH = "newBatch";
    private static final Boolean ROTARY_STATE_EVENT_OCCURRED = true;
    private static final Boolean ROTARY_STATE_EVENT_CLEARED = false;
    private static final String TONE_PIN_PROPERTY = "tonePin";

    private final AppController appController;
    private GpioController gpio;
    private Map<String, Boolean> rotaryStates =
        new HashMap<>();

    private Queue<Tone> tonesToPlay = new LinkedList<>();
    private Thread toneThread = null;
    private Integer tonePin = null;

    public enum RotaryEvent
    {
        CW, CCW, Push, PushRelease
    }

    public RpiGpioPlugin(
        Properties properties, AppController appController)
    {
        this.appController = appController;

        connectPins(properties);
        playTones(toTones(200, "P1V1c2"));
    }

    @Override
    public void shutdown()
    {
        if (gpio != null)
        {
            gpio.shutdown();
        }
    }

    @Override
    public void batchFinished(File workingDirectory)
    {
        // Not interested.
    }

    private void connectPins(Properties properties)
    {
        Map<String, Properties> pins =
            PropertiesHelper.readSubProperties(properties, "pin");

        // create gpio controller
        gpio = GpioFactory.getInstance();

        for (String pinName : pins.keySet())
        {
            // TODO: introduce handler classes for pin types an factories.
            Properties pinProperties = pins.get(pinName);
            String pinType = pinProperties.getProperty("type");
            if ("gpio".equals(pinType))
            {
                String raspiPinName = pinProperties.getProperty("raspiPin");
                String action = pinProperties.getProperty("action");
                Pin raspiPin = getPin(raspiPinName);
                PinPullResistance pinPullResistance =
                    PinPullResistance.valueOf(
                        pinProperties.getProperty("pinPullResistance"));

                connectPin(gpio, raspiPin, pinPullResistance, action);
                logger.info(
                    "Pin " + raspiPin + " connected to " + action);
            }
            else if ("rotary".equals(pinType))
            {
                Pin raspiPinA =
                    getPin(
                        pinProperties.getProperty("raspiPinA"));
                Pin raspiPinB =
                    getPin(
                        pinProperties.getProperty("raspiPinB"));
                Pin raspiPinPush =
                    getPin(
                        pinProperties.getProperty("raspiPinPush"));
                PinPullResistance pinPullResistance =
                    PinPullResistance.valueOf(
                        pinProperties.getProperty("pinPullResistance"));

                String action = pinProperties.getProperty("action");
                connectRotary(
                    gpio, pinName,
                    raspiPinA, raspiPinB, raspiPinPush,
                    pinPullResistance, action);
                logger.info(
                    "Rotary " + pinName + " (" +
                        raspiPinA.toString() + ", " +
                        raspiPinB.toString() + ", " +
                        raspiPinPush.toString() + ") connected to " + action);
            }
        }

        if (properties.getProperty(TONE_PIN_PROPERTY) != null)
        {
            String tonePinStr = properties.getProperty(TONE_PIN_PROPERTY);
            Pin tonePin = getPin(tonePinStr);
            this.tonePin = tonePin.getAddress();
            SoftTone.softToneCreate(this.tonePin);
        }
    }

    private Pin getPin(String raspiPinName)
    {
        Pin raspiPin =
            RaspiPin.getPinByName(raspiPinName);
        if (raspiPin == null)
        {
            raspiPin = RaspiPin.getPinByName(raspiPinName.replaceAll("_", " "));
        }
        if (raspiPin == null)
        {
            throw new IllegalStateException("RaspberryPi pin for name '" + raspiPinName + "' not found.");
        }
        return raspiPin;
    }

    private void connectRotary(
        GpioController gpio, String pinName,
        Pin pinA,
        Pin pinB,
        Pin pinPush,
        PinPullResistance pinPullResistance,
        String action)
    {
        rotaryStates.put(pinName, ROTARY_STATE_EVENT_CLEARED);

        GpioPinDigitalInput inputA =
            gpio.provisionDigitalInputPin(pinA, pinPullResistance);
        inputA.setShutdownOptions(true);
//        inputA.setDebounce(10);

        GpioPinDigitalInput inputB =
            gpio.provisionDigitalInputPin(pinB, pinPullResistance);
        inputB.setShutdownOptions(true);
//        inputB.setDebounce(10);

        GpioPinDigitalInput inputPush =
            gpio.provisionDigitalInputPin(pinPush, pinPullResistance);
        inputPush.setShutdownOptions(true);
//        inputPush.setDebounce(10);

        inputA.addListener(
            (GpioPinListenerDigital) event ->
                {
//                    logger.info("> Pin " + event.getPin() + " " + event.getEdge() + " while " + inputB.getName() + " is " + inputB.getState());
                    if (inputB.isLow() &&
                        (event.getEdge() == PinEdge.RISING) &&
                        (rotaryStates.get(pinName) != ROTARY_STATE_EVENT_OCCURRED))
                    {
                        rotaryStates.put(pinName, ROTARY_STATE_EVENT_OCCURRED);
                        handleRotationEvent(pinName, action, RotaryEvent.CCW);
                    }
                    else if (inputB.isHigh() &&
                        (event.getEdge() == PinEdge.FALLING))
                    {
                        rotaryStates.put(pinName, ROTARY_STATE_EVENT_CLEARED);
                    }
                });
        inputB.addListener(
            (GpioPinListenerDigital) event ->
            {
//                logger.info("> Pin " + event.getPin() + " " + event.getEdge() + " while " + inputA.getName() + " is " + inputA.getState());
                if (inputA.isLow() &&
                    (event.getEdge() == PinEdge.RISING) &&
                    (rotaryStates.get(pinName) != ROTARY_STATE_EVENT_OCCURRED))
                {
                    rotaryStates.put(pinName, ROTARY_STATE_EVENT_OCCURRED);
                    handleRotationEvent(pinName, action, RotaryEvent.CW);
                }
                else if (inputA.isHigh() &&
                    (event.getEdge() == PinEdge.FALLING))
                {
                    rotaryStates.put(pinName, ROTARY_STATE_EVENT_CLEARED);
                }
            });
        inputPush.addListener(
            (GpioPinListenerDigital) event ->
            {
                boolean pushed =
                    event.getState().isLow() ==
                        event.getPin().isPullResistance(PinPullResistance.PULL_DOWN);
                handleRotationEvent(pinName,
                    action,
                    pushed ? RotaryEvent.Push : RotaryEvent.PushRelease);
            });
    }

    private void handleRotationEvent(
        String pinName, String action, RotaryEvent rotaryEvent)
    {
        //        logger.info(
        //            "Rotation on " + pinName + " (" + action + "): " + rotaryEvent);
        // TODO: utilize more control
        if (rotaryEvent == RotaryEvent.Push)
        {
            appController.switchOnionSkin(true);
        }
        else if (rotaryEvent == RotaryEvent.PushRelease)
        {
            appController.switchOnionSkin(false);
        }
        else
        {
            appController.adjustCameraControl("Focus (absolute)",
                rotaryEvent == RotaryEvent.CW ? 1 : -1);
        }
    }

    private void connectPin(
        GpioController gpio,
        Pin pin,
        PinPullResistance pinPullResistance,
        String action)
    {
        GpioPinDigitalInput button =
            gpio.provisionDigitalInputPin(pin, pinPullResistance);

        // set shutdown state for this input pin
        button.setShutdownOptions(true);
//        button.setDebounce(10);

        // create and register gpio pin listener
        button.addListener(
            (GpioPinListenerDigital) event ->
                handlePinEvent(
                    event.getPin(), event.getState(), event.getEdge(), action));
    }

    private boolean pinBackPressing = false;

    private void handlePinEvent(
        GpioPin pin, PinState state, PinEdge edge, String action)
    {
//        logger.info("> Pin " + pin + ": " + edge);
        if (state.isLow() == pin.isPullResistance(
            PinPullResistance.PULL_DOWN))
        {
            if (ACTION_REMOVE_LAST_IMAGE.equals(action))
            {
                pinBackPressing = false;
            }
            return; // Pin is in default state.
        }
//        logger.info("> activating action: " + action);
        // -- TODO: move action definitions to appController
        if (ACTION_SNAPSHOT.equals(action))
        {
            appController.snapshot();
        }
        else if (ACTION_REMOVE_LAST_IMAGE.equals(action))
        {
            pinBackPressing = true;
            appController.removeLastImage();
        }
        else if (ACTION_PLAYBACK.equals(action))
        {
            appController.playbackMode();
        }
        else if (ACTION_NEW_BATCH.equals(action))
        {
            if (pinBackPressing)
            {
                shutdownTheSystem();
            }
            else
            {
                appController.createNewBatch();
            }
        }
        else
        {
            throw new IllegalStateException("Unknown action '" + action + "'");
        }
    }

    private boolean shutdownInProgress = false;

    private synchronized void shutdownTheSystem()
    {
        if (!shutdownInProgress)
        {
            logger.info("Shutting down.");
            shutdownInProgress = true;
            playTones(toTones(400, "H1"));
            ProcessHelper
                .runCmdWBackground(null, null, "sudo shutdown -h now");
        }
    }

    @Override
    public void frameCaptured(File imageFile)
    {
        playTones(toTones(100, "l1_1l1"));
    }

    public synchronized void playTones(List<Tone> tones)
    {
        if (tonePin == null)
        {
            logger.trace(
                "Tone playing was requested, but not tone pin was defined");
            return;
        }
        if (toneThread != null)
        {
            toneThread.interrupt();
            try
            {
                toneThread.join(200);
            }
            catch (InterruptedException e)
            {
                logger.error("Error occurred", e);
            }
        }
        tonesToPlay.clear();
        tonesToPlay.addAll(tones);
        toneThread = new Thread(this::playTones);
        toneThread.start();
    }

    private void playTones()
    {
        while (true)
        {
            if (tonesToPlay.isEmpty())
            {
                SoftTone.softToneStop(tonePin);
                break;
            }
            else
            {
                Tone tone = tonesToPlay.poll();
                SoftTone.softToneWrite(tonePin, tone.frequency);
                try
                {
                    Thread.sleep(tone.length);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        }
    }

    private static double TROT = 1.05946309435929; // -- The twelfth root of two
    private static double A440 = 440.0; // -- A440 (pitch standard)

    /**
     *   A C   F H   K M O   R T   W Y a   d f   i k m   p r   u w y
     *    B D E G I J L N P Q S U V X Z b c e g h j l n o q s t v x z
     */
    public List<Tone> toTones(int baseTimeMs, String noteFormat)
    {
        List<Tone> result = new ArrayList<>(noteFormat.length() / 2);
        for(int i = 0; i < noteFormat.length(); i += 2)
        {
            char cPitch = noteFormat.charAt(i);
            int cLength = Integer.parseInt(Character.toString(noteFormat.charAt(i+1)));

            int frequency = getFrequency(cPitch);
            int length = baseTimeMs * cLength;

            int silenceLength = baseTimeMs / 10;
            result.add(new Tone(frequency, length - silenceLength));
            result.add(new Silence(silenceLength));
        }
        return result;
    }

    private int getFrequency(char cPitch)
    {
        if(cPitch == '_') {
            // -- add silence
            return 0;
        } else
        {
            double val = A440;
            int tune = (int) (cPitch - 'l'); // -- 'l' character means A note
            if (tune < -9)
            {
                tune += 6; // -- Skip characters between character capital 'Z' and 'a'.
            }
            if (tune > 0)
            {
                while (tune != 0)
                {
                    val *= TROT;
                    --tune;
                }
            }
            else
            {
                while (tune != 0)
                {
                    val /= TROT;
                    ++tune;
                }
            }
            return new Double(val).intValue();
        }
    }

    ///////////////////////////////////////////////////////////////////////

    public static class Tone
    {
        private int frequency;
        private int length;

        public Tone(int frequency, int length)
        {
            this.frequency = frequency;
            this.length = length;
        }

        public int getFrequency()
        {
            return frequency;
        }

        public int getLength()
        {
            return length;
        }
    }

    public static class Silence extends Tone
    {

        public Silence(int length)
        {
            super(0, length);
        }
    }
}
