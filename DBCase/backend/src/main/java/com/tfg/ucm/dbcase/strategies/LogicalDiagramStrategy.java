package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.*;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isAttribute;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isForeignKey;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedMultigraph;
import org.springframework.stereotype.Service;

@Service
public class LogicalDiagramStrategy implements DiagramStrategy<LogicalInput> {
    private static final Pattern PK_PATTERN = Pattern.compile("^__([a-zA-Z]+)__$");

    @Override
    public DiagramType getType() {
        return DiagramType.LOGICAL;
    }

    @Override
    public Class<LogicalInput> getInputType() {
        return LogicalInput.class;
    }

    @Override
    public Diagram generate(LogicalInput diagram) {

        Graph<Node, Edge> result = new DirectedMultigraph<>(Edge.class);

        parseRestriction(diagram.restriction(), result);
        parseRestriction(diagram.lossRestriction(), result);
        parseRelationship(diagram.relationship(), result);

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();
        List<Node> allNodes = graph.vertexSet().stream().filter(n -> !isAttribute(n)).toList();

        StringBuilder relationships = new StringBuilder();
        StringBuilder restrictions = new StringBuilder();

        for (Node node : allNodes) {
            relationships.append(buildEntry(node, graph, restrictions));
        }

        return new LinkedHashMap<>(
               Map.of(
                        "relationship", relationships.toString(),
                        "restriction", restrictions.toString(),
                        "lossRestriction", ""));
    }

    private String buildEntry(
            Node node,
            Graph<Node, Edge> graph,
            StringBuilder restrictions) {
        StringBuilder attrList = new StringBuilder();

        graph.vertexSet().stream()
                .filter(n -> isAttribute(n) && graph.containsEdge(node, n))
                .forEach(attr -> appendAttr(attr, node, graph, attrList, restrictions));

        List<Node> fks = graph.vertexSet().stream().filter(Node::isFk).toList();

        for (Node fk : fks) {
            if (!attrList.isEmpty()) {
                attrList.append(", ");
            }
            attrList.append(fk.getName());

            Node attrRef = Graphs.successorListOf(graph, fk).get(0);

            restrictions
                    .append(node.getName())
                    .append(".")
                    .append(fk.getName())
                    .append(" -> ")
                    .append(fk.getReference())
                    .append(".")
                    .append(attrRef.getName())
                    .append("\n");
        }

        return attrList.isEmpty() ? "" : node.getName() + " (" + attrList + ")\n";
    }

    private void appendAttr(
            Node attr,
            Node table,
            Graph<Node, Edge> graph,
            StringBuilder attrList,
            StringBuilder restrictions) {
        String name =
                attr.isFk()
                        ? graph.getAllEdges(table, attr).stream()
                                .map(NodeClassifier::getFkAttrName)
                                .filter(java.util.Objects::nonNull)
                                .findFirst()
                                .orElse(attr.getName())
                        : attr.getName();
        if (!attrList.isEmpty()) {
            attrList.append(", ");
        }
        attrList.append(attr.isPk() && !attr.isFk() ? "__" + name + "__" : name);
        if (isForeignKey(attr, table)) {
            restrictions
                    .append(table.getName())
                    .append(".")
                    .append(name)
                    .append(" -> ")
                    .append(attr.getReference())
                    .append(".")
                    .append(name)
                    .append("\n");
        }
    }

    private void parseRelationship(String relationship, Graph<Node, Edge> diagram) {
        relationship
                .lines()
                .filter(line -> !line.isBlank())
                .forEach(
                        line -> {
                            String[] parts = line.split(" ", 2);
                            String name = parts[0].trim();
                            Node entity = getOrCreateNode(name, diagram);
                            String[] attributes = parts[1].replaceAll("[()]", "").split(",");
                            Stream.of(attributes)
                                    .forEach(attr -> addAttribute(entity, attr.trim(), diagram));
                        });
    }

    private void addAttribute(Node entity, String attribute, Graph<Node, Edge> diagram) {
        Matcher matcher = PK_PATTERN.matcher(attribute);
        boolean pk = matcher.find();
        String attrName = pk ? matcher.group(1) : attribute;

        Node attr = getOrCreateAttr(attrName, entity, diagram);

        if(pk) {
            Auxiliary.addPrimaryAttr(attr, entity, diagram);
        }
        else {
            Auxiliary.addEdge(entity, attr, diagram);;
        }
    }

    private void parseRestriction(String restriction, Graph<Node, Edge> diagram) {
        restriction
                .lines()
                .filter(line -> !line.isBlank())
                .forEach(
                        line -> {
                            String[] parts = line.split("->");
                            String[] srcParts = parts[0].split("\\.");
                            String[] refParts = parts[1].split("\\.");

                            String srcName = srcParts[0].trim();
                            String srcAttrName = srcParts[1].trim();
                            String refName = refParts[0].trim();
                            String refAttrName = refParts[1].trim();
                            Node src = getOrCreateNode(srcName, diagram);
                            Node ref = getOrCreateNode(srcName, diagram);
                            Node attrSrc = getOrCreateAttr(srcAttrName, src, diagram);
                            Node attrRef = getOrCreateAttr(refAttrName, src, diagram);

                            Auxiliary.addForeignAttr(attrSrc, src, refName, diagram);
                            Auxiliary.addPrimaryAttr(attrRef, ref, diagram);
                            Auxiliary.addEdge(attrSrc, attrRef, diagram);
                        });
    }
}
