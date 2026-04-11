package com.tfg.ucm.dbcase.dto;

import lombok.Data;

@Data
public class Participant {

    private Node entity;
    private String role;
    private String cardinality;
}
