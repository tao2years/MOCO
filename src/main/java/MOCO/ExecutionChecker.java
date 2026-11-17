package MOCO;

import IoTSystem.DelayedMessage;
import IoTSystem.DeviceController.*;
import IoTSystem.DeviceTwin.*;
import IoTSystem.Message;
import VirtualDevice.*;
import graph.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.DelayQueue;

/**
 * Pre- and post-execution consistency checks.
 *
 * <p>preCheck validates a message against the out-edge API index of the
 * device's behavior model. postCheck compares twin/physical states after the
 * API ran with the state the behavior model predicts, and schedules repair
 * actions on the delayed queue when a deviation is detected.</p>
 */
public class ExecutionChecker {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Graph<Object, Object> gateway_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/gateway.json");
    private static final Graph<Object, Object> light_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/yeelight.json");
    private static final Graph<Object, Object> cm_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/coffeeMachine.json");
    private static final Graph<Object, Object> vc_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/videoCamera.json");
    private static final Graph<Object, Object> wm_behaviorGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/washingMachine.json");

    private static final long REPAIR_DELAY_MS = 100;

    public static boolean preCheck(Message message, String currentState, Map<String, Set<String>> gateway_outEdgeApis,
                                   Map<String, Set<String>> light_outEdgeApis, Map<String, Set<String>> cm_outEdgeApis,
                                   Map<String, Set<String>> vc_outEdgeApis, Map<String, Set<String>> wm_outEdgeApis) {
        if (currentState.equals("Invalid")) {
            return false;
        }
        String deviceType = message.getDeviceType();
        Map<String, Set<String>> outEdgeApis;
        switch (deviceType) {
            case "CoffeeMachine":
                outEdgeApis = cm_outEdgeApis;
                break;
            case "Gateway":
                outEdgeApis = gateway_outEdgeApis;
                break;
            case "Yeelight":
                outEdgeApis = light_outEdgeApis;
                break;
            case "VideoCamera":
                outEdgeApis = vc_outEdgeApis;
                break;
            case "WashingMachine":
                outEdgeApis = wm_outEdgeApis;
                break;
            default:
                return false;
        }
        // NOTE: uses toString().contains() semantics, matching the original
        // implementation (a substring of any API name matches).
        Set<String> apis = outEdgeApis.get(currentState);
        return apis != null && apis.toString().contains(message.getDeviceAPI());
    }

    public static void postCheck(String api, String preState, Object controller, DelayQueue<DelayedMessage> delayedQueue, Message message) {
        String controllerName = controller.getClass().getSimpleName();
        checkDeviations(controllerName, preState, api, controller, delayedQueue, message);
    }

    public static void checkDeviations(String controllerName, String preState, String api, Object controller,
                                       DelayQueue<DelayedMessage> delayedQueue, Message message) {
        switch (controllerName) {
            case "CMController":
                checkCMDeviation(preState, api, (CMController) controller, delayedQueue, message);
                break;
            case "GatewayController":
                checkGWDeviation(preState, api, (GatewayController) controller, delayedQueue, message);
                break;
            case "LightController":
                checkLightDeviation(preState, api, (LightController) controller, delayedQueue, message);
                break;
            case "VCController":
                checkVCDeviation(preState, api, (VCController) controller, delayedQueue, message);
                break;
            case "WMController":
                checkWMDeviation(preState, api, (WMController) controller, delayedQueue, message);
                break;
            default:
                LOGGER.info("Controller not found");
        }
    }

    /** Device-specific operations used by {@link #checkControllerDeviation}. */
    private interface DeviationTarget<C> {
        String readTwin(C controller);

        String readPhysical(C controller);

        void repairTwin(C controller, String targetState);
    }

    private interface ActionGenerator<C> {
        void generate(List<String> actionLists, C controller, DelayQueue<DelayedMessage> delayedQueue);
    }

    private static void checkCMDeviation(String preState, String api, CMController controller,
                                         DelayQueue<DelayedMessage> delayedQueue, Message message) {
        checkControllerDeviation(preState, api, controller, delayedQueue, message,
                cm_behaviorGraph, new DeviationTarget<CMController>() {
                    public String readTwin(CMController c) {
                        return c.getCmTwin().toSystemDeviceString();
                    }

                    public String readPhysical(CMController c) {
                        return c.getCoffeeMachine().toSystemStateString();
                    }

                    public void repairTwin(CMController c, String targetState) {
                        c.getCmTwin().setTargetState(targetState);
                    }
                }, new ActionGenerator<CMController>() {
                    public void generate(List<String> actionLists, CMController c, DelayQueue<DelayedMessage> q) {
                        generateCMActions(actionLists, c, q);
                    }
                });
    }

    private static void checkGWDeviation(String preState, String api, GatewayController controller,
                                         DelayQueue<DelayedMessage> delayedQueue, Message message) {
        checkControllerDeviation(preState, api, controller, delayedQueue, message,
                gateway_behaviorGraph, new DeviationTarget<GatewayController>() {
                    public String readTwin(GatewayController c) {
                        return c.getGatewayTwin().toSystemDeviceString();
                    }

                    public String readPhysical(GatewayController c) {
                        return c.getGateway().toSystemString();
                    }

                    public void repairTwin(GatewayController c, String targetState) {
                        c.getGatewayTwin().setTargetState(targetState);
                    }
                }, new ActionGenerator<GatewayController>() {
                    public void generate(List<String> actionLists, GatewayController c, DelayQueue<DelayedMessage> q) {
                        generateGWActions(actionLists, c, q);
                    }
                });
    }

    private static void checkLightDeviation(String preState, String api, LightController controller,
                                            DelayQueue<DelayedMessage> delayedQueue, Message message) {
        checkControllerDeviation(preState, api, controller, delayedQueue, message,
                light_behaviorGraph, new DeviationTarget<LightController>() {
                    public String readTwin(LightController c) {
                        return c.getLightTwin().toSystemString();
                    }

                    public String readPhysical(LightController c) {
                        return c.getYeelight().toSystemString();
                    }

                    public void repairTwin(LightController c, String targetState) {
                        c.getLightTwin().setTargetState(targetState);
                    }
                }, new ActionGenerator<LightController>() {
                    public void generate(List<String> actionLists, LightController c, DelayQueue<DelayedMessage> q) {
                        generateLightActions(actionLists, c, q);
                    }
                });
    }

    private static void checkVCDeviation(String preState, String api, VCController controller,
                                         DelayQueue<DelayedMessage> delayedQueue, Message message) {
        checkControllerDeviation(preState, api, controller, delayedQueue, message,
                vc_behaviorGraph, new DeviationTarget<VCController>() {
                    public String readTwin(VCController c) {
                        return c.getVcTwin().toSystemDeviceString();
                    }

                    public String readPhysical(VCController c) {
                        return c.getVc().toSystemString();
                    }

                    public void repairTwin(VCController c, String targetState) {
                        c.getVcTwin().setTargetState(targetState);
                    }
                }, new ActionGenerator<VCController>() {
                    public void generate(List<String> actionLists, VCController c, DelayQueue<DelayedMessage> q) {
                        generateVCActions(actionLists, c, q);
                    }
                });
    }

    private static void checkWMDeviation(String preState, String api, WMController controller,
                                         DelayQueue<DelayedMessage> delayedQueue, Message message) {
        checkControllerDeviation(preState, api, controller, delayedQueue, message,
                wm_behaviorGraph, new DeviationTarget<WMController>() {
                    public String readTwin(WMController c) {
                        return c.getWmTwin().toDeviceString();
                    }

                    public String readPhysical(WMController c) {
                        return c.getWm().toString();
                    }

                    public void repairTwin(WMController c, String targetState) {
                        c.getWmTwin().setTargetState(targetState);
                    }
                }, new ActionGenerator<WMController>() {
                    public void generate(List<String> actionLists, WMController c, DelayQueue<DelayedMessage> q) {
                        generateVMActions(actionLists, c, q);
                    }
                });
    }

    /**
     * Shared deviation-check skeleton previously duplicated per controller:
     * compare twin and physical states against the behavior-model target,
     * then either retry the message or generate repair actions.
     */
    private static <C> void checkControllerDeviation(String preState, String api, C controller,
                                                      DelayQueue<DelayedMessage> delayedQueue, Message message,
                                                      Graph<Object, Object> behaviorGraph,
                                                      DeviationTarget<C> target,
                                                      ActionGenerator<C> actions) {
        String currentState = target.readTwin(controller);
        String targetState = getTargetStateBasedOnBehaviourModels(preState, api, behaviorGraph);
        if (!currentState.equals(targetState) && !targetState.equals("null")) {
            LOGGER.error("Digital Deviation Detected");
            target.repairTwin(controller, targetState);
        }
        String currentPhysicalState = target.readPhysical(controller);
        if (!currentPhysicalState.equals(targetState) && !targetState.equals("null")) {
            LOGGER.error("Physical Deviation Detected");
            if (currentPhysicalState.equals(preState)) {
                delayedQueue.offer(new DelayedMessage(message, REPAIR_DELAY_MS));
            } else {
                List<String> actionLists =
                        calculateSolutions(behaviorGraph, currentPhysicalState, targetState, message);
                actions.generate(actionLists, controller, delayedQueue);
                LOGGER.info("Action List: " + actionLists);
            }
        }
    }

    private static void generateVMActions(List<String> actionLists, WMController controller, DelayQueue<DelayedMessage> delayedQueue) {
        for (String action : actionLists) {
            if ("START_FROM_ORIGIN".equals(action)) {
                controller.setWm(new WashingMachine());
                controller.setWmTwin(new WMTwin());
            } else {
                delayedQueue.offer(new DelayedMessage(
                        new Message("WashingMachine", action, new String[]{}), REPAIR_DELAY_MS));
            }
        }
    }

    private static void generateVCActions(List<String> actionLists, VCController controller, DelayQueue<DelayedMessage> delayedQueue) {
        for (String action : actionLists) {
            if ("START_FROM_ORIGIN".equals(action)) {
                controller.setVc(new VideoCamera());
                controller.setVcTwin(new VCTwin());
            } else {
                delayedQueue.offer(new DelayedMessage(
                        new Message("VideoCamera", action, new String[]{}), REPAIR_DELAY_MS));
            }
        }
    }

    private static void generateLightActions(List<String> actionLists, LightController controller, DelayQueue<DelayedMessage> delayedQueue) {
        for (String action : actionLists) {
            switch (action) {
                case "START_FROM_ORIGIN":
                    controller.setYeelight(new Yeelight(10, false, 255, 255, 255));
                    controller.setLightTwin(new LightTwin(10, false, 255, 255, 255));
                    break;
                case "setBrightness":
                    delayedQueue.offer(new DelayedMessage(
                            new Message("Yeelight", "setBrightness", new String[]{"50"}), REPAIR_DELAY_MS));
                    break;
                case "setRGB":
                    delayedQueue.offer(new DelayedMessage(
                            new Message("Yeelight", "setRGB", new String[]{"120", "130", "111"}), REPAIR_DELAY_MS));
                    break;
                default:
                    delayedQueue.offer(new DelayedMessage(
                            new Message("Yeelight", action, new String[]{}), REPAIR_DELAY_MS));
                    break;
            }
        }
    }

    private static void generateGWActions(List<String> actionLists, GatewayController controller, DelayQueue<DelayedMessage> delayedQueue) {
        for (String action : actionLists) {
            switch (action) {
                case "START_FROM_ORIGIN":
                    controller.setGateway(new Gateway());
                    controller.setGatewayTwin(new GatewayTwin());
                    break;
                case "setLightBrightness":
                    delayedQueue.offer(new DelayedMessage(
                            new Message("Gateway", "setLightBrightness", new String[]{"50"}), REPAIR_DELAY_MS));
                    break;
                case "addDevice":
                    delayedQueue.offer(new DelayedMessage(
                            new Message("Gateway", "addDevice", new String[]{"device1"}), REPAIR_DELAY_MS));
                    break;
                case "removeDevice":
                    delayedQueue.offer(new DelayedMessage(
                            new Message("Gateway", "removeDevice", new String[]{"device1"}), REPAIR_DELAY_MS));
                    break;
                default:
                    delayedQueue.offer(new DelayedMessage(
                            new Message("Gateway", action, new String[]{}), REPAIR_DELAY_MS));
                    break;
            }
        }
    }

    private static void generateCMActions(List<String> actionLists, CMController controller, DelayQueue<DelayedMessage> delayedQueue) {
        for (String action : actionLists) {
            switch (action) {
                case "START_FROM_ORIGIN":
                    controller.setCoffeeMachine(new CoffeeMachine());
                    controller.setCmTwin(new CMTwin());
                    break;
                case "brewCoffee":
                    delayedQueue.offer(new DelayedMessage(
                            new Message("CoffeeMachine", "brewCoffee", new String[]{"1"}), REPAIR_DELAY_MS));
                    break;
                default:
                    delayedQueue.offer(new DelayedMessage(
                            new Message("CoffeeMachine", action, new String[]{}), REPAIR_DELAY_MS));
                    break;
            }
        }
    }

    public static String getTargetStateBasedOnBehaviourModels(String preState, String cmd, Graph<Object, Object> behaviorGraph) {
        Collection<graph.Edge> edges = behaviorGraph.getEdges();
        for (graph.Edge edge : edges) {
            if (edge.getSource().contains(preState) && edge.getName().equals(cmd)) {
                return Node.fromString(edge.getTarget()).getContent();
            }
        }
        return "null";
    }

    public static List<String> calculateSolutions(Graph<Object, Object> behaviorGraph, String source, String target, Message message) {
        String sourceNode = findNodeContaining(behaviorGraph, source, "sourceNode");
        String targetNode = findNodeContaining(behaviorGraph, target, "targetNode");
        String originNode = findNodeContaining(behaviorGraph, "S0", "S0");
        LOGGER.info("Calculate Solutions");

        List<List<String>> paths = Utils.findShortestPaths(behaviorGraph, sourceNode, targetNode, originNode);
        String requestedApi = message.getDeviceAPI();
        List<String> endsWithApi = null;
        List<String> endsWithApiWithoutOrigin = null;
        List<String> withoutOrigin = null;
        for (List<String> path : paths) {
            if (path.isEmpty()) {
                continue;
            }
            boolean fromOrigin = path.contains("START_FROM_ORIGIN");
            boolean matchesApi = path.get(path.size() - 1).equals(requestedApi);
            if (matchesApi && !fromOrigin) {
                return path;
            }
            if (matchesApi && endsWithApi == null) {
                endsWithApi = path;
            }
            if (!fromOrigin && withoutOrigin == null) {
                withoutOrigin = path;
            }
        }
        if (endsWithApi != null) {
            return endsWithApi;
        }
        if (withoutOrigin != null) {
            return withoutOrigin;
        }
        return paths.isEmpty() ? new ArrayList<>() : paths.get(0);
    }

    /** Last edge source whose serialized form contains {@code fragment}. */
    private static String findNodeContaining(Graph<Object, Object> graph, String fragment, String label) {
        for (graph.Edge edge : graph.getEdges()) {
            if (edge.getSource().contains(fragment)) {
                LOGGER.info(edge.getSource());
                return edge.getSource();
            }
        }
        return "";
    }

}
