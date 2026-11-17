package MOCO;

import graph.Graph;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Characterization tests for MOCO utility logic: model loading, out-edge API
 * index and shortest path calculation. These tests run from the repository
 * root (like Maven surefire does) and use the checked-in behavior models.
 */
public class UtilsTest {

    private static Graph<Object, Object> yeelightGraph;
    private static Graph<Object, Object> wmGraph;
    private static Map<String, Set<String>> yeelightOutApis;

    @BeforeClass
    public static void loadModels() {
        yeelightGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/yeelight.json");
        wmGraph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/washingMachine.json");
        yeelightOutApis = Utils.loadOutEdgeApis(yeelightGraph);
    }

    @Test
    public void yeelightModelLoadsNodesAndEdges() {
        assertTrue(yeelightGraph.nodeCount() > 0);
        assertTrue(yeelightGraph.getEdges().size() > 0);
    }

    @Test
    public void outEdgeApisIndexMapsEveryStateToItsApiNames() {
        // Edge names carry API metadata ("api|preemptive|humanIntervention")
        // and are indexed verbatim.
        assertEquals(8, yeelightOutApis.size());
        for (Map.Entry<String, Set<String>> entry : yeelightOutApis.entrySet()) {
            for (String api : entry.getValue()) {
                assertTrue(api.matches("(turnOn|turnOff|setBrightness|setRGB)\\|(true|false)\\|(true|false)"));
            }
        }
        assertTrue(yeelightOutApis.containsKey("Yeelight{'powerOn':false, 'brightness':false, 'rgb':false}"));
        // index keys are node contents (state strings), not node ids
        Set<String> offApis = yeelightOutApis.get("Yeelight{'powerOn':false, 'brightness':false, 'rgb':false}");
        assertEquals(new HashSet<>(Arrays.asList("turnOn|true|false")), offApis);
        Set<String> onApis = yeelightOutApis.get("Yeelight{'powerOn':true, 'brightness':false, 'rgb':false}");
        assertEquals(new HashSet<>(Arrays.asList(
                "turnOff|true|false", "setBrightness|true|false", "setRGB|true|false")), onApis);
    }

    @Test
    public void findShortestPathsReturnsApiPathWhenConnected() {
        // nodes are serialized Node{...} strings; turnOn S0->S4 is direct
        String source = findNodeContaining(yeelightGraph, "'powerOn':false, 'brightness':false, 'rgb':false");
        String target = findNodeContaining(yeelightGraph, "'powerOn':true, 'brightness':false, 'rgb':false");
        List<List<String>> paths = Utils.findShortestPaths(yeelightGraph, source, target, source);
        assertFalse(paths.isEmpty());
        // path entries are the stored edge names (with metadata)
        assertEquals(Collections.singletonList("turnOn|true|false"), paths.get(0));
    }

    @Test
    public void findShortestPathsUsesOriginWhenNoDirectPathExists() {
        // from a "washing+not rinsing" state to "washing+rinsing+spinning" the
        // only route runs through other states; path starts with an API name
        String source = findNodeContaining(wmGraph, "'rinsing':false");
        String target = findNodeContaining(wmGraph, "'spinning':true");
        List<List<String>> paths = Utils.findShortestPaths(wmGraph, source, target, findS0(wmGraph));
        assertFalse(paths.isEmpty());
        for (List<String> path : paths) {
            for (String api : path) {
                // every entry is an API name, never a node id
                assertFalse(api.startsWith("Node{"));
            }
        }
    }

    @Test
    public void findShortestPathsFallsBackToEmptyPathOnUnknownNodes() {
        List<List<String>> paths = Utils.findShortestPaths(yeelightGraph, "missing-1", "missing-2", "missing-0");
        // no node matches, so an empty path list entry is returned
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).isEmpty());
    }

    @Test
    public void loadGraphFromFileToleratesMissingFileByReturningEmptyGraph() {
        // Documented current behavior: IO errors are only printed, an empty graph is returned
        Graph<Object, Object> graph = Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/missing.json");
        assertEquals(0, graph.nodeCount());
    }

    @Test
    public void nodeFromStringParsesSerializedNode() {
        Node node = Node.fromString("Node{\"nodeId\":\"S3\", \"content\":\"Yeelight{'powerOn':false}\"}");
        assertEquals("S3", node.getNodeId());
        assertEquals("Yeelight{'powerOn':false}", node.getContent());
    }

    @Test
    public void edgeFromStringParsesSerializedEdge() {
        // Documented current behavior: fromString expects JSON object syntax,
        // so it is used with Gson-serialized edges, not with toString output.
        // toString output ("Edge{source=...}") is not parseable by it.
        try {
            new Edge(null, null, null).fromString(
                    "Edge{source='S0', target='S1', api='turnOn'}");
            fail("expected JSONException");
        } catch (com.alibaba.fastjson.JSONException expected) {
            // expected: not valid JSON
        }
    }

    @Test
    public void edgeFromJsonStringParsesFields() {
        Edge edge = new Edge(null, null, null).fromString(
                "{\"source\":\"S0\",\"target\":\"S1\",\"api\":\"turnOn\"}");
        assertEquals("S0", edge.getSource());
        assertEquals("S1", edge.getTarget());
        assertEquals("turnOn", edge.getApi());
    }

    private static String findNodeContaining(Graph<Object, Object> graph, String fragment) {
        for (Object edgeObj : graph.getEdges()) {
            graph.Edge edge = (graph.Edge) edgeObj;
            if (edge.getSource().contains(fragment)) {
                return edge.getSource();
            }
        }
        throw new AssertionError("no node containing " + fragment);
    }

    private static String findS0(Graph<Object, Object> graph) {
        for (Object edgeObj : graph.getEdges()) {
            graph.Edge edge = (graph.Edge) edgeObj;
            if (edge.getSource().contains("S0")) {
                return edge.getSource();
            }
        }
        throw new AssertionError("no S0 node");
    }
}
