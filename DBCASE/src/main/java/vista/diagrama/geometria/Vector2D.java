package vista.diagrama.geometria;


/**
 * Los objetos de esta clase son vectores de R^2
 * <br>Fecha: (7/7/01 18:13:04)
 * @author: Ricardo de los Santos Villodres l&t;rdv00155@averroes.cica.es>
 * <br>Maite Ruiz Garcia l&t;mrg00156@averroes.cica.es>
 */
@SuppressWarnings("serial")
public class Vector2D extends java.awt.geom.Point2D.Double {
	/**
Vector cero de R^2.
*/	
	public final static Vector2D O_=new Vector2D(0,0);
	/**
Primer vector de la base can�nica de R^2
*/	
	public final static Vector2D I_=new Vector2D(1,0);
	/**
Segundo vector de la base can�nica de R^2
*/	
	public final static Vector2D J_=new Vector2D(0,1);

/**
 * <Vector constructor comment.>
 */
public Vector2D() {
	super();
}
/**
 * <Vector constructor comment.>Constructor heredado de la superclase. Aqu� v1 y v2 son
 las componentes de este vector
 
 * @param arg1 double
 * @param arg2 double
 */
public Vector2D(double v1, double v2) {
	super(v1, v2);
}

/**
 * <Insert the method's description here.>Vector AB
 <br>Fecha: (7/7/01 18:14:40)
 * @param A api2d.Punto
 * @param B api2d.Punto
 */
public Vector2D(Punto A, Punto B) {
	super(B.getX()-A.getX(),B.getY()-A.getY());
	}

/**
 * <Insert the method's description here.>Creamos una copia del vector v
 * <br>Fecha: (20/7/01 11:55:06)
 * @param v api2d.Vector
 */
public Vector2D(Vector2D v) {
	this();
	this.x=v.getX();
	this.y=v.getY();
	}


/**
 * <Insert the method's description here.>ATrasladamos el punto p seg�n este vector.
 * <br>Fecha: (9/7/01 12:14:10)
 * @return api2d.Punto
 * @param p api2d.Punto
 */
public Punto accion(Punto p) {
	return new Punto(this.getX()+p.getX(),this.getY()+p.getY());
}
/**
 * <Insert the method's description here.>Multiplicamos este vector por el n�mero a.
 * <br>Fecha: (8/7/01 13:53:01)
 * @return api2d.Vector2D
 * @param a double
 */
public Vector2D prod(double a) {
	return new Vector2D(this.getX()*a,this.getY()*a);
}
/**
 * <Insert the method's description here.>Vector av
 * <br>Fecha: (8/7/01 13:56:33)
 * @return api2d.Vector
 * @param a double
 * @param v api2d.Vector
 */
public static Vector2D prod(double a, Vector2D v) {
	return v.prod(a);
}

/**
 * <Insert the method's description here.>Producto escalar de este vector por w.
 * <br>Fecha: (7/7/01 18:18:51)
 * @return double
 * @param w api2d.Vector2D
 */
public double prod(Vector2D w) {
	return (this.getX()*w.getX()+this.getY()*w.getY());
}
/**
 * <Insert the method's description here.>Producto escalar v.w
 * <br>Fecha: (8/7/01 13:54:40)
 * @return double
 * @param v api2d.Vector
 * @param w api2d.Vector
 */
public static double prod(Vector2D v, Vector2D w) {
	return v.prod(w);
}
public double det(Vector2D w) {
	return this.getX()*w.getY()-this.getY()*w.getX();
}


/**
 * <Insert the method's description here.>Si este vector es v devolvemos v+w.
 * <br>Fecha: (20/7/01 12:10:07)
 * @return api2d.Vector2D
 * @param w api2d.Vector2D
 */
public Vector2D suma(Vector2D w) {
	return new Vector2D(this.getX()+w.getX(),this.getY()+w.getY());
		
}
public double modulo() {
	return this.distance(O_);
	}


}
