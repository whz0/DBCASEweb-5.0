package com.tfg.ucm.dbcase.dto;

import java.util.List;
import lombok.Builder;

@Builder
public class Undefined {
    private int id;
    private String name;
    private List<Attribute> attributes;
}
