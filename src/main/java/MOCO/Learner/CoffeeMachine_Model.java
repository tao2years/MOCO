package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.CoffeeMachine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link CoffeeMachine}.
 * Exploration logic lives in {@link BehaviorModelLearner}.
 */
public class CoffeeMachine_Model extends BehaviorModelLearner<CoffeeMachine> {
    private static final Logger LOGGER = LogManager.getLogger();

    public CoffeeMachine_Model() {
        super(new CoffeeMachine());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true, false));
        apis.add(new API("turnOff", true, false));
        apis.add(new API("addWater", false, false));
        apis.add(new API("addCoffeeBean", false, false));
        apis.add(new API("addMilk", false, false));
        apis.add(new API("placeCup", true, true));
        apis.add(new API("fetchCoffee", true, true));
        for (int i = 1; i <= 3; i++) {
            apis.add(new API("brewCoffee_" + i, false, false));
        }
        return apis;
    }

    @Override
    protected String executeApi(API api, CoffeeMachine device) {
        switch (api.getName()) {
            case "turnOn":
                return device.turnOn().equals("success") ? device.toString() : "skip";
            case "turnOff":
                return device.turnOff().equals("success") ? device.toString() : "skip";
            case "addWater":
                return device.addWater().equals("success") ? device.toString() : "skip";
            case "addCoffeeBean":
                return device.addCoffeeBean().equals("success") ? device.toString() : "skip";
            case "addMilk":
                return device.addMilk().equals("success") ? device.toString() : "skip";
            case "placeCup":
                return device.placeCup().equals("success") ? device.toString() : "skip";
            case "fetchCoffee":
                return device.fetchCoffee().equals("success") ? device.toString() : "skip";
            case "brewCoffee_1":
                return device.brewCoffee(1).equals("success") ? device.toString() : "skip";
            case "brewCoffee_2":
                return device.brewCoffee(2).equals("success") ? device.toString() : "skip";
            case "brewCoffee_3":
                return device.brewCoffee(3).equals("success") ? device.toString() : "skip";
            default:
                return device.toString();
        }
    }

    @Override
    protected CoffeeMachine fromString(String state) {
        return CoffeeMachine.fromString(state);
    }

    @Override
    protected String toSystemState(CoffeeMachine device) {
        return device.toSystemStateString();
    }

    @Override
    protected String toStringState(CoffeeMachine device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<CoffeeMachine> newInstance() {
        return new CoffeeMachine_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        CoffeeMachine_Model learner = new CoffeeMachine_Model();
        // 100 warm-up runs, then iterate until the edge count is stable
        Set<Edge> edges = learner.learnEdges(100, 9999);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/coffeeMachine_output.dot",
                "src/main/java/MOCO/modelFiles/coffeeMachine.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node number: {}, Edge number: {}", nodeMap.size(), edges.size());
    }
}
