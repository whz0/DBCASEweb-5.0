package com.tfg.ucm.dbcase.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(of = "label")
public class Edge {

    private String label;
    private String cardinalityMin;
    private String cardinalityMax;
}
