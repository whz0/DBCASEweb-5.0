package persistencia;

public class NodoEntidad {
	private String nombre;
	private EntidadYAridad entidadYAridad;
	private byte tipo;
	
	public NodoEntidad(String nombre, EntidadYAridad entidadYAridad, String tipo) {
		this.entidadYAridad = entidadYAridad;
		this.nombre = nombre;
		switch(tipo) {
		case "padre":this.tipo=1;break;
		case "hija":this.tipo=2;break;
		default: this.tipo=0;
		}
	}
	public EntidadYAridad getEntidadYAridad() {
		return entidadYAridad;
	}
	@Override
	public String toString() {
		return nombre;
	}
	public String getRango() {
		int prango = this.entidadYAridad.getPrincipioRango();
		int frango = this.entidadYAridad.getFinalRango();
		String rango = "";
		rango += (prango == 2147483647)?"N":prango;
		rango +=" - ";
		rango += (frango == 2147483647)?"N":frango;
		return rango;
	}
	public boolean esNormal() {
		return tipo==0;
	}
	public boolean esPadre() {
		return tipo==1;
	}
	public boolean esHija() {
		return tipo==2;
	}
}
