package modelo.transfers;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import persistencia.EntidadYAridad;
@SuppressWarnings({"rawtypes", "unchecked"})
public class TransferRelacion extends Transfer {

	private int idRelacion;
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
	private int offsetAttr=0;

	public TransferRelacion() {}
	public boolean isIsA() {
		return getTipo().equals("IsA");
	}
	
	public TransferRelacion clonar() {
		TransferRelacion clon_tr = new TransferRelacion();
		clon_tr.setIdRelacion(this.getIdRelacion());
		clon_tr.setNombre(this.getNombre());
		clon_tr.setTipo(this.getTipo());
		clon_tr.setRol(this.getRol());
		clon_tr.setListaEntidadesYAridades((Vector) this.getListaEntidadesYAridades().clone());
		clon_tr.setListaAtributos((Vector) this.getListaAtributos().clone());
		if (!clon_tr.isIsA()){
			clon_tr.setListaRestricciones((Vector) this.getListaRestricciones().clone());
			clon_tr.setListaUniques((Vector) this.getListaUniques().clone());
		}
		clon_tr.setPosicion((Point2D) this.getPosicion().clone());
		clon_tr.setVolumen(this.getVolumen());
		clon_tr.setFrecuencia(this.getFrecuencia());
		clon_tr.setOffsetAttr(this.getOffsetAttr());
		return clon_tr;
	}
	
	//Antes sólo tenía un parámetro, el primero; arg0
	public void CopiarRelacion(TransferRelacion arg0,int idBuscado,Boolean repetido) {
		this.idRelacion = arg0.getIdRelacion();
		this.nombre = arg0.getNombre();
		this.tipo = arg0.getTipo();
		this.listaEntidadesYAridades = arg0.getListaEntidadesYAridades();
		this.listaAtributos = arg0.getListaAtributos();
		this.listaRestricciones = arg0.getListaRestricciones();
		this.volumen = arg0.getVolumen();
		this.frecuencia = arg0.getFrecuencia();
		this.listaUniques = arg0.getListaUniques();
		this.rol=arg0.getRol();
		this.offsetAttr = arg0.getOffsetAttr();
		//Si entidad ya está asociada con dicha relación, la línea que las unirá deberá ser diferente a la existente
		//Filtramos la lista de entidades quitando las entidades que ya estan
		//Puesto que se van a permitir hacer relaciones circulares no filtramos la lista de entidades
		Vector<EntidadYAridad> vectorTupla = this.listaEntidadesYAridades;
		Vector vectorIdsEntidades = new Vector();
		int cont = 0;
		while(cont<vectorTupla.size()){
			vectorIdsEntidades.add((vectorTupla.get(cont)).getEntidad());
			cont++;
		}
		cont = 0;
		int limite=this.getListaEntidadesYAridades().size();
		while((cont<limite) && (repetido==false)){
			if(vectorIdsEntidades.contains(idBuscado)) repetido=true;
			cont++;
		}
		if (repetido) this.posicion = new Point2D.Double(arg0.getPosicion().getX(), arg0.getPosicion().getY());
		else this.posicion = new Point2D.Double(arg0.getPosicion().getX(), arg0.getPosicion().getY());
	}
	
	public Point2D nextAttributePos(Point2D p) {
		int ancho = getNombre().length();
		//para evitar las esquinas
		if(p.getX()>0 && p.getX()<(100 + (ancho<8?0:2*ancho)+(((offsetAttr/8+1)*8)/8)*62.5) && offsetAttr%8>4)
			offsetAttr = (offsetAttr/8+1)*8;
		if(p.getY()>0 && p.getY()<(100+(offsetAttr/8)*62.5) && (offsetAttr%8==7 || offsetAttr%8<2))
			offsetAttr = (offsetAttr/8+1)*8 + 2;
		
		ancho = 120 + (ancho<8?0:2*ancho)+(offsetAttr/8)*75;
		int alto = 80+(offsetAttr/8)*50;
		double constant = 2.4674011002723395;
		
		//coloca los atributos en circulos concentricos
		p.setLocation(
				Math.round(ancho*Math.sin(offsetAttr/(Math.PI/constant))+p.getX()), 
				Math.round(alto*Math.sin(offsetAttr/(Math.PI/constant)-(Math.PI/2))+p.getY())
				);
		offsetAttr++;
		return p;
	}
	
	public void CopiarRelacionUnoUno(Vector v) {
		this.idRelacion = (Integer)v.get(0);
		this.nombre = (String)v.get(4);
		this.tipo = (String)v.get(7);
		this.listaEntidadesYAridades =(Vector) v.get(5);
		this.listaAtributos = (Vector)v.get(6);
		this.rol = (String)v.get(8);
		//Si entidad ya está asociada con dicha relación, la línea que las unirá deberá ser diferente a la existente
		Vector<EntidadYAridad> vectorTupla = this.listaEntidadesYAridades;
		Vector vectorIdsEntidades = new Vector();
		int cont = 0;
		while(cont<vectorTupla.size()){
			vectorIdsEntidades.add((vectorTupla.get(cont)).getEntidad());
			cont++;
		}			
	}
	
	public boolean hayFlechas() {
		for (Iterator it = listaEntidadesYAridades.iterator(); it.hasNext();){
			EntidadYAridad e = (EntidadYAridad)it.next();
			if(e.getFinalRango() == 1)return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.nombre;
	}

	@Override
	public Shape toShape() {
		// Si es IsA retorna el triángulo
		if (getTipo().equals(new String("IsA"))) return this.toShapeIsA();
		
		// Si es grande ajusta el tamaño al nombre
		int anchura = Math.max(this.nombre.length() * 11 / 2,50);
		int altura = Math.max(this.nombre.length() * 5 / 2,30);
		Polygon p = new Polygon();
		p.addPoint(-anchura, 0);
		p.addPoint(0, -altura);
		p.addPoint(anchura, 0);
		p.addPoint(0, altura);
		return p;
	}

	private Shape toShapeIsA() {
		Polygon p = new Polygon();		
		p.addPoint(-35, -23);
		p.addPoint(35, -23);
		p.addPoint(0, 35);
		return p;
	}

	// Dibuja el segundo rombo externo
	public Shape outerShape() {
		Polygon figura;
		// Si el tamaño del nombre es pequeño dibuja rombo standard
		if (this.nombre.length() < 8) {
			figura = new Polygon();
			figura.addPoint(-57, 0);
			figura.addPoint(0, -35);
			figura.addPoint(57, 0);
			figura.addPoint(0, 35);
			return figura;
		}
		// Si es grande ajusta el tamaño al nombre
		int anchura = (this.nombre.length() * 11 / 2) + 7;
		int altura = (this.nombre.length() * 5 / 2) + 5;
		figura = new Polygon();
		figura.addPoint(-anchura, 0);
		figura.addPoint(0, -altura);
		figura.addPoint(anchura, 0);
		figura.addPoint(0, altura);
		return figura;
	}
	
	public int getIdRelacion() {
		return idRelacion;
	}
	public void setIdRelacion(int idRelacion) {
		this.idRelacion = idRelacion;
	}
	public Vector getListaAtributos() {
		return listaAtributos;
	}
	public void setListaAtributos(Vector listaAtributos) {
		this.listaAtributos = listaAtributos;
	}
	public Vector getListaEntidadesYAridades() {
		return listaEntidadesYAridades;
	}
	public void setListaEntidadesYAridades(Vector listaEntidadesYAridades) {
		this.listaEntidadesYAridades = listaEntidadesYAridades;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getRol() {
		return rol;
	}
	public void setRol(String nuevoRol) {
		this.rol = nuevoRol;
	}
	public int getOffsetAttr() {
		return offsetAttr;
	}
	public void setOffsetAttr(int offsetAttr) {
		this.offsetAttr = offsetAttr;
	}
	@Override
	public Point2D getPosicion() {
		return posicion;
	}
	public void setPosicion(Point2D posicion) {
		this.posicion = posicion;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public Vector getListaRestricciones() {
		return listaRestricciones;
	}
	public void setListaRestricciones(Vector listaRestricciones) {
		this.listaRestricciones = listaRestricciones;
	}
	public Vector getListaUniques() {
		return listaUniques;
	}
	public void setListaUniques(Vector listaUniques) {
		this.listaUniques = listaUniques;
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
	public EntidadYAridad getEntidadYAridad(int idEntidad) {
		for(int i=0;i<listaEntidadesYAridades.size();i++)
    		if(((EntidadYAridad) listaEntidadesYAridades.get(i)).getEntidad() == idEntidad)
    			return (EntidadYAridad) listaEntidadesYAridades.get(i);
		return null;
	}
}
