package com.tfg.ucm.dbcase.dto.erdiagram;

import java.util.List;

public record ErAttributeDTO(
        String id,
        String name,
        Position position,
        String parentId,
        boolean isKey,
        boolean isComposite,
        boolean isMultivalued,
        boolean isNotNull,
        boolean isUnique,
        String domain,
        int size,
        List<String> components) {}
