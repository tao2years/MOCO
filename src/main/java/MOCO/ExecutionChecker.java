package MOCO;

import IoTSystem.DeviceController.*;
import IoTSystem.DeviceTwin.CMTwin;
import IoTSystem.Message;
import graph.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ExecutionChecker {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Graph<Object, Object> gateway_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/gateway.json");
    private static final Graph<Object, Object> light_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/yeelight.json");
    private static final Graph<Object, Object> cm_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/coffeeMachine.json");
    private static final Graph<Object, Object> vc_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/videoCamera.json");
    private static final Graph<Object, Object> wm_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/washingMachine.json");

    public static boolean preCheck(Message message, String currentState, Map<String, Set<String>> gateway_outEdgeApis,
                                   Map<String, Set<String>> light_outEdgeApis, Map<String, Set<String>> cm_outEdgeApis,
                                   Map<String, Set<String>> vc_outEdgeApis, Map<String, Set<String>> wm_outEdgeApis) {
        LOGGER.info("[*] PreCheck. Msg:" + message + " CurrentState:" + currentState);
        if (currentState.equals("Invalid")){
            return false;
        }
        String deviceType = message.getDeviceType();
        LOGGER.info(currentState);
        LOGGER.info(cm_outEdgeApis.keySet());
        switch (deviceType) {
            case "CoffeeMachine":
                return cm_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            case "Gateway":
                return gateway_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            case "Yeelight":
                return light_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            case "VideoCamera":
                return vc_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            case "WashingMachine":
                return wm_outEdgeApis.get(currentState).contains(message.getDeviceAPI());
            default:
                return false;
        }
    }

    public static ArrayList<String> postCheck(String api, String preState, Object controller){
        String controllerName = controller.getClass().getSimpleName();
        checkDeviations(controllerName, preState, api, controller);
        return new ArrayList<>();
    }

    public static void checkDeviations(String controllerName, String preState, String api, Object controller){
        switch (controllerName){
            case "CMController":
                String currentState = ((CMController) controller).getCmTwin().toSystemDeviceString();
                String targetState = getTargetStateBasedOnBehaviourModels(preState, api, cm_behaviorGraph);
                if (!currentState.equals(targetState) && !targetState.equals("null")){
                    LOGGER.info("Digital Deviation Detected");
                    ((CMController) controller).getCmTwin().setTargetState(targetState);
                }
                break;
            case "GatewayController":
                String currentState_gateway = ((GatewayController) controller).getGatewayTwin().toSystemDeviceString();
                String targetState_gateway = getTargetStateBasedOnBehaviourModels(preState, api, gateway_behaviorGraph);
                if (!currentState_gateway.equals(targetState_gateway) && !targetState_gateway.equals("null")){
                    LOGGER.info("Digital Deviation Detected");
                    ((GatewayController) controller).getGatewayTwin().setTargetState(targetState_gateway);
                }
                break;
            case "LightController":
                String current_light = ((LightController) controller).getLightTwin().toSystemString();
                String target_light = getTargetStateBasedOnBehaviourModels(preState, api, light_behaviorGraph);
                if (!current_light.equals(target_light) && !target_light.equals("null")){
                    LOGGER.info("Digital Deviation Detected");
                    ((LightController) controller).getLightTwin().setTargetState(target_light);
                }
                break;
            case "VCController":
                String current_vc = ((VCController) controller).getVcTwin().toSystemDeviceString();
                String target_vc = getTargetStateBasedOnBehaviourModels(preState, api, vc_behaviorGraph);
                if (!current_vc.equals(target_vc) && !target_vc.equals("null")){
                    LOGGER.error("Digital Deviation Detected");
                    ((VCController) controller).getVcTwin().setTargetState(target_vc);
                }
                break;
            case "WMController":
                String current_wm = ((WMController) controller).getWmTwin().toDeviceString();
                String target_wm = getTargetStateBasedOnBehaviourModels(preState, api, wm_behaviorGraph);
                if (!current_wm.equals(target_wm) && !target_wm.equals("null")){
                    LOGGER.error("Digital Deviation Detected");
                    ((WMController) controller).getWmTwin().setTargetState(target_wm);
                }
                break;
            default:
                LOGGER.info("Controller not found");
        }
    }

    public static String getTargetStateBasedOnBehaviourModels (String preState, String cmd, Graph<Object, Object> behaviorGraph) {
        Collection<graph.Edge> edges = behaviorGraph.getEdges();
        for (graph.Edge edge : edges) {
            if (edge.getSource().contains(preState) && edge.getName().equals(cmd)) {
//                LOGGER.info("[****************] Target State: " + Node.fromString(edge.getTarget()).getContent());
                return Node.fromString(edge.getTarget()).getContent();
            }
        }
        return "null";
    }

}
