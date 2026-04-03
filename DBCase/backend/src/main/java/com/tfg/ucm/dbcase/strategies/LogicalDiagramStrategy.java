package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Relationship;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.springframework.stereotype.Service;

@Service
public class LogicalDiagramStrategy implements DiagramStrategy {
    @Override
    public String getType() {
        return "logical";
    }

    @Override
    public Diagram generate(Object diagram) {

        Graph<Node, Edge> result = new DirectedMultigraph<>(Edge.class);

        LinkedHashMap<String, String> dia = (LinkedHashMap<String, String>) diagram;
        parseRestriction(dia.get("restriction"), result);
        parseRelationship(dia.get("relationship"), result);
        parseLossRestriction(dia.get("lossRestriction"), result);

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        return null;
    }

    private void parseRelationship(String relationship, Graph<Node, Edge> diagram) {

        relationship
                .lines()
                .forEach(
                        (line) -> {
                            String[] parts = line.split(" ", 2);
                            Node entity = Entity.builder().name(parts[0]).build();
                            String[] attributes = parts[1].replaceAll("[()]", "").split(",");

                            Iterator<String> i = Arrays.stream(attributes).iterator();

                            while (i.hasNext()) {
                                addAttribute(entity, i.next().trim(), diagram);
                            }
                        });
    }

    private void addAttribute(Node entity, String attribute, Graph<Node, Edge> diagram) {

        Pattern pattern = Pattern.compile("^__([a-zA-Z]+)__$");
        Matcher matcher = pattern.matcher(attribute);
        boolean pk = matcher.find();
        if (pk) {
            attribute = matcher.group(1);
        }
        final String attributeName = attribute;
        boolean exists =
                diagram.vertexSet().stream().anyMatch(a -> a.getName().equals(attributeName));
        if (!exists) {
            Node attr = Attribute.builder().name(attributeName).pk(pk).build();
            diagram.addVertex(attr);
            diagram.addEdge(
                    entity,
                    attr,
                    Edge.builder().label("attribute" + attributeName + entity.getName()).build());
        }
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
