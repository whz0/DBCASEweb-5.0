package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.ExecuteSqlRequest;
import com.tfg.ucm.dbcase.dto.TestDatabaseRequest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.stereotype.Service;

@Service
public class DatabaseExecutionService {

    public boolean test(TestDatabaseRequest request) {
        try {
            Connection connection =
                    DriverManager.getConnection(
                            request.getDatabaseUrl().buildUrl(),
                            request.getUsername(),
                            request.getPassword());
            return connection.isValid(10);
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return false;
        }
    }

    public void execute(ExecuteSqlRequest req) throws SQLException {
        try (Connection conn =
                        DriverManager.getConnection(
                                req.getDatabaseUrl().buildUrl(),
                                req.getUsername(),
                                req.getPassword());
                Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            for (String statement : req.getSql().split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
            conn.commit();
        }
    }
}
