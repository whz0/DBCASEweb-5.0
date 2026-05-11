package com.tfg.ucm.dbcase.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Disabled("Pendiente de implementación")
class NodeClassifierTest {

    private static Graph<Node, Edge> buildGraph(Node... nodes) {
        Graph<Node, Edge> g = new DirectedMultigraph<>(Edge.class);
        for (Node n : nodes) g.addVertex(n);
        return g;
    }

    private static Node entity(String name) {
        return Node.builder().uuid(name).name(name).isAttribute(false).build();
    }

    private static Node attr(String name, boolean pk, boolean fk, String ref) {
        return Node.builder()
                .uuid(name)
                .name(name)
                .isAttribute(true)
                .isPk(pk)
                .isFk(fk)
                .reference(ref)
                .build();
    }

    private static void connect(Graph<Node, Edge> g, Node src, Node tgt) {
        g.addEdge(src, tgt, Edge.builder().label(src.getName() + "->" + tgt.getName()).build());
    }

    private static Stream<Arguments> isRelationshipProvider() {
        Node e1 = entity("E1");
        Node pk1 = attr("pk1", true, false, null);
        Graph<Node, Edge> g1 = buildGraph(e1, pk1);
        connect(g1, e1, pk1);

        Node rel = entity("Rel");
        Node fkpk = attr("fkpk", true, true, "Other");
        Graph<Node, Edge> g2 = buildGraph(rel, fkpk);
        connect(g2, rel, fkpk);

        Node e2 = entity("E2");
        Node fkOnly = attr("fk1", false, true, "Other");
        Graph<Node, Edge> g3 = buildGraph(e2, fkOnly);
        connect(g3, e2, fkOnly);

        Node attrNode = attr("a", false, false, null);
        Graph<Node, Edge> g4 = buildGraph(attrNode);

        return Stream.of(
                Arguments.of(e1, g1, false),
                Arguments.of(rel, g2, true),
                Arguments.of(e2, g3, false),
                Arguments.of(attrNode, g4, false));
    }

    //    @ParameterizedTest
    //    @MethodSource("isRelationshipProvider")
    //    void testIsRelationship(Node node, Graph<Node, Edge> graph, boolean expected) {
    //        assertEquals(expected, NodeClassifier.isRelationship(node, graph));
    //    }

    @ParameterizedTest
    @MethodSource("isRelationshipProvider")
    void testIsEntity(Node node, Graph<Node, Edge> graph, boolean isRelationship) {
        boolean expectedEntity = !node.isAttribute() && !isRelationship;
        assertEquals(expectedEntity, NodeClassifier.isEntity(node, graph));
    }

    private static Stream<Arguments> isForeignKeyProvider() {
        Node owner = entity("Owner");
        Node fkExternal = attr("fk", false, true, "Other");
        Node fkSelf = attr("fk_self", false, true, "Owner");
        Node notFk = attr("plain", false, false, null);
        Node notAttr = entity("NotAttr");

        return Stream.of(
                Arguments.of(fkExternal, owner, true),
                Arguments.of(fkSelf, owner, false),
                Arguments.of(notFk, owner, false),
                Arguments.of(notAttr, owner, false));
    }

    //    @ParameterizedTest
    //    @MethodSource("isForeignKeyProvider")
    //    void testIsForeignKey(Node attr, Node owner, boolean expected) {
    //        assertEquals(expected, NodeClassifier.isForeignKey(attr, owner));
    //    }

    private static Stream<Arguments> getFkAttrNameProvider() {
        return Stream.of(
                Arguments.of("fk:Rel:attrName", "attrName"),
                Arguments.of("fk:A:B:colName", "colName"),
                Arguments.of("attr:something", null),
                Arguments.of(null, null),
                Arguments.of("fk:", ""));
    }

    //    @ParameterizedTest
    //    @MethodSource("getFkAttrNameProvider")
    //    void testGetFkAttrName(String label, String expected) {
    //        Edge edge = Edge.builder().label(label).build();
    //        assertEquals(expected, NodeClassifier.getFkAttrName(edge));
    //    }

    private static Stream<Arguments> classifyAProvider() {
        Node attrNode = attr("a", false, false, null);
        Graph<Node, Edge> g1 = buildGraph(attrNode);

        Node entityNode = entity("E");
        Graph<Node, Edge> g2 = buildGraph(entityNode);

        Node relNode = entity("Rel");
        Node fkpk = attr("fkpk", true, true, "Other");
        Graph<Node, Edge> g3 = buildGraph(relNode, fkpk);
        connect(g3, relNode, fkpk);

        return Stream.of(
                Arguments.of(attrNode, g1, NodeType.ATTRIBUTE),
                Arguments.of(entityNode, g2, NodeType.ENTITY),
                Arguments.of(relNode, g3, NodeType.RELATIONSHIP));
    }

    //    @ParameterizedTest
    //    @MethodSource("classifyAProvider")
    //    void testClassifyA(Node node, Graph<Node, Edge> graph, NodeType expected) {
    //        assertEquals(expected, NodeClassifier.classifyA(node, graph));
    //    }
}
