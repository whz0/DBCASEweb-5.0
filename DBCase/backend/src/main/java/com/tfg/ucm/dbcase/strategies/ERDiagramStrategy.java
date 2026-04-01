package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Diagram;
import org.springframework.stereotype.Service;

@Service
public class ERDiagramStrategy implements DiagramStrategy {

    @Override
    public String getType() {
        return "er";
    }

    @Override
    public Diagram generate(Object diagram) {
        return null;
    }
}
