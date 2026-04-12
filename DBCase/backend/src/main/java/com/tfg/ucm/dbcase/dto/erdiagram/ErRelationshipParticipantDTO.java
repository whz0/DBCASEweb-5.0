package com.tfg.ucm.dbcase.dto.erdiagram;

public record ErRelationshipParticipantDTO(
        String entityId, String cardinalityMin, String cardinalityMax, String role) {}
