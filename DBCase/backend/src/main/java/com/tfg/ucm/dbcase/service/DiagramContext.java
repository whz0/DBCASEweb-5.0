package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.strategies.DiagramStrategy;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DiagramContext {

    private final Map<String, DiagramStrategy> strategies;

    public DiagramContext(List<DiagramStrategy> list) {
        this.strategies =
                list.stream()
                        .collect(Collectors.toMap(DiagramStrategy::getType, Function.identity()));
    }

    public Diagram generate(String type, Object diagram) throws Exception {
        return strategies.get(type).generate(diagram);
    }
}
