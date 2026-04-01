package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Relationship;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class LogicalDiagramStrategy implements DiagramStrategy {
    @Override
    public String getType() {
        return "logical";
    }

    @Override
    public Diagram generate(Object diagram) {

        Map<String, Node> nodes = new HashMap<>();
        Set<Edge> edges = new HashSet<>();

        LinkedHashMap<String, String> dia = (LinkedHashMap<String, String>) diagram;
        parseRestriction(dia.get("restriction"), nodes, edges);
        parseRelationship(dia.get("relationship"), nodes, edges);
        parseLossRestriction(dia.get("lossRestriction"), nodes, edges);

        return Diagram.builder()
                .nodes(nodes.values().stream().toList())
                .edges(edges.stream().toList())
                .build();
    }

    private void parseRelationship(String relationship, Map<String, Node> nodes, Set<Edge> rel) {

        relationship
                .lines()
                .forEach(
                        (line) -> {
                            String[] parts = line.split(" ", 2);
                            String entity = parts[0];
                            String[] attributes = parts[1].replaceAll("[()]", "").split(",");

                            Iterator<String> i = Arrays.stream(attributes).iterator();

                            while (i.hasNext()) {
                                addAttribute(entity, i.next(), nodes, rel);
                            }
                        });
    }

    private void addAttribute(
            String entity, String attribute, Map<String, Node> nodes, Set<Edge> rel) {

        Node isRel = nodes.get(entity);
        if (isRel != null && isRel.getClass() != Relationship.class) {
            Pattern pattern = Pattern.compile("^__([a-zA-Z])__$");
            Matcher matcher = pattern.matcher(attribute);
            boolean pk = matcher.find();
            if (pk) {
                attribute = matcher.group(1);
            }
            if (!nodes.containsKey(attribute)) {
                nodes.put(attribute, Attribute.builder().name(attribute).pk(pk).build());
            }
            rel.add(Edge.builder().target(attribute).source(entity).build());
        }
    }

    private void parseRestriction(String restriction, Map<String, Node> nodes, Set<Edge> rel) {

        restriction
                .lines()
                .forEach(
                        (line) -> {
                            String[] restrict = line.split("->");
                            String origin = restrict[0].split("\\.")[0].trim();
                            String target = restrict[1].split("\\.")[0].trim();

                            if (!nodes.containsKey(origin)) {
                                nodes.put(origin, Relationship.builder().name(origin).build());
                            }

                            if (!nodes.containsKey(target)) {
                                nodes.put(target, Entity.builder().name(target).build());
                            }

                            rel.add(Edge.builder().source(origin).target(target).build());
                        });
    }

    private void parseLossRestriction(
            String lossRestriction, Map<String, Node> nodes, Set<Edge> rel) {}
}
