package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NodeClassifierTest {

    private static Graph<Node, Edge> graph(Node... nodes) {
        Graph<Node, Edge> g = new DirectedPseudograph<>(Edge.class);
        for (Node n : nodes) g.addVertex(n);
        return g;
    }

    private static Node entity(String name) {
        return Node.builder()
                .uuid(UUID.randomUUID().toString())
                .name(name)
                .isAttribute(false)
                .build();
    }

    private static Node attr(String name, boolean pk, boolean fk, String ref) {
        return Node.builder()
                .uuid(UUID.randomUUID().toString())
                .name(name)
                .isAttribute(true)
                .isPk(pk)
                .isFk(fk)
                .reference(ref)
                .build();
    }

    private static void connect(Graph<Node, Edge> g, Node src, Node tgt) {
        g.addEdge(
                src,
                tgt,
                Edge.builder()
                        .uuid(UUID.randomUUID().toString())
                        .label(src.getName() + "->" + tgt.getName())
                        .build());
    }

    private static Stream<Arguments> isEntityProvider() {
        Node e1 = entity("E1");
        Node pk1 = attr("pk1", true, false, null);
        Graph<Node, Edge> g1 = graph(e1, pk1);
        connect(g1, e1, pk1);

        Node rel = entity("Rel");
        Node fkPk = attr("fkpk", true, true, "Other");
        Graph<Node, Edge> g2 = graph(rel, fkPk);
        connect(g2, rel, fkPk);

        Node e2 = entity("E2");
        Node fkOnly = attr("fk1", false, true, "Other");
        Graph<Node, Edge> g3 = graph(e2, fkOnly);
        connect(g3, e2, fkOnly);

        Node attrNode = attr("a", false, false, null);
        Graph<Node, Edge> g4 = graph(attrNode);

        return Stream.of(
                Arguments.of(e1, g1, true),
                Arguments.of(rel, g2, false),
                Arguments.of(e2, g3, false),
                Arguments.of(attrNode, g4, false));
    }

    @ParameterizedTest
    @MethodSource("isEntityProvider")
    void testIsEntity(Node node, Graph<Node, Edge> g, boolean expected) {
        assertEquals(expected, NodeClassifier.isEntity(node, g));
    }

    private static Stream<Arguments> isNMRelProvider() {
        Node nm = entity("NM");
        Node fkPk1 = attr("fk1", true, true, "A");
        Node fkPk2 = attr("fk2", true, true, "B");
        Graph<Node, Edge> g1 = graph(nm, fkPk1, fkPk2);
        connect(g1, nm, fkPk1);
        connect(g1, nm, fkPk2);

        Node ent = entity("E");
        Node ownPk = attr("id", true, false, null);
        Graph<Node, Edge> g2 = graph(ent, ownPk);
        connect(g2, ent, ownPk);

        Node e3 = entity("E3");
        Graph<Node, Edge> g3 = graph(e3);

        return Stream.of(
                Arguments.of(nm, g1, true),
                Arguments.of(ent, g2, false),
                Arguments.of(e3, g3, false));
    }

    @ParameterizedTest
    @MethodSource("isNMRelProvider")
    void testIsNMRel(Node node, Graph<Node, Edge> g, boolean expected) {
        assertEquals(expected, NodeClassifier.isNMRel(node, g));
    }

    @Test
    void hasUniqueFkReturnsFkWithUniqueFlag() {
        Node owner = entity("Owner");
        Node unique = attr("fk_unique", false, true, "Other");
        unique.setUnique(true);
        Node plain = attr("fk_plain", false, true, "Another");

        Graph<Node, Edge> g = graph(owner, unique, plain);
        connect(g, owner, unique);
        connect(g, owner, plain);

        Set<Node> result = NodeClassifier.hasUniqueFk(owner, g);
        assertEquals(1, result.size());
        assertTrue(result.contains(unique));
    }

    @Test
    void hasUniqueFkReturnsEmptyWhenNoUniqueFk() {
        Node owner = entity("Owner");
        Node plain = attr("fk", false, true, "Other");

        Graph<Node, Edge> g = graph(owner, plain);
        connect(g, owner, plain);

        assertTrue(NodeClassifier.hasUniqueFk(owner, g).isEmpty());
    }

    @Test
    void isTotalRelReturnsTrueWhenBothSidesHaveNotNullUniqueFk() {
        Node n1 = entity("N1");
        Node n2 = entity("N2");

        Node fk1 = attr("fk1", false, true, "N2");
        fk1.setUnique(true);
        fk1.setNotNull(true);

        Node fk2 = attr("fk2", false, true, "N1");
        fk2.setUnique(true);
        fk2.setNotNull(true);

        Graph<Node, Edge> g = graph(n1, n2, fk1, fk2);
        connect(g, n1, fk1);
        connect(g, n2, fk2);

        assertTrue(NodeClassifier.isTotalRel(n1, n2, g));
    }

    @Test
    void isTotalRelReturnsFalseWhenOneSideMissing() {
        Node n1 = entity("N1");
        Node n2 = entity("N2");

        Node fk1 = attr("fk1", false, true, "N2");
        fk1.setUnique(true);
        fk1.setNotNull(true);

        Graph<Node, Edge> g = graph(n1, n2, fk1);
        connect(g, n1, fk1);

        assertFalse(NodeClassifier.isTotalRel(n1, n2, g));
    }
}
