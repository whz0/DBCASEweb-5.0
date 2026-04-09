package com.tfg.ucm.dbcase.dto.input;

public record ErRelationshipParticipantDTO(
        String entityId, String cardinalityMin, String cardinalityMax, String role) {}
