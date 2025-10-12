package modelo.servicios;

import controlador.Controlador;
import modelo.transfers.*;
import org.springframework.context.MessageSource;
import persistencia.*;

import java.util.Vector;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ValidadorBD extends GeneradorEsquema {
    private String mensaje;
    private static ValidadorBD INSTANCE;
    private String errores = "";
    private String warnings = "";

    public ValidadorBD(MessageSource messageSource) {
        super(messageSource);
    }

    public ValidadorBD() {
        super();
    }

    public static ValidadorBD getInstancia() {
        if (INSTANCE == null) INSTANCE = new ValidadorBD();
        return INSTANCE;
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    //Dado un transfer y un texto, genera un error (formato html)
    private void error(Transfer t, String texto) {
        if (t instanceof TransferEntidad)
            texto = this.getTraduction("textosId.theentity") + " " + t.getNombre() + " " + texto;
        else if (t instanceof TransferRelacion)
            texto = this.getTraduction("textosId.therelation") + " " + t.getNombre() + " " + texto;
        else if (t instanceof TransferAtributo)
            texto = this.getTraduction("textosId.theattribute") + " " + t + " " + texto;
        else if (t instanceof TransferDominio)
            texto = this.getTraduction("textosId.thedomain") + " " + t.getNombre() + " " + texto;
        errores += "<p class='text-dark'>" + texto + "</p>";
    }

    //Dado un transfer y un texto, genera un warning (formato html)
    private void warning(Transfer t, String texto) {
        if (t instanceof TransferEntidad)
            texto = this.getTraduction("textosId.theentity") + " " + t.getNombre() + " " + texto;
        else if (t instanceof TransferRelacion)
            texto = this.getTraduction("textosId.therelation") + " " + t.getNombre() + " " + texto;
        else if (t instanceof TransferAtributo)
            texto = this.getTraduction("textosId.theattribute") + " " + t + " " + texto;
        else if (t instanceof TransferDominio)
            texto = this.getTraduction("textosId.thedomain") + " " + t.getNombre() + " " + texto;
        warnings += "<p class='text-dark'>" + texto + "</p>";
    }

    private void construyeErroresWarnings() {
        if (!warnings.isEmpty())
            mensaje += "<div class='pl-1 pt-1 pr-1 alert alert-warning'><p class='h5 text-dark font-weight-bold'>" + this.getTraduction("textosId.warning") + "</p>" + warnings + "</div>";
        if (!errores.isEmpty())
            mensaje += "<div class='pl-1 pt-1 pr-1 alert alert-danger'><p class='h5 text-dark font-weight-bold'>" + this.getTraduction("textosId.error") + "</p>" + errores + "</div>";
    }

    /*
     * metodo principal
     * boolean modelo: diferencia entre esquema logico y fisico
     */
    protected boolean validaBaseDeDatos(boolean modelo, StringBuilder warning) {
        mensaje = "";
        warnings = "";
        this.errores = "";
        boolean valido;
        if (esVacio()) {
            warning(null, this.getTraduction("textosId.emptyDiagram"));
            valido = false;
        } else {
            valido = true;
            valido &= this.validaDominios();
            valido &= this.validaAtributos();
            valido &= this.validaEntidades();
            valido &= this.validaRelaciones();
        }
        // Mostrar el texto
        construyeErroresWarnings();
        if (!valido)
            //if(modelo)controlador.mensajeDesde_SS(TC.SS_ValidacionM,mensaje);
            //else controlador.mensajeDesde_SS(TC.SS_ValidacionC,mensaje);
            warning.append(mensaje);
        return valido;
    }

    private boolean esVacio() {
        DAOEntidades daoEntidades = new DAOEntidades(controlador.getPath());
        DAORelaciones daoRelaciones = new DAORelaciones(controlador.getPath());
        return daoRelaciones.ListaDeRelaciones().isEmpty() && daoEntidades.ListaDeEntidades().isEmpty();
    }

    private boolean validaEntidades() {
        DAOEntidades daoEntidades = new DAOEntidades(controlador.getPath());
        Vector<TransferEntidad> entidades = daoEntidades.ListaDeEntidades();
        boolean valido = true;
        int i = 0;
        TransferEntidad t = new TransferEntidad();
        while (i < entidades.size() && valido) {
            t = entidades.elementAt(i);
            // No validamos las claves si se trata de una agregacion
            if (t.getIdEntidad() >= 1000) {
                //por ahora validamos las claves y avisamos de si es padre de varias isA
                valido &= validaKey(t); //&& this.validaNombresAtributosEntidad(t);
                validaFidelidadEntidadEnIsA(t);
            }
            i++;
        }
        return valido;
    }

    private boolean validaNombresAtributosEntidad(TransferEntidad te) {
        //comprueba que una entidad tenga atributos con nombres distintos.
        Vector<TransferAtributo> ats = dameAtributosEnTransfer(te.getListaAtributos());
        if (ats.size() < 1) {
            error(te, this.getTraduction("textosId.noAttrib"));
            return false;
        }
        TransferAtributo ti, tj;
        int i = 0, j = 1;
        boolean valido = true;
        while (i < ats.size()) {
            ti = ats.elementAt(i);
            while (j < ats.size()) {
                tj = ats.elementAt(j);
                if (ti.getNombre().equalsIgnoreCase(tj.getNombre())) {
                    valido = false;
                    error(tj, this.getTraduction("textosId.isRepeatedInEnt") + te.getNombre());
                }
                j++;
            }
            i++;
            j = i + 1;
        }
        return valido;
    }

    private boolean validaComponentesRelacionDebil(TransferRelacion tr) {
        boolean valida = true;
        Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
        int tam = veya.size();
        int contD = 0, contF = 0;
        switch (tam) {
            case 0:
                error(tr, this.getTraduction("textosId.noEntRelation"));
                valida = false;
                break;
            case 1:
                error(tr, this.getTraduction("textosId.oneEntWeakRel"));
                valida = false;
                break;
            default:
                //en una relacion debil, necesitamos que haya como minimo una entidad fuerte y una debil.
                contD = this.dameNumEntidadesDebiles(tr);
                contF = tam - contD;
        }
        if (contD < 1) {
            error(tr, this.getTraduction("textosId.noWeakEntRel"));
            valida = false;
        }
        if (contD > 1) {
            error(tr, this.getTraduction("textosId.manyWeakEntRel"));
            valida = false;
        }
        if (contF < 1) {
            error(tr, this.getTraduction("textosId.noStrongEntRel"));
            valida = false;
        }
        return valida;
    }

    private void validaFidelidadEntidadEnIsA(TransferEntidad te) {
        DAORelaciones daoRelaciones = new DAORelaciones(controlador.getPath());
        Vector<TransferRelacion> relaciones = daoRelaciones.ListaDeRelaciones();
        //si la entidad es padre de una relacion isA comprueba que solo lo sea de una, sino, dara un aviso.
        int i = 0;
        int papi = 0;
        while (i < relaciones.size()) {
            TransferRelacion tr = relaciones.elementAt(i);
            if (tr.isIsA()) {
                Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
                if (!veya.isEmpty() && veya.elementAt(0).getEntidad() == te.getIdEntidad()) papi++;
            }
            i++;
        }
        if (papi > 1)
            warning(te, this.getTraduction("textosId.entParentIn") + " " + papi + this.getTraduction("textosId.isaRelations"));
    }

    //metodos privados de validacion de relaciones.
    private boolean validaComponentesRelacionIsA(TransferRelacion tr) {
        boolean valida = true;
        if (dameNumEntidadesDebiles(tr) > 0) {
            valida = false;
            error(tr, this.getTraduction("textosId.noWeakEntRel"));
        } else {
            Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
            int tam = veya.size();
            switch (tam) {
                case 0:
                    error(tr, this.getTraduction("textosId.noParentRel"));
                    valida = false;
                    break;
                case 1:
                    error(tr, this.getTraduction("textosId.noChildRel"));
                    valida = false;
                    break;
                default:
                    break;

            }
        }
        return valida;
    }

    private int dameNumEntidadesDebiles(TransferRelacion tr) {
        DAOEntidades daoEntidades = new DAOEntidades(controlador.getPath());
        TransferEntidad aux = new TransferEntidad();
        Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
        int cont = 0;
        for (int k = 0; k < veya.size(); k++) {
            aux.setIdEntidad(veya.elementAt(k).getEntidad());
            aux = daoEntidades.consultarEntidad(aux);
            if (aux.isDebil()) cont++;
        }
        return cont;
    }

    private boolean misDebilesEstanEnDebiles(TransferRelacion rel) {
        DAORelaciones daoRelaciones = new DAORelaciones(this.getControlador().getPath());
        DAOEntidades daoEntidades = new DAOEntidades(this.getControlador().getPath());
        Vector<TransferRelacion> relaciones = daoRelaciones.ListaDeRelaciones();
        boolean enDebil = false;
        boolean encontrada = false;
        TransferEntidad entDebil = new TransferEntidad();
        int k = 0;
        Vector<EntidadYAridad> veyaRel = rel.getListaEntidadesYAridades();
        while (k < veyaRel.size() && !encontrada) {
            EntidadYAridad eya = veyaRel.elementAt(k);
            entDebil.setIdEntidad(eya.getEntidad());
            entDebil = daoEntidades.consultarEntidad(entDebil);
            if (entDebil.isDebil()) encontrada = true;
            k++;
        }
        int i = 0;
        int j = 0;
        while (i < relaciones.size() && !enDebil) {
            TransferRelacion tr = relaciones.elementAt(i);
            Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
            j = 0;
            while (j < veya.size() && !enDebil) {
                EntidadYAridad eya = veya.elementAt(j);
                if (eya.getEntidad() == entDebil.getIdEntidad() && tr.getTipo().equals("Debil")) enDebil = true;
                j++;
            }
            i++;
        }
        return enDebil;
    }

    private boolean validaComponentesRelacionNormal(TransferRelacion tr) {
        boolean valida = true;
        if (dameNumEntidadesDebiles(tr) > 0 && !this.misDebilesEstanEnDebiles(tr)) {
            valida = false;
            error(tr, this.getTraduction("textosId.noWeakEntRel"));
        } else {
            Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
            int tam = veya.size();
//			for(int i =0; i< veya.size();i++){
//				if(veya.get(i).getEntidad() <1000) tam=-1;
//			}
            switch (tam) {
                case 0:
                    error(tr, this.getTraduction("textosId.noEntRelation"));
                    valida = false;
                    break;
                case 1:
                    error(tr, this.getTraduction("textosId.oneEntRel"));
                    valida = false;
                    break;
                default:
                    break;
            }
        }
        return valida;
    }

    private boolean validaRelaciones() {
        DAORelaciones daoRelaciones = new DAORelaciones(controlador.getPath());
        Vector<TransferRelacion> relaciones = daoRelaciones.ListaDeRelaciones();
        boolean valido = true;
        int i = 0;
        TransferRelacion t = new TransferRelacion();
        while (i < relaciones.size()) {
            t = relaciones.elementAt(i);
            if (t.isIsA()) valido &= validaComponentesRelacionIsA(t);
            else if (t.getTipo().equals("Normal"))
                valido &= validaComponentesRelacionNormal(t);
            else valido &= validaComponentesRelacionDebil(t);
            i++;
        }
        return valido;
    }

    private boolean validaDominios() {
        DAODominios daoDominios = new DAODominios(super.controlador.getPath());
        Vector<TransferDominio> dominios = daoDominios.ListaDeDominios();
        boolean valido = true;
        int i = 0;
        TransferDominio t;
        while (i < dominios.size()) {
            t = dominios.get(i);
            boolean encontrado = false;
            int j = i + 1;
            while (!encontrado && j < dominios.size()) {
                encontrado = t.getNombre().equals(dominios.get(j).getNombre());
                j++;
            }
            if (encontrado) {
                valido = false;
                error(t, this.getTraduction("textosId.repeatedDomNames"));
            }
            // Validar que tiene valores y son distintos
            Vector<String> valores = t.getListaValores();
            if (valores == null || valores.size() < 1) {
                valido = false;
                error(t, this.getTraduction("textosId.noValueDom"));
            } else {
                String valorComprobado = null;
                int k = 0;
                boolean seRepite = false;
                while (k < valores.size() && !seRepite) {
                    valorComprobado = valores.get(k);
                    int m = k + 1;
                    while (m < valores.size() && !seRepite) {
                        seRepite = (valorComprobado.equals(valores.get(m)));
                        m++;
                    }
                    k++;
                }
                if (seRepite) {
                    valido = false;
                    error(t, this.getTraduction("textosId.theValue") + valorComprobado + this.getTraduction("textosId.isRepeated"));
                }
            }
            // Comprobar si se usa (esto solo da un aviso si falla)
            DAOAtributos daoAtributos = new DAOAtributos(controlador);
            Vector<TransferAtributo> atributos = daoAtributos.ListaDeAtributos();
            boolean esta = false;
            int k = 0;
            while (!esta && k < atributos.size()) {
                esta = atributos.get(k).getDominio().equalsIgnoreCase(t.getNombre());
                k++;
            }
            if (!esta) warning(t, this.getTraduction("textosId.noUseDom"));
            i++;
        }
        return valido;
    }

    //metodos privados de validacion de entidades
    private boolean validaKey(TransferEntidad te) {
        DAOAtributos daoAtributos = new DAOAtributos(controlador);
        //valida si la entidad tiene clave y si esta dentro de sus atributos.
        //ademas si la entidad es debil, debe tener un atributo discriminante.
        boolean valido = true;
        boolean noMulti = true;
        boolean compuesto = false;
        Vector atbs = te.getListaAtributos();
        Vector keys = te.getListaClavesPrimarias();
        int contador = 0;
        TransferAtributo aux = new TransferAtributo(controlador);
        Vector<int[]> resultados = entidadPerteneceAisA(te);
        int enIsA = 0;

        switch (resultados.size()) {
            case 0:
                enIsA = -1;
                break; //no aparece
            case 1:
                enIsA = resultados.elementAt(0)[1];
                break; //aparece una vez nos quedamos con lo que haya
            default:  //si aparece mas nos quedamos con la que tenga como padre
                for (int m = 0; m < resultados.size(); m++) if (resultados.elementAt(m)[1] == 1) enIsA = 1;
                break;
        }

        // si no tiene clave primaria
        boolean relacionada = false;
        if (keys.isEmpty() && enIsA <= 0) {
            // Comprobar que no esta asociada a ninguna relacion
            DAORelaciones daoRelaciones = new DAORelaciones(controlador.getPath());
            Vector<TransferRelacion> rels = daoRelaciones.ListaDeRelaciones();
            int k = 0;
            while (k < rels.size() && !relacionada) {
                Vector<EntidadYAridad> ents = rels.get(k).getListaEntidadesYAridades();
                int m = 0;
                while (!relacionada && m < ents.size()) {
                    relacionada = ents.get(m).getEntidad() == te.getIdEntidad();
                    m++;
                }
                k++;
            }
            if (relacionada) {
                if (te.isDebil()) error(te, this.getTraduction("textosId.noKeyWeakEntity"));
                else error(te, this.getTraduction("textosId.noKeyEntRel"));
                valido = false;
            }
        } else
            // si tiene clave primaria, que esten dentro de sus atributos
            relacionada = true;
        if (!te.isDebil())
            if (!this.vectorEnterosContenidoEnVector(keys, atbs)) {
                //dos casos. que la clave sea un atbto compuesto o que no haya clave.
                //comprobamos que hay un atbto compuesto y si lo hay, lo comprobamos.
                while (contador < atbs.size()) {
                    aux.setIdAtributo(this.objectToInt(atbs.elementAt(contador)));
                    aux = daoAtributos.consultarAtributo(aux);
                    if (aux.getCompuesto()) {
                        if (compruebaClaveCompuesto(keys, aux)) compuesto = true;

                    }
                    contador++;
                }
                if (compuesto) warning(te, this.getTraduction("textosId.allChildrenKey"));
                else {
                    valido = false;
                    error(te, this.getTraduction("textosId.noAttribKey"));
                }
            } else { // comprobamos que no haya una clave que sea un atributo multivalorado.
                while (contador < keys.size() && noMulti) {
                    aux.setIdAtributo(this.objectToInt(keys.elementAt(contador)));
                    aux = daoAtributos.consultarAtributo(aux);
                    if (aux.isMultivalorado()) {
                        valido = false;
                        noMulti = false;
                        error(aux, this.getTraduction("textosId.multivalueKey"));
                    }
                    contador++;
                }
            }
        if (valido && !relacionada) warning(te, this.getTraduction("textosId.noPrimaryKey"));
        return valido;
    }

    private boolean vectorEnterosContenidoEnVector(Vector subVector, Vector vector) {
        // comprueba si un vector de enteros esta dentro de otro si estos no estan parametrizados (no funciona contains)
        boolean esta = false;
        boolean contiene = true;
        for (int i = 0; i < subVector.size(); i++) {
            esta = false;
            for (int j = 0; j < vector.size(); j++)
                if (objectToInt(subVector.elementAt(i)) == objectToInt(vector.elementAt(j))) esta = true;
            contiene = esta && contiene;
        }
        return contiene;
    }

    /*lo que hay que tener en cuenta para ver si una entidad pertenece a una relacion isA (como hija) segun nuestro
     disenio es que sea como minimo la segunda de la lista de entidades y aridades de la relacion. Esto es porque
     la primera indica que es el padre y este si tiene que tener clave.
     El metodo devuelve lo siguiente:
     en la primera componente:
     la relacion isA en la que actua.
     en la segunda componente:
     si no pertenece a una isA -> -1
     sino: si es padre -> 0
     si es hija -> 1
     */
    private Vector<int[]> entidadPerteneceAisA(TransferEntidad te) {
        DAORelaciones daoRelaciones = new DAORelaciones(controlador.getPath());
        Vector<TransferRelacion> relaciones = daoRelaciones.ListaDeRelaciones();
        Vector<int[]> resultados = new Vector<int[]>();

        int i = 0;
        int j = 0;
        while (i < relaciones.size()) {
            TransferRelacion tr = relaciones.elementAt(i);
            Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
            int[] parejaResultados = new int[2];
            parejaResultados[0] = tr.getIdRelacion();
            while (j < veya.size()) {
                EntidadYAridad eya = veya.elementAt(j);
                if (eya.getEntidad() == te.getIdEntidad() && tr.isIsA()) {
                    if (j == 0) {
                        parejaResultados[1] = 0;
                        resultados.add(parejaResultados);
                    } else {
                        parejaResultados[1] = 1;
                        resultados.add(parejaResultados);
                    }
                }
                j++;
            }
            j = 0;
            i++;
        }
        return resultados;
    }

    private boolean compruebaClaveCompuesto(Vector clavesEntidad, TransferAtributo ta) {
        DAOAtributos daoAtributos = new DAOAtributos(controlador);
        int i = 0;
        boolean todosBien = true;
        Vector subs = ta.getListaComponentes();
        TransferAtributo aux = new TransferAtributo(controlador);
        if (!ta.getCompuesto()) {
            return estaEnVectorDeEnteros(clavesEntidad, ta.getIdAtributo());
        } else {
            while (i < subs.size() && todosBien) {
                aux.setIdAtributo(objectToInt(subs.elementAt(i)));
                aux = daoAtributos.consultarAtributo(aux);
                todosBien = todosBien && compruebaClaveCompuesto(clavesEntidad, aux);
                i++;
            }
            return todosBien;
        }
    }

    /* hemos de validar lo siguiente:
     * - Cada atributo pertenece a una sola entidad
     * - Cada atributo tiene un dominio definido
     * - Los atributos multivalorados no son clave
     */
    private boolean validaAtributos() {
        DAOAtributos daoAtributos = new DAOAtributos(controlador);
        Vector<TransferAtributo> atributos = daoAtributos.ListaDeAtributos();
        boolean valido = true;
        int i = 0;
        TransferAtributo t = new TransferAtributo(controlador);
        while (i < atributos.size()) {
            t = atributos.elementAt(i);
            valido &= validaFidelidadAtributo(t) && validaDominioDeAtributo(t);
            if (t.getCompuesto()) valido &= validaCompuesto(t);
            i++;
        }
        return valido;
    }

    // comprueba si el atributo pertenece solo a una entidad.
    private boolean validaFidelidadAtributo(TransferAtributo ta) {
        DAOAtributos daoAtributos = new DAOAtributos(controlador);
        DAOEntidades daoEntidades = new DAOEntidades(controlador.getPath());
        DAORelaciones daoRelaciones = new DAORelaciones(controlador.getPath());
        Vector<TransferAtributo> atributos = daoAtributos.ListaDeAtributos();
        Vector<TransferEntidad> entidades = daoEntidades.ListaDeEntidades();
        Vector<TransferRelacion> relaciones = daoRelaciones.ListaDeRelaciones();
        boolean valido = true;
        boolean enEntidad = false;
        TransferEntidad te = new TransferEntidad();
        TransferRelacion tr = new TransferRelacion();
        int cont = 0;
        int i = 0;
        while (i < entidades.size() && cont <= 1) {
            te = entidades.elementAt(i);
            if (estaEnVectorDeEnteros(te.getListaAtributos(), ta.getIdAtributo())) {
                cont++;
                enEntidad = true;
            }
            i++;
        }
        if (!enEntidad) {
            cont = 0;
            i = 0;
            while (i < relaciones.size() && cont <= 1) {
                tr = relaciones.elementAt(i);
                if (estaEnVectorDeEnteros(tr.getListaAtributos(), ta.getIdAtributo())) cont++;
                i++;
            }
        }

        if (cont == 0) {
            //entonces es un subatributo, comprobamos q no esta repetido entre los subatributos
            i = 0;
            int contSubAtrib = 0;
            TransferAtributo aux = new TransferAtributo(controlador);
            while (i < atributos.size() && contSubAtrib <= 1) {
                aux = atributos.elementAt(i);
                if (aux.getCompuesto())
                    if (estaEnVectorDeEnteros(aux.getListaComponentes(), ta.getIdAtributo()))
                        contSubAtrib++;
                i++;
            }
            if (contSubAtrib != 1) {
                error(ta, this.getTraduction("textosId.manyAttribSubattrib"));
                valido = false;
            }
        } else if (cont != 1) {
            error(ta, this.getTraduction("textosId.manyEntities"));
            valido = false;
        }
        return valido;
    }

    private boolean validaCompuesto(TransferAtributo ta) {
        boolean valido = true;
        int numSubs = ta.getListaComponentes().size();
        switch (numSubs) {
            case 0:
                valido = false;
                error(ta, this.getTraduction("textosId.noSubattrib"));
                break;
            case 1:
                warning(ta, this.getTraduction("textosId.oneSubattrib"));
                break;
        }

        return valido;
    }

    private String quitaParenDominio(String dominio) {
        int c = dominio.indexOf("(");
        return dominio.substring(0, c);
    }

    private boolean validaDominioDeAtributo(TransferAtributo ta) {
        //comprueba que tenga dominio
        boolean valido = true;
        String dom = ta.getDominio();
        if (ta.getCompuesto()) {
            if (!dom.equals("null")) {
                valido = false;
                error(ta, this.getTraduction("textosId.composedAttribute"));
            }
        } else {
            if (dom.equals("") || dom.equals("null")) {
                valido = false;
                error(ta, this.getTraduction("textosId.noDomain"));
            } else {
                if (dom.contains("(")) dom = quitaParenDominio(dom);
                int i = 0;
                while (i < TipoDominio.values().length && !TipoDominio.values()[i].equals(dom)) i++;
                if (i > TipoDominio.values().length) {
                    error(ta, this.getTraduction("textosId.unkownDomain"));
                    valido = false;
                }
            }
        }
        return valido;
    }
}