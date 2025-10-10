package modelo.servicios;

import java.util.ArrayList;
import java.util.HashMap;

public class DataAtributoEntidadOrigen {
    private int idAtributo;
    private int idEntidad;
    private String nameEntidad;

    public DataAtributoEntidadOrigen(int idAtributo, int idEntidad, String nameEntidad) {
        this.idAtributo = idAtributo;
        this.idEntidad = idEntidad;
        this.nameEntidad = nameEntidad;
    }

    public int getIdAtributo() {
        return idAtributo;
    }

    public void setIdAtributo(int idAtributo) {
        this.idAtributo = idAtributo;
    }

    public int getIdEntidad() {
        return idEntidad;
    }

    public void setIdEntidad(int idEntidad) {
        this.idEntidad = idEntidad;
    }

    public String getNameEntidad() {
        return nameEntidad;
    }

    public void setNameEntidad(String nameEntidad) {
        this.nameEntidad = nameEntidad;
    }
    
}