package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.*;

import com.tfg.ucm.dbcase.dto.*;
import com.tfg.ucm.dbcase.dto.input.*;
import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class ERDiagramStrategyTest {

    private ERDiagramStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ERDiagramStrategy();
    }

    @Test
    void testGenerateEntityVertex() throws Exception {
        ErInput input = new ErInput(List.of(new ErEntityDTO("e1",
                "A",
                null,
                false,
                List.of(),
                List.of())),
                List.of(),
                List.of(),
                List.of());

        Diagram diagram = strategy.generate(input);

        Node entity = diagram.getDiagram().vertexSet().stream()
                .filter(n -> n.getName().equals("A")).findFirst().orElse(null);
        assertNotNull(entity);
        assertInstanceOf(Entity.class, entity);
    }

    @Test
    void testGenerateWeakEntity() throws Exception {
        ErInput input = new ErInput(List.of(new ErEntityDTO("e1",
                "B",
                null,
                true,
                List.of(),
                List.of())),
                List.of(),
                List.of(),
                List.of());

        Diagram diagram = strategy.generate(input);

        Entity entity = (Entity) diagram.getDiagram().vertexSet().stream()
                .filter(n -> n.getName().equals("B")).findFirst().orElseThrow();
        assertTrue(entity.isWeak());
    }

    @Test
    void testGenerateAttributeLinkedToEntity() throws Exception {
        ErInput input = new ErInput(List.of(new ErEntityDTO("e1",
                "A",
                null,
                false,
                List.of("B"),
                List.of())),
                List.of(),
                List.of(new ErAttributeDTO("a2",
                        "C",
                        null,
                        "e1",
                        false,
                        false,
                        false,
                        false,
                        false,
                        null,
                        0,
                        List.of())),
                List.of());

        Diagram diagram = strategy.generate(input);
        Graph<Node, Edge> graph = diagram.getDiagram();

        Node entity = graph.vertexSet().stream()
                .filter(n -> n.getName().equals("A")).findFirst().orElseThrow();
        Node attribute = graph.vertexSet().stream()
                .filter(n -> n.getName().equals("C")).findFirst().orElseThrow();

        assertInstanceOf(Attribute.class, attribute);
        assertTrue(graph.containsEdge(entity, attribute));
    }

    @Test
    void testGeneratePrimaryKeyAttribute() throws Exception {
        ErInput input = new ErInput(List.of(new ErEntityDTO("e1",
                "A",
                null,
                false,
                List.of("a1"),
                List.of("a1"))),
                List.of(),
                List.of(new ErAttributeDTO("a1",
                        "B",
                        null,
                        "e1",
                        true,
                        false,
                        false,
                        false,
                        false,
                        null,
                        0,
                        List.of())),
                List.of());

        Diagram diagram = strategy.generate(input);

        Attribute attr = (Attribute) diagram.getDiagram().vertexSet().stream()
                .filter(n -> n.getName().equals("B")).findFirst().orElseThrow();
        assertTrue(attr.isPk());
    }

    @Test
    void testGenerateRelationshipWithParticipants() throws Exception {
        ErInput input = new ErInput(List.of(
                new ErEntityDTO("e1",
                        "A",
                        null,
                        false,
                        List.of(),
                        List.of()),
                new ErEntityDTO("e2",
                        "B",
                        null,
                        false,
                        List.of(),
                        List.of())),
                List.of(
                        new ErRelationshipDTO("r1",
                                "C",
                                null,
                                "Normal",
                                List.of(
                                        new ErRelationshipParticipantDTO("e1",
                                                "1",
                                                "N",
                                                null),
                                        new ErRelationshipParticipantDTO("e2",
                                                "1",
                                                "1",
                                                null)),
                                List.of())),
                List.of(),
                List.of());

        Diagram diagram = strategy.generate(input);
        Graph<Node, Edge> graph = diagram.getDiagram();

        Node rel = graph.vertexSet().stream().filter(n -> n.getName().equals("C")).findFirst().orElse(null);
        assertNotNull(rel);
        assertInstanceOf(Relationship.class, rel);

        Node node1 = graph.vertexSet().stream().filter(n -> n.getName().equals("A")).findFirst().orElseThrow();
        Node node2 = graph.vertexSet().stream().filter(n -> n.getName().equals("B")).findFirst().orElseThrow();
        assertTrue(graph.containsEdge(node1, rel));
        assertTrue(graph.containsEdge(node2, rel));
    }

    @Test
    void testGenerateBfsFromEntityReachesAttributes() throws Exception {
        ErInput input = new ErInput(List.of(
                new ErEntityDTO("e1",
                        "A",
                        null,
                        false,
                        List.of("a1", "a2"),
                        List.of("a1"))),
                List.of(),
                List.of(new ErAttributeDTO("a1",
                                "B",
                                null,
                                "e1",
                                true,
                                false,
                                false,
                                false,
                                false,
                                null,
                                0,
                                List.of()),
                        new ErAttributeDTO("a2",
                                "C",
                                null,
                                "e1",
                                false,
                                false,
                                false,
                                true,
                                false,
                                null,
                                0,
                                List.of())),
                List.of());

        Diagram diagram = strategy.generate(input);
        Graph<Node, Edge> graph = diagram.getDiagram();

        Node entity = graph.vertexSet().stream()
                .filter(n -> n.getName().equals("A")).findFirst().orElseThrow();
        BreadthFirstIterator<Node, Edge> bfs = new BreadthFirstIterator<>(graph, entity);
        bfs.next();

        int count = 0;
        while (bfs.hasNext()) {
            Node node = bfs.next();
            assertInstanceOf(Attribute.class, node);
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    void testTransformReturnsErInput() throws Exception {
        ErInput input = new ErInput(List.of(new ErEntityDTO("e1",
                "A",
                null,
                false,
                List.of(),
                List.of())),
                List.of(),
                List.of(),
                List.of());

        Diagram diagram = strategy.generate(input);
        Object result = strategy.transform(diagram);

        assertInstanceOf(ErInput.class, result);
    }

    @Test
    void testRoundTripEntities() throws Exception {
        ErInput input = new ErInput(List.of(new ErEntityDTO("e1",
                "A",
                null,
                false,
                List.of(),
                List.of())),
                List.of(),
                List.of(),
                List.of());

        Diagram diagram = strategy.generate(input);
        ErInput result = (ErInput) strategy.transform(diagram);

        assertEquals(1, result.entities().size());
        assertEquals("A", result.entities().get(0).name());
    }

    @Test
    void testRoundTripAttributes() throws Exception {
        ErInput input = new ErInput(List.of(new ErEntityDTO("e1",
                "A",
                null,
                false,
                List.of("a1"),
                List.of("a1"))),
                List.of(),
                List.of(new ErAttributeDTO("a1",
                        "B",
                        null,
                        "e1",
                        true,
                        false,
                        false,
                        false,
                        false,
                        null,
                        0,
                        List.of())),
                List.of());

        Diagram diagram = strategy.generate(input);
        ErInput result = (ErInput) strategy.transform(diagram);

        assertEquals(1, result.attributes().size());
        ErAttributeDTO attr = result.attributes().get(0);
        assertEquals("B", attr.name());
        assertTrue(attr.isKey());
    }

    @Test
    void testRoundTripRelationship() throws Exception {
        ErInput input = new ErInput(List.of(new ErEntityDTO("e1",
                        "A",
                        null,
                        false,
                        List.of(),
                        List.of()),
                new ErEntityDTO("e2",
                        "B",
                        null,
                        false,
                        List.of(),
                        List.of())),
                List.of(new ErRelationshipDTO("r1",
                        "C",
                        null,
                        "Normal",
                        List.of(new ErRelationshipParticipantDTO("e1",
                                        "1",
                                        "N",
                                        null),
                                new ErRelationshipParticipantDTO("e2",
                                        "1",
                                        "1",
                                        null)),
                        List.of())),
                List.of(),
                List.of());

        Diagram diagram = strategy.generate(input);
        ErInput result = (ErInput) strategy.transform(diagram);

        assertEquals(1, result.relationships().size());
        assertEquals("C", result.relationships().get(0).name());
        assertEquals(2, result.relationships().get(0).participants().size());
    }
}
