package IoTSystem.DeviceController;

import IoTSystem.DeviceTwin.CMTwin;
import VirtualDevice.CoffeeMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CMController extends AbstractDeviceController<CoffeeMachine, CMTwin> {
    private static final Logger LOGGER = LogManager.getLogger();

    public CMController(String deviceId, CoffeeMachine coffeeMachine, CMTwin cmTwin) {
        super(deviceId, coffeeMachine, cmTwin, CoffeeMachine.class, CMTwin.class);
    }

    public CoffeeMachine getCoffeeMachine() {
        return getDevice();
    }

    public void setCoffeeMachine(CoffeeMachine coffeeMachine) {
        setDevice(coffeeMachine);
    }

    public CMTwin getCmTwin() {
        return getTwin();
    }

    public void setCmTwin(CMTwin cmTwin) {
        setTwin(cmTwin);
    }

    public void turnOn() {
        if (getCoffeeMachine().turnOn().equals("success"))
            getCmTwin().setPowerOn(true);
    }

    public void turnOff() {
        if (getCoffeeMachine().turnOff().equals("success"))
            getCmTwin().setPowerOn(false);
    }

    public void addCoffeeBean() {
        if (getCoffeeMachine().addCoffeeBean().equals("success")) {
            getCmTwin().setCurrentBeanVolume(getCoffeeMachine().getCurrentBeanVolume());
            getCmTwin().setBeanReady(getCoffeeMachine().isBeanReady());
        }
    }

    public void addWater() {
        if (getCoffeeMachine().addWater().equals("success")) {
            getCmTwin().setCurrentWaterVolume(getCoffeeMachine().getCurrentWaterVolume());
            getCmTwin().setWaterReady(getCoffeeMachine().isWaterReady());
        }
    }

    public void addMilk() {
        if (getCoffeeMachine().addMilk().equals("success")) {
            getCmTwin().setCurrentMilkVolume(getCoffeeMachine().getCurrentMilkVolume());
            getCmTwin().setMilkReady(getCoffeeMachine().isMilkReady());
        }
    }

    public void placeCup() {
        if (getCoffeeMachine().placeCup().equals("success"))
            getCmTwin().setCupReady(true);
    }

    public void fetchCoffee() {
        if (getCoffeeMachine().fetchCoffee().equals("success")) {
            getCmTwin().setCupReady(false);
            getCmTwin().setThisTimeCoffeeReady(false);
        }
    }

    public void brewCoffee(int type) {
        getCoffeeMachine().brewCoffee(type);
        getCmTwin().updateAllFromString(getCoffeeMachine().toString());
    }

    public static void main(String[] args) {
        CMController cmController =
                new CMController("CM001", new CoffeeMachine(), new CMTwin());
        cmController.turnOn();
        cmController.addCoffeeBean();
        cmController.addWater();
        cmController.addMilk();
        cmController.placeCup();
        cmController.brewCoffee(1);
        cmController.printInternalState();
        cmController.fetchCoffee();
        cmController.turnOff();
        cmController.printInternalState();
    }
}
