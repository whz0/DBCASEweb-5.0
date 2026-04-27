package com.tfg.ucm.dbcase.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfg.ucm.dbcase.config.JWTFilter;
import com.tfg.ucm.dbcase.config.RateLimitingFilter;
import com.tfg.ucm.dbcase.config.SecurityConfig;
import com.tfg.ucm.dbcase.dto.TransformRequest;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import com.tfg.ucm.dbcase.service.DiagramTransformationService;
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
        value = DiagramController.class,
        excludeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = {SecurityConfig.class, JWTFilter.class, RateLimitingFilter.class})
        })
@AutoConfigureMockMvc(addFilters = false)
class DiagramControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private DiagramTransformationService diagramTransformationService;

    private static Stream<Arguments> generateSuccessProvider() {
        return Stream.of(
                Arguments.of(new LogicalInput("A (__B__)", "", ""), "result-1"),
                Arguments.of(new LogicalInput("X (__Y__, Z)", "", ""), "result-2"));
    }

    @ParameterizedTest(name = "generate success: {1}")
    @MethodSource("generateSuccessProvider")
    @WithMockUser
    void testGenerate_Success(LogicalInput input, String expectedResult) throws Exception {
        TransformRequest request = new TransformRequest(DiagramType.LOGICAL, input, DiagramType.DB);
        when(diagramTransformationService.transformDiagram(any())).thenReturn(expectedResult);

        mockMvc.perform(
                        post("/api/diagram/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(expectedResult));
    }

    private static Stream<Arguments> generateFailureProvider() {
        return Stream.of(
                Arguments.of(new LogicalInput("invalid", "", ""), new Exception("parse error")),
                Arguments.of(new LogicalInput("", "", ""), new RuntimeException("empty input")));
    }

    @ParameterizedTest(name = "generate failure: {1}")
    @MethodSource("generateFailureProvider")
    @WithMockUser
    void testGenerate_Failure(LogicalInput input, Exception exception) throws Exception {
        TransformRequest request = new TransformRequest(DiagramType.LOGICAL, input, DiagramType.DB);
        when(diagramTransformationService.transformDiagram(any())).thenThrow(exception);

        mockMvc.perform(
                        post("/api/diagram/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
