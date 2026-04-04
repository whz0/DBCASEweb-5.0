package com.tfg.ucm.dbcase.dto;

import java.util.List;

public record ErEntityDTO(
        String id,
        String name,
        Position position,
        boolean isWeak,
        List<String> attributes,
        List<String> primaryKeys) {}
