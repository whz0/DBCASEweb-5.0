package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tfg.ucm.dbcase.dto.ExecuteSqlRequest;
import com.tfg.ucm.dbcase.dto.TestDatabaseRequest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

class DatabaseExecutionServiceTest {

    private final DatabaseExecutionService service = new DatabaseExecutionService();

    // --- test() ---

    private static Stream<Arguments> testConnectionProvider() {
        return Stream.of(Arguments.of(true, true), Arguments.of(false, false));
    }

    @ParameterizedTest(name = "connection.isValid={0} → test()={1}")
    @MethodSource("testConnectionProvider")
    void testTest_ReturnsConnectionValidity(boolean isValid, boolean expected) throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.isValid(10)).thenReturn(isValid);

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            boolean result = service.test(new TestDatabaseRequest("jdbc:h2:mem:test", "sa", ""));

            assertEquals(expected, result);
        }
    }

    @Test
    void testTest_SQLException_ReturnsFalse() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.isValid(anyInt())).thenThrow(new SQLException("connection refused"));

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            boolean result = service.test(new TestDatabaseRequest("jdbc:bad:url", "sa", ""));

            assertFalse(result);
        }
    }

    // --- execute() ---

    private static Stream<String> executeSqlProvider() {
        return Stream.of(
                "CREATE TABLE T(id INT)", "CREATE TABLE A(id INT); CREATE TABLE B(id INT)");
    }

    @ParameterizedTest(name = "execute sql: {0}")
    @MethodSource("executeSqlProvider")
    void testExecute_Success(String sql) throws Exception {
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        when(conn.createStatement()).thenReturn(stmt);

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            service.execute(new ExecuteSqlRequest("postgresql", "jdbc:h2:mem:test", "sa", "", sql));

            verify(conn).commit();
            verify(conn).setAutoCommit(false);
        }
    }

    @Test
    void testExecute_ThrowsOnSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        Statement stmt = mock(Statement.class);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.execute(anyString())).thenThrow(new SQLException("syntax error"));

        try (MockedStatic<DriverManager> dm = mockStatic(DriverManager.class)) {
            dm.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(conn);

            assertThrows(
                    Exception.class,
                    () ->
                            service.execute(
                                    new ExecuteSqlRequest(
                                            "postgresql",
                                            "jdbc:h2:mem:test",
                                            "sa",
                                            "",
                                            "INVALID SQL")));
        }
    }
}
