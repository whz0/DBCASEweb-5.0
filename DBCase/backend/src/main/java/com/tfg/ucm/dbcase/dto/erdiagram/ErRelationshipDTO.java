package com.tfg.ucm.dbcase.dto.erdiagram;

import java.util.List;

public record ErRelationshipDTO(
        String id,
        String name,
        Position position,
        String type,
        String aggregationName,
        List<ErRelationshipParticipantDTO> participants,
        List<String> attributes) {}
