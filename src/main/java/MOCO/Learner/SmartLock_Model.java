package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import VirtualDevice.SmartLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Learns the behavior model of the virtual {@link SmartLock}.
 */
public class SmartLock_Model extends BehaviorModelLearner<SmartLock> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Password used when learning the unlock transition. */
    private static final String CORRECT_PASSWORD = "1234";

    public SmartLock_Model() {
        super(new SmartLock());
    }

    @Override
    protected List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("lock", true, false));
        apis.add(new API("unlock", true, false));
        return apis;
    }

    @Override
    protected String executeApi(API api, SmartLock smartLock) {
        switch (api.getName()) {
            case "lock":
                return smartLock.lock().equals("success") ? smartLock.toString() : "skip";
            case "unlock":
                return smartLock.unlock(CORRECT_PASSWORD).equals("success") ? smartLock.toString() : "skip";
            default:
                return smartLock.toString();
        }
    }

    @Override
    protected SmartLock fromString(String state) {
        return SmartLock.fromString(state);
    }

    @Override
    protected String toSystemState(SmartLock device) {
        return device.toSystemString();
    }

    @Override
    protected String toStringState(SmartLock device) {
        return device.toString();
    }

    @Override
    protected BehaviorModelLearner<SmartLock> newInstance() {
        return new SmartLock_Model();
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        SmartLock_Model learner = new SmartLock_Model();
        Set<Edge> edges = learner.learnEdges(0, 2);
        LOGGER.info("[Finished generating graph] Total edges: {}", edges.size());
        Map<String, String> nodeMap = learner.export(edges,
                "src/main/java/MOCO/modelFiles/smartlock_output.dot",
                "src/main/java/MOCO/modelFiles/smartlock.json");
        double seconds = (System.nanoTime() - startTime) / 1000000000.0;
        LOGGER.info("Program ran for {} seconds.", seconds);
        LOGGER.info("Node Count: {} Edge Count: {}", nodeMap.size(), edges.size());
    }
}
