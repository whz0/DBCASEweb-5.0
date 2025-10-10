package persistencia;

import vista.lenguaje.Lenguaje;

public class EntidadYAridad {
	private int entidad;
	private int pRango;
	private int fRango;
	private String rol;

	@Override
	public EntidadYAridad clone() {
		EntidadYAridad clon_eya = new EntidadYAridad();
		clon_eya.setEntidad(this.getEntidad());
		clon_eya.setPrincipioRango(this.getPrincipioRango());
		clon_eya.setFinalRango(this.getFinalRango());
		clon_eya.setRol(this.getRol());
		return clon_eya;
	}

	public EntidadYAridad() {}

	public EntidadYAridad(int entidad, int principioRango, int finalRango, String rol) {
		this.entidad = entidad;
		this.pRango = principioRango;
		this.fRango = finalRango;
		this.rol=rol;
	}
	
	public int getEntidad() {
		return entidad;
	}

	public void setEntidad(int entidad) {
		this.entidad = entidad;
	}

	public int getFinalRango() {
		return fRango;
	}

	public void setFinalRango(int finalRango) {
		this.fRango = finalRango;
	}

	public int getPrincipioRango() {
		return pRango;
	}

	public void setPrincipioRango(int principioRango) {
		this.pRango = principioRango;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}	
	public String hazCadenaChachi() {
		String cadena;
		String frangoaux;
		if (fRango == Integer.MAX_VALUE)
			frangoaux = "n";
		else
			frangoaux = Integer.toString(fRango);
		cadena = "(" + entidad + "," + pRango + "," + frangoaux +"," + rol + ")";
		return cadena;
	}

	public EntidadYAridad sacaValoresDeString(String cadena) {
		int coma1 = cadena.indexOf(",");
		int coma2 = cadena.substring(coma1 + 1).indexOf(",") + coma1 + 1;
		int coma3 = cadena.substring(coma2 + 1).indexOf(",") + coma2 + 1;
		int e, p, f;
		String r;
		String aux;
		e = Integer.parseInt(cadena.substring(1, coma1)); //e= id de la entidad
		p = Integer.parseInt(cadena.substring(coma1 + 1, coma2));//p=principio de la aridad
		r= cadena.substring(coma3+1,cadena.length()-1);//r= rol
		aux = cadena.substring(coma2+1,coma3);// aux=fin de la aridad
		if (aux.equals("n")) f = Integer.MAX_VALUE;
		else f = Integer.parseInt(aux);
		return new EntidadYAridad(e, p, f,r);
	}
	
	@Override
	public String toString(){
		String a = Lenguaje.text(Lenguaje.ENT_ARITY);
		a = a + Lenguaje.text(Lenguaje.ROL) + rol + Lenguaje.text(Lenguaje.ID_ENT) + entidad; 
		return a;
	}
}