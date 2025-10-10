package modelo.conectorDBMS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

import vista.lenguaje.Lenguaje;

public class ConectorAccessMdb extends ConectorAccessOdbc {

	protected String _usuario;
	protected String _password;
	
	@Override
	public void abrirConexion(String ruta, String usuario, String password)
			throws SQLException {
		_usuario = usuario;
		_password = password;
	}

	@Override
	public void usarDatabase(String nombre) throws SQLException {
		// Obtener el conector
		String rutaCompleta = "jdbc:odbc:driver={Microsoft Access Driver (*.mdb)};DBQ="
				+ nombre;
		String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			System.err.println(Lenguaje.text(Lenguaje.NO_CONECTOR));
			throw new SQLException(Lenguaje.text(Lenguaje.NO_CONECTOR));
		}

		// Eiminar la base de datos, y crear una nueva
		File f = new File(nombre);
		if (f.exists()) f.delete();
		
		boolean creado = copyfile("/data/void.mdb", nombre);
		if (!creado) {
			JOptionPane.showMessageDialog(null,
				Lenguaje.text(Lenguaje.ERROR)+".\n" +
				"File could not be created. Check permissions and " + 
				"that there is space enough in the hard drive.",
				Lenguaje.text(Lenguaje.DBCASE),
				JOptionPane.PLAIN_MESSAGE);
			return;
		}
		_conexion = DriverManager.getConnection(rutaCompleta, _usuario, _password);
		if (!_conexion.isClosed())
			System.out.println("Conectado correctamente a '" + nombre + "'...");
	}
	
	private static boolean copyfile(String srFile, String dtFile) {
		try {
			//File f1 = new File(srFile);
			File f2 = new File(dtFile);
			Object o = new Object();
			InputStream in = o.getClass().getResourceAsStream(srFile);
			// For Append the file.
			// OutputStream out = new FileOutputStream(f2,true);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
			in.close();
			out.close();
			return true;
		} catch (FileNotFoundException ex) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
}
