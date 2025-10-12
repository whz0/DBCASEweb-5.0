package modelo.conectorDBMS;

import modelo.servicios.Enumerado;
import modelo.servicios.Tabla;
import modelo.transfers.TipoDominio;
import vista.lenguaje.Lenguaje;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

/**
 * Conecta la aplicación a una base de datos de Microsoft Access
 *
 * @author Denis Cepeda
 */
public class ConectorAccessOdbc extends ConectorDBMS {

    protected Connection _conexion;

    @Override
    public void abrirConexion(String ruta, String usuario, String password) throws SQLException {
        String driver = "sun.jdbc.odbc.JdbcOdbcDriver";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println(Lenguaje.text(Lenguaje.NO_CONECTOR));
            throw new SQLException(Lenguaje.text(Lenguaje.NO_CONECTOR));
        }
        _conexion = DriverManager.getConnection(ruta, usuario, password);

        if (!_conexion.isClosed())
            System.out.println("Conectado correctamente a '" + ruta + "' usando TCP/IP...");
    }

    @Override
    public void cerrarConexion() throws SQLException {
        if (_conexion != null) _conexion.close();
    }

    @Override
    public void ejecutarOrden(String orden) throws SQLException {
        Statement st = _conexion.createStatement();
        st.executeUpdate(orden);
        st.close();
    }

    @Override
    public String obtenerCodigoCreacionTabla(Tabla t) {
        // Crear la tabla
        String codigo = "CREATE TABLE " + t.getNombreTabla() + " (";

        // Para cada atributo...
        Vector<String[]> atributos = t.getAtributos();
        for (int i = 0; i < atributos.size(); i++) {
            if (i > 0) codigo += ", ";
            //metemos el atributo
            codigo += atributos.elementAt(i)[0];
            // metemos el dominio
            codigo += " " + equivalenciaTipoAccess(atributos.elementAt(i)[1]);

            // Indicamos si not null
            if (atributos.elementAt(i)[4].equalsIgnoreCase("1"))
                codigo += " NOT NULL";
        }
        //cerramos la creacion de la tabla
        codigo += ");\n";
        return codigo;
    }

    @Override
    public String obtenerCodigoCreacionTablaHTML(Tabla t) {
        // Crear la tabla
        String codigo = "<p><strong>CREATE TABLE </strong>" + t.getNombreTabla() + " (";

        // Para cada atributo...
        Vector<String[]> atributos = t.getAtributos();
        for (int i = 0; i < atributos.size(); i++) {
            if (i > 0) codigo += ", ";
            //metemos el atributo
            codigo += atributos.elementAt(i)[0];
            //metemos el dominio
            String dominio = equivalenciaTipoAccess(atributos.elementAt(i)[1]);
            codigo += " <strong>" + dominio + "</strong>";

            // Indicamos si not null
            if (atributos.elementAt(i)[4].equalsIgnoreCase("1"))
                codigo += "<strong> NOT NULL</strong>";
        }
        //cerramos la creacion de la tabla
        codigo += ");</p>";
        return codigo;
    }

    @Override
    public String obtenerCodigoClavesTablaHTML(Tabla t) {
        String codigo = "";

        //si tiene claves primarias, las añadimos
        Vector<String[]> primaries = t.getPrimaries();
        if (!primaries.isEmpty()) {
            codigo += "<p><strong>ALTER TABLE </strong>" + t.getNombreTabla() +
                    "<strong> ADD CONSTRAINT </strong>" + t.getNombreTabla_ini() + "_pk" + t.getNombreTabla_fin() +
                    "<strong> PRIMARY KEY </strong>" + "(";
            for (int i = 0; i < primaries.size(); i++) {
                if (i > 0) codigo += ", ";
                codigo += primaries.elementAt(i)[0];
            }
            codigo += ");</p>";
        }


        //si tiene claves foraneas
        Vector<String[]> foreigns = t.getForeigns();
        if (!foreigns.isEmpty()) {
            boolean abierto = false;
            String keys = "", rfrncs = "";
            for (int j = 0; j < foreigns.size(); j++) {
                if (!abierto) {
                    codigo += "<p><strong>ALTER TABLE </strong>" + t.getNombreTabla() + "<strong> ADD CONSTRAINT </strong>" +
                            t.getNombreTabla_ini() + "_" + foreigns.elementAt(j)[0].substring(1, foreigns.elementAt(j)[0].length() - 1) + t.getNombreTabla_fin() + "<strong> FOREIGN KEY </strong>(";

                    abierto = true;
                }
                keys += foreigns.elementAt(j)[0];
                rfrncs += nombreColumn(foreigns.elementAt(j)[2], foreigns.elementAt(j)[3]);
                if (foreigns.size() - j > 1) {
                    if (foreigns.elementAt(j + 1)[3] != foreigns.elementAt(j)[3]) {
                        codigo += keys + ") <strong> REFERENCES </strong>" + foreigns.elementAt(j)[3] + "(" + rfrncs + ");</p>";
                        abierto = false;
                        keys = "";
                        rfrncs = "";
                    } else {
                        keys += ", ";
                        rfrncs += ", ";
                    }
                } else {
                    codigo += keys + ") <strong> REFERENCES </strong>" + foreigns.elementAt(j)[3] + "(" + rfrncs + ");</p>";
                    abierto = false;
                    keys = "";
                    rfrncs = "";
                }
            }
        }

        // Si tiene uniques, se ponen
        Vector<String> uniques = t.getUniques();
        if (!uniques.isEmpty()) {
            codigo += "<p><strong>ALTER TABLE </strong>" + t.getNombreTabla() +
                    "<strong> ADD CONSTRAINT </strong>" + t.getNombreTabla_ini() + "_unique_" + t.getNombreTabla_fin() +
                    "<strong> UNIQUE</strong> (";
            for (int j = 0; j < uniques.size(); j++) {
                codigo += uniques.elementAt(j);
                if (uniques.size() - j > 1) codigo += ", ";
            }
            codigo += ");</p>";
        }
        return codigo;
    }

    @Override
    public String obtenerCodigoEnumerado(Enumerado e) {
        // Crear la tabla
        String codigo = "CREATE TABLE " + e.getNombre() + " (";
        if (e.getTipo() == TipoDominio.VARCHAR || e.getTipo() == TipoDominio.CHAR || e.getTipo() == TipoDominio.TEXT)
            codigo += "value_list " + e.getTipo() + "(" + e.getLongitud() + ")";
        else codigo += "value_list " + e.getTipo();
        codigo += ");\n";

        // Establecer la clave primaria
        codigo += "ALTER TABLE " + e.getNombre() + " ADD CONSTRAINT " + e.getNombre() + "_pk" +
                " PRIMARY KEY (value_list);\n";

        // Insertar los valores
        for (int i = 0; i < e.getNumeroValores(); i++) {
            String valor = e.getValor(i);
            if (valor.startsWith("'")) {
                valor = valor.substring(1, valor.length() - 1);
            }
            if (e.getTipo() == TipoDominio.VARCHAR || e.getTipo() == TipoDominio.CHAR || e.getTipo() == TipoDominio.TEXT)
                codigo += "INSERT INTO " + e.getNombre() + " (value_list) values ('" + valor + "');\n";
            else codigo += "INSERT INTO " + e.getNombre() + " (value_list) values (" + valor + ");\n";
        }

        codigo += "\n";
        return codigo;
    }

    @Override
    public String obtenerCodigoEnumeradoHTML(Enumerado e) {
        // Crear la tabla
        String codigo = "<p><strong>CREATE TABLE </strong>" + e.getNombre() + " (";
        if (e.getTipo() == TipoDominio.VARCHAR)
            codigo += "value_list " + "<strong>" + e.getTipo() + "(" + e.getLongitud() + ")</strong>";
        else codigo += "value_list " + "<strong>" + e.getTipo() + "</strong>";
        codigo += ")" + ";</p>";

        // Establecer la clave primaria
        codigo += "<p><strong>ALTER TABLE </strong>" + e.getNombre() +
                "<strong> ADD CONSTRAINT </strong>" + e.getNombre() + "_pk" +
                "<strong> PRIMARY KEY </strong>" + "(value_list);</p>";

        // Insertar los valores
        for (int i = 0; i < e.getNumeroValores(); i++) {
            String valor = e.getValor(i);
            if (valor.startsWith("'")) valor = valor.substring(1, valor.length() - 1);
            if (e.getTipo() == TipoDominio.VARCHAR || e.getTipo() == TipoDominio.CHAR || e.getTipo() == TipoDominio.TEXT)
                codigo += "<p><strong>INSERT INTO </strong>" + e.getNombre() + " (value_list) " +
                        "<strong> VALUES </strong>" + "(" + "'" + valor + "'" + ");" + "</p>";
            else codigo += "<p><strong>INSERT INTO </strong>" + e.getNombre() + " (value_list) " +
                    "<strong> VALUES </strong>(" + valor + ");</p>";
        }
        return codigo;
    }

    // METODOS AUXILIARES
    private String equivalenciaTipoAccess(String tipo) {
        // Tipos simples que no hay que modificar
        if (tipo.equalsIgnoreCase("INTEGER") || tipo.equalsIgnoreCase("DATETIME")) return tipo;
        // Tipos simples a modificar
        if (tipo.equalsIgnoreCase("FLOAT")) return "DOUBLE";
        if (tipo.equalsIgnoreCase("BIT")) return "YESNO";
        if (tipo.equalsIgnoreCase("DATE")) return "DATETIME";
        if (tipo.equalsIgnoreCase("BLOB")) return "IMAGE";
        if (tipo.equalsIgnoreCase("GEOMETRY")) return "SDO_GEOMETRY";
        if (tipo.equalsIgnoreCase("VARCHAR")) return "VARCHAR";

        // Tipos compuestos que no hay que modificar
        if (tipo.indexOf("(") > 0) {
            String tipoSinParam = tipo.substring(0, tipo.indexOf("("));
            if (tipoSinParam.equalsIgnoreCase("CHAR") || tipoSinParam.equalsIgnoreCase("VARCHAR")) return tipo;
            // Tipos compuestos que sí hay que modificar
            String param = tipo.substring(tipo.indexOf("("));
            if (tipoSinParam.equalsIgnoreCase("TEXT")) return "MEMO" + param;
            if (tipoSinParam.equalsIgnoreCase("DECIMAL")) return "CURRENCY";
            if (tipoSinParam.equalsIgnoreCase("INTEGER")) return "INTEGER";
        }
        // Tipos pertenecientes a los dominios creados
        return tipo + "_sinAnalizar";
    }

    @Override
    public void usarDatabase(String nombre) throws SQLException {
    }
}
