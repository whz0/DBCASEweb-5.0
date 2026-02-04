package modelo.conectorDBMS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Vector;

import modelo.servicios.Enumerado;
import modelo.servicios.Tabla;
import modelo.transfers.TipoDominio;
import vista.lenguaje.Lenguaje;

/**
 * Conecta la aplicación con un gestor de bases de datos Oracle
 *
 * @author Denis Cepeda
 */
public class ConectorOracle extends ConectorDBMS {

    private Connection _conexion;

    @Override
    public void abrirConexion(String ruta, String usuario, String password)
            throws SQLException {
        String driver = "oracle.jdbc.OracleDriver";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println(Lenguaje.text(Lenguaje.NO_CONECTOR));
            e.printStackTrace();

            return;
        }

        int rompe = ruta.lastIndexOf("#");
        String database = "";
        if (rompe > 0) {
            database = ":" + ruta.substring(rompe + 1);
            ruta = ruta.substring(0, rompe);
        }

        //_conexion = DriverManager.getConnection(ruta, usuario, password);
        _conexion = DriverManager.getConnection("jdbc:oracle:thin:" + usuario +
                "/" + password + "@" + ruta + database);

        _conexion.setAutoCommit(false);

        if (!_conexion.isClosed())
            System.out.println("Conectado correctamente a '" + ruta + "' usando TCP/IP...");
    }

    @Override
    public void cerrarConexion() throws SQLException {
        if (_conexion != null)
            _conexion.close();
    }

    @Override
    public void ejecutarOrden(String orden) throws SQLException {
        // Si acaba en ;, quitarlo
        int fin = orden.indexOf(";");
        if (fin >= 0) {
            orden = orden.substring(0, fin);
        }

        Statement st = _conexion.createStatement();
        st.executeUpdate(orden);
        st.close();
    }

    @Override
    public String obtenerCodigoCreacionTabla(Tabla t) {
        // Crear la tabla
        StringBuilder codigo = new StringBuilder("CREATE TABLE " + t.getNombreTabla() + " (");

        // Para cada atributo...
        Vector<String[]> atributos = t.getAtributos();
        for (int i = 0; i < atributos.size(); i++) {
            if (i > 0) codigo.append(", ");
            //metemos el atributo
            codigo.append(atributos.elementAt(i)[0]);
            // metemos el dominio
            codigo.append(" ").append(equivalenciaTipoOracle(atributos.elementAt(i)[1]));

            // not null
            if (atributos.elementAt(i)[4].equalsIgnoreCase("1"))
                codigo.append(" NOT NULL");
        }
        //cerramos la creacion de la tabla
        codigo.append(");\n");
        return codigo.toString();
    }

    @Override
    public String obtenerCodigoCreacionTablaHTML(Tabla t) {
        // Crear la tabla
        StringBuilder codigo = new StringBuilder("<p><strong>CREATE TABLE </strong>" + t.getNombreTabla() + " (");

        // Para cada atributo...
        Vector<String[]> atributos = t.getAtributos();
        for (int i = 0; i < atributos.size(); i++) {
            if (i > 0) codigo.append(", ");
            //metemos el atributo
            codigo.append(atributos.elementAt(i)[0]);
            //metemos el dominio
            String dominio = equivalenciaTipoOracle(atributos.elementAt(i)[1]);
            codigo.append(" <strong>").append(dominio).append("</strong>");

            // Not null
            if (atributos.elementAt(i)[4].equalsIgnoreCase("1"))
                codigo.append("<strong> NOT NULL</strong>");
        }
        //cerramos la creacion de la tabla
        codigo.append(")" + ";</p>");
        return codigo.toString();
    }

    @Override
    public String obtenerCodigoClavesTablaHTML(Tabla t) {
        StringBuilder codigo = new StringBuilder();

        //si tiene claves primarias, las añadimos
        Vector<String[]> primaries = t.getPrimaries();
        if (!primaries.isEmpty()) {
            codigo.append("<p><strong>ALTER TABLE </strong>").append(t.getNombreTabla()).append("<strong> ADD CONSTRAINT </strong>").append(t.getNombreTabla_ini()).append("_pk").append(t.getNombreTabla_fin()).append("<strong> PRIMARY KEY </strong>").append("(");
            for (int i = 0; i < primaries.size(); i++) {
                if (i > 0) codigo.append(", ");
                codigo.append(primaries.elementAt(i)[0]);
            }
            codigo.append(");</p>");
        }
        //si tiene claves foraneas:
        Vector<String[]> foreigns = t.getForeigns();
        if (!foreigns.isEmpty()) {
            boolean abierto = false;
            StringBuilder keys = new StringBuilder();
            StringBuilder rfrncs = new StringBuilder();
            for (int j = 0; j < foreigns.size(); j++) {
                if (!abierto) {
                    codigo
                            .append("<p><strong>ALTER TABLE </strong>")
                            .append(t.getNombreTabla())
                            .append("<strong> ADD CONSTRAINT </strong>")
                            .append(t.getNombreTabla_ini())
                            .append("_")
                            .append(foreigns.elementAt(j)[0], 1, foreigns.elementAt(j)[0].length() - 1)
                            .append(t.getNombreTabla_fin())
                            .append("<strong> FOREIGN KEY </strong>(");
                    abierto = true;
                }
                keys.append(foreigns.elementAt(j)[0]);
                rfrncs.append(nombreColumn(foreigns.elementAt(j)[2], foreigns.elementAt(j)[3]));
                if (foreigns.size() - j > 1) {
                    if (!Objects.equals(foreigns.elementAt(j + 1)[3], foreigns.elementAt(j)[3])) {
                        codigo.append(keys).append(") <strong> REFERENCES </strong>").append(foreigns.elementAt(j)[3]).append("(").append(rfrncs).append(");</p>");
                        abierto = false;
                        keys = new StringBuilder();
                        rfrncs = new StringBuilder();
                    } else {
                        keys.append(", ");
                        rfrncs.append(", ");
                    }
                } else {
                    codigo.append(keys).append(") <strong> REFERENCES </strong>").append(foreigns.elementAt(j)[3]).append("(").append(rfrncs).append(");</p>");
                    abierto = false;
                    keys = new StringBuilder();
                    rfrncs = new StringBuilder();
                }
            }
        }

        // Si tiene uniques, se ponen
        Vector<String> uniques = t.getUniques();
        if (!uniques.isEmpty()) {
            codigo.append("<p><strong>ALTER TABLE </strong>").append(t.getNombreTabla()).append("<strong> ADD CONSTRAINT </strong>").append(t.getNombreTabla_ini()).append("_unique_").append(t.getNombreTabla_fin()).append("<strong> UNIQUE</strong> (");
            for (int j = 0; j < uniques.size(); j++) {
                codigo.append(uniques.elementAt(j));
                if (uniques.size() - j > 1) codigo.append(", ");
            }
            codigo.append(");</p>");
        }

        return codigo.toString();
    }

    @Override
    public String obtenerCodigoEnumerado(Enumerado e) {
        // Crear la tabla
        StringBuilder codigo = new StringBuilder("CREATE TABLE " + e.getNombre() + " (");
        if (e.getTipo() == TipoDominio.VARCHAR || e.getTipo() == TipoDominio.CHAR || e.getTipo() == TipoDominio.TEXT)
            codigo.append("value_list ").append(e.getTipo()).append("(").append(e.getLongitud()).append(")");
        else codigo.append("value_list ").append(e.getTipo());
        codigo.append(");\n");

        // Establecer la clave primaria
        codigo.append("ALTER TABLE ").append(e.getNombre()).append(" ADD CONSTRAINT ").append(e.getNombre()).append("_pk").append(" PRIMARY KEY (value_list);\n");

        // Insertar los valores
        for (int i = 0; i < e.getNumeroValores(); i++) {
            String valor = e.getValor(i);
            if (valor.startsWith("'")) valor = valor.substring(1, valor.length() - 1);
            if (e.getTipo() == TipoDominio.VARCHAR || e.getTipo() == TipoDominio.CHAR || e.getTipo() == TipoDominio.TEXT)
                codigo.append("INSERT INTO ").append(e.getNombre()).append(" values ('").append(valor).append("');\n");
            else codigo.append("INSERT INTO ").append(e.getNombre()).append(" values (").append(valor).append(");\n");
        }
        codigo.append("\n");
        return codigo.toString();
    }

    @Override
    public String obtenerCodigoEnumeradoHTML(Enumerado e) {
        // Crear la tabla
        StringBuilder codigo = new StringBuilder("<p><strong>CREATE TABLE </strong>" + e.getNombre() + " (");
        if (e.getTipo() == TipoDominio.VARCHAR)
            codigo.append("value_list " + "<strong>").append(e.getTipo()).append("(").append(e.getLongitud()).append(")</strong>");
        else codigo.append("value_list " + "<strong>").append(e.getTipo()).append("</strong>");
        codigo.append(")" + ";</p>");

        // Establecer la clave primaria
        codigo.append("<p><strong>ALTER TABLE </strong>").append(e.getNombre()).append("<strong> ADD CONSTRAINT </strong>").append(e.getNombre()).append("_pk").append("<strong> PRIMARY KEY </strong>").append("(value_list);</p>");

        // Insertar los valores
        for (int i = 0; i < e.getNumeroValores(); i++) {
            String valor = e.getValor(i);
            if (valor.startsWith("'")) valor = valor.substring(1, valor.length() - 1);
            if (e.getTipo() == TipoDominio.VARCHAR || e.getTipo() == TipoDominio.CHAR || e.getTipo() == TipoDominio.TEXT)
                codigo.append("<p><strong>INSERT INTO </strong>").append(e.getNombre()).append("<strong> VALUES </strong>").append("(").append("'").append(valor).append("'").append(");").append("</p>");
            else
                codigo.append("<p><strong>INSERT INTO </strong>").append(e.getNombre()).append("<strong> VALUES </strong>(").append(valor).append(");</p>");
        }
        return codigo.toString();
    }

    // METODOS AUXILIARES
    private String equivalenciaTipoOracle(String tipo) {
        // Tipos simples que no hay que modificar
        if (tipo.equalsIgnoreCase("INTEGER") ||
                tipo.equalsIgnoreCase("DATE") ||
                tipo.equalsIgnoreCase("BLOB") ||
                tipo.equalsIgnoreCase("VARCHAR")) {
            return tipo;
        }

        // Tipos simples a modificar
        if (tipo.equalsIgnoreCase("FLOAT")) {
            return "REAL";
        }
        if (tipo.equalsIgnoreCase("BIT")) {
            return "CHAR(1)";
        }
        if (tipo.equalsIgnoreCase("DATETIME")) {
            return "TIMESTAMP";
        }
        if (tipo.equalsIgnoreCase("GEOMETRY")) {
            return "SDO_GEOMETRY";
        }

        // Tipos compuestos que no hay que modificar
        if (tipo.indexOf("(") > 0) {
            String tipoSinParam = tipo.substring(0, tipo.indexOf("("));
            if (tipoSinParam.equalsIgnoreCase("CHAR") ||
                    tipoSinParam.equalsIgnoreCase("INTEGER")) {
                return tipo;
            }

            // Tipos compuestos que sí hay que modificar
            String param = tipo.substring(tipo.indexOf("("));
            if (tipoSinParam.equalsIgnoreCase("VARCHAR")) {
                return "VARCHAR2" + param;
            }
            if (tipoSinParam.equalsIgnoreCase("TEXT")) {
                return "CLOB" + param;
            }
            if (tipoSinParam.equalsIgnoreCase("DECIMAL")) {
                return "NUMBER" + param;
            }
        }

        // Tipos pertenecientes a los dominios creados
        return tipo + "_sinAnalizar";
    }

    @Override
    public void usarDatabase(String nombre) throws SQLException {
        Statement stBuscar = _conexion.createStatement();
        String consulta = "SELECT table_name FROM user_tables";
        ResultSet rs = stBuscar.executeQuery(consulta);

        while (rs.next()) {
            System.out.println("    " + rs.getString("table_name"));
            System.out.println("Eliminando tabla...");
            try {
                Statement stBorrar = _conexion.createStatement();
                stBorrar.executeUpdate("DROP TABLE " + rs.getString("table_name") +
                        " CASCADE CONSTRAINTS");
            } catch (SQLException e) {
                // No pasa nada. Esto es que ya se ha borrado
                // con el cascade constraints, o que es una vista
            }
        }
    }
}