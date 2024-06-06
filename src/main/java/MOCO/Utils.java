package MOCO;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import graph.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Utils {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void generateDotFile(Set<Edge> dotSet, int numNodes, String filePath) {
        if (dotSet == null) {
            throw new IllegalArgumentException("dotSet cannot be null");
        }

        StringBuilder dotContent = new StringBuilder();
        dotContent.append("digraph g {\n\n");

        for (Edge edge : dotSet) {
            if (edge.getSource() == null || edge.getTarget() == null || edge.getApi() == null) {
                System.err.println("Edge contains null values: " + edge);
                continue;
            }
            dotContent.append("\t")
                    .append(edge.getSource())
                    .append(" -> ")
                    .append(edge.getTarget())
                    .append(" [label=\"")
                    .append(edge.getApi())
                    .append("\"];\n");
        }

        dotContent.append("\n}\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(dotContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveEdgesToFile(Collection<graph.Edge> edges, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            new Gson().toJson(edges, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Graph<Object, Object> loadGraphFromFile(String fileName) {
        Graph<Object, Object> behaviorGraph = new Graph<>(true, true, false);
        try (FileReader reader = new FileReader(fileName)) {
            Type edgeType = new TypeToken<Collection<graph.Edge>>(){}.getType();
            Collection<graph.Edge> edges = new Gson().fromJson(reader, edgeType);
            for (graph.Edge edge : edges) {
                behaviorGraph.setEdge(edge.getSource(), edge.getTarget(), edge.getName(), edge.getName());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return behaviorGraph;
    }

    public static Map<String, Set<String>> loadOutEdgeApis(Graph<Object, Object> behaviorGraph){
        Map<String, Set<String>> outEdgeApis = new HashMap<>();
        Collection<graph.Edge> edges = behaviorGraph.getEdges();
        edges.forEach(edge ->{
            Node source = Node.fromString(edge.getSource());
            String nodeContent = source.getContent();
            if (!outEdgeApis.containsKey(nodeContent)){
                outEdgeApis.put(nodeContent, new HashSet<>());
            }
            outEdgeApis.get(nodeContent).add(edge.getName());
        });
        return outEdgeApis;
    }

    public static void main(String[] args) {
        String fileName = "src/main/java/MOCO/modelFiles/gateway.json";
        Utils utils = new Utils();
        Graph<Object, Object> gateway_behaviorGraph = utils.loadGraphFromFile(fileName);
        Collection<graph.Edge> edges = gateway_behaviorGraph.getEdges();
        edges.forEach(System.out::println);

        Map<String, Set<String>> outEdgeApis = loadOutEdgeApis(gateway_behaviorGraph);

        for (Map.Entry<String, Set<String>> entry : outEdgeApis.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
