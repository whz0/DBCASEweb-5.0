package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Participant;
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

        Graph<Node, Edge> graph = diagram.getDiagram();

        List<Node> tableNodes =
                graph.vertexSet().stream().filter(n -> !(n instanceof Attribute)).toList();

        for (Node startNode : tableNodes) {
            StringBuilder attrList = new StringBuilder();

            graph.vertexSet().stream()
                    .filter(n -> n instanceof Attribute && graph.containsEdge(startNode, n))
                    .map(Attribute.class::cast)
                    .forEach(
                            attr -> {
                                if (!attrList.isEmpty()) {
                                    attrList.append(", ");
                                }
                                attrList.append(
                                        attr.isPk()
                                                ? "__" + attr.getName() + "__"
                                                : attr.getName());

                                if (attr.isPk() && attr.getFk() != null) {
                                    restrictionBuilder
                                            .append(attr.getFk())
                                            .append(".")
                                            .append(attr.getName())
                                            .append(" -> ")
                                            .append(startNode.getName())
                                            .append(".")
                                            .append(attr.getName())
                                            .append("\n");
                                }
                            });

            relationshipBuilder
                    .append(startNode.getName())
                    .append(" (")
                    .append(attrList)
                    .append(")\n");
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
                            String nodeName = parts[0].trim();
                            boolean isRel =
                                    diagram.vertexSet().stream()
                                            .anyMatch(
                                                    n ->
                                                            n.getName().equals(nodeName)
                                                                    && n instanceof Relationship);
                            Node entity = getOrCreate(nodeName, isRel, diagram);
                            String[] attributes = parts[1].replaceAll("[()]", "").split(",");
                            Stream.of(attributes)
                                    .forEach(attr -> addAttribute(entity, attr.trim(), diagram));
                        });
    }

    private void addAttribute(Node entity, String attribute, Graph<Node, Edge> diagram) {
        Matcher matcher = PK_PATTERN.matcher(attribute);
        boolean pk = matcher.find();
        final String attrName = pk ? matcher.group(1) : attribute;
        Optional<Node> exists =
                diagram.vertexSet().stream().filter(a -> a.getName().equals(attrName)).findAny();
        Node attr;
        if (exists.isEmpty()) {
            Domain type = pk ? Domain.INTEGER : null;
            String fk = (pk && entity instanceof Relationship) ? entity.getName() : null;
            attr = Attribute.builder().name(attrName).pk(pk).dataType(type).fk(fk).build();
            diagram.addVertex(attr);
        } else {
            attr = exists.get();
            if (pk
                    && entity instanceof Relationship
                    && attr instanceof Attribute a
                    && a.getFk() == null) {
                a.setFk(entity.getName());
            }
        }
        diagram.addEdge(
                entity,
                attr,
                Edge.builder().label("attribute" + attrName + entity.getName()).build());
    }

    private void parseRestriction(String restriction, Graph<Node, Edge> diagram) {
        restriction
                .lines()
                .filter(line -> !line.isBlank())
                .forEach(
                        line -> {
                            String[] parts = line.split("->");
                            String srcName = parts[0].split("\\.")[0].trim();
                            String attrName = parts[0].split("\\.")[1].trim();
                            String tgtName = parts[1].split("\\.")[0].trim();
                            Node src = getOrCreate(srcName, true, diagram);
                            Node tgt = getOrCreate(tgtName, false, diagram);
                            if (src instanceof Relationship rel) {
                                boolean alreadyIn =
                                        rel.getParticipants().stream()
                                                .anyMatch(p -> p.getEntity().equals(tgt));
                                if (!alreadyIn) {
                                    Participant p = new Participant();
                                    p.setEntity(tgt);
                                    rel.getParticipants().add(p);
                                }
                            }

                            diagram.vertexSet().stream()
                                    .filter(
                                            n ->
                                                    n instanceof Attribute
                                                            && n.getName().equals(attrName))
                                    .map(n -> (Attribute) n)
                                    .findFirst()
                                    .ifPresent(
                                            a -> {
                                                if (a.getFk() == null) {
                                                    a.setFk(srcName);
                                                }
                                            });
                            diagram.addEdge(
                                    src,
                                    tgt,
                                    Edge.builder().label("rel" + srcName + tgtName).build());
                        });
    }

    private Node getOrCreate(String name, boolean isRelationship, Graph<Node, Edge> diagram) {
        return diagram.vertexSet().stream()
                .filter(n -> n.getName().equals(name))
                .findFirst()
                .orElseGet(
                        () -> {
                            Node n =
                                    isRelationship
                                            ? Relationship.builder()
                                            .name(name)
                                            .participants(new java.util.ArrayList<>())
                                            .attributes(new java.util.ArrayList<>())
                                            .build()
                                            : Entity.builder().name(name).build();
                            diagram.addVertex(n);
                            return n;
                        });
    }

    private void parseLossRestriction(String lossRestriction, Graph<Node, Edge> diagram) {
    }
}
