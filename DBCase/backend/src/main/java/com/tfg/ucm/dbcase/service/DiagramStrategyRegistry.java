package com.tfg.ucm.dbcase.service;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.DiagramInput;
import com.tfg.ucm.dbcase.dto.DiagramType;
import com.tfg.ucm.dbcase.strategies.DiagramStrategy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DiagramStrategyRegistry {

    private final Map<DiagramType, DiagramStrategy<? extends DiagramInput>> strategies;

    public DiagramStrategyRegistry(List<DiagramStrategy<? extends DiagramInput>> list) {
        this.strategies =
                list.stream()
                        .collect(Collectors.toMap(DiagramStrategy::getType, Function.identity()));
    }

    public DiagramStrategy<?> getStrategy(DiagramType type) {
        return Optional.ofNullable(strategies.get(type))
                .orElseThrow(
                        () -> new IllegalArgumentException("Unsupported diagram type: " + type));
    }

    @SuppressWarnings("unchecked")
    public <I extends DiagramInput> Diagram generate(DiagramType type, I input) throws Exception {
        DiagramStrategy<I> strategy = (DiagramStrategy<I>) getStrategy(type);

        if (!strategy.getInputType().isInstance(input)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Strategy for %s expects %s but received %s",
                            type,
                            strategy.getInputType().getSimpleName(),
                            input.getClass().getSimpleName()));
        }

        return strategy.generate(input);
    }

    public Object transform(DiagramType type, Diagram diagram) {
        return getStrategy(type).transform(diagram);
    }
}
