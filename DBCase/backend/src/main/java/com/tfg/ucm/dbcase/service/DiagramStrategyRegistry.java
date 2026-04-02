package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.strategies.DiagramStrategy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DiagramStrategyRegistry {

    private final Map<String, DiagramStrategy> strategies;

    public DiagramStrategyRegistry(List<DiagramStrategy> list) {
        this.strategies =
                list.stream()
                        .collect(Collectors.toMap(DiagramStrategy::getType, Function.identity()));
    }

    public DiagramStrategy getStrategy(String type) {
        return Optional.ofNullable(strategies.get(type))
                .orElseThrow(
                        () -> new IllegalArgumentException("Unsupported diagram type: " + type));
    }
}
