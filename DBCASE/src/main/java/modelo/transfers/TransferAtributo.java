package modelo.transfers;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import controlador.Controlador;
@SuppressWarnings("rawtypes")
public class TransferAtributo extends Transfer{

	private int idAtributo;
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
	private Controlador c;
	private int entidad_origenID;
	private String entidad_origenName;

	public TransferAtributo(Controlador c) {
		this.c = c;
		this.entidad_origenID = -1;
	}
	
	public TransferAtributo clonar (){
		TransferAtributo clon_ta = new TransferAtributo(c);
		clon_ta.setIdAtributo(this.getIdAtributo());
		clon_ta.setNombre(this.getNombre());
		clon_ta.setDominio(this.getDominio());
		clon_ta.setCompuesto(this.getCompuesto());
		clon_ta.setNotnull(this.getNotnull());
		clon_ta.setUnique(this.getUnique());
		clon_ta.setListaComponentes((Vector) this.getListaComponentes().clone());
		clon_ta.setMultivalorado(this.isMultivalorado());
		clon_ta.setPosicion((Point2D) this.getPosicion().clone());
		clon_ta.setListaRestricciones((Vector) this.getListaRestricciones().clone());
		clon_ta.setClavePrimaria(this.isClavePrimaria());
		clon_ta.setVolumen(this.getVolumen());
		clon_ta.setFrecuencia(this.getFrecuencia());
		clon_ta.setEntidad_origenID(this.getEntidad_origenID());
		clon_ta.setEntidad_origenName(this.getEntidad_origenName());
		return clon_ta;
	}

	public void CopiarAtributo(TransferAtributo arg0){
		this.idAtributo = arg0.getIdAtributo();
		this.nombre = arg0.getNombre();
		this.dominio = arg0.getDominio();
		this.compuesto = arg0.getCompuesto();
		this.notnull = arg0.getNotnull();
		this.unique = arg0.getUnique();
		this.listaComponentes = arg0.getListaComponentes();
		this.volumen = arg0.getVolumen();
		this.frecuencia = arg0.getFrecuencia();
		this.multivalorado = arg0.isMultivalorado();
		this.listaRestricciones = arg0.getListaRestricciones();
		this.posicion = new Point2D.Double(arg0.getPosicion().getX(),
				   arg0.getPosicion().getY());
		this.ClavePrimaria = arg0.isClavePrimaria();
		this.entidad_origenID = arg0.getEntidad_origenID();
		this.entidad_origenName = arg0.getEntidad_origenName();

	}
	
	@Override
	public Point2D getPosicion() {
		return posicion;
	}
	public void setPosicion(Point2D posicion) {
		this.posicion = posicion;
	}
	public boolean getCompuesto() {
		return compuesto;
	}
	public void setCompuesto(boolean compuesto) {
		this.compuesto = compuesto;
	}
	public boolean getNotnull() {
		return notnull;
	}
	public boolean isNullable() {
		return c.isNullAttrs() && !notnull && !ClavePrimaria && !compuesto;
	}
	public void setNotnull(boolean notnull) {
		this.notnull = notnull;
	}
	public boolean getUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public String getDominio() {
		return dominio;
	}
	public void setDominio(String dominio) {
		this.dominio = dominio;
	}
	public int getIdAtributo() {
		return idAtributo;
	}
	public void setIdAtributo(int idAtributo) {
		this.idAtributo = idAtributo;
	}
	public Vector getListaComponentes() {
		return listaComponentes;
	}
	public void setListaComponentes(Vector listaComponentes) {
		this.listaComponentes = listaComponentes;
	}
	public boolean isMultivalorado() {
		return multivalorado;
	}
	public void setMultivalorado(boolean multivalorado) {
		this.multivalorado = multivalorado;
	}
	public boolean isClavePrimaria() {
		return ClavePrimaria;
	}
	public void setClavePrimaria(boolean clavePrimaria) {
		ClavePrimaria = clavePrimaria;
	}
	public Vector getListaRestricciones() {
		return listaRestricciones;
	}
	public void setListaRestricciones(Vector listaRestricciones) {
		this.listaRestricciones = listaRestricciones;
	}
	public int getVolumen() {
		return volumen;
	}
	public void setVolumen(int volumen) {
		this.volumen = volumen;
	}
	public int getFrecuencia() {
		return frecuencia;
	}
	public void setFrecuencia(int frecuencia) {
		this.frecuencia = frecuencia;
	}
	public boolean isSubatributo() {
		return subatributo;
	}
	public void setSubatributo(boolean subatributo) {
		this.subatributo = subatributo;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	@Override
	public String toString() {
		return isNullable() ? (this.nombre + "*") : this.nombre;
	}
	@Override
	public Shape toShape() {
		Ellipse2D figura;
		// Si el tamaño del nombre es pequeño dibuja elipse standard
		if (this.nombre.length() < 8){
			figura = new Ellipse2D.Double(-50,-20,100,40);
			return figura;
		}
		// Si es grande ajusta el tamaño al nombre
		int anchura = this.nombre.length() * 11 / 2;
		int altura = this.nombre.length() * 6 / 2;
		figura = new Ellipse2D.Double(-anchura,-altura,anchura * 2,altura*2);
		
		return figura;
	}
	// Dibuja la segunda elipse externa
	public Shape outerShape(){
		Ellipse2D figura;
		// Si el tamaño del nombre es pequeño dibuja elipse standard
		if (this.nombre.length() < 8){
			figura = new Ellipse2D.Double(-55,-25,110,50);
			return figura;
		}
		// Si es grande ajusta el tamaño al nombre
		int anchura = (this.nombre.length() * 11 / 2) + 5;
		int altura = (this.nombre.length() * 6 / 2) + 5;
		figura = new Ellipse2D.Double(-anchura,-altura,anchura * 2,altura*2);
		return figura;
	}

	public int getEntidad_origenID() {
		return entidad_origenID;
	}

	public void setEntidad_origenID(int entidad_origenID) {
		this.entidad_origenID = entidad_origenID;
	}

	public String getEntidad_origenName() {
		return entidad_origenName;
	}

	public void setEntidad_origenName(String entidad_origenName) {
		this.entidad_origenName = entidad_origenName;
	}

	
}
