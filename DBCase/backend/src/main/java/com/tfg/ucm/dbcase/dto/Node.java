package com.tfg.ucm.dbcase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    private String name;
    private boolean isAttribute;
    private boolean isPk;
    private boolean isNotNull;
    private boolean isUnique;
    private Domain dataType;
    private String reference;
}
