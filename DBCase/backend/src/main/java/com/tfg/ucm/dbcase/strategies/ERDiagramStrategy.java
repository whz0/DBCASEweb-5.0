package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.input.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.input.ErInput;
import com.tfg.ucm.dbcase.dto.input.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.input.ErRelationshipParticipantDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.springframework.stereotype.Service;

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

        Map<String, Node> nodeById = new HashMap<>();

        for (ErEntityDTO e : input.entities()) {
            Entity entity = Entity.builder().name(e.name()).weak(e.isWeak()).build();
            graph.addVertex(entity);
            nodeById.put(e.id(), entity);
        }

        for (ErRelationshipDTO r : input.relationships()) {
            Relationship rel = Relationship.builder().name(r.name()).build();
            graph.addVertex(rel);
            nodeById.put(r.id(), rel);
        }

        for (ErAttributeDTO a : input.attributes()) {
            Attribute attr =
                    Attribute.builder()
                            .name(a.name())
                            .pk(a.isKey())
                            .compose(a.isComposite())
                            .multivalue(a.isMultivalued())
                            .noEmpty(a.isNotNull())
                            .unique(a.isUnique())
                            .size(a.size())
                            .build();
            graph.addVertex(attr);
            nodeById.put(a.id(), attr);

            Node parent = nodeById.get(a.parentId());
            if (parent != null) {
                graph.addEdge(
                        parent,
                        attr,
                        Edge.builder().label("attr:" + a.parentId() + ":" + a.id()).build());
            }
        }

        for (ErRelationshipDTO r : input.relationships()) {
            Node rel = nodeById.get(r.id());
            for (ErRelationshipParticipantDTO p : r.participants()) {
                Node entity = nodeById.get(p.entityId());
                if (entity != null && rel != null) {
                    String label =
                            "part:"
                                    + p.entityId()
                                    + ":"
                                    + r.id()
                                    + ":"
                                    + p.cardinalityMin()
                                    + ":"
                                    + p.cardinalityMax()
                                    + ":"
                                    + p.role();
                    graph.addEdge(entity, rel, Edge.builder().label(label).build());
                }
            }
        }

        return Diagram.builder().diagram(graph).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();

        List<ErEntityDTO> entities = new ArrayList<>();
        List<ErRelationshipDTO> relationships = new ArrayList<>();
        List<ErAttributeDTO> attributes = new ArrayList<>();

        Map<String, String> idByName = new HashMap<>();
        int[] counter = {0};
        graph.vertexSet().forEach(n -> idByName.put(n.getName(), "n" + counter[0]++));

        for (Node node : graph.vertexSet()) {
            String id = idByName.get(node.getName());

            if (node instanceof Entity entity) {
                List<String> attrIds = new ArrayList<>();
                List<String> pkIds = new ArrayList<>();
                BreadthFirstIterator<Node, Edge> bfs = new BreadthFirstIterator<>(graph, node);
                bfs.next();
                while (bfs.hasNext()) {
                    Node n = bfs.next();
                    if (n instanceof Attribute a) {
                        String aid = idByName.get(a.getName());
                        attrIds.add(aid);
                        if (a.isPk()) {
                            pkIds.add(aid);
                        }
                    }
                }
                entities.add(
                        new ErEntityDTO(
                                id, entity.getName(), null, entity.isWeak(), attrIds, pkIds));

            } else if (node instanceof Relationship rel) {
                List<ErRelationshipParticipantDTO> participants = new ArrayList<>();
                for (Edge e : graph.incomingEdgesOf(rel)) {
                    Node source = graph.getEdgeSource(e);
                    if (source instanceof Entity) {
                        String[] parts = e.getLabel().split(":");
                        String min = parts.length > 3 ? parts[3] : "";
                        String max = parts.length > 4 ? parts[4] : "";
                        String role =
                                parts.length > 5 && !parts[5].equals("null") ? parts[5] : null;
                        participants.add(
                                new ErRelationshipParticipantDTO(
                                        idByName.get(source.getName()), min, max, role));
                    }
                }
                relationships.add(
                        new ErRelationshipDTO(
                                id, rel.getName(), null, "Normal", participants, List.of()));

            } else if (node instanceof Attribute attr) {
                String parentId =
                        graph.incomingEdgesOf(attr).stream()
                                .map(graph::getEdgeSource)
                                .filter(n -> !(n instanceof Attribute))
                                .map(n -> idByName.get(n.getName()))
                                .findFirst()
                                .orElse(null);
                attributes.add(
                        new ErAttributeDTO(
                                id,
                                attr.getName(),
                                null,
                                parentId,
                                attr.isPk(),
                                attr.isCompose(),
                                attr.isMultivalue(),
                                attr.isNoEmpty(),
                                attr.isUnique(),
                                null,
                                attr.getSize(),
                                List.of()));
            }
        }

        return new ErInput(entities, relationships, attributes, List.of());
    }
}
