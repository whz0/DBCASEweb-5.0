package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreate;

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

        List<Node> tableNodes = graph.vertexSet().stream().filter(n -> !n.isAttribute()).toList();

        for (Node startNode : tableNodes) {
            StringBuilder attrList = new StringBuilder();

            graph.vertexSet().stream()
                    .filter(n -> n.isAttribute() && graph.containsEdge(startNode, n))
                    .forEach(
                            attr -> {
                                if (!attrList.isEmpty()) {
                                    attrList.append(", ");
                                }
                                attrList.append(
                                        attr.isPk()
                                                ? "__" + attr.getName() + "__"
                                                : attr.getName());

                                if (attr.isPk() && attr.getReference() != null) {
                                    restrictionBuilder
                                            .append(startNode.getName())
                                            .append(".")
                                            .append(attr.getName())
                                            .append(" -> ")
                                            .append(attr.getReference())
                                            .append(".")
                                            .append(attr.getName())
                                            .append("\n");
                                }
                            });

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

        Node attr = getOrCreate(attrName, diagram);

        attr.setAttribute(true);
        attr.setPk(pk);
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

                            Node attr = getOrCreate(attrName, diagram);

                            attr.setAttribute(true);
                            attr.setPk(true);
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
