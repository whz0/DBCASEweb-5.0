package com.tfg.ucm.dbcase.dto;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Undefined extends Node {
    private int id;
    private String name;
    private List<Attribute> attributes;
}
