package com.tfg.ucm.dbcase.dto.input;

public record LogicalInput(String relationship, String restriction, String lossRestriction)
        implements DiagramInput {}
