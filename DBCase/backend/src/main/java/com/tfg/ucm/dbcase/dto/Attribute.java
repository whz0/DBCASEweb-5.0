package com.tfg.ucm.dbcase.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@SuperBuilder(toBuilder = true)
public class Attribute extends Node {
    private boolean pk;
    private boolean compose;
    private boolean noEmpty;
    private boolean unique;
    private boolean multivalue;
    private int size;
    private Domain dataType;
}
