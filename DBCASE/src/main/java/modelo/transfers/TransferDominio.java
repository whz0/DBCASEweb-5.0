package modelo.transfers;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Vector;
@SuppressWarnings("rawtypes")
public class TransferDominio extends Transfer{
	private int idDominio;
	private String nombre;
	private TipoDominio tipoBase;
	
	private Vector listaValores;
	
	public TransferDominio(){
		
	};

	public TransferDominio clonar(){
		TransferDominio clon_td = new TransferDominio();
		clon_td.setIdDominio(this.getIdDominio());
		clon_td.setNombre(this.getNombre());
		clon_td.setTipoBase(this.getTipoBase());
		clon_td.setListaValores((Vector) this.getListaValores().clone());
		return clon_td;
	}
	
	public void CopiarDominio(TransferDominio arg0){
		this.idDominio = arg0.getIdDominio();
		this.nombre = arg0.getNombre();
		this.tipoBase = arg0.getTipoBase();
		this.listaValores = arg0.getListaValores();
	}
	
	public int getIdDominio() {
		return idDominio;
	}
	public void setIdDominio(int idDominio) {
		this.idDominio = idDominio;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public TipoDominio getTipoBase() {
		return tipoBase;
	}
	public void setTipoBase(TipoDominio tipoBase) {
		this.tipoBase = tipoBase;
	}
	public Vector getListaValores() {
		return listaValores;
	}
	public void setListaValores(Vector listaValores) {
		this.listaValores = listaValores;
	}
	
	@Override
	public String toString() {
		return this.nombre;
	}
	@Override
	public Point2D getPosicion() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Shape toShape() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVolumen() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFrecuencia() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVolumen(int v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFrecuencia(int f) {
		// TODO Auto-generated method stub
		
	}
	

	
	
	
}


