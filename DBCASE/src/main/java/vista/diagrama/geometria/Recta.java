package vista.diagrama.geometria;

public class Recta {
	private Punto punto;
	private Vector2D direccion;

/**
 * <Descripcion>Recta que pasa por los puntos P y Q.
 * <br>
 * Fecha: (27/7/01 13:50:23)
 * @param P api2d.Punto
 * @param Q api2d.Punto
 * @exception api2d.Exception Los puntos P y Q deben ser distintos.
 */
public Recta(Punto P, Punto Q) throws Exception{
	this(P,new Vector2D(P,Q));
}

@SuppressWarnings("serial")
public Recta(Punto P, Vector2D v) throws Exception{
	if(v.equals(Vector2D.O_))throw new Exception(){};
	this.punto=P;
	this.direccion=v;
	}


public Recta() {
	super();
}

/**
 * <Descripcion>Distancia desde el punto P hasta esta recta.
 * <br>
 * Fecha: (27/7/01 13:43:57)
 * @return double
 * @param p api2d.Punto
 */
public double distancia(Punto P) {
	Vector2D v=new Vector2D(punto,P);
	return Math.abs(v.prod(normal()));
}

public Vector2D normal() {
	return new Vector2D((-1)*direccion().getY(),direccion().getX());
	}

public Vector2D direccion() {
	Vector2D v=new Vector2D(direccion);
	if(v.getX()==0){
		direccion=new Vector2D(0,1);
		}else if(v.getX()<0){
			v=v.prod(-1);
			}
	return v.prod(1/v.modulo());
}

private Vector2D vectorNormal() {
	return new Vector2D(-direccion.getY(),direccion.getX());
}

public Recta perpendicular(Punto P) {
	Recta r= new Recta();
	try{
		r=new Recta(P,vectorNormal());
		}catch(Exception e){}
	return r;
}

public boolean pasaPor(Punto P) {
	double eps = 0.01;
	double c = direccion.det(new Vector2D(punto,P)); 
	if(	(c < eps) && (c>-eps) )
		return true;
	else
		return false;
}


/**
 * <Descripcion>Calculamos el punto de corte de esta recta con la recta r.
 * <br>
 * Fecha: (27/7/01 13:44:38)
 * @return api2d.Punto
 * @param r api2d.Recta
 * @exception api2d.Exception La excepcion se produce cuando las rectas no se
 cortan o cuando coinciden.
 */
public Punto interseccion(Recta r) {
	try{
		double denominador=this.direccion.det(r.direccion);
		if(denominador==0){
			return null;
		}	
		Vector2D AB=new Vector2D(this.punto,r.punto);
		double t=AB.det(r.direccion)/denominador;
		return this.direccion.prod(t).accion(this.punto);
	}catch( Exception e ){
		return null;
	}
}

}//class
