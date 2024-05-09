package it.univr.xplg.generator.utils;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TreeVisualizer {
    public void exportToDotFile(DirectedAcyclicGraph<UUID, DefaultEdge> graph, String filePath) {
        final DOTExporter<UUID, DefaultEdge> exporter = new DOTExporter<>(uuid -> "v" + uuid.toString().replace("-", ""));
        exporter.setVertexAttributeProvider((uuid) -> {
            final Map<String, Attribute> map = new HashMap<>();
            map.put("ID", DefaultAttribute.createAttribute(uuid.toString()));
            map.put("label", DefaultAttribute.createAttribute(uuid.toString()));

            return map;
        });

        try (final Writer writer = new FileWriter(filePath)) {
            exporter.exportGraph(graph, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
