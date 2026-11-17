package IoTSystem.DeviceController;

import IoTSystem.DeviceTwin.WMTwin;
import VirtualDevice.WashingMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WMController extends AbstractDeviceController<WashingMachine, WMTwin> {

    public WMController(String deviceId, WashingMachine wm, WMTwin wmTwin) {
        super(deviceId, wm, wmTwin, WashingMachine.class, WMTwin.class);
    }

    public WashingMachine getWm() {
        return getDevice();
    }

    public void setWm(WashingMachine wm) {
        setDevice(wm);
    }

    public WMTwin getWmTwin() {
        return getTwin();
    }

    public void setWmTwin(WMTwin wmTwin) {
        setTwin(wmTwin);
    }

    public void turnOn() {
        if (getWm().turnOn().equals("success"))
            getWmTwin().setPowerOn(true);
    }

    public void turnOff() {
        if (getWm().turnOff().equals("success"))
            getWmTwin().setPowerOn(false);
    }

    public void openDoor() {
        if (getWm().openDoor().equals("success"))
            getWmTwin().setDoorOpen(true);
    }

    public void closeDoor() {
        if (getWm().closeDoor().equals("success"))
            getWmTwin().setDoorOpen(false);
    }

    public void fillWater() {
        if (getWm().fillWater().equals("success"))
            getWmTwin().setWaterIn(true);
    }

    public void startWashing() {
        if (getWm().startWashing().equals("success"))
            getWmTwin().setWashing(true);
    }

    public void startRinsing() {
        if (getWm().startRinsing().equals("success"))
            getWmTwin().setRinsing(true);
    }

    public void startSpinning() {
        if (getWm().startSpinning().equals("success"))
            getWmTwin().setSpinning(true);
    }

    public void stop() {
        if (getWm().stop().equals("success")) {
            getWmTwin().setWashing(false);
            getWmTwin().setRinsing(false);
            getWmTwin().setSpinning(false);
        }
    }

    public static void main(String[] args) {
        WMController wmController =
                new WMController("wm1", new WashingMachine(), new WMTwin());
        wmController.printInternalState();
        wmController.turnOn();
        wmController.openDoor();
        wmController.closeDoor();
        wmController.fillWater();
        wmController.startWashing();
        wmController.startRinsing();
        wmController.startSpinning();
        wmController.stop();
        wmController.printInternalState();
    }
}
