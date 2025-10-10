package modelo.conectorDBMS;

import java.sql.SQLException;
import modelo.servicios.Enumerado;
import modelo.servicios.Tabla;

/**
 * Clase que determina el comportamiento de los conectores a gestores de
 * bases de datos
 * 
 * @author Denis Cepeda
 */
public abstract class ConectorDBMS {
	/**
	 * Abre la conexión entre la aplicación y el manager de datos
	 *
	 * @param ruta Dirección del gestor de bases de datos
	 * @param usuario Nombre de usuario con el que se desea acceder a la base de datos
	 * @param password Contraseña de acceso a la base de datoss
	 */
	public abstract void abrirConexion (String ruta, String usuario, String password) throws SQLException;
	/**
	 * Ejecuta el comando dado en la base de datos abierta. El comando no genera ninguna
	 * respuesta
	 * 
	 * @param orden Orden a ejecutar. Debe ser de tipo INSERT, UPDATE, CREATE o REMOVE
	 */
	public abstract void ejecutarOrden(String orden) throws SQLException;
	
	/**
	 * Cierra la conexión, si es que ésta estaba abierta
	 */
	public abstract void cerrarConexion() throws SQLException;
	
	/**
	 * Una vez abierta la conexión, comprueba si la base de datos con el nombre 
	 * indicado existe. Si es así, la elimina.
	 * 
	 * Después creará una base de datos con ese nombre para realizar todas las 
	 * instrucciones siguientes.
	 * @param nombre
	 * 
	 * @throws SQLException Si se produce algún error al establecer la
	 * base de datos
	 */
	public abstract void usarDatabase(String nombre) throws SQLException;
	
	/**
	 * Genera el código de creación de una tabla en SQL válido para el conector.
	 * @param t Tabla a traducir. No posee valores ambíguos.
	 * @return El código SQL válido para el conector.
	 */
	public abstract String obtenerCodigoCreacionTabla(Tabla t);
	
	/**
	 * Genera el código de creación de una tabla en SQL válido para el conector, 
	 * con formato HTML.
	 * @param t Tabla a traducir. No posee valores ambíguos.
	 * @return El código SQL válido para el conector, con formato HTML.
	 */
	public abstract String obtenerCodigoCreacionTablaHTML(Tabla t);
	
	/**
	 * Genera el código necesario para añadir a la tabla las claves primarias y
	 * foráneas que tiene definida.
	 * @param t Tabla a traducir. No posee valores ambíguos.
	 * @return El código SQL válido para el conector.
	 */
	public String obtenerCodigoClavesTabla(Tabla t) {
		String codigo=obtenerCodigoClavesTablaHTML(t);
		codigo.replace("<strong>", "");
		codigo.replace("</strong>", "");
		codigo.replace("<p>", "");
		codigo.replace("</p>", "\n");
		return codigo;
	}
	
	/**
	 * Genera el código necesario para añadir a la tabla las claves primarias y
	 * foráneas que tiene definida, con formato HTML
	 * @param t Tabla a traducir. No posee valores ambíguos.
	 * @return El código SQL válido para el conector, con formato HTML
	 */
	public abstract String obtenerCodigoClavesTablaHTML(Tabla t);
	
	/**
	 * Genera el código necesario para crear nuevos dominios
	 * @param e Tipo enumerado que define el nuevo dominio
	 * @return El código SQL válido para el conector
	 */
	public abstract String obtenerCodigoEnumerado(Enumerado e);
	
	/**
	 * Genera el código necesario para crear nuevos dominios, en formato HTML
	 * @param e Tipo enumerado que define el nuevo dominio
	 * @return El código SQL válido para el conector, en formato HTML
	 */
	public abstract String obtenerCodigoEnumeradoHTML(Enumerado e);
	
	/**
	 * Genera el código necesario para añadir restricciones a una tabla
	 * @param t Tabla a traducir. No posee valores ambíguos.
	 * @param restriccion restriccion introducida por el usuario.
	 * @return El código SQL válido para el conector.
	 */
	public  String obtenerCodigoRestriccionTabla(Tabla t) {
		String salida = "";
		for (String r : t.getConstraints())
			salida+= "ALTER TABLE " + t.getNombreTabla()  
					+ " ADD CONSTRAINT "+ t.getNombreConstraint()
					+ " CHECK "
					+ "("+r+");\n";
		for (String u : t.getUniques())
			salida+= "ALTER TABLE " + t.getNombreTabla()  
					+ " ADD CONSTRAINT "+ t.getNombreConstraint()
					+ " UNIQUE "
					+ "("+u+");\n";
		return salida;
	}
	
	/**
	 * Genera el código necesario para añadir restricciones a una tabla con formato HTML
	 * @param t Tabla a traducir. No posee valores ambíguos.
	 * @param restriccion restriccion introducida por el usuario.
	 * @return El código SQL válido para el conector, con formato HTML
	 */
	public String obtenerCodigoRestriccionTablaHTML(Tabla t) {
		String salida = "";
		for (String r : t.getConstraints())
			salida+= "<p><strong>ALTER TABLE</strong> " + t.getNombreTabla()  
					+ " <strong>ADD CONSTRAINT</strong> " + t.getNombreConstraint()
					+ " <strong>CHECK</strong>"
					+ " ("+r.replace("<", "&lt;")+");</p>";
		for (String u : t.getUniques())
			salida+= "<p><strong>ALTER TABLE</strong> " + t.getNombreTabla()  
			+ " <strong>ADD CONSTRAINT</strong> " + t.getNombreConstraint()
			+ " <strong>UNIQUE</strong>"
			+ " ("+u.replace("<", "&lt;")+");</p>";
		return salida;
	}
	
	protected String nombreColumn(String referencia, String tabla) {
			return referencia.substring(0,1) + referencia.substring(tabla.length()+2);
	}
}
