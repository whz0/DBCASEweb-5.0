package com.tfg.ucm.dbcase.strategies;

import com.tfg.ucm.dbcase.dto.Diagram;
import com.tfg.ucm.dbcase.dto.DiagramType;
import com.tfg.ucm.dbcase.dto.ErInput;
import org.springframework.stereotype.Service;

@Service
public class ERDiagramStrategy implements DiagramStrategy<ErInput> {

    @Override
    public DiagramType getType() {
        return DiagramType.ER;
    }

    @Override
    public Class<ErInput> getInputType() {
        return ErInput.class;
    }

    @Override
    public Diagram generate(ErInput diagram) {
        return null;
    }

    @Override
    public Object transform(Diagram diagram) {
        return null;
    }
}
