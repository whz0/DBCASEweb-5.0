package com.tfg.ucm.dbcase.dto.input;

import java.util.List;

public record ErDomainDTO(String id, String name, String baseType, List<String> values) {}
