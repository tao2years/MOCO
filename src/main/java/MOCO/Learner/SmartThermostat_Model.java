package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.SmartThermostat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link SmartThermostat}.
 */
public class SmartThermostat_Model extends BehaviorModelLearner<SmartThermostat> {
    private static final Logger LOGGER = LogManager.getLogger();

    public SmartThermostat_Model() {
        super(new SmartThermostat());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true, false));
        apis.add(new API("turnOff", true, false));
        apis.add(new API("setTargetTemperature", true, false));
        apis.add(new API("setModeCool", true, false));
        apis.add(new API("setModeHeat", true, false));
        apis.add(new API("setModeAuto", true, false));
        apis.add(new API("setModeOff", true, false));
        return apis;
    }

    @Override
    protected String executeApi(API api, SmartThermostat smartThermostat) {
        switch (api.getName()) {
            case "turnOn":
                return smartThermostat.turnOn().equals("success") ? smartThermostat.toString() : "skip";
            case "turnOff":
                return smartThermostat.turnOff().equals("success") ? smartThermostat.toString() : "skip";
            case "setTargetTemperature":
                return smartThermostat.setTargetTemperature(22.0).equals("success") ? smartThermostat.toString() : "skip";
            case "setModeCool":
                return smartThermostat.setMode("cool").equals("success") ? smartThermostat.toString() : "skip";
            case "setModeHeat":
                return smartThermostat.setMode("heat").equals("success") ? smartThermostat.toString() : "skip";
            case "setModeAuto":
                return smartThermostat.setMode("auto").equals("success") ? smartThermostat.toString() : "skip";
            case "setModeOff":
                return smartThermostat.setMode("off").equals("success") ? smartThermostat.toString() : "skip";
            default:
                return smartThermostat.toString();
        }
    }

    @Override
    protected SmartThermostat fromString(String state) {
        return SmartThermostat.fromString(state);
    }

    @Override
    protected String toSystemState(SmartThermostat device) {
        return device.toSystemString();
    }

    @Override
    protected String toStringState(SmartThermostat device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<SmartThermostat> newInstance() {
        return new SmartThermostat_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        SmartThermostat_Model learner = new SmartThermostat_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/smartthermostat_output.dot",
                "src/main/java/MOCO/modelFiles/smartthermostat.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
