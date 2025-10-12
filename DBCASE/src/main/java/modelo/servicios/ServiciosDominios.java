package modelo.servicios;


import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TipoDominio;
import modelo.transfers.TransferDominio;
import persistencia.DAODominios;
import vista.lenguaje.Lenguaje;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ServiciosDominios {

    // Controlador
    Controlador controlador;

    public void ListaDeDominios() {
        Object[] items = modelo.transfers.TipoDominio.values();
        DAODominios dao = new DAODominios(this.controlador.getPath());
        Vector<TransferDominio> lista_dominios = dao.ListaDeDominios();
        for (int i = 0; i < items.length; i++) {
            TransferDominio td = new TransferDominio();
            td.setNombre(items[i].toString());
            td.setTipoBase((TipoDominio) items[i]);
            td.setListaValores(null);
            lista_dominios.add(td);
        }
        controlador.mensajeDesde_SD(TC.SD_ListarDominios_HECHO, lista_dominios);
    }

    /* Anadir Dominio
     * Parametros: un TransferDominio que contiene el nombre del nuevo dominio
     * Devuelve: El dominio en un TransferDominio y el mensaje -> SD_InsertarDominio_HECHO
     * Condiciones:
     * Si el nombre es vacio -> SD_InsertarDominio_ERROR_NombreDeDominioEsVacio
     * Si el nombre ya existe -> SD_InsertarDominio_ERROR_NombreDeDominioYaExiste
     * Si al usar el DAODominio se produce un error -> SD_InsertarDominio_ERROR_DAO
     */
    public void anadirDominio(TransferDominio td) {
        if (td.getNombre().isEmpty()) {
            controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_NombreDeDominioEsVacio, null);
            return;
        }
        for (int i = 0; i < td.getListaValores().size(); i++) {
            if (td.getListaValores().get(i).toString().equals("")) {
                Vector v = new Vector();
                v.add(td);
                v.add(Lenguaje.text(Lenguaje.EMPTY_VALUE));
                controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                return;
            }
        }
        DAODominios daoDominios = new DAODominios(this.controlador.getPath());
        Vector<TransferDominio> lista = daoDominios.ListaDeDominios();
        for (Iterator it = lista.iterator(); it.hasNext(); ) {
            TransferDominio elem_td = (TransferDominio) it.next();
            if (elem_td.getNombre().equalsIgnoreCase(td.getNombre())) {
                controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_NombreDeDominioYaExiste, td);
                return;
            }
        }
        //comprobamos que todos los valores se correspondan con el tipo base
        if (comprobarTipoBase(td)) {
            int id = daoDominios.anadirDominio(td);
            if (id == -1) controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_DAO, null);
            else {
                td.setIdDominio(id);
                controlador.mensajeDesde_SD(TC.SD_InsertarDominio_HECHO, daoDominios.consultarDominio(td));
            }
        }

    }

    /*
     * Renombrar un dominio
     * -> Recibe el dominio y el nuevo nombre
     */
    public void renombrarDominio(Vector v) {
        TransferDominio td = (TransferDominio) v.get(0);
        String nuevoNombre = (String) v.get(1);
        String antiguoNombre = td.getNombre();
        // Si nombre es vacio -> ERROR
        if (nuevoNombre.isEmpty()) {
            controlador.mensajeDesde_SD(TC.SD_RenombrarDominio_ERROR_NombreDeDominioEsVacio, v);
            return;
        }
        DAODominios daoDominio = new DAODominios(this.controlador.getPath());
        Vector<TransferDominio> lista = daoDominio.ListaDeDominios();
        if (lista == null) {
            controlador.mensajeDesde_SD(TC.SD_RenombrarDominio_ERROR_DAODominios, v);
            return;
        }
        for (Iterator it = lista.iterator(); it.hasNext(); ) {
            TransferDominio elem_td = (TransferDominio) it.next();
            if (elem_td.getNombre().equalsIgnoreCase(nuevoNombre) && (elem_td.getIdDominio() != td.getIdDominio())) {
                controlador.mensajeDesde_SD(TC.SD_RenombrarDominio_ERROR_NombreDeDominioYaExiste, v);
                return;
            }
        }
        td.setNombre(nuevoNombre);
        if (!daoDominio.modificarDominio(td)) {
            td.setNombre(antiguoNombre);
            controlador.mensajeDesde_SD(TC.SD_RenombrarDominio_ERROR_DAODominios, v);
        } else {
            v.add(antiguoNombre);
            controlador.mensajeDesde_SD(TC.SD_RenombrarDominio_HECHO, v);
        }
    }


    /* Eliminar dominio
     * Parametros: el TransferEntidad que contiene el dominio que se desea eliminar
     * Devuelve: Un TransferDominio que contiene el dominio eliminado y el mensaje -> SD_EliminarDominio_HECHO
     * Condiciones:
     * Se produce un error al usar el DAODominios -> SD_EliminarDominio_ERROR_DAODominios
     */
    public void eliminarDominio(TransferDominio td) {
        DAODominios daoDominios = new DAODominios(this.controlador.getPath());
        // Eliminamos el Dominio
        if (!daoDominios.borrarDominio(td))
            controlador.mensajeDesde_SD(TC.SD_EliminarDominio_ERROR_DAODominios, td);
        else {
            Vector<Object> vector = new Vector<Object>();
            //Vector<Object> vectorAtributosModificados = new Vector<Object>();
            vector.add(td);
            controlador.mensajeDesde_SD(TC.SD_EliminarDominio_HECHO, vector);
        }
    }

    public void modificarDominio(Vector<Object> v) {
        TransferDominio td = (TransferDominio) v.get(0);
        Vector<String> nuevosValores = (Vector<String>) v.get(1);
        Vector<String> antiguosValores = td.getListaValores();
        modelo.transfers.TipoDominio nuevoTipoB = (modelo.transfers.TipoDominio) v.get(2);
        modelo.transfers.TipoDominio antiguoTipoB = td.getTipoBase();
        // Si nombre es vacio -> ERROR
        if (nuevosValores == null) {
            controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_ERROR_ElementosDominioEsVacio, v);
            return;
        }
        if (nuevoTipoB == null) {
            controlador.mensajeDesde_SD(TC.SD_ModificarTipoBaseDominio_ERROR_TipoBaseDominioEsVacio, v);
            return;
        }
        for (int i = 0; i < nuevosValores.size(); i++) {
            if (nuevosValores.get(i).equals("")) {
                controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_ERROR_ValorNoValido, v);
                return;
            }
        }
        DAODominios daoDominio = new DAODominios(this.controlador.getPath());
        Vector<TransferDominio> lista = daoDominio.ListaDeDominios();
        if (lista == null) {
            controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_ERROR_DAODominios, v);
            return;
        }
        td.setTipoBase(nuevoTipoB);
        td.setListaValores(nuevosValores);
        if (comprobarTipoBase(td)) {
            if (!daoDominio.modificarDominio(td)) {
                td.setListaValores(antiguosValores);
                controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_ERROR_DAODominios, v);
            } else {
                v.add(antiguosValores);
                v.add(antiguoTipoB);
                controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_HECHO, v);
            }
        } else {
            td.setListaValores(antiguosValores);
            td.setTipoBase(antiguoTipoB);
        }
    }

    //Se usa para ordenar los valores
    public void modificarElementosDominio(Vector<Object> v) {
        TransferDominio td = (TransferDominio) v.get(0);
        Vector<String> nuevosValores = (Vector<String>) v.get(1);
        Vector<String> antiguosValores = td.getListaValores();
        // Si nombre es vacio -> ERROR
        if (nuevosValores == null) {
            controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_ERROR_ElementosDominioEsVacio, v);
            return;
        }
        for (int i = 0; i < nuevosValores.size(); i++) {
            if (nuevosValores.get(i).equals("")) {
                controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_ERROR_ValorNoValido, v);
                return;
            }
        }
        DAODominios daoDominio = new DAODominios(this.controlador.getPath());
        Vector<TransferDominio> lista = daoDominio.ListaDeDominios();
        if (lista == null) {
            controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_ERROR_DAODominios, v);
            return;
        }

        td.setListaValores(nuevosValores);
        if (comprobarTipoBase(td)) {
            if (!daoDominio.modificarDominio(td)) {
                td.setListaValores(antiguosValores);
                controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_ERROR_DAODominios, v);
            } else {
                v.add(antiguosValores);
                controlador.mensajeDesde_SD(TC.SD_ModificarElementosDominio_HECHO, v);
            }
        } else td.setListaValores(antiguosValores);
    }

    private boolean comprobarTipoBase(TransferDominio td) {
        Vector listaValores = td.getListaValores();
        modelo.transfers.TipoDominio tipoBase = td.getTipoBase();
        switch (tipoBase) {
            case INTEGER: {
                for (int i = 0; i < listaValores.size(); i++) {
                    try {
                        String s = ((String) listaValores.get(i));
                        @SuppressWarnings("unused")
                        int a = Integer.parseInt(s);
                    } catch (Exception e) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.INCORRECT_NUMBER));
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    }
                }
                break;
            }
            case FLOAT: {
                for (int i = 0; i < listaValores.size(); i++) {
                    String s = ((String) listaValores.get(i));
                    // comprueba que sea una cadena válida
                    boolean resultado;
                    if (s.contains(".")) {
                        Pattern p = Pattern.compile("-?([0-9])+(.[0-9]+)?(E(-?)[0-9]+)?");
                        Matcher m = p.matcher(s);
                        resultado = m.matches();
                    } else {
                        Pattern p = Pattern.compile("-?([0-9])+(E(-?)[0-9]+)?");
                        Matcher m = p.matcher(s);
                        resultado = m.matches();
                    }
                    if (!resultado) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.INCORRECT_VALUE_EX) + " 1, 0.5, 1.5E100, -1, -0.5");
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    }
                }
                break;
            }
            case BIT: {
                for (int i = 0; i < listaValores.size(); i++) {
                    try {
                        String s = ((String) listaValores.get(i));
                        if (!(s.equals("0") || s.equals("1"))) {
                            Vector v = new Vector();
                            v.add(td);
                            v.add(Lenguaje.text(Lenguaje.INCORRECT_BIT_VALUE));
                            controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                            return false;
                        }

                    } catch (Exception e) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.INCORRECT_BIT_VALUE));
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    }
                }
                break;
            }
            case DATE: {
                for (int i = 0; i < listaValores.size(); i++) {
                    String s = (String) listaValores.get(i);
                    if (!(s.startsWith("'") && s.endsWith("'"))) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.QUOTATION_MARKS));
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    } else {
                        s = s.replaceAll("'", "");
                        try {
                            SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd");
                            formatoFecha.setLenient(false);
                            formatoFecha.parse(s);
                            if (s.length() != 8) {
                                Vector v = new Vector();
                                v.add(td);
                                v.add(Lenguaje.text(Lenguaje.INCORRECT_DATE));
                                controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                                return false;
                            }
                        } catch (Exception e) {
                            Vector v = new Vector();
                            v.add(td);
                            v.add(Lenguaje.text(Lenguaje.INCORRECT_DATE));
                            controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                            return false;
                        }
                    }
                }
                break;
            }
            case TIME: {
                for (int i = 0; i < listaValores.size(); i++) {
                    String s = ((String) listaValores.get(i));
                    if (!(s.startsWith("'") && s.endsWith("'"))) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.QUOTATION_MARKS));
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    } else {
                        s = s.replaceAll("'", "");
                        // comprueba que sean correctos
                        Pattern p = Pattern.compile("(([0-1]?[0-9])|([2][0-3]))(:[0-5][0-9])?(:[0-5][0-9])?(.[0-9][0-9]?[0-9]?)?");
                        Matcher m = p.matcher(s);
                        boolean resultado = m.matches();
                        if (!resultado) {
                            Vector v = new Vector();
                            v.add(td);
                            v.add(Lenguaje.text(Lenguaje.INCORRECT_VALUE_EX) + " '00:00:00.999', '22', '22:05', '22:59:59'");
                            controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                            return false;
                        }
                    }
                }
                break;
            }
            case DATETIME: {
                for (int i = 0; i < listaValores.size(); i++) {
                    try {
                        String s = ((String) listaValores.get(i));

                        if (!(s.startsWith("'") && s.endsWith("'"))) {
                            Vector v = new Vector();
                            v.add(td);
                            v.add(Lenguaje.text(Lenguaje.QUOTATION_MARKS));
                            controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                            return false;
                        } else {
                            s = s.replaceAll("'", "");
                            //separo hasta el espacio, la fecha y la hora
                            if (s.indexOf(" ") != -1) {
                                int espacio = s.indexOf(" ");
                                String date = s.substring(0, espacio);
                                String time = s.substring(espacio + 1);
                                System.out.println(date);
                                System.out.println(time);
                                //comprobar date
                                boolean resulDate;
                                try {
                                    SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd");
                                    formatoFecha.setLenient(false);
                                    formatoFecha.parse(date);
                                    //para comprobar que no haya cosas alfinal de la cadena
                                    resulDate = date.length() == 8;

                                } catch (Exception e) {
                                    resulDate = false;
                                }
                                //comprobar time
                                Pattern p = Pattern.compile("(([0-1]?[0-9])|([2][0-3]))(:[0-5][0-9])?(:[0-5][0-9])?(.[0-9][0-9]?[0-9]?)?");
                                Matcher m = p.matcher(time);
                                boolean resulTime = m.matches();
                                //si error en alguno:
                                if (!resulDate || !resulTime) {
                                    Vector v = new Vector();
                                    v.add(td);
                                    v.add(Lenguaje.text(Lenguaje.INCORRECT_VALUE_EX) +
                                            " '20201125','281125 12','281125 12:34:00','281125 12:34:00.200' ");
                                    controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                                    return false;
                                }
                            } else {//es sólo la fecha
                                try {
                                    SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd");
                                    formatoFecha.setLenient(false);
                                    formatoFecha.parse(s);

                                } catch (Exception e) {
                                    Vector v = new Vector();
                                    v.add(td);
                                    v.add(Lenguaje.text(Lenguaje.INCORRECT_VALUE_EX) +
                                            " '20231125','20231125 12','20201125 12:34:00','20191125 12:34:00.200' ");
                                    controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                                    return false;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.INCORRECT_VALUE_EX) +
                                " '20191125','20251125 12','20121125 12:34:00','20081125 12:34:00.200' ");
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    }
                }

                break;
            }
            case BLOB: {
				/*for (int i=0; i<listaValores.size();i++){
					
				}
				*/
                break;
            }
            case GEOMETRY: {
				/*for (int i=0; i<listaValores.size();i++){

				}
				*/
                break;
            }
            case CHAR: {
                for (int i = 0; i < listaValores.size(); i++) {
                    String s = (String) listaValores.get(i);
                    if (!(s.startsWith("'") && s.endsWith("'"))) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.QUOTATION_MARKS));
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    }
                }
                break;
            }
            case VARCHAR: {
                for (int i = 0; i < listaValores.size(); i++) {
                    String s = (String) listaValores.get(i);
                    if (!(s.startsWith("'") && s.endsWith("'"))) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.QUOTATION_MARKS));
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    }
                }
                break;
            }
            case TEXT: {
                for (int i = 0; i < listaValores.size(); i++) {
                    String s = (String) listaValores.get(i);
                    if (!(s.startsWith("'") && s.endsWith("'"))) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.QUOTATION_MARKS));
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    }
                }
                break;
            }
            case DECIMAL: {
                for (int i = 0; i < listaValores.size(); i++) {
                    String s = ((String) listaValores.get(i));
                    // comprueba que no contenga caracteres prohibidos
                    Pattern p = Pattern.compile("-?[0-9]+(.[0-9]+)?");
                    Matcher m = p.matcher(s);
                    boolean resultado = m.matches();
                    if (!resultado) {
                        Vector v = new Vector();
                        v.add(td);
                        v.add(Lenguaje.text(Lenguaje.INCORRECT_VALUE_EX) + " 1, 0.5");
                        controlador.mensajeDesde_SD(TC.SD_InsertarDominio_ERROR_ValorNoValido, v);
                        return false;
                    }
                }
                break;
            }
        }
        return true;
    }

    public Controlador getControlador() {
        return controlador;
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }
}
