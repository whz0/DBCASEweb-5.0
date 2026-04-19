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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jgrapht.Graph;
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
        Graph<Node, Edge> graph = diagram.getDiagram();
        List<Node> tableNodes = graph.vertexSet().stream().filter(n -> !isAttribute(n)).toList();
        var injections = Auxiliary.resolveFkInjections(tableNodes, graph);

        StringBuilder relationships = new StringBuilder();
        StringBuilder restrictions = new StringBuilder();

        for (Node table : tableNodes) {
            if (shouldSkip(table, graph)) {
                continue;
            }
            relationships.append(
                    buildTableEntry(
                            table, graph, injections.getOrDefault(table, List.of()), restrictions));
        }

        return new java.util.LinkedHashMap<>(
                java.util.Map.of(
                        "relationship", relationships.toString(),
                        "restriction", restrictions.toString(),
                        "lossRestriction", ""));
    }

    private boolean shouldSkip(Node node, Graph<Node, Edge> graph) {
        return NodeClassifier.isRelationship(node, graph)
                && NodeClassifier.classify(node, graph) != NodeClassifier.RelationshipKind.NM;
    }

    private String buildTableEntry(
            Node table,
            Graph<Node, Edge> graph,
            List<Auxiliary.FkInjection> fks,
            StringBuilder restrictions) {
        StringBuilder attrList = new StringBuilder();

        graph.vertexSet().stream()
                .filter(n -> isAttribute(n) && graph.containsEdge(table, n))
                .forEach(attr -> appendAttr(attr, table, graph, attrList, restrictions));

        for (var fk : fks) {
            if (!attrList.isEmpty()) {
                attrList.append(", ");
            }
            attrList.append(fk.attrName());
            restrictions
                    .append(table.getName())
                    .append(".")
                    .append(fk.attrName())
                    .append(" -> ")
                    .append(fk.referencedTable())
                    .append(".")
                    .append(fk.attrName())
                    .append("\n");
        }

        return attrList.isEmpty() ? "" : table.getName() + " (" + attrList + ")\n";
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
