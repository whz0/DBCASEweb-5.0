package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Diagram;

public interface DiagramStrategy {

    public String getType();

    public Diagram generate(Object diagram) throws Exception;
}
