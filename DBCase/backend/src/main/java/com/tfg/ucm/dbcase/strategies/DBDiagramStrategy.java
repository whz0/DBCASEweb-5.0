package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isAttribute;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.Multigraph;
import org.springframework.stereotype.Service;

@Service
public class DBDiagramStrategy implements DiagramStrategy<PhysicalInput> {
    @Override
    public DiagramType getType() {
        return DiagramType.DB;
    }

    @Override
    public Class<PhysicalInput> getInputType() {
        return PhysicalInput.class;
    }

    @Override
    public Diagram generate(PhysicalInput diagram) throws Exception {
        Graph<Node, Edge> result = new Multigraph<>(Edge.class);

        for (String statement : diagram.sql().split(";")) {
            if (!statement.isBlank()) {
                parseStatement(statement.trim(), result);
            }
        }

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        StringBuilder sqlBuilder = new StringBuilder();
        Graph<Node, Edge> graph = diagram.getDiagram();

        java.util.Map<Node, java.util.List<FkInjection>> injections = new java.util.HashMap<>();
        List<Node> allNodes = graph.vertexSet().stream().filter(n -> !isAttribute(n)).toList();

        for (Node node : allNodes) {
            if (!NodeClassifier.isRelationship(node, graph)) {
                continue;
            }
            NodeClassifier.RelationshipKind kind = NodeClassifier.classify(node, graph);
            List<Edge> fkEdges = NodeClassifier.getFkEdges(node, graph);
            if (fkEdges.size() != 2 || kind == NodeClassifier.RelationshipKind.NM) {
                continue;
            }

            Edge edgeA = fkEdges.get(0);
            Edge edgeB = fkEdges.get(1);
            Node attrA = Graphs.getOppositeVertex(graph, edgeA, node);
            Node attrB = Graphs.getOppositeVertex(graph, edgeB, node);
            Node entityA =
                    allNodes.stream()
                            .filter(n -> n.getName().equals(attrA.getReference()))
                            .findFirst()
                            .orElse(null);
            Node entityB =
                    allNodes.stream()
                            .filter(n -> n.getName().equals(attrB.getReference()))
                            .findFirst()
                            .orElse(null);
            if (entityA == null || entityB == null) {
                continue;
            }

            boolean totalA = "1".equals(edgeA.getCardinalityMin());
            boolean totalB = "1".equals(edgeB.getCardinalityMin());

            if (kind == NodeClassifier.RelationshipKind.ONE_TO_ONE) {
                injections
                        .computeIfAbsent(entityA, k -> new java.util.ArrayList<>())
                        .add(
                                new FkInjection(
                                        NodeClassifier.getFkAttrName(edgeB),
                                        entityB.getName(),
                                        totalA || totalB));
            } else {
                Node nSideEntity =
                        (kind == NodeClassifier.RelationshipKind.ONE_TO_N) ? entityB : entityA;
                Node oneSideEntity =
                        (kind == NodeClassifier.RelationshipKind.ONE_TO_N) ? entityA : entityB;
                boolean totalNSide =
                        (kind == NodeClassifier.RelationshipKind.ONE_TO_N) ? totalB : totalA;
                injections
                        .computeIfAbsent(nSideEntity, k -> new java.util.ArrayList<>())
                        .add(
                                new FkInjection(
                                        NodeClassifier.getFkAttrName(
                                                kind == NodeClassifier.RelationshipKind.ONE_TO_N
                                                        ? edgeA
                                                        : edgeB),
                                        oneSideEntity.getName(),
                                        totalNSide));
            }
        }

        for (Node node : allNodes) {
            if (NodeClassifier.isRelationship(node, graph)
                    && NodeClassifier.classify(node, graph) != NodeClassifier.RelationshipKind.NM) {
                continue;
            }
            sqlBuilder.append(buildTable(node, graph, injections.getOrDefault(node, List.of())));
        }
        return sqlBuilder;
    }

    private record FkInjection(String attrName, String referencedTable, boolean isTotal) {}

    private String buildTable(Node entity, Graph<Node, Edge> graph, List<FkInjection> injectedFks) {
        StringBuilder columns = new StringBuilder();
        StringBuilder constraints = new StringBuilder();

        Graphs.neighborListOf(graph, entity)
                .forEach(
                        attr -> {
                            if (!attr.isAttribute()) {
                                return;
                            }
                            String dataType =
                                    attr.getDataType() != null
                                            ? attr.getDataType().toString()
                                            : "?";

                            if (attr.isFk()) {
                                String fkName =
                                        graph.getAllEdges(entity, attr).stream()
                                                .map(NodeClassifier::getFkAttrName)
                                                .filter(n -> n != null)
                                                .findFirst()
                                                .orElse(attr.getName());
                                String notNull = attr.isNotNull() ? " NOT NULL" : "";
                                columns.append("	")
                                        .append(fkName)
                                        .append(" INTEGER")
                                        .append(notNull)
                                        .append(",\n");
                                constraints
                                        .append("	FOREIGN KEY (")
                                        .append(fkName)
                                        .append(") REFERENCES ")
                                        .append(attr.getReference())
                                        .append("(")
                                        .append(fkName)
                                        .append("),\n");
                            } else if (attr.isPk()) {
                                if (dataType.equals("?")) {
                                    dataType = "INTEGER";
                                }
                                columns.append("\t")
                                        .append(attr.getName())
                                        .append(" ")
                                        .append(dataType)
                                        .append(" PRIMARY KEY")
                                        .append(attr.isUnique() ? " UNIQUE" : "")
                                        .append(attr.isNotNull() ? " NOT NULL" : "")
                                        .append(",\n");
                            } else {
                                columns.append("\t")
                                        .append(attr.getName())
                                        .append(" ")
                                        .append(dataType)
                                        .append(attr.isUnique() ? " UNIQUE" : "")
                                        .append(attr.isNotNull() ? " NOT NULL" : "")
                                        .append(",\n");
                            }
                        });

        for (FkInjection fk : injectedFks) {
            String notNull = fk.isTotal() ? " NOT NULL" : "";
            columns.append("\t")
                    .append(fk.attrName())
                    .append(" INTEGER")
                    .append(notNull)
                    .append(",\n");
            constraints
                    .append("\tFOREIGN KEY (")
                    .append(fk.attrName())
                    .append(") REFERENCES ")
                    .append(fk.referencedTable())
                    .append("(")
                    .append(fk.attrName())
                    .append("),\n");
        }

        if (columns.isEmpty()) {
            return "";
        }
        String body = columns.toString() + constraints.toString();
        if (body.endsWith(",\n")) {
            body = body.substring(0, body.length() - 2) + "\n";
        }
        return "CREATE TABLE " + entity.getName() + "(\n" + body + ");\n\n";
    }

    private void parseStatement(String sqlStr, Graph<Node, Edge> diagram) throws Exception {
        try {
            Statement statement = CCJSqlParserUtil.parse(sqlStr);
            if (statement instanceof CreateTable createTable) {
                String entityName = createTable.getTable().getName();
                Node entity = Node.builder().name(entityName).build();
                diagram.addVertex(entity);
                if (createTable.getIndexes() != null) {
                    parseIndex(createTable.getIndexes(), entity, diagram);
                }
                parseColumns(createTable.getColumnDefinitions(), entity, diagram);
            }
        } catch (JSQLParserException e) {
            throw new Exception("Error en el formato");
        }
    }

    private void parseIndex(List<Index> indexes, Node entity, Graph<Node, Edge> diagram) {
        for (Index index : indexes) {
            boolean isPk = index.getType().equalsIgnoreCase("primary key");
            boolean isFk =
                    index instanceof ForeignKeyIndex
                            || index.getType().equalsIgnoreCase("foreign key");
            String referencedTable;
            if (isFk && index instanceof ForeignKeyIndex fki && fki.getTable() != null) {
                referencedTable = fki.getTable().getName();
            } else if (isPk) {
                referencedTable = entity.getName();
            } else {
                referencedTable = null;
            }
            index.getColumns()
                    .forEach(
                            column -> {
                                String name = column.getColumnName();
                                Node attr = getOrCreateAttr(name, entity, diagram);
                                attr.setAttribute(true);
                                attr.setPk(isPk);
                                attr.setFk(isFk);
                                attr.setReference(referencedTable);

                                diagram.addEdge(
                                        entity,
                                        attr,
                                        Edge.builder()
                                                .label("attr" + entity.getName() + name)
                                                .build());
                            });
        }
    }

    private void parseColumns(
            List<ColumnDefinition> columns, Node entity, Graph<Node, Edge> diagram) {
        for (ColumnDefinition col : columns) {
            String name = col.getColumnName();
            String type = col.getColDataType().getDataType().toUpperCase();
            List<String> specs = col.getColumnSpecs();
            boolean isPk = false, isUnique = false, isNotNull = false;
            if (specs != null) {
                Set<String> normalizedSpecs =
                        specs.stream().map(String::toLowerCase).collect(Collectors.toSet());
                isPk = Stream.of("primary", "key").allMatch(normalizedSpecs::contains);
                isUnique = specs.stream().anyMatch(s -> s.equalsIgnoreCase("unique"));
                isNotNull = Stream.of("not", "null").allMatch(normalizedSpecs::contains);
            }
            Domain domain;
            try {
                domain = Domain.valueOf(type);
            } catch (IllegalArgumentException e) {
                domain = null;
            }

            Node attr = getOrCreateAttr(name, entity, diagram);
            attr.setAttribute(true);
            attr.setDataType(domain);
            if (isPk) {
                attr.setPk(true);
            }
            attr.setNotNull(isNotNull);
            attr.setUnique(isUnique);

            if (diagram.getAllEdges(entity, attr).isEmpty()) {
                diagram.addEdge(
                        entity,
                        attr,
                        Edge.builder().label("attr" + entity.getName() + name).build());
            }
        }
    }
}
