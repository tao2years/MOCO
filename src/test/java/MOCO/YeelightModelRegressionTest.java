package MOCO;

import graph.Graph;
import MOCO.Learner.Yeelight_Model;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end regression: the Yeelight behavior model checked into
 * {@code src/main/java/MOCO/modelFiles/yeelight.json} must be exactly the
 * model the current Learner code produces. The Yeelight virtual device is
 * fully deterministic (no I/O, no randomness), so re-running the learner
 * from scratch must reproduce the golden transitions one-to-one.
 *
 * If this test fails, either the Yeelight virtual device or the Learner
 * exploration changed the learned behavior. That is an intentional model
 * change only when the golden file is regenerated together with it.
 */
public class YeelightModelRegressionTest {

    private static final Path GOLDEN_JSON =
            Paths.get("src", "main", "java", "MOCO", "modelFiles", "yeelight.json");

    private static final Path GOLDEN_DOT =
            Paths.get("src", "main", "java", "MOCO", "modelFiles", "yeelight_output.dot");

    private static final Pattern NODE_ID_PATTERN = Pattern.compile("\"nodeId\":\"(S\\d+)\"");

    private static final Pattern DOT_EDGE_PATTERN =
            Pattern.compile("\\t(\\S+) -> (\\S+) \\[label=\"([^\"]*)\"\\];");

    @Test
    public void regeneratedModelMatchesGoldenFile() throws IOException {
        assertTrue("golden yeelight.json not found; run tests from the project root: "
                + GOLDEN_JSON.toAbsolutePath(), Files.isRegularFile(GOLDEN_JSON));

        // 1. Regenerate the behavior model from scratch with the current code.
        Graph<Object, Object> regenerated = Yeelight_Model.buildBehaviorGraph();

        assertEquals("learned node count drifted from the golden model",
                8, regenerated.nodeCount());
        assertEquals("learned transition count drifted from the golden model",
                16, regenerated.edgeCount());

        // 2. Load the golden transitions from the checked-in model file.
        Graph<Object, Object> golden = Utils.loadGraphFromFile(GOLDEN_JSON.toString());

        assertEquals("golden model no longer holds the pinned node count",
                8, golden.nodeCount());
        assertEquals("golden model no longer holds the pinned transition count",
                16, golden.edgeCount());

        // 3. The learned transition set must equal the golden transition set.
        Set<String> regeneratedEdges = transitionKeys(regenerated);
        Set<String> goldenEdges = transitionKeys(golden);

        assertEquals("regenerated Yeelight model differs from the golden model; "
                        + "if the model change is intentional, regenerate "
                        + GOLDEN_JSON + " and " + GOLDEN_DOT,
                goldenEdges, regeneratedEdges);
    }

    @Test
    public void regeneratedModelMatchesGoldenDotFile() throws IOException {
        assertTrue("golden yeelight_output.dot not found; run tests from the project root: "
                + GOLDEN_DOT.toAbsolutePath(), Files.isRegularFile(GOLDEN_DOT));

        Graph<Object, Object> regenerated = Yeelight_Model.buildBehaviorGraph();
        Set<String> regeneratedEdges = transitionKeys(regenerated);

        String dot = new String(Files.readAllBytes(GOLDEN_DOT), StandardCharsets.UTF_8);
        Set<String> dotEdges = new HashSet<>();
        Matcher matcher = DOT_EDGE_PATTERN.matcher(dot);
        while (matcher.find()) {
            dotEdges.add(matcher.group(1) + "->" + matcher.group(2) + ":" + matcher.group(3));
        }

        // The dot generator iterates a HashSet, so line order is not meaningful:
        // compare transition sets.
        assertEquals("regenerated Yeelight model differs from the golden dot file",
                dotEdges, regeneratedEdges);
    }

    @Test
    public void explorationIsDeterministicAcrossRuns() {
        Set<String> first = transitionKeys(Yeelight_Model.buildBehaviorGraph());
        Set<String> second = transitionKeys(Yeelight_Model.buildBehaviorGraph());
        assertEquals("two learner runs produced different Yeelight models", first, second);
    }

    /**
     * Canonical transition keys: (source node id, target node id, api label).
     * Node ids are extracted from the Node-encoded endpoints, so a state that
     * kept its content but changed its id still counts as a change.
     */
    private static Set<String> transitionKeys(Graph<Object, Object> graph) {
        Set<String> keys = new HashSet<>();
        for (graph.Edge edge : graph.getEdges()) {
            String source = nodeIdOf((String) edge.getSource());
            String target = nodeIdOf((String) edge.getTarget());
            keys.add(source + "->" + target + ":" + edge.getName());
        }
        return keys;
    }

    private static String nodeIdOf(String nodeString) {
        Matcher matcher = NODE_ID_PATTERN.matcher(nodeString);
        assertTrue("node is not Node-encoded: " + nodeString, matcher.find());
        return matcher.group(1);
    }
}
