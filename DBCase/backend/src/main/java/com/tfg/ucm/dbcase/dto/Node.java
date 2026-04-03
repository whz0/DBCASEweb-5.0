package com.tfg.ucm.dbcase.dto;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class Node {

    public Node() {}
    ;

    private String name;
    private String color;
}
