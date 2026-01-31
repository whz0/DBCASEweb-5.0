package controlador;

import modelo.servicios.*;
import modelo.transfers.*;
import persistencia.EntidadYAridad;
import vista.GUIPrincipal;
import vista.frames.*;
import vista.lenguaje.Lenguaje;
import vista.tema.Theme;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.Iterator;
import java.util.Vector;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Controlador {
    // GUIs
    private GUIPrincipal theGUIPrincipal;
    private GUI_SaveAs theGUIWorkSpace;
    // Fuera
    private GUI_InsertarEntidad theGUIInsertarEntidad;
    private GUI_InsertarRelacion theGUIInsertarRelacion;
    private GUI_InsertarDominio theGUIInsertarDominio;
    private GUI_Conexion theGUIConexion;
    private GUI_SeleccionarConexion theGUISeleccionarConexion;
    // Entidades
    private GUI_RenombrarEntidad theGUIRenombrarEntidad;
    private GUI_AnadirAtributoEntidad theGUIAnadirAtributoEntidad;
    private GUI_InsertarRestriccionAEntidad theGUIAnadirRestriccionAEntidad;
    private GUI_TablaUniqueEntidad theGUITablaUniqueEntidad;
    // Atributos
    private GUI_RenombrarAtributo theGUIRenombrarAtributo;
    private GUI_EditarDominioAtributo theGUIEditarDominioAtributo;
    private GUI_AnadirSubAtributoAtributo theGUIAnadirSubAtributoAtributo;
    private GUI_InsertarRestriccionAAtributo theGUIAnadirRestriccionAAtributo;
    // Relaciones IsA
    private GUI_EstablecerEntidadPadre theGUIEstablecerEntidadPadre;
    private GUI_QuitarEntidadPadre theGUIQuitarEntidadPadre;
    private GUI_AnadirEntidadHija theGUIAnadirEntidadHija;
    private GUI_QuitarEntidadHija theGUIQuitarEntidadHija;
    // Relaciones Normales
    private GUI_RenombrarRelacion theGUIRenombrarRelacion;
    private GUI_AnadirAtributoRelacion theGUIAnadirAtributoRelacion;
    private GUI_AnadirAtributo theGUIAnadirAtributo;
    private GUI_AnadirEntidadARelacion theGUIAnadirEntidadARelacion;
    private GUI_QuitarEntidadARelacion theGUIQuitarEntidadARelacion;
    private GUI_EditarCardinalidadEntidad theGUIEditarCardinalidadEntidad;
    private GUI_InsertarRestriccionARelacion theGUIAnadirRestriccionARelacion;
    private GUI_TablaUniqueRelacion theGUITablaUniqueRelacion;
    // Dominios
    private GUI_RenombrarDominio theGUIRenombrarDominio;
    private GUI_ModificarDominio theGUIModificarElementosDominio;
    // About
    private GUI_About about;
    // Servicios
    private ServiciosEntidades theServiciosEntidades;
    private ServiciosAtributos theServiciosAtributos;
    private ServiciosRelaciones theServiciosRelaciones;
    private ServiciosDominios theServiciosDominios;
    private GeneradorEsquema theServiciosSistema;
    // Otros
    private String path;
    private Vector<TransferAtributo> listaAtributos;
    private boolean cambios;
    private boolean nullAttrs;
    private File filetemp;
    private File fileguardar;
    private GUI_Pregunta panelOpciones;
    private Theme theme;
    private int modoVista;

    public Controlador() {
        iniciaFrames();
        cambios = false;
        // GUIPrincipal
        theGUIPrincipal = new GUIPrincipal();
        theGUIPrincipal.setControlador(this);
        theme = Theme.getInstancia();
    }

    public Controlador(String debug) throws IOException {
//		iniciaFrames();
        cambios = false;
        theServiciosEntidades = new ServiciosEntidades();
        theServiciosEntidades.setControlador(this);
        theServiciosAtributos = new ServiciosAtributos();
        theServiciosAtributos.setControlador(this);
        theServiciosRelaciones = new ServiciosRelaciones();
        theServiciosRelaciones.setControlador(this);
        theServiciosDominios = new ServiciosDominios();
        theServiciosDominios.setControlador(this);
        theServiciosSistema = new GeneradorEsquema();
        theServiciosSistema.reset();
        theServiciosSistema.setControlador(this);


//		theGUIPrincipal = new GUIPrincipal();
//		theGUIPrincipal.setControlador(this);

        // creamos la carpeta projects si no existe
        File directory = new File(System.getProperty("user.dir") + "/projects");
        if (!directory.exists())
            directory.mkdir();

        // Obtenemos configuración inicial (si la hay)
        ConfiguradorInicial conf = new ConfiguradorInicial();
        conf.leerFicheroConfiguracion();

        // Obtenemos el lenguaje en el que vamos a trabajar
        //Lenguaje.encuentraLenguajes();

        if (conf.existeFichero()) {
            Vector<String> lengs = Lenguaje.obtenLenguajesDisponibles();
            boolean encontrado = false;
            int k = 0;
            while (!encontrado && k < lengs.size()) {
                encontrado = lengs.get(k).equalsIgnoreCase(conf.obtenLenguaje());
                k++;
            }
            if (encontrado)
                Lenguaje.cargaLenguaje(conf.obtenLenguaje());
            else
                Lenguaje.cargaLenguajePorDefecto();
        } else {
            //Lenguaje.cargaLenguajePorDefecto();-
            Theme.loadDefaultTheme();
        }

        this.setFiletemp(File.createTempFile("dbcase", "xml"));
        creaFicheroXML(this.getFiletemp());

        String ruta = this.getFiletemp().getPath();
        this.setPath(ruta);
        this.setNullAttrs(conf.obtenNullAttr());

        // Abrimos el documento guardado últimamente
        File ultimo = new File(conf.obtenUltimoProyecto());
        if (ultimo.exists()) {
            this.setFileguardar(ultimo);
            this.setModoVista(conf.obtenModoVista());
            this.setNullAttrs(conf.obtenNullAttr());
            String abrirPath = conf.obtenUltimoProyecto();
            String tempPath = this.filetemp.getAbsolutePath();
            FileCopy(abrirPath, tempPath);

            // Reinicializamos la GUIPrincipal
            //this.getTheGUIPrincipal().setActiva(this.getModoVista());
            // Reiniciamos los datos de los servicios de sistema
            this.getTheServiciosSistema().reset();
            this.setCambios(false);
            //	this.getTheGUIPrincipal().loadInfo();

        } else {
            this.setModoVista(0);
            //	this.getTheGUIPrincipal().setActiva(this.getModoVista());
        }
        // Establecemos la base de datos por defecto
        //	if (conf.existeFichero())
        //this.getTheGUIPrincipal().cambiarConexion(conf.obtenGestorBBDD());


        // GUIPrincipal
//		theGUIPrincipal = new GUIPrincipal();
//		theGUIPrincipal.setControlador(this);
//		theme = Theme.getInstancia();
    }

    public static void main(String[] args) {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            if ("Nimbus".equals(info.getName())) {
                try {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                         | UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                break;
            }
        // creamos la carpeta projects si no existe
        File directory = new File(System.getProperty("user.dir") + "/projects");
        if (!directory.exists())
            directory.mkdir();

        // Obtenemos configuración inicial (si la hay)
        ConfiguradorInicial conf = new ConfiguradorInicial();
        conf.leerFicheroConfiguracion();

        // Obtenemos el lenguaje en el que vamos a trabajar
        Lenguaje.encuentraLenguajes();
        Theme.loadThemes();
        Theme.changeTheme(conf.obtenTema());

        if (conf.existeFichero()) {
            Vector<String> lengs = Lenguaje.obtenLenguajesDisponibles();
            boolean encontrado = false;
            int k = 0;
            while (!encontrado && k < lengs.size()) {
                encontrado = lengs.get(k).equalsIgnoreCase(conf.obtenLenguaje());
                k++;
            }
            if (encontrado)
                Lenguaje.cargaLenguaje(conf.obtenLenguaje());
            else
                Lenguaje.cargaLenguajePorDefecto();
        } else {
            Lenguaje.cargaLenguajePorDefecto();
            Theme.loadDefaultTheme();
        }
        Controlador controlador = new Controlador();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    controlador.setFiletemp(File.createTempFile("dbcase", "xml"));
                    creaFicheroXML(controlador.getFiletemp());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ERROR_TEMP_FILE),
                            Lenguaje.text(Lenguaje.DBCASE), JOptionPane.ERROR_MESSAGE);
                }
                String ruta = controlador.getFiletemp().getPath();
                controlador.setPath(ruta);
                controlador.setNullAttrs(conf.obtenNullAttr());

                // Abrimos el documento guardado últimamente
                File ultimo = new File(conf.obtenUltimoProyecto());
                if (ultimo.exists()) {
                    controlador.setFileguardar(ultimo);
                    controlador.setModoVista(conf.obtenModoVista());
                    controlador.setNullAttrs(conf.obtenNullAttr());
                    String abrirPath = conf.obtenUltimoProyecto();
                    String tempPath = controlador.filetemp.getAbsolutePath();
                    FileCopy(abrirPath, tempPath);

                    // Reinicializamos la GUIPrincipal
                    controlador.getTheGUIPrincipal().setActiva(controlador.getModoVista());
                    // Reiniciamos los datos de los servicios de sistema
                    controlador.getTheServiciosSistema().reset();
                    controlador.setCambios(false);
                    controlador.getTheGUIPrincipal().loadInfo();

                } else {
                    controlador.setModoVista(0);
                    controlador.getTheGUIPrincipal().setActiva(controlador.getModoVista());
                }
                // Establecemos la base de datos por defecto
                if (conf.existeFichero())
                    controlador.getTheGUIPrincipal().cambiarConexion(conf.obtenGestorBBDD());
            }
        });
    }

    public static boolean creaFicheroXML(File f) {
        FileWriter fw;
        String ruta = f.getPath();
        try {
            fw = new FileWriter(ruta);
            fw.write("<?xml version=" + '\"' + "1.0" + '\"' + " encoding=" + '\"' + "utf-8" + '\"' + " ?>" + '\n');
            // "ISO-8859-1"
            fw.write("<Inf_dbcase>" + "\n");
            fw.write("<EntityList proximoID=\"1\">" + "\n" + "</EntityList>" + "\n");
            fw.write("<RelationList proximoID=\"1\">" + "\n" + "</RelationList>" + "\n");
            fw.write("<AttributeList proximoID=\"1\">" + "\n" + "</AttributeList>" + "\n");
            fw.write("<DomainList proximoID=\"1\">" + "\n" + "</DomainList>");
            fw.write("</Inf_dbcase>" + "\n");
            fw.close();
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ERROR_CREATING_FILE) + "\n" + ruta,
                    Lenguaje.text(Lenguaje.DBCASE), JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void iniciaFrames() {
        // Creamos todos los servicios y les asignamos el controlador
        theServiciosEntidades = new ServiciosEntidades();
        theServiciosEntidades.setControlador(this);
        theServiciosAtributos = new ServiciosAtributos();
        theServiciosAtributos.setControlador(this);
        theServiciosRelaciones = new ServiciosRelaciones();
        theServiciosRelaciones.setControlador(this);
        theServiciosDominios = new ServiciosDominios();
        theServiciosDominios.setControlador(this);
        theServiciosSistema = new GeneradorEsquema();
        theServiciosSistema.reset();
        theServiciosSistema.setControlador(this);

        // Fuera
        theGUIInsertarEntidad = new GUI_InsertarEntidad();
        theGUIInsertarEntidad.setControlador(this);
        theGUIInsertarRelacion = new GUI_InsertarRelacion();
        theGUIInsertarRelacion.setControlador(this);
        theGUIInsertarDominio = new GUI_InsertarDominio();
        theGUIInsertarDominio.setControlador(this);
        theGUIConexion = new GUI_Conexion();
        theGUIConexion.setControlador(this);
        theGUISeleccionarConexion = new GUI_SeleccionarConexion();
        theGUISeleccionarConexion.setControlador(this);

        // Entidades
        theGUIRenombrarEntidad = new GUI_RenombrarEntidad();
        theGUIRenombrarEntidad.setControlador(this);
        theGUIAnadirAtributoEntidad = new GUI_AnadirAtributoEntidad();
        theGUIAnadirAtributoEntidad.setControlador(this);
        theGUIAnadirRestriccionAEntidad = new GUI_InsertarRestriccionAEntidad();
        theGUIAnadirRestriccionAEntidad.setControlador(this);
        theGUIAnadirAtributo = new GUI_AnadirAtributo();
        theGUIAnadirAtributo.setControlador(this);

        // Atributos
        theGUIRenombrarAtributo = new GUI_RenombrarAtributo();
        theGUIRenombrarAtributo.setControlador(this);
        theGUIEditarDominioAtributo = new GUI_EditarDominioAtributo();
        theGUIEditarDominioAtributo.setControlador(this);
        theGUIAnadirSubAtributoAtributo = new GUI_AnadirSubAtributoAtributo();
        theGUIAnadirSubAtributoAtributo.setControlador(this);
        theGUIAnadirRestriccionAAtributo = new GUI_InsertarRestriccionAAtributo();
        theGUIAnadirRestriccionAAtributo.setControlador(this);

        // Relaciones IsA
        theGUIEstablecerEntidadPadre = new GUI_EstablecerEntidadPadre();
        theGUIEstablecerEntidadPadre.setControlador(this);
        theGUIQuitarEntidadPadre = new GUI_QuitarEntidadPadre();
        theGUIQuitarEntidadPadre.setControlador(this);
        theGUIAnadirEntidadHija = new GUI_AnadirEntidadHija();
        theGUIAnadirEntidadHija.setControlador(this);
        theGUIQuitarEntidadHija = new GUI_QuitarEntidadHija();
        theGUIQuitarEntidadHija.setControlador(this);

        // Relaciones Normales
        theGUIRenombrarRelacion = new GUI_RenombrarRelacion();
        theGUIRenombrarRelacion.setControlador(this);
        theGUIAnadirEntidadARelacion = new GUI_AnadirEntidadARelacion();
        theGUIAnadirEntidadARelacion.setControlador(this);
        theGUIQuitarEntidadARelacion = new GUI_QuitarEntidadARelacion();
        theGUIQuitarEntidadARelacion.setControlador(this);
        theGUIEditarCardinalidadEntidad = new GUI_EditarCardinalidadEntidad();
        theGUIEditarCardinalidadEntidad.setControlador(this);
        theGUIAnadirAtributoRelacion = new GUI_AnadirAtributoRelacion();
        theGUIAnadirAtributoRelacion.setControlador(this);
        theGUIAnadirRestriccionARelacion = new GUI_InsertarRestriccionARelacion();
        theGUIAnadirRestriccionARelacion.setControlador(this);

        // Dominios
        theGUIRenombrarDominio = new GUI_RenombrarDominio();
        theGUIRenombrarDominio.setControlador(this);
        theGUIModificarElementosDominio = new GUI_ModificarDominio();
        theGUIModificarElementosDominio.setControlador(this);

        // Otras
        about = new GUI_About();
        theGUIWorkSpace = new GUI_SaveAs();
        theGUIWorkSpace.setControlador(this);
        panelOpciones = new GUI_Pregunta();
    }

    // Mensajes que le manda la GUI_WorkSpace al Controlador
    public void mensajeDesde_GUIWorkSpace(TC mensaje, Object datos) {
        switch (mensaje) {
            case GUI_WorkSpace_Nuevo: {
                this.setPath((String) datos);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        getTheServiciosSistema().reset();
                        theGUIPrincipal.loadInfo();
                        getTheGUIPrincipal().reiniciar();
                    }
                });
                setCambios(false);
                break;
            }
            case GUI_WorkSpace_Click_Abrir: {
                String abrirPath = (String) datos;
                String tempPath = this.filetemp.getAbsolutePath();
                FileCopy(abrirPath, tempPath);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        getTheServiciosSistema().reset();
                        theGUIPrincipal.loadInfo();
                        getTheGUIPrincipal().reiniciar();
                    }
                });
                setCambios(false);
                break;
            }
            case GUI_WorkSpace_Click_Guardar: {

                String guardarPath = (String) datos;
                String tempPath = this.filetemp.getAbsolutePath();

                FileCopy(tempPath, guardarPath);
                this.getTheGUIWorkSpace().setInactiva();
                setCambios(false);
                break;
            }
            case GUI_WorkSpace_ERROR_CreacionFicherosXML: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INITIAL_ERROR) + "\n" + Lenguaje.text(Lenguaje.OF_XMLFILES) + "\n" + this.getPath(), Lenguaje.text(Lenguaje.DBCASE), JOptionPane.ERROR_MESSAGE);
                break;

            }
            default:
                break;
        }// Switch
    }

    // Mensajes que manda el Panel de Diseño al Controlador
    public void mensajeDesde_PanelDiseno(TC mensaje, Object datos) {
        switch (mensaje) {
            case PanelDiseno_Click_InsertarEntidad: {
                Point2D punto = (Point2D) datos;
                this.getTheGUIInsertarEntidad().setPosicionEntidad(punto);
                this.getTheGUIInsertarEntidad().setActiva();
                break;
            }
            case PanelDiseno_Click_InsertarAtributo: {
                Vector<Transfer> listaTransfers = (Vector<Transfer>) datos;
                //removemos IsA
                Iterator<Transfer> itr = listaTransfers.iterator();
                while (itr.hasNext()) {
                    Transfer t = itr.next();
                    if (t instanceof TransferRelacion) {
                        if (((TransferRelacion) t).isIsA())
                            itr.remove();
                    } else if (t instanceof TransferAtributo)
                        if (!((TransferAtributo) t).isCompuesto())
                            itr.remove();
                }
                if (listaTransfers.isEmpty())
                    JOptionPane.showMessageDialog(null,
                            "ERROR.\nAdd an entity or a relation first\n",
                            Lenguaje.text(Lenguaje.ADD_ENTITY_RELATION),
                            JOptionPane.PLAIN_MESSAGE);
                else {
                    this.getTheGUIAnadirAtributo().setListaTransfers(listaTransfers);
                    this.getTheGUIAnadirAtributo().setActiva();
                }
                break;
            }
            case PanelDiseno_Click_RenombrarEntidad: {
                TransferEntidad te = (TransferEntidad) datos;
                this.getTheGUIRenombrarEntidad().setEntidad(te);
                this.getTheGUIRenombrarEntidad().setActiva();
                break;
            }
            case PanelDiseno_Click_DebilitarEntidad: {
                TransferEntidad te = (TransferEntidad) datos;
                if (!te.isDebil() && this.getTheServiciosRelaciones().tieneHermanoDebil(te))
                    JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ALREADY_WEAK_ENTITY), Lenguaje.text(Lenguaje.ERROR), 0);
                else this.getTheServiciosEntidades().debilitarEntidad(te);
                break;
            }
            case PanelDiseno_Click_EliminarEntidad: {
                Vector<Object> v = (Vector<Object>) datos;
                TransferEntidad te = (TransferEntidad) v.get(0);
                boolean preguntar = (Boolean) v.get(1);
                int respuesta = 0;
                if (preguntar) {
                    String tieneAtributos = "";
                    if (!te.getListaAtributos().isEmpty())
                        tieneAtributos = Lenguaje.text(Lenguaje.DELETE_ATTRIBUTES_WARNING) + "\n";
                    String tieneRelacion = "";
                    if (te.isDebil()) tieneRelacion = Lenguaje.text(Lenguaje.WARNING_DELETE_WEAK_RELATION) + "\n";
                    respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.ENTITY) + " \"" + te.getNombre() + "\" " + Lenguaje.text(Lenguaje.REMOVE_FROM_SYSTEM) + "\n" +
                                    tieneAtributos + tieneRelacion + Lenguaje.text(Lenguaje.WISH_CONTINUE),
                            Lenguaje.text(Lenguaje.DELETE_ENTITY));
                }
                //Si quiere borrar la entidad
                /*Se entrará con preguntar a false si se viene de eliminar una relación débil
                 * (para borrar también la entidad y sus atributos)*/
                if ((respuesta == 0) || (!preguntar)) {
                    // Eliminamos sus atributos
                    Vector lista_atributos = te.getListaAtributos();
                    int conta = 0;
                    TransferAtributo ta = new TransferAtributo(this);
                    while (conta < lista_atributos.size()) {
                        String idAtributo = (String) lista_atributos.get(conta);
                        ta.setIdAtributo(Integer.valueOf(idAtributo));
                        this.getTheServiciosAtributos().eliminarAtributo(ta);
                        conta++;
                    }
                    //Si la entidad es débil eliminamos la relación débil asociada
                    if (te.isDebil()) {
                        Vector<TransferRelacion> lista_rel = this.getTheServiciosRelaciones().ListaDeRelacionesNoVoid();
                        int cont = 0, aux = 0;
                        boolean encontrado = false;
                        EntidadYAridad eya;
                        int idEntidad;
                        TransferRelacion tr = new TransferRelacion();
                        //Para cada relación
                        while (cont < lista_rel.size()) {
                            //Si la relación es débil
                            if (lista_rel.get(cont).getTipo().equals("Debil")) {
                                //Compruebo si las entidades asociadas son la entidad débil que se va a eliminar
                                while ((!encontrado) && (aux < lista_rel.get(cont).getListaEntidadesYAridades().size())) {
                                    eya = (EntidadYAridad) (lista_rel.get(cont).getListaEntidadesYAridades().get(aux));
                                    idEntidad = eya.getEntidad();
                                    if (te.getIdEntidad() == idEntidad) {
                                        tr.setIdRelacion(lista_rel.get(cont).getIdRelacion());
                                        this.getTheServiciosRelaciones().eliminarRelacionNormal(tr);
                                        encontrado = true;
                                    }
                                    aux++;
                                }
                                aux = 0;
                            }
                            cont++;
                        }
                    }
                    // Eliminamos la entidad
                    this.getTheServiciosEntidades().eliminarEntidad(te);
                }
                break;
            }
            case PanelDiseno_Click_AnadirRestriccionAEntidad: {
                TransferEntidad te = (TransferEntidad) datos;
                this.getTheGUIAnadirRestriccionAEntidad().setEntidad(te);
                this.getTheGUIAnadirRestriccionAEntidad().setActiva();
                break;
            }
            case PanelDiseno_Click_AnadirRestriccionAAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheGUIAnadirRestriccionAAtributo().setAtributo(ta);
                this.getTheGUIAnadirRestriccionAAtributo().setActiva();
                break;
            }
            case PanelDiseno_Click_AnadirRestriccionARelacion: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIAnadirRestriccionARelacion().setRelacion(tr);
                this.getTheGUIAnadirRestriccionARelacion().setActiva();
                break;
            }
            case PanelDiseno_Click_TablaUniqueAEntidad: {
                TransferEntidad te = (TransferEntidad) datos;
                theGUITablaUniqueEntidad = new GUI_TablaUniqueEntidad();
                theGUITablaUniqueEntidad.setControlador(this);
                this.getTheGUITablaUniqueEntidad().setEntidad(te);
                this.getTheGUITablaUniqueEntidad().setActiva();
                break;
            }
            case PanelDiseno_Click_TablaUniqueARelacion: {
                TransferRelacion tr = (TransferRelacion) datos;
                theGUITablaUniqueRelacion = new GUI_TablaUniqueRelacion();
                theGUITablaUniqueRelacion.setControlador(this);
                this.getTheGUITablaUniqueRelacion().setRelacion(tr);
                this.getTheGUITablaUniqueRelacion().setActiva();
                break;
            }
            case PanelDiseno_Click_AnadirAtributoEntidad: {
                TransferEntidad te = (TransferEntidad) datos;
                this.getTheGUIAnadirAtributoEntidad().setEntidad(te);
                this.getTheGUIAnadirAtributoEntidad().setActiva();
                break;
            }
            case PanelDiseno_Click_RenombrarAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheGUIRenombrarAtributo().setAtributo(ta);
                this.getTheGUIRenombrarAtributo().setActiva();
                break;
            }
            case PanelDiseno_Click_EliminarAtributo: {
                Vector<Object> v = (Vector<Object>) datos;
                TransferAtributo ta = (TransferAtributo) v.get(0);
                boolean preguntar = (Boolean) v.get(1);
                int respuesta = 0;
                if (preguntar) {
                    String eliminarSubatributos = "";
                    if (!ta.getListaComponentes().isEmpty())
                        eliminarSubatributos = Lenguaje.text(Lenguaje.DELETE_ATTRIBUTES_WARNING) + "\n";
                    respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.ATTRIBUTE) + " \"" + ta.getNombre() + "\" " + Lenguaje.text(Lenguaje.REMOVE_FROM_SYSTEM) + "\n" +
                                    eliminarSubatributos + Lenguaje.text(Lenguaje.WISH_CONTINUE),
                            Lenguaje.text(Lenguaje.DELETE_ATTRIB));
                }
                if (respuesta == 0) {
                    if (ta.isUnique()) {
                        TransferAtributo clon_atributo = ta.clonar();
                        this.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarUniqueAtributo, clon_atributo);
                    }
                    TransferAtributo clon_atributo2 = ta.clonar();
                    this.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarReferenciasUniqueAtributo, clon_atributo2);
                    TransferAtributo clon_atributo3 = ta.clonar();
                    this.getTheServiciosAtributos().eliminarAtributo(clon_atributo3);
                }
                break;
            }
            case PanelDiseno_Click_InsertarRelacionNormal: {
                Point2D punto = (Point2D) datos;
                this.getTheGUIInsertarRelacion().setPosicionRelacion(punto);
                this.getTheGUIInsertarRelacion().setActiva();
                break;
            }
            case PanelDiseno_Click_RenombrarRelacion: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIRenombrarRelacion().setRelacion(tr);
                this.getTheGUIRenombrarRelacion().setActiva();
                break;
            }
            /*Aunque desde el panel de diseño no se puede debilitar una relación este caso sigue
             * utilizándose cuando se crea una entidad débil ya que debe generarse también una
             * relación débil asociada a ella.*/
            case PanelDiseno_Click_DebilitarRelacion: {
                TransferRelacion tr = (TransferRelacion) datos;
                //Si es una relacion fuerte...
                if (tr.getTipo().equals("Normal")) {
                    int numDebiles = this.getTheServiciosRelaciones().numEntidadesDebiles(tr);
                    // ...y tiene más de una entidad débil no se puede debilitar
                    if (numDebiles > 1) {
                        JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATION_WEAK_ENTITIES), Lenguaje.text(Lenguaje.ERROR), 0);
                        break;
                    }
                    int respuesta1 = -1;//-1 no hay conflicto, 0 el usuario dice SI, 1 el usuario dice NO
                    int respuesta2 = -1;
                    // ...y tiene atributos y se quiere debilitar hay que eliminar sus atributos
                    if (!tr.getListaAtributos().isEmpty()) {
                        respuesta1 = panelOpciones.setActiva(
                                Lenguaje.text(Lenguaje.WEAK_RELATION) + " \"" + tr.getNombre() + "\"" +
                                        Lenguaje.text(Lenguaje.DELETE_ATTRIBUTES_WARNING2) + "\n" +
                                        Lenguaje.text(Lenguaje.WISH_CONTINUE),
                                Lenguaje.text(Lenguaje.DBCASE));
                    }
                    // ...y tiene una entidad débil hay que cambiar la cardinalidad
                    if (numDebiles == 1 && respuesta1 != 1) {
                        respuesta2 = panelOpciones.setActiva(
                                Lenguaje.text(Lenguaje.WEAK_RELATION) + "\"" + tr.getNombre() + "\"" +
                                        Lenguaje.text(Lenguaje.MODIFYING_CARDINALITY) + ".\n" +
                                        Lenguaje.text(Lenguaje.WISH_CONTINUE),
                                Lenguaje.text(Lenguaje.DBCASE));
                    }
                    if (respuesta2 == 0 && respuesta1 != 1) {
                        //Aqui se fija la cardinalidad de la entidad débil como de 1 a 1.
                        Vector<Object> v = new Vector<Object>();
                        EntidadYAridad informacion;
                        int i = 0;
                        boolean actualizado = false;
                        while ((!actualizado) && (i < tr.getListaEntidadesYAridades().size())) {
                            informacion = (EntidadYAridad) (tr.getListaEntidadesYAridades().get(i));
                            int idEntidad = informacion.getEntidad();
                            if (this.getTheServiciosEntidades().esDebil(idEntidad)) {
                                actualizado = true;
                                int idRelacion = tr.getIdRelacion();
                                int finRango = 1;
                                int iniRango = 1;
                                String nombre = tr.getNombre();
                                Point2D posicion = tr.getPosicion();
                                Vector<Object> listaEnti = tr.getListaEntidadesYAridades();
                                EntidadYAridad aux = (EntidadYAridad) listaEnti.get(i);
                                aux.setFinalRango(1);
                                aux.setPrincipioRango(1);
                                listaEnti.remove(i);
                                listaEnti.add(aux);
                                Vector<Object> listaAtri = tr.getListaAtributos();
                                String tipo = tr.getTipo();
                                String rol = tr.getRol();
                                v.add(idRelacion);
                                v.add(idEntidad);
                                v.add(iniRango);
                                v.add(finRango);
                                v.add(nombre);
                                v.add(listaEnti);
                                v.add(listaAtri);
                                v.add(tipo);
                                v.add(rol);
                                v.add(posicion);
                                this.getTheServiciosRelaciones().aridadEntidadUnoUno(v);
                            }
                            i++;

                        }
                    }
                    if (respuesta1 == 0 && respuesta2 != 1) {
                        // Eliminamos sus atributos
                        Vector lista_atributos = tr.getListaAtributos();
                        int cont = 0;
                        TransferAtributo ta = new TransferAtributo(this);
                        while (cont < lista_atributos.size()) {
                            String idAtributo = (String) lista_atributos.get(cont);
                            ta.setIdAtributo(Integer.valueOf(idAtributo));
                            this.getTheServiciosAtributos().eliminarAtributo(ta);
                            cont++;
                        }
                    }
                    if (respuesta1 != 1 && respuesta2 != 1) {
                        // Modificamos la relacion
                        tr.getListaAtributos().clear();
                        this.getTheServiciosRelaciones().debilitarRelacion(tr);
                    }
                } else {
                    this.getTheServiciosRelaciones().debilitarRelacion(tr);
                }
                break;
            }
            case PanelDiseno_Click_EditarDominioAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheGUIEditarDominioAtributo().setAtributo(ta);
                this.getTheGUIEditarDominioAtributo().setActiva();
                break;
            }
            case PanelDiseno_Click_EditarCompuestoAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                // Si es un atributo compuesto y tiene subatributos al ponerlo como simple hay que eliminar sus atributos
                if (ta.isCompuesto() && !ta.getListaComponentes().isEmpty()) {
			/*	Object[] options = {Lenguaje.getMensaje(Lenguaje.YES),Lenguaje.getMensaje(Lenguaje.NO)};
				int respuesta = JOptionPane.showOptionDialog(
						null,
						Lenguaje.getMensaje(Lenguaje.MODIFY_ATTRIBUTE)+"\""+ta.getNombre()+"\""+
						Lenguaje.getMensaje(Lenguaje.DELETE_ATTRIBUTES_WARNING3)+"\n" +
						Lenguaje.getMensaje(Lenguaje.WISH_CONTINUE),
						Lenguaje.getMensaje(Lenguaje.DBCASE),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[1]);*/
                    int respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.MODIFY_ATTRIBUTE) + "\"" + ta.getNombre() + "\"" +
                                    Lenguaje.text(Lenguaje.DELETE_ATTRIBUTES_WARNING3) + "\n" +
                                    Lenguaje.text(Lenguaje.WISH_CONTINUE),
                            Lenguaje.text(Lenguaje.DBCASE));
                    if (respuesta == 0) {
                        // Eliminamos sus subatributos
                        Vector lista_atributos = ta.getListaComponentes();
                        int cont = 0;
                        TransferAtributo tah = new TransferAtributo(this);
                        while (cont < lista_atributos.size()) {
                            String idAtributo = (String) lista_atributos.get(cont);
                            tah.setIdAtributo(Integer.valueOf(idAtributo));
                            this.getTheServiciosAtributos().eliminarAtributo(tah);
                            cont++;
                        }
                        // Modificamos el atributo
                        ta.getListaComponentes().clear();
                        this.getTheServiciosAtributos().editarCompuestoAtributo(ta);
                    }
                }
                // Si no es compuesto o es compuesto pero no tiene subatributos
                else {
                    this.getTheServiciosAtributos().editarCompuestoAtributo(ta);
                }
                break;
            }
            case PanelDiseno_Click_EditarNotNullAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheServiciosAtributos().editarNotNullAtributo(ta);
                break;
            }
            case PanelDiseno_Click_EditarUniqueAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheServiciosAtributos().editarUniqueAtributo(ta);

                this.getTheServiciosEntidades().ListaDeEntidades();
                this.getTheServiciosAtributos().ListaDeAtributos();
                this.getTheServiciosRelaciones().ListaDeRelaciones();
                //modificar la tabla de Uniques de la entidad o la relacion a la que pertenece
                Vector<TransferRelacion> relaciones = this.getTheGUIPrincipal().getListaRelaciones();
                Vector<TransferEntidad> entidades = this.getTheGUIPrincipal().getListaEntidades();
                boolean encontrado = false;
                boolean esEntidad = false;
                TransferEntidad te = null;
                TransferRelacion tr = null;
                int i = 0;
                while (i < entidades.size() && !encontrado) {
                    te = entidades.get(i);
                    if (this.getTheServiciosEntidades().tieneAtributo(te, ta)) {
                        encontrado = true;
                        esEntidad = true;
                    }
                    i++;
                }
                i = 0;
                while (i < relaciones.size() && !encontrado) {
                    tr = relaciones.get(i);
                    if (this.getTheServiciosRelaciones().tieneAtributo(tr, ta)) {
                        encontrado = true;
                    }
                    i++;
                }
                if (encontrado) {
                    if (esEntidad) {
                        Vector v = new Vector();
                        v.add(te);
                        v.add(ta);
                        this.getTheServiciosEntidades().setUniqueUnitario(v);
                    } else {//esRelacion
                        Vector v = new Vector();
                        v.add(tr);
                        v.add(ta);
                        this.getTheServiciosRelaciones().setUniqueUnitario(v);
                    }
                }

                break;
            }
            case PanelDiseno_Click_EliminarReferenciasUniqueAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheServiciosAtributos().editarUniqueAtributo(ta);

                this.getTheServiciosEntidades().ListaDeEntidades();
                this.getTheServiciosAtributos().ListaDeAtributos();
                this.getTheServiciosRelaciones().ListaDeRelaciones();
                //modificar la tabla de Uniques de la entidad o la relacion a la que pertenece
                Vector<TransferRelacion> relaciones = this.getTheGUIPrincipal().getListaRelaciones();
                Vector<TransferEntidad> entidades = this.getTheGUIPrincipal().getListaEntidades();
                boolean encontrado = false;
                boolean esEntidad = false;
                TransferEntidad te = null;
                TransferRelacion tr = null;
                int i = 0;
                while (i < entidades.size() && !encontrado) {
                    te = entidades.get(i);
                    if (this.getTheServiciosEntidades().tieneAtributo(te, ta)) {
                        encontrado = true;
                        esEntidad = true;
                    }
                    i++;
                }
                i = 0;
                while (i < relaciones.size() && !encontrado) {
                    tr = relaciones.get(i);
                    if (this.getTheServiciosRelaciones().tieneAtributo(tr, ta)) {
                        encontrado = true;
                    }
                    i++;
                }
                if (encontrado) {
                    if (esEntidad) {
                        Vector v = new Vector();
                        v.add(te);
                        v.add(ta);
                        this.getTheServiciosEntidades().eliminarReferenciasUnitario(v);
                    } else {//esRelacion
                        Vector v = new Vector();
                        v.add(tr);
                        v.add(ta);
                        this.getTheServiciosRelaciones().eliminarReferenciasUnitario(v);
                    }
                }

                break;
            }
            case PanelDiseno_Click_ModificarUniqueAtributo: {
                Vector v1 = (Vector) datos;
                TransferAtributo ta = (TransferAtributo) v1.get(0);
                String antiguoNombre = (String) v1.get(1);

                this.getTheServiciosAtributos().editarUniqueAtributo(ta);

                this.getTheServiciosEntidades().ListaDeEntidades();
                this.getTheServiciosAtributos().ListaDeAtributos();
                this.getTheServiciosRelaciones().ListaDeRelaciones();
                //modificar la tabla de Uniques de la entidad o la relacion a la que pertenece
                Vector<TransferRelacion> relaciones = this.getTheGUIPrincipal().getListaRelaciones();
                Vector<TransferEntidad> entidades = this.getTheGUIPrincipal().getListaEntidades();
                boolean encontrado = false;
                boolean esEntidad = false;
                TransferEntidad te = null;
                TransferRelacion tr = null;
                int i = 0;
                while (i < entidades.size() && !encontrado) {
                    te = entidades.get(i);
                    if (this.getTheServiciosEntidades().tieneAtributo(te, ta)) {
                        encontrado = true;
                        esEntidad = true;
                    }
                    i++;
                }
                i = 0;
                while (i < relaciones.size() && !encontrado) {
                    tr = relaciones.get(i);
                    if (this.getTheServiciosRelaciones().tieneAtributo(tr, ta)) {
                        encontrado = true;
                    }
                    i++;
                }
                if (encontrado) {
                    if (esEntidad) {
                        Vector v = new Vector();
                        v.add(te);
                        v.add(ta);
                        v.add(antiguoNombre);
                        this.getTheServiciosEntidades().renombraUnique(v);
                    } else {//esRelacion
                        Vector v = new Vector();
                        v.add(tr);
                        v.add(ta);
                        v.add(antiguoNombre);
                        this.getTheServiciosRelaciones().renombraUnique(v);
                    }
                }

                break;
            }
            case PanelDiseno_Click_EditarMultivaloradoAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheServiciosAtributos().editarMultivaloradoAtributo(ta);
                break;
            }
            case PanelDiseno_Click_AnadirSubAtributoAAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheGUIAnadirSubAtributoAtributo().setAtributo(ta);
                this.getTheGUIAnadirSubAtributoAtributo().setActiva();
                break;
            }
            case PanelDiseno_Click_EditarClavePrimariaAtributo: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                this.getTheServiciosAtributos().editarClavePrimariaAtributo(v);
                break;
            }
            case PanelDiseno_MoverEntidad: {
                TransferEntidad te = (TransferEntidad) datos;
                this.getTheServiciosEntidades().moverPosicionEntidad(te);
                break;
            }
            case PanelDiseno_MoverAtributo: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheServiciosAtributos().moverPosicionAtributo(ta);
                break;
            }
            case PanelDiseno_MoverRelacion: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheServiciosRelaciones().moverPosicionRelacion(tr);
                break;
            }
            case PanelDiseno_Click_AnadirAtributoRelacion: {
                TransferRelacion tr = (TransferRelacion) datos;
                // Si es una relacion debil no se pueden anadir atributos
                if (tr.getTipo().equals("Debil")) {
                    JOptionPane.showMessageDialog(
                            null, "ERROR.\n" +
                                    Lenguaje.text(Lenguaje.NO_ATTRIBUTES_RELATION) + ".\n" +
                                    Lenguaje.text(Lenguaje.THE_RELATION) + "\"" + tr.getNombre() + "\"" + Lenguaje.text(Lenguaje.IS_WEAK) + ".\n",
                            Lenguaje.text(Lenguaje.ADD_ENTITY_RELATION),
                            JOptionPane.PLAIN_MESSAGE);
                    return;
                }

                this.getTheGUIAnadirAtributoRelacion().setRelacion(tr);
                this.getTheGUIAnadirAtributoRelacion().setActiva();
                break;
            }
            /*
             * Relaciones IsA
             */
            case PanelDiseno_Click_EstablecerEntidadPadre: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIEstablecerEntidadPadre().setRelacion(tr);
                this.getTheGUIEstablecerEntidadPadre().setActiva();
                break;
            }
            case PanelDiseno_Click_QuitarEntidadPadre: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIQuitarEntidadPadre().setRelacion(tr);
                this.getTheGUIQuitarEntidadPadre().setActiva();
                break;
            }
            case PanelDiseno_Click_AnadirEntidadHija: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIAnadirEntidadHija().setRelacion(tr);
                this.getTheGUIAnadirEntidadHija().setActiva();
                break;
            }
            case PanelDiseno_Click_QuitarEntidadHija: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIQuitarEntidadHija().setRelacion(tr);
                this.getTheGUIQuitarEntidadHija().setActiva();
                break;
            }
            case PanelDiseno_Click_EliminarRelacionIsA: {
                Vector<Object> v = (Vector<Object>) datos;
                TransferRelacion tr = (TransferRelacion) v.get(0);
                boolean preguntar = (Boolean) v.get(1);
                int respuesta = 0;
                if (preguntar) {
                    respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.ISA_RELATION_DELETE) + "\n" +
                                    Lenguaje.text(Lenguaje.WISH_CONTINUE),
                            Lenguaje.text(Lenguaje.DELETE_ISA_RELATION));
                }
                if (respuesta == 0)
                    this.getTheServiciosRelaciones().eliminarRelacionIsA(tr);
                break;
            }
            case PanelDiseno_Click_InsertarRelacionIsA: {
                Point2D punto = (Point2D) datos;
                TransferRelacion tr = new TransferRelacion();
                tr.setPosicion(punto);
                this.getTheServiciosRelaciones().anadirRelacionIsA(tr);
                break;
            }
            /*
             * Relaciones normales
             */
            case PanelDiseno_Click_EliminarRelacionNormal: {
                Vector<Object> v = (Vector<Object>) datos;
                TransferRelacion tr = (TransferRelacion) v.get(0);
                boolean preguntar = (Boolean) v.get(1);
                int respuesta = 0;
                if (preguntar) {
                    String tieneAtributos = "";
                    if (!tr.getListaAtributos().isEmpty())
                        tieneAtributos = Lenguaje.text(Lenguaje.DELETE_ATTRIBUTES_WARNING) + "\n";
                    String tieneEntidad = "";
                    //Informar de que también se va a eliminar la entidad débil asociada
                    if (tr.getTipo().equals("Debil"))
                        tieneEntidad = Lenguaje.text(Lenguaje.WARNING_DELETE_WEAK_ENTITY) + "\n";
                    respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.THE_RELATION) + " \"" + tr.getNombre() + "\" " +
                                    Lenguaje.text(Lenguaje.REMOVE_FROM_SYSTEM) + "\n" +
                                    tieneAtributos + tieneEntidad +
                                    Lenguaje.text(Lenguaje.WISH_CONTINUE),
                            Lenguaje.text(Lenguaje.DELETE_RELATION));
                }
                //Si se desea eliminar la relación
                if (respuesta == 0) {
                    // Eliminamos sus atributos
                    Vector lista_atributos = tr.getListaAtributos();
                    int conta = 0;
                    TransferAtributo ta = new TransferAtributo(this);
                    while (conta < lista_atributos.size()) {
                        String idAtributo = (String) lista_atributos.get(conta);
                        ta.setIdAtributo(Integer.valueOf(idAtributo));
                        this.getTheServiciosAtributos().eliminarAtributo(ta);
                        conta++;
                    }
                    //Se elimina también la entidad débil asociada
                    if (tr.getTipo().equals("Debil")) {
                        Vector lista_entidades = tr.getListaEntidadesYAridades();
                        int cont = 0;
                        TransferEntidad te = new TransferEntidad();
                        while (cont < lista_entidades.size()) {
                            EntidadYAridad eya = (EntidadYAridad) (tr.getListaEntidadesYAridades().get(cont));
                            int idEntidad = eya.getEntidad();
                            te.setIdEntidad(idEntidad);
                            //Tengo que rellenar los atributos de te
                            Vector<TransferEntidad> auxiliar = (this.theGUIQuitarEntidadARelacion.getListaEntidades());
                            boolean encontrado = false;
                            int i = 0;
                            while ((!encontrado) && (i < auxiliar.size())) {
                                if (auxiliar.get(i).getIdEntidad() == idEntidad) {
                                    encontrado = true;
                                    te.setListaAtributos(auxiliar.get(i).getListaAtributos());
                                } else
                                    i++;
                            }
                            //Elimino también la entidad débil
                            if (this.getTheServiciosEntidades().esDebil(idEntidad)) {
                                //Esto es para borrar los atributos de la entidad débil y la propia entidad débil
                                Vector<Object> vAux = new Vector<Object>();
                                vAux.add(te);
                                vAux.add(false);
                                this.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarEntidad, vAux);
                            }
                            cont++;
                        }
                    }
                    // Eliminamos la relacion
                    this.getTheServiciosRelaciones().eliminarRelacionNormal(tr);
                }
                break;
            }
            case PanelDiseno_Click_AnadirEntidadARelacion: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIAnadirEntidadARelacion().setRelacion(tr);
                this.getTheGUIAnadirEntidadARelacion().setActiva();
                break;
            }
            case PanelDiseno_Click_QuitarEntidadARelacion: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIQuitarEntidadARelacion().setRelacion(tr);
                this.getTheGUIQuitarEntidadARelacion().setActiva();
                break;
            }
            case PanelDiseno_Click_EditarCardinalidadEntidad: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIEditarCardinalidadEntidad().setRelacion(tr);
                this.getTheGUIEditarCardinalidadEntidad().setActiva();
                break;
            }
            /*
             * Dominios
             */
            case PanelDiseno_Click_CrearDominio: {
                this.getTheGUIInsertarDominio().setActiva();
                break;
            }
            case PanelDiseno_Click_RenombrarDominio: {
                TransferDominio td = (TransferDominio) datos;
                this.getTheGUIRenombrarDominio().setDominio(td);
                this.getTheGUIRenombrarDominio().setActiva();
                break;
            }
            case PanelDiseno_Click_EliminarDominio: {
                TransferDominio td = (TransferDominio) datos;
                int respuesta = panelOpciones.setActiva(
                        Lenguaje.text(Lenguaje.DOMAIN) + " \"" + td.getNombre() + "\" " + Lenguaje.text(Lenguaje.REMOVE_FROM_SYSTEM) + "\n" +
                                Lenguaje.text(Lenguaje.MODIFYING_ATTRIBUTES_WARNING4) + "\n" +
                                Lenguaje.text(Lenguaje.WISH_CONTINUE),
                        Lenguaje.text(Lenguaje.DELETE_DOMAIN));
                if (respuesta == 0) {
                    TipoDominio valorBase = td.getTipoBase();
                    String dominioEliminado = td.getNombre();
                    this.getTheServiciosAtributos().ListaDeAtributos();
                    int cont = 0;
                    TransferAtributo ta = new TransferAtributo(this);
                    while (cont < listaAtributos.size()) {
                        ta = listaAtributos.get(cont);
                        if (ta.getDominio().equals(dominioEliminado)) {
                            Vector<Object> v = new Vector();
                            v.add(ta);
                            String valorB = "";
                            if (valorBase.name().equals("TEXT") || valorBase.name().equals("VARCHAR"))
                                valorB = valorBase + "(20)";
                            else valorB = valorBase.toString();

                            v.add(valorB);

                            if (valorBase.name().equals("TEXT") || valorBase.name().equals("VARCHAR")) v.add("20");
                            this.getTheServiciosAtributos().editarDomnioAtributo(v);
                        }
                        cont++;
                    }
                    // Eliminamos el dominio
                    this.getTheServiciosDominios().eliminarDominio(td);
                }
                break;
            }
            case PanelDiseno_Click_ModificarDominio: {
                TransferDominio td = (TransferDominio) datos;
                this.getTheGUIModificarElementosDominio().setDominio(td);
                this.getTheGUIModificarElementosDominio().setActiva();
                break;
            }
            case PanelDiseno_Click_OrdenarValoresDominio: {
                TransferDominio td = (TransferDominio) datos;
                quicksort((Vector<String>) td.getListaValores());

                Vector<Object> v = new Vector();
                v.add(td);
                v.add(td.getListaValores());
                this.getTheServiciosDominios().modificarElementosDominio(v);
                break;
            }

            /*
             * Panel de informacion
             */
            case PanelDiseno_MostrarDatosEnPanelDeInformacion: {
                JTree arbol = (JTree) datos;
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_MostrarDatosEnPanelDeInformacion, arbol);
                break;
            }
            case PanelDiseno_ActualizarDatosEnTablaDeVolumenes: {
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_ActualizarDatosEnTablaDeVolumenes, datos);
                break;
            }
            case PanelDiseno_MostrarDatosEnTablaDeVolumenes: {
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_MostrarDatosEnTablaDeVolumenes, datos);
                break;
            }
            default:
                break;
        } // switch
    }

    // Mensajes que manda la GUIPrincipal al Controlador
    @SuppressWarnings("static-access")
    public void mensajeDesde_GUIPrincipal(TC mensaje, Object datos) {
        switch (mensaje) {
            case GUIPrincipal_ObtenDBMSDisponibles: {
                Vector<TransferConexion> vtc =
                        this.getTheServiciosSistema().obtenerTiposDeConexion();
                this.getTheGUIPrincipal().setListaConexiones(vtc);
                break;
            }
            case GUIPrincipal_ActualizameLaListaDeEntidades: {
                this.getTheServiciosEntidades().ListaDeEntidades();
                break;
            }
            case GUIPrincipal_ActualizameLaListaDeAtributos: {
                this.getTheServiciosAtributos().ListaDeAtributos();
                break;
            }
            case GUIPrincipal_ActualizameLaListaDeRelaciones: {
                this.getTheServiciosRelaciones().ListaDeRelaciones();
                break;
            }
            case GUIPrincipal_ActualizameLaListaDeDominios: {
                this.getTheServiciosDominios().ListaDeDominios();
                break;
            }
            case GUI_Principal_ABOUT: {
                about.setActiva(true);
                break;
            }
            case GUI_Principal_IniciaFrames: {
                iniciaFrames();
                break;
            }
            /*
             * Barra de menus
             */
            case GUI_Principal_Click_Submenu_Salir: {
                if (cambios) {
                    int respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.WISH_SAVE),
                            Lenguaje.text(Lenguaje.DBCASE), true);
                    if (respuesta == 1) guardarYSalir();
                    else if (respuesta == 0) {
                        theGUIWorkSpace = new GUI_SaveAs();
                        theGUIWorkSpace.setControlador(this);
                        if (this.getTheGUIWorkSpace().setActiva(2)) salir();
                    }
                } else guardarYSalir();
                break;
            }
            case GUI_Principal_NULLATTR: {
                setNullAttrs(!nullAttrs);
                this.getTheGUIPrincipal().loadInfo();
                break;
            }
            case GUI_Principal_Click_Imprimir: {
                this.getTheGUIPrincipal().imprimir();
                break;
            }
            case GUI_Principal_Click_ModoProgramador: {
                this.getTheGUIPrincipal().modoProgramador();
                break;
            }
            case GUI_Principal_Click_ModoDiseno: {
                this.getTheGUIPrincipal().modoDiseno();
                break;
            }
            case GUI_Principal_Click_ModoVerTodo: {
                this.getTheGUIPrincipal().modoVerTodo();
                break;
            }
            case GUI_Principal_Click_Salir: {
                if (cambios) {
                    int respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.WISH_SAVE),
                            Lenguaje.text(Lenguaje.DBCASE), true);
                    if (respuesta == 1) guardarYSalir();
                    else if (respuesta == 0) {
                        theGUIWorkSpace = new GUI_SaveAs();
                        theGUIWorkSpace.setControlador(this);
                        if (this.getTheGUIWorkSpace().setActiva(2)) guardarYSalir();
                    }
                } else guardarYSalir();
                break;
            }
            case GUI_Principal_Click_Submenu_Abrir: {
                if (cambios) {
                    int respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.WISH_SAVE),
                            Lenguaje.text(Lenguaje.DBCASE), true);
                    if (respuesta == 1) {
                        theGUIWorkSpace = new GUI_SaveAs();
                        theGUIWorkSpace.setControlador(this);
                        this.getTheGUIWorkSpace().setActiva(1);
                    } else if (respuesta == 0) {
                        theGUIWorkSpace = new GUI_SaveAs();
                        theGUIWorkSpace.setControlador(this);
                        boolean guardado = this.getTheGUIWorkSpace().setActiva(2);
                        if (guardado) {
                            theGUIWorkSpace = new GUI_SaveAs();
                            theGUIWorkSpace.setControlador(this);
                            this.getTheGUIWorkSpace().setActiva(1);
                        }
                    }
                } else {
                    theGUIWorkSpace = new GUI_SaveAs();
                    theGUIWorkSpace.setControlador(this);
                    this.getTheGUIWorkSpace().setActiva(1);
                }
                break;
            }
            case GUI_Principal_Click_Submenu_Guardar: {
                theGUIWorkSpace = new GUI_SaveAs();
                theGUIWorkSpace.setControlador(this);
                this.getTheGUIWorkSpace().setActiva(2);
                getTheGUIPrincipal().setTitle(getTitle());
                break;
            }
            case GUI_Principal_Click_Submenu_GuardarComo: {
                theGUIWorkSpace = new GUI_SaveAs();
                theGUIWorkSpace.setControlador(this);
                this.getTheGUIWorkSpace().setActiva(3);
                getTheGUIPrincipal().setTitle(getTitle());
                break;
            }
            case GUI_Principal_Click_Submenu_Nuevo: {
                if (cambios) {
                    int respuesta = panelOpciones.setActiva(
                            Lenguaje.text(Lenguaje.WISH_SAVE),
                            Lenguaje.text(Lenguaje.DBCASE), true);
                    if (respuesta == 1) {
                        filetemp.delete();
                        this.getTheGUIWorkSpace().nuevoTemp();
                        setCambios(false);
                    } else if (respuesta == 0) {
                        theGUIWorkSpace = new GUI_SaveAs();
                        theGUIWorkSpace.setControlador(this);
                        if (this.getTheGUIWorkSpace().setActiva(2)) {
                            filetemp.delete();
                            this.getTheGUIWorkSpace().nuevoTemp();
                            setCambios(false);
                        }
                    }
                } else {
                    filetemp.delete();
                    this.getTheGUIWorkSpace().nuevoTemp();
                    setCambios(false);
                }
                break;
            }
            case GUI_Principal_CambiarLenguaje: {
                // Extraer lenguaje seleccionado
                String lenguaje = (String) datos;

                // Cambiar lenguaje
                Lenguaje.cargaLenguaje(lenguaje);

                /* guardar, "guardado", tempguarda... y todo eso. guardar en un temporal nuevo y luego abrirlo para dejarlo como estuviese*/
                boolean cambios = this.cambios;
                File fileguardar = this.fileguardar;
                try {
                    if (filetemp.exists()) {
                        File guardado = File.createTempFile("dbcase", "xml");
                        FileCopy(filetemp.getAbsolutePath(), guardado.getAbsolutePath());
                        mensajeDesde_GUIWorkSpace(TC.GUI_WorkSpace_Click_Abrir, guardado.getAbsolutePath());
                        guardado.delete();
                    } else this.getTheGUIWorkSpace().nuevoTemp();
                } catch (IOException e) {
                }

                this.fileguardar = fileguardar;
                this.cambios = cambios;

                break;
            }
            case GUI_Principal_CambiarTema: {
                theme.changeTheme((String) datos);
                /* guardar, "guardado", tempguarda... y todo eso. guardar en un temporal nuevo y luego abrirlo para dejarlo como estuviese*/
                boolean cambios = this.cambios;
                File fileguardar = this.fileguardar;
                try {
                    if (filetemp.exists()) {
                        File guardado = File.createTempFile("dbcase", "xml");
                        FileCopy(filetemp.getAbsolutePath(), guardado.getAbsolutePath());
                        mensajeDesde_GUIWorkSpace(TC.GUI_WorkSpace_Click_Abrir, guardado.getAbsolutePath());
                        guardado.delete();
                    } else this.getTheGUIWorkSpace().nuevoTemp();
                } catch (IOException e) {
                }

                this.fileguardar = fileguardar;
                this.cambios = cambios;
                break;
            }
            /*
             * Limpiar pantalla
             */
            case GUI_Principal_Click_BotonLimpiarPantalla: {
                this.getTheServiciosSistema().reset();
                break;
            }
            /*
             * Generacion del script SQL
             */
            case GUI_Principal_Click_BotonGenerarModeloRelacional: {
                this.getTheServiciosSistema().generaModeloRelacional();
                break;
            }
            case GUI_Principal_Click_BotonGenerarScriptSQL: {
                TransferConexion tc = (TransferConexion) datos;
                this.getTheServiciosSistema().generaScriptSQL(tc);
                break;
            }
            case GUI_Principal_Click_BotonGenerarArchivoScriptSQL: {
                String texto = (String) datos;
                this.getTheServiciosSistema().exportarCodigo(texto, true);
                break;
            }
            case GUI_Principal_Click_BotonGenerarArchivoModelo: {
                String texto = (String) datos;
                this.getTheServiciosSistema().exportarCodigo(texto, false);
                break;
            }
            case GUI_Principal_Click_BotonEjecutarEnDBMS: {
                TransferConexion tc = (TransferConexion) datos;
                this.getTheGuiSeleccionarConexion().setConexion(tc);
                this.getTheGuiSeleccionarConexion().setActiva();
            }
            default:
                break;
        } // switch
    }

    // Mensajes que le mandan las GUIs al controlador
    public void mensajeDesde_GUI(TC mensaje, Object datos) {
        switch (mensaje) {
            case GUIInsertarEntidad_Click_BotonInsertar: {
                TransferEntidad te = (TransferEntidad) datos;
                this.getTheServiciosEntidades().anadirEntidad(te);
                ActualizaArbol(te);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIInsertarEntidadDebil_Click_BotonInsertar: {
                TransferEntidad te = (TransferEntidad) datos;
                boolean exito = this.getTheServiciosEntidades().SePuedeAnadirEntidad(te);
                this.getTheGUIInsertarEntidad().comprobadaEntidad(exito);
                ActualizaArbol(te);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIInsertarEntidadDebil_Entidad_Relacion_Repetidos: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ENTITY_REL), Lenguaje.text(Lenguaje.ERROR), 0);
                break;
            }
            case GUIRenombrarEntidad_Click_BotonRenombrar: {
                Vector v = (Vector) datos;
                this.getTheServiciosEntidades().renombrarEntidad(v);

                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIAnadirAtributoEntidad_Click_BotonAnadir: {
                Vector<Transfer> vectorTransfers = (Vector<Transfer>) datos;
                this.getTheServiciosEntidades().anadirAtributo(vectorTransfers);
                ActualizaArbol(vectorTransfers.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIAnadirAtributoRelacion_Click_BotonAnadir: {
                Vector<Transfer> vectorTransfers = (Vector<Transfer>) datos;
                this.getTheServiciosRelaciones().anadirAtributo(vectorTransfers);
                ActualizaArbol(vectorTransfers.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIAnadirAtributoEntidad_ActualizameLaListaDeDominios: {
                this.getTheServiciosDominios().ListaDeDominios();
                break;
            }
            case GUIPonerRestriccionesAEntidad_Click_BotonAceptar: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosEntidades().setRestricciones(v);
                ActualizaArbol((Transfer) v.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIPonerRestriccionesARelacion_Click_BotonAceptar: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosRelaciones().setRestricciones(v);
                ActualizaArbol((Transfer) v.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIPonerRestriccionesAAtributo_Click_BotonAceptar: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosAtributos().setRestricciones(v);
                ActualizaArbol((Transfer) v.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIInsertarRestriccionAEntidad_Click_BotonAnadir: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosEntidades().anadirRestriccion(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIQuitarRestriccionAEntidad_Click_BotonAnadir: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosEntidades().quitarRestriccion(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIInsertarRestriccionAAtributo_Click_BotonAnadir: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosAtributos().anadirRestriccion(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIQuitarRestriccionAAtributo_Click_BotonAnadir: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosAtributos().quitarRestriccion(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIInsertarRestriccionARelacion_Click_BotonAnadir: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosRelaciones().anadirRestriccion(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIQuitarRestriccionARelacion_Click_BotonAnadir: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosRelaciones().quitarRestriccion(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIPonerUniquesAEntidad_Click_BotonAceptar: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosEntidades().setUniques(v);
                ActualizaArbol((Transfer) v.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIPonerUniquesARelacion_Click_BotonAceptar: {
                Vector v = (Vector<Transfer>) datos;
                this.getTheServiciosRelaciones().setUniques(v);
                ActualizaArbol((Transfer) v.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }

            case GUIEditarDominioAtributo_ActualizameLaListaDeDominios: {
                this.getTheServiciosDominios().ListaDeDominios();
                break;
            }
            case GUIAnadirSubAtributoAtributo_ActualizameLaListaDeDominios: {
                this.getTheServiciosDominios().ListaDeDominios();
                break;
            }
            case GUIAnadirAtributoRelacion_ActualizameLaListaDeDominios: {
                this.getTheServiciosDominios().ListaDeDominios();
                break;
            }
            case GUIRenombrarAtributo_Click_BotonRenombrar: {
                Vector v = (Vector) datos;
                this.getTheServiciosAtributos().renombrarAtributo(v);

                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIEditarDominioAtributo_Click_BotonEditar: {
                Vector v = (Vector) datos;
                this.getTheServiciosAtributos().editarDomnioAtributo(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIEditarCompuestoAtributo_Click_BotonAceptar: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheServiciosAtributos().editarCompuestoAtributo(ta);
                ActualizaArbol(ta);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIEditarMultivaloradoAtributo_Click_BotonAceptar: {
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheServiciosAtributos().editarMultivaloradoAtributo(ta);
                ActualizaArbol(ta);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIAnadirSubAtributoAtributo_Click_BotonAnadir: {
                Vector<Transfer> vectorTransfers = (Vector<Transfer>) datos;
                this.getTheServiciosAtributos().anadirAtributo(vectorTransfers);
                ActualizaArbol(vectorTransfers.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIEditarClavePrimariaAtributo_ActualizameListaEntidades: {
                this.getTheServiciosEntidades().ListaDeEntidades();
                break;
            }
            case GUIEditarClavePrimariaAtributo_ActualizameListaAtributos: {
                this.getTheServiciosAtributos().ListaDeAtributos();
                break;
            }
            case GUIEditarClavePrimariaAtributo_Click_BotonAceptar: {
                Vector<Transfer> vectorAtributoyEntidad = (Vector<Transfer>) datos;
                this.getTheServiciosAtributos().editarClavePrimariaAtributo(vectorAtributoyEntidad);
                ActualizaArbol(vectorAtributoyEntidad.get(1));
                this.getTheServiciosSistema().reset();
                break;
            }
            /*
             * Relaciones
             */
            case GUIInsertarRelacion_Click_BotonInsertar: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheServiciosRelaciones().anadirRelacion(tr);
                ActualizaArbol(tr);
                this.getTheServiciosSistema().reset();
                break;
            }

            case GUIInsertarRelacionIsA_Click_BotonInsertar: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheServiciosRelaciones().anadirRelacionIsA(tr);
                ActualizaArbol(tr);
                this.getTheServiciosSistema().reset();
                break;
            }

            case GUIInsertarRelacionDebil_Click_BotonInsertar: {
                TransferRelacion tr = (TransferRelacion) datos;
                boolean exito = this.getTheServiciosRelaciones().SePuedeAnadirRelacion(tr);
                this.getTheGUIInsertarEntidad().comprobadaRelacion(exito);
                ActualizaArbol(tr);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIRenombrarRelacion_Click_BotonRenombrar: {
                Vector<Object> v = (Vector) datos;
                TransferRelacion tr = (TransferRelacion) v.get(0);
                String nuevoNombre = (String) v.get(1);
                this.getTheServiciosRelaciones().renombrarRelacion(tr, nuevoNombre);
                ActualizaArbol(tr);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIEstablecerEntidadPadre_ActualizameListaEntidades: {
                this.getTheServiciosEntidades().ListaDeEntidades();
                break;
            }
            case GUIEstablecerEntidadPadre_ClickBotonAceptar: {
                Vector<Transfer> relacionIsAyEntidadPadre = (Vector<Transfer>) datos;
                this.getTheServiciosRelaciones().establecerEntidadPadreEnRelacionIsA(relacionIsAyEntidadPadre);
                ActualizaArbol(relacionIsAyEntidadPadre.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIQuitarEntidadPadre_ClickBotonSi: {
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheServiciosRelaciones().quitarEntidadPadreEnRelacionIsA(tr);
                ActualizaArbol(tr);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIAnadirEntidadHija_ActualizameListaEntidades: {
                this.getTheServiciosEntidades().ListaDeEntidades();
                break;
            }
            case GUIAnadirEntidadHija_ClickBotonAnadir: {
                Vector<Transfer> relacionIsAyEntidadPadre = (Vector<Transfer>) datos;
                this.getTheServiciosRelaciones().anadirEntidadHijaEnRelacionIsA(relacionIsAyEntidadPadre);
                ActualizaArbol(relacionIsAyEntidadPadre.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIQuitarEntidadHija_ActualizameListaEntidades: {
                this.getTheServiciosEntidades().ListaDeEntidades();
                break;
            }
            case GUIQuitarEntidadHija_ClickBotonQuitar: {
                Vector<Transfer> relacionIsAyEntidadPadre = (Vector<Transfer>) datos;
                this.getTheServiciosRelaciones().quitarEntidadHijaEnRelacionIsA(relacionIsAyEntidadPadre);
                ActualizaArbol(relacionIsAyEntidadPadre.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            /*
             * Relaciones normales
             */
            case GUIAnadirEntidadARelacion_ActualizameListaEntidades: {
                this.getTheServiciosEntidades().ListaDeEntidades();
                break;
            }
            case GUIAnadirEntidadARelacion_ClickBotonAnadir: {
                // v tiene: [transferRelacion, idEntidad, inicioRango, finalRango, rol]
                Vector v = (Vector) datos;
                //Vamos a controlar que no se añada una segunda entidad débil a una relación débil
                TransferRelacion tr = (TransferRelacion) v.get(0);
                TransferEntidad te = (TransferEntidad) v.get(1);

                Vector<EntidadYAridad> vectorTupla = tr.getListaEntidadesYAridades();
                boolean relDebil = tr.getTipo().equals("Debil");
                boolean entDebil = te.isDebil();
                boolean relTieneEntDebil = false;
                for (int i = 0; i < vectorTupla.size(); i++) {
                    int entidad = vectorTupla.get(i).getEntidad();
                    if (this.getTheServiciosEntidades().esDebil(entidad)) {
                        relTieneEntDebil = true;
                        break;
                    }
                }

                if (relDebil && entDebil && relTieneEntDebil)
                    JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ALREADY_WEAK_ENTITY), Lenguaje.text(Lenguaje.ERROR), 0);
                else this.getTheServiciosRelaciones().anadirEntidadARelacion(v);

                ActualizaArbol(tr);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIQuitarEntidadARelacion_ActualizameListaEntidades: {
                this.getTheServiciosEntidades().ListaDeEntidades();
                break;
            }
            case GUIQuitarEntidadARelacion_ClickBotonQuitar: {
                //Vector<Transfer> v = (Vector<Transfer>) datos;
                Vector<Object> v = (Vector<Object>) datos;
                this.getTheServiciosRelaciones().quitarEntidadARelacion(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIEditarCardinalidadEntidad_ActualizameListaEntidades: {
                this.getTheServiciosEntidades().ListaDeEntidades();
                break;
            }
            case GUIEditarCardinalidadEntidad_ClickBotonEditar: {
                Vector<Object> v = (Vector<Object>) datos;
                this.getTheServiciosRelaciones().editarAridadEntidad(v);
                ActualizaArbol((Transfer) v.get(0));
                this.getTheServiciosSistema().reset();
                break;
            }
            /*
             * Dominios
             */
            case GUIInsertarDominio_Click_BotonInsertar: {
                TransferDominio td = (TransferDominio) datos;
                this.getTheServiciosDominios().anadirDominio(td);

                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIRenombrarDominio_Click_BotonRenombrar: {
                Vector<Object> v = (Vector) datos;

                TransferDominio td = (TransferDominio) v.get(0);
                String nuevoNombre = (String) v.get(1);
                String dominioRenombrado = td.getNombre();
                this.getTheServiciosAtributos().ListaDeAtributos();
                int cont = 0;
                TransferAtributo ta = new TransferAtributo(this);
                while (cont < listaAtributos.size()) {
                    ta = listaAtributos.get(cont);
                    if (ta.getDominio().equals(dominioRenombrado)) {
                        Vector<Object> vect = new Vector();
                        vect.add(ta);
                        vect.add(nuevoNombre);

                        this.getTheServiciosAtributos().editarDomnioAtributo(vect);
                    }
                    cont++;
                }
                this.getTheServiciosDominios().renombrarDominio(v);
                this.getTheServiciosSistema().reset();
                break;
            }
            case GUIModificarDominio_Click_BotonEditar: {
                Vector<Object> v = (Vector) datos;
                this.getTheServiciosDominios().modificarDominio(v);

                this.getTheServiciosSistema().reset();
                break;
            }

            /*
             * Conectar a DBMS
             */
            case GUIConfigurarConexionDBMS_Click_BotonEjecutar: {
                TransferConexion tc = (TransferConexion) datos;
                this.getTheServiciosSistema().ejecutarScriptEnDBMS(tc, this.theGUIPrincipal.getInstrucciones());
                break;
            }

            case GUIConexionDBMS_PruebaConexion: {
                TransferConexion tc = (TransferConexion) datos;
                this.getTheServiciosSistema().compruebaConexion(tc);
                break;
            }
            default:
                break;
        } // Switch
    }

    // Mensajes que mandan los Servicios de Entidades al Controlador
    public void mensajeDesde_SE(TC mensaje, Object datos) {
        switch (mensaje) {

            /*
             * Listar entidades
             */
            case SE_ListarEntidades_HECHO: {
                this.getTheGUIPrincipal().setListaEntidades((Vector) datos);
                this.getTheGUIEstablecerEntidadPadre().setListaEntidades((Vector) datos);
                this.getTheGUIAnadirEntidadHija().setListaEntidades((Vector) datos);
                this.getTheGUIQuitarEntidadHija().setListaEntidades((Vector) datos);
                this.getTheGUIAnadirEntidadARelacion().setListaEntidades((Vector) datos);
                this.getTheGUIQuitarEntidadARelacion().setListaEntidades((Vector) datos);
                this.getTheGUIEditarCardinalidadEntidad().setListaEntidades((Vector) datos);
                this.getTheGUIInsertarEntidad().setListaEntidades((Vector) datos);
                break;
            }
            /*
             * Insercion de entidades
             */
            case SE_InsertarEntidad_ERROR_NombreDeEntidadEsVacio: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_ENT_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadEsVacio: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_ENT_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_InsertarEntidad_ERROR_NombreDeEntidadYaExiste: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ENT_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadYaExiste: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ENT_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_InsertarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_REL_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_REL_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_InsertarEntidad_ERROR_DAO: {
                this.getTheGUIInsertarEntidad().setInactiva();
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ENTITIES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);
                break;
            }
            case SE_InsertarEntidad_HECHO: {
                this.getTheGUIInsertarEntidad().setInactiva();
                setCambios(true);
                TransferEntidad te = (TransferEntidad) datos;
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_InsertarEntidad, te);
                break;
            }

            /*
             * Renombrar entidades
             */
            case SE_RenombrarEntidad_ERROR_NombreDeEntidadEsVacio: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_ENT_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_RenombrarEntidad_ERROR_NombreDeEntidadYaExiste: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ENT_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_RenombrarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_REL_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_RenombrarEntidad_ERROR_DAOEntidades: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ENTITIES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_RenombrarEntidad_ERROR_DAORelaciones: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_RenombrarEntidad_HECHO: {
                Vector v = (Vector) datos;
                TransferEntidad te = (TransferEntidad) v.get(0);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_RenombrarEntidad, te);
                this.getTheGUIRenombrarEntidad().setInactiva();
                break;
            }
            /*
             * Debilitar/Fortalecer una entidad
             */
            case SE_DebilitarEntidad_ERROR_DAOEntidades: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ENTITIES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_DebilitarEntidad_HECHO: {
                TransferEntidad te = (TransferEntidad) datos;
                setCambios(true);
                ActualizaArbol(te);
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_DebilitarEntidad, te);
                break;
            }
            /*
             * Añadir atributo a una relacion
             */
            case SE_AnadirAtributoAEntidad_ERROR_NombreDeAtributoVacio: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_ATTRIB_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_AnadirAtributoAEntidad_ERROR_NombreDeAtributoYaExiste: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ATTRIB_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_AnadirAtributoAEntidad_ERROR_TamanoNoEsEntero: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_SIZE3), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_AnadirAtributoAEntidad_ERROR_TamanoEsNegativo: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_SIZE2), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_AnadirAtributoAEntidad_ERROR_DAOAtributos: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                this.getTheGUIAnadirAtributoRelacion().setInactiva();
                break;
            }
            case SE_AnadirAtributoAEntidad_ERROR_DAOEntidades: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ENTITIES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                this.getTheGUIAnadirAtributoRelacion().setInactiva();
                break;
            }
            case SE_AnadirAtributoAEntidad_HECHO: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                v.get(1);
                setCambios(true);

//			this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirAtributoAEntidad, v);
                //this.getTheGUIAnadirAtributoEntidad().setInactiva();
                break;
            }
            /*
             * Elimimacion de una entidad
             */
            case SE_EliminarEntidad_ERROR_DAOEntidades: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ENTITIES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SE_EliminarEntidad_HECHO: {
                ((Vector) datos).get(0);
                setCambios(true);
                ((Vector) datos).get(1);

                ActualizaArbol(null);
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EliminarEntidad, datos);
                break;
            }
            /*
             * Mover Entidad en el panel de diseno (cambiar la posicion)
             */
            case SE_MoverPosicionEntidad_ERROR_DAOEntidades: {
                TransferEntidad te = (TransferEntidad) datos;
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ENTITIES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_MoverEntidad_ERROR, te);
                break;
            }
            case SE_MoverPosicionEntidad_HECHO: {
                setCambios(true);
                TransferEntidad te = (TransferEntidad) datos;
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_MoverEntidad_HECHO, te);
                break;
            }
            /*
             * Restricciones a entidad
             */
            case SE_AnadirRestriccionAEntidad_HECHO: {
                Vector v = (Vector) datos;
                TransferEntidad te = (TransferEntidad) v.get(0);
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirRestriccionEntidad, te);
                //this.getTheGUIAnadirRestriccionAEntidad().setInactiva();
                break;
            }
            case SE_QuitarRestriccionAEntidad_HECHO: {
                Vector v = (Vector) datos;
                TransferEntidad te = (TransferEntidad) v.get(0);
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_QuitarRestriccionEntidad, te);
                break;
            }
            case SE_setRestriccionesAEntidad_HECHO: {
                Vector v = (Vector) datos;
                TransferEntidad te = (TransferEntidad) v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_setRestriccionesEntidad, te);
                break;
            }
            /*
             * Restricciones a entidad
             */
            case SE_AnadirUniqueAEntidad_HECHO: {
                Vector v = (Vector) datos;
                TransferEntidad te = (TransferEntidad) v.get(0);
                TransferEntidad clon_entidad = te.clonar();
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirUniqueEntidad, clon_entidad);
                //this.getTheGUIAnadirRestriccionAEntidad().setInactiva();
                break;
            }
            case SE_QuitarUniqueAEntidad_HECHO: {
                Vector v = (Vector) datos;
                TransferEntidad te = (TransferEntidad) v.get(0);
                TransferEntidad clon_entidad = te.clonar();
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_QuitarUniqueEntidad, clon_entidad);
                break;
            }
            case SE_setUniquesAEntidad_HECHO: {
                Vector v = (Vector) datos;
                TransferEntidad te = (TransferEntidad) v.get(1);
                TransferEntidad clon_entidad = te.clonar();
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_setUniquesEntidad, clon_entidad);
                break;
            }
            case SE_setUniqueUnitarioAEntidad_HECHO: {
                Vector v = (Vector) datos;
                TransferEntidad te = (TransferEntidad) v.get(0);
                TransferEntidad clon_entidad = te.clonar();
                setCambios(true);
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_setUniqueUnitarioEntidad, clon_entidad);
                break;
            }
            default:
                break;
        } // switch
    }

    // Mensajes que mandan los Servicios de Dominios al Controlador
    public void mensajeDesde_SD(TC mensaje, Object datos) {
        switch (mensaje) {
            /*
             * Listar dominios
             */
            case SD_ListarDominios_HECHO: {
                this.getTheGUIPrincipal().setListaDominios((Vector) datos);
                this.getTheGUIAnadirAtributoEntidad().setListaDominios((Vector) datos);
                this.getTheGUIAnadirAtributo().setListaDominios((Vector) datos);
                this.getTheGUIEditarDominioAtributo().setListaDominios((Vector) datos);
                this.getTheGUIAnadirAtributoRelacion().setListaDominios((Vector) datos);
                this.getTheGUIAnadirSubAtributoAtributo().setListaDominios((Vector) datos);
                break;
            }
            /*
             * Insercion de dominios
             */
            case SD_InsertarDominio_ERROR_NombreDeDominioEsVacio: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_DOM_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_InsertarDominio_ERROR_NombreDeDominioYaExiste: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_DOM_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_InsertarDominio_ERROR_DAO: {
                this.getTheGUIInsertarDominio().setInactiva();
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.DOMAINS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_InsertarDominio_ERROR_ValorNoValido: {
                Vector v = (Vector) datos;
                v.get(0);
                String error = (String) v.get(1);
                JOptionPane.showMessageDialog(null, error, Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_InsertarDominio_HECHO: {
                this.getTheGUIInsertarDominio().setInactiva();
                TransferDominio td = (TransferDominio) datos;
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_InsertarDominio, td);
                break;
            }

            /*
             * Renombrar dominios
             */
            case SD_RenombrarDominio_ERROR_NombreDeDominioEsVacio: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_DOM_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_RenombrarDominio_ERROR_NombreDeDominioYaExiste: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_DOM_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_RenombrarDominio_ERROR_DAODominios: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.DOMAINS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_RenombrarDominio_HECHO: {
                Vector v = (Vector) datos;
                TransferDominio td = (TransferDominio) v.get(0);
                v.get(1);
                v.get(2);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_RenombrarDominio, td);
                this.getTheGUIRenombrarDominio().setInactiva();
                break;
            }
            /*
             * Elimimacion de un dominio
             */
            case SD_EliminarDominio_ERROR_DAODominios: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.DOMAINS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_EliminarDominio_HECHO: {
                setCambios(true);
                TransferDominio td = (TransferDominio) ((Vector) datos).get(0);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EliminarDominio, td);
                break;
            }
            /*
             * Modificar dominios
             */
            case SD_ModificarTipoBaseDominio_HECHO: {
                setCambios(true);
                Vector v = (Vector) datos;
                TransferDominio td = (TransferDominio) v.get(0);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_ModificarTipoBaseDominio, td);
                break;
            }
            case SD_ModificarTipoBaseDominio_ERROR_DAODominios: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_DOM_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_ModificarTipoBaseDominio_ERROR_TipoBaseDominioEsVacio: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_TYPE_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_ModificarElementosDominio_HECHO: {
                setCambios(true);
                Vector v = (Vector) datos;
                TransferDominio td = (TransferDominio) v.get(0);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_ModificarTipoBaseDominio, td);
                this.getTheGUIModificarElementosDominio().setInactiva();
                break;
            }
            case SD_ModificarElementosDominio_ERROR_DAODominios: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_DOM_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_ModificarElementosDominio_ERROR_ElementosDominioEsVacio: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_VALUES), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SD_ModificarElementosDominio_ERROR_ValorNoValido: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_VALUE), Lenguaje.text(Lenguaje.ERROR), 0);
                break;
            }
            default:
                break;
        } // switch
    }

    // Mensajes que mandan los Servicios de Atributos al Controlador
    public void mensajeDesde_SA(TC mensaje, Object datos) {
        switch (mensaje) {

            case SA_ListarAtributos_HECHO: {
                // Ponemos la lista de atributos actualizada a las GUIs que lo necesitan
                this.getTheGUIPrincipal().setListaAtributos((Vector) datos);
                this.setListaAtributos((Vector) datos);
                break;
            }
            /*
             * Eliminacion de atributos
             */
            case SA_EliminarAtributo_ERROR_DAOAtributos: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);
                break;
            }
            case SA_EliminarAtributo_HECHO: {
                setCambios(true);
                Vector<Transfer> vectorAtributoYElemMod = (Vector<Transfer>) datos;
                vectorAtributoYElemMod.get(0);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EliminarAtributo, vectorAtributoYElemMod);
                ActualizaArbol(null);
                break;
            }
            /*
             * Renombrar atributo
             */
            case SA_RenombrarAtributo_ERROR_NombreDeAtributoEsVacio: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_ATTRIB_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_RenombrarAtributo_ERROR_NombreDeAtributoYaExiste: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_SUBATR_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_RenombrarAtributo_ERROR_DAOAtributos: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_RenombrarAtributo_HECHO: {
                setCambios(true);
                Vector v = (Vector) datos;
                TransferAtributo ta = (TransferAtributo) v.get(0);
                String antiguoNombre = (String) v.get(2);

                TransferAtributo clon_atributo = ta.clonar();
                Vector v1 = new Vector();
                v1.add(clon_atributo);
                v1.add(antiguoNombre);
                this.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_ModificarUniqueAtributo, v1);


                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_RenombrarAtributo, ta);
                this.getTheGUIRenombrarAtributo().setInactiva();
                break;
            }
            /*
             * Editar dominio atributo
             */
            case SA_EditarDominioAtributo_ERROR_DAOAtributos: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_EditarDominioAtributo_ERROR_TamanoNoEsEntero: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_SIZE1), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_EditarDominioAtributo_ERROR_TamanoEsNegativo: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_SIZE2), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_EditarDominioAtributo_HECHO: {
                setCambios(true);
                TransferAtributo ta = (TransferAtributo) datos;

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EditarDominioAtributo, ta);
                this.getTheGUIEditarDominioAtributo().setInactiva();

                break;
            }
            /*
             * Editar caracter compuesto de atributo
             */
            case SA_EditarCompuestoAtributo_ERROR_DAOAtributos: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_EditarCompuestoAtributo_HECHO: {
                setCambios(true);
                TransferAtributo ta = (TransferAtributo) datos;
                ta.getNombre();
                ActualizaArbol(ta);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EditarCompuestoAtributo, ta);
                break;
            }
            /*
             * Editar caracter multivalorado de atributo
             */
            case SA_EditarMultivaloradoAtributo_ERROR_DAOAtributos: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_EditarMultivaloradoAtributo_HECHO: {
                setCambios(true);
                TransferAtributo ta = (TransferAtributo) datos;
                ActualizaArbol(ta);
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EditarMultivaloradoAtributo, ta);
                break;
            }

            case SA_EditarNotNullAtributo_ERROR_DAOAtributos: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_EditarNotNullAtributo_HECHO: {
                setCambios(true);
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EditarNotNullAtributo, ta);
                ActualizaArbol(ta);
                break;
            }
            case SA_EditarUniqueAtributo_ERROR_DAOAtributos: {
		/*	TransferAtributo ta = (TransferAtributo) datos;
			//JOptionPane.showMessageDialog(null, Lenguaje.getMensaje(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.getMensaje(Lenguaje.ERROR), 0);
			this.getTheGUIPrincipal().anadeMensajeAreaDeSucesos(
					"ERROR: No se ha podido editar el caracter unique del atributo \""+ ta.getNombre() + "\". " +
			"Se ha producido un error en el acceso al fichero de atributos.");*/
                break;
            }
            case SA_EditarUniqueAtributo_HECHO: {
                setCambios(true);
                TransferAtributo ta = (TransferAtributo) datos;
                ActualizaArbol(ta);
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EditarUniqueAtributo, ta);
                break;
            }

            /*
             * Añadir un subatributo a un atributo
             */
            case SA_AnadirSubAtributoAtributo_ERROR_NombreDeAtributoVacio: {
                Vector v = (Vector) datos;
                v.get(0);

                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_SUBATTR_NAME), Lenguaje.text(Lenguaje.ERROR), 0);
                break;
            }
            case SA_AnadirSubAtributoAtributo_ERROR_NombreDeAtributoYaExiste: {
                Vector v = (Vector) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_SUBATR_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_AnadirSubAtributoAtributo_ERROR_TamanoNoEsEntero: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_SIZE1), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_AnadirSubAtributoAtributo_ERROR_TamanoEsNegativo: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_SIZE2), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_AnadirSubAtributoAtributo_ERROR_DAOAtributosHijo: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);
                break;
            }
            case SA_AnadirSubAtributoAtributo_ERROR_DAOAtributosPadre: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_AnadirSubAtributoAtributo_HECHO: {
                setCambios(true);
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirSubAtributoAAtributo, v);
                this.getTheGUIAnadirSubAtributoAtributo().setInactiva();
                break;
            }
            /*
             * Editar atributo como clave primaria de una entidad
             */
            case SA_EditarClavePrimariaAtributo_ERROR_DAOEntidades: {
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SA_EditarClavePrimariaAtributo_HECHO: {
                setCambios(true);
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(0);
                vt.get(1);
                //this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EditarClavePrimariaAtributo, vt);
                break;
            }
            /*
             * Restricciones a Atributo
             */
            case SA_AnadirRestriccionAAtributo_HECHO: {
                Vector v = (Vector) datos;
                TransferAtributo te = (TransferAtributo) v.get(0);
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirRestriccionAtributo, te);
                //this.getTheGUIAnadirRestriccionAAtributo().setInactiva();
                break;
            }
            case SA_QuitarRestriccionAAtributo_HECHO: {
                Vector v = (Vector) datos;
                TransferAtributo te = (TransferAtributo) v.get(0);
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_QuitarRestriccionAtributo, te);
                break;
            }
            case SA_setRestriccionesAAtributo_HECHO: {
                Vector v = (Vector) datos;
                TransferAtributo te = (TransferAtributo) v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_setRestriccionesAtributo, te);
                break;
            }
            /*
             * Mover Atributo en el panel de diseno (cambiar la posicion)
             */
            case SA_MoverPosicionAtributo_ERROR_DAOAtributos: {
                TransferAtributo ta = (TransferAtributo) datos;
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_MoverAtributo_ERROR, ta);
                break;
            }
            case SA_MoverPosicionAtributo_HECHO: {
                setCambios(true);
                TransferAtributo ta = (TransferAtributo) datos;
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_MoverAtributo_HECHO, ta);
                break;
            }
            default:
                break;
        } // switch
    }

    // Mensajes que mandan los Servicios de Relaciones al Controlador
    public void mensajeDesde_SR(TC mensaje, Object datos) {
        switch (mensaje) {

            case SR_ListarRelaciones_HECHO: {
                this.getTheGUIPrincipal().setListaRelaciones((Vector) datos);
                break;
            }
            /*
             * Insercion de Relaciones
             */
            case SR_InsertarRelacion_ERROR_NombreDeRelacionEsVacio: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_REL_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_InsertarRelacion_ERROR_NombreDeRelacionYaExiste: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_REL_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_InsertarRelacion_ERROR_NombreDeRelacionYaExisteComoEntidad: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ENT_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_InsertarRelacion_ERROR_NombreDelRolYaExiste: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ROL_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }

            case SR_InsertarRelacion_ERROR_NombreDeRolNecesario: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.NECESARY_ROL), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }

            case SR_InsertarRelacion_ERROR_DAORelaciones: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);
                this.getTheGUIInsertarRelacion().setInactiva();

                break;
            }
            case SR_InsertarRelacion_HECHO: {
                setCambios(true);
                //	this.getTheGUIInsertarRelacion().setInactiva();
                TransferRelacion te = (TransferRelacion) datos;
                //	this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_InsertarRelacion, te);
                break;
            }
            /*
             * Eliminacion de una relacion
             */
            case SR_EliminarRelacion_ERROR_DAORelaciones: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }

            /*creo q esta no se usa nunca*/
            case SR_EliminarRelacion_HECHO: {
                setCambios(true);
                TransferRelacion tr = (TransferRelacion) datos;

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EliminarRelacion, tr);

                break;
            }

            // Renombrar relacion
            case SR_RenombrarRelacion_ERROR_NombreDeRelacionEsVacio: {
                Vector v = (Vector) datos;
                v.get(2);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_REL_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_RenombrarRelacion_ERROR_NombreDeRelacionYaExiste: {
                Vector v = (Vector) datos;
                v.get(1);
                v.get(2);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_REL_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_RenombrarRelacion_ERROR_NombreDeRelacionYaExisteComoEntidad: {
                this.getTheGUIRenombrarRelacion().setInactiva();
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ENT_NAME), Lenguaje.text(Lenguaje.ERROR), 0);
                this.getTheGUIRenombrarRelacion().setActiva();


                break;
            }
            case SR_RenombrarRelacion_ERROR_DAORelaciones: {
                Vector v = (Vector) datos;
                v.get(1);
                v.get(2);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_RenombrarRelacion_ERROR_DAOEntidades: {
                Vector v = (Vector) datos;
                v.get(1);
                v.get(2);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ENTITIES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_RenombrarRelacion_HECHO: {
                setCambios(true);
                Vector v = (Vector) datos;
                TransferRelacion tr = (TransferRelacion) v.get(0);
                v.get(1);
                v.get(2);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_RenombrarRelacion, tr);
                this.getTheGUIRenombrarRelacion().setInactiva();
                break;
            }
            /*
             * Debilitar una relacion
             */
            case SR_DebilitarRelacion_ERROR_DAORelaciones: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);
                break;
            }
            case SR_DebilitarRelacion_HECHO: {
                setCambios(true);
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_DebilitarRelacion, tr);
                ActualizaArbol(tr);
                break;
            }
            /*
             * Restricciones a Relacion
             */
            case SR_AnadirRestriccionARelacion_HECHO: {
                Vector v = (Vector) datos;
                TransferRelacion te = (TransferRelacion) v.get(0);
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirRestriccionRelacion, te);
                //this.getTheGUIAnadirRestriccionAAtributo().setInactiva();
                break;
            }
            case SR_QuitarRestriccionARelacion_HECHO: {
                Vector v = (Vector) datos;
                TransferRelacion te = (TransferRelacion) v.get(0);
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_QuitarRestriccionRelacion, te);
                break;
            }
            case SR_setRestriccionesARelacion_HECHO: {
                Vector v = (Vector) datos;
                TransferRelacion te = (TransferRelacion) v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_setRestriccionesRelacion, te);
                break;
            }

            /*
             * Mover Relacion en el panel de diseno (cambiar la posicion)
             */
            case SR_MoverPosicionRelacion_ERROR_DAORelaciones: {
                TransferRelacion tr = (TransferRelacion) datos;
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_MoverRelacion_ERROR, tr);
                break;
            }
            case SR_MoverPosicionRelacion_HECHO: {
                setCambios(true);
                TransferRelacion tr = (TransferRelacion) datos;
                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_MoverRelacion_HECHO, tr);
                break;
            }

            /*
             * Añadir atributo a una relacion
             */
            case SR_AnadirAtributoARelacion_ERROR_NombreDeAtributoVacio: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.EMPTY_ATTRIB_NAME), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirAtributoARelacion_ERROR_NombreDeAtributoYaExiste: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.REPEATED_ATTRIB_NAME_REL), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirAtributoARelacion_ERROR_TamanoNoEsEntero: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_SIZE1), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirAtributoARelacion_ERROR_TamanoEsNegativo: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_SIZE2), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirAtributoARelacion_ERROR_DAOAtributos: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.ATTRIBUTES_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                this.getTheGUIAnadirAtributoRelacion().setInactiva();
                break;
            }
            case SR_AnadirAtributoARelacion_ERROR_DAORelaciones: {
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                this.getTheGUIAnadirAtributoRelacion().setInactiva();
                break;
            }
            case SR_AnadirAtributoARelacion_HECHO: {
                setCambios(true);
                Vector<Transfer> v = (Vector<Transfer>) datos;
                v.get(0);
                v.get(1);

			/*this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirAtributoARelacion, v);
			this.getTheGUIAnadirAtributoRelacion().setInactiva();*/
                break;
            }

            /*
             * Establecer la entidad padre en una relacion IsA
             */
            case SR_EstablecerEntidadPadre_ERROR_DAORelaciones: {
                this.getTheGUIEstablecerEntidadPadre().setInactiva();
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EstablecerEntidadPadre_HECHO: {
                setCambios(true);
//			this.getTheGUIEstablecerEntidadPadre().setInactiva();
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(1);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EstablecerEntidadPadre, vt);
                break;
            }
            /*
             * Quitar la entidad padre en una relacion IsA
             */
            case SR_QuitarEntidadPadre_ERROR_DAORelaciones: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);
                this.getTheGUIQuitarEntidadPadre().setInactiva();

                break;
            }
            case SR_QuitarEntidadPadre_HECHO: {
                setCambios(true);
                this.getTheGUIQuitarEntidadPadre().setInactiva();
                TransferRelacion tr = (TransferRelacion) datos;

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_QuitarEntidadPadre, tr);
                break;
            }
            /*
             * Anadir una entidad hija a una relacion IsA
             */
            case SR_AnadirEntidadHija_ERROR_DAORelaciones: {
                this.getTheGUIEstablecerEntidadPadre().setInactiva();
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirEntidadHija_HECHO: {
                setCambios(true);
                this.getTheGUIAnadirEntidadHija().setInactiva();
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(1);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirEntidadHija, vt);
                break;
            }
            /*
             * Quitar una entidad hija en una relacion IsA
             */
            case SR_QuitarEntidadHija_ERROR_DAORelaciones: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);
                this.getTheGUIQuitarEntidadHija().setInactiva();
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(1);

                break;
            }
            case SR_QuitarEntidadHija_HECHO: {
                setCambios(true);
                this.getTheGUIQuitarEntidadHija().setInactiva();
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(1);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_QuitarEntidadHija, vt);
                break;
            }
            /*
             * Eliminar una relacion IsA
             */
            case SR_EliminarRelacionIsA_ERROR_DAORelaciones: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EliminarRelacionIsA_HECHO: {
                setCambios(true);
                TransferRelacion tr = (TransferRelacion) datos;

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EliminarRelacionIsA, tr);
                ActualizaArbol(null);
                break;
            }
            /*
             * Eliminar una relacion Normal
             */
            case SR_EliminarRelacionNormal_ERROR_DAORelaciones: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EliminarRelacionNormal_HECHO: {
                setCambios(true);
                TransferRelacion tr = (TransferRelacion) datos;

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EliminarRelacionNormal, tr);
                ActualizaArbol(null);
                break;
            }
            /*
             * Insertar una relacion IsA
             */
            case SR_InsertarRelacionIsA_ERROR_DAORelaciones: {
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_InsertarRelacionIsA_HECHO: {
                setCambios(true);
                TransferRelacion tr = (TransferRelacion) datos;

                //this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_InsertarRelacionIsA, tr);
                ActualizaArbol(tr);
                break;
            }
            /*
             * Anadir una entidad a una relacion normal
             */
            case SR_AnadirEntidadARelacion_ERROR_InicioNoEsEnteroOn: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY1), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirEntidadARelacion_ERROR_InicioEsNegativo: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY2), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirEntidadARelacion_ERROR_FinalNoEsEnteroOn: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY3), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirEntidadARelacion_ERROR_FinalEsNegativo: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY4), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirEntidadARelacion_ERROR_InicioMayorQueFinal: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY5), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirEntidadARelacion_ERROR_DAORelaciones: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_AnadirEntidadARelacion_HECHO: {
                setCambios(true);
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                v.get(2);
                v.get(3);

                //this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirEntidadARelacion, v);
                //this.getTheGUIAnadirEntidadARelacion().setInactiva();
                break;
            }
            /*
             * Quitar una entidad en una relacion Normal
             */
            case SR_QuitarEntidadARelacion_ERROR_DAORelaciones: {
                this.getTheGUIQuitarEntidadARelacion().setInactiva();
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(0);
                vt.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_QuitarEntidadARelacion_HECHO: {
                setCambios(true);
                this.getTheGUIQuitarEntidadARelacion().setInactiva();
                Vector<Transfer> vt = (Vector<Transfer>) datos;
                vt.get(0);
                vt.get(1);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_QuitarEntidadARelacion, vt);
                break;
            }
            /*
             * Editar la aridad de una entidad en una relacion
             */
            case SR_EditarCardinalidadEntidad_ERROR_InicioNoEsEnteroOn: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY1), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EditarCardinalidadEntidad_ERROR_InicioEsNegativo: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY2), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EditarCardinalidadEntidad_ERROR_FinalNoEsEnteroOn: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY3), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EditarCardinalidadEntidad_ERROR_FinalEsNegativo: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY4), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EditarCardinalidadEntidad_ERROR_InicioMayorQueFinal: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.INCORRECT_CARDINALITY5), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EditarCardinalidadEntidad_ERROR_DAORelaciones: {
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.RELATIONS_FILE_ERROR), Lenguaje.text(Lenguaje.ERROR), 0);

                break;
            }
            case SR_EditarCardinalidadEntidad_HECHO: {
                setCambios(true);
                Vector v = (Vector) datos;
                v.get(0);
                v.get(1);
                v.get(2);
                v.get(3);
                v.get(4);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_EditarCardinalidadEntidad, v);
                this.getTheGUIEditarCardinalidadEntidad().setInactiva();
                break;

            }

            case SR_AridadEntidadUnoUno_HECHO: {
                setCambios(true);
                Vector v = (Vector) datos;

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_CardinalidadUnoUno, v);
                break;
            } // switch
            case SR_AnadirUniqueARelacion_HECHO: {
                Vector v = (Vector) datos;
                TransferRelacion tr = (TransferRelacion) v.get(0);
                TransferRelacion clon_relacion = tr.clonar();
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_AnadirUniqueRelacion, clon_relacion);
                //this.getTheGUIAnadirRestriccionAEntidad().setInactiva();
                break;
            }
            case SR_QuitarUniqueARelacion_HECHO: {
                Vector v = (Vector) datos;
                TransferRelacion tr = (TransferRelacion) v.get(0);
                TransferRelacion clon_relacion = tr.clonar();
                v.get(1);
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_QuitarUniqueRelacion, clon_relacion);
                break;
            }

            case SR_setUniquesARelacion_HECHO: {
                Vector v = (Vector) datos;
                TransferRelacion tr = (TransferRelacion) v.get(1);
                TransferRelacion clon_relacion = tr.clonar();
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_setUniquesRelacion, clon_relacion);
                break;
            }
            case SR_setUniqueUnitarioARelacion_HECHO: {
                Vector v = (Vector) datos;
                TransferRelacion tr = (TransferRelacion) v.get(0);
                TransferRelacion clon_relacion = tr.clonar();
                setCambios(true);

                this.getTheGUIPrincipal().mensajesDesde_Controlador(TC.Controlador_setUniqueUnitarioRelacion, clon_relacion);
                break;
            }
            default:
                break;
        }
    }

    // Mensajes que mandan los Servicios del Sistema al Controlador
    @SuppressWarnings("incomplete-switch")
    public void mensajeDesde_SS(TC mensaje, Object datos) {
        switch (mensaje) {
            case SS_ValidacionM: {
                String info = (String) datos;
                this.getTheGUIPrincipal().escribeEnModelo(info);
                break;
            }
            case SS_ValidacionC: {
                String info = (String) datos;
                this.getTheGUIPrincipal().escribeEnCodigo(info);
                break;
            }
            case SS_GeneracionScriptSQL: {
                String info = (String) datos;
                this.getTheGUIPrincipal().escribeEnCodigo(info);
                this.getTheGUIPrincipal().setScriptGeneradoCorrectamente(true);
                break;
            }
            case SS_GeneracionArchivoScriptSQL: {
                String info = (String) datos;
                this.getTheGUIPrincipal().escribeEnCodigo(info);
                break;
            }
            case SS_GeneracionModeloRelacional: {
                String info = (String) datos;
                this.getTheGUIPrincipal().escribeEnModelo(info);
                break;
            }
        }// switch
    }

    // Utilidades
    private static void quicksort(Vector<String> a) {
        quicksort(a, 0, a.size() - 1);
    }

    // quicksort a[left] to a[right]
    private static void quicksort(Vector<String> a, int left, int right) {
        if (right <= left)
            return;
        int i = partition(a, left, right);
        quicksort(a, left, i - 1);
        quicksort(a, i + 1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(Vector<String> a, int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while ((a.get(++i).compareToIgnoreCase(a.get(right)) < 0)) // find item on left to swap
                ; // a[right] acts as sentinel
            while ((a.get(right).compareToIgnoreCase(a.get(--j)) < 0)) // find item on right to swap
                if (j == left)
                    break; // don't go out-of-bounds
            if (i >= j)
                break; // check if pointers cross
            exch(a, i, j); // swap two elements into place
        }
        exch(a, i, right); // swap with partition element
        return i;
    }

    private static void exch(Vector<String> a, int i, int j) {
        // exchanges++;
        String swap = a.get(i);
        a.set(i, a.get(j));
        a.set(j, swap);
    }

    public static void FileCopy(String sourceFile, String destinationFile) {
        try {
            File inFile = new File(sourceFile);
            File outFile = new File(destinationFile);

            FileInputStream in = new FileInputStream(inFile);
            FileOutputStream out = new FileOutputStream(outFile);

            int c;
            while ((c = in.read()) != -1)
                out.write(c);
            in.close();
            out.close();
        } catch (IOException e) {
            System.err.println("Hubo un error de entrada/salida");
        }
    }

    private void guardarConf() {
        String ruta = "";
        if (fileguardar != null && fileguardar.exists())
            ruta = fileguardar.getPath();
        ConfiguradorInicial conf = new ConfiguradorInicial(Lenguaje.getIdiomaActual(),
                this.getTheGUIPrincipal().getConexionActual().getRuta(), ruta, theme.getThemeName(),
                this.getTheGUIPrincipal().getPanelsMode(), nullAttrs);
        conf.guardarFicheroCofiguracion();
    }

    private void guardarYSalir() {
        guardarConf();
        salir();
    }

    private void salir() {
        filetemp.delete();
        System.exit(0);
    }

    private void ActualizaArbol(Transfer t) {
        //	this.getTheGUIPrincipal().getPanelDiseno().EnviaInformacionNodo(t);
    }

    /**
     * Getters y Setters
     */
    private void setListaAtributos(Vector datos) {
        this.listaAtributos = datos;
    }

    public GUI_AnadirAtributoEntidad getTheGUIAnadirAtributoEntidad() {
        return theGUIAnadirAtributoEntidad;
    }

    public void setTheGUIAnadirAtributoEntidad(GUI_AnadirAtributoEntidad theGUIAnadirAtributoEntidad) {
        this.theGUIAnadirAtributoEntidad = theGUIAnadirAtributoEntidad;
    }

    public GUI_InsertarRestriccionAEntidad getTheGUIAnadirRestriccionAEntidad() {
        return theGUIAnadirRestriccionAEntidad;
    }

    public GUI_InsertarRestriccionAAtributo getTheGUIAnadirRestriccionAAtributo() {
        return theGUIAnadirRestriccionAAtributo;
    }

    public GUI_InsertarRestriccionARelacion getTheGUIAnadirRestriccionARelacion() {
        return theGUIAnadirRestriccionARelacion;
    }

    public void setTheGUIAnadirRestriccionAEntidad(GUI_InsertarRestriccionAEntidad theGUIAnadirRestriccionAEntidad) {
        this.theGUIAnadirRestriccionAEntidad = theGUIAnadirRestriccionAEntidad;
    }

    public GUI_AnadirAtributoRelacion getTheGUIAnadirAtributoRelacion() {
        return theGUIAnadirAtributoRelacion;
    }

    public GUI_AnadirAtributo getTheGUIAnadirAtributo() {
        return theGUIAnadirAtributo;
    }

    public void setTheGUIAnadirAtributoRelacion(GUI_AnadirAtributoRelacion theGUIAnadirAtributoRelacion) {
        this.theGUIAnadirAtributoRelacion = theGUIAnadirAtributoRelacion;
    }

    public GUI_AnadirEntidadHija getTheGUIAnadirEntidadHija() {
        return theGUIAnadirEntidadHija;
    }

    public void setTheGUIAnadirEntidadHija(GUI_AnadirEntidadHija theGUIAnadirEntidadHija) {
        this.theGUIAnadirEntidadHija = theGUIAnadirEntidadHija;
    }

    public GUI_AnadirSubAtributoAtributo getTheGUIAnadirSubAtributoAtributo() {
        return theGUIAnadirSubAtributoAtributo;
    }

    public void setTheGUIAnadirSubAtributoAtributo(GUI_AnadirSubAtributoAtributo theGUIAnadirSubAtributoAtributo) {
        this.theGUIAnadirSubAtributoAtributo = theGUIAnadirSubAtributoAtributo;
    }

    public GUI_EditarDominioAtributo getTheGUIEditarDominioAtributo() {
        return theGUIEditarDominioAtributo;
    }

    public void setTheGUIEditarDominioAtributo(GUI_EditarDominioAtributo theGUIEditarDominioAtributo) {
        this.theGUIEditarDominioAtributo = theGUIEditarDominioAtributo;
    }

    public GUI_EstablecerEntidadPadre getTheGUIEstablecerEntidadPadre() {
        return theGUIEstablecerEntidadPadre;
    }

    public void setTheGUIEstablecerEntidadPadre(GUI_EstablecerEntidadPadre theGUIEstablecerEntidadPadre) {
        this.theGUIEstablecerEntidadPadre = theGUIEstablecerEntidadPadre;
    }

    public GUI_InsertarEntidad getTheGUIInsertarEntidad() {
        return theGUIInsertarEntidad;
    }

    public void setTheGUIInsertarEntidad(GUI_InsertarEntidad theGUIInsertarEntidad) {
        this.theGUIInsertarEntidad = theGUIInsertarEntidad;
    }

    public GUI_InsertarRelacion getTheGUIInsertarRelacion() {
        return theGUIInsertarRelacion;
    }

    public GUI_InsertarDominio getTheGUIInsertarDominio() {
        return theGUIInsertarDominio;
    }

    public void setTheGUIInsertarRelacion(GUI_InsertarRelacion theGUIInsertarRelacion) {
        this.theGUIInsertarRelacion = theGUIInsertarRelacion;
    }

    public GUI_Conexion getTheGUIConfigurarConexionDBMS() {
        return theGUIConexion;
    }

    public void setTheGUIConfigurarConexionDBMS(GUI_Conexion theGUIConexion) {
        this.theGUIConexion = theGUIConexion;
    }

    public GUI_SeleccionarConexion getTheGuiSeleccionarConexion() {
        return theGUISeleccionarConexion;
    }

    public void setTheGuiSelecceionarConexion(GUI_SeleccionarConexion selector) {
        this.theGUISeleccionarConexion = selector;
    }

    public GUIPrincipal getTheGUIPrincipal() {
        return theGUIPrincipal;
    }

    public void setTheGUIPrincipal(GUIPrincipal theGUIPrincipal) {
        this.theGUIPrincipal = theGUIPrincipal;
    }

    public GUI_QuitarEntidadHija getTheGUIQuitarEntidadHija() {
        return theGUIQuitarEntidadHija;
    }

    public void setTheGUIQuitarEntidadHija(GUI_QuitarEntidadHija theGUIQuitarEntidadHija) {
        this.theGUIQuitarEntidadHija = theGUIQuitarEntidadHija;
    }

    public GUI_QuitarEntidadPadre getTheGUIQuitarEntidadPadre() {
        return theGUIQuitarEntidadPadre;
    }

    public void setTheGUIQuitarEntidadPadre(GUI_QuitarEntidadPadre theGUIQuitarEntidadPadre) {
        this.theGUIQuitarEntidadPadre = theGUIQuitarEntidadPadre;
    }

    public GUI_RenombrarAtributo getTheGUIRenombrarAtributo() {
        return theGUIRenombrarAtributo;
    }

    public void setTheGUIRenombrarAtributo(GUI_RenombrarAtributo theGUIRenombrarAtributo) {
        this.theGUIRenombrarAtributo = theGUIRenombrarAtributo;
    }

    public GUI_RenombrarEntidad getTheGUIRenombrarEntidad() {
        return theGUIRenombrarEntidad;
    }

    public void setTheGUIRenombrarEntidad(GUI_RenombrarEntidad theGUIRenombrarEntidad) {
        this.theGUIRenombrarEntidad = theGUIRenombrarEntidad;
    }

    public GUI_RenombrarRelacion getTheGUIRenombrarRelacion() {
        return theGUIRenombrarRelacion;
    }

    public void setTheGUIRenombrarRelacion(GUI_RenombrarRelacion theGUIRenombrarRelacion) {
        this.theGUIRenombrarRelacion = theGUIRenombrarRelacion;
    }

    public GUI_RenombrarDominio getTheGUIRenombrarDominio() {
        return theGUIRenombrarDominio;
    }

    public void setTheGUIRenombrarDominio(GUI_RenombrarDominio theGUIRenombrarDominio) {
        this.theGUIRenombrarDominio = theGUIRenombrarDominio;
    }

    public GUI_ModificarDominio getTheGUIModificarElementosDominio() {
        return theGUIModificarElementosDominio;
    }

    public void setTheGUIModificarElementosDominio(GUI_ModificarDominio theGUIModificarElementosDominio) {
        this.theGUIModificarElementosDominio = theGUIModificarElementosDominio;
    }

    public ServiciosAtributos getTheServiciosAtributos() {
        return theServiciosAtributos;
    }

    public void setTheServiciosAtributos(ServiciosAtributos theServiciosAtributos) {
        this.theServiciosAtributos = theServiciosAtributos;
    }

    public ServiciosEntidades getTheServiciosEntidades() {
        return theServiciosEntidades;
    }

    public void setTheServiciosEntidades(ServiciosEntidades theServiciosEntidades) {
        this.theServiciosEntidades = theServiciosEntidades;
    }

    public ServiciosRelaciones getTheServiciosRelaciones() {
        return theServiciosRelaciones;
    }

    public void setTheServiciosRelaciones(ServiciosRelaciones theServiciosRelaciones) {
        this.theServiciosRelaciones = theServiciosRelaciones;
    }

    public ServiciosDominios getTheServiciosDominios() {
        return theServiciosDominios;
    }

    public void setTheServiciosDominios(ServiciosDominios theServiciosDominios) {
        this.theServiciosDominios = theServiciosDominios;
    }

    public GUI_AnadirEntidadARelacion getTheGUIAnadirEntidadARelacion() {
        return theGUIAnadirEntidadARelacion;
    }

    public void setTheGUIAnadirEntidadARelacion(GUI_AnadirEntidadARelacion theGUIAnadirEntidadARelacion) {
        this.theGUIAnadirEntidadARelacion = theGUIAnadirEntidadARelacion;
    }

    public GUI_QuitarEntidadARelacion getTheGUIQuitarEntidadARelacion() {
        return theGUIQuitarEntidadARelacion;
    }

    public void setTheGUIQuitarEntidadARelacion(GUI_QuitarEntidadARelacion theGUIQuitarEntidadARelacion) {
        this.theGUIQuitarEntidadARelacion = theGUIQuitarEntidadARelacion;
    }

    public GUI_EditarCardinalidadEntidad getTheGUIEditarCardinalidadEntidad() {
        return theGUIEditarCardinalidadEntidad;
    }

    public void setTheGUIEditarCardinalidadEntidad(GUI_EditarCardinalidadEntidad theGUIEditarCardinalidadEntidad) {
        this.theGUIEditarCardinalidadEntidad = theGUIEditarCardinalidadEntidad;
    }

    public GeneradorEsquema getTheServiciosSistema() {
        return theServiciosSistema;
    }

    public void setTheServiciosSistema(GeneradorEsquema theServiciosSistema) {
        this.theServiciosSistema = theServiciosSistema;
    }

    public GUI_SaveAs getTheGUIWorkSpace() {
        return theGUIWorkSpace;
    }

    public void setTheGUIWorkSpace(GUI_SaveAs theGUIWorkSpace) {
        this.theGUIWorkSpace = theGUIWorkSpace;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getFiletemp() {
        return filetemp;
    }

    public void setFiletemp(File temp) {
        this.filetemp = temp;
    }

    public File getFileguardar() {
        return fileguardar;
    }

    public void setFileguardar(File guardar) {
        this.fileguardar = guardar;
    }

    public GUI_Pregunta getPanelOpciones() {
        return this.panelOpciones;
    }

    private GUI_TablaUniqueEntidad getTheGUITablaUniqueEntidad() {
        return this.theGUITablaUniqueEntidad;
    }

    private GUI_TablaUniqueRelacion getTheGUITablaUniqueRelacion() {
        return this.theGUITablaUniqueRelacion;
    }

    private void setModoVista(int m) {
        this.modoVista = m;
    }

    private int getModoVista() {
        return modoVista;
    }

    private void setCambios(boolean b) {
        cambios = b;
        //	getTheGUIPrincipal().setTitle(getTitle());
    }

    public boolean isNullAttrs() {
        return nullAttrs;
    }

    public void setNullAttrs(boolean nullAttrs) {
        this.nullAttrs = nullAttrs;
    }

    public String getTitle() {
        try {
            return Lenguaje.text(Lenguaje.DBCASE) + " - " + getFileguardar().getName() + (cambios ? "*" : "");
        } catch (NullPointerException e) {
            return Lenguaje.text(Lenguaje.DBCASE);
        }
    }
}