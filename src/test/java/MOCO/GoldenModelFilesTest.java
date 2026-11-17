package MOCO;

import graph.Graph;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Golden-file validation for the learned device behavior models stored under
 * {@code src/main/java/MOCO/modelFiles}.
 *
 * Every model file pins the state machine MOCA learned for one device. The
 * ExecutionChecker loads these graphs at runtime, so a silently corrupted or
 * accidentally edited model changes system behavior without any code change.
 * These tests make the repository fail fast when that happens:
 *
 * <ul>
 *   <li>each JSON file parses and holds a set of unique transition triples;</li>
 *   <li>node ids follow the S0..Sn convention with no gaps;</li>
 *   <li>each JSON file and its matching *_output.dot file describe the same
 *       transition set (compared as sets; dot line order follows HashSet
 *       iteration and is not meaningful);</li>
 *   <li>node and transition counts match the recorded baseline, so a change
 *       to the Learner or a VirtualDevice surface shows up as a diff here.</li>
 * </ul>
 *
 * When a model is regenerated on purpose, update the expected counts below
 * in the same commit and state the reason there.
 */
public class GoldenModelFilesTest {

    private static final Path MODEL_DIR = Paths.get("src", "main", "java", "MOCO", "modelFiles");

    private static final Pattern NODE_ID_PATTERN = Pattern.compile("\"nodeId\":\"(S\\d+)\"");

    private static final Pattern DOT_EDGE_PATTERN =
            Pattern.compile("\\t(\\S+) -> (\\S+) \\[label=\"([^\"]*)\"\\];");

    /**
     * Baseline of learned model sizes (file -> [nodeCount, uniqueTransitionCount]).
     */
    private static final Map<String, int[]> EXPECTED_COUNTS = new HashMap<>();

    static {
        EXPECTED_COUNTS.put("camera_Real.json", new int[]{36, 396});
        EXPECTED_COUNTS.put("coffeeMachine.json", new int[]{32, 152});
        EXPECTED_COUNTS.put("fan_Real.json", new int[]{8, 24});
        EXPECTED_COUNTS.put("gateway.json", new int[]{32, 128});
        EXPECTED_COUNTS.put("heater_Real.json", new int[]{4, 12});
        EXPECTED_COUNTS.put("humidifier_Real.json", new int[]{48, 480});
        EXPECTED_COUNTS.put("smartlock.json", new int[]{2, 2});
        EXPECTED_COUNTS.put("smartplug.json", new int[]{4, 12});
        EXPECTED_COUNTS.put("smartspeaker.json", new int[]{10, 30});
        EXPECTED_COUNTS.put("smartthermostat.json", new int[]{16, 56});
        EXPECTED_COUNTS.put("smartvacuum.json", new int[]{9, 33});
        EXPECTED_COUNTS.put("videoCamera.json", new int[]{1024, 10240});
        EXPECTED_COUNTS.put("washingMachine.json", new int[]{20, 57});
        EXPECTED_COUNTS.put("yeelight.json", new int[]{8, 16});
    }

    private static final class ModelFile {
        String fileName;
        final List<String[]> triples = new ArrayList<>();
        final Set<String> nodeIds = new HashSet<>();
    }

    private static final Map<String, ModelFile> MODELS = new HashMap<>();

    @BeforeClass
    public static void loadAllModels() throws IOException {
        assertTrue("model directory not found; run tests from the project root: "
                + MODEL_DIR.toAbsolutePath(), Files.isDirectory(MODEL_DIR));

        for (String fileName : EXPECTED_COUNTS.keySet()) {
            ModelFile model = new ModelFile();
            model.fileName = fileName;
            parseJsonModel(fileName, model);
            MODELS.put(fileName, model);
        }
    }

    private static void parseJsonModel(String fileName, ModelFile model) throws IOException {
        String content = new String(Files.readAllBytes(MODEL_DIR.resolve(fileName)), StandardCharsets.UTF_8);
        com.google.gson.JsonArray array = com.google.gson.JsonParser.parseString(content).getAsJsonArray();

        for (com.google.gson.JsonElement element : array) {
            com.google.gson.JsonObject edge = element.getAsJsonObject();
            String source = requiredString(edge, fileName, "source");
            String target = requiredString(edge, fileName, "target");
            String name = requiredString(edge, fileName, "name");
            model.triples.add(new String[]{source, target, name});
        }
    }

    private static String requiredString(com.google.gson.JsonObject edge, String fileName, String field) {
        com.google.gson.JsonElement value = edge.get(field);
        if (value == null || !value.isJsonPrimitive()) {
            fail(fileName + ": transition entry misses string field '" + field + "'");
        }
        return value.getAsString();
    }

    private static String nodeIdOf(String nodeString, String fileName) {
        Matcher matcher = NODE_ID_PATTERN.matcher(nodeString);
        assertTrue(fileName + ": node is not Node-encoded: " + nodeString, matcher.find());
        return matcher.group(1);
    }

    private Set<String> tripleKey(List<String[]> triples) {
        return triples.stream()
                .map(t -> t[0] + "->" + t[1] + ":" + t[2])
                .collect(Collectors.toSet());
    }

    @Test
    public void everyModelFileIsPresent() {
        for (String fileName : EXPECTED_COUNTS.keySet()) {
            assertTrue(fileName + " is missing from " + MODEL_DIR,
                    Files.isRegularFile(MODEL_DIR.resolve(fileName)));
            String dotName = dotNameFor(fileName);
            assertTrue(dotName + " is missing from " + MODEL_DIR,
                    Files.isRegularFile(MODEL_DIR.resolve(dotName)));
        }
    }

    @Test
    public void transitionsAreUnique() {
        for (ModelFile model : MODELS.values()) {
            Set<String> keys = tripleKey(model.triples);
            assertEquals(model.fileName + " contains duplicate transition entries",
                    model.triples.size(), keys.size());
        }
    }

    @Test
    public void nodeIdsFollowS0ToSnWithoutGaps() {
        for (ModelFile model : MODELS.values()) {
            for (String[] triple : model.triples) {
                model.nodeIds.add(nodeIdOf(triple[0], model.fileName));
                model.nodeIds.add(nodeIdOf(triple[1], model.fileName));
            }
            int max = -1;
            for (String nodeId : model.nodeIds) {
                int number = Integer.parseInt(nodeId.substring(1));
                assertTrue(model.fileName + ": node id out of sequence: " + nodeId, number >= 0);
                max = Math.max(max, number);
            }
            assertEquals(model.fileName + ": node ids are not contiguous S0..Sn",
                    model.nodeIds.size(), max + 1);
        }
    }

    @Test
    public void modelSizesMatchBaseline() {
        for (Map.Entry<String, int[]> entry : EXPECTED_COUNTS.entrySet()) {
            ModelFile model = MODELS.get(entry.getKey());
            int expectedNodes = entry.getValue()[0];
            int expectedTransitions = entry.getValue()[1];

            // recompute node ids (assertion order guarantees this ran, but stay independent)
            Set<String> nodeIds = new HashSet<>();
            for (String[] triple : model.triples) {
                nodeIds.add(nodeIdOf(triple[0], model.fileName));
                nodeIds.add(nodeIdOf(triple[1], model.fileName));
            }

            assertEquals(entry.getKey() + ": node count drifted from the golden baseline",
                    expectedNodes, nodeIds.size());
            assertEquals(entry.getKey() + ": transition count drifted from the golden baseline",
                    expectedTransitions, tripleKey(model.triples).size());
        }
    }

    @Test
    public void dotFilesMatchTheirJsonModels() throws IOException {
        for (ModelFile model : MODELS.values()) {
            String dotName = dotNameFor(model.fileName);
            String dotContent = new String(
                    Files.readAllBytes(MODEL_DIR.resolve(dotName)), StandardCharsets.UTF_8);

            Set<String> dotEdges = new HashSet<>();
            Matcher matcher = DOT_EDGE_PATTERN.matcher(dotContent);
            while (matcher.find()) {
                dotEdges.add(matcher.group(1) + "->" + matcher.group(2) + ":" + matcher.group(3));
            }

            Set<String> jsonEdges = new HashSet<>();
            for (String[] triple : model.triples) {
                jsonEdges.add(nodeIdOf(triple[0], model.fileName)
                        + "->" + nodeIdOf(triple[1], model.fileName) + ":" + triple[2]);
            }

            assertEquals(dotName + " does not describe the same transitions as " + model.fileName,
                    jsonEdges, dotEdges);
        }
    }

    @Test
    public void everyModelLoadsThroughTheRuntimeLoader() {
        // The ExecutionChecker loads each model exactly this way; keep the
        // convention (directed multigraph, Node-encoded endpoints) verifiable.
        for (String fileName : EXPECTED_COUNTS.keySet()) {
            Graph<Object, Object> loaded = Utils.loadGraphFromFile(
                    MODEL_DIR.resolve(fileName).toString());
            int expectedTransitions = EXPECTED_COUNTS.get(fileName)[1];
            assertEquals(fileName + ": loaded graph transition count differs",
                    expectedTransitions, loaded.edgeCount());
            assertEquals(fileName + ": loaded graph node count differs",
                    EXPECTED_COUNTS.get(fileName)[0], loaded.nodeCount());
        }
    }

    private static String dotNameFor(String jsonName) {
        String stem = jsonName.substring(0, jsonName.length() - ".json".length());
        return stem.replace("Real", "real") + "_output.dot";
    }
}
