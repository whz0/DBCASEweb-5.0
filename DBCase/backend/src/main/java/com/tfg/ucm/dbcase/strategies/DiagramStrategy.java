package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.input.DiagramInput;
import com.tfg.ucm.dbcase.dto.input.DiagramType;

public interface DiagramStrategy<I extends DiagramInput> {

    DiagramType getType();

    Class<I> getInputType();

    Diagram generate(I diagram) throws Exception;

    Object transform(Diagram diagram);
}
