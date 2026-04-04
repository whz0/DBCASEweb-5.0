package com.tfg.ucm.dbcase.dto;

public record LogicalInput(String relationship, String restriction, String lossRestriction)
        implements DiagramInput {}
