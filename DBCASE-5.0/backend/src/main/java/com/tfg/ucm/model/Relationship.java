package com.tfg.ucm.model;

import java.awt.geom.Point2D;
import java.util.Vector;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import lombok.Data;

@Entity
@Data
public class Relationship {

    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
    private String tipo;
    private String rol;
    private Vector listaEntidadesYAridades;
    private Vector listaAtributos;
    private Vector listaRestricciones;
    private Vector listaUniques;
    private Point2D posicion;
    private int volumen;
    private int frecuencia;
    private int offsetAttr = 0;
}
