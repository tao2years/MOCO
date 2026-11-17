package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.Gateway;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link Gateway}.
 */
public class Gateway_Model extends BehaviorModelLearner<Gateway> {
    private static final Logger LOGGER = LogManager.getLogger();

    public Gateway_Model() {
        super(new Gateway());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnLightOn", true, false));
        apis.add(new API("turnLightOff", true, false));
        apis.add(new API("setLightBrightness", true, false));
        apis.add(new API("turnAlarmOn", true, false));
        apis.add(new API("turnAlarmOff", true, false));
        apis.add(new API("addDevice", true, false));
        apis.add(new API("removeDevice", true, false));
        return apis;
    }

    @Override
    protected String executeApi(API api, Gateway gateway) {
        switch (api.getName()) {
            case "turnLightOn":
                return gateway.turnLightOn().equals("success") ? gateway.toString() : "skip";
            case "turnLightOff":
                return gateway.turnLightOff().equals("success") ? gateway.toString() : "skip";
            case "setLightBrightness":
                return gateway.setLightBrightness(50).equals("success") ? gateway.toString() : "skip";
            case "turnAlarmOn":
                return gateway.turnAlarmOn().equals("success") ? gateway.toString() : "skip";
            case "turnAlarmOff":
                return gateway.turnAlarmOff().equals("success") ? gateway.toString() : "skip";
            case "addDevice":
                return gateway.addDevice("device").equals("success") ? gateway.toString() : "skip";
            case "removeDevice":
                return gateway.removeDevice("device").equals("success") ? gateway.toString() : "skip";
            default:
                return gateway.toString();
        }
    }

    @Override
    protected Gateway fromString(String state) {
        return Gateway.fromString(state);
    }

    @Override
    protected String toSystemState(Gateway device) {
        return device.toSystemString();
    }

    @Override
    protected String toStringState(Gateway device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<Gateway> newInstance() {
        return new Gateway_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Gateway_Model learner = new Gateway_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/gateway_output.dot",
                "src/main/java/MOCO/modelFiles/gateway.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
