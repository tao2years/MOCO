package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.WashingMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link WashingMachine}.
 * Note: this device has no separate system-state view, so both node content
 * and node id use the full device state string (as the original did).
 */
public class WashingMachine_Model extends BehaviorModelLearner<WashingMachine> {
    private static final Logger LOGGER = LogManager.getLogger();

    public WashingMachine_Model() {
        super(new WashingMachine());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true, false));
        apis.add(new API("turnOff", true, false));
        apis.add(new API("openDoor", true, false));
        apis.add(new API("closeDoor", true, false));
        apis.add(new API("fillWater", false, false));
        apis.add(new API("startWashing", false, true));
        apis.add(new API("startRinsing", false, true));
        apis.add(new API("startSpinning", false, true));
        apis.add(new API("stop", true, true));
        return apis;
    }

    @Override
    protected String executeApi(API api, WashingMachine wm) {
        switch (api.getName()) {
            case "turnOn":
                return wm.turnOn().equals("success") ? wm.toString() : "skip";
            case "turnOff":
                return wm.turnOff().equals("success") ? wm.toString() : "skip";
            case "openDoor":
                return wm.openDoor().equals("success") ? wm.toString() : "skip";
            case "closeDoor":
                return wm.closeDoor().equals("success") ? wm.toString() : "skip";
            case "fillWater":
                return wm.fillWater().equals("success") ? wm.toString() : "skip";
            case "startWashing":
                return wm.startWashing().equals("success") ? wm.toString() : "skip";
            case "startRinsing":
                return wm.startRinsing().equals("success") ? wm.toString() : "skip";
            case "startSpinning":
                return wm.startSpinning().equals("success") ? wm.toString() : "skip";
            case "stop":
                return wm.stop().equals("success") ? wm.toString() : "skip";
            default:
                return wm.toString();
        }
    }

    @Override
    protected WashingMachine fromString(String state) {
        return WashingMachine.fromString(state);
    }

    @Override
    protected String toSystemState(WashingMachine device) {
        return device.toString();
    }

    @Override
    protected String toStringState(WashingMachine device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<WashingMachine> newInstance() {
        return new WashingMachine_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        WashingMachine_Model learner = new WashingMachine_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/washingMachine_output.dot",
                "src/main/java/MOCO/modelFiles/washingMachine.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
