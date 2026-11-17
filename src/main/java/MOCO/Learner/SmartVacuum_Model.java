package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.SmartVacuum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link SmartVacuum}.
 */
public class SmartVacuum_Model extends BehaviorModelLearner<SmartVacuum> {
    private static final Logger LOGGER = LogManager.getLogger();

    public SmartVacuum_Model() {
        super(new SmartVacuum());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true, false));
        apis.add(new API("turnOff", true, false));
        apis.add(new API("startCleaning", true, false));
        apis.add(new API("stopCleaning", true, false));
        apis.add(new API("setModeAuto", true, false));
        apis.add(new API("setModeSpot", true, false));
        apis.add(new API("setModeEdge", true, false));
        return apis;
    }

    @Override
    protected String executeApi(API api, SmartVacuum smartVacuum) {
        switch (api.getName()) {
            case "turnOn":
                return smartVacuum.turnOn().equals("success") ? smartVacuum.toString() : "skip";
            case "turnOff":
                return smartVacuum.turnOff().equals("success") ? smartVacuum.toString() : "skip";
            case "startCleaning":
                return smartVacuum.startCleaning().equals("success") ? smartVacuum.toString() : "skip";
            case "stopCleaning":
                return smartVacuum.stopCleaning().equals("success") ? smartVacuum.toString() : "skip";
            case "setModeAuto":
                return smartVacuum.setMode("auto").equals("success") ? smartVacuum.toString() : "skip";
            case "setModeSpot":
                return smartVacuum.setMode("spot").equals("success") ? smartVacuum.toString() : "skip";
            case "setModeEdge":
                return smartVacuum.setMode("edge").equals("success") ? smartVacuum.toString() : "skip";
            default:
                return smartVacuum.toString();
        }
    }

    @Override
    protected SmartVacuum fromString(String state) {
        return SmartVacuum.fromString(state);
    }

    @Override
    protected String toSystemState(SmartVacuum device) {
        return device.toSystemString();
    }

    @Override
    protected String toStringState(SmartVacuum device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<SmartVacuum> newInstance() {
        return new SmartVacuum_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        SmartVacuum_Model learner = new SmartVacuum_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/smartvacuum_output.dot",
                "src/main/java/MOCO/modelFiles/smartvacuum.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
