package VirtualDevice;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Characterization tests for the simple virtual devices (WashingMachine,
 * Gateway, Yeelight, SmartPlug) covering their input/output contracts.
 */
public class VirtualDeviceTest {

    private WashingMachine wm;

    @Before
    public void setUp() {
        wm = new WashingMachine();
    }

    // ---------- WashingMachine ----------

    @Test
    public void washingMachineInitialIsAllOff() {
        assertEquals("WashingMachine{'powerOn':false, 'doorOpen':false, 'waterIn':false,"
                + " 'washing':false, 'rinsing':false, 'spinning':false}", wm.toString());
    }

    @Test
    public void washingMachinePowerToggleIsIdempotent() {
        assertEquals("success", wm.turnOn());
        assertEquals("skip", wm.turnOn());
        assertEquals("success", wm.turnOff());
        assertEquals("skip", wm.turnOff());
    }

    @Test
    public void fillWaterRequiresPower() {
        assertEquals("skip", wm.fillWater());
        wm.turnOn();
        assertEquals("success", wm.fillWater());
        assertEquals("skip", wm.fillWater());
    }

    @Test
    public void doorCanBeOpenedAndClosedFreely() {
        assertEquals("success", wm.openDoor());
        assertEquals("skip", wm.openDoor());
        assertEquals("success", wm.closeDoor());
        assertEquals("skip", wm.closeDoor());
    }

    @Test
    public void washCycleRequiresPowerWaterAndClosedDoor() {
        assertEquals("skip", wm.startWashing());
        wm.turnOn();
        wm.fillWater();
        wm.openDoor();
        assertEquals("skip", wm.startWashing());
        wm.closeDoor();
        assertEquals("success", wm.startWashing());
        assertEquals("skip", wm.startWashing());
    }

    @Test
    public void fullWashCycleThenStop() {
        wm.turnOn();
        wm.fillWater();
        assertEquals("success", wm.startWashing());
        assertEquals("success", wm.startRinsing());
        assertEquals("success", wm.startSpinning());
        assertEquals("skip", wm.startSpinning());
        assertEquals("success", wm.stop());
        assertEquals("skip", wm.stop());
        // after stop, washing can start again only if water still in
        assertEquals("success", wm.startWashing());
    }

    @Test
    public void washingMachineFromStringRoundTrip() {
        wm.turnOn();
        wm.fillWater();
        wm.startWashing();
        WashingMachine restored = WashingMachine.fromString(wm.toString());
        assertEquals(wm.toString(), restored.toString());
    }

    // ---------- Gateway ----------

    @Test
    public void gatewayLightToggleAndBrightness() {
        Gateway gw = new Gateway();
        assertEquals("skip", gw.setLightBrightness(50));
        assertEquals("success", gw.turnLightOn());
        assertEquals("skip", gw.turnLightOn());
        assertEquals("success", gw.setLightBrightness(50));
        assertEquals("skip", gw.setLightBrightness(101));
        assertEquals("success", gw.turnLightOff());
        assertEquals("skip", gw.turnLightOff());
    }

    @Test
    public void gatewayAlarmToggle() {
        Gateway gw = new Gateway();
        assertEquals("success", gw.turnAlarmOn());
        assertEquals("skip", gw.turnAlarmOn());
        assertEquals("success", gw.turnAlarmOff());
    }

    @Test
    public void gatewayDeviceListIsBoundedAtThree() {
        Gateway gw = new Gateway();
        assertEquals("success", gw.addDevice("d1"));
        assertEquals("success", gw.addDevice("d2"));
        assertEquals("success", gw.addDevice("d3"));
        assertEquals("skip", gw.addDevice("d4"));
        assertEquals(3, gw.getDeviceList());
        // removeDevice only checks the counter, not membership
        assertEquals("success", gw.removeDevice("d1"));
        assertEquals(2, gw.getDeviceList());
        assertEquals("success", gw.removeDevice("anything"));
        assertEquals(1, gw.getDeviceList());
        gw.removeDevice("x");
        assertEquals("skip", gw.removeDevice("y"));
        assertEquals(0, gw.getDeviceList());
    }

    @Test
    public void gatewayConstructorLeavesDeviceListUninitialized() {
        // Documented current behavior: the no-arg constructor never assigns
        // deviceList, so it defaults to 0 via field initialization.
        Gateway gw = new Gateway();
        assertEquals(0, gw.getDeviceList());
    }

    @Test
    public void gatewayDeviceListValueIsReflectedInToString() {
        Gateway gw = new Gateway();
        assertTrue(gw.toString().contains("'deviceList':0"));
        gw.addDevice("d");
        assertTrue(gw.toString().contains("'deviceList':1"));
    }

    @Test
    public void gatewayFromStringRoundTrip() {
        Gateway gw = new Gateway(true, 50, true, 2);
        Gateway restored = Gateway.fromString(gw.toString());
        assertEquals(gw.toString(), restored.toString());
    }

    // ---------- Yeelight ----------

    @Test
    public void yeelightDefaultsToOff() {
        Yeelight y = new Yeelight();
        assertEquals("Yeelight{'brightness':-1, 'powerOn':false, 'rgb':'-1,-1,-1'}", y.toString());
        // brightness 0 is currently accepted (only bounds are checked)
        assertEquals("skip", y.setRGB(1, 2, 3));
    }

    @Test
    public void yeelightOnOffAndRanges() {
        Yeelight y = new Yeelight();
        assertEquals("success", y.turnOn());
        assertEquals("skip", y.turnOn());
        assertEquals("success", y.setBrightness(50));
        // brightness 0 is currently accepted (only bounds are checked)
        assertEquals("success", y.setBrightness(0));
        assertEquals("skip", y.setBrightness(101));
        assertEquals("success", y.setRGB(255, 255, 255));
        assertEquals("skip", y.setRGB(256, 0, 0));
        assertEquals("success", y.turnOff());
    }

    @Test
    public void yeelightFromStringRoundTrip() {
        Yeelight y = new Yeelight(50, true, 255, 255, 255);
        Yeelight restored = Yeelight.fromString(y.toString());
        assertEquals(y.toString(), restored.toString());
    }

    // ---------- SmartPlug ----------

    @Test
    public void smartPlugPowerAndTimer() {
        SmartPlug plug = new SmartPlug();
        assertEquals("success", plug.turnOn());
        assertEquals("skip", plug.turnOn());
        assertEquals("success", plug.setTimer(10));
        assertEquals("skip", plug.setTimer(-1));
        assertEquals("success", plug.turnOff());
        assertEquals("SmartPlug{'powerOn':false, 'timer':10}", plug.toString());
    }

    @Test
    public void smartPlugToSystemStringFlagsTimer() {
        SmartPlug plug = new SmartPlug(false, 30);
        assertEquals("SmartPlug{'powerOn':false, 'timer':true}", plug.toSystemString());
        SmartPlug zero = new SmartPlug(true, 0);
        assertEquals("SmartPlug{'powerOn':true, 'timer':false}", zero.toSystemString());
    }
}
