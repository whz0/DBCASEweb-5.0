package modelo.transfers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * Encapsula la información referente a una conexión a base de datos
 *
 * @author Denis Cepeda
 */
@AllArgsConstructor
@Getter
@Setter
public class TransferConexion extends Transfer {
    private int tipoConexion;
    private String ruta;
    private boolean crearConexion;

    private String database;
    private String usuario;
    private String password;

    public TransferConexion(int tipo, String nombre) {
        this.tipoConexion = tipo;
        this.ruta = nombre;
        this.crearConexion = false;
        this.database = "";
        this.usuario = "";
        this.password = "";
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public Shape toShape() {
        return null;
    }

    @Override
    public Point2D getPosicion() {
        return null;
    }

    @Override
    public String getNombre() {
        return "";
    }

    @Override
    public int getVolumen() {
        return 0;
    }

    @Override
    public int getFrecuencia() {
        return 0;
    }

    @Override
    public void setVolumen(int v) {

    }

    @Override
    public void setFrecuencia(int f) {

    }
}