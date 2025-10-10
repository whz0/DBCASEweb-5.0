package controlador;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import modelo.transfers.TransferConexion;

/**
 * Clase encargada de gestionar la configuración por defecto de la aplicación
 */
public class ConfiguradorInicial{
	// --- --- --- ATRIBUTOS --- --- ---
	/**
	 * Lenguaje por defecto
	 */
	protected String _lenguaje;
	/**
	 * Tema por defecto
	 */
	protected String _tema;
	
	/**
	 * Perspectiva por defecto
	 */
	protected int _modoVista;
	/**
	 * Perspectiva por defecto
	 */
	protected boolean _nullAttr;
	/**
	 * Gestor de bases de datos por defecto
	 */
	protected String _gestorBBDD;
	/**
	 * Último proyecto visitado. Si es "", es que el proyecto anterior no se
	 * guardó
	 */
	protected String _ultimoProyecto;
	/**
	 * Indica si existe un fichero .config con la configuración actual
	 */
	protected boolean _existe;
	
	protected Hashtable<String, TransferConexion> _conexiones;

	// --- --- --- CONSTRUCTORES --- --- ---
	/**
	 * Construye un configurador inicial vacío
	 */
	public ConfiguradorInicial(){
		_tema = "";
		_lenguaje = "";
		_gestorBBDD = "";
		_ultimoProyecto = "";
		_modoVista = 0;
		_existe = false;
		_nullAttr = false;
		_conexiones = new Hashtable<String, TransferConexion>();
		_conexiones.clear();
	}
	
	/**
	 * Construye un configurador inicial con los parámetros dados
	 * 
	 * @param lenguaje Lenguaje por defecto
	 * @param gestorBBDD Gestor de bases de datos por defecto
	 * @param ultimoProy Último proyecto abierto
	 */
	public ConfiguradorInicial(String lenguaje, String gestorBBDD, String ultimoProy, String theme, int modoVista, boolean nullAttr){
		_modoVista = modoVista;
		_tema = theme;
		_lenguaje = lenguaje;
		_gestorBBDD = gestorBBDD;
		_ultimoProyecto = ultimoProy;
		_existe = false;
		_nullAttr = nullAttr;
		
		ConfiguradorInicial aux = new ConfiguradorInicial();
		aux.leerFicheroConfiguracion();
		
		if (aux.existeFichero()) _conexiones = aux.obtenConexiones();
		else{
			_conexiones = new Hashtable<String, TransferConexion>();
			_conexiones.clear();
		}
	}
	
	// --- --- --- METODOS --- --- ---
	/**
	 * Crea un nuevo fichero con los datos dados, y sobreescribe el anterior (si hay).
	 * 
	 * @param lenguaje Lenguaje por defecto
	 * @param gestorBBDD Gestor de bases de datos por defecto
	 * @param ultimoProyecto último proyecto abierto
	 */
	public void guardarFicheroCofiguracion(){
		// Borrar el fichero anterior
		File anterior = new File("./dbcase.config");
		if (anterior.exists()){
			anterior.delete();
		}
		
		// Abrir el fichero y escribir en él
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter("./dbcase.config"));
			
			out.write("<?xml version=" + '\"' + "1.0" + '\"' + " encoding="
					+ '\"' + "utf-8" + '\"' + " ?>" + '\n');
			
			// Cabecera config
			out.write("<config ");
			out.write("language=\"" + _lenguaje + "\" ");
			out.write("database=\"" + _gestorBBDD + "\" ");
			out.write("lastProject=\"" + _ultimoProyecto + "\" ");
			out.write("theme=\"" + _tema + "\" ");
			out.write("modoVista=\"" + _modoVista + "\" ");
			out.write("nullAttr=\"" + _nullAttr + "\"");
			out.write(" > \n");
			
			// Conexiones
			Enumeration<String> keys = _conexiones.keys();
			while (keys.hasMoreElements()){
				String nombre = keys.nextElement();
				TransferConexion conexion = _conexiones.get(nombre);
				
				out.write("<connection ");
				out.write("name=\"" + nombre + "\" ");
				out.write("type=\"" + conexion.getTipoConexion() + "\" ");
				out.write("path=\"" + conexion.getRuta() + "\" ");
				out.write("database=\"" + conexion.getDatabase() + "\" ");
				out.write("user=\"" + conexion.getUsuario() + "\" ");
				out.write("password=\"" + conexion.getPassword() + "\" ");
				out.write(" /> \n");
			}
			
			// Fin config
			out.write("</config> \n");
			out.close();
		} catch (IOException e) {
			_existe = false;
			return;
		}
		
		_existe = true;
	}
	
	public void leerFicheroConfiguracion(){
		// Comprobar que existe el fichero
		File anterior = new File("./dbcase.config");
		if (!anterior.exists()){
			_existe = false;
			return;
		}
		
		// Parsear el documento
		Document doc = null;
		DocumentBuilder parser = null;
		try {
			DocumentBuilderFactory factoria = DocumentBuilderFactory.newInstance();
			parser = factoria.newDocumentBuilder();
			doc = parser.parse("./dbcase.config");
			
			// Asignar los valores
			NamedNodeMap atributos = doc.getFirstChild().getAttributes();
			
			_lenguaje = atributos.getNamedItem("language").getNodeValue();
			_gestorBBDD = atributos.getNamedItem("database").getNodeValue();
			_ultimoProyecto = atributos.getNamedItem("lastProject").getNodeValue();
			_tema = atributos.getNamedItem("theme").getNodeValue();
			_modoVista = Integer.parseInt(atributos.getNamedItem("modoVista").getNodeValue());
			_nullAttr = Boolean.parseBoolean(atributos.getNamedItem("nullAttr").getNodeValue());
			// Obtener conexiones
			NodeList connections = doc.getElementsByTagName("connection");
			for (int i=0; i < connections.getLength(); i++){
				Node n = connections.item(i);
				NamedNodeMap atribs = n.getAttributes();
				
				String nombre = atribs.getNamedItem("name").getNodeValue();

				int tipo = Integer.parseInt(
						atribs.getNamedItem("type").getNodeValue());
				String ruta = atribs.getNamedItem("path").getNodeValue();
				String database = atribs.getNamedItem("database").getNodeValue();
				String usuario = atribs.getNamedItem("user").getNodeValue();
				String pass = atribs.getNamedItem("password").getNodeValue();
				
				TransferConexion con = new TransferConexion(tipo, ruta, false, database, usuario, pass);
				
				_conexiones.put(nombre, con);
			}
		}
		catch (Exception e) {
			_existe = false;
			return;
		}
		_existe = true;
		return;
	}
	
	public boolean existeFichero(){
		return _existe;
	}
	
	public boolean estaDisponibleNombreConexion (String nombre) {
		return (_conexiones.get(nombre) == null);
	}
	
	public void anadeConexion(String nombre, TransferConexion tc) {
		_conexiones.put(nombre, tc);
	}
	
	public void quitaConexion(String nombre){
		_conexiones.remove(nombre);
	}
	
	// --- --- --- A C C E S O R E S / M U T A D O R E S --- --- ---
	public String obtenLenguaje(){
		return _lenguaje;
	}
	
	public String obtenTema(){
		return _tema;
	}
	
	public int obtenModoVista(){
		return _modoVista;
	}
	
	public boolean obtenNullAttr(){
		return _nullAttr;
	}
	
	public String obtenGestorBBDD(){
		return _gestorBBDD;
	}
	
	public String obtenUltimoProyecto(){
		return _ultimoProyecto;
	}
	
	public Hashtable<String, TransferConexion> obtenConexiones(){
		return _conexiones;
	}
	
	public TransferConexion obtenConexion(String nombreConexion){
		return _conexiones.get(nombreConexion);
	}
	
	public void ponLenguaje(String lenguaje){
		_lenguaje = lenguaje;
	}
	
	public void ponTema(String tema){
		_tema = tema;
	}
	
	public void ponModoVista(int modoVista){
		_modoVista = modoVista;
	}
	
	public void ponGestorBBDD(String gestorBBDD){
		_gestorBBDD = gestorBBDD;
	}
	
	public void ponUltimoProyecto(String ultimoProyecto){
		_ultimoProyecto = ultimoProyecto;
	}
	
	public void ponConexiones(Hashtable<String, TransferConexion> conexiones){
		_conexiones = conexiones;
	}
}
