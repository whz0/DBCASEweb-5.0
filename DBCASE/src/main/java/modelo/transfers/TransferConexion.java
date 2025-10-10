package modelo.transfers;

import java.awt.Shape;
import java.awt.geom.Point2D;

/**
 * Encapsula la información referente a una conexión a base de datos
 * 
 * @author Denis Cepeda
 */
public class TransferConexion extends Transfer {

	// --- --- --- ATRIBUTOS --- --- ---
	private int _tipoConexion;
	private String _ruta;
	private boolean _crearConexion;
	
	private String _database;
	private String _usuario;
	private String _password;
	
	// --- --- --- CONSTRUCTORES --- --- ---
	public TransferConexion(){
		_tipoConexion = -1;
		_ruta = "";
		_crearConexion = false;
		_database = "";
		_usuario = "";
		_password = "";
	}
	
	public TransferConexion(int tipo, String nombre){
		_tipoConexion = tipo;
		_ruta = nombre;
		_crearConexion = false;
		_database = "";
		_usuario = "";
		_password = "";
	}
	
	public TransferConexion(int tipo, String ruta, boolean crear, String database, 
							String usuario, String password){
		_tipoConexion = tipo;
		_ruta = ruta;
		_crearConexion = crear;
		_database = database;
		_usuario = usuario;
		_password = password;
	}
	
	// --- --- --- ACCESORES / MODIFICADORES --- --- ---
	public int getTipoConexion() {
		return _tipoConexion;
	}

	public void setTipoConexion(int conexion) {
		_tipoConexion = conexion;
	}

	public String getRuta() {
		return _ruta;
	}

	public void setRuta(String ruta) {
		_ruta = ruta;
	}
	
	public boolean getCrearConexion() {
		return _crearConexion;
	}

	public void setCrearConexion(boolean crear) {
		_crearConexion = crear;
	}
	public String getDatabase() {
		return _database;
	}

	public void setDatabase(String database) {
		_database = database;
	}

	public String getUsuario() {
		return _usuario;
	}

	public void setUsuario(String _usuario) {
		this._usuario = _usuario;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String _password) {
		this._password = _password;
	}
	
	// --- --- --- MÉTODOS HEREDADOS --- --- ---
	@Override
	public Point2D getPosicion() {
		// No se representa gráficamente
		return null;
	}

	@Override
	public Shape toShape() {
		// No se representa gráficamente
		return null;
	}

	@Override
	public String toString() {
		return null;
	}

	@Override
	public String getNombre() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getVolumen() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFrecuencia() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVolumen(int v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFrecuencia(int f) {
		// TODO Auto-generated method stub
		
	}
}