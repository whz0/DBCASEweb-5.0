package com.tfg.ucm.dbcase.dto;

public record ErRelationshipParticipantDTO(
        String entityId, String cardinalityMin, String cardinalityMax, String role) {}
