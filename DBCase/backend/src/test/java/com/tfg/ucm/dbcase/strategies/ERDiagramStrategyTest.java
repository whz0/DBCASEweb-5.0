package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.Undefined;
import com.tfg.ucm.dbcase.dto.input.ErAttributeDTO;
import com.tfg.ucm.dbcase.dto.input.ErEntityDTO;
import com.tfg.ucm.dbcase.dto.input.ErInput;
import com.tfg.ucm.dbcase.dto.input.ErRelationshipDTO;
import com.tfg.ucm.dbcase.dto.input.ErRelationshipParticipantDTO;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Pendiente de implementación")
class ERDiagramStrategyTest {

    private ERDiagramStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ERDiagramStrategy();
    }

    @Test
    void testGenerateEntityVertex() throws Exception {
        ErInput input =
                new ErInput(
                        List.of(new ErEntityDTO("e1", "A", null, false, List.of(), List.of())),
                        List.of(),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);

        Node entity =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("A"))
                        .findFirst()
                        .orElse(null);
        assertNotNull(entity);
        assertInstanceOf(Entity.class, entity);
    }

    @Test
    void testGenerateWeakEntity() {
        ErInput input =
                new ErInput(
                        List.of(new ErEntityDTO("e1", "B", null, true, List.of(), List.of())),
                        List.of(),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);

        Entity entity =
                (Entity)
                        diagram.getDiagram().vertexSet().stream()
                                .filter(n -> n.getName().equals("B"))
                                .findFirst()
                                .orElseThrow();
        assertTrue(entity.isWeak());
    }

    @Test
    void testGenerateAttributeLinkedToEntity() {
        ErInput input =
                new ErInput(
                        List.of(new ErEntityDTO("e1", "A", null, false, List.of("a2"), List.of())),
                        List.of(),
                        List.of(
                                new ErAttributeDTO(
                                        "a2", "C", null, "e1", false, false, false, false, false,
                                        null, 0, List.of())),
                        List.of());

        Diagram diagram = strategy.generate(input);
        Graph<Node, Edge> graph = diagram.getDiagram();

        Node entity =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals("A"))
                        .findFirst()
                        .orElseThrow();
        Node attribute =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals("C"))
                        .findFirst()
                        .orElseThrow();

        assertInstanceOf(Attribute.class, attribute);
        assertTrue(graph.containsEdge(entity, attribute));
    }

    @Test
    void testGeneratePrimaryKeyAttribute() {
        ErInput input =
                new ErInput(
                        List.of(
                                new ErEntityDTO(
                                        "e1", "A", null, false, List.of("a1"), List.of("a1"))),
                        List.of(),
                        List.of(
                                new ErAttributeDTO(
                                        "a1", "B", null, "e1", true, false, false, false, false,
                                        null, 0, List.of())),
                        List.of());

        Diagram diagram = strategy.generate(input);

        Attribute attr =
                (Attribute)
                        diagram.getDiagram().vertexSet().stream()
                                .filter(n -> n.getName().equals("B"))
                                .findFirst()
                                .orElseThrow();
        assertTrue(attr.isPk());
    }

    @Test
    void testGenerateAttributeFkSetWhenParentIsRelationship() {
        ErInput input =
                new ErInput(
                        List.of(
                                new ErEntityDTO("e1", "A", null, false, List.of(), List.of()),
                                new ErEntityDTO("e2", "B", null, false, List.of(), List.of())),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1",
                                        "R",
                                        null,
                                        "Normal",
                                        List.of(
                                                new ErRelationshipParticipantDTO(
                                                        "e1", "1", "N", null),
                                                new ErRelationshipParticipantDTO(
                                                        "e2", "1", "N", null)),
                                        List.of("a1"))),
                        List.of(
                                new ErAttributeDTO(
                                        "a1", "X", null, "r1", false, false, false, false, false,
                                        null, 0, List.of())),
                        List.of());

        Diagram diagram = strategy.generate(input);

        Attribute attr =
                (Attribute)
                        diagram.getDiagram().vertexSet().stream()
                                .filter(n -> n.getName().equals("X"))
                                .findFirst()
                                .orElseThrow();
        assertEquals("R", attr.getFk());
    }

    @Test
    void testGenerateAttributeFkNullWhenParentIsEntity() {
        ErInput input =
                new ErInput(
                        List.of(new ErEntityDTO("e1", "A", null, false, List.of("a1"), List.of())),
                        List.of(),
                        List.of(
                                new ErAttributeDTO(
                                        "a1", "X", null, "e1", false, false, false, false, false,
                                        null, 0, List.of())),
                        List.of());

        Diagram diagram = strategy.generate(input);

        Attribute attr =
                (Attribute)
                        diagram.getDiagram().vertexSet().stream()
                                .filter(n -> n.getName().equals("X"))
                                .findFirst()
                                .orElseThrow();
        assertNull(attr.getFk());
    }

    @Test
    void testGenerateRelationshipWithParticipants() {
        ErInput input =
                new ErInput(
                        List.of(
                                new ErEntityDTO("e1", "A", null, false, List.of(), List.of()),
                                new ErEntityDTO("e2", "B", null, false, List.of(), List.of())),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1",
                                        "C",
                                        null,
                                        "Normal",
                                        List.of(
                                                new ErRelationshipParticipantDTO(
                                                        "e1", "1", "N", null),
                                                new ErRelationshipParticipantDTO(
                                                        "e2", "1", "1", null)),
                                        List.of())),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);
        Graph<Node, Edge> graph = diagram.getDiagram();

        Node rel =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals("C"))
                        .findFirst()
                        .orElse(null);
        assertNotNull(rel);
        assertInstanceOf(Relationship.class, rel);

        Relationship relationship = (Relationship) rel;
        assertEquals(2, relationship.getParticipants().size());
        assertTrue(
                relationship.getParticipants().stream()
                        .anyMatch(p -> p.getEntity().getName().equals("A")));
        assertTrue(
                relationship.getParticipants().stream()
                        .anyMatch(p -> p.getEntity().getName().equals("B")));
    }

    @Test
    void testGenerateRelationshipCardinalityStoredInParticipant() {
        ErInput input =
                new ErInput(
                        List.of(
                                new ErEntityDTO("e1", "A", null, false, List.of(), List.of()),
                                new ErEntityDTO("e2", "B", null, false, List.of(), List.of())),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1",
                                        "R",
                                        null,
                                        "Normal",
                                        List.of(
                                                new ErRelationshipParticipantDTO(
                                                        "e1", "1", "N", null),
                                                new ErRelationshipParticipantDTO(
                                                        "e2", "1", "N", null)),
                                        List.of())),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);

        Relationship rel =
                (Relationship)
                        diagram.getDiagram().vertexSet().stream()
                                .filter(n -> n.getName().equals("R"))
                                .findFirst()
                                .orElseThrow();
        assertTrue(rel.isNM());
    }

    @Test
    void testGenerateBfsFromEntityReachesAttributes() {
        ErInput input =
                new ErInput(
                        List.of(
                                new ErEntityDTO(
                                        "e1",
                                        "A",
                                        null,
                                        false,
                                        List.of("a1", "a2"),
                                        List.of("a1"))),
                        List.of(),
                        List.of(
                                new ErAttributeDTO(
                                        "a1", "B", null, "e1", true, false, false, false, false,
                                        null, 0, List.of()),
                                new ErAttributeDTO(
                                        "a2", "C", null, "e1", false, false, false, true, false,
                                        null, 0, List.of())),
                        List.of());

        Diagram diagram = strategy.generate(input);
        Graph<Node, Edge> graph = diagram.getDiagram();

        Node entity =
                graph.vertexSet().stream()
                        .filter(n -> n.getName().equals("A"))
                        .findFirst()
                        .orElseThrow();
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
    void testGenerateZeroParticipantsCreatesUndefined() {
        ErInput input =
                new ErInput(
                        List.of(),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1",
                                        "SuspiciousRel",
                                        null,
                                        "Normal",
                                        List.of(),
                                        List.of())),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);

        Node node =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("SuspiciousRel"))
                        .findFirst()
                        .orElse(null);
        assertNotNull(node);
        assertInstanceOf(Undefined.class, node);
    }

    @Test
    void testGenerateOneParticipantCreatesUndefined() {
        ErInput input =
                new ErInput(
                        List.of(new ErEntityDTO("e1", "A", null, false, List.of(), List.of())),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1",
                                        "SuspiciousRel",
                                        null,
                                        "Normal",
                                        List.of(
                                                new ErRelationshipParticipantDTO(
                                                        "e1", "1", "N", null)),
                                        List.of())),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);

        Node node =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.getName().equals("SuspiciousRel"))
                        .findFirst()
                        .orElse(null);
        assertNotNull(node);
        assertInstanceOf(Undefined.class, node);
    }

    @Test
    void testTransformReturnsErInput() {
        ErInput input =
                new ErInput(
                        List.of(new ErEntityDTO("e1", "A", null, false, List.of(), List.of())),
                        List.of(),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);
        Object result = strategy.transform(diagram);

        assertInstanceOf(ErInput.class, result);
    }

    @Test
    void testRoundTripEntities() {
        ErInput input =
                new ErInput(
                        List.of(new ErEntityDTO("e1", "A", null, false, List.of(), List.of())),
                        List.of(),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);
        ErInput result = (ErInput) strategy.transform(diagram);

        assertEquals(1, result.entities().size());
        assertEquals("A", result.entities().get(0).name());
    }

    @Test
    void testRoundTripAttributes() {
        ErInput input =
                new ErInput(
                        List.of(
                                new ErEntityDTO(
                                        "e1", "A", null, false, List.of("a1"), List.of("a1"))),
                        List.of(),
                        List.of(
                                new ErAttributeDTO(
                                        "a1", "B", null, "e1", true, false, false, false, false,
                                        null, 0, List.of())),
                        List.of());

        Diagram diagram = strategy.generate(input);
        ErInput result = (ErInput) strategy.transform(diagram);

        assertEquals(1, result.attributes().size());
        ErAttributeDTO attr = result.attributes().get(0);
        assertEquals("B", attr.name());
        assertTrue(attr.isKey());
    }

    @Test
    void testRoundTripRelationship() {
        ErInput input =
                new ErInput(
                        List.of(
                                new ErEntityDTO("e1", "A", null, false, List.of(), List.of()),
                                new ErEntityDTO("e2", "B", null, false, List.of(), List.of())),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1",
                                        "C",
                                        null,
                                        "Normal",
                                        List.of(
                                                new ErRelationshipParticipantDTO(
                                                        "e1", "1", "N", null),
                                                new ErRelationshipParticipantDTO(
                                                        "e2", "1", "1", null)),
                                        List.of())),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);
        ErInput result = (ErInput) strategy.transform(diagram);

        assertEquals(1, result.relationships().size());
        assertEquals("C", result.relationships().get(0).name());
        assertEquals(2, result.relationships().get(0).participants().size());
    }

    @Test
    void testTransformUndefinedAddsQuestionMark() {
        ErInput input =
                new ErInput(
                        List.of(),
                        List.of(
                                new ErRelationshipDTO(
                                        "r1", "Unclear", null, "Normal", List.of(), List.of())),
                        List.of(),
                        List.of());

        Diagram diagram = strategy.generate(input);
        ErInput result = (ErInput) strategy.transform(diagram);

        assertEquals(1, result.relationships().size());
        assertTrue(result.relationships().get(0).name().endsWith("?"));
    }
}
