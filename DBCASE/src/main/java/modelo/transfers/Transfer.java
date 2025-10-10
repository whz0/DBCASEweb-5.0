package modelo.transfers;

import java.awt.Shape;
import java.awt.geom.Point2D;

/*
 *	Clase abstracta para realizar polimorfismo sobre todos los Transfers
 */
public abstract class Transfer {

	/*
	 *	Transforma el transfer en la cadena mostrada 
	 */
	@Override
	public abstract String toString();
	
	/*
	 *	Retorna el Shape del tipo para dibujarlo en el panel 
	 */
	public abstract Shape toShape();
	
	/*
	 *	Retorna la posición en el grafo en la que se encuentra 
	 */
	public abstract Point2D getPosicion();

	public abstract String getNombre();
	
	public abstract int getVolumen();
	
	public abstract int getFrecuencia();
	
	public abstract void setVolumen(int v);
	
	public abstract void setFrecuencia(int f);
}
