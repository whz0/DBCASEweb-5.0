package com.tfg.ucm.dbcase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.TransformRequest;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import com.tfg.ucm.dbcase.dto.input.LogicalInput;
import com.tfg.ucm.dbcase.dto.input.PhysicalInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiagramTransformationServiceTest {

    @Mock private DiagramStrategyRegistry registry;

    @InjectMocks private DiagramTransformationService service;

    @Test
    void testTransformLogicalIntoPhysical() throws Exception {
        LogicalInput input = new LogicalInput("A (__B__, C)", "", "");
        Diagram diagram = Diagram.builder().build();
        String expectedSql =
                """
                CREATE TABLE A(
                    B INTEGER PRIMARY KEY,
                    C ?
                );
                """;

        when(registry.generate(DiagramType.LOGICAL, input)).thenReturn(diagram);
        when(registry.transform(DiagramType.DB, diagram)).thenReturn(expectedSql);

        TransformRequest request = new TransformRequest(DiagramType.LOGICAL, input, DiagramType.DB);
        Object result = service.transformDiagram(request);

        assertEquals(expectedSql, result);
        verify(registry).generate(DiagramType.LOGICAL, input);
        verify(registry).transform(DiagramType.DB, diagram);
    }

    @Test
    void testTransformPhysicalIntoLogical() throws Exception {
        PhysicalInput input =
                new PhysicalInput(
                        """
                CREATE TABLE A(
                    B INTEGER PRIMARY KEY
                )
                """);
        Diagram diagram = Diagram.builder().build();
        Object expectedLogical = "A (__B__)";

        when(registry.generate(DiagramType.DB, input)).thenReturn(diagram);
        when(registry.transform(DiagramType.LOGICAL, diagram)).thenReturn(expectedLogical);

        TransformRequest request = new TransformRequest(DiagramType.DB, input, DiagramType.LOGICAL);
        Object result = service.transformDiagram(request);

        assertEquals(expectedLogical, result);
    }

    @Test
    void testTransformThrows() throws Exception {
        LogicalInput input = new LogicalInput("invalid", "", "");
        when(registry.generate(any(), any())).thenThrow(new Exception("parse error"));

        TransformRequest request = new TransformRequest(DiagramType.LOGICAL, input, DiagramType.DB);

        assertThrows(Exception.class, () -> service.transformDiagram(request));
    }
}
