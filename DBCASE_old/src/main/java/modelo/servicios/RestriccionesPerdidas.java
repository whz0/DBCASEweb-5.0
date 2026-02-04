package modelo.servicios;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class RestriccionesPerdidas extends ArrayList<RestriccionPerdida> {

    @Override
    public String toString() {
        String total = "";
        String candidata = "";
        String tabla = "";
        for (RestriccionPerdida r : this) {
            switch (r.getTipo()) {
                case RestriccionPerdida.TOTAL:
                    total += r;
                    break;
                case RestriccionPerdida.CANDIDATA:
                    candidata += r;
                    break;
                case RestriccionPerdida.TABLA:
                    tabla += r;
                    break;
                default:
                    break;
            }
        }
        String res = "";
        // res += (candidata!="")?"<h3>"+Lenguaje.text(Lenguaje.CANDIDATE_KEYS)+"</h3>"+candidata:"";
        // res += (total!="")?"<h3>"+Lenguaje.text(Lenguaje.CARDINALITY)+"</h3>"+total:"";
        // res += (tabla!="")?"<h3>"+Lenguaje.text(Lenguaje.TABLE_CONSTR)+"</h3>"+tabla:"";
        res += (candidata != "") ? "<h5>Claves Candidatas</h5>" + candidata : "";
        res += (total != "") ? "<h5>Cardinalidad</h5>" + total : "";
        res += (tabla != "") ? "<h5>Tabla de Constantes</h5>" + tabla : "";
        return res;
    }


    public String toString(String clavCandidatas, String cardinalidad, String constantes) {
        String total = "";
        String candidata = "";
        String tabla = "";
        for (RestriccionPerdida r : this) {
            switch (r.getTipo()) {
                case RestriccionPerdida.TOTAL:
                    total += r;
                    break;
                case RestriccionPerdida.CANDIDATA:
                    candidata += r;
                    break;
                case RestriccionPerdida.TABLA:
                    tabla += r;
                    break;
                default:
                    break;
            }
        }
        String res = "";
        // res += (candidata!="")?"<h3>"+Lenguaje.text(Lenguaje.CANDIDATE_KEYS)+"</h3>"+candidata:"";
        // res += (total!="")?"<h3>"+Lenguaje.text(Lenguaje.CARDINALITY)+"</h3>"+total:"";
        // res += (tabla!="")?"<h3>"+Lenguaje.text(Lenguaje.TABLE_CONSTR)+"</h3>"+tabla:"";
        // res += (candidata!="")?"<h5>Claves Candidatas</h5>"+candidata:"";
        // res += (total!="")?"<h5>Cardinalidad</h5>"+total:"";
        // res += (tabla!="")?"<h5>Tabla de Constantes</h5>"+tabla:"";
        res += (candidata != "") ? "<h5>" + clavCandidatas + "</h5>" + candidata : "";
        res += (total != "") ? "<h5>" + cardinalidad + "</h5>" + total : "";
        res += (tabla != "") ? "<h5>" + constantes + "</h5>" + tabla : "";
        return res;
    }

}
