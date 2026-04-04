package com.tfg.ucm.dbcase.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ErInput.class, name = "er"),
    @JsonSubTypes.Type(value = LogicalInput.class, name = "logical"),
    @JsonSubTypes.Type(value = PhysicalInput.class, name = "db")
})
public interface DiagramInput {}
