package com.tfg.ucm.dbcase.model;

import java.awt.geom.Point2D;
import java.util.Vector;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import lombok.Data;

@Entity
@Data
public class MyEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
    private boolean debil;
    private Vector listaAtributos;
    private Vector listaClavesPrimarias;
    private Vector listaRestricciones;
    private Vector listaUniques;
    private Point2D posicion;
    private int volumen;
    private int frecuencia;
    private int offsetAttr = 0;
}
