package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.SmartPlug;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link SmartPlug}.
 */
public class SmartPlug_Model extends BehaviorModelLearner<SmartPlug> {
    private static final Logger LOGGER = LogManager.getLogger();

    public SmartPlug_Model() {
        super(new SmartPlug());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true, false));
        apis.add(new API("turnOff", true, false));
        apis.add(new API("setTimer0", true, false));
        apis.add(new API("setTimer50", true, false));
        return apis;
    }

    @Override
    protected String executeApi(API api, SmartPlug smartPlug) {
        switch (api.getName()) {
            case "turnOn":
                return smartPlug.turnOn().equals("success") ? smartPlug.toString() : "skip";
            case "turnOff":
                return smartPlug.turnOff().equals("success") ? smartPlug.toString() : "skip";
            case "setTimer0":
                return smartPlug.setTimer(0).equals("success") ? smartPlug.toString() : "skip";
            case "setTimer50":
                return smartPlug.setTimer(50).equals("success") ? smartPlug.toString() : "skip";
            default:
                return smartPlug.toString();
        }
    }

    @Override
    protected SmartPlug fromString(String state) {
        return SmartPlug.fromString(state);
    }

    @Override
    protected String toSystemState(SmartPlug device) {
        return device.toSystemString();
    }

    @Override
    protected String toStringState(SmartPlug device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<SmartPlug> newInstance() {
        return new SmartPlug_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        SmartPlug_Model learner = new SmartPlug_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/smartplug_output.dot",
                "src/main/java/MOCO/modelFiles/smartplug.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
