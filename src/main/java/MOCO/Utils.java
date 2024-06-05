package MOCO;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class Utils {
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
}
