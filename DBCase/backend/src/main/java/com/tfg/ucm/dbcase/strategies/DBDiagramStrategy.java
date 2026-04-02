package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Attribute;
import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.Edge;
import com.tfg.ucm.dbcase.dto.Entity;
import com.tfg.ucm.dbcase.dto.Node;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.springframework.stereotype.Service;

@Service
public class DBDiagramStrategy implements DiagramStrategy {
    @Override
    public String getType() {
        return "db";
    }

    @Override
    public Diagram generate(Object diagram) throws Exception {

        Map<String, Node> nodes = new HashMap<>();
        Set<Edge> edges = new HashSet<>();

        String[] statements = ((String) diagram).split(";");
        Iterator<String> i = Arrays.stream(statements).iterator();
        while (i.hasNext()) {
            String statement = i.next();
            parseStatement(statement, nodes, edges);
        }

        return Diagram.builder()
                .nodes(nodes.values().stream().toList())
                .edges(edges.stream().toList())
                .build();
    }

    @Override
    public Object transform(Diagram diagram) {
        return null;
    }

    private void parseStatement(String sqlStr, Map<String, Node> nodes, Set<Edge> edges)
            throws Exception {
        try {
            Statement statement = CCJSqlParserUtil.parse(sqlStr);
            if (statement instanceof CreateTable createTable) {
                String entity = createTable.getTable().getName();
                nodes.put(entity, Entity.builder().name(entity).build());
                parseColumns(createTable.getColumnDefinitions(), entity, nodes, edges);
                parseIndex(createTable.getIndexes(), entity, nodes, edges);
            }
        } catch (JSQLParserException e) {
            throw new Exception("Error en el formato");
        }
    }

    private void parseIndex(
            List<Index> index, String entity, Map<String, Node> nodes, Set<Edge> edges) {
        for (Index i : index) {
            boolean isPk = i.getType().toLowerCase().contentEquals("primary key");
            boolean isFk = i.getType().toLowerCase().contentEquals("foreign key");
            i.getColumns()
                    .forEach(
                            (column) -> {
                                String col = column.getColumnName();
                                Node node = nodes.get(col);
                                if (node instanceof Attribute attribute) {
                                    nodes.put(col, attribute.toBuilder().pk(isPk).fk(isFk).build());
                                }
                                edges.add(Edge.builder().source(entity).target(col).build());
                            });
        }
    }

    private void parseColumns(
            List<ColumnDefinition> columns,
            String entity,
            Map<String, Node> nodes,
            Set<Edge> edges) {
        for (ColumnDefinition col : columns) {
            String name = col.getColumnName();
            String type = col.getColDataType().getDataType();
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
            nodes.put(
                    name,
                    Attribute.builder()
                            .name(name)
                            .color(type)
                            .pk(isPk)
                            .unique(isUnique)
                            .noEmpty(isNotNull)
                            .build());
            edges.add(Edge.builder().source(entity).target(name).build());
        }
    }
}
