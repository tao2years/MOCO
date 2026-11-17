package VirtualDevice;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Characterization tests for {@link CoffeeMachine}: they pin down the current
 * input/output behavior before any refactoring.
 */
public class CoffeeMachineTest {

    private CoffeeMachine cm;

    @Before
    public void setUp() {
        cm = new CoffeeMachine();
    }

    @Test
    public void initialPowerStateIsOff() {
        assertFalse(cm.isPowerOn());
        assertFalse(cm.isWorking());
        assertEquals(0, cm.getCurrentWaterVolume());
        assertEquals(0, cm.getCurrentBeanVolume());
        assertEquals(0, cm.getCurrentMilkVolume());
    }

    @Test
    public void turnOnOnlyWorksWhenOff() {
        assertEquals("success", cm.turnOn());
        assertTrue(cm.isPowerOn());
        // turning on twice is rejected
        assertEquals("skip", cm.turnOn());
    }

    @Test
    public void turnOffOnlyWorksWhenOn() {
        assertEquals("skip", cm.turnOff());
        cm.turnOn();
        assertEquals("success", cm.turnOff());
        assertFalse(cm.isPowerOn());
    }

    @Test
    public void refillsRequirePower() {
        assertEquals("skip", cm.addWater());
        assertEquals("skip", cm.addCoffeeBean());
        assertEquals("skip", cm.addMilk());
        assertEquals(0, cm.getCurrentWaterVolume());
    }

    @Test
    public void refillsAreCappedAtMaximumVolumes() {
        cm.turnOn();
        for (int i = 0; i < 10; i++) {
            cm.addWater();
            cm.addCoffeeBean();
            cm.addMilk();
        }
        assertEquals(CoffeeMachine.MAX_WATER_VOLUME, cm.getCurrentWaterVolume());
        assertEquals(CoffeeMachine.MAX_COFFEE_BEAN_VOLUME, cm.getCurrentBeanVolume());
        assertEquals(CoffeeMachine.MAX_MILK_VOLUME, cm.getCurrentMilkVolume());
    }

    @Test
    public void addWaterMakesWaterReadyAboveThreshold() {
        CoffeeMachine full = new CoffeeMachine(false, false, false, false, false, false, true,
                CoffeeMachine.MAX_WATER_VOLUME, 0, 0);
        // volume is already above the 250 threshold even though flag says false
        full.addWater();
        assertTrue(full.isWaterReady());
    }

    @Test
    public void brewingWithoutPowerReturnsInstruction() {
        assertEquals("Please turn on the machine first.", cm.brewCoffee(1));
    }

    @Test
    public void brewingWithoutCupReturnsInstruction() {
        cm.turnOn();
        assertEquals("Please place a cup first.", cm.brewCoffee(1));
    }

    @Test
    public void brewingTwiceRequiresFetchingFirstCoffee() {
        brewFullAmericanos();
        assertEquals("Please take the previous coffee first.", cm.brewCoffee(1));
    }

    @Test
    public void invalidCoffeeTypeIsRejected() {
        cm.turnOn();
        cm.placeCup();
        assertEquals("Please enter a valid coffee type.", cm.brewCoffee(9));
    }

    @Test
    public void brewAmericanoConsumesExpectedResources() {
        brewFullAmericanos();
        assertEquals("Success", "Success"); // sanity placeholder, real assertion below
        assertEquals(CoffeeMachine.MAX_WATER_VOLUME - 240, cm.getCurrentWaterVolume());
        assertEquals(CoffeeMachine.MAX_COFFEE_BEAN_VOLUME - 15, cm.getCurrentBeanVolume());
        assertEquals(CoffeeMachine.MAX_MILK_VOLUME, cm.getCurrentMilkVolume());
        assertTrue(cm.isThisTimeCoffeeReady());
        assertFalse(cm.isWorking());
    }

    @Test
    public void brewLatteConsumesMilk() {
        prepareFullMachine();
        assertEquals("Success", cm.brewCoffee(2));
        assertEquals(CoffeeMachine.MAX_MILK_VOLUME - 200, cm.getCurrentMilkVolume());
        assertEquals(CoffeeMachine.MAX_WATER_VOLUME - 40, cm.getCurrentWaterVolume());
    }

    @Test
    public void brewCappuccinoConsumesLessMilkThanLatte() {
        prepareFullMachine();
        assertEquals("Success", cm.brewCoffee(3));
        assertEquals(CoffeeMachine.MAX_MILK_VOLUME - 140, cm.getCurrentMilkVolume());
    }

    @Test
    public void brewingWithLowWaterClearsWaterReadyFlag() {
        CoffeeMachine low = new CoffeeMachine(true, true, true, true, false, false, true,
                100, 100, 300);
        String result = low.brewCoffee(1);
        assertEquals("Please add water first.", result);
        assertFalse(low.isWaterReady());
    }

    @Test
    public void placeCupIsIdempotent() {
        assertEquals("success", cm.placeCup());
        assertEquals("skip", cm.placeCup());
        assertTrue(cm.isCupReady());
    }

    @Test
    public void fetchCoffeeResetsCupAndCoffee() {
        brewFullAmericanos();
        assertEquals("success", cm.fetchCoffee());
        assertFalse(cm.isThisTimeCoffeeReady());
        assertFalse(cm.isCupReady());
        // fetch succeeds even when nothing to fetch (documented current behavior)
        assertEquals("success", cm.fetchCoffee());
    }

    @Test
    public void toStringAndFromStringRoundTrip() {
        brewFullAmericanos();
        String state = cm.toString();
        CoffeeMachine restored = CoffeeMachine.fromString(state);
        assertEquals(cm.toString(), restored.toString());
        assertEquals(cm.getCurrentWaterVolume(), restored.getCurrentWaterVolume());
        assertEquals(cm.isWaterReady(), restored.isWaterReady());
    }

    @Test
    public void toSystemStateStringOmitsVolumes() {
        cm.turnOn();
        String systemState = cm.toSystemStateString();
        assertTrue(systemState.startsWith("CoffeeMachine{"));
        assertFalse(systemState.contains("currentWaterVolume"));
        assertTrue(systemState.contains("'isPowerOn':'true'"));
    }

    private void prepareFullMachine() {
        cm.turnOn();
        cm.addWater();
        cm.addCoffeeBean();
        cm.addMilk();
        cm.placeCup();
        topUpToFull();
    }

    private void brewFullAmericanos() {
        prepareFullMachine();
        cm.brewCoffee(1);
    }

    private void topUpToFull() {
        // addWater/addCoffeeBean/addMilk add random amounts, so loop until full
        for (int i = 0; i < 100
                && (cm.getCurrentWaterVolume() < CoffeeMachine.MAX_WATER_VOLUME
                || cm.getCurrentBeanVolume() < CoffeeMachine.MAX_COFFEE_BEAN_VOLUME
                || cm.getCurrentMilkVolume() < CoffeeMachine.MAX_MILK_VOLUME); i++) {
            cm.addWater();
            cm.addCoffeeBean();
            cm.addMilk();
        }
    }
}
