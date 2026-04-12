package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import java.util.LinkedHashMap;
import org.jgrapht.Graph;
import org.jgrapht.graph.Multigraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogicalDiagramStrategyTest {

    private LogicalDiagramStrategy strategy;
    private Diagram diagram;

    @BeforeEach
    void setUp() {
        strategy = new LogicalDiagramStrategy();

        Graph<Node, Edge> graph = new Multigraph<>(Edge.class);

        Node entity1 = Node.builder().name("A").build();
        graph.addVertex(entity1);

        Node attribute1 =
                Node.builder().name("b").isAttribute(true).isPk(true).reference("A").build();
        Node attribute2 =
                Node.builder()
                        .name("c")
                        .isAttribute(true)
                        .isUnique(true)
                        .isNotNull(true)
                        .dataType(Domain.VARCHAR)
                        .build();

        graph.addVertex(attribute1);
        graph.addVertex(attribute2);
        graph.addEdge(
                entity1,
                attribute1,
                Edge.builder().label("attr" + entity1.getName() + attribute1.getName()).build());
        graph.addEdge(
                entity1,
                attribute2,
                Edge.builder().label("attr" + entity1.getName() + attribute2.getName()).build());

        Node entity2 = Node.builder().name("D").build();
        graph.addVertex(entity2);

        Node attribute3 =
                Node.builder()
                        .name("e")
                        .isAttribute(true)
                        .isPk(true)
                        .dataType(Domain.INTEGER)
                        .reference("A")
                        .build();
        Node attribute4 = Node.builder().name("f").isAttribute(true).build();

        graph.addVertex(attribute3);
        graph.addVertex(attribute4);
        graph.addEdge(
                entity2,
                attribute3,
                Edge.builder().label("attr" + entity2.getName() + attribute3.getName()).build());
        graph.addEdge(
                entity2,
                attribute4,
                Edge.builder().label("attr" + entity2.getName() + attribute4.getName()).build());
        graph.addEdge(
                entity1,
                attribute3,
                Edge.builder().label("attr" + entity1.getName() + attribute3.getName()).build());

        diagram = Diagram.builder().diagram(graph).build();
    }

    @Test
    void testGenerateSingleEntity() {
        LogicalInput input = new LogicalInput("A (b, c)", "", "");

        Diagram diagram = strategy.generate(input);

        assertNotNull(diagram.getDiagram());
        assertEquals(
                1, diagram.getDiagram().vertexSet().stream().filter(n -> !n.isAttribute()).count());
        assertEquals(
                2, diagram.getDiagram().vertexSet().stream().filter(Node::isAttribute).count());
        assertTrue(
                diagram.getDiagram().vertexSet().stream()
                        .anyMatch(n -> !n.isAttribute() && n.getName().equals("A")));
        assertTrue(
                diagram.getDiagram().vertexSet().stream()
                        .anyMatch(n -> n.isAttribute() && n.getName().equals("b")));
    }

    @Test
    void testGeneratePkAttribute() {
        LogicalInput input = new LogicalInput("A (__b__, c)", "", "");

        Diagram diagram = strategy.generate(input);

        Node attribute =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("b"))
                        .findFirst()
                        .orElseThrow();
        assertTrue(attribute.isPk());
    }

    @Test
    void testGenerateReferencesWithRestrictionInput() {
        LogicalInput input =
                new LogicalInput(
                        """
                                A (__b__, c)
                                D (__b__, e)
                                """,
                        """
                                D.b -> A.b
                                """,
                        "");

        Diagram diagram = strategy.generate(input);

        Node referenced =
                diagram.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("b"))
                        .findFirst()
                        .orElseThrow();
        assertEquals("A", referenced.getReference());
    }

    @Test
    void testGenerateGraphWithEmptyInput() throws Exception {
        LogicalInput input = new LogicalInput("", "", "");

        Diagram diagram = strategy.generate(input);

        assertTrue(diagram.getDiagram().vertexSet().isEmpty());
    }

    @Test
    void testTransformGraphIntoLinkedHashMap() {
        Object raw = strategy.transform(diagram);
        assertInstanceOf(LinkedHashMap.class, raw);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result = (LinkedHashMap<String, String>) raw;

        assertNotNull(result.get("relationship"));
        assertNotNull(result.get("restriction"));
        assertNotNull(result.get("lossRestriction"));
    }

    @Test
    void testTransformRestrictionUsesAttrFk() {
        @SuppressWarnings("unchecked")
        String restriction =
                ((LinkedHashMap<String, String>) strategy.transform(diagram)).get("restriction");
        assertTrue(restriction.contains("D.e -> A.e"));
    }

    @Test
    void testPkAttributeDataTypeIsInteger() {
        LogicalInput input = new LogicalInput("A (__b__)", "", "");
        Diagram d = strategy.generate(input);

        Node pk =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("b"))
                        .findFirst()
                        .orElseThrow();

        assertEquals(Domain.INTEGER, pk.getDataType());
    }

    @Test
    void testSharedAttributeCreateOneVertex() {
        LogicalInput input =
                new LogicalInput(
                        """
                                A (__c__)
                                B (__c__)
                                """,
                        "",
                        "");
        Diagram d = strategy.generate(input);

        assertEquals(
                1,
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("c"))
                        .count());
    }

    @Test
    void testRoundTripRelationship() {
        LogicalInput input = new LogicalInput("A (__b__, c) ", "", "");
        Diagram diagram = strategy.generate(input);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result =
                (LinkedHashMap<String, String>) strategy.transform(diagram);

        assertTrue(result.get("relationship").contains("A"));
        assertTrue(result.get("relationship").contains("__b__"));
        assertTrue(result.get("relationship").contains("c"));
    }

    @Test
    void testRoundTripRestriction() {
        LogicalInput input =
                new LogicalInput(
                        """
                                A (__b__, c)
                                D (__b__, e)
                                """,
                        """
                                D.b -> A.b
                                """,
                        "");
        Diagram diagram = strategy.generate(input);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result =
                (LinkedHashMap<String, String>) strategy.transform(diagram);

        assertTrue(result.get("restriction").contains("D.b -> A.b"));
    }
}
