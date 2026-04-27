package com.tfg.ucm.dbcase.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.ucm.dbcase.config.JWTFilter;
import com.tfg.ucm.dbcase.config.RateLimitingFilter;
import com.tfg.ucm.dbcase.config.SecurityConfig;
import com.tfg.ucm.dbcase.dto.ExecuteSqlRequest;
import com.tfg.ucm.dbcase.dto.TestDatabaseRequest;
import com.tfg.ucm.dbcase.service.DatabaseExecutionService;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = DatabaseController.class,
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {SecurityConfig.class, JWTFilter.class, RateLimitingFilter.class})
        })
@AutoConfigureMockMvc(addFilters = false)
class DatabaseControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private DatabaseExecutionService databaseExecutionService;

    private static Stream<Arguments> testConnectionProvider() {
        return Stream.of(
                Arguments.of(true, 200, "Se ha realizado la conexión con la base de datos"),
                Arguments.of(false, 400, "Ha fallado la conexión"));
    }

    @ParameterizedTest(name = "test connection: success={0}")
    @MethodSource("testConnectionProvider")
    @WithMockUser
    void testTestEndpoint(boolean serviceResult, int expectedStatus, String expectedBody)
            throws Exception {
        TestDatabaseRequest request = new TestDatabaseRequest("jdbc:h2:mem:test", "sa", "");
        when(databaseExecutionService.test(any())).thenReturn(serviceResult);

        mockMvc.perform(
                        post("/api/database/test")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().string(expectedBody));
    }

    private static Stream<Arguments> executeProvider() {
        return Stream.of(
                Arguments.of(false, "SQL ejecutado con éxito", 200),
                Arguments.of(true, "execution failed", 400));
    }

    @ParameterizedTest(name = "execute: throws={0}")
    @MethodSource("executeProvider")
    @WithMockUser
    void testExecuteEndpoint(boolean throws_, String expectedBody, int expectedStatus)
            throws Exception {
        ExecuteSqlRequest request =
                new ExecuteSqlRequest(
                        "postgresql", "jdbc:h2:mem:test", "sa", "", "CREATE TABLE T(id INT)");

        if (throws_) {
            doThrow(new Exception(expectedBody)).when(databaseExecutionService).execute(any());
        }

        mockMvc.perform(
                        post("/api/database/execute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().string(expectedBody));
    }
}
