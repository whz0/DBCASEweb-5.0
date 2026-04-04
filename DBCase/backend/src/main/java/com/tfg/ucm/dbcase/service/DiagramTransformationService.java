package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.TransformRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiagramTransformationService {

    private final DiagramStrategyRegistry registry;

    public Object transformDiagram(TransformRequest request) throws Exception {
        final Diagram internalDiagram = registry.generate(request.getType(), request.getDiagram());
        return registry.transform(request.getTransformTo(), internalDiagram);
    }
}
