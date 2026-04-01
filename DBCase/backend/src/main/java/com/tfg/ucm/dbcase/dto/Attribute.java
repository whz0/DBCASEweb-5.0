package com.tfg.ucm.dbcase.dto;

import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class Attribute extends Node {

    private boolean pk;
    private boolean fk;
    private boolean compose;
    private boolean noEmpty;
    private boolean unique;
    private boolean multivalue;
    private int size;
}
