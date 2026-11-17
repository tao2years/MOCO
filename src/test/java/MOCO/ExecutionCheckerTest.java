package MOCO;

import IoTSystem.Message;
import graph.Graph;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Characterization tests for ExecutionChecker: preCheck legality decisions and
 * target-state lookup against the checked-in behavior models.
 */
public class ExecutionCheckerTest {

    private static Map<String, Set<String>> yeelightOutApis;
    private static Map<String, Set<String>> emptyApis;
    private static final String YEELIGHT_OFF =
            "Yeelight{'powerOn':false, 'brightness':false, 'rgb':false}";
    private static final String YEELIGHT_ON =
            "Yeelight{'powerOn':true, 'brightness':false, 'rgb':false}";

    @BeforeClass
    public static void setUp() {
        Graph<Object, Object> graph =
                Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/yeelight.json");
        yeelightOutApis = Utils.loadOutEdgeApis(graph);
        emptyApis = new HashMap<>();
    }

    @Test
    public void invalidCurrentStateAlwaysFailsPreCheck() {
        Message msg = new Message("Yeelight", "turnOn", new String[]{});
        assertFalse(ExecutionChecker.preCheck(msg, "Invalid",
                emptyApis, emptyApis, emptyApis, emptyApis, emptyApis));
    }

    @Test
    public void unknownDeviceTypeFailsPreCheck() {
        Message msg = new Message("Unknown", "turnOn", new String[]{});
        assertFalse(ExecutionChecker.preCheck(msg, YEELIGHT_OFF,
                emptyApis, emptyApis, emptyApis, emptyApis, emptyApis));
    }

    @Test
    public void knownApiFromKnownStatePassesPreCheck() {
        Message msg = new Message("Yeelight", "turnOn", new String[]{});
        assertTrue(ExecutionChecker.preCheck(msg, YEELIGHT_OFF,
                emptyApis, yeelightOutApis, emptyApis, emptyApis, emptyApis));
    }

    @Test
    public void preCheckUsesContainsSoPrefixApiNamesAlsoMatch() {
        // Documented current behavior: contains() semantics, not exact equality
        Message msg = new Message("Yeelight", "turn", new String[]{});
        assertTrue(ExecutionChecker.preCheck(msg, YEELIGHT_OFF,
                emptyApis, yeelightOutApis, emptyApis, emptyApis, emptyApis));
    }

    @Test
    public void getTargetStateBasedOnBehaviourModelsResolvesTransition() {
        Graph<Object, Object> graph =
                Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/yeelight.json");
        // Edge names carry API metadata ("turnOn|true|false") so a bare API
        // name like "turnOn" never equals the stored name; the lookup only
        // succeeds when the full name string is passed.
        String target = ExecutionChecker.getTargetStateBasedOnBehaviourModels(
                YEELIGHT_OFF, "turnOn|true|false", graph);
        assertEquals(YEELIGHT_ON, target);
    }

    @Test
    public void getTargetStateWithBareApiNameMissesBecauseOfNameMetadata() {
        // Documented current behavior: callers pass the bare API name, while
        // the stored edge name is "api|preemptive|humanIntervention".
        Graph<Object, Object> graph =
                Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/yeelight.json");
        assertEquals("null", ExecutionChecker.getTargetStateBasedOnBehaviourModels(
                YEELIGHT_OFF, "turnOn", graph));
    }

    @Test
    public void getTargetStateBasedOnBehaviourModelsFallsBackToNull() {
        Graph<Object, Object> graph =
                Utils.loadGraphFromFile("src/main/java/MOCO/modelFiles/yeelight.json");
        assertEquals("null", ExecutionChecker.getTargetStateBasedOnBehaviourModels(
                YEELIGHT_OFF, "noSuchApi", graph));
    }
}
