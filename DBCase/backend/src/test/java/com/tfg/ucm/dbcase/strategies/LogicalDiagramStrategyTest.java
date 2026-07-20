package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.DataType;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import java.util.LinkedHashMap;
import java.util.UUID;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogicalDiagramStrategyTest {

    private LogicalDiagramStrategy strategy;

    private Diagram diagram;

    @BeforeEach
    void setUp() {
        strategy = new LogicalDiagramStrategy();
        diagram = buildDiagram();
    }

    private Diagram buildDiagram() {
        Graph<Node, Edge> graph = new DirectedPseudograph<>(Edge.class);

        Node entityA = node("A");
        Node attrB = attr("b", true, false, null, DataType.of(Domain.INTEGER));
        Node attrC = attr("c", false, false, null, DataType.of(Domain.VARCHAR));
        attrC.setUnique(true);
        attrC.setNotNull(true);

        Node entityD = node("D");
        Node attrE = attr("e", true, true, "A", DataType.of(Domain.INTEGER));
        attrE.setNotNull(true);
        Node attrF = attr("f", false, false, null, null);

        for (Node n : new Node[] {entityA, attrB, attrC, entityD, attrE, attrF}) {
            graph.addVertex(n);
        }
        edge(graph, entityA, attrB);
        edge(graph, entityA, attrC);
        edge(graph, entityD, attrE);
        edge(graph, entityD, attrF);
        edge(graph, attrE, attrB);

        return Diagram.builder().diagram(graph).build();
    }

    private Node node(String name) {
        return Node.builder()
                .uuid(UUID.randomUUID().toString())
                .name(name)
                .isAttribute(false)
                .build();
    }

    private Node attr(String name, boolean pk, boolean fk, String ref, DataType dt) {
        return Node.builder()
                .uuid(UUID.randomUUID().toString())
                .name(name)
                .isAttribute(true)
                .isPk(pk)
                .isFk(fk)
                .reference(ref)
                .dataType(dt)
                .build();
    }

    private void edge(Graph<Node, Edge> g, Node src, Node tgt) {
        g.addEdge(
                src,
                tgt,
                Edge.builder()
                        .uuid(UUID.randomUUID().toString())
                        .label(src.getName() + "->" + tgt.getName())
                        .build());
    }

    @Test
    void generateSingleEntityCreatesNode() {
        LogicalInput input = new LogicalInput("A (b, c)", "", "");
        Diagram d = strategy.generate(input);

        assertNotNull(d.getDiagram());
        assertEquals(1, d.getDiagram().vertexSet().stream().filter(n -> !n.isAttribute()).count());
        assertTrue(
                d.getDiagram().vertexSet().stream()
                        .anyMatch(n -> !n.isAttribute() && n.getName().equals("A")));
    }

    @Test
    void generatePkAttributeSetsPkFlag() {
        LogicalInput input = new LogicalInput("A (__b__, c)", "", "");
        Diagram d = strategy.generate(input);

        Node pk =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("b"))
                        .findFirst()
                        .orElseThrow();
        assertTrue(pk.isPk());
    }

    @Test
    void generateNullableAttributeMarkedNotNull_false() {
        LogicalInput input = new LogicalInput("A (__b__, c*)", "", "");
        Diagram d = strategy.generate(input);

        Node nullable =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.getName().equals("c"))
                        .findFirst()
                        .orElseThrow();
        assertTrue(!nullable.isNotNull());
    }

    @Test
    void generateRestrictionSetsFkReference() {
        LogicalInput input =
                new LogicalInput(
                        """
                A (__b__, c)
                D (__b__, e)
                """,
                        "D.b -> A.b",
                        "");

        Diagram d = strategy.generate(input);

        Node fkB =
                d.getDiagram().vertexSet().stream()
                        .filter(n -> n.isAttribute() && n.isFk() && "A".equals(n.getReference()))
                        .findFirst()
                        .orElse(null);
        assertNotNull(fkB);
    }

    @Test
    void generateEmptyInputProducesEmptyGraph() {
        LogicalInput input = new LogicalInput("", "", "");
        Diagram d = strategy.generate(input);
        assertTrue(d.getDiagram().vertexSet().isEmpty());
    }

    @Test
    void transformReturnsLinkedHashMap() {
        Object raw = strategy.transform(diagram);
        assertInstanceOf(LinkedHashMap.class, raw);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result = (LinkedHashMap<String, String>) raw;
        assertNotNull(result.get("relationship"));
        assertNotNull(result.get("restriction"));
        assertNotNull(result.get("lossRestriction"));
    }

    @Test
    void transformRelationshipContainsPkSyntax() {
        @SuppressWarnings("unchecked")
        String rel =
                ((LinkedHashMap<String, String>) strategy.transform(diagram)).get("relationship");
        assertTrue(rel.contains("__b__"));
    }

    @Test
    void transformSingleFkRestrictionUsesSimpleNotation() {
        @SuppressWarnings("unchecked")
        String restriction =
                ((LinkedHashMap<String, String>) strategy.transform(diagram)).get("restriction");
        assertTrue(restriction.contains("D.e -> A.b"));
    }

    @Test
    void transformCompositeFkGroupedWithBraces() throws Exception {
        Graph<Node, Edge> graph = new DirectedPseudograph<>(Edge.class);

        Node entityA = node("A");
        Node pkId1 = attr("id1", true, false, null, DataType.of(Domain.INTEGER));
        Node pkId2 = attr("id2", true, false, null, DataType.of(Domain.INTEGER));

        Node entityD = node("D");
        Node fkId1 = attr("d_id1", true, true, "A", DataType.of(Domain.INTEGER));
        fkId1.setNotNull(true);
        Node fkId2 = attr("d_id2", true, true, "A", DataType.of(Domain.INTEGER));
        fkId2.setNotNull(true);

        for (Node n : new Node[] {entityA, pkId1, pkId2, entityD, fkId1, fkId2}) {
            graph.addVertex(n);
        }
        edge(graph, entityA, pkId1);
        edge(graph, entityA, pkId2);
        edge(graph, entityD, fkId1);
        edge(graph, entityD, fkId2);
        edge(graph, fkId1, pkId1);
        edge(graph, fkId2, pkId2);

        Diagram d = Diagram.builder().diagram(graph).build();

        @SuppressWarnings("unchecked")
        String restriction =
                ((LinkedHashMap<String, String>) strategy.transform(d)).get("restriction");

        assertTrue(restriction.contains("D.{d_id1,d_id2} -> A.{id1,id2}"));
        long lines = restriction.lines().filter(l -> l.contains("-> A.")).count();
        assertEquals(1, lines);
    }

    @Test
    void transformTotalParticipationEmitsLossRestriction() {
        @SuppressWarnings("unchecked")
        String lossRestriction =
                ((LinkedHashMap<String, String>) strategy.transform(diagram))
                        .get("lossRestriction");
        assertTrue(lossRestriction.contains("A.b -> D.e"));
    }

    @Test
    void roundTripRelationship() {
        LogicalInput input = new LogicalInput("A (__b__, c)", "", "");
        Diagram d = strategy.generate(input);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result =
                (LinkedHashMap<String, String>) strategy.transform(d);

        String rel = result.get("relationship");
        assertTrue(rel.contains("A"));
        assertTrue(rel.contains("__b__"));
        assertTrue(rel.contains("c"));
    }

    @Test
    void roundTripRestriction() {
        LogicalInput input =
                new LogicalInput(
                        """
                A (__b__, c)
                D (__b__, e)
                """,
                        "D.b -> A.b",
                        "");

        Diagram d = strategy.generate(input);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> result =
                (LinkedHashMap<String, String>) strategy.transform(d);

        assertTrue(result.get("restriction").contains("D.b -> A.b"));
    }
}
