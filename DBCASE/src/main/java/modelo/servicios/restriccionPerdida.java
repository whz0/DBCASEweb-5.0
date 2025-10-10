package modelo.servicios;

import vista.lenguaje.Lenguaje;

public class restriccionPerdida {

	private String relacion;
	private String clave;
	private String entidad;
	private String restriccion;
	private int[] card;
	private int tipo;
	public static final int TOTAL = 1;
	public static final int CANDIDATA = 2;
	public static final int TABLA = 3;
	
	//Para tipo candidata y tabla
	public restriccionPerdida(String par1, String par2, int tipo) {
		this.card = new int[2];
		switch(tipo) {
		case CANDIDATA:
			this.clave = par1;
			this.relacion = par2;
			break;
		case TABLA:
			this.entidad = par1;
			this.restriccion = par2;
			break;
		}
		this.tipo = tipo;
	}
	
	//Para tipo total
	public restriccionPerdida(String relacion, String entidad, int from, int to, int tipo) {
		this.card = new int[2];
		this.relacion = relacion;
		this.entidad = entidad;
		this.card[0] = from;
		this.card[1] = to;
		this.tipo = tipo;
	}
	
	public int getTipo() {
		return tipo;
	}
	
	//Genera el codigo en HTML
	@Override
	public String toString() {
			String s = "<p> ";
			switch(tipo) {
			case TOTAL:
				s += entidad + " -> " + relacion + descTotal();
				break;
			case CANDIDATA:
				s+= clave + " es una clave candidata de la relacion ";
				s+= relacion;
				break;
			case TABLA:
				s += entidad + ".";
				s += restriccion.replace("<", "&lt;") + " ";
				break;
			default:break;
			}
			s += "</p>";
			return s;
	}
	private String descTotal() {
		// String res=" ";
		// boolean c = false;
		// if(card[0]==1) res+=Lenguaje.text(Lenguaje.PART_TOTAL);
		// else if(card[0]>1 && card[0]<Integer.MAX_VALUE) {
		// 	res+=res.equals(" ") ? (Lenguaje.text(Lenguaje.CARDMINDE) + card[0]) : (Lenguaje.text(Lenguaje.YCARDMINDE) + card[0]);
		// 	c=true;
		// }
		// if(card[1]>1 && card[1]<Integer.MAX_VALUE)
		// 	res+=res.equals(" ") ? (Lenguaje.text(Lenguaje.CARDMAXDE) + card[1]) : (c?Lenguaje.text(Lenguaje.YMAXDE)+ card[1] :Lenguaje.text(Lenguaje.YCARDMAXDE) + card[1]);
		// return res+=Lenguaje.text(Lenguaje.OF) + entidad.split("\\(")[0] + Lenguaje.text(Lenguaje.FOR) + relacion.split("\\(")[0];

		String res=" ";
		boolean c = false;
		if(card[0]==1) res+="participacion total";
		else if(card[0]>1 && card[0]<Integer.MAX_VALUE) {
			res+=res.equals(" ") ? ("cardinalidad minima de " + card[0]) : (" y una cardinalidad minima de " + card[0]);
			c=true;
		}
		if(card[1]>1 && card[1]<Integer.MAX_VALUE)
			res+=res.equals(" ") ? ("Máxima cardinalidad de "+ card[1]) : (c?" y máximo de "+ card[1] :" y una cardinalidad máxima de " + card[1]);
		return res+=" de " + entidad.split("\\(")[0] + " en " + relacion.split("\\(")[0];
	}
}