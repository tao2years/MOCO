package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.Yeelight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link Yeelight}.
 */
public class Yeelight_Model extends BehaviorModelLearner<Yeelight> {
    private static final Logger LOGGER = LogManager.getLogger();

    public Yeelight_Model() {
        super(new Yeelight());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true, false));
        apis.add(new API("turnOff", true, false));
        apis.add(new API("setBrightness", true, false));
        apis.add(new API("setRGB", true, false));
        return apis;
    }

    @Override
    protected String executeApi(API api, Yeelight yeelight) {
        switch (api.getName()) {
            case "turnOn":
                return yeelight.turnOn().equals("success") ? yeelight.toString() : "skip";
            case "turnOff":
                return yeelight.turnOff().equals("success") ? yeelight.toString() : "skip";
            case "setBrightness":
                return yeelight.setBrightness(50).equals("success") ? yeelight.toString() : "skip";
            case "setRGB":
                return yeelight.setRGB(50, 50, 50).equals("success") ? yeelight.toString() : "skip";
            case "setMode":
                return yeelight.setColorMode(1).equals("success") ? yeelight.toString() : "skip";
            default:
                return yeelight.toString();
        }
    }

    @Override
    protected Yeelight fromString(String state) {
        return Yeelight.fromString(state);
    }

    @Override
    protected String toSystemState(Yeelight device) {
        return device.toSystemString();
    }

    @Override
    protected String toStringState(Yeelight device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<Yeelight> newInstance() {
        return new Yeelight_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Yeelight_Model learner = new Yeelight_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/yeelight_output.dot",
                "src/main/java/MOCO/modelFiles/yeelight.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
