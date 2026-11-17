package IoTSystem.DeviceController;

import IoTSystem.DeviceTwin.GatewayTwin;
import VirtualDevice.Gateway;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GatewayController extends AbstractDeviceController<Gateway, GatewayTwin> {
    private static final Logger LOGGER = LogManager.getLogger();

    public GatewayController(String deviceId, Gateway gateway, GatewayTwin gatewayTwin) {
        super(deviceId, gateway, gatewayTwin, Gateway.class, GatewayTwin.class);
    }

    public Gateway getGateway() {
        return getDevice();
    }

    public void setGateway(Gateway gateway) {
        setDevice(gateway);
    }

    public GatewayTwin getGatewayTwin() {
        return getTwin();
    }

    public void setGatewayTwin(GatewayTwin gatewayTwin) {
        setTwin(gatewayTwin);
    }

    public void turnLightOn() {
        if (getGateway().turnLightOn().equals("success"))
            getGatewayTwin().setLightOn(true);
    }

    public void turnLightOff() {
        if (getGateway().turnLightOff().equals("success"))
            getGatewayTwin().setLightOn(false);
    }

    public void setLightBrightness(int brightness) {
        if (getGateway().setLightBrightness(brightness).equals("success"))
            getGatewayTwin().setLightBrightness(brightness);
    }

    public void turnAlarmOn() {
        if (getGateway().turnAlarmOn().equals("success"))
            getGatewayTwin().setAlarmOn(true);
    }

    public void turnAlarmOff() {
        if (getGateway().turnAlarmOff().equals("success"))
            getGatewayTwin().setAlarmOn(false);
    }

    public void addDevice(String device) {
        if (getGateway().addDevice(device).equals("success"))
            getGatewayTwin().setDeviceList(getGatewayTwin().getDeviceList() + 1);
    }

    public void removeDevice(String device) {
        if (getGateway().removeDevice(device).equals("success"))
            getGatewayTwin().setDeviceList(getGatewayTwin().getDeviceList() - 1);
    }

    public static void main(String[] args) {
        GatewayController controller = new GatewayController("gateway1",
                new Gateway(), new GatewayTwin());
        controller.turnLightOn();
        controller.setLightBrightness(50);
        controller.turnAlarmOn();
        controller.addDevice("device1");
        controller.addDevice("device2");
        controller.removeDevice("device1");
        controller.turnAlarmOff();
        controller.turnLightOff();
        controller.printInternalState();
    }
}
