package com.tfg.ucm.dbcase.dto;

import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
public class Entity extends Node {

    private boolean weak;
}
