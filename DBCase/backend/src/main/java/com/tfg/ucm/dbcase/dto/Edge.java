package com.tfg.ucm.dbcase.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Edge {

    private String source;
    private String target;
    private String label;
}
