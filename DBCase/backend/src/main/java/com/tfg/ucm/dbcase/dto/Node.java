package com.tfg.ucm.dbcase.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Node {

    public Node() {}
    ;

    @EqualsAndHashCode.Include private String name;
    private String color;
}
