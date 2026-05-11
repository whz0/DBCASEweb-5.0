package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateNode;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
    private static final Pattern PK_PATTERN = Pattern.compile("^__([a-zA-Z0-9_]+)__$");
    private static final Pattern NULLABLE_PATTERN = Pattern.compile("^\\*([a-zA-Z0-9_]+)\\*$");

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
        parseRestriction(diagram.restriction(), false, result);
        parseRestriction(diagram.lossRestriction(), true, result);
        parseRelationship(diagram.relationship(), result);
        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();
        Set<Node> allNodes =
                graph.vertexSet().stream()
                        .filter(n -> !n.isAttribute())
                        .collect(Collectors.toSet());

        StringBuilder relationships = new StringBuilder();
        StringBuilder restrictions = new StringBuilder();
        StringBuilder lostRestrictions = new StringBuilder();

        Set<String> visited = new HashSet<>();
        Set<String> usedRefs = new HashSet<>();

        for (Node node : allNodes) {
            if (NodeClassifier.isNMRel(node, graph)) {
                buildNMRel(node, graph, visited, relationships, restrictions, lostRestrictions);
            } else if (NodeClassifier.isEntity(node, graph)) {
                Set<Node> uniqueFks = NodeClassifier.hasUniqueFk(node, graph);
                if (!uniqueFks.isEmpty()) {
                    uniqueFks.forEach(
                            fk -> {
                                Node ref =
                                        graph.vertexSet().stream()
                                                .filter(n -> n.getName().equals(fk.getReference()))
                                                .findFirst()
                                                .orElse(null);
                                if (ref != null) {
                                    if (NodeClassifier.isTotalRel(node, ref, graph)) {
                                        buildTotalRel(
                                                node,
                                                fk,
                                                usedRefs,
                                                graph,
                                                visited,
                                                relationships,
                                                restrictions,
                                                lostRestrictions);
                                    } else {
                                        buildPartialRel(
                                                node,
                                                fk,
                                                usedRefs,
                                                graph,
                                                visited,
                                                relationships,
                                                restrictions,
                                                lostRestrictions);
                                    }
                                }
                            });
                } else {
                    buildPureEntity(node, graph, visited, relationships);
                }
            } else {
                relationships.append(node.getName()).append(" ???\n");
            }
        }

        return new LinkedHashMap<>(
                Map.of(
                        "relationship", relationships.toString(),
                        "restriction", restrictions.toString(),
                        "lossRestriction", lostRestrictions.toString()));
    }

    private void buildNMRel(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            StringBuilder relationship,
            StringBuilder restrictions,
            StringBuilder lostRestriction) {

        Graphs.successorListOf(graph, node).stream()
                .filter(n -> n.isAttribute() && n.isFk())
                .collect(Collectors.toSet())
                .forEach(
                        n -> {
                            Node ref =
                                    graph.vertexSet().stream()
                                            .filter(r -> r.getName().equals(n.getReference()))
                                            .findFirst()
                                            .orElse(null);

                            Node refAttr = Graphs.successorListOf(graph, n).getFirst();

                            assert ref != null;
                            restrictions
                                    .append(node.getName())
                                    .append(".")
                                    .append(n.getName())
                                    .append(" -> ")
                                    .append(ref.getName())
                                    .append(".")
                                    .append(refAttr.getName())
                                    .append("\n");
                        });
    }

    private String buildEntry(
            Node node, Graph<Node, Edge> graph, StringBuilder restrictions, boolean skipFk) {
        StringBuilder pks = new StringBuilder();
        StringBuilder others = new StringBuilder();
        for (Node attr : Graphs.successorListOf(graph, node)) {
            if (!attr.isAttribute()) {
                continue;
            }
            if (attr.isFk()) {
                if (skipFk) {
                    continue;
                }
                appendRestriction(attr, node, graph, restrictions);
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
                others.append(!attr.isNotNull() ? "*" + attr.getName() + "*" : attr.getName());
            }
        }
        if (!pks.isEmpty() && !others.isEmpty()) {
            return pks + ", " + others;
        }
        return pks.isEmpty() ? others.toString() : pks.toString();
    }

    private void appendRestrictions(Node node, Graph<Node, Edge> graph, StringBuilder target) {
        Graphs.successorListOf(graph, node).stream()
                .filter(a -> a.isAttribute() && a.isFk())
                .forEach(attr -> appendRestriction(attr, node, graph, target));
    }

    private void appendRestriction(
            Node attr, Node node, Graph<Node, Edge> graph, StringBuilder restrictions) {
        Node refNode =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals(attr.getReference()))
                        .findFirst()
                        .orElse(null);
        List<Node> successors = Graphs.successorListOf(graph, attr);
        if (successors.isEmpty() || refNode == null) {
            return;
        }
        Node refAttr = successors.getFirst();
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
                            Node attrRef = getOrCreateAttr(refAttrName, ref, diagram);

                            if (lost) {
                                attrSrc.setUnique(true);
                                Node unique =
                                        Graphs.successorListOf(diagram, ref).stream()
                                                .filter(
                                                        a ->
                                                                a.isAttribute()
                                                                        && a.getReference()
                                                                                .equals(srcName))
                                                .findFirst()
                                                .orElse(null);

                                if (unique != null) {
                                    unique.setUnique(true);
                                }
                            }

                            Auxiliary.addForeignAttr(attrSrc, src, refName, diagram);
                            Auxiliary.addPrimaryAttr(attrRef, ref, diagram);
                            Auxiliary.addEdge(attrSrc, attrRef, diagram);
                        });
    }

    private void buildTotalRel(
            Node node,
            Node fk,
            Set<String> usedRefs,
            Graph<Node, Edge> graph,
            Set<String> visited,
            StringBuilder relationship,
            StringBuilder restrictions,
            StringBuilder lostRestriction) {
        if (!visited.contains(node.getUuid())) {
            visited.add(node.getUuid());
            boolean refAlreadyUsed = usedRefs.contains(fk.getReference());
            if (!refAlreadyUsed) {
                usedRefs.add(node.getName());
            }
            String entry;
            if (refAlreadyUsed) {
                appendRestrictions(node, graph, lostRestriction);
                entry = buildEntry(node, graph, new StringBuilder(), true);
            } else {
                appendRestrictions(node, graph, restrictions);
                entry = buildEntry(node, graph, new StringBuilder(), false);
            }
            relationship.append(node.getName()).append(" (").append(entry).append(")\n");
        }
    }

    private void buildPartialRel(
            Node node,
            Node fk,
            Set<String> usedRefs,
            Graph<Node, Edge> graph,
            Set<String> visited,
            StringBuilder relationship,
            StringBuilder restrictions,
            StringBuilder lostRestriction) {
        if (!visited.contains(node.getUuid())) {
            visited.add(node.getUuid());
            boolean refAlreadyUsed = usedRefs.contains(fk.getReference());
            if (!refAlreadyUsed) {
                usedRefs.add(node.getName());
            }
            String entry;
            if (refAlreadyUsed) {
                appendRestrictions(node, graph, lostRestriction);
                entry = buildEntry(node, graph, new StringBuilder(), true);
            } else {
                entry = buildEntry(node, graph, restrictions, false);
            }
            relationship.append(node.getName()).append(" (").append(entry).append(")\n");
        }
    }

    private void buildPureEntity(
            Node node, Graph<Node, Edge> graph, Set<String> visited, StringBuilder relationship) {
        if (!visited.contains(node.getUuid())) {
            visited.add(node.getUuid());
            StringBuilder dummy = new StringBuilder();
            String entry = buildEntry(node, graph, dummy, false);
            relationship.append(node.getName()).append(" (").append(entry).append(")\n");
        }
    }
}
