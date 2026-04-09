package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Domain;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import com.tfg.ucm.dbcase.dto.Relationship;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;
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

        String[] statements = diagram.sql().split(";");
        Iterator<String> i = Arrays.stream(statements).iterator();
        while (i.hasNext()) {
            String statement = i.next();
            parseStatement(statement, result);
        }

        return Diagram.builder().diagram(result).build();
    }

    @Override
    public Object transform(Diagram diagram) {

        StringBuilder sqlBuilder = new StringBuilder();
        Graph<Node, Edge> graph = diagram.getDiagram();
        Set<Node> visited = new HashSet<>();

        List<Node> tableNodes =
                graph.vertexSet().stream()
                        .filter(n -> n instanceof Entity || n instanceof Relationship)
                        .toList();

        for (Node startNode : tableNodes) {
            if (visited.contains(startNode)) {
                continue;
            }

            StringBuilder currentTable = new StringBuilder();
            currentTable.append("CREATE TABLE ").append(startNode.getName()).append("(\n");
            visited.add(startNode);

            BreadthFirstIterator<Node, Edge> bfs = new BreadthFirstIterator<>(graph, startNode);
            bfs.next();

            while (bfs.hasNext()) {
                Node vertex = bfs.next();
                if (visited.contains(vertex)) {
                    continue;
                }
                visited.add(vertex);

                if (vertex instanceof Entity || vertex instanceof Relationship) {
                    break;
                }

                if (vertex instanceof Attribute attr) {
                    String dataType =
                            attr.getDataType() != null ? attr.getDataType().toString() : "?";
                    String notNull = attr.isNoEmpty() ? " NOT NULL" : "";
                    String unique = attr.isUnique() ? " UNIQUE" : "";

                    String referencedTable =
                            Graphs.successorListOf(graph, attr).stream()
                                    .filter(n -> n instanceof Entity || n instanceof Relationship)
                                    .map(Node::getName)
                                    .findFirst()
                                    .orElse(null);

                    String constraint =
                            referencedTable != null
                                    ? " REFERENCES " + referencedTable
                                    : attr.isPk() ? " PRIMARY KEY" : "";

                    currentTable
                            .append("  ")
                            .append(attr.getName())
                            .append(" ")
                            .append(dataType)
                            .append(constraint)
                            .append(notNull)
                            .append(unique)
                            .append(",\n");
                }
            }

            sqlBuilder.append(currentTable).append(");\n\n");
        }

        return sqlBuilder;
    }

    private void parseStatement(String sqlStr, Graph<Node, Edge> diagram) throws Exception {
        try {
            Statement statement = CCJSqlParserUtil.parse(sqlStr);
            if (statement instanceof CreateTable createTable) {
                String entityName = createTable.getTable().getName();
                Node entity = Entity.builder().name(entityName).build();
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
            boolean isFk = index.getType().equalsIgnoreCase("foreign key");
            index.getColumns()
                    .forEach(
                            column -> {
                                String name = column.getColumnName();
                                Optional<Node> exists =
                                        diagram.vertexSet().stream()
                                                .filter(n -> n.getName().equals(name))
                                                .findFirst();
                                Node attribute =
                                        exists.orElseGet(
                                                () -> {
                                                    Node a =
                                                            Attribute.builder()
                                                                    .name(name)
                                                                    .pk(isPk)
                                                                    .build();
                                                    diagram.addVertex(a);
                                                    return a;
                                                });
                                if (attribute instanceof Attribute a) {
                                    a.setPk(isPk);
                                }
                                Edge edge =
                                        Edge.builder()
                                                .label("attribute" + entity.getName() + name)
                                                .build();
                                if (isPk) {
                                    diagram.addEdge(entity, attribute, edge);
                                } else if (isFk) {
                                    diagram.addEdge(attribute, entity, edge);
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
            boolean isPk = false;
            boolean isUnique = false;
            boolean isNotNull = false;
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
                domain = Domain.VARCHAR;
            }
            Optional<Node> exists =
                    diagram.vertexSet().stream().filter(n -> n.getName().equals(name)).findFirst();
            if (exists.isPresent()) {
                if (exists.get() instanceof Attribute a) {
                    a.setDataType(domain);
                    a.setUnique(isUnique);
                    a.setNoEmpty(isNotNull);
                    // ensure entity -> attribute edge exists
                    if (diagram.getAllEdges(entity, exists.get()).isEmpty()) {
                        diagram.addEdge(
                                entity,
                                exists.get(),
                                Edge.builder().label("attr" + name + entity.getName()).build());
                    }
                }
            } else {
                Node attribute =
                        Attribute.builder()
                                .name(name)
                                .dataType(domain)
                                .pk(isPk)
                                .unique(isUnique)
                                .noEmpty(isNotNull)
                                .build();
                diagram.addVertex(attribute);
                // Always: entity -> attribute
                diagram.addEdge(
                        entity,
                        attribute,
                        Edge.builder().label("attr" + name + entity.getName()).build());
            }
        }
    }
}
