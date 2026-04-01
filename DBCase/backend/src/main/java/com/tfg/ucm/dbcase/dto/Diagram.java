package com.tfg.ucm.dbcase.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Diagram {

    private List<Node> nodes;
    private List<Edge> edges;
}
