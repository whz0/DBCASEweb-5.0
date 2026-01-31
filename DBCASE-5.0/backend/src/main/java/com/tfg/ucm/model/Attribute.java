package com.tfg.ucm.model;

import java.awt.geom.Point2D;
import java.util.Vector;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import lombok.Data;

@Entity
@Data
public class Attribute {

    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
    private String dominio;
    private boolean ClavePrimaria;
    private boolean compuesto;
    private boolean notnull;
    private boolean unique;
    private boolean subatributo;
    private int volumen;
    private int frecuencia;
    private Vector listaComponentes;
    private boolean multivalorado;
    private Vector listaRestricciones;
    private Point2D posicion;
    private int entidad_origenID;
    private String entidad_origenName;
}
