package com.tfg.ucm.dbcase.model;

import java.util.Vector;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import lombok.Data;

@Entity
@Data
public class Domain {

    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
    //sprivate TipoDominio tipoBase;
    private Vector listaValores;
}
