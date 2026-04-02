package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.TransformRequest;
import com.tfg.ucm.dbcase.strategies.DiagramStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiagramTransformationService {

    private final DiagramStrategyRegistry registry;

    public Object transformDiagram(TransformRequest request) throws Exception {
        final DiagramStrategy sourceStrategy = registry.getStrategy(request.getType());
        final Diagram internalDiagram = sourceStrategy.generate(request.getDiagram());
        final DiagramStrategy targetStrategy = registry.getStrategy(request.getTransformTo());

        return targetStrategy.transform(internalDiagram);
    }
}
