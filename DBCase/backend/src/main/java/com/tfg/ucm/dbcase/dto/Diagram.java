package com.tfg.ucm.dbcase.dto;

import lombok.Builder;
import lombok.Data;
import org.jgrapht.Graph;

@Data
@Builder
public class Diagram {

    private Graph<Node, Edge> diagram;
}
