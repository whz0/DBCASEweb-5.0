package com.tfg.ucm.dbcase.dto.input;

import java.util.List;

public record ErRelationshipDTO(
        String id,
        String name,
        Position position,
        String type,
        List<ErRelationshipParticipantDTO> participants,
        List<String> attributes) {}
