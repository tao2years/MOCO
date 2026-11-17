package VirtualDevice;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the Yeelight virtual device, the deterministic device
 * model whose full behavior space is pinned by the golden files validated in
 * {@code MOCO.YeelightModelRegressionTest}. These tests keep the device
 * semantics the learner relies on stable: state transitions, guards and the
 * string round-trip used to serialize states between exploration steps.
 */
public class YeelightTest {

    @Test
    public void initialStateIsOffWithUnsetValues() {
        Yeelight light = new Yeelight();
        assertFalse(light.toString().contains("'powerOn':true"));
        assertTrue(light.toString().contains("'brightness':-1"));
        assertTrue(light.toString().contains("-1,-1,-1"));
    }

    @Test
    public void turnOnOnlyWorksWhenOff() {
        Yeelight light = new Yeelight();
        assertEquals("success", light.turnOn());
        assertEquals("skip", light.turnOn());
    }

    @Test
    public void turnOffOnlyWorksWhenOn() {
        Yeelight light = new Yeelight();
        assertEquals("skip", light.turnOff());
        assertEquals("success", light.turnOn());
        assertEquals("success", light.turnOff());
    }

    @Test
    public void brightnessAndRgbRequirePowerOn() {
        Yeelight light = new Yeelight();
        assertEquals("skip", light.setBrightness(50));
        assertEquals("skip", light.setRGB(1, 2, 3));
        light.turnOn();
        assertEquals("success", light.setBrightness(50));
        assertEquals("success", light.setRGB(1, 2, 3));
    }

    @Test
    public void brightnessAndRgbBounds() {
        Yeelight light = new Yeelight();
        light.turnOn();
        assertEquals("skip", light.setBrightness(101));
        assertEquals("skip", light.setBrightness(-1));
        assertEquals("success", light.setBrightness(100));
        assertEquals("skip", light.setRGB(256, 0, 0));
        assertEquals("success", light.setRGB(255, 255, 255));
    }

    @Test
    public void colorModeRequiresPowerOnAndRange() {
        Yeelight light = new Yeelight();
        assertEquals("skip", light.setColorMode(1));
        light.turnOn();
        assertEquals("success", light.setColorMode(1));
        assertEquals("skip", light.setColorMode(0));
        assertEquals("skip", light.setColorMode(4));
        assertEquals("success", light.setColorMode(3));
    }

    @Test
    public void stateStringRoundTrips() {
        Yeelight light = new Yeelight(50, true, 10, 20, 30);
        Yeelight copy = Yeelight.fromString(light.toString());
        assertEquals(light.toString(), copy.toString());
        assertEquals("success", copy.turnOff());
    }

    @Test
    public void systemStateSummarizesValidRanges() {
        Yeelight off = new Yeelight();
        assertTrue(off.toSystemString().contains("'powerOn':false"));
        assertTrue(off.toSystemString().contains("'brightness':false"));
        assertTrue(off.toSystemString().contains("'rgb':false"));

        Yeelight on = new Yeelight(50, true, 10, 20, 30);
        assertTrue(on.toSystemString().contains("'powerOn':true"));
        assertTrue(on.toSystemString().contains("'brightness':true"));
        assertTrue(on.toSystemString().contains("'rgb':true"));
    }
}
