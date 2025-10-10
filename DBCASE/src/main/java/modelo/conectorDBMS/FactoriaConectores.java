package modelo.conectorDBMS;

import java.util.Vector;

/**
 * Provee conectores de distintos tipos a la aplicación
 * 
 * @author Denis Cepeda
 */
public final class FactoriaConectores {
	
	public static final int CONECTOR_MYSQL = 0;
	public static final int CONECTOR_MSACCESS_MDB = 1;
	public static final int CONECTOR_MSACCESS_ODBC = 2;
	public static final int CONECTOR_ORACLE = 3;
	
	public static final ConectorDBMS obtenerConector(int tipoConector){
		switch (tipoConector){
		case CONECTOR_MYSQL:
			return new ConectorMySQL();
			
		case CONECTOR_MSACCESS_MDB:
			return new ConectorAccessMdb();
			
		case CONECTOR_MSACCESS_ODBC:
			return new ConectorAccessOdbc();
			
		case CONECTOR_ORACLE:
			return new ConectorOracle();
			
		default:
			return null;
		}
	}
	
	public static final Vector<String> obtenerTodosLosConectores(){
		Vector<String> v = new Vector<String>();
		v.clear();
		v.add(CONECTOR_MYSQL, "MySQL");
		v.add(CONECTOR_MSACCESS_MDB, "Microsoft Access .mdb");
		v.add(CONECTOR_MSACCESS_ODBC, "Microsoft Access via ODBC");
		v.add(CONECTOR_ORACLE, "Oracle");
		
		return v;
	}
}