package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.VideoCamera;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link VideoCamera}.
 */
public class VideoCamera_Model extends BehaviorModelLearner<VideoCamera> {
    private static final Logger LOGGER = LogManager.getLogger();

    public VideoCamera_Model() {
        super(new VideoCamera());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true, false));
        apis.add(new API("turnOff", true, false));
        apis.add(new API("turnOnMotionRecord", true, false));
        apis.add(new API("turnOffMotionRecord", true, false));
        apis.add(new API("turnOnLight", true, false));
        apis.add(new API("turnOffLight", true, false));
        apis.add(new API("turnOnFullColor", true, false));
        apis.add(new API("turnOffFullColor", true, false));
        apis.add(new API("turnOnFlip", true, false));
        apis.add(new API("turnOffFlip", true, false));
        apis.add(new API("turnOnImproveProgram", true, false));
        apis.add(new API("turnOffImproveProgram", true, false));
        apis.add(new API("turnOnWdr", true, false));
        apis.add(new API("turnOffWdr", true, false));
        apis.add(new API("turnOnTrack", true, false));
        apis.add(new API("turnOffTrack", true, false));
        apis.add(new API("turnOffWatermark", true, false));
        apis.add(new API("setMaxClient", true, false));
        apis.add(new API("setNightMode", true, false));
        apis.add(new API("setMiniLevel", true, false));
        return apis;
    }

    @Override
    protected String executeApi(API api, VideoCamera videoCamera) {
        switch (api.getName()) {
            case "turnOn":
                return videoCamera.turnOn().equals("success") ? videoCamera.toString() : "skip";
            case "turnOff":
                return videoCamera.turnOff().equals("success") ? videoCamera.toString() : "skip";
            case "turnOnMotionRecord":
                return videoCamera.turnOnMotionRecord().equals("success") ? videoCamera.toString() : "skip";
            case "turnOffMotionRecord":
                return videoCamera.turnOffMotionRecord().equals("success") ? videoCamera.toString() : "skip";
            case "turnOnLight":
                return videoCamera.turnOnLight().equals("success") ? videoCamera.toString() : "skip";
            case "turnOffLight":
                return videoCamera.turnOffLight().equals("success") ? videoCamera.toString() : "skip";
            case "turnOnFullColor":
                return videoCamera.turnOnFullColor().equals("success") ? videoCamera.toString() : "skip";
            case "turnOffFullColor":
                return videoCamera.turnOffFullColor().equals("success") ? videoCamera.toString() : "skip";
            case "turnOnFlip":
                return videoCamera.turnOnFlip().equals("success") ? videoCamera.toString() : "skip";
            case "turnOffFlip":
                return videoCamera.turnOffFlip().equals("success") ? videoCamera.toString() : "skip";
            case "turnOnImproveProgram":
                return videoCamera.turnOnImproveProgram().equals("success") ? videoCamera.toString() : "skip";
            case "turnOffImproveProgram":
                return videoCamera.turnOffImproveProgram().equals("success") ? videoCamera.toString() : "skip";
            case "turnOnWdr":
                return videoCamera.turnOnWdr().equals("success") ? videoCamera.toString() : "skip";
            case "turnOffWdr":
                return videoCamera.turnOffWdr().equals("success") ? videoCamera.toString() : "skip";
            case "turnOnTrack":
                return videoCamera.turnOnTrack().equals("success") ? videoCamera.toString() : "skip";
            case "turnOffTrack":
                return videoCamera.turnOffTrack().equals("success") ? videoCamera.toString() : "skip";
            case "turnOffWatermark":
                return videoCamera.turnOffWatermark().equals("success") ? videoCamera.toString() : "skip";
            case "setMaxClient":
                return videoCamera.setMaxClient(2).equals("success") ? videoCamera.toString() : "skip";
            case "setNightMode":
                return videoCamera.setNightMode(2).equals("success") ? videoCamera.toString() : "skip";
            case "setMiniLevel":
                return videoCamera.setMiniLevel(2).equals("success") ? videoCamera.toString() : "skip";
            default:
                return videoCamera.toString();
        }
    }

    @Override
    protected VideoCamera fromString(String state) {
        return VideoCamera.fromString(state);
    }

    @Override
    protected String toSystemState(VideoCamera device) {
        return device.toSystemString();
    }

    @Override
    protected String toStringState(VideoCamera device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<VideoCamera> newInstance() {
        return new VideoCamera_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        VideoCamera_Model learner = new VideoCamera_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/videoCamera_output.dot",
                "src/main/java/MOCO/modelFiles/videoCamera.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
