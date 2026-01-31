package modelo.transfers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Vector;

@SuppressWarnings("rawtypes")
@Getter
@Setter
@NoArgsConstructor
public class TransferDominio extends Transfer {
    private int idDominio;
    private String nombre;
    private TipoDominio tipoBase;

    private Vector listaValores;

    public TransferDominio clonar() {
        TransferDominio clon_td = new TransferDominio();
        clon_td.setIdDominio(this.getIdDominio());
        clon_td.setNombre(this.getNombre());
        clon_td.setTipoBase(this.getTipoBase());
        clon_td.setListaValores((Vector) this.getListaValores().clone());
        return clon_td;
    }

    @Override
    public String toString() {
        return this.nombre;
    }

    @Override
    public Point2D getPosicion() {
        return null;
    }

    @Override
    public Shape toShape() {
        return null;
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
    public void setVolumen(int v) {}

    @Override
    public void setFrecuencia(int f) {}
}


