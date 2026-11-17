package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.SmartSpeaker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link SmartSpeaker}.
 */
public class SmartSpeaker_Model extends BehaviorModelLearner<SmartSpeaker> {
    private static final Logger LOGGER = LogManager.getLogger();

    public SmartSpeaker_Model() {
        super(new SmartSpeaker());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true, false));
        apis.add(new API("turnOff", true, false));
        apis.add(new API("playTrack", true, false));
        apis.add(new API("stopTrack", true, false));
        apis.add(new API("setVolume0", true, false));
        apis.add(new API("setVolume50", true, false));
        return apis;
    }

    @Override
    protected String executeApi(API api, SmartSpeaker smartSpeaker) {
        switch (api.getName()) {
            case "turnOn":
                return smartSpeaker.turnOn().equals("success") ? smartSpeaker.toString() : "skip";
            case "turnOff":
                return smartSpeaker.turnOff().equals("success") ? smartSpeaker.toString() : "skip";
            case "playTrack":
                return smartSpeaker.playTrack("Track1").equals("success") ? smartSpeaker.toString() : "skip";
            case "stopTrack":
                return smartSpeaker.stopTrack().equals("success") ? smartSpeaker.toString() : "skip";
            case "setVolume0":
                return smartSpeaker.setVolume(0).equals("success") ? smartSpeaker.toString() : "skip";
            case "setVolume50":
                return smartSpeaker.setVolume(50).equals("success") ? smartSpeaker.toString() : "skip";
            default:
                return smartSpeaker.toString();
        }
    }

    @Override
    protected SmartSpeaker fromString(String state) {
        return SmartSpeaker.fromString(state);
    }

    @Override
    protected String toSystemState(SmartSpeaker device) {
        return device.toSystemString();
    }

    @Override
    protected String toStringState(SmartSpeaker device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<SmartSpeaker> newInstance() {
        return new SmartSpeaker_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        SmartSpeaker_Model learner = new SmartSpeaker_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/smartspeaker_output.dot",
                "src/main/java/MOCO/modelFiles/smartspeaker.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
