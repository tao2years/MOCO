package MOCO;

import graph.Graph;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExecutionCheckerTest {

    @Test
    public void loadOutEdgeApisGroupsApisByNodeContent() {
        Graph<Object, Object> graph = new Graph<>(true, true, false);
        String source = new Node("S0", "Device{state='off'}").toString();
        String target1 = new Node("S1", "Device{state='on'}").toString();
        String target2 = new Node("S2", "Device{state='ready'}").toString();

        graph.setEdge(source, target1, "turnOn", "turnOn");
        graph.setEdge(source, target2, "prepare", "prepare");

        Map<String, Set<String>> outEdgeApis = Utils.loadOutEdgeApis(graph);

        assertEquals(1, outEdgeApis.size());
        assertTrue(outEdgeApis.get("Device{state='off'}").contains("turnOn"));
        assertTrue(outEdgeApis.get("Device{state='off'}").contains("prepare"));
    }

    @Test
    public void getTargetStateReturnsTargetContentWhenEdgeMatches() {
        Graph<Object, Object> graph = new Graph<>(true, true, false);
        String source = new Node("S0", "Device{state='off'}").toString();
        String target = new Node("S1", "Device{state='on'}").toString();

        graph.setEdge(source, target, "turnOn", "turnOn");

        String result = ExecutionChecker.getTargetStateBasedOnBehaviourModels("Device{state='off'}", "turnOn", graph);

        assertEquals("Device{state='on'}", result);
    }

    @Test
    public void getTargetStateReturnsNullStringWhenNoEdgeMatches() {
        Graph<Object, Object> graph = new Graph<>(true, true, false);
        String source = new Node("S0", "Device{state='off'}").toString();
        String target = new Node("S1", "Device{state='on'}").toString();

        graph.setEdge(source, target, "turnOn", "turnOn");

        String result = ExecutionChecker.getTargetStateBasedOnBehaviourModels("Device{state='off'}", "turnOff", graph);

        assertEquals("null", result);
    }

    @Test
    public void calculateSolutionsPrefersPathEndingWithMessageApi() {
        Graph<Object, Object> graph = new Graph<>(true, true, false);
        String source = new Node("S0", "Device{state='off'}").toString();
        String middle = new Node("S1", "Device{state='ready'}").toString();
        String target = new Node("S2", "Device{state='on'}").toString();
        String done = new Node("S3", "Device{state='done'}").toString();

        graph.setEdge(source, middle, "prepare", "prepare");
        graph.setEdge(middle, target, "turnOn", "turnOn");
        graph.setEdge(target, done, "done", "done");

        List<String> result = ExecutionChecker.calculateSolutions(graph, "Device{state='off'}", "Device{state='on'}", new IoTSystem.Message("Device", "turnOn", new String[]{}));

        assertEquals(Arrays.asList("prepare", "turnOn"), result);
    }
}
