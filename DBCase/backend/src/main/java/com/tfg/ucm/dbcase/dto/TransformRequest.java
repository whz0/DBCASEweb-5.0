package com.tfg.ucm.dbcase.dto;

import com.tfg.ucm.dbcase.dto.input.DiagramInput;
import com.tfg.ucm.dbcase.dto.input.DiagramType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransformRequest {

    private DiagramType type;
    private DiagramInput diagram;
    private DiagramType transformTo;
}
