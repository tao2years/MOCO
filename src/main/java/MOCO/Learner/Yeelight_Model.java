package MOCO.Learner;

import MOCO.API;
import MOCO.Edge;
import MOCO.Node;
import VirtualDevice.Yeelight;
import graph.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static MOCO.Utils.generateDotFile;
import static MOCO.Utils.saveEdgesToFile;

public class Yeelight_Model {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<String, Set<String>> stateTransitions;

    public Yeelight_Model() {
        stateTransitions = new HashMap<>();
    }

    private static List<API> getAllPossibleAPIs() {
        List<API> apis = new ArrayList<>();
        apis.add(new API("turnOn", true,false));
        apis.add(new API("turnOff", true,false));
        apis.add(new API("setBrightness", true,false));
        apis.add(new API("setRGB", true,false));
        return apis;
    }

    private static String executeApi(String api, Yeelight yeelight) {
        switch (api) {
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

    private static void generateGraph(Set<Edge> edges){
        Yeelight device = new Yeelight();
        String initState = device.toString();
        String initSystemState = device.toSystemString();
        stateTransitions.put(initSystemState, new HashSet<>());

//        LOGGER.info("Initial state: " + initState + " System state: " + initSystemState);
        exploreState(initState, initSystemState, edges);
    }

    private static void exploreState(String state, String systemState, Set<Edge> edges){
        List<API> apis = getAllPossibleAPIs();

        for (API api : apis) {
            Yeelight currentDevice = Yeelight.fromString(state);
            String nextState = executeApi(api.getName(), currentDevice);
            if (!nextState.equals("skip")){
                Yeelight nextWM = Yeelight.fromString(nextState);
                String nextSystemState = nextWM.toSystemString();
                Edge edge = new Edge(systemState, nextSystemState, api.toString());
                edges.add(edge);

                if (!stateTransitions.containsKey(nextSystemState)) {
                    stateTransitions.put(nextSystemState, new HashSet<>());
                    exploreState(nextWM.toString(), nextSystemState, edges);
                }

                stateTransitions.get(systemState).add(api.getName());

            }
        }
    }

    /**
     * Explores the Yeelight virtual device state space and collects the learned transitions.
     * The exploration is fully deterministic: the virtual device has no I/O or randomness,
     * so repeated runs always yield the same edge set.
     *
     * @return the set of learned transitions between abstract system states
     */
    public static Set<Edge> exploreEdges() {
        Set<Edge> edges = new HashSet<>();
        for (int i = 0; i < 2; i++) {
            LOGGER.info("Iteration: " + i);
            Set<Edge> temp = new HashSet<>();
            Yeelight_Model test = new Yeelight_Model();
            test.generateGraph(temp);
            edges.addAll(temp);
        }
        return edges;
    }

    /**
     * Regenerates the Yeelight behavior model from scratch.
     * States are numbered S0..Sn in case-insensitive alphabetical order of their
     * abstract system state string, which keeps the node ids stable across runs.
     * This is the same construction {@link #main(String[])} uses to write the
     * golden files, exposed so the CI golden-file regression test can re-run it.
     *
     * @return the learned behavior graph with Node-encoded node ids and API edge labels
     */
    public static Graph<Object, Object> buildBehaviorGraph() {
        Set<Edge> edges = exploreEdges();

        Set<String> nodesSet = new TreeSet<>();
        for (Edge edge : edges) {
            nodesSet.add(edge.getSource());
            nodesSet.add(edge.getTarget());
        }
        Set<String> nodeOrderSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        nodeOrderSet.addAll(nodesSet);

        // Create node numbers map
        Map<String, String> nodeNumbers = new HashMap<>();
        int nodeCount = 0;
        for (String node : nodeOrderSet) {
            String nodeNumber = "S" + nodeCount;
            nodeNumbers.put(node, nodeNumber);
            nodeCount++;
        }

        Graph<Object, Object> userGraph = new Graph<>(true, true, false);
        Set<Edge> dotSet = new HashSet<>();
        for (Edge edge : edges) {
            String nodeNumber = nodeNumbers.get(edge.getSource());
            String targetNumber = nodeNumbers.get(edge.getTarget());
            String api = edge.getApi();
            userGraph.setEdge(new Node(nodeNumber, edge.getSource()).toString(),
                    new Node(targetNumber, edge.getTarget()).toString(), api, api);
            dotSet.add(new Edge(nodeNumber, targetNumber, api));
        }

        LOGGER.info("Node Count: " + nodeCount + " Edge Count: " + dotSet.size());
        return userGraph;
    }

    private static String nodeId(String nodeString) {
        int start = nodeString.indexOf("\"nodeId\":\"") + "\"nodeId\":\"".length();
        return nodeString.substring(start, nodeString.indexOf('"', start));
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        Graph<Object, Object> userGraph = buildBehaviorGraph();

        long endTime = System.nanoTime(); // end time
        long elapsedTime = endTime - startTime; // running time

        double seconds = (double) elapsedTime / 1000000000.0; // transfer to seconds

        LOGGER.info("Program ran for " + seconds + " seconds.");

        Set<Edge> dotSet = new HashSet<>();
        for (graph.Edge edge : userGraph.getEdges()) {
            dotSet.add(new Edge(nodeId(edge.getSource()), nodeId(edge.getTarget()), edge.getName()));
        }

        String dotFile = "src/main/java/MOCO/modelFiles/yeelight_output.dot";
        generateDotFile(dotSet, userGraph.nodeCount(), dotFile);

        Collection<graph.Edge> edge = userGraph.getEdges();
        String fileName = "src/main/java/MOCO/modelFiles/yeelight.json";
        saveEdgesToFile(edge, fileName);

    }

}
