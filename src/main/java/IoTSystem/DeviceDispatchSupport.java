package IoTSystem;

import IoTSystem.DeviceController.CMController;
import IoTSystem.DeviceController.GatewayController;
import IoTSystem.DeviceController.LightController;
import IoTSystem.DeviceController.VCController;
import IoTSystem.DeviceController.WMController;

public class DeviceDispatchSupport {
    public static final String COFFEE_MACHINE = "CoffeeMachine";
    public static final String GATEWAY = "Gateway";
    public static final String YEELIGHT = "Yeelight";
    public static final String VIDEO_CAMERA = "VideoCamera";
    public static final String WASHING_MACHINE = "WashingMachine";

    private final CMController cmController;
    private final GatewayController gatewayController;
    private final LightController lightController;
    private final VCController vcController;
    private final WMController wmController;

    public DeviceDispatchSupport(CMController cmController, GatewayController gatewayController,
                                 LightController lightController, VCController vcController,
                                 WMController wmController) {
        this.cmController = cmController;
        this.gatewayController = gatewayController;
        this.lightController = lightController;
        this.vcController = vcController;
        this.wmController = wmController;
    }

    public Object getController(String deviceType) {
        switch (deviceType) {
            case COFFEE_MACHINE:
                return cmController;
            case GATEWAY:
                return gatewayController;
            case YEELIGHT:
                return lightController;
            case VIDEO_CAMERA:
                return vcController;
            case WASHING_MACHINE:
                return wmController;
            default:
                return null;
        }
    }

    public String getPreExecutionState(String deviceType) {
        switch (deviceType) {
            case COFFEE_MACHINE:
                return cmController.getCmTwin().toSystemStateString().replace("CMTwin", "CoffeeMachine");
            case GATEWAY:
                return gatewayController.getGatewayTwin().toSystemString().replace("GatewayTwin", "Gateway");
            case YEELIGHT:
                return lightController.getLightTwin().toSystemString().replace("LightTwin", "Yeelight");
            case VIDEO_CAMERA:
                return vcController.getVcTwin().toSystemString().replace("VCTwin", "VideoCamera");
            case WASHING_MACHINE:
                return wmController.getWmTwin().toString().replace("WMTwin", "WashingMachine");
            default:
                return "Invalid";
        }
    }

    public String getCurrentState(String deviceType) {
        switch (deviceType) {
            case COFFEE_MACHINE:
                return cmController.getCmTwin().toSystemDeviceString();
            case GATEWAY:
                return gatewayController.getGatewayTwin().toSystemDeviceString();
            case YEELIGHT:
                return lightController.getLightTwin().toSystemDeviceString();
            case VIDEO_CAMERA:
                return vcController.getVcTwin().toSystemDeviceString();
            case WASHING_MACHINE:
                return wmController.getWmTwin().toDeviceString();
            default:
                return "Invalid";
        }
    }

    public void printInternalState(String deviceType) {
        switch (deviceType) {
            case COFFEE_MACHINE:
                cmController.printInternalState();
                break;
            case GATEWAY:
                gatewayController.printInternalState();
                break;
            case YEELIGHT:
                lightController.printInternalState();
                break;
            case VIDEO_CAMERA:
                vcController.printInternalState();
                break;
            case WASHING_MACHINE:
                wmController.printInternalState();
                break;
            default:
                break;
        }
    }
}
