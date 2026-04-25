package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateNode;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isAttribute;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

        result.edgeSet().forEach(System.out::println);

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();
        Set<Node> allNodes =
                graph.vertexSet().stream().filter(n -> !isAttribute(n)).collect(Collectors.toSet());

        StringBuilder relationships = new StringBuilder();
        StringBuilder restrictions = new StringBuilder();

        for (Node node : allNodes) {
            String attrList = buildEntry(node, graph, restrictions);
            relationships.append(node.getName()).append(" (").append(attrList).append(")\n");
        }

        return new LinkedHashMap<>(
                Map.of(
                        "relationship", relationships.toString(),
                        "restriction", restrictions.toString(),
                        "lossRestriction", ""));
    }

    private String buildEntry(Node node, Graph<Node, Edge> graph, StringBuilder restrictions) {
        StringBuilder attrList = new StringBuilder();

        System.out.println(node.getName());
        Graphs.neighborListOf(graph, node).forEach(System.out::println);

        Graphs.neighborListOf(graph, node)
                .forEach(
                        attr -> {
                            if (attr.isFk()) {
                                appendRestriction(attr, node, graph, restrictions);
                            }
                            if (!attrList.isEmpty()) {
                                attrList.append(", ");
                            }
                            attrList.append(
                                    attr.isPk() ? "__" + attr.getName() + "__" : attr.getName());
                        });
        return attrList.toString();
    }

    private void appendRestriction(
            Node attr, Node node, Graph<Node, Edge> graph, StringBuilder restrictions) {

        Node refNode =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals(attr.getReference()))
                        .findFirst()
                        .orElse(null);
        Node refAttr = Graphs.successorListOf(graph, attr).get(0);

        assert refNode != null;
        restrictions
                .append(node.getName())
                .append(".")
                .append(attr.getName())
                .append(" -> ")
                .append(refNode.getName())
                .append(".")
                .append(refAttr.getName())
                .append("\n");
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

        if (pk) {
            Auxiliary.addPrimaryAttr(attr, entity, diagram);
        } else {
            Auxiliary.addEdge(entity, attr, diagram);
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
                            Node ref = getOrCreateNode(refName, diagram);
                            Node attrSrc = getOrCreateAttr(srcAttrName, src, diagram);
                            Node attrRef = getOrCreateAttr(refAttrName, src, diagram);

                            Auxiliary.addForeignAttr(attrSrc, src, refName, diagram);
                            Auxiliary.addPrimaryAttr(attrRef, ref, diagram);
                            Auxiliary.addEdge(attrSrc, attrRef, diagram);
                        });
    }
}
