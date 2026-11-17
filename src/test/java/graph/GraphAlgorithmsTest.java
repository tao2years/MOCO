package graph;

import graph.algorithms.Topsort;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the graph algorithms the MOCA pipeline depends on:
 * Tarjan SCCs, topological sort, cycle detection and connected components.
 * Assertions compare component membership as sets, because node iteration
 * order inside the graph is a HashMap artifact and must not affect results.
 */
public class GraphAlgorithmsTest {

    private Graph<Object, Object> graph;

    @Before
    public void setUp() {
        graph = new Graph<>(true, true, false);
    }

    private Set<Set<String>> asComponentSets(List<List<String>> components) {
        return components.stream()
                .map(HashSet::new)
                .collect(Collectors.toSet());
    }

    @Test
    public void tarjanFindsStronglyConnectedComponents() {
        // a <-> b (SCC), b -> c, c <-> d (SCC)
        graph.setEdge("a", "b");
        graph.setEdge("b", "a");
        graph.setEdge("b", "c");
        graph.setEdge("c", "d");
        graph.setEdge("d", "c");

        Set<Set<String>> components = asComponentSets(graph.tarjan());

        Set<Set<String>> expected = new HashSet<>();
        expected.add(new HashSet<>(java.util.Arrays.asList("a", "b")));
        expected.add(new HashSet<>(java.util.Arrays.asList("c", "d")));
        assertEquals(expected, components);
    }

    @Test
    public void tarjanOnAcyclicGraphReturnsSingletons() {
        graph.setEdge("a", "b");
        graph.setEdge("b", "c");

        Set<Set<String>> components = asComponentSets(graph.tarjan());

        Set<Set<String>> expected = new HashSet<>();
        expected.add(java.util.Collections.singleton("a"));
        expected.add(java.util.Collections.singleton("b"));
        expected.add(java.util.Collections.singleton("c"));
        assertEquals(expected, components);
    }

    @Test
    public void tarjanCoversEveryNodeOnce() {
        graph.setEdge("a", "b");
        graph.setEdge("b", "c");
        graph.setEdge("c", "a");
        graph.setEdge("c", "d");

        List<List<String>> components = graph.tarjan();
        int totalNodes = components.stream().mapToInt(List::size).sum();
        assertEquals(graph.nodeCount(), totalNodes);
    }

    @Test
    public void topsortOrdersDag() {
        // a -> b -> c and a -> c
        graph.setEdge("a", "b");
        graph.setEdge("b", "c");
        graph.setEdge("a", "c");

        List<String> order = graph.topsort();

        assertEquals(3, order.size());
        assertTrue(order.indexOf("a") < order.indexOf("b"));
        assertTrue(order.indexOf("b") < order.indexOf("c"));
        assertTrue(order.indexOf("a") < order.indexOf("c"));
    }

    @Test(expected = Topsort.CycleException.class)
    public void topsortThrowsOnCycle() {
        graph.setEdge("a", "b");
        graph.setEdge("b", "a");
        graph.topsort();
    }

    @Test
    public void isAcyclicTrueForDag() {
        graph.setEdge("a", "b");
        graph.setEdge("b", "c");
        assertTrue(graph.isAcyclic());
    }

    @Test
    public void isAcyclicFalseForCycle() {
        graph.setEdge("a", "b");
        graph.setEdge("b", "a");
        assertFalse(graph.isAcyclic());
    }

    @Test
    public void isAcyclicFalseForSelfLoop() {
        graph.setEdge("a", "a");
        assertFalse(graph.isAcyclic());
    }

    @Test
    public void findCyclesReturnsOnlyCyclicComponents() {
        // a <-> b (cycle), b -> c (acyclic tail)
        graph.setEdge("a", "b");
        graph.setEdge("b", "a");
        graph.setEdge("b", "c");

        Set<Set<String>> cycles = asComponentSets(graph.findCycles());

        Set<Set<String>> expected = new HashSet<>();
        expected.add(new HashSet<>(java.util.Arrays.asList("a", "b")));
        assertEquals(expected, cycles);
    }

    @Test
    public void findCyclesDetectsSelfLoop() {
        graph.setEdge("a", "a");
        graph.setEdge("a", "b");

        Set<Set<String>> cycles = asComponentSets(graph.findCycles());
        Set<Set<String>> expected = new HashSet<>();
        expected.add(java.util.Collections.singleton("a"));
        assertEquals(expected, cycles);
    }

    @Test
    public void findCyclesEmptyOnDag() {
        graph.setEdge("a", "b");
        assertTrue(graph.findCycles().isEmpty());
    }

    @Test
    public void componentsSplitDisconnectedGraphs() {
        // two disconnected pairs: a-b and c-d
        graph.setEdge("a", "b");
        graph.setEdge("c", "d");

        List<Graph<Object, Object>> components = graph.components();

        assertEquals(2, components.size());
        Set<Set<String>> nodeSets = components.stream()
                .map(c -> new HashSet<>(c.getNodes()))
                .collect(Collectors.toSet());
        Set<Set<String>> expected = new HashSet<>();
        expected.add(new HashSet<>(java.util.Arrays.asList("a", "b")));
        expected.add(new HashSet<>(java.util.Arrays.asList("c", "d")));
        assertEquals(expected, nodeSets);
    }
}
