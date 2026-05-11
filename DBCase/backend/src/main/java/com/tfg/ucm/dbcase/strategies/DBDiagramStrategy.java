package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.addEdge;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.addForeignAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.addPrimaryAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateAttr;
import static com.tfg.ucm.dbcase.strategies.Auxiliary.getOrCreateNode;

import com.tfg.ucm.dbcase.dto.DataType;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.HashSet;
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
import org.jgrapht.graph.DirectedMultigraph;
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
        Graph<Node, Edge> result = new DirectedMultigraph<>(Edge.class);

        for (String statement : diagram.sql().split(";")) {
            if (!statement.isBlank()) {
                parseStatement(statement.trim(), result);
            }
        }

        Set<Node> uniquesNotNull =
                result.vertexSet().stream()
                        .filter(a -> a.isAttribute() && a.isFk() && a.isUnique() && a.isNotNull())
                        .collect(Collectors.toSet());

        for (Node unique : uniquesNotNull) {
            Node node = Graphs.predecessorListOf(result, unique).getFirst();
            Node pk =
                    Graphs.successorListOf(result, node).stream()
                            .filter(a -> a.isAttribute() && a.isPk() && !a.isFk())
                            .findFirst()
                            .orElse(null);
            Node refNode = getOrCreateNode(unique.getReference(), result);
            if (pk != null) {
                Node attr = getOrCreateAttr(pk.getName(), refNode, result);
                attr.setUnique(true);
                attr.setNotNull(true);
                addForeignAttr(attr, refNode, node.getName(), result);
                addEdge(attr, pk, result);
            }
        }

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();
        List<Node> allNodes = graph.vertexSet().stream().filter(n -> !n.isAttribute()).toList();

        StringBuilder sql = new StringBuilder();
        Set<String> usedRefs = new HashSet<>();
        for (Node node : allNodes) {
            sql.append(buildTable(node, graph, usedRefs));
        }
        return sql;
    }

    private String buildTable(Node entity, Graph<Node, Edge> graph, Set<String> usedRefs) {
        StringBuilder columns = new StringBuilder();
        StringBuilder constraints = new StringBuilder();

        Graphs.successorListOf(graph, entity)
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
                                if (attr.isUnique() && usedRefs.contains(attr.getReference())) {
                                    return;
                                }
                                if (attr.isUnique()) {
                                    usedRefs.add(entity.getName());
                                }
                                String fkName = attr.getName();
                                String unique = attr.isUnique() ? " UNIQUE" : "";
                                String notNull = attr.isNotNull() ? " NOT NULL" : "";
                                String isPk = attr.isPk() ? " PRIMARY KEY" : "";

                                columns.append("\t")
                                        .append(fkName)
                                        .append(" INTEGER")
                                        .append(isPk)
                                        .append(unique)
                                        .append(notNull)
                                        .append(",\n");

                                List<Node> successors = Graphs.successorListOf(graph, attr);
                                if (successors.isEmpty()) {
                                    return;
                                }
                                Node attrRef = successors.getFirst();
                                constraints
                                        .append("\tFOREIGN KEY (")
                                        .append(fkName)
                                        .append(") REFERENCES ")
                                        .append(attr.getReference())
                                        .append("(")
                                        .append(attrRef.getName())
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
                String nodeName = createTable.getTable().getName();
                Node entity = getOrCreateNode(nodeName, diagram);
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

    private void parseIndex(List<Index> indexes, Node node, Graph<Node, Edge> diagram) {
        for (Index index : indexes) {
            boolean isPk = index.getType().equalsIgnoreCase("primary key");
            boolean isFk =
                    index instanceof ForeignKeyIndex
                            || index.getType().equalsIgnoreCase("foreign key");
            index.getColumns()
                    .forEach(
                            column -> {
                                String name = column.getColumnName();
                                Node attr = getOrCreateAttr(name, node, diagram);
                                if (isFk && index instanceof ForeignKeyIndex fki) {
                                    String tableName = fki.getTable().getName();

                                    List<String> ref = fki.getReferencedColumnNames();
                                    List<String> src = fki.getColumnsNames();

                                    int i = src.indexOf(name);

                                    if (i != -1) {
                                        String refAttrName = ref.get(i);
                                        Node refTable = getOrCreateNode(tableName, diagram);
                                        Node refAttr =
                                                getOrCreateAttr(refAttrName, refTable, diagram);
                                        addEdge(refTable, refAttr, diagram);
                                        addEdge(attr, refAttr, diagram);
                                    }

                                    addForeignAttr(attr, node, tableName, diagram);
                                } else if (isPk) {
                                    addPrimaryAttr(attr, node, diagram);
                                }
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

            List<String> typeParams = col.getColDataType().getArgumentsStringList();
            DataType dataType =
                    (domain != null)
                            ? (typeParams != null && !typeParams.isEmpty()
                                    ? DataType.of(
                                            domain, Integer.parseInt(typeParams.getFirst().trim()))
                                    : DataType.of(domain))
                            : null;

            Node attr = getOrCreateAttr(name, entity, diagram);
            attr.setDataType(dataType);
            attr.setNotNull(isNotNull);
            attr.setUnique(isUnique);
            if (isPk) {
                addPrimaryAttr(attr, entity, diagram);
            } else {
                addEdge(entity, attr, diagram);
            }
        }
    }
}
