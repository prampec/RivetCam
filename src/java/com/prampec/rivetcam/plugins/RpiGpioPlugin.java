package com.prampec.rivetcam.plugins;

import java.io.File;
import java.util.*;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.prampec.rivetcam.AppController;
import com.prampec.rivetcam.RivetCamPlugin;
import com.prampec.util.PropertiesHelper;

/**
 * Use Raspberry Pi (or compatible) GPIOs as inputs.
 */
public class RpiGpioPlugin implements RivetCamPlugin
{
    private static final String ACTION_SNAPSHOT = "snapshot";
    private static final String ACTION_REMOVE_LAST_IMAGE =
        "removeLastImage";
    private static final String ACTION_PLAYBACK = "playback";
    private static final String ACTION_NEW_BATCH = "newBatch";
    private static final Boolean ROTARY_STATE_EVENT_OCCURRED = true;
    private static final Boolean ROTARY_STATE_EVENT_CLEARED = false;

    private final AppController appController;
    private GpioController gpio;
    private Map<String, Boolean> rotaryStates =
        new HashMap<>();

    public enum RotaryEvent
    {
        CW, CCW, Push, PushRelease
    }

    public RpiGpioPlugin(
        Properties properties, AppController appController)
    {
        this.appController = appController;

        connectPins(properties);
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
                System.out.println(
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
                System.out.println(
                    "Rotary " + pinName + " (" +
                        raspiPinA.toString() + ", " +
                        raspiPinB.toString() + ", " +
                        raspiPinPush.toString() + ") connected to " + action);
            }
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
//                    System.out.println("> Pin " + event.getPin() + " " + event.getEdge() + " while " + inputB.getName() + " is " + inputB.getState());
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
//                System.out.println("> Pin " + event.getPin() + " " + event.getEdge() + " while " + inputA.getName() + " is " + inputA.getState());
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
        //        System.out.println(
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

    private void handlePinEvent(
        GpioPin pin, PinState state, PinEdge edge, String action)
    {
//        System.out.println("> Pin " + pin + ": " + edge);
        if (state.isLow() == pin.isPullResistance(
            PinPullResistance.PULL_DOWN))
        {
            return; // Pin is in default state.
        }
//        System.out.println("> activating action: " + action);
        // -- TODO: move action definitions to appController
        if (ACTION_SNAPSHOT.equals(action))
        {
            appController.snapshot();
        }
        else if (ACTION_REMOVE_LAST_IMAGE.equals(action))
        {
            appController.removeLastImage();
        }
        else if (ACTION_PLAYBACK.equals(action))
        {
            appController.playbackMode();
        }
        else if (ACTION_NEW_BATCH.equals(action))
        {
            appController.createNewBatch();
        }
        else
        {
            throw new IllegalStateException("Unknown action '" + action + "'");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("<--Pi4J--> GPIO Listen Example ... started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(
            RaspiPin.GPIO_25, PinPullResistance.PULL_UP);

        // set shutdown state for this input pin
        myButton.setShutdownOptions(true);

        // create and register gpio pin listener
        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(
                GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getEdge());
            }
        });

        System.out.println(" ... complete the GPIO #02 circuit and see the listener feedback here in the console.");

        // keep program running until user aborts (CTRL-C)
        while(true) {
            Thread.sleep(500);
        }

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}
