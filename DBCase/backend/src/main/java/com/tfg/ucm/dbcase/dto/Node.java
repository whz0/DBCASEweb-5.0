package com.tfg.ucm.dbcase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "name")
public class Node {
    private String name;
    private boolean isAttribute;
    private boolean isPk;
    private boolean isFk;
    private boolean isNotNull;
    private boolean isUnique;
    private Domain dataType;
    private String reference;
}
