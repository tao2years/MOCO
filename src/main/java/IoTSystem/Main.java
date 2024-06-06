package IoTSystem;

import IoTSystem.DeviceController.*;
import IoTSystem.DeviceTwin.*;
import MOCO.MessageProxy;
import MOCO.Utils;
import VirtualDevice.*;
import graph.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

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

        yeelight = new Yeelight(10, true, 255, 255, 255);
        lightTwin = new LightTwin(10, true, 255, 255, 255);
        lightController = new LightController("light001", yeelight, lightTwin);

        vc = new VideoCamera();
        vcTwin = new VCTwin();
        vcController = new VCController("vc001", vc, vcTwin);

        wm = new WashingMachine();
        wmTwin = new WMTwin();
        wmController = new WMController("wm001", wm, wmTwin);

        taskScheduler = new TaskScheduler(0, cmController, transGatewayController, lightController, vcController, wmController);
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

    public static void sendMessage(){
        // Test adding and getting messages
        Message message1 = new Message("CoffeeMachine", "turnOn", new String[]{});
        Message message2 = new Message("CoffeeMachine", "addWater", new String[]{});
        Message message3 = new Message("CoffeeMachine", "addMilk", new String[]{});
        Message message4 = new Message("CoffeeMachine", "addCoffeeBean", new String[]{});
        Message message5 = new Message("CoffeeMachine", "placeCup", new String[]{});
        Message message6 = new Message("CoffeeMachine", "brewCoffee", new String[]{"1"});

        taskScheduler.addMessage(message1);
        taskScheduler.addMessage(message2);
        taskScheduler.addMessage(message3);
        taskScheduler.addMessage(message4);
        taskScheduler.addMessage(message5);
        taskScheduler.addMessage(message6);
    }

    public static String getCurrentStateBasedOnMsg (Message message){
        String deviceType =  message.getDeviceType();
        switch (deviceType) {
            case "CoffeeMachine":
                return cmTwin.toSystemStateString().replace("CMTwin","CoffeeMachine");
            case "Gateway":
                return gatewayTwin.toSystemString().replace("GatewayTwin","Gateway");
            case "Yeelight":
                return lightTwin.toSystemString().replace("LightTwin","Yeelight");
            case "VideoCamera":
                return vcTwin.toSystemString().replace("VCTwin","VideoCamera");
            case "WashingMachine":
                return wmTwin.toString().replace("WMTwin","WashingMachine");
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


//        sendMessage();
//        taskScheduler.start();
        Message message1 = new Message("CoffeeMachine", "turnOn", new String[]{});
        String currentState = getCurrentStateBasedOnMsg(message1);
        messageProxy.addMessage(message1, currentState);
        TaskScheduler.sleep(2000);
        Message message2 = new Message("CoffeeMachine", "brewCoffee", new String[]{"1"});
        String currentState2 = getCurrentStateBasedOnMsg(message2);
        messageProxy.addMessage(message2, currentState2);
//        taskScheduler.shutdown();
    }
}
