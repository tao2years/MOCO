package IoTSystem.DeviceController;

import IoTSystem.DeviceTwin.LightTwin;
import VirtualDevice.Yeelight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LightController {
    private static final Logger LOGGER = LogManager.getLogger();
    String deviceId;
    Yeelight yeelight;
    LightTwin lightTwin;

    public LightController(String deviceId, Yeelight yeelight, LightTwin lightTwin) {
        this.deviceId = deviceId;
        this.yeelight = yeelight;
        this.lightTwin = lightTwin;
    }

    public void turnOn() {
        String result = yeelight.turnOn();
        if (result.equals("success"))
            lightTwin.setPowerOn(true);
    }

    public void turnOff() {
        String result = yeelight.turnOff();
        if (result.equals("success"))
            lightTwin.setPowerOn(false);
    }

    public void setBrightness(int brightness) {
        String result = yeelight.setBrightness(brightness);
        if (result.equals("success"))
            lightTwin.setBrightness(brightness);
    }

    public void setRGB(int r, int g, int b) {
        String result = yeelight.setRGB(r, g, b);
        if (result.equals("success"))
            lightTwin.setRgb(r,g,b);
    }



    public void printInternalState(){
        LOGGER.info("Device ID: " + deviceId);
        String yeelightString = yeelight.toString();
        LOGGER.info("Yeelight: " + yeelightString);
        String lightTwinString = lightTwin.toString();
        LOGGER.info("Light Twin: " + lightTwinString);
        boolean equal = yeelightString.replace("Yeelight","").equals(lightTwinString.replace("LightTwin",""));
        LOGGER.info("Equal: " + equal);
    }

    public static void main(String[] args) {
        Yeelight yeelight = new Yeelight(10, true, 255, 255, 255);
        LightTwin lightTwin = new LightTwin(10, true, 255, 255, 255);
        LightController lightController = new LightController("light1", yeelight, lightTwin);
        lightController.printInternalState();
        lightController.turnOff();
        LOGGER.info("[*] Turn off the light");
        lightController.printInternalState();
        lightController.turnOn();
        LOGGER.info("[*] Turn on the light");
        lightController.printInternalState();
//        lightController.turnOff();
//        LOGGER.info("[*] Turn off the light");
//        lightController.printInternalState();
        lightController.setBrightness(50);
        LOGGER.info("[*] Set brightness to 50");
        lightController.printInternalState();
        lightController.setRGB(120, 130, 111);
        LOGGER.info("[*] Set RGB to 120, 130, 111");
        lightController.printInternalState();
    }


}
