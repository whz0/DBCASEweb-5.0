package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.DiagramInput;
import com.tfg.ucm.dbcase.dto.DiagramType;
import com.tfg.ucm.dbcase.dto.ErInput;
import com.tfg.ucm.dbcase.dto.PhysicalInput;
import com.tfg.ucm.dbcase.strategies.DiagramStrategy;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiagramStrategyRegistryTest {

    private DiagramStrategyRegistry registry;

    @Mock private DiagramStrategy<ErInput> erStrategy;

    @Mock private DiagramStrategy<PhysicalInput> sqlStrategy;

    @BeforeEach
    void setUp() {
        when(erStrategy.getType()).thenReturn(DiagramType.ER);
        doReturn(ErInput.class).when(erStrategy).getInputType();

        when(sqlStrategy.getType()).thenReturn(DiagramType.DB);
        doReturn(PhysicalInput.class).when(sqlStrategy).getInputType();

        registry = new DiagramStrategyRegistry(List.of(erStrategy, sqlStrategy));
    }

    @Test
    void shouldGetStrategyByTypeManual() {
        assertEquals(erStrategy, registry.getStrategy(DiagramType.ER));
        assertEquals(sqlStrategy, registry.getStrategy(DiagramType.DB));
    }

    @Test
    void shouldThrowExceptionWhenTypeNotFound() {
        assertThrows(
                IllegalArgumentException.class, () -> registry.getStrategy(DiagramType.LOGICAL));
    }

    @Test
    void shouldGenerateSuccessfullyWithCorrectInput() throws Exception {
        ErInput input = mock(ErInput.class);
        Diagram expectedDiagram = Diagram.builder().build();
        when(erStrategy.generate(input)).thenReturn(expectedDiagram);

        Diagram result = registry.generate(DiagramType.ER, input);

        assertEquals(expectedDiagram, result);
        verify(erStrategy).generate(input);
    }

    @ParameterizedTest
    @MethodSource("provideMismatchedInputs")
    void shouldThrowExceptionWhenInputTypeMismatch(DiagramType type, DiagramInput wrongInput) {
        assertThrows(IllegalArgumentException.class, () -> registry.generate(type, wrongInput));
    }

    private static Stream<Arguments> provideMismatchedInputs() {
        return Stream.of(
                Arguments.of(DiagramType.ER, mock(PhysicalInput.class)),
                Arguments.of(DiagramType.DB, mock(ErInput.class)));
    }

    @Test
    void shouldTransformSuccessfully() {
        Diagram diagram = Diagram.builder().build();
        Object expectedOutput = "SQL Output";
        when(sqlStrategy.transform(diagram)).thenReturn(expectedOutput);

        Object result = registry.transform(DiagramType.DB, diagram);

        assertEquals(expectedOutput, result);
        verify(sqlStrategy).transform(diagram);
    }
}
