package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateNode;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedPseudograph;
import org.springframework.stereotype.Service;

@Service
public class LogicalDiagramStrategy implements DiagramStrategy<LogicalInput> {
    private static final Pattern PK_PATTERN = Pattern.compile("^__([a-zA-Z0-9_]+)__$");
    private static final Pattern NULLABLE_PATTERN = Pattern.compile("([a-zA-Z0-9_]+)\\*$");

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
        Graph<Node, Edge> result = new DirectedPseudograph<>(Edge.class);
        parseRestriction(diagram.restriction(), false, result);
        parseRestriction(diagram.lossRestriction(), true, result);
        parseRelationship(diagram.relationship(), result);
        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();
        List<Node> allNodes =
                graph.vertexSet().stream()
                        .filter(n -> !n.isAttribute())
                        .sorted(Comparator.comparing(Node::getName))
                        .collect(Collectors.toList());

        StringBuilder relationships = new StringBuilder();
        StringBuilder restrictions = new StringBuilder();
        StringBuilder lostRestrictions = new StringBuilder();

        for (Node node : allNodes) {
            buildRelationship(node, graph, relationships);
            buildRestrictions(node, graph, restrictions, lostRestrictions);
        }

        return new LinkedHashMap<>(
                Map.of(
                        "relationship", relationships.toString(),
                        "restriction", restrictions.toString(),
                        "lossRestriction", lostRestrictions.toString()));
    }

    private void buildRelationship(Node node, Graph<Node, Edge> graph, StringBuilder out) {
        StringBuilder pks = new StringBuilder();
        StringBuilder others = new StringBuilder();

        for (Node attr : Graphs.successorListOf(graph, node)) {
            if (!attr.isAttribute()) {
                continue;
            }

            if (attr.isFk() && attr.isUnique() && attr.isTotal() && !attr.isNotNull()) {
                continue;
            }

            if (attr.isPk()) {
                if (!pks.isEmpty()) {
                    pks.append(", ");
                }
                pks.append("__").append(attr.getName()).append("__");
            } else {
                if (!others.isEmpty()) {
                    others.append(", ");
                }
                others.append(!attr.isNotNull() ? attr.getName() + "*" : attr.getName());
            }
        }

        StringBuilder attributes = new StringBuilder();
        if (!pks.isEmpty() && !others.isEmpty()) {
            attributes.append(pks).append(", ").append(others);
        } else {
            attributes.append(pks.isEmpty() ? others.toString() : pks.toString());
        }

        if (!attributes.isEmpty()) {
            out.append(node.getName()).append(" ( ").append(attributes).append(")\n");
        }
    }

    private void buildRestrictions(
            Node node,
            Graph<Node, Edge> graph,
            StringBuilder restrictions,
            StringBuilder lostRestrictions) {

        // Group FK attributes by the table they reference
        Map<String, List<Node>> fksByRef = new java.util.LinkedHashMap<>();
        for (Node attr : Graphs.successorListOf(graph, node)) {
            if (!attr.isAttribute() || !attr.isFk() || attr.getReference() == null) {
                continue;
            }
            fksByRef.computeIfAbsent(attr.getReference(), k -> new ArrayList<>()).add(attr);
        }

        for (Map.Entry<String, List<Node>> entry : fksByRef.entrySet()) {
            List<Node> fks = entry.getValue();
            Node firstAttr = fks.get(0);

            if (firstAttr.isPk() && firstAttr.isNotNull()) {
                appendRestriction(fks, node, graph, restrictions);
                appendTotalParticipation(fks, node, graph, lostRestrictions);
            } else if (firstAttr.isUnique()) {
                appendRestriction(fks, node, graph, restrictions);
                boolean refExists =
                        graph.vertexSet().stream()
                                .anyMatch(
                                        n ->
                                                !n.isAttribute()
                                                        && n.getName()
                                                                .equals(firstAttr.getReference()));
                if (firstAttr.isNotNull() && refExists) {
                    appendOneOneLost(fks, node, graph, lostRestrictions);
                }
            } else {
                appendRestriction(fks, node, graph, restrictions);
            }
        }
    }

    /**
     * Formats the left or right side of a restriction. Single FK -> "table.attr" Multiple FKs ->
     * "table.{attr1,attr2}"
     */
    private String formatSide(String tableName, List<String> attrNames) {
        if (attrNames.size() == 1) {
            return tableName + "." + attrNames.get(0);
        }
        return tableName + ".{" + String.join(",", attrNames) + "}";
    }

    private void appendRestriction(
            List<Node> attrs, Node node, Graph<Node, Edge> graph, StringBuilder restrictions) {
        String ref = attrs.get(0).getReference();
        Node refNode =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals(ref))
                        .findFirst()
                        .orElse(null);
        if (refNode == null) {
            return;
        }
        List<String> srcNames = new ArrayList<>();
        List<String> refNames = new ArrayList<>();
        for (Node attr : attrs) {
            List<Node> successors = Graphs.successorListOf(graph, attr);
            if (successors.isEmpty()) {
                continue;
            }
            srcNames.add(attr.getName());
            refNames.add(successors.getFirst().getName());
        }
        if (srcNames.isEmpty()) {
            return;
        }
        restrictions
                .append(formatSide(node.getName(), srcNames))
                .append(" -> ")
                .append(formatSide(refNode.getName(), refNames))
                .append("\n");
    }

    private void appendTotalParticipation(
            List<Node> attrs, Node node, Graph<Node, Edge> graph, StringBuilder lostRestrictions) {
        String ref = attrs.get(0).getReference();
        List<String> srcNames = new ArrayList<>();
        List<String> refNames = new ArrayList<>();
        for (Node attr : attrs) {
            List<Node> successors = Graphs.successorListOf(graph, attr);
            if (successors.isEmpty()) {
                continue;
            }
            srcNames.add(attr.getName());
            refNames.add(successors.getFirst().getName());
        }
        if (srcNames.isEmpty()) {
            return;
        }
        lostRestrictions
                .append(formatSide(ref, refNames))
                .append(" -> ")
                .append(formatSide(node.getName(), srcNames))
                .append("\n");
    }

    private void appendOneOneLost(
            List<Node> attrs, Node node, Graph<Node, Edge> graph, StringBuilder lostRestrictions) {
        String ref = attrs.get(0).getReference();
        List<String> srcNames = new ArrayList<>();
        List<String> refNames = new ArrayList<>();
        for (Node attr : attrs) {
            List<Node> successors = Graphs.successorListOf(graph, attr);
            if (successors.isEmpty()) {
                continue;
            }
            srcNames.add(attr.getName());
            refNames.add(successors.getFirst().getName());
        }
        if (srcNames.isEmpty()) {
            return;
        }
        lostRestrictions
                .append("∀ ")
                .append(formatSide(ref, refNames))
                .append(" ∃ ")
                .append(formatSide(node.getName(), srcNames))
                .append("  (1:1 participation of ")
                .append(ref)
                .append(")\n");
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
        Matcher pkMatcher = PK_PATTERN.matcher(attribute);
        if (pkMatcher.find()) {
            Node attr = getOrCreateAttr(pkMatcher.group(1), entity, diagram);
            Auxiliary.addPrimaryAttr(attr, entity, diagram);
            return;
        }
        Matcher nullMatcher = NULLABLE_PATTERN.matcher(attribute);
        if (nullMatcher.find()) {
            Node attr = getOrCreateAttr(nullMatcher.group(1), entity, diagram);
            attr.setNotNull(false);
            Auxiliary.addEdge(entity, attr, diagram);
        } else {
            Node attr = getOrCreateAttr(attribute, entity, diagram);
            attr.setNotNull(true);
            Auxiliary.addEdge(entity, attr, diagram);
        }
    }

    private void parseRestriction(String restriction, boolean lost, Graph<Node, Edge> diagram) {
        restriction
                .lines()
                .filter(line -> !line.isBlank())
                .filter(line -> line.contains("->") && line.contains("."))
                .forEach(
                        line -> {
                            String[] parts = line.split("->");
                            if (parts.length < 2) {
                                return;
                            }
                            String[] srcParts = parts[0].trim().split("\\.");
                            String[] refParts = parts[1].trim().split("\\.");
                            if (srcParts.length < 2 || refParts.length < 2) {
                                return;
                            }
                            String srcName = srcParts[0].trim();
                            String srcAttrName = srcParts[1].trim();
                            String refName = refParts[0].trim();
                            String refAttrName = refParts[1].trim();
                            Node src = getOrCreateNode(srcName, diagram);
                            Node ref = getOrCreateNode(refName, diagram);
                            Node attrSrc = getOrCreateAttr(srcAttrName, src, diagram);
                            Node attrRef = getOrCreateAttr(refAttrName, ref, diagram);

                            if (lost) {
                                attrSrc.setUnique(true);
                                Graphs.successorListOf(diagram, ref).stream()
                                        .filter(
                                                a ->
                                                        a.isAttribute()
                                                                && a.getReference() != null
                                                                && a.getReference().equals(srcName))
                                        .findFirst()
                                        .ifPresent(unique -> unique.setUnique(true));
                            }

                            Auxiliary.addForeignAttr(attrSrc, src, refName, diagram);
                            Auxiliary.addPrimaryAttr(attrRef, ref, diagram);
                            Auxiliary.addEdge(attrSrc, attrRef, diagram);
                        });
    }
}
