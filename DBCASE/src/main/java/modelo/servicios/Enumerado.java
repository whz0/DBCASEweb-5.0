package modelo.servicios;

import java.util.Vector;

import modelo.conectorDBMS.ConectorDBMS;
import modelo.conectorDBMS.FactoriaConectores;
import modelo.transfers.TipoDominio;
import modelo.transfers.TransferConexion;

/**
 * Almacena los datos referentes a un dominio
 */
public class Enumerado {
	// --- --- --- ATRIBUTOS --- --- ---
	
	/**
	 * Tipo
	 */
	private TipoDominio _tipo;
	/**
	 * Nombre del dominio
	 */
	private String _nombre;
	/**
	 * Lista de valores posibles que puede tomar el dominio
	 */
	private Vector<String> _valores;
	/**
	 * Indica la longitud del valor más largo de los posibles
	 */
	private int _longitudVarchar;
	
	
	// --- --- --- CONSTRUCTORES --- --- ---
	public Enumerado(String nombre, TipoDominio tipo){
		_nombre = nombre;
		_tipo = tipo;
		_valores = new Vector<String>(0,1);
		_longitudVarchar = -1;
	}
	
	
	// --- --- --- ACCESORES / MODIFICADORES --- --- ---
	public String getNombre(){
		return _nombre;
	}
	
	public int getNumeroValores(){
		return _valores.size();
	}
	
	public TipoDominio getTipo(){
		return _tipo;
	}
	
	/**
	 * Obtiene el número de caracteres del valor más largo de los posibles.
	 * 
	 * @return La longitud del varchar a declarar
	 */
	public int getLongitud(){
		return _longitudVarchar;
	}
	
	/**
	 * Obtiene el parámetro que se encuentra en la posición dada
	 * 
	 * @param i Índice del parámetro
	 * @return "" si no hay ningún parámetro en ese valor
	 */
	public String getValor(int i){
		if (_valores == null || _valores.size() < i){
			return "";
		}
		return _valores.get(i);
	}
	
	/**
	 * Añade el valor dado a la lista de valores del enumerado. Si éste ya se encuentra, 
	 * no lo añade
	 * @param valor Nuevo valor del enumerado
	 */
	public void anadeValor(String valor){
		if (!_valores.contains(valor)){
			_valores.add(valor);
			
			if (valor.length() > _longitudVarchar) _longitudVarchar = valor.length();
		}
	}

	
	// --- --- --- MÉTODOS --- --- ---
	public String codigoHTMLCreacionDeEnum(TransferConexion conexion) {
		ConectorDBMS traductor = FactoriaConectores.obtenerConector(conexion.getTipoConexion());
		return traductor.obtenerCodigoEnumeradoHTML(this);
	}


	public String codigoEstandarCreacionDeEnum(TransferConexion conexion) {
		ConectorDBMS traductor = FactoriaConectores.obtenerConector(conexion.getTipoConexion());
		return traductor.obtenerCodigoEnumerado(this);
	}
		
}
