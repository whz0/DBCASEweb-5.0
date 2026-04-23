package com.tfg.ucm.dbcase.strategies;

import static com.tfg.ucm.dbcase.strategies.Auxiliary.*;
import static com.tfg.ucm.dbcase.strategies.NodeClassifier.isAttribute;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.List;
import java.util.Objects;
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

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {
        Graph<Node, Edge> graph = diagram.getDiagram();
        List<Node> allNodes = graph.vertexSet().stream().filter(n -> !isAttribute(n)).toList();

        StringBuilder sql = new StringBuilder();
        for (Node node : allNodes) {
            sql.append(buildTable(node, graph));
        }
        return sql;
    }

    private String buildTable(
            Node entity, Graph<Node, Edge> graph) {
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
                                String fkName = attr.getName();
                                String notNull = attr.isNotNull() ? " NOT NULL" : "";
                                String isPk = attr.isPk() ? " PRIMARY KEY" : "";

                                columns.append("\t")
                                        .append(fkName)
                                        .append(" INTEGER")
                                        .append(isPk)
                                        .append(notNull)
                                        .append(",\n");

                                Node attrRef = Graphs.successorListOf(graph, attr).get(0);
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
            index.getColumns()
                    .forEach(
                            column -> {
                                String name = column.getColumnName();
                                Node attr = getOrCreateAttr(name, entity, diagram);
                                if(isFk && index instanceof ForeignKeyIndex fki) {
                                    String tableName = fki.getTable().getName();
                                    Node node = getOrCreateNode(tableName, diagram);
                                    List<String> ref = fki.getReferencedColumnNames();
                                    List<String> src = fki.getColumnsNames();

                                    int i = src.indexOf(name);

                                    if(i != -1) {
                                        String refAttrName = ref.get(i);
                                        Node refTable = getOrCreateNode(tableName, diagram);
                                        Node refAttr = getOrCreateAttr(refAttrName, refTable, diagram);
                                        addEdge(refTable, refAttr, diagram);
                                        addEdge(attr, refAttr, diagram);
                                    }

                                    addForeignAttr(attr, node, tableName, diagram);
                                }
                                else if(isPk) {
                                    addPrimaryAttr(attr, entity, diagram);
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

            Node attr = getOrCreateAttr(name, entity, diagram);
            attr.setDataType(domain);
            attr.setNotNull(isNotNull);
            attr.setUnique(isUnique);
            if (isPk) {
                addPrimaryAttr(attr, entity, diagram);
            }
            else {
                addEdge(entity, attr, diagram);
            }
        }
    }
}
