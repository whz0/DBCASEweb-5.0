package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreate;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isAttribute;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isForeignKey;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.Multigraph;
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

        Graph<Node, Edge> result = new Multigraph<>(Edge.class);

        parseRestriction(diagram.restriction(), result);
        parseRelationship(diagram.relationship(), result);
        parseLossRestriction(diagram.lossRestriction(), result);

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        StringBuilder relationshipBuilder = new StringBuilder();
        StringBuilder restrictionBuilder = new StringBuilder();

        Graph<Node, Edge> graph = diagram.getDiagram();

        java.util.Map<Node, java.util.List<FkInjection>> injections = new java.util.HashMap<>();

        List<Node> tableNodes = graph.vertexSet().stream().filter(n -> !isAttribute(n)).toList();

        for (Node node : tableNodes) {
            if (!NodeClassifier.isRelationship(node, graph)) {
                continue;
            }

            NodeClassifier.RelationshipKind kind = NodeClassifier.classify(node, graph);
            List<Edge> fkEdges = NodeClassifier.getFkEdges(node, graph);
            if (fkEdges.size() != 2 || kind == NodeClassifier.RelationshipKind.NM) {
                continue;
            }

            Edge edgeA = fkEdges.get(0);
            Edge edgeB = fkEdges.get(1);
            Node attrA = Graphs.getOppositeVertex(graph, edgeA, node);
            Node attrB = Graphs.getOppositeVertex(graph, edgeB, node);

            Node entityA =
                    tableNodes.stream()
                            .filter(n -> n.getName().equals(attrA.getReference()))
                            .findFirst()
                            .orElse(null);
            Node entityB =
                    tableNodes.stream()
                            .filter(n -> n.getName().equals(attrB.getReference()))
                            .findFirst()
                            .orElse(null);
            if (entityA == null || entityB == null) {
                continue;
            }

            boolean totalA = "1".equals(edgeA.getCardinalityMin());
            boolean totalB = "1".equals(edgeB.getCardinalityMin());

            if (kind == NodeClassifier.RelationshipKind.ONE_TO_ONE) {
                injections
                        .computeIfAbsent(entityA, k -> new java.util.ArrayList<>())
                        .add(
                                new FkInjection(
                                        NodeClassifier.getFkAttrName(edgeB),
                                        entityB.getName(),
                                        totalA || totalB));
            } else {
                Node nSideEntity =
                        (kind == NodeClassifier.RelationshipKind.ONE_TO_N) ? entityB : entityA;
                Node oneSideEntity =
                        (kind == NodeClassifier.RelationshipKind.ONE_TO_N) ? entityA : entityB;
                boolean totalNSide =
                        (kind == NodeClassifier.RelationshipKind.ONE_TO_N) ? totalB : totalA;
                injections
                        .computeIfAbsent(nSideEntity, k -> new java.util.ArrayList<>())
                        .add(
                                new FkInjection(
                                        NodeClassifier.getFkAttrName(
                                                kind == NodeClassifier.RelationshipKind.ONE_TO_N
                                                        ? edgeA
                                                        : edgeB),
                                        oneSideEntity.getName(),
                                        totalNSide));
            }
        }

        for (Node startNode : tableNodes) {
            if (NodeClassifier.isRelationship(startNode, graph)) {
                NodeClassifier.RelationshipKind kind = NodeClassifier.classify(startNode, graph);
                if (kind != NodeClassifier.RelationshipKind.NM) {
                    continue;
                }
            }

            StringBuilder attrList = new StringBuilder();

            graph.vertexSet().stream()
                    .filter(n -> isAttribute(n) && graph.containsEdge(startNode, n))
                    .forEach(
                            attr -> {
                                String displayName =
                                        attr.isFk()
                                                ? graph.getAllEdges(startNode, attr).stream()
                                                        .map(e -> NodeClassifier.getFkAttrName(e))
                                                        .filter(n -> n != null)
                                                        .findFirst()
                                                        .orElse(attr.getName())
                                                : attr.getName();
                                if (!attrList.isEmpty()) {
                                    attrList.append(", ");
                                }
                                attrList.append(
                                        attr.isPk() && !attr.isFk()
                                                ? "__" + displayName + "__"
                                                : displayName);
                                if (isForeignKey(attr, startNode)) {
                                    restrictionBuilder
                                            .append(startNode.getName())
                                            .append(".")
                                            .append(displayName)
                                            .append(" -> ")
                                            .append(attr.getReference())
                                            .append(".")
                                            .append(displayName)
                                            .append("\n");
                                }
                            });

            List<FkInjection> fks = injections.getOrDefault(startNode, List.of());
            for (FkInjection fk : fks) {
                if (!attrList.isEmpty()) {
                    attrList.append(", ");
                }
                attrList.append(fk.attrName());
                restrictionBuilder
                        .append(startNode.getName())
                        .append(".")
                        .append(fk.attrName())
                        .append(" -> ")
                        .append(fk.referencedTable())
                        .append(".")
                        .append(fk.attrName())
                        .append("\n");
            }

            if (!attrList.isEmpty()) {
                relationshipBuilder
                        .append(startNode.getName())
                        .append(" (")
                        .append(attrList)
                        .append(")\n");
            }
        }

        result.put("relationship", relationshipBuilder.toString());
        result.put("restriction", restrictionBuilder.toString());
        result.put("lossRestriction", "");
        return result;
    }

    private record FkInjection(String attrName, String referencedTable, boolean isTotal) {}

    private void parseRelationship(String relationship, Graph<Node, Edge> diagram) {
        relationship
                .lines()
                .filter(line -> !line.isBlank())
                .forEach(
                        line -> {
                            String[] parts = line.split(" ", 2);
                            String name = parts[0].trim();
                            Node entity = getOrCreate(name, diagram);
                            String[] attributes = parts[1].replaceAll("[()]", "").split(",");
                            Stream.of(attributes)
                                    .forEach(attr -> addAttribute(entity, attr.trim(), diagram));
                        });
    }

    private void addAttribute(Node entity, String attribute, Graph<Node, Edge> diagram) {
        Matcher matcher = PK_PATTERN.matcher(attribute);
        boolean pk = matcher.find();
        final String attrName = pk ? matcher.group(1) : attribute;
        Domain type = pk ? Domain.INTEGER : null;

        Node attr = getOrCreateAttr(attrName, entity, diagram);

        attr.setAttribute(true);
        attr.setPk(pk);
        if (pk) {
            attr.setFk(false);
            attr.setReference(null);
        }
        attr.setDataType(type);

        diagram.addEdge(
                entity, attr, Edge.builder().label("attr" + entity.getName() + attrName).build());
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
                            String attrName = srcParts[1].trim();
                            String refName = refParts[0].trim();
                            Node src = getOrCreate(srcName, diagram);

                            Node attr = getOrCreateAttr(attrName, src, diagram);

                            attr.setAttribute(true);
                            attr.setPk(false);
                            attr.setFk(true);
                            attr.setDataType(Domain.INTEGER);
                            attr.setReference(refName);

                            diagram.addEdge(
                                    src,
                                    attr,
                                    Edge.builder().label("attr" + srcName + attrName).build());
                        });
    }

    private void parseLossRestriction(String lossRestriction, Graph<Node, Edge> diagram) {}
}
