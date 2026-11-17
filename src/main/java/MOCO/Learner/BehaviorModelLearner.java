package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import MOCO.Node;
import graph.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static MOCO.Utils.generateDotFile;
import static MOCO.Utils.saveEdgesToFile;

/**
 * Common scaffolding for every virtual-device behavior-model learner.
 *
 * <p>A concrete learner declares the device under learning (D), the initial
 * device, the API alphabet and how each API is dispatched. This base class
 * then provides the shared state exploration, node numbering, dot/json
 * export logic that previously was duplicated (with small variations)
 * across all {@code *_Model} classes.</p>
 *
 * <p>Instances are stateful: each instance tracks its own visited-state set,
 * so create a new instance per exploration run (as the learners' {@code main}
 * methods do).</p>
 *
 * @param <D> virtual device type under learning
 */
public abstract class BehaviorModelLearner<D> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Safety bound for the coffee-machine fixed-point loop. */
    protected static final int MAX_MODE = 20;

    private static final int HISTORY_SIZE = 20;
    private static final int MAX_REPORTED_PATHS = 3;

    private final Map<String, Set<String>> stateTransitions = new HashMap<>();
    private final D initialDevice;

    protected BehaviorModelLearner(D initialDevice) {
        this.initialDevice = initialDevice;
    }

    // ----- contract for concrete learners -----

    /** API alphabet the learner explores on every state. */
    protected abstract List<API> getAllPossibleAPIs();

    /** Applies {@code api} to {@code device}; returns "skip" if rejected. */
    protected abstract String executeApi(API api, D device);

    /** Rebuilds a device from its serialized {@code toString()} state. */
    protected abstract D fromString(String state);

    /** Serialized system-level state used as graph node content. */
    protected abstract String toSystemState(D device);

    /** Serialized full state used to clone devices between transitions. */
    protected abstract String toStringState(D device);

    // ----- shared exploration -----

    /**
     * Explores the reachable state space starting from the initial device
     * and records every observed transition in {@code edges}.
     */
    public void generateGraph(Set<Edge> edges) {
        String initSystemState = toSystemState(initialDevice);
        stateTransitions.put(initSystemState, new HashSet<>());
        exploreState(toStringState(initialDevice), initSystemState, edges);
    }

    private void exploreState(String state, String systemState, Set<Edge> edges) {
        for (API api : getAllPossibleAPIs()) {
            D currentDevice = fromString(state);
            String nextState = executeApi(api, currentDevice);
            if (!nextState.equals("skip")) {
                D nextDevice = fromString(nextState);
                String nextSystemState = toSystemState(nextDevice);
                edges.add(new Edge(systemState, nextSystemState, api.toString()));

                nextState = chaseFixedPoint(api, nextDevice, nextSystemState, edges);

                if (!stateTransitions.containsKey(nextSystemState)) {
                    stateTransitions.put(nextSystemState, new HashSet<>());
                    exploreState(nextState, nextSystemState, edges);
                }
                stateTransitions.get(systemState).add(api.getName());
            }
        }
    }

    /**
     * The virtual devices update volumes/flags in two steps (API applies the
     * visible state change, then the derived flags follow on a later call).
     * When the system state did not change but the full state did, re-applying
     * the same API converges to the fixed point; this loop follows it while
     * recording the intermediate edges. Guarded by {@link #MAX_MODE} to stay
     * safe on devices that keep changing.
     */
    private String chaseFixedPoint(API api, D nextDevice, String nextSystemState, Set<Edge> edges) {
        String nextState = toStringState(nextDevice);
        int temp = 0;
        while (nextSystemState.equals(toSystemState(nextDevice))
                && !toStringState(nextDevice).equals(nextState)) {
            String state = nextState;
            nextState = executeApi(api, nextDevice);
            nextDevice = fromString(nextState);
            edges.add(new Edge(nextSystemState, toSystemState(nextDevice), api.toString()));
            temp++;
            if (temp > MAX_MODE) {
                break;
            }
        }
        return nextState;
    }

    // ----- shared statistics / export helpers -----

    /** Number of distinct system states seen so far. */
    public int discoveredStateCount() {
        return stateTransitions.size();
    }

    /**
     * Repeatedly explores the device and accumulates edges until the edge
     * count is stable for {@code #HISTORY_SIZE} consecutive runs, bounded by
     * {@code maxRuns}.
     */
    public Set<Edge> learnEdges(int warmupRuns, int maxRuns) {
        Set<Edge> edges = new HashSet<>();
        int[] sizeHistory = new int[HISTORY_SIZE];
        int historyIndex = 0;
        for (int i = 0; i < warmupRuns; i++) {
            Set<Edge> run = new HashSet<>();
            newInstance().generateGraph(run);
            edges.addAll(run);
        }
        for (int i = warmupRuns; i < maxRuns; i++) {
            Set<Edge> run = new HashSet<>();
            newInstance().generateGraph(run);
            edges.addAll(run);
            sizeHistory[historyIndex] = edges.size();
            historyIndex = (historyIndex + 1) % HISTORY_SIZE;
            if (sizeHistoryStable(sizeHistory)) {
                break;
            }
        }
        return edges;
    }

    /** True when the whole history records the same non-zero size. */
    protected static boolean sizeHistoryStable(int[] sizeHistory) {
        if (sizeHistory[0] == 0) {
            return false;
        }
        int size = sizeHistory[0];
        for (int i = 1; i < sizeHistory.length; i++) {
            if (sizeHistory[i] != size) {
                return false;
            }
        }
        return true;
    }

    /** Creates a fresh, unexplored learner of the same type. */
    protected abstract BehaviorModelLearner<D> newInstance();

    /**
     * Writes the learned edges as numbered dot nodes (S0..Sn) and the graph
     * JSON consumed by the runtime monitors. Returns the node-numbering map.
     */
    public Map<String, String> export(Set<Edge> edges, String dotFile, String jsonFile) {
        Graph<Object, Object> userGraph = new Graph<>(true, true, false);
        Map<String, String> nodeMap = numberNodes(edges);
        Set<Edge> dotSet = new HashSet<>();
        for (String node : nodeMap.keySet()) {
            Set<String> visitedApis = new HashSet<>();
            for (Edge edge : edges) {
                if (edge.getSource().equals(node)) {
                    String target = edge.getTarget();
                    String api = edge.getApi();
                    String targetAndApi = target + " " + api;
                    if (!visitedApis.contains(targetAndApi)) {
                        userGraph.setEdge(new Node(nodeMap.get(node), node).toString(),
                                new Node(nodeMap.get(target), target).toString(), api, api);
                        dotSet.add(new Edge(nodeMap.get(node), nodeMap.get(target), api));
                        visitedApis.add(targetAndApi);
                    }
                }
            }
        }
        generateDotFile(dotSet, nodeMap.size(), dotFile);
        saveEdgesToFile(userGraph.getEdges(), jsonFile);
        return nodeMap;
    }

    /**
     * Assigns node numbers S0..Sn to distinct states, case-insensitively
     * ordered (kept identical to the original learners' numbering so the
     * generated model files remain byte-comparable).
     */
    protected static Map<String, String> numberNodes(Set<Edge> edges) {
        Set<String> nodesSet = new TreeSet<>();
        for (Edge edge : edges) {
            nodesSet.add(edge.getSource());
            nodesSet.add(edge.getTarget());
        }
        Set<String> nodeOrderSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        nodeOrderSet.addAll(nodesSet);
        Map<String, String> nodeNumbers = new HashMap<>();
        int nodeCount = 0;
        for (String node : nodeOrderSet) {
            nodeNumbers.put(node, "S" + nodeCount);
            nodeCount++;
        }
        return nodeNumbers;
    }
}
