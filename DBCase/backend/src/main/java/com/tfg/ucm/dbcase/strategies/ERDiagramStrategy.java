package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.erdiagram.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipParticipantDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.Position;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.ErInput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedMultigraph;
import org.springframework.stereotype.Service;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.*;

@Service
public class ERDiagramStrategy implements DiagramStrategy<ErInput> {

    @Override
    public DiagramType getType() {
        return DiagramType.ER;
    }

    @Override
    public Class<ErInput> getInputType() {
        return ErInput.class;
    }

    @Override
    public Diagram generate(ErInput input) {
        Graph<Node, Edge> graph = new DirectedMultigraph<>(Edge.class);
        Map<String, ErRelationshipDTO> relationshipDTOMap = new HashMap<>();
        Map<String, ErEntityDTO> entityDTOMap = new HashMap<>();
        Map<String, ErAttributeDTO> attributeDTOMap = new HashMap<>();

        for (ErRelationshipDTO r : input.relationships()) {
            relationshipDTOMap.put(r.id(), r);
        }

        for (ErEntityDTO e : input.entities()) {
            entityDTOMap.put(e.id(), e);
        }

        for (ErAttributeDTO a : input.attributes()) {
            attributeDTOMap.put(a.id(), a);
        }

        for (ErRelationshipDTO erRel : input.relationships()) {
            Map<String, String> listN = new HashMap<>();
            Map<String, String> listOne = new HashMap<>();
            for (ErRelationshipParticipantDTO erPart : erRel.participants()) {

                ErEntityDTO entity = entityDTOMap.get(erPart.entityId());
                // Si el número de relaciones N es igual que el número de
                // participantes entonces habrá que crear tabla intermedia
                if (erPart.cardinalityMax().equals("N")) {
                    mapAttribute(listN, entity.primaryKeys(), entity.name());
                } else {
                    mapAttribute(listOne, entity.primaryKeys(), entity.name());
                    for (String attrName : listOne.keySet()) {
                        Node entityRef = getOrCreateNode(listOne.get(attrName), graph);
                        Node attrRef = getOrCreateAttr(attrName, entityRef, graph);
                        Node entitySrc = getOrCreateNode(entity.name(), graph);
                        Node attrSrc = getOrCreateAttr(attrName, entitySrc, graph);
                        addEdge(attrSrc, attrRef, graph);
                    }
                }
                processEntity(entityDTOMap.get(erPart.entityId()), input.attributes(), graph);
            }
            if (listN.size() == erRel.participants().size() || !erRel.attributes().isEmpty()) {
                processRelationship(erRel, input.attributes(), graph);
            }
        }

        return Diagram.builder().diagram(graph).build();
    }

    private void mapAttribute(Map<String, String> map, List<String> pks,
                              String entity) {
        for (String pk : pks) {
            map.put(pk, entity);
        }
    }

    private void processEntity(ErEntityDTO erEnt,
                               List<ErAttributeDTO> erAttributes, Graph<Node, Edge> graph) {

        Node entity = getOrCreateNode(erEnt.name(), graph);

        for (String attrName : erEnt.attributes()) {
            ErAttributeDTO erAttr = erAttributes.stream().filter(a -> a.name().equals(attrName))
                    .findFirst().orElse(null);

            if (erAttr != null) {
                Node attr = getOrCreateAttr(attrName, entity, graph);

                if (erAttr.isKey()) {
                    addPrimaryAttr(attr, entity, graph);
                } else {
                    addEdge(entity, attr, graph);
                }
            }
        }

    }

    private void processRelationship(ErRelationshipDTO erRel,
                                     List<ErAttributeDTO> erAttributes, Graph<Node, Edge> graph) {

        Node rel = getOrCreateNode(erRel.name(), graph);

        for (String attrName : erRel.attributes()) {
            ErAttributeDTO erAttr = erAttributes.stream().filter(a -> a.name().equals(attrName))
                    .findFirst().orElse(null);

            if (erAttr != null) {
                Node attr = getOrCreateAttr(attrName, rel, graph);

                if (erAttr.isKey()) {
                    addPrimaryAttr(attr, rel, graph);
                } else {
                    addEdge(rel, attr, graph);
                }
            }
        }

    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();

        List<ErEntityDTO> entities = new ArrayList<>();
        List<ErRelationshipDTO> relationships = new ArrayList<>();
        List<ErAttributeDTO> attributes = new ArrayList<>();

        Set<Node> nodes =
                graph.vertexSet().stream()
                        .filter(n -> !n.isAttribute())
                        .collect(Collectors.toSet());

        HashMap<String, Node> mapByName = new HashMap<>();
        nodes.forEach(n -> mapByName.put(n.getName(), n));

        Set<Node> relationshipNodes = nodes.stream()
                .filter((n) -> NodeClassifier.isRelationship(n, graph)).collect(Collectors.toSet());

        int i = 10, j = 10;
        for (Node rel : relationshipNodes) {
            Position pos = new Position(i += 10, j += 10);
            buildRelationship(rel, pos, graph, mapByName, relationships, entities, attributes);
        }

        return new ErInput(entities, relationships, attributes, List.of(), List.of());
    }

    private void buildEntity(
            Node node,
            Position pos,
            Graph<Node, Edge> graph,
            Map<String, Node> mapByName,
            List<ErEntityDTO> entities,
            List<ErRelationshipDTO> relationships,
            List<ErAttributeDTO> attributes) {

        Set<Node> attrs = Graphs.neighborSetOf(graph, node);

        Position around = pos;
        List<String> ownAttributes = new ArrayList<>();
        List<String> ownPks = new ArrayList<>();
        for (Node attr : attrs) {
            addingAttribute(node, attr, around, attributes, ownAttributes, ownPks);
        }
        entities.add(new ErEntityDTO(node.getUuid(),
                node.getName(),
                pos,
                false,
                ownAttributes,
                ownPks));
    }

    private void buildRelationship(
            Node node,
            Position pos,
            Graph<Node, Edge> graph,
            Map<String, Node> mapByName,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (mapByName.containsKey(node.getName())) {
            mapByName.remove(node.getName());

            List<Node> nodes = Graphs.neighborListOf(graph, node);
            List<ErRelationshipParticipantDTO> participants = new ArrayList<>();
            List<String> ownAttrs = new ArrayList<>();

            for (Node n : nodes) {
                if (n.isFk()) {
                    Node ref = mapByName.get(n.getReference());
                    pos = new Position(pos.x() + 10, pos.y() + 10);
                    if (n.isAttribute()) {
                        addingAttribute(node, n, pos, attributes, ownAttrs, List.of());
                    } else if (NodeClassifier.isRelationship(ref, graph)) {
                        buildRelationship(n, pos, graph, mapByName, relationships, entities, attributes);
                    } else {
                        buildEntity(n, pos, graph, mapByName, entities, relationships, attributes);
                    }
                }
            }
            relationships.add(new ErRelationshipDTO(node.getUuid(),
                    node.getName(),
                    pos,
                    "String",
                    participants,
                    ownAttrs));
        }
    }

    private void addingAttribute(Node node, Node attr, Position pos, List<ErAttributeDTO> attributes,
                                 List<String> ownAttributes,
                                 List<String> ownPks) {
        attributes.add(new ErAttributeDTO(attr.getUuid(),
                attr.getName(),
                pos,
                node.getUuid(),
                attr.isPk(),
                false,
                false,
                attr.isNotNull(),
                attr.isUnique(),
                "String",
                10,
                List.of()));
        if (attr.isPk()) {
            ownPks.add(attr.getUuid());
        } else {
            ownAttributes.add(attr.getUuid());
        }
    }
}
