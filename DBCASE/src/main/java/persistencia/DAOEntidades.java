package persistencia;

import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import modelo.transfers.TransferEntidad;
import vista.lenguaje.Lenguaje;

@SuppressWarnings("rawtypes")
public class DAOEntidades {

    // Atributos
    private final Document doc;
    private String path;


    // Constructora del DAO
    public DAOEntidades(String path) {
        this.path = path;
        //this.path += "\\persistencia.xml";
        this.path = this.path.replace(" ", "%20");
        this.path = this.path.replace('\\', '/');
        this.doc = dameDoc();

    }

    // Metodos del DAOEntidades
    public int anadirEntidad(TransferEntidad tc) {
        // Resultado que se devolvera
        int resultado = 0;
        resultado = tc.getIdEntidad();
        //sacamos la <ListaEntidades>
        NodeList LC = doc.getElementsByTagName("EntityList");
        // Sacamos el nodo
        Node listado = LC.item(0);
        //nuevo id.
        //int proximoID =dameIdEntidad(listado);
        //this.dameEntidad(doc);
        //listado.getAttributes().item(0).setNodeValue(Integer.toString(proximoID+1));
        Node ListaEntidades = LC.item(0);
        // Ya estamos situados
        Element raiz = doc.createElement("Entity");
        raiz.setAttribute("EntityId", Integer.toString(resultado));
        // Nombre
        Element elem = doc.createElement("Name");
        elem.appendChild(doc.createTextNode(tc.getNombre()));
        raiz.appendChild(elem);
        // Debil
        elem = doc.createElement("Weak");
        elem.appendChild(doc.createTextNode(tc.isDebil() + ""));
        raiz.appendChild(elem);
        // ListaAtributos
        Element raizListaAtributos = doc.createElement("AttribList");
        raiz.appendChild(raizListaAtributos);

        //estamos en la lista de Atributos.
        for (int cont = 0; cont < tc.getListaAtributos().size(); cont++) {
            Element Atributo = doc.createElement("Attrib");
            Atributo.appendChild(doc.createTextNode(tc.getListaAtributos().elementAt(cont).toString()));
            raizListaAtributos.appendChild(Atributo);
        }
        // ListaClavesPrimarias
        Element raizListaClavesPrimarias = doc.createElement("PrimaryKeyList");
        raiz.appendChild(raizListaClavesPrimarias);
        for (int cont = 0; cont < tc.getListaClavesPrimarias().size(); cont++) {
            Element cp = doc.createElement("PrimaryKey");
            cp.appendChild(doc.createTextNode(tc.getListaClavesPrimarias().elementAt(cont).toString()));
            raizListaClavesPrimarias.appendChild(cp);
        }
        // ListaRestricciones
        Element raizListaRestricciones = doc.createElement("AssertionList");
        raiz.appendChild(raizListaRestricciones);
        for (int cont = 0; cont < tc.getListaRestricciones().size(); cont++) {
            Element cp = doc.createElement("Assertion");
            cp.appendChild(doc.createTextNode(tc.getListaRestricciones().elementAt(cont).toString()));
            raizListaRestricciones.appendChild(cp);
        }
        // ListaUniques
        Element raizListaUniques = doc.createElement("UniqueList");
        raiz.appendChild(raizListaUniques);
        //estamos en la lista de Uniques.
        for (int cont = 0; cont < tc.getListaUniques().size(); cont++) {
            Element cp = doc.createElement("Uniques");
            cp.appendChild(doc.createTextNode(tc.getListaUniques().elementAt(cont).toString()));
            raizListaUniques.appendChild(cp);
        }

        //posicion
        elem = doc.createElement("Position");
        elem.appendChild(doc.createTextNode((int) (tc.getPosicion().getX()) + "," + (int) (tc.getPosicion().getY())));
        raiz.appendChild(elem);

        // Lo añadimos a la lista de Entidades
        ListaEntidades.appendChild(raiz);
        // Actualizamos el resultado
        //resultado = proximoID;
        // Guardamos los cambios en el fichero xml y controlamos la excepcion
        this.guardaDoc();

        // Devolvemos el resultado de la operacion
        return resultado;
    }

    public TransferEntidad consultarEntidad(TransferEntidad tc) {
        // Entidad que devolveremos
        TransferEntidad transfer = null;
        // Obtenemos el nodo del Entidad
        Node nodoEntidadBuscado = dameNodoEntidad(tc.getIdEntidad());
        // Lo transformamos a Entidad si es distinto de null
        if (nodoEntidadBuscado != null)
            transfer = nodoEntidadATransferEntidad(nodoEntidadBuscado);
        // Lo devolvemos
        return transfer;
    }

    public boolean modificarEntidad(TransferEntidad tc) {
        // Resultado que devolveremos
        boolean respuesta = true;
        // Obtenemos el Entidad
        Node EntidadBuscado = dameNodoEntidad(tc.getIdEntidad());
        if (EntidadBuscado != null) {
            // Cambiamos los datos del Entidad
            ponValorAElemento(
                    dameNodoPedidoDeEntidad(EntidadBuscado, "Name"), tc
                            .getNombre());
            ponValorAElemento(dameNodoPedidoDeEntidad(EntidadBuscado,
                    "Weak"), Boolean.toString((tc.isDebil())));
            ponValorAElemento(dameNodoPedidoDeEntidad(EntidadBuscado,
                    "Position"), ((int) (tc.getPosicion().getX()) + "," + (int) (tc.getPosicion().getY())));

            //--------------------------------------
            Node listaC = dameNodoPedidoDeEntidad(EntidadBuscado, "AttribList");
            int i = 0;
            Node n;
            System.out.println("child nodes length" + listaC.getChildNodes().getLength());
            while (i < listaC.getChildNodes().getLength()) {
                n = this.dameNodoPedidoDeEntidad(listaC, "Attrib");
                if (n != null) listaC.removeChild(n);
                else i++;
            }

            for (int cont = 0; cont < tc.getListaAtributos().size(); cont++) {
                Element Atributo = doc.createElement("Attrib");
                Atributo.appendChild(doc.createTextNode(tc.getListaAtributos().elementAt(cont).toString()));
                listaC.appendChild(Atributo);
            }


            Node listaV = dameNodoPedidoDeEntidad(EntidadBuscado, "PrimaryKeyList");
            int j = 0;
            while (j < listaV.getChildNodes().getLength()) {
                n = this.dameNodoPedidoDeEntidad(listaV, "PrimaryKey");
                if (n != null) listaV.removeChild(n);
                j++;
            }

            for (int cont = 0; cont < tc.getListaClavesPrimarias().size(); cont++) {
                Element clavePrimaria = doc.createElement("PrimaryKey");
                clavePrimaria.appendChild(doc.createTextNode(tc.getListaClavesPrimarias().elementAt(cont).toString()));
                listaV.appendChild(clavePrimaria);
            }
            //-------
            Node listaR = dameNodoPedidoDeEntidad(EntidadBuscado, "AssertionList");
            int k = 0;
            while (k < listaR.getChildNodes().getLength()) {
                n = this.dameNodoPedidoDeEntidad(listaR, "Assertion");
                if (n != null) listaR.removeChild(n);
                k++;
            }
            for (int cont = 0; cont < tc.getListaRestricciones().size(); cont++) {
                Element restriccion = doc.createElement("Assertion");
                restriccion.appendChild(doc.createTextNode(tc.getListaRestricciones().elementAt(cont).toString()));
                listaR.appendChild(restriccion);
            }
            Node listaU = dameNodoPedidoDeEntidad(EntidadBuscado, "UniqueList");
            k = 0;
            while (k < listaU.getChildNodes().getLength()) {
                n = this.dameNodoPedidoDeEntidad(listaU, "Uniques");
                if (n != null) listaU.removeChild(n);
                k++;
            }
            for (int cont = 0; cont < tc.getListaUniques().size(); cont++) {
                Element unique = doc.createElement("Uniques");
                unique.appendChild(doc.createTextNode(tc.getListaUniques().elementAt(cont).toString()));
                listaU.appendChild(unique);
            }
            //---------------------------
        } else
            respuesta = false;
        // Guardamos los cambios en el fichero xml y controlamos la excepcion
        this.guardaDoc();

        // Devolvemos la respuesta
        return respuesta;
    }

    public boolean borrarEntidad(TransferEntidad tc) {
        Node EntidadBuscado = dameNodoEntidad(tc.getIdEntidad());
        NodeList LC = doc.getElementsByTagName("EntityList");
        // Sacamos el nodo
        Node raiz = LC.item(0);
        boolean borrado = false;
        if ((EntidadBuscado != null) && (raiz != null)) {
            raiz.removeChild(EntidadBuscado);
            borrado = true;
        }
        this.guardaDoc();

        return borrado;
    }

    public Vector<TransferEntidad> ListaDeEntidades() {
        // Vector que devolveremos
        Vector<TransferEntidad> vectorDeTransfers = new Vector<TransferEntidad>();
        TransferEntidad te = new TransferEntidad();
        TransferEntidad aux = new TransferEntidad();
        // Obtenemos los Entidades y los vamos anadiendo
        NodeList lista = doc.getElementsByTagName("Entity");
        int numEntidades = lista.getLength(); //vemos cuantas entidades hay en el XML
        String id;

        for (int i = 0; i < numEntidades; i++) {
            //obtenemos el ID de cada entidad, la consultamos y la metemos en el vector.
            id = lista.item(i).getAttributes().item(0).getNodeValue();
            aux.setIdEntidad(Integer.parseInt(id));
            te = this.consultarEntidad(aux);
            vectorDeTransfers.add(te);
        }
        //devolvemos las entidades con todos sus datos.
        return vectorDeTransfers;
    }

    // Metodos privados

    private Node dameNodoEntidad(int id) {
        Node EntidadBuscado = null;
        NodeList LC = doc.getElementsByTagName("Entity");
        // Obtener el Entidad id de un nodo Entidad
        boolean encontrado = false;
        int cont = 0;
        while ((!encontrado) && (cont < LC.getLength())) {
            // Obtenemos el nodo
            Node nodoEntidadActual = LC.item(cont);
            // Obtenemos sus Entidades
            NamedNodeMap EntidadesNodoEntidadActual = nodoEntidadActual
                    .getAttributes();
            // Obtenemos el id del Entidad
            int idNodoEntidadActual = Integer
                    .parseInt(EntidadesNodoEntidadActual.item(0).getNodeValue());
            // Comparamos
            if (id == idNodoEntidadActual) {
                encontrado = true;
                EntidadBuscado = nodoEntidadActual;
            } else
                cont++;
        }
        return EntidadBuscado;
    }

    private TransferEntidad nodoEntidadATransferEntidad(Node nodo) {
        int id = dameIdEntidad(nodo);
        String nombre = dameValorDelElemento(dameNodoPedidoDeEntidad(nodo,
                "Name"));
        boolean debil = Boolean
                .parseBoolean(dameValorDelElemento(dameNodoPedidoDeEntidad(
                        nodo, "Weak")));
        Vector listaC = nodoListaAObjetoLista(dameNodoPedidoDeEntidad(nodo, "AttribList"), "Attrib");
        Vector listaV = nodoListaAObjetoLista(dameNodoPedidoDeEntidad(nodo, "PrimaryKeyList"), "PrimaryKey");
        Vector listaR = nodoListaAObjetoLista(dameNodoPedidoDeEntidad(nodo, "AssertionList"), "Assertion");
        Vector listaU = nodoListaAObjetoLista(dameNodoPedidoDeEntidad(nodo, "UniqueList"), "Uniques");

        Point2D posicion = this.damePunto(dameValorDelElemento(dameNodoPedidoDeEntidad(nodo, "Position")));


        // Creamos el transfer
		/*TransferEntidad transfer = SingletonFactoriaTransfers
				.obtenerInstancia().generaTransferEntidad();*/
        TransferEntidad transfer = new TransferEntidad();
        transfer.setIdEntidad(id);
        transfer.setNombre(nombre);
        transfer.setDebil(debil);
        transfer.setListaAtributos(listaC);
        transfer.setPosicion(posicion);
        transfer.setListaClavesPrimarias(listaV);
        transfer.setListaRestricciones(listaR);
        transfer.setListaUniques(listaU);
        // Lo devolvemos
        return transfer;
    }

    private Vector nodoListaAObjetoLista(Node nodo, String tipoLista) {
        //tipoLista puede ser : Atributo y clavePrimaria
        //Resultado que devolveremos
        Vector<String> lista = new Vector<String>();
        // Sacamos la lista de hijos
        NodeList LD = nodo.getChildNodes();

        // Buscamos en la lista los nodos del tipo que queremos (tipolista)
        int cont = 0;
        while (cont < LD.getLength()) {
            Node aux = LD.item(cont);
            if (aux.getNodeName() == tipoLista && aux.hasChildNodes()) {
                lista.addElement(dameValorDelElemento(aux));
            }
            cont++;
        }
        // Devolvemos la lista
        return lista;
    }

    private String dameValorDelElemento(Node elemento) {
        return elemento.getFirstChild().getNodeValue();
    }

    private void ponValorAElemento(Node elemento, String clavePrimaria) {
        elemento.getFirstChild().setNodeValue(clavePrimaria);
    }

    private int dameIdEntidad(Node Entidad) {
        return Integer.parseInt(Entidad.getAttributes().item(0).getNodeValue());
    }

    private Node dameNodoPedidoDeEntidad(Node Entidad, String elemento) {
        Node nodoBuscado = null;
        // Generamos los hijos de Entidad
        NodeList hijos = Entidad.getChildNodes();
        // Buscamos hasta encontar un elemento "elemento"
        int cont = 0;
        Node aux = hijos.item(0);
        if (aux != null)
            while (aux.getNodeName() != (elemento) && (cont < hijos.getLength())) {
                aux = hijos.item(cont++);
            }
        // Ya tenemos en aux el nodoBuscado
        nodoBuscado = aux;
        // Lo devolvemos
        return nodoBuscado;
    }

    private Point2D damePunto(String posicion) {
        Point2D p = new Point2D.Double();
        String primero, segundo;
        int coma = posicion.indexOf(",");
        primero = posicion.substring(0, coma);
        segundo = posicion.substring(coma + 1);
        p.setLocation(Double.parseDouble(primero), Double.parseDouble(segundo));
        return p;
    }

    // Metodos para el tratamiento del fichero xml
    private Document dameDoc() {
        Document doc = null;
        DocumentBuilder parser = null;
        try {
            DocumentBuilderFactory factoria = DocumentBuilderFactory.newInstance();
            parser = factoria.newDocumentBuilder();
            doc = parser.parse(this.path);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    Lenguaje.text(Lenguaje.ERROR) + ":\n" +
                            Lenguaje.text(Lenguaje.UNESPECTED_XML_ERROR) + " \"" + path + ".xml\"",
                    Lenguaje.text(Lenguaje.DBCASE),
                    JOptionPane.ERROR_MESSAGE);
        }
        return doc;
    }

    private void guardaDoc() {
        //OutputFormat formato = new OutputFormat(doc, "utf-8", true);
        OutputFormat formato = new OutputFormat();
        StringWriter s = new StringWriter();
        XMLSerializer ser = new XMLSerializer(s, formato);
        try {
            ser.serialize(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // El FileWriter necesita espacios en la ruta
        this.path = this.path.replace("%20", " ");
        FileWriter f = null;
        /*debido a que la funcion FileWriter da un error de acceso
         * de vez en cuando, forzamos su ejecucion hasta que funcione correctamente*/
        boolean centinela = true;
        while (centinela) {
            try {
                f = new FileWriter(this.path);
                centinela = false;
            } catch (IOException e) {
                centinela = true;
            }
        }
        this.path = this.path.replace(" ", "%20");
        ser = new XMLSerializer(f, formato);
        try {
            ser.serialize(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
