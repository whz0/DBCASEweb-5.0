package vista.diagrama.geometria;


/**
 * <Insert the type's description here.>Representamos puntos del plano como elementos
 de R^2
 * <br>Fecha: (7/7/01 18:04:25)
 * @author: Ricardo de los Santos Villodres l&t;rdv00155@averroes.cica.es>
 * <br>Maite Ruiz Garcia l&t;mrg00156@averroes.cica.es>
 */
@SuppressWarnings("serial")
public class Punto extends java.awt.geom.Point2D.Double {
	public final static Punto O_=new Punto(0.0,0.0);
	/**
 * <Punto constructor comment.>
 */
public Punto() {
	super();
}
/**
 * <Punto constructor comment.>Punto a partir de sus coordenadas en la referencia can�nica
 de R^2
 * @param arg1 double
 * @param arg2 double
 */
public Punto(double arg1, double arg2) {
	super(arg1, arg2);
}
/**
 * <Insert the method's description here.>Copiamos el punto p
 <br>
 * Fecha: (20/7/01 11:55:06)
 * @param v api2d.Vector
 */
public Punto(Punto p) {
	this();
	this.x=p.getX();
	this.y=p.getY();
	}
/**
 * <Descripcion>�Es este punto el mismo que P?
 * <br>
 * Fecha: (8/7/01 13:33:08)
 * @return boolean
 * @param P api2d.Punto
 */
public boolean equals(Punto P) {
	if(this.getX()==P.getX() && this.getY()==P.getY())return true;
	return false;
}
}
