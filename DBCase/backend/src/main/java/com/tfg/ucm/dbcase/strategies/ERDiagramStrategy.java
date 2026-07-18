package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.addEdge;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.addForeignAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.addPrimaryAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.editFk;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateNode;

import com.tfg.ucm.dbcase.dto.DataType;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.erdiagram.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErRelationshipParticipantDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.ErUndefinedDTO;
import com.tfg.ucm.dbcase.dto.erdiagram.Position;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.ErInput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedPseudograph;
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
        Graph<Node, Edge> graph = new DirectedPseudograph<>(Edge.class);

        Map<String, ErEntityDTO> entityDTOMap = new HashMap<>();
        Map<String, ErAttributeDTO> attributeDTOMap = new HashMap<>();

        for (ErEntityDTO e : input.entities()) {
            entityDTOMap.put(e.id(), e);
        }
        for (ErAttributeDTO a : input.attributes()) {
            attributeDTOMap.put(a.id(), a);
        }

        Map<String, String> customDomainMap = new HashMap<>();
        if (input.domains() != null) {
            for (var d : input.domains()) {
                if (d.baseType() != null) {
                    customDomainMap.put(d.name().toUpperCase(), d.baseType().toUpperCase());
                }
            }
        }

        List<ErRelationshipDTO> normalRel = new ArrayList<>();
        List<ErRelationshipDTO> otherRel = new ArrayList<>();

        for (ErRelationshipDTO erRel : input.relationships()) {
            if (erRel.type().equalsIgnoreCase("normal") || erRel.type().equalsIgnoreCase("weak")) {
                normalRel.add(erRel);
            } else {
                otherRel.add(erRel);
            }
        }

        Set<String> originalEntityIds = new HashSet<>(entityDTOMap.keySet());

        for (ErEntityDTO erEnt : input.entities()) {
            processOwnAttributes(erEnt, attributeDTOMap, customDomainMap, graph);
        }

        List<ErRelationshipDTO> aggregations = new ArrayList<>();
        for (ErRelationshipDTO erRel : otherRel) {
            if (erRel.type().equalsIgnoreCase("aggregation")) {
                generateAggregation(erRel, entityDTOMap, attributeDTOMap, customDomainMap, graph);
                aggregations.add(erRel);
            } else if (erRel.type().equalsIgnoreCase("isa")) {
                generateIsA(erRel, entityDTOMap, attributeDTOMap, customDomainMap, graph);
            }
        }

        for (ErRelationshipDTO erRel : aggregations) {
            String aggName =
                    erRel.aggregationName() != null ? erRel.aggregationName() : erRel.name();
            addPksToAggregation(erRel.id(), aggName, entityDTOMap, attributeDTOMap, graph);
        }

        for (ErRelationshipDTO erRel : normalRel) {
            processRel(
                    erRel,
                    entityDTOMap,
                    attributeDTOMap,
                    customDomainMap,
                    graph,
                    !erRel.attributes().isEmpty());
        }

        for (Map.Entry<String, ErEntityDTO> entry : entityDTOMap.entrySet()) {
            if (!originalEntityIds.contains(entry.getKey())) {
                processOwnAttributes(entry.getValue(), attributeDTOMap, customDomainMap, graph);
            }
        }

        for (ErUndefinedDTO u : input.undefineds()) {
            getOrCreateNode(u.name(), graph);
        }

        return Diagram.builder().diagram(graph).build();
    }

    private void processRel(
            ErRelationshipDTO erRel,
            Map<String, ErEntityDTO> entityDTOMap,
            Map<String, ErAttributeDTO> attributeDTOMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph,
            boolean createIntermediate) {

        String name = erRel.aggregationName() != null ? erRel.aggregationName() : erRel.name();
        Node relationship = getOrCreateNode(name, graph);

        boolean self =
                erRel.participants().stream()
                                .map(ErRelationshipParticipantDTO::entityId)
                                .distinct()
                                .count()
                        == 1;

        if (erRel.participants().size() > 2) {
            processAttributes(
                    relationship, erRel.attributes(), attributeDTOMap, customDomainMap, graph);
            generateNAria(relationship, erRel, entityDTOMap, attributeDTOMap, graph, self);
        } else if (erRel.participants().size() == 2) {
            ErRelationshipParticipantDTO one = erRel.participants().get(0);
            ErRelationshipParticipantDTO other = erRel.participants().get(1);

            boolean isNM =
                    one.cardinalityMax().equalsIgnoreCase("n")
                            && other.cardinalityMax().equalsIgnoreCase("n");
            boolean isOneOne =
                    one.cardinalityMax().equals("1") && other.cardinalityMax().equals("1");

            ErEntityDTO entityOne = entityDTOMap.get(one.entityId());
            ErEntityDTO entityOther = entityDTOMap.get(other.entityId());
            if (entityOne == null || entityOther == null) {
                return;
            }

            if (isNM) {

                processAttributes(
                        relationship, erRel.attributes(), attributeDTOMap, customDomainMap, graph);
                generateNM(
                        relationship,
                        one,
                        other,
                        entityOne,
                        entityOther,
                        attributeDTOMap,
                        graph,
                        self);
            } else if (isOneOne) {
                if (createIntermediate) {
                    processAttributes(
                            relationship,
                            erRel.attributes(),
                            attributeDTOMap,
                            customDomainMap,
                            graph);
                }
                generateOneOne(
                        relationship,
                        one,
                        other,
                        entityOne,
                        entityOther,
                        attributeDTOMap,
                        customDomainMap,
                        erRel.attributes(),
                        graph,
                        createIntermediate,
                        self);
            } else {
                ErRelationshipParticipantDTO oneSide =
                        one.cardinalityMax().equalsIgnoreCase("1") ? one : other;
                ErRelationshipParticipantDTO nSide =
                        !one.cardinalityMax().equalsIgnoreCase("1") ? one : other;

                if (createIntermediate) {
                    processAttributes(
                            relationship,
                            erRel.attributes(),
                            attributeDTOMap,
                            customDomainMap,
                            graph);
                }
                generateOneN(
                        relationship,
                        oneSide,
                        nSide,
                        entityDTOMap.get(oneSide.entityId()),
                        entityDTOMap.get(nSide.entityId()),
                        nSide.cardinalityMin(),
                        attributeDTOMap,
                        customDomainMap,
                        erRel.attributes(),
                        graph,
                        createIntermediate,
                        self);
            }
        }
    }

    private void addPksToAggregation(
            String id,
            String name,
            Map<String, ErEntityDTO> entityDTOMap,
            Map<String, ErAttributeDTO> attributeDTOMap,
            Graph<Node, Edge> graph) {

        Node node = getOrCreateNode(name, graph);

        List<Node> pks = Graphs.successorListOf(graph, node).stream().filter(Node::isPk).toList();

        List<String> pksId = new ArrayList<>();
        for (Node pk : pks) {
            DataType dt = pk.getDataType();
            attributeDTOMap.put(
                    pk.getUuid(),
                    new ErAttributeDTO(
                            pk.getUuid(),
                            pk.getName(),
                            null,
                            null,
                            true,
                            false,
                            false,
                            false,
                            false,
                            dt != null ? dt.toString() : null,
                            dt != null ? dt.length() : 0,
                            List.of()));
            pksId.add(pk.getUuid());
        }
        entityDTOMap.put(id, new ErEntityDTO(id, name, null, false, List.of(), pksId));
    }

    private void generateIsA(
            ErRelationshipDTO rel,
            Map<String, ErEntityDTO> entityMap,
            Map<String, ErAttributeDTO> attrMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph) {

        ErRelationshipParticipantDTO parentId =
                rel.participants().stream()
                        .filter(p -> p.role().equalsIgnoreCase("parent"))
                        .findFirst()
                        .orElse(null);

        assert parentId != null;
        List<ErAttributeDTO> pks = new ArrayList<>();
        ErEntityDTO parent = entityMap.get(parentId.entityId());
        for (String pkId : parent.primaryKeys()) {
            pks.add(attrMap.get(pkId));
        }

        List<String> parentPkIds = pks.stream().map(ErAttributeDTO::id).toList();
        for (ErRelationshipParticipantDTO participantDTO : rel.participants()) {
            ErEntityDTO entity = entityMap.get(participantDTO.entityId());
            if (entity.id().equals(parent.id())) {
                continue;
            }
            if (entity.primaryKeys().isEmpty()) {
                List<String> mergedPks = new ArrayList<>(parentPkIds);
                mergedPks.addAll(entity.primaryKeys());
                entityMap.put(
                        entity.id(),
                        new ErEntityDTO(
                                entity.id(),
                                entity.name(),
                                entity.position(),
                                entity.isWeak(),
                                entity.attributes(),
                                mergedPks));
            }
        }

        for (ErRelationshipParticipantDTO participantDTO : rel.participants()) {
            ErEntityDTO entity = entityMap.get(participantDTO.entityId());
            if (entity.id().equals(parent.id())) {
                continue;
            }
            Node childNode = getOrCreateNode(entity.name(), graph);
            for (ErAttributeDTO pk : pks) {
                Node existingPk =
                        Graphs.successorListOf(graph, childNode).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isPk()
                                                        && a.getName().equals(pk.name()))
                                .findFirst()
                                .orElse(null);
                if (existingPk != null) {
                    existingPk.setFk(true);
                    existingPk.setReference(parent.name());
                    Node refNode = getOrCreateNode(parent.name(), graph);
                    Node attrRef = getOrCreateAttr(pk.name(), refNode, graph);
                    Auxiliary.addPrimaryAttr(attrRef, refNode, graph);
                    Auxiliary.addEdge(existingPk, attrRef, graph);
                } else {
                    addFkToRef(
                            pk.name(),
                            childNode,
                            parent.name(),
                            true,
                            false,
                            false,
                            false,
                            graph,
                            "");
                }
            }
        }
    }

    private void generateAggregation(
            ErRelationshipDTO rel,
            Map<String, ErEntityDTO> entityMap,
            Map<String, ErAttributeDTO> attrMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph) {

        processRel(rel, entityMap, attrMap, customDomainMap, graph, true);
    }

    private void generateNAria(
            Node relationshipNode,
            ErRelationshipDTO rel,
            Map<String, ErEntityDTO> entityMap,
            Map<String, ErAttributeDTO> attrMap,
            Graph<Node, Edge> graph,
            boolean self) {

        for (ErRelationshipParticipantDTO participant : rel.participants()) {
            ErEntityDTO entity = entityMap.get(participant.entityId());
            List<String> pksName = getPksName(entity, attrMap);
            for (String pk : pksName) {
                String role = self ? participant.role() : "";
                addFkToRef(
                        pk,
                        relationshipNode,
                        entity.name(),
                        true,
                        false,
                        !participant.cardinalityMin().equals("0"),
                        !participant.cardinalityMin().equals("0"),
                        graph,
                        role);
            }
        }
    }

    private void generateNM(
            Node relationshipNode,
            ErRelationshipParticipantDTO participantOne,
            ErRelationshipParticipantDTO participantOther,
            ErEntityDTO entityOne,
            ErEntityDTO entityOther,
            Map<String, ErAttributeDTO> attrMap,
            Graph<Node, Edge> graph,
            boolean self) {

        List<String> pksName = getPksName(entityOne, attrMap);
        for (String pk : pksName) {
            String role = self ? participantOne.role() : "";
            addFkToRef(
                    pk,
                    relationshipNode,
                    entityOne.name(),
                    true,
                    false,
                    !participantOne.cardinalityMin().equals("0"),
                    !participantOne.cardinalityMin().equals("0"),
                    graph,
                    role);
        }

        pksName = getPksName(entityOther, attrMap);
        for (String pk : pksName) {

            String role = self ? participantOther.role() : "";
            if (self
                    && entityOne.id().equals(entityOther.id())
                    && (role == null || role.isEmpty())) {
                role = "2";
            }
            addFkToRef(
                    pk,
                    relationshipNode,
                    entityOther.name(),
                    true,
                    false,
                    !participantOther.cardinalityMin().equals("0"),
                    !participantOther.cardinalityMin().equals("0"),
                    graph,
                    role);
        }
    }

    private void generateOneN(
            Node relationshipNode,
            ErRelationshipParticipantDTO participantOne,
            ErRelationshipParticipantDTO participantN,
            ErEntityDTO oneSide,
            ErEntityDTO nSide,
            String minCard,
            Map<String, ErAttributeDTO> attrMap,
            Map<String, String> customDomainMap,
            List<String> relAttrIds,
            Graph<Node, Edge> graph,
            boolean createIntermediate,
            boolean self) {

        boolean nIsTotal = !minCard.equals("0");
        Node node = createIntermediate ? relationshipNode : getOrCreateNode(nSide.name(), graph);
        List<String> pksName = null;
        if (createIntermediate) {
            pksName = getPksName(nSide, attrMap);
            for (String pk : pksName) {
                String role = self ? participantN.role() : "";
                addFkToRef(pk, node, nSide.name(), true, false, false, false, graph, role);
            }
        } else {
            processAttributes(node, relAttrIds, attrMap, customDomainMap, graph);
        }
        pksName = getPksName(oneSide, attrMap);
        for (String pk : pksName) {
            String role = self ? participantOne.role() : "";
            addFkToRef(pk, node, oneSide.name(), false, false, nIsTotal, nIsTotal, graph, role);
        }
    }

    private void generateOneOne(
            Node relationship,
            ErRelationshipParticipantDTO participantOne,
            ErRelationshipParticipantDTO participantOther,
            ErEntityDTO entityOne,
            ErEntityDTO entityOther,
            Map<String, ErAttributeDTO> attrMap,
            Map<String, String> customDomainMap,
            List<String> relAttrIds,
            Graph<Node, Edge> graph,
            boolean createIntermediate,
            boolean self) {

        boolean oneIsTotal = !participantOne.cardinalityMin().equals("0");
        boolean otherIsTotal = !participantOther.cardinalityMin().equals("0");

        if (createIntermediate) {
            boolean onePk = oneIsTotal || !otherIsTotal;
            List<String> pksName = getPksName(entityOther, attrMap);
            for (String pk : pksName) {
                String role = self ? participantOther.role() : "";
                addFkToRef(
                        pk,
                        relationship,
                        entityOther.name(),
                        onePk,
                        !onePk,
                        oneIsTotal,
                        oneIsTotal,
                        graph,
                        role);
            }
            pksName = getPksName(entityOne, attrMap);
            for (String pk : pksName) {
                String role = self ? participantOne.role() : "";
                addFkToRef(
                        pk,
                        relationship,
                        entityOne.name(),
                        !onePk,
                        onePk,
                        otherIsTotal,
                        otherIsTotal,
                        graph,
                        role);
            }
            return;
        }

        if (!oneIsTotal && !otherIsTotal) {
            Node intermediate = getOrCreateNode(entityOne.name() + entityOther.name(), graph);
            List<String> pksName = getPksName(entityOne, attrMap);
            for (String pk : pksName) {
                String role = self ? participantOne.role() : "";
                addFkToRef(
                        pk, intermediate, entityOne.name(), true, false, false, false, graph, role);
            }
            pksName = getPksName(entityOther, attrMap);
            for (String pk : pksName) {
                String role = self ? participantOther.role() : "";
                addFkToRef(
                        pk,
                        intermediate,
                        entityOther.name(),
                        false,
                        true,
                        false,
                        false,
                        graph,
                        role);
            }
            processAttributes(intermediate, relAttrIds, attrMap, customDomainMap, graph);

        } else if (oneIsTotal && otherIsTotal) {
            Node nodeOne = getOrCreateNode(entityOne.name(), graph);

            if (self) {
                List<String> pksName = getPksName(entityOther, attrMap);
                for (String pk : pksName) {
                    String role = participantOther.role();
                    addFkToRef(
                            pk, nodeOne, entityOther.name(), false, true, true, false, graph, role);
                }
                processAttributes(nodeOne, relAttrIds, attrMap, customDomainMap, graph);
            } else {
                Node nodeOther = getOrCreateNode(entityOther.name(), graph);

                List<String> pksName = getPksName(entityOther, attrMap);
                for (String pk : pksName) {
                    addFkToRef(
                            pk, nodeOne, entityOther.name(), false, true, true, false, graph, "");
                }

                for (Node attr : new ArrayList<>(Graphs.successorListOf(graph, nodeOther))) {
                    if (!attr.isAttribute() || attr.isPk()) {
                        continue;
                    }
                    Node copied = getOrCreateAttr(attr.getName(), nodeOne, graph);
                    copied.setNotNull(attr.isNotNull());
                    copied.setUnique(attr.isUnique());
                    copied.setDataType(attr.getDataType());
                    if (attr.isFk()) {
                        copied.setFk(true);
                        copied.setReference(attr.getReference());
                        Graphs.successorListOf(graph, attr)
                                .forEach(refAttr -> addEdge(copied, refAttr, graph));
                        addEdge(nodeOne, copied, graph);
                    } else {
                        addEdge(nodeOne, copied, graph);
                    }
                }

                processAttributes(nodeOne, relAttrIds, attrMap, customDomainMap, graph);
                graph.removeVertex(nodeOther);
            }

        } else {
            Node totalNode =
                    getOrCreateNode(oneIsTotal ? entityOne.name() : entityOther.name(), graph);
            ErRelationshipParticipantDTO partialParticipant =
                    oneIsTotal ? participantOther : participantOne;
            ErEntityDTO partialEntity = oneIsTotal ? entityOther : entityOne;
            List<String> pksName = getPksName(partialEntity, attrMap);
            for (String pk : pksName) {
                String role = self ? partialParticipant.role() : "";
                addFkToRef(
                        pk, totalNode, partialEntity.name(), false, true, true, false, graph, role);
            }
            processAttributes(totalNode, relAttrIds, attrMap, customDomainMap, graph);
        }
    }

    private List<String> getPksName(ErEntityDTO entity, Map<String, ErAttributeDTO> attrMap) {
        List<String> pks = new ArrayList<>();

        for (String pkId : entity.primaryKeys()) {
            ErAttributeDTO attr = attrMap.get(pkId);
            if (attr != null) {
                pks.add(attr.name());
            }
        }

        return pks;
    }

    private void addFkToRef(
            String attrName,
            Node owner,
            String ref,
            boolean isPk,
            boolean isUnique,
            boolean isNotNull,
            boolean isTotal,
            Graph<Node, Edge> graph,
            String role) {
        final String resolvedName = resolveFkName(attrName + role, ref, owner, graph);
        boolean clashesWithOwnPk =
                Graphs.neighborListOf(graph, owner).stream()
                        .anyMatch(
                                a ->
                                        a.isAttribute()
                                                && a.getName().equals(resolvedName)
                                                && a.isPk()
                                                && !a.isFk());
        String fkName = clashesWithOwnPk ? attrName + role + "_" + ref : resolvedName;
        Node attrNode = getOrCreateAttr(fkName, owner, graph);
        editFk(attrNode, isPk, isUnique, isNotNull, isTotal);
        addForeignAttr(attrNode, owner, ref, graph);

        Node refNode = getOrCreateNode(ref, graph);
        Node attrRef = getOrCreateAttr(attrName, refNode, graph);
        addPrimaryAttr(attrRef, refNode, graph);

        addEdge(attrNode, attrRef, graph);
    }

    private String resolveFkName(String attrName, String ref, Node owner, Graph<Node, Edge> graph) {
        boolean collision =
                Graphs.neighborListOf(graph, owner).stream()
                        .anyMatch(
                                a ->
                                        a.isAttribute()
                                                && a.getName().equals(attrName)
                                                && a.isFk()
                                                && !ref.equals(a.getReference()));
        if (collision) {
            return attrName + "_" + ref;
        }
        boolean willCollide =
                Graphs.neighborListOf(graph, owner).stream()
                        .anyMatch(
                                a ->
                                        a.isAttribute()
                                                && a.getName().equals(attrName)
                                                && !a.isFk()
                                                && !a.isPk());
        return willCollide ? attrName + "_" + ref : attrName;
    }

    private void processAttributes(
            Node owner,
            List<String> attrIds,
            Map<String, ErAttributeDTO> attributeDTOMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph) {
        for (String attrId : attrIds) {
            ErAttributeDTO erAttr = attributeDTOMap.get(attrId);
            if (erAttr == null) {
                continue;
            }
            Node attr = getOrCreateAttr(erAttr.name(), owner, graph);
            attr.setNotNull(erAttr.isNotNull());
            attr.setUnique(erAttr.isUnique());
            Domain domain = null;
            if (erAttr.domain() != null) {
                String domainKey = erAttr.domain().toUpperCase();
                String resolved = customDomainMap.getOrDefault(domainKey, domainKey);
                try {
                    domain = Domain.valueOf(resolved);
                } catch (Exception ignored) {
                }
            }
            if (domain != null) {
                attr.setDataType(
                        erAttr.size() > 0
                                ? DataType.of(domain, erAttr.size())
                                : DataType.of(domain));
            }
            if (erAttr.isKey()) {
                addPrimaryAttr(attr, owner, graph);
            } else {
                addEdge(owner, attr, graph);
            }
        }
    }

    private void processOwnAttributes(
            ErEntityDTO entity,
            Map<String, ErAttributeDTO> attributeDTOMap,
            Map<String, String> customDomainMap,
            Graph<Node, Edge> graph) {

        Node node = getOrCreateNode(entity.name(), graph);
        processAttributes(node, entity.attributes(), attributeDTOMap, customDomainMap, graph);
        processAttributes(node, entity.primaryKeys(), attributeDTOMap, customDomainMap, graph);
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();

        List<ErEntityDTO> entities = new ArrayList<>();
        List<ErRelationshipDTO> relationships = new ArrayList<>();
        List<ErAttributeDTO> attributes = new ArrayList<>();
        List<ErUndefinedDTO> undefineds = new ArrayList<>();

        List<Node> nodes =
                graph.vertexSet().stream()
                        .filter(n -> !n.isAttribute())
                        .sorted(Comparator.comparing(Node::getName))
                        .collect(Collectors.toList());

        Set<String> visited = new HashSet<>();

        int total = nodes.size();
        int idx = 0;
        for (Node node : nodes) {
            Position pos = circlePos(new Position(0, 0), idx++, total, Math.max(200, total * 80));
            List<Node> ownAttrs =
                    Graphs.successorListOf(graph, node).stream().filter(Node::isAttribute).toList();

            if (ownAttrs.isEmpty()) {
                undefineds.add(new ErUndefinedDTO(node.getUuid(), node.getName(), pos, List.of()));
                continue;
            }
            boolean allAttrsFk = ownAttrs.stream().allMatch(Node::isFk);
            boolean hasOwnPk = ownAttrs.stream().anyMatch(a -> a.isPk() && !a.isFk());
            if (allAttrsFk && !hasOwnPk && !isIntermediateOneOne(node, graph)) {
                continue;
            }
            if (isIntermediateOneOne(node, graph)) {
                buildIntermediateOneOne(
                        node, graph, visited, pos, relationships, entities, attributes);
            } else if (NodeClassifier.isNMRel(node, graph)) {
                long distinctRefs =
                        Graphs.successorListOf(graph, node).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isFk()
                                                        && a.getReference() != null)
                                .map(Node::getReference)
                                .distinct()
                                .count();
                if (distinctRefs > 2) {
                    buildNArias(node, graph, visited, pos, relationships, entities, attributes);
                } else {
                    buildNMRel(node, graph, visited, pos, relationships, entities, attributes);
                }
            } else if (NodeClassifier.isEntity(node, graph)) {
                Set<Node> uniqueFks = NodeClassifier.hasUniqueFk(node, graph);
                Set<Node> plainFks =
                        Graphs.successorListOf(graph, node).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isFk()
                                                        && !a.isUnique()
                                                        && !a.isPk())
                                .collect(Collectors.toSet());
                if (uniqueFks.isEmpty() && plainFks.isEmpty()) {
                    buildEntity(node, graph, visited, pos, entities, attributes);
                } else if (uniqueFks.isEmpty()) {
                    plainFks.forEach(
                            fk -> {
                                graph.vertexSet().stream()
                                        .filter(n -> n.getName().equals(fk.getReference()))
                                        .findFirst()
                                        .ifPresent(
                                                ref ->
                                                        buildOneNRel(
                                                                node,
                                                                ref,
                                                                graph,
                                                                visited,
                                                                pos,
                                                                relationships,
                                                                entities,
                                                                attributes));
                            });
                } else {
                    uniqueFks.forEach(
                            fk ->
                                    processOneOneFk(
                                            fk,
                                            node,
                                            graph,
                                            visited,
                                            pos,
                                            relationships,
                                            entities,
                                            attributes));
                }
            } else {
                undefineds.add(new ErUndefinedDTO(node.getUuid(), node.getName(), pos, List.of()));
            }
        }

        return new ErInput(entities, relationships, attributes, List.of(), undefineds);
    }

    private void processOneOneFk(
            Node fk,
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        boolean refExists =
                graph.vertexSet().stream().anyMatch(n -> isRealEntity(n, fk.getReference(), graph));
        if (!refExists) {
            buildAbsorbedOneOne(node, fk, graph, visited, pos, relationships, entities, attributes);
        } else {
            graph.vertexSet().stream()
                    .filter(n -> !n.isAttribute() && n.getName().equals(fk.getReference()))
                    .findFirst()
                    .ifPresent(
                            ref ->
                                    buildOneOneRel(
                                            node,
                                            ref,
                                            graph,
                                            visited,
                                            pos,
                                            relationships,
                                            entities,
                                            attributes));
        }
    }

    private boolean isRealEntity(Node n, String name, Graph<Node, Edge> graph) {
        if (n.isAttribute() || !n.getName().equals(name)) {
            return false;
        }
        return Graphs.successorListOf(graph, n).stream()
                .anyMatch(a -> a.isAttribute() && a.isPk() && !a.isFk());
    }

    private boolean isIntermediateOneOne(Node node, Graph<Node, Edge> graph) {
        List<Node> attrs =
                Graphs.successorListOf(graph, node).stream().filter(Node::isAttribute).toList();
        long ownPks = attrs.stream().filter(a -> a.isPk() && !a.isFk()).count();
        if (ownPks > 0) {
            return false;
        }
        long fkPks = attrs.stream().filter(a -> a.isFk() && a.isPk()).count();
        long fkUniques = attrs.stream().filter(a -> a.isFk() && a.isUnique() && !a.isPk()).count();
        return fkPks == 1 && fkUniques == 1;
    }

    private void buildIntermediateOneOne(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (visited.contains(node.getUuid())) {
            return;
        }
        visited.add(node.getUuid());

        List<Node> attrs =
                Graphs.successorListOf(graph, node).stream().filter(Node::isAttribute).toList();

        Node fkPkAttr = attrs.stream().filter(a -> a.isFk() && a.isPk()).findFirst().orElse(null);
        Node fkUniqueAttr =
                attrs.stream()
                        .filter(a -> a.isFk() && a.isUnique() && !a.isPk())
                        .findFirst()
                        .orElse(null);
        if (fkPkAttr == null || fkUniqueAttr == null) {
            return;
        }

        Node entityOne =
                graph.vertexSet().stream()
                        .filter(
                                n ->
                                        !n.isAttribute()
                                                && n.getName().equals(fkPkAttr.getReference()))
                        .findFirst()
                        .orElse(null);
        Node entityOther =
                graph.vertexSet().stream()
                        .filter(
                                n ->
                                        !n.isAttribute()
                                                && n.getName().equals(fkUniqueAttr.getReference()))
                        .findFirst()
                        .orElse(null);
        if (entityOne == null || entityOther == null) {
            return;
        }

        List<String> relAttrIds = new ArrayList<>();
        AtomicInteger ownIdx = new AtomicInteger(0);
        List<Node> ownAttrs = attrs.stream().filter(a -> !a.isFk()).toList();
        ownAttrs.forEach(
                a -> {
                    Position aPos = circlePos(pos, ownIdx.getAndIncrement(), ownAttrs.size(), 80);
                    attributes.add(
                            new ErAttributeDTO(
                                    a.getUuid(),
                                    a.getName(),
                                    aPos,
                                    node.getUuid(),
                                    false,
                                    false,
                                    false,
                                    a.isNotNull(),
                                    a.isUnique(),
                                    a.getDataType() != null
                                            ? a.getDataType().domain().name()
                                            : null,
                                    a.getDataType() != null ? a.getDataType().length() : 0,
                                    List.of()));
                    relAttrIds.add(a.getUuid());
                });

        buildEntity(entityOne, graph, visited, circlePos(pos, 0, 2, 120), entities, attributes);
        buildEntity(entityOther, graph, visited, circlePos(pos, 1, 2, 120), entities, attributes);

        List<ErRelationshipParticipantDTO> participants =
                List.of(
                        new ErRelationshipParticipantDTO(entityOne.getUuid(), "0", "1", ""),
                        new ErRelationshipParticipantDTO(entityOther.getUuid(), "0", "1", ""));

        relationships.add(
                new ErRelationshipDTO(
                        node.getUuid(),
                        node.getName(),
                        pos,
                        "Normal",
                        null,
                        participants,
                        relAttrIds));
    }

    private void buildAbsorbedOneOne(
            Node node,
            Node fkAttr,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (visited.contains(node.getUuid())) {
            return;
        }

        String absorbedName = fkAttr.getReference();
        String absorbedUuid = UUID.randomUUID().toString();

        List<Node> fkTargets = Graphs.successorListOf(graph, fkAttr);
        if (fkTargets.isEmpty()) {
            return;
        }
        Node absorbedPkAttr = fkTargets.getFirst();

        Position absorbedPos = circlePos(pos, 1, 2, 120);
        Position absorbedPkPos = circlePos(absorbedPos, 0, 1, 80);
        attributes.add(
                new ErAttributeDTO(
                        absorbedPkAttr.getUuid(),
                        absorbedPkAttr.getName(),
                        absorbedPkPos,
                        absorbedUuid,
                        true,
                        false,
                        false,
                        true,
                        false,
                        absorbedPkAttr.getDataType() != null
                                ? absorbedPkAttr.getDataType().domain().name()
                                : null,
                        absorbedPkAttr.getDataType() != null
                                ? absorbedPkAttr.getDataType().length()
                                : 0,
                        List.of()));

        entities.add(
                new ErEntityDTO(
                        absorbedUuid,
                        absorbedName,
                        absorbedPos,
                        false,
                        List.of(),
                        List.of(absorbedPkAttr.getUuid())));

        buildEntity(node, graph, visited, circlePos(pos, 0, 2, 120), entities, attributes);

        List<ErRelationshipParticipantDTO> participants =
                List.of(
                        new ErRelationshipParticipantDTO(node.getUuid(), "1", "1", ""),
                        new ErRelationshipParticipantDTO(absorbedUuid, "1", "1", ""));

        relationships.add(
                new ErRelationshipDTO(
                        UUID.randomUUID().toString(),
                        node.getName() + absorbedName,
                        pos,
                        "Normal",
                        null,
                        participants,
                        List.of()));
    }

    private void buildEntity(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (!visited.contains(node.getUuid())) {
            visited.add(node.getUuid());

            List<String> attrs = new ArrayList<>();
            List<String> pks = new ArrayList<>();

            List<Node> ownAttrList =
                    Graphs.successorListOf(graph, node).stream()
                            .filter(n -> n.isAttribute() && !n.isFk())
                            .toList();
            int attrTotal = ownAttrList.size();
            AtomicInteger attrIdx = new AtomicInteger(0);
            ownAttrList.forEach(
                    attr -> {
                        Position attrPos = circlePos(pos, attrIdx.getAndIncrement(), attrTotal, 80);
                        attributes.add(
                                new ErAttributeDTO(
                                        attr.getUuid(),
                                        attr.getName(),
                                        attrPos,
                                        node.getUuid(),
                                        attr.isPk(),
                                        false,
                                        false,
                                        attr.isNotNull(),
                                        attr.isUnique(),
                                        attr.getDataType() != null
                                                ? attr.getDataType().domain().name()
                                                : null,
                                        attr.getDataType() != null
                                                ? attr.getDataType().length()
                                                : 0,
                                        List.of()));
                        if (attr.isPk()) {
                            pks.add(attr.getUuid());
                        } else {
                            attrs.add(attr.getUuid());
                        }
                    });

            entities.add(new ErEntityDTO(node.getUuid(), node.getName(), pos, false, attrs, pks));
        }
    }

    private void buildNArias(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (visited.contains(node.getUuid())) {
            return;
        }
        visited.add(node.getUuid());

        List<Node> ownAttrsList =
                Graphs.neighborListOf(graph, node).stream()
                        .filter(a -> a.isAttribute() && !a.isPk())
                        .toList();
        List<String> ownAttrsId = new ArrayList<>();
        AtomicInteger ownIdx = new AtomicInteger(0);
        ownAttrsList.forEach(
                a -> {
                    Position aPos =
                            circlePos(pos, ownIdx.getAndIncrement(), ownAttrsList.size(), 80);
                    attributes.add(
                            new ErAttributeDTO(
                                    a.getUuid(),
                                    a.getName(),
                                    aPos,
                                    node.getUuid(),
                                    a.isPk(),
                                    false,
                                    false,
                                    a.isNotNull(),
                                    a.isUnique(),
                                    a.getDataType() != null
                                            ? a.getDataType().domain().name()
                                            : null,
                                    a.getDataType() != null ? a.getDataType().length() : 0,
                                    List.of()));
                    ownAttrsId.add(a.getUuid());
                });

        List<ErRelationshipParticipantDTO> participants = new ArrayList<>();
        Graphs.successorListOf(graph, node).stream()
                .filter(a -> a.isAttribute() && a.isPk() && a.isFk())
                .toList()
                .forEach(
                        attr ->
                                buildParticipants(
                                        attr,
                                        graph,
                                        visited,
                                        pos,
                                        participants,
                                        entities,
                                        attributes));

        relationships.add(
                new ErRelationshipDTO(
                        node.getUuid(),
                        node.getName(),
                        pos,
                        "Normal",
                        null,
                        participants,
                        ownAttrsId));
    }

    private void buildNMRel(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (!visited.contains(node.getUuid())) {
            visited.add(node.getUuid());

            List<String> ownAttrsId = new ArrayList<>();

            List<Node> ownAttrsList =
                    Graphs.neighborListOf(graph, node).stream()
                            .filter(attr -> attr.isAttribute() && !attr.isPk())
                            .distinct()
                            .toList();
            int nmAttrTotal = ownAttrsList.size();
            AtomicInteger nmAttrIdx = new AtomicInteger(0);
            ownAttrsList.forEach(
                    a -> {
                        Position aPos =
                                circlePos(pos, nmAttrIdx.getAndIncrement(), nmAttrTotal, 80);
                        attributes.add(
                                new ErAttributeDTO(
                                        a.getUuid(),
                                        a.getName(),
                                        aPos,
                                        node.getUuid(),
                                        a.isPk(),
                                        false,
                                        false,
                                        a.isNotNull(),
                                        a.isUnique(),
                                        a.getDataType() != null
                                                ? a.getDataType().domain().name()
                                                : null,
                                        a.getDataType() != null ? a.getDataType().length() : 0,
                                        List.of()));

                        ownAttrsId.add(a.getUuid());
                    });

            List<ErRelationshipParticipantDTO> participants = new ArrayList<>();

            Graphs.successorListOf(graph, node).stream()
                    .filter(Node::isPk)
                    .collect(Collectors.toSet())
                    .forEach(
                            attr ->
                                    buildParticipants(
                                            attr,
                                            graph,
                                            visited,
                                            pos,
                                            participants,
                                            entities,
                                            attributes));

            relationships.add(
                    new ErRelationshipDTO(
                            node.getUuid(),
                            node.getName(),
                            pos,
                            "Normal",
                            null,
                            participants,
                            ownAttrsId));
        }
    }

    private void buildParticipants(
            Node node,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipParticipantDTO> participants,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {
        Node entity =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals(node.getReference()))
                        .findFirst()
                        .orElse(null);

        if (entity != null) {
            participants.add(new ErRelationshipParticipantDTO(entity.getUuid(), "0", "n", ""));

            int pIdx = participants.size() - 1;
            Position entityPos = circlePos(pos, pIdx, Math.max(participants.size(), 2), 120);
            buildEntity(entity, graph, visited, entityPos, entities, attributes);
        }
    }

    private void buildOneOneRel(
            Node node,
            Node ref,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (visited.contains(node.getUuid()) && visited.contains(ref.getUuid())) {
            return;
        }

        String minNode =
                Graphs.successorListOf(graph, node).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isFk()
                                                        && a.isUnique()
                                                        && ref.getName().equals(a.getReference()))
                                .anyMatch(Node::isNotNull)
                        ? "1"
                        : "0";
        String minRef =
                Graphs.successorListOf(graph, ref).stream()
                                .filter(
                                        a ->
                                                a.isAttribute()
                                                        && a.isFk()
                                                        && a.isUnique()
                                                        && node.getName().equals(a.getReference()))
                                .anyMatch(Node::isNotNull)
                        ? "1"
                        : "0";

        List<ErRelationshipParticipantDTO> participants = new ArrayList<>();
        participants.add(new ErRelationshipParticipantDTO(node.getUuid(), minNode, "1", ""));
        participants.add(new ErRelationshipParticipantDTO(ref.getUuid(), minRef, "1", ""));

        buildEntity(node, graph, visited, circlePos(pos, 0, 2, 120), entities, attributes);
        buildEntity(ref, graph, visited, circlePos(pos, 1, 2, 120), entities, attributes);

        relationships.add(
                new ErRelationshipDTO(
                        UUID.randomUUID().toString(),
                        node.getName() + ref.getName(),
                        pos,
                        "Normal",
                        null,
                        participants,
                        List.of()));
    }

    private void buildOneNRel(
            Node node,
            Node ref,
            Graph<Node, Edge> graph,
            Set<String> visited,
            Position pos,
            List<ErRelationshipDTO> relationships,
            List<ErEntityDTO> entities,
            List<ErAttributeDTO> attributes) {

        if (visited.contains(node.getUuid()) && visited.contains(ref.getUuid())) {
            return;
        }

        List<ErRelationshipParticipantDTO> participants = new ArrayList<>();

        String min =
                Graphs.successorListOf(graph, node).stream()
                                .filter(
                                        a ->
                                                a.getReference() != null
                                                        && a.getReference().equals(ref.getName())
                                                        && a.isNotNull())
                                .collect(Collectors.toSet())
                                .isEmpty()
                        ? "0"
                        : "1";
        participants.add(new ErRelationshipParticipantDTO(node.getUuid(), min, "n", ""));

        participants.add(new ErRelationshipParticipantDTO(ref.getUuid(), "0", "1", ""));

        buildEntity(node, graph, visited, circlePos(pos, 0, 2, 120), entities, attributes);
        buildEntity(ref, graph, visited, circlePos(pos, 1, 2, 120), entities, attributes);

        relationships.add(
                new ErRelationshipDTO(
                        UUID.randomUUID().toString(),
                        node.getName() + ref.getName(),
                        pos,
                        "Normal",
                        null,
                        participants,
                        List.of()));
    }

    private Position circlePos(Position center, int index, int total, double radius) {
        if (total == 0) {
            return center;
        }
        double angle = Math.PI * index / total;
        return new Position(
                center.x() + radius * Math.cos(angle), center.y() + radius * Math.sin(angle));
    }
}
