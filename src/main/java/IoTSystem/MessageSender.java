package IoTSystem;

import MOCO.MessageProxy;

import static IoTSystem.TaskScheduler.sleep;

/**
 * Sends scripted message sequences through the MOCA proxy.
 * Each helper sends one message, waits for it to settle when it was accepted
 * (keeping the pacing of the original per-message 100 ms sleeps), and returns
 * whether the proxy accepted the last message.
 */
public class MessageSender {

    private MessageSender() {
    }

    /** Sends one message; sleeps 100 ms when the proxy accepted it. */
    private static void send(MessageProxy messageProxy, Message message) {
        String currentState = Main.getCurrentStateBasedOnMsg(message);
        boolean accepted = messageProxy.addMessage(message, currentState);
        if (accepted) {
            sleep(100);
        }
    }

    private static void send(MessageProxy messageProxy, Message message, Message stateSource) {
        String currentState = Main.getCurrentStateBasedOnMsg(stateSource);
        boolean accepted = messageProxy.addMessage(message, currentState);
        if (accepted) {
            sleep(100);
        }
    }

    private static void send(MessageProxy messageProxy, String deviceType, String api, String... args) {
        send(messageProxy, new Message(deviceType, api, args));
    }

    public static void sendMessageYeelight(MessageProxy messageProxy) {
        send(messageProxy, "Yeelight", "turnOn");
        send(messageProxy, "Yeelight", "turnOff");
        // original code read the state from the first message here
        send(messageProxy, new Message("Yeelight", "turnOn", new String[]{}),
                new Message("Yeelight", "turnOn", new String[]{}));
        send(messageProxy, "Yeelight", "setBrightness", "50");
        send(messageProxy, "Yeelight", "setRGB", "120", "130", "111");
    }

    public static void sendMessageVideoCamera(MessageProxy messageProxy) {
        String device = "VideoCamera";
        send(messageProxy, device, "turnOn");
        send(messageProxy, device, "turnOnLight");
        send(messageProxy, device, "turnOnFullColor");
        send(messageProxy, device, "turnOnFlip");
        send(messageProxy, device, "turnOnImproveProgram");
        send(messageProxy, device, "turnOnWdr");
        send(messageProxy, device, "turnOnTrack");
        send(messageProxy, device, "turnOnTrack");
        send(messageProxy, device, "turnOff");
        send(messageProxy, device, "turnOffTrack");
    }

    public static void sendMessageGateway(MessageProxy messageProxy) {
        String device = "Gateway";
        send(messageProxy, device, "turnLightOn");
        send(messageProxy, device, "turnLightOff");
        send(messageProxy, device, "turnLightOn");
        send(messageProxy, device, "setLightBrightness", "50");
        send(messageProxy, device, "turnAlarmOn");
        send(messageProxy, device, "turnAlarmOff");
        send(messageProxy, device, "addDevice", "device1");
        send(messageProxy, device, "removeDevice", "device1");
        // original code read the state from the first message here
        send(messageProxy, new Message(device, "turnLightOn", new String[]{}),
                new Message(device, "turnLightOn", new String[]{}));
        send(messageProxy, device, "addDevice", "device1");
    }

    public static void sendMessageWashingMachine(MessageProxy messageProxy) {
        String device = "WashingMachine";
        send(messageProxy, device, "turnOn");
        send(messageProxy, device, "openDoor");
        send(messageProxy, device, "closeDoor");
        send(messageProxy, device, "fillWater");
        send(messageProxy, device, "startWashing");
        send(messageProxy, device, "startRinsing");
        send(messageProxy, device, "startSpinning");
        send(messageProxy, device, "stop");
        send(messageProxy, device, "turnOff");
        send(messageProxy, device, "stop");
    }

    public static void sendMessageCoffeeMachine(MessageProxy messageProxy) {
        String device = "CoffeeMachine";
        send(messageProxy, device, "turnOn");
        send(messageProxy, device, "turnOff");
        send(messageProxy, device, "addWater");
        send(messageProxy, device, "addCoffeeBean");
        send(messageProxy, device, "addMilk");
        send(messageProxy, device, "placeCup");
        send(messageProxy, device, "fetchCoffee");
        for (int i = 1; i <= 3; i++) {
            send(messageProxy, device, "brewCoffee", String.valueOf(i));
        }
    }
}
