package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
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
        parseRelationship(diagram.relationship(), result);
        parseLossRestriction(diagram.lossRestriction(), result);

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {

        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        StringBuilder relationshipBuilder = new StringBuilder();
        StringBuilder restrictionBuilder = new StringBuilder();
        StringBuilder lossRestrictionBuilder = new StringBuilder();

        Graph<Node, Edge> graph = diagram.getDiagram();

        List<Node> entities =
                graph.vertexSet().stream().filter(n -> !(n instanceof Attribute)).toList();

        entities.forEach(
                ent -> {
                    String line = ent.getName() + " (";
                    List<Edge> edges = graph.edgesOf(ent).stream().toList();
                    for (int i = 0; i < edges.size(); i++) {
                        Edge edge = edges.get(i);
                        Node node = graph.getEdgeTarget(edge);
                        if (node instanceof Attribute attribute) {
                            boolean pk = attribute.isPk();
                            line =
                                    line.concat(
                                            String.format(
                                                    "%s"
                                                            + attribute.getName()
                                                            + "%s"
                                                            + (i == edges.size() - 1 ? "" : ", "),
                                                    pk ? "__" : "",
                                                    pk ? "__" : ""));

                            if (ent instanceof Relationship relationship) {
                                Node entity =
                                        Graphs.neighborListOf(graph, node).stream()
                                                .filter(n -> !(n.equals(relationship)))
                                                .findFirst()
                                                .get();
                                String restriction =
                                        relationship.getName()
                                                + "."
                                                + attribute.getName()
                                                + " -> "
                                                + entity.getName()
                                                + "."
                                                + attribute.getName()
                                                + "\n";
                                restrictionBuilder.append(restriction);
                            }
                        }
                    }
                    line = line.concat(")\n");
                    relationshipBuilder.append(line);
                });

        result.put("relationship", relationshipBuilder.toString());
        result.put("restriction", restrictionBuilder.toString());
        result.put("lossRestriction", lossRestrictionBuilder.toString());

        return result;
    }

    private void parseRelationship(String relationship, Graph<Node, Edge> diagram) {
        relationship
                .lines()
                .forEach(
                        (line) -> {
                            String[] parts = line.split(" ", 2);
                            Optional<Node> exists =
                                    diagram.vertexSet().stream()
                                            .filter(n -> n.getName().equals(parts[0]))
                                            .findAny();
                            Node entity =
                                    exists.orElseGet(() -> Entity.builder().name(parts[0]).build());
                            String[] attributes = parts[1].replaceAll("[()]", "").split(",");

                            Stream.of(attributes)
                                    .forEach(
                                            attribute ->
                                                    addAttribute(
                                                            entity, attribute.trim(), diagram));
                        });
    }

    private void addAttribute(Node entity, String attribute, Graph<Node, Edge> diagram) {
        Matcher matcher = PK_PATTERN.matcher(attribute);
        boolean pk = matcher.find();
        if (pk) {
            attribute = matcher.group(1);
        }
        final String attributeName = attribute;
        Optional<Node> exists =
                diagram.vertexSet().stream()
                        .filter(a -> a.getName().equals(attributeName))
                        .findAny();
        Node attr;
        if (exists.isEmpty()) {
            Domain type = pk ? Domain.INTEGER : Domain.VARCHAR;
            attr = Attribute.builder().name(attributeName).pk(pk).dataType(type).build();
            diagram.addVertex(attr);
        } else {
            attr = exists.get();
        }
        diagram.addEdge(
                entity,
                attr,
                Edge.builder().label("attribute" + attributeName + entity.getName()).build());
    }

    private void parseRestriction(String restriction, Graph<Node, Edge> diagram) {

        restriction
                .lines()
                .forEach(
                        (line) -> {
                            String[] restrict = line.split("->");
                            String origin = restrict[0].split("\\.")[0].trim();
                            String target = restrict[1].split("\\.")[0].trim();
                            Node relationship = Relationship.builder().name(origin).build();
                            Node entity = Entity.builder().name(target).build();
                            diagram.addVertex(relationship);
                            diagram.addVertex(entity);

                            diagram.addEdge(
                                    relationship,
                                    entity,
                                    Edge.builder()
                                            .label("relationship" + relationship + entity)
                                            .build());
                        });
    }

    private void parseLossRestriction(String lossRestriction, Graph<Node, Edge> diagram) {}
}
