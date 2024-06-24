package IoTSystem;

import IoTSystem.DeviceController.*;
import IoTSystem.DeviceTwin.*;
import MOCO.MessageProxy;
import MOCO.Utils;
import VirtualDevice.*;
import graph.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;

import static IoTSystem.TaskScheduler.sleep;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    // Device, Twin, Controller
    private static CoffeeMachine coffeeMachine;
    private static CMTwin cmTwin;
    private static CMController cmController;

    private static Gateway gateway;
    private static GatewayTwin gatewayTwin;
    private static GatewayController transGatewayController;

    private static Yeelight yeelight;
    private static LightTwin lightTwin;
    private static LightController lightController;

    private static VideoCamera vc;
    private static VCTwin vcTwin;
    private static VCController vcController;

    private static WashingMachine wm;
    private static WMTwin wmTwin;
    private static WMController wmController;

    // TaskScheduler
    private static TaskScheduler taskScheduler;

    // device behavior models
    private static String gatewayFile = "src/main/java/MOCO/modelFiles/gateway.json";
    private static Graph<Object, Object> gateway_behaviorGraph;
    private static String lightFile = "src/main/java/MOCO/modelFiles/yeelight.json";
    private static Graph<Object, Object> light_behaviorGraph;
    private static String cmFile = "src/main/java/MOCO/modelFiles/coffeeMachine.json";
    private static Graph<Object, Object> cm_behaviorGraph;
    private static String vcFile = "src/main/java/MOCO/modelFiles/videoCamera.json";
    private static Graph<Object, Object> vc_behaviorGraph;
    private static String wmFile = "src/main/java/MOCO/modelFiles/washingMachine.json";
    private static Graph<Object, Object> wm_behaviorGraph;

    // outAPIs
    private static Map<String, Set<String>> gateway_outEdgeApis;
    private static Map<String, Set<String>> light_outEdgeApis;
    private static Map<String, Set<String>> cm_outEdgeApis;
    private static Map<String, Set<String>> vc_outEdgeApis;
    private static Map<String, Set<String>> wm_outEdgeApis;

    public static void initialize() {
        coffeeMachine = new CoffeeMachine();
        cmTwin = new CMTwin();
        cmController = new CMController("CM001", coffeeMachine, cmTwin);

        gateway = new Gateway();
        gatewayTwin = new GatewayTwin();
        transGatewayController = new GatewayController("gateway001", gateway, gatewayTwin);

        yeelight = new Yeelight(10, false, 255, 255, 255);
        lightTwin = new LightTwin(10, false, 255, 255, 255);
        lightController = new LightController("light001", yeelight, lightTwin);

        vc = new VideoCamera();
        vcTwin = new VCTwin();
        vcController = new VCController("vc001", vc, vcTwin);

        wm = new WashingMachine();
        wmTwin = new WMTwin();
        wmController = new WMController("wm001", wm, wmTwin);

        taskScheduler = new TaskScheduler(5, cmController, transGatewayController, lightController, vcController, wmController);
    }

    public static void loadBehaviorModelsAndOutApis() {
        gateway_behaviorGraph = Utils.loadGraphFromFile(gatewayFile);
        light_behaviorGraph = Utils.loadGraphFromFile(lightFile);
        cm_behaviorGraph = Utils.loadGraphFromFile(cmFile);
        vc_behaviorGraph = Utils.loadGraphFromFile(vcFile);
        wm_behaviorGraph = Utils.loadGraphFromFile(wmFile);
        gateway_outEdgeApis = Utils.loadOutEdgeApis(gateway_behaviorGraph);
        light_outEdgeApis = Utils.loadOutEdgeApis(light_behaviorGraph);
        cm_outEdgeApis = Utils.loadOutEdgeApis(cm_behaviorGraph);
        vc_outEdgeApis = Utils.loadOutEdgeApis(vc_behaviorGraph);
        wm_outEdgeApis = Utils.loadOutEdgeApis(wm_behaviorGraph);
    }

    public static void sendMessageYeelight(MessageProxy messageProxy) {
        Message lcMessage1 = new Message("Yeelight", "turnOn", new String[]{});
        String currentState = getCurrentStateBasedOnMsg(lcMessage1);
        boolean flag1 = messageProxy.addMessage(lcMessage1, currentState);
        if (flag1)
            sleep(100);

        Message lcMessage2 = new Message("Yeelight", "turnOff", new String[]{});
        currentState = getCurrentStateBasedOnMsg(lcMessage2);
        boolean flag2 = messageProxy.addMessage(lcMessage2, currentState);
        if (flag2)
            sleep(100);

        Message lcMessage5 = new Message("Yeelight", "turnOn", new String[]{});
        currentState = getCurrentStateBasedOnMsg(lcMessage1);
        boolean flag3 = messageProxy.addMessage(lcMessage5, currentState);
        if (flag3)
            sleep(100);

        Message lcMessage3 = new Message("Yeelight", "setBrightness", new String[]{"50"});
        currentState = getCurrentStateBasedOnMsg(lcMessage3);
        boolean flag4 = messageProxy.addMessage(lcMessage3, currentState);
        if (flag4)
            sleep(100);

        Message lcMessage4 = new Message("Yeelight", "setRGB", new String[]{"120", "130", "111"});
        currentState = getCurrentStateBasedOnMsg(lcMessage4);
        boolean flag5 = messageProxy.addMessage(lcMessage4, currentState);
        if (flag5)
            sleep(100);
    }

    public static void sendMessageVideoCamera(MessageProxy messageProxy) {
        Message vcMessage1 = new Message("VideoCamera", "turnOn", new String[]{});
        String currentState = getCurrentStateBasedOnMsg(vcMessage1);
        boolean flag = messageProxy.addMessage(vcMessage1, currentState);
        if (flag)
            sleep(100);

        Message vcMessage2 = new Message("VideoCamera", "turnOnLight", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage2);
        flag = messageProxy.addMessage(vcMessage2, currentState);
        if (flag)
            sleep(100);

        Message vcMessage3 = new Message("VideoCamera", "turnOnFullColor", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage3);
        flag = messageProxy.addMessage(vcMessage3, currentState);
        if (flag)
            sleep(100);

        Message vcMessage4 = new Message("VideoCamera", "turnOnFlip", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage4);
        flag = messageProxy.addMessage(vcMessage4, currentState);
        if (flag)
            sleep(100);

        Message vcMessage5 = new Message("VideoCamera", "turnOnImproveProgram", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage5);
        flag = messageProxy.addMessage(vcMessage5, currentState);
        if (flag)
            sleep(100);

        Message vcMessage6 = new Message("VideoCamera", "turnOnWdr", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage6);
        flag = messageProxy.addMessage(vcMessage6, currentState);
        if (flag)
            sleep(100);

        Message vcMessage7 = new Message("VideoCamera", "turnOnTrack", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage7);
        flag = messageProxy.addMessage(vcMessage7, currentState);
        if (flag)
            sleep(100);

        Message vcMessage8 = new Message("VideoCamera", "turnOnTrack", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage8);
        flag = messageProxy.addMessage(vcMessage8, currentState);
        if (flag)
            sleep(100);

        Message vcMessage9 = new Message("VideoCamera", "turnOff", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage9);
        flag = messageProxy.addMessage(vcMessage9, currentState);
        if (flag)
            sleep(100);

        Message vcMessage10 = new Message("VideoCamera", "turnOffTrack", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage10);
        flag = messageProxy.addMessage(vcMessage10, currentState);
        if (flag)
            sleep(100);
    }

    public static void sendMessageGateway(MessageProxy messageProxy) {
        Message gwMessage0 = new Message("Gateway", "turnLightOn", new String[]{});
        String currentState = getCurrentStateBasedOnMsg(gwMessage0);
        boolean flag = messageProxy.addMessage(gwMessage0, currentState);
        if (flag)
            sleep(100);

        Message gwMessage1 = new Message("Gateway", "turnLightOff", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage1);
        flag = messageProxy.addMessage(gwMessage1, currentState);
        if (flag)
            sleep(100);

        Message gwMessage2 = new Message("Gateway", "turnLightOn", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage2);
        flag = messageProxy.addMessage(gwMessage2, currentState);
        if (flag)
            sleep(100);

        Message gwMessage3 = new Message("Gateway", "setLightBrightness", new String[]{"50"});
        currentState = getCurrentStateBasedOnMsg(gwMessage3);
        flag = messageProxy.addMessage(gwMessage3, currentState);
        if (flag)
            sleep(100);

        Message gwMessage4 = new Message("Gateway", "turnAlarmOn", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage4);
        flag = messageProxy.addMessage(gwMessage4, currentState);
        if (flag)
            sleep(100);

        Message gwMessage5 = new Message("Gateway", "turnAlarmOff", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage5);
        flag = messageProxy.addMessage(gwMessage5, currentState);
        if (flag)
            sleep(100);

        Message gwMessage6 = new Message("Gateway", "addDevice", new String[]{"device1"});
        currentState = getCurrentStateBasedOnMsg(gwMessage6);
        flag = messageProxy.addMessage(gwMessage6, currentState);
        if (flag)
            sleep(100);

        Message gwMessage7 = new Message("Gateway", "removeDevice", new String[]{"device1"});
        currentState = getCurrentStateBasedOnMsg(gwMessage7);
        flag = messageProxy.addMessage(gwMessage7, currentState);
        if (flag)
            sleep(100);

        Message gwMessage8 = new Message("Gateway", "turnLightOn", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage0);
        flag = messageProxy.addMessage(gwMessage8, currentState);
        if (flag)
            sleep(100);

        Message gwMessage9 = new Message("Gateway", "addDevice", new String[]{"device1"});
        currentState = getCurrentStateBasedOnMsg(gwMessage9);
        flag = messageProxy.addMessage(gwMessage9, currentState);
        if (flag)
            sleep(100);
    }

    public static void sendMessage(MessageProxy messageProxy) {
        // Test adding and getting messages for CoffeeMachine
        Message message1 = new Message("CoffeeMachine", "turnOn", new String[]{});
        String currentState = getCurrentStateBasedOnMsg(message1);
        messageProxy.addMessage(message1, currentState);

        Message message2 = new Message("CoffeeMachine", "addWater", new String[]{});
        currentState = getCurrentStateBasedOnMsg(message2);
        messageProxy.addMessage(message2, currentState);

//        /*
        // Add messages for WashingMachine
        Message wmMessage1 = new Message("WashingMachine", "turnOn", new String[]{});
        currentState = getCurrentStateBasedOnMsg(wmMessage1);
        messageProxy.addMessage(wmMessage1, currentState);

        Message wmMessage2 = new Message("WashingMachine", "openDoor", new String[]{});
        currentState = getCurrentStateBasedOnMsg(wmMessage2);
        messageProxy.addMessage(wmMessage2, currentState);

        Message wmMessage3 = new Message("WashingMachine", "closeDoor", new String[]{});
        currentState = getCurrentStateBasedOnMsg(wmMessage3);
        messageProxy.addMessage(wmMessage3, currentState);

        Message wmMessage4 = new Message("WashingMachine", "fillWater", new String[]{});
        currentState = getCurrentStateBasedOnMsg(wmMessage4);
        messageProxy.addMessage(wmMessage4, currentState);

        // Add messages for Gateway
        Message gwMessage1 = new Message("Gateway", "turnLightOn", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage1);
        messageProxy.addMessage(gwMessage1, currentState);

        Message gwMessage2 = new Message("Gateway", "turnLightOff", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage2);
        messageProxy.addMessage(gwMessage2, currentState);

        Message gwMessage3 = new Message("Gateway", "setLightBrightness", new String[]{"50"});
        currentState = getCurrentStateBasedOnMsg(gwMessage3);
        messageProxy.addMessage(gwMessage3, currentState);

        Message gwMessage4 = new Message("Gateway", "turnAlarmOn", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage4);
        messageProxy.addMessage(gwMessage4, currentState);

        Message gwMessage5 = new Message("Gateway", "turnAlarmOff", new String[]{});
        currentState = getCurrentStateBasedOnMsg(gwMessage5);
        messageProxy.addMessage(gwMessage5, currentState);

        Message gwMessage6 = new Message("Gateway", "addDevice", new String[]{"device1"});
        currentState = getCurrentStateBasedOnMsg(gwMessage6);
        messageProxy.addMessage(gwMessage6, currentState);

        Message gwMessage7 = new Message("Gateway", "removeDevice", new String[]{"device1"});
        currentState = getCurrentStateBasedOnMsg(gwMessage7);
        messageProxy.addMessage(gwMessage7, currentState);

        // Add messages for VideoCamera
        Message vcMessage1 = new Message("VideoCamera", "turnOnMotionRecord", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage1);
        messageProxy.addMessage(vcMessage1, currentState);

        Message vcMessage2 = new Message("VideoCamera", "turnOnLight", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage2);
        messageProxy.addMessage(vcMessage2, currentState);

        Message vcMessage3 = new Message("VideoCamera", "turnOnFullColor", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage3);
        messageProxy.addMessage(vcMessage3, currentState);

        Message vcMessage4 = new Message("VideoCamera", "turnOnFlip", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage4);
        messageProxy.addMessage(vcMessage4, currentState);

        Message vcMessage5 = new Message("VideoCamera", "turnOnImproveProgram", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage5);
        messageProxy.addMessage(vcMessage5, currentState);

        Message vcMessage6 = new Message("VideoCamera", "turnOnWdr", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage6);
        messageProxy.addMessage(vcMessage6, currentState);

        Message vcMessage7 = new Message("VideoCamera", "turnOnTrack", new String[]{});
        currentState = getCurrentStateBasedOnMsg(vcMessage7);
        messageProxy.addMessage(vcMessage7, currentState);

        // Add messages for VideoCamera (continued)
        currentState = getCurrentStateBasedOnMsg(vcMessage7);
        messageProxy.addMessage(vcMessage7, currentState);

        // Add messages for Yeelight
        Message lcMessage1 = new Message("Yeelight", "turnOn", new String[]{});
        currentState = getCurrentStateBasedOnMsg(lcMessage1);
        messageProxy.addMessage(lcMessage1, currentState);

        Message lcMessage2 = new Message("Yeelight", "turnOff", new String[]{});
        currentState = getCurrentStateBasedOnMsg(lcMessage2);
        messageProxy.addMessage(lcMessage2, currentState);

        Message lcMessage3 = new Message("Yeelight", "setBrightness", new String[]{"50"});
        currentState = getCurrentStateBasedOnMsg(lcMessage3);
        messageProxy.addMessage(lcMessage3, currentState);

        Message lcMessage4 = new Message("Yeelight", "setRGB", new String[]{"120", "130", "111"});
        currentState = getCurrentStateBasedOnMsg(lcMessage4);
        messageProxy.addMessage(lcMessage4, currentState);

//         */
    }

    public static String getCurrentStateBasedOnMsg (Message message){
        String deviceType =  message.getDeviceType();
        switch (deviceType) {
            case "CoffeeMachine":
                return cmTwin.toSystemDeviceString();
            case "Gateway":
                return gatewayTwin.toSystemDeviceString();
            case "Yeelight":
                return lightTwin.toSystemDeviceString();
            case "VideoCamera":
                return vcTwin.toSystemDeviceString();
            case "WashingMachine":
                return wmTwin.toDeviceString();
            default:
                return "Invalid";
        }
    }


    public static void main(String[] args) {
        LOGGER.info("Starting loading files ...");
        initialize();
        loadBehaviorModelsAndOutApis();
        LOGGER.info("Files loaded successfully!");

        MessageProxy messageProxy = new MessageProxy(taskScheduler, gateway_outEdgeApis, light_outEdgeApis, cm_outEdgeApis, vc_outEdgeApis, wm_outEdgeApis, true);

        messageProxy.setProxyOn(false);

        long startTime = System.nanoTime(); // start time of the program
        taskScheduler.start();


        for (int i=0; i<1; i++) {
//            sendMessageYeelight(messageProxy);
//            sendMessageVideoCamera(messageProxy);
            sendMessageGateway(messageProxy);
        }



//        Message message1 = new Message("CoffeeMachine", "turnOn", new String[]{});
//        String currentState = getCurrentStateBasedOnMsg(message1);
//        messageProxy.addMessage(message1, currentState);
//        TaskScheduler.sleep(2000);
//        Message message2 = new Message("CoffeeMachine", "brewCoffee", new String[]{"1"});
//        String currentState2 = getCurrentStateBasedOnMsg(message2);
//        messageProxy.addMessage(message2, currentState2);
        taskScheduler.shutdown();
        long endTime = System.nanoTime(); // end time of the program
        double duration = (endTime - startTime) / 1000000000.0 - 5;
        LOGGER.info("Program ran for " + duration + " seconds.");
    }
}
