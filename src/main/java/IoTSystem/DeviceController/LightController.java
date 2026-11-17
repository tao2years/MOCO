package IoTSystem.DeviceController;

import IoTSystem.DeviceTwin.LightTwin;
import VirtualDevice.Yeelight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LightController extends AbstractDeviceController<Yeelight, LightTwin> {
    private static final Logger LOGGER = LogManager.getLogger();

    public LightController(String deviceId, Yeelight yeelight, LightTwin lightTwin) {
        super(deviceId, yeelight, lightTwin, Yeelight.class, LightTwin.class);
    }

    public Yeelight getYeelight() {
        return getDevice();
    }

    public void setYeelight(Yeelight yeelight) {
        setDevice(yeelight);
    }

    public LightTwin getLightTwin() {
        return getTwin();
    }

    public void setLightTwin(LightTwin lightTwin) {
        setTwin(lightTwin);
    }

    public void turnOn() {
        if (getYeelight().turnOn().equals("success"))
            getLightTwin().setPowerOn(true);
    }

    public void turnOff() {
        if (getYeelight().turnOff().equals("success"))
            getLightTwin().setPowerOn(false);
    }

    public void setBrightness(int brightness) {
        if (getYeelight().setBrightness(brightness).equals("success"))
            getLightTwin().setBrightness(brightness);
    }

    public void setRGB(int r, int g, int b) {
        if (getYeelight().setRGB(r, g, b).equals("success"))
            getLightTwin().setRgb(r, g, b);
    }

    public static void main(String[] args) {
        LightController lightController = new LightController("light1",
                new Yeelight(10, true, 255, 255, 255),
                new LightTwin(10, true, 255, 255, 255));
        lightController.printInternalState();
        lightController.turnOff();
        lightController.turnOn();
        lightController.setBrightness(50);
        lightController.setRGB(120, 130, 111);
        lightController.printInternalState();
    }
}
