package com.tfg.ucm.dbcase.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiagramType {
    ER("er"),
    LOGICAL("logical"),
    DB("db");

    @JsonValue private final String value;
}
