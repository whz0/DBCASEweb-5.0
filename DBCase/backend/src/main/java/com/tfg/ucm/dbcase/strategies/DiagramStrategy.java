package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Diagram;

public interface DiagramStrategy {

    String getType();

    Diagram generate(Object diagram) throws Exception;

    Object transform(Diagram diagram);
}
