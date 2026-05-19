package com.tfg.ucm.dbcase.dto;

import java.util.Map;

public record DatabaseUrl(String databaseType, String host, Integer port, String databaseName) {

    private static final Map<DatabaseType, Integer> DEFAULT_PORTS =
            Map.of(
                    DatabaseType.MYSQL, 3306,
                    DatabaseType.POSTGRESQL, 5432,
                    DatabaseType.ORACLE, 1521);

    public String buildUrl() {

        try {
            DatabaseType type = DatabaseType.valueOf(databaseType.toUpperCase());
            if (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.0")) {
                throw new IllegalArgumentException("No se permite localhost");
            }
            int finalPort = port == null ? DEFAULT_PORTS.get(type) : port;
            return switch (type) {
                case MYSQL -> "jdbc:mysql://%s:%d/%s".formatted(host, finalPort, databaseName);
                case POSTGRESQL ->
                        "jdbc:postgresql://%s:%d/%s".formatted(host, finalPort, databaseName);
                case ORACLE ->
                        "jdbc:oracle:thin:@//%s:%d/%s".formatted(host, finalPort, databaseName);
            };
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error, base de datos no soportado");
        }
    }
}
