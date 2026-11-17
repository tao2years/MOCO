package graph;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression tests for the {@link Graph} library that MOCO's behavior models
 * are built on. They pin down the structural invariants the Learner and the
 * ExecutionChecker rely on: node/edge bookkeeping, incident edge queries and
 * the multigraph semantics used by every learned model file.
 */
public class GraphTest {

    private Graph<Object, Object> graph;

    @Before
    public void setUp() {
        graph = new Graph<>(true, true, false);
    }

    @Test
    public void newGraphIsEmpty() {
        assertEquals(0, graph.nodeCount());
        assertEquals(0, graph.edgeCount());
        assertTrue(graph.getNodes().isEmpty());
        assertTrue(graph.getEdges().isEmpty());
    }

    @Test
    public void setNodeThenHasNode() {
        graph.setNode("a");
        assertTrue(graph.hasNode("a"));
        assertFalse(graph.hasNode("b"));
        assertEquals(1, graph.nodeCount());
    }

    @Test
    public void setNodeTwiceDoesNotDuplicate() {
        graph.setNode("a");
        graph.setNode("a");
        assertEquals(1, graph.nodeCount());
    }

    @Test
    public void setNodeWithReplacesValue() {
        graph.setNode("a", "first");
        graph.setNode("a", "second");
        assertEquals("second", graph.getNode("a"));
        assertEquals(1, graph.nodeCount());
    }

    @Test
    public void setNodeWithoutValueKeepsExisting() {
        graph.setNode("a", "first");
        graph.setNode("a");
        assertEquals("first", graph.getNode("a"));
    }

    @Test
    public void setNodesAddsAll() {
        graph.setNodes(java.util.Arrays.asList("a", "b", "c"));
        assertEquals(3, graph.nodeCount());
        assertTrue(graph.hasNode("a"));
        assertTrue(graph.hasNode("b"));
        assertTrue(graph.hasNode("c"));
    }

    @Test
    public void removeNodeDropsIncidentEdges() {
        graph.setEdge("a", "b", "ab");
        graph.setEdge("b", "c", "bc");

        graph.removeNode("b");

        assertFalse(graph.hasNode("b"));
        assertEquals(2, graph.nodeCount());
        assertEquals(0, graph.edgeCount());
        assertFalse(graph.hasEdge("a", "b"));
        assertFalse(graph.hasEdge("b", "c"));
        // the untouched nodes survive
        assertTrue(graph.hasNode("a"));
        assertTrue(graph.hasNode("c"));
    }

    @Test
    public void removeUnknownNodeIsNoOp() {
        graph.setNode("a");
        graph.removeNode("nope");
        assertEquals(1, graph.nodeCount());
    }

    @Test
    public void setEdgeCreatesNodesAutomatically() {
        graph.setEdge("a", "b", "label");

        assertEquals(2, graph.nodeCount());
        assertEquals(1, graph.edgeCount());
        assertEquals("label", graph.getEdge("a", "b"));
        assertTrue(graph.hasEdge("a", "b"));
    }

    @Test
    public void setEdgeTwiceUpdatesLabel() {
        graph.setEdge("a", "b", "first");
        graph.setEdge("a", "b", "second");

        assertEquals(1, graph.edgeCount());
        assertEquals("second", graph.getEdge("a", "b"));
    }

    @Test
    public void selfLoopIsAllowed() {
        graph.setEdge("a", "a", "loop");
        assertEquals(1, graph.edgeCount());
        assertTrue(graph.hasEdge("a", "a"));
    }

    @Test
    public void multigraphKeepsNamedEdgesApart() {
        graph.setEdge("a", "b", "one", "e1");
        graph.setEdge("a", "b", "two", "e2");

        assertEquals(2, graph.edgeCount());
        assertEquals("one", graph.getEdge("a", "b", "e1"));
        assertEquals("two", graph.getEdge("a", "b", "e2"));
    }

    @Test(expected = IllegalOperationException.class)
    public void namedEdgeRejectedInNonMultigraph() {
        Graph<Object, Object> simple = new Graph<>(true, false, false);
        simple.setEdge("a", "b", null, "name");
    }

    @Test
    public void removeEdgeDecrementsCounts() {
        graph.setEdge("a", "b", "ab");
        graph.setEdge("b", "c", "bc");

        graph.removeEdge("a", "b");

        assertEquals(1, graph.edgeCount());
        assertFalse(graph.hasEdge("a", "b"));
        assertTrue(graph.hasEdge("b", "c"));
        assertEquals(3, graph.nodeCount());
    }

    @Test
    public void removeUnknownEdgeIsNoOp() {
        graph.setEdge("a", "b", "ab");
        graph.removeEdge("x", "y");
        assertEquals(1, graph.edgeCount());
    }

    @Test
    public void getEdgeOfUnknownPairIsNull() {
        graph.setNode("a");
        assertNull(graph.getEdge("a", "b"));
    }

    @Test
    public void sourcesAndSinks() {
        // a -> b -> c, a -> c
        graph.setEdge("a", "b");
        graph.setEdge("b", "c");
        graph.setEdge("a", "c");

        assertEquals(java.util.Collections.singleton("a"), graph.getSources());
        assertEquals(java.util.Collections.singleton("c"), graph.getSinks());
    }

    @Test
    public void predecessorsAndSuccessors() {
        graph.setEdge("a", "b");
        graph.setEdge("b", "c");

        assertEquals(java.util.Collections.singleton("a"), graph.predecessors("b"));
        assertEquals(java.util.Collections.singleton("c"), graph.successors("b"));
        assertTrue(graph.predecessors("a").isEmpty());
        assertTrue(graph.successors("c").isEmpty());
    }

    @Test
    public void neighborsUnionsBothDirections() {
        graph.setEdge("a", "b");
        graph.setEdge("c", "b");

        Set<String> neighbors = graph.neighbors("b").stream().collect(Collectors.toSet());
        assertEquals(new java.util.HashSet<>(java.util.Arrays.asList("a", "c")), neighbors);
    }

    @Test
    public void inAndOutEdgesFilterByNeighbour() {
        graph.setEdge("a", "b", "ab");
        graph.setEdge("c", "b", "cb");

        Collection<Edge> inB = graph.inEdges("b");
        assertEquals(2, inB.size());

        Collection<Edge> inBFromA = graph.inEdges("b", "a");
        assertEquals(1, inBFromA.size());
        assertEquals("a", inBFromA.iterator().next().getSource());
        assertEquals("b", inBFromA.iterator().next().getTarget());
    }

    @Test
    public void nodeEdgesCombinesInAndOut() {
        graph.setEdge("a", "b");
        graph.setEdge("b", "c");

        assertEquals(2, graph.nodeEdges("b").size());
    }

    @Test
    public void queriesOnUnknownNodeReturnEmpty() {
        assertTrue(graph.inEdges("nope").isEmpty());
        assertTrue(graph.outEdges("nope").isEmpty());
        assertTrue(graph.predecessors("nope").isEmpty());
        assertTrue(graph.successors("nope").isEmpty());
    }

    @Test
    public void setPathCreatesChain() {
        graph.setPath(java.util.Arrays.asList("a", "b", "c"));

        assertEquals(3, graph.nodeCount());
        assertEquals(2, graph.edgeCount());
        assertTrue(graph.hasEdge("a", "b"));
        assertTrue(graph.hasEdge("b", "c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNullPathThrows() {
        graph.setPath((java.util.Collection<String>) null);
    }

    @Test
    public void filterNodesKeepsInducedEdges() {
        graph.setEdge("a", "b", "ab");
        graph.setEdge("b", "c", "bc");
        graph.setEdge("a", "c", "ac");

        Graph<Object, Object> filtered = graph.filterNodes(id -> !id.equals("b"));

        assertEquals(2, filtered.nodeCount());
        assertEquals(1, filtered.edgeCount());
        assertTrue(filtered.hasEdge("a", "c"));
        assertFalse(filtered.hasEdge("a", "b"));
        assertFalse(filtered.hasEdge("b", "c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void filterNodesWithNullFilterThrows() {
        graph.filterNodes(null);
    }

    @Test
    public void isLeafForDirectedGraphUsesSuccessors() {
        graph.setEdge("a", "b");
        assertTrue(graph.isLeaf("b"));
        assertFalse(graph.isLeaf("a"));
    }

    @Test
    public void directedFlagExposed() {
        // setUp() builds a directed multigraph, like every learned model graph
        assertTrue(graph.isDirected());
        assertTrue(graph.isMultiGraph());
        assertFalse(graph.isCompound());
        assertFalse(new Graph<>(false, true, true).isDirected());
        assertTrue(new Graph<>(false, true, true).isCompound());
    }

    @Test
    public void edgeEqualityIsByContent() {
        Edge first = new Edge("a", "b", "name");
        Edge second = new Edge("a", "b", "name");
        Edge other = new Edge("a", "b", "other");

        assertEquals(first, second);
        assertFalse(first.equals(other));
        assertFalse(first.equals("not-an-edge"));
        assertEquals(new Edge("a", "b"), new Edge("a", "b"));
    }

    @Test
    public void edgeGetters() {
        Edge edge = new Edge("a", "b", "name");
        assertEquals("a", edge.getSource());
        assertEquals("b", edge.getTarget());
        assertEquals("name", edge.getName());
        assertNull(new Edge("a", "b").getName());
    }

    @Test
    public void utilsIsEmpty() {
        assertTrue(Utils.isEmpty(null));
        assertTrue(Utils.isEmpty(""));
        assertFalse(Utils.isEmpty("x"));
    }
}
