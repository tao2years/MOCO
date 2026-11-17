package IoTSystem;

import IoTSystem.DeviceTwin.CMTwin;
import IoTSystem.DeviceTwin.WMTwin;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Characterization tests for the IoTSystem plumbing: Message, MessageQueue,
 * DelayedMessage and DeviceTwin state strings.
 */
public class IoTSystemTest {

    private MessageQueue queue;

    @Before
    public void setUp() {
        queue = new MessageQueue();
    }

    // ---------- Message ----------

    @Test
    public void messageHoldsDeviceApiAndArgs() {
        Message msg = new Message("Yeelight", "setRGB", new String[]{"120", "130", "111"});
        assertEquals("Yeelight", msg.getDeviceType());
        assertEquals("setRGB", msg.getDeviceAPI());
        assertArrayEquals(new String[]{"120", "130", "111"}, msg.getDeviceAPIArgs());
    }

    @Test
    public void messageToStringFormat() {
        Message msg = new Message("Gateway", "addDevice", new String[]{"device1"});
        assertEquals("Message{'deviceType':'Gateway', 'deviceAPI':'addDevice',"
                + " 'deviceAPIArgs':'[device1]'}", msg.toString());
    }

    // ---------- MessageQueue ----------

    @Test
    public void messageQueueIsFifo() throws InterruptedException {
        Message first = new Message("Gateway", "turnLightOn", new String[]{});
        Message second = new Message("Gateway", "turnLightOff", new String[]{});
        queue.addMessage(first);
        queue.addMessage(second);
        assertSame(first, queue.getMessage());
        assertSame(second, queue.getMessage());
    }

    @Test
    public void historyRecordsAreKeptInOrder() {
        queue.addHistory("a");
        queue.addHistory("b");
        List<String> history = queue.getHistory();
        assertEquals(2, history.size());
        assertEquals("a", history.get(0));
        // returned history is a defensive copy
        history.add("c");
        assertEquals(2, queue.getHistory().size());
    }

    // ---------- DelayedMessage ----------

    @Test
    public void delayedMessageExposesOriginalMessage() {
        Message msg = new Message("WashingMachine", "stop", new String[]{});
        DelayedMessage delayed = new DelayedMessage(msg, 100);
        assertSame(msg, delayed.getMessage());
        assertTrue(delayed.getDelay(java.util.concurrent.TimeUnit.MILLISECONDS) > 0);
        assertTrue(delayed.getDelay(java.util.concurrent.TimeUnit.MILLISECONDS) <= 100);
    }

    @Test
    public void delayedMessageOrdersByStartTime() {
        Message msg = new Message("Gateway", "turnLightOn", new String[]{});
        DelayedMessage early = new DelayedMessage(msg, 0);
        DelayedMessage late = new DelayedMessage(msg, 5000);
        assertTrue(early.compareTo(late) < 0);
        assertEquals(0, early.compareTo(early));
    }

    // ---------- Twin state strings ----------

    @Test
    public void cmTwinDefaultsAndSystemString() {
        CMTwin twin = new CMTwin();
        assertFalse(twin.isPowerOn());
        assertEquals("CMTwin{'waterReady':'false', 'beanReady':'false', 'milkReady':'false',"
                + " 'cupReady':'false', 'thisTimeCoffeeReady':'false',"
                + " 'isWorking':'false', 'isPowerOn':'false'}", twin.toSystemStateString());
        // device-view string uses the physical device name
        assertTrue(twin.toSystemDeviceString().startsWith("CoffeeMachine{"));
    }

    @Test
    public void wmTwinDefaultsAndDeviceString() {
        WMTwin twin = new WMTwin();
        assertFalse(twin.isPowerOn());
        assertEquals("WashingMachine{'powerOn':false, 'doorOpen':false, 'waterIn':false,"
                        + " 'washing':false, 'rinsing':false, 'spinning':false}",
                twin.toDeviceString());
    }

    @Test
    public void wmTwinSetTargetStateParsesDeviceString() {
        WMTwin twin = new WMTwin();
        twin.setTargetState("WashingMachine{'powerOn':true, 'doorOpen':true, 'waterIn':true,"
                + " 'washing':true, 'rinsing':true, 'spinning':true}");
        assertTrue(twin.isPowerOn());
        assertTrue(twin.isDoorOpen());
        assertTrue(twin.isWashing());
    }

    @Test
    public void cmTwinFromStringRoundTrip() {
        CMTwin twin = new CMTwin();
        twin.setPowerOn(true);
        twin.setWaterReady(true);
        twin.setCurrentWaterVolume(500);
        CMTwin restored = CMTwin.fromString(twin.toString());
        assertEquals(twin, restored);
    }
}
