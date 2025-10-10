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

import controlador.Controlador;
import modelo.transfers.TransferAtributo;
import vista.lenguaje.Lenguaje;
@SuppressWarnings("rawtypes")
public class DAOAtributos{
	
	// Atributos
	private Document doc;
	private String path;
	private Controlador c;
	
	// Constructora del DAO
	public DAOAtributos(Controlador c){
		this.c = c;
		this.path = c.getPath();
		//this.path += "\\persistencia.xml";
		this.path = this.path.replace(" ", "%20");
		this.path = this.path.replace('\\', '/');
		this.doc = dameDoc();
	}
	
	// Metodos del DAOAtributos
	public int anadirAtributo(TransferAtributo tc) {
		// Resultado que se devolvera
		int resultado = 0;
		// Generamos el id del nuevo Atributo
		// Sacamos la <ListaAtributos>
		NodeList LC = doc.getElementsByTagName("AttributeList");
		// Sacamos el nodo
		Node listado = LC.item(0);
		int proximoID =dameAtributo(listado);
		listado.getAttributes().item(0).setNodeValue(Integer.toString(proximoID+1));
		Node ListaAtributos = LC.item(0);
		
		// Ya estamos situados
		Element raiz = doc.createElement("Attribute");
    	raiz.setAttribute("AttributeId",Integer.toString(proximoID));
		// Nombre
		Element elem = doc.createElement("Name");
		elem.appendChild(doc.createTextNode(tc.getNombre()));
		raiz.appendChild(elem);
		// Dominio
		elem = doc.createElement("Dom");
		elem.appendChild(doc.createTextNode(tc.getDominio()));
		raiz.appendChild(elem);
		// Compuesto
		elem = doc.createElement("Composed");
		elem.appendChild(doc.createTextNode(tc.getCompuesto() + ""));
		raiz.appendChild(elem);
		// Notnull
		elem = doc.createElement("NotNull");
		elem.appendChild(doc.createTextNode(tc.getNotnull() + ""));
		raiz.appendChild(elem);
		// Unique
		elem = doc.createElement("Unique");
		elem.appendChild(doc.createTextNode(tc.getUnique() + ""));
		raiz.appendChild(elem);
		// ListaComponentes
		Element raizListaComponentes = doc.createElement("ComponentList");
		raiz.appendChild(raizListaComponentes);
		//estamos en la lista de componentes.
		for (int cont = 0; cont <tc.getListaComponentes().size(); cont++) {
			Element subAtributo = doc.createElement("SubAttribute");
			subAtributo.appendChild(doc.createTextNode(tc.getListaComponentes().elementAt(cont).toString()));
			raizListaComponentes.appendChild(subAtributo);
		}
		// Multivalorado
		elem = doc.createElement("Multivalued");
		elem.appendChild(doc.createTextNode(tc.isMultivalorado() + ""));
		raiz.appendChild(elem);
		// ListaRestricciones
		Element raizListaRestricciones = doc.createElement("AssertionList");
		raiz.appendChild(raizListaRestricciones);
		//estamos en la lista de Atributos.
		for (int cont = 0; cont <tc.getListaRestricciones().size(); cont++) {
			Element cp = doc.createElement("Assertion");
			cp.appendChild(doc.createTextNode(tc.getListaRestricciones().elementAt(cont).toString()));
			raizListaRestricciones.appendChild(cp);
		}
				//posicion
		elem = doc.createElement("Position");
		elem.appendChild(doc.createTextNode((int)(tc.getPosicion().getX())+","+(int)(tc.getPosicion().getY())));
		raiz.appendChild(elem);
		
		elem = doc.createElement("entidadOrigin_ID");
		elem.appendChild(doc.createTextNode(String.valueOf(tc.getEntidad_origenID())));
		raiz.appendChild(elem);

		elem = doc.createElement("entidadOrigin_Name");
		elem.appendChild(doc.createTextNode(String.valueOf(tc.getEntidad_origenName())));
		raiz.appendChild(elem);
		
		// Lo anadimos a la lista de Atributos
		ListaAtributos.appendChild(raiz);
		// Actualizamos el resultado
		resultado = proximoID;
		// Guardamos los cambios en el fichero xml y controlamos la excepcion
		this.guardaDoc();
		
		// Devolvemos el resultado de la operacion
		return resultado;
	}

	public TransferAtributo consultarAtributo(TransferAtributo tc) {
		// Atributo que devolveremos
		TransferAtributo transfer = null;
		// Obtenemos el nodo del Atributo
		Node nodoAtributoBuscado = dameNodoAtributo(tc.getIdAtributo());
		// Lo transformamos a Atributo si es distinto de null
		if (nodoAtributoBuscado != null)
			transfer = nodoAtributoATransferAtributo(nodoAtributoBuscado);
		// Lo devolvemos
		return transfer;
	}

	public boolean modificarAtributo(TransferAtributo tc) {
		// Resultado que devolveremos
		boolean respuesta = true;
		// Obtenemos el Atributo
		Node AtributoBuscado = dameNodoAtributo(tc.getIdAtributo());
		if (AtributoBuscado != null) {
			// Cambiamos los datos del Atributo
			ponValorAElemento(
					dameNodoPedidoDeAtributo(AtributoBuscado, "Name"), tc
							.getNombre());
			ponValorAElemento(dameNodoPedidoDeAtributo(AtributoBuscado,
					"Dom"), tc.getDominio());
			ponValorAElemento(dameNodoPedidoDeAtributo(AtributoBuscado,
					"Composed"), Boolean.toString((tc.getCompuesto())));
			ponValorAElemento(dameNodoPedidoDeAtributo(AtributoBuscado,
					"NotNull"), Boolean.toString((tc.getNotnull())));
			ponValorAElemento(dameNodoPedidoDeAtributo(AtributoBuscado,
					"Unique"), Boolean.toString((tc.getUnique())));
			ponValorAElemento(dameNodoPedidoDeAtributo(AtributoBuscado,
					"Multivalued"), Boolean.toString((tc.isMultivalorado())));
			ponValorAElemento(dameNodoPedidoDeAtributo(AtributoBuscado,
					"Position"), ((int)(tc.getPosicion().getX())+","+(int)(tc.getPosicion().getY())));
			
			Node listaC=dameNodoPedidoDeAtributo(AtributoBuscado,"ComponentList");
			int i=0;
			Node n;
			while (i<listaC.getChildNodes().getLength()){
				n=this.dameNodoPedidoDeAtributo(listaC,"SubAttribute");
				if (n!=null)listaC.removeChild(n);
				i++;
			}
			
			for (int cont = 0; cont <tc.getListaComponentes().size(); cont++) {
					Element subAtributo = doc.createElement("SubAttribute");
					subAtributo.appendChild(doc.createTextNode(tc.getListaComponentes().elementAt(cont).toString()));
					listaC.appendChild(subAtributo);			
			}
			
			Node listaR=dameNodoPedidoDeAtributo(AtributoBuscado,"AssertionList");
			int k=0;
			while (k<listaR.getChildNodes().getLength()){
				n=this.dameNodoPedidoDeAtributo(listaR,"Assertion");
				if (n!=null)listaR.removeChild(n);
				k++;
			}
			for (int cont = 0; cont <tc.getListaRestricciones().size(); cont++) {
					Element restriccion = doc.createElement("Assertion");
					restriccion.appendChild(doc.createTextNode(tc.getListaRestricciones().elementAt(cont).toString()));
					listaR.appendChild(restriccion);			
			}
			
		} else
			respuesta = false;
		// Guardamos los cambios en el fichero xml y controlamos la excepcion
		this.guardaDoc();
		
		// Devolvemos la respuesta
		return respuesta;
	}
	

	public boolean borrarAtributo(TransferAtributo tc) {
		Node atributoBuscado = dameNodoAtributo(tc.getIdAtributo());
		NodeList LC = doc.getElementsByTagName("AttributeList");
		// Sacamos el nodo
		Node raiz = LC.item(0); 
		boolean borrado=false;
			if ((atributoBuscado != null) && (raiz!=null)){
				raiz.removeChild(atributoBuscado);
				borrado=true;
			}
			this.guardaDoc();
			
		return borrado;
	}
	public Vector<TransferAtributo> ListaDeAtributos() {
		// Vector que devolveremos
		Vector<TransferAtributo> vectorDeTransfers = new Vector<TransferAtributo>();
		TransferAtributo ta= new TransferAtributo(c);
		TransferAtributo aux= new TransferAtributo(c);
		// Obtenemos los atributos y los vamos anadiendo
		NodeList lista=doc.getElementsByTagName("Attribute");
		int numAtributos = lista.getLength(); //vemos cuantos atributoss hay en el XML
		String id;

		for (int i=0;i<numAtributos;i++){
			//obtenemos el ID de cada atributo, lo consultamos y lo metemos en el vector.
			id= lista.item(i).getAttributes().item(0).getNodeValue();
			aux.setIdAtributo(Integer.parseInt(id));
			ta=this.consultarAtributo(aux);
			vectorDeTransfers.add(ta);
		}
		//devolvemos los atributos con todos sus datos.
		return vectorDeTransfers;
	}
	
	public String nombreDeAtributo(int identificador) {
		// Vector que devolveremos
		// Obtenemos los atributos y los vamos anadiendo
		NodeList lista=doc.getElementsByTagName("Attribute");
		int numAtributos = lista.getLength(); //vemos cuantos atributoss hay en el XML
		int id;

		for (int i=0;i<numAtributos;i++){
			//obtenemos el ID de cada atributo, lo consultamos y lo metemos en el vector.
			id= Integer.parseInt(lista.item(i).getAttributes().item(0).getNodeValue());
			if (identificador == id)
				return lista.item(i).getChildNodes().item(1).getTextContent();
		}
		return null;
	}
	
	public boolean uniqueAtributo(int identificador) {
		// Vector que devolveremos
		// Obtenemos los atributos y los vamos anadiendo
		NodeList lista=doc.getElementsByTagName("Attribute");
		int numAtributos = lista.getLength(); //vemos cuantos atributoss hay en el XML
		int id;

		for (int i=0;i<numAtributos;i++){
			//obtenemos el ID de cada atributo, lo consultamos y lo metemos en el vector.
			id= Integer.parseInt(lista.item(i).getAttributes().item(0).getNodeValue());
			if (identificador == id){
				if( Boolean.parseBoolean(lista.item(i).getChildNodes().item(9).getTextContent()))
					return true;
			}
		}
		return false;
	}
	
	
	// Metodos privados

	private Node dameNodoAtributo(int id) {
		Node AtributoBuscado = null;
		NodeList LC = doc.getElementsByTagName("Attribute");
		// Obtener el atributo id de un nodo Atributo
		boolean encontrado = false;
		int cont = 0;
		while ((!encontrado) && (cont < LC.getLength())) {
			// Obtenemos el nodo
			Node nodoAtributoActual = LC.item(cont);
			// Obtenemos sus atributos
			NamedNodeMap atributosNodoAtributoActual = nodoAtributoActual
					.getAttributes();
			// Obtenemos el id del Atributo
			int idNodoAtributoActual = Integer
					.parseInt(atributosNodoAtributoActual.item(0).getNodeValue());
			// Comparamos
			if (id == idNodoAtributoActual) {
				encontrado = true;
				AtributoBuscado = nodoAtributoActual;
			} else
				cont++;
		}
		return AtributoBuscado;
	}

	private TransferAtributo nodoAtributoATransferAtributo(Node nodo) {
		int id = dameAtributo(nodo);
		String nombre = dameValorDelElemento(dameNodoPedidoDeAtributo(nodo,
				"Name"));
		String dominio = dameValorDelElemento(dameNodoPedidoDeAtributo(nodo,
				"Dom"));
		boolean compuesto = Boolean
				.parseBoolean(dameValorDelElemento(dameNodoPedidoDeAtributo(
						nodo, "Composed")));
		boolean notnull = Boolean
				.parseBoolean(dameValorDelElemento(dameNodoPedidoDeAtributo(
						nodo, "NotNull")));
		boolean unique = Boolean
				.parseBoolean(dameValorDelElemento(dameNodoPedidoDeAtributo(
						nodo, "Unique")));
		boolean multivalorado = Boolean
				.parseBoolean(dameValorDelElemento(dameNodoPedidoDeAtributo(
						nodo, "Multivalued")));
		
		Point2D posicion=this.damePunto(dameValorDelElemento(dameNodoPedidoDeAtributo(nodo,"Position")));
		
		Vector listaC = nodoListaAObjetoLista(dameNodoPedidoDeAtributo(nodo,"ComponentList"), "SubAttribute");
		Vector listaR = nodoListaAObjetoLista(dameNodoPedidoDeAtributo(nodo,"AssertionList"),"Assertion");
		
		// Creamos el transfer
		/*TransferAtributo transfer = SingletonFactoriaTransfers
				.obtenerInstancia().generaTransferAtributo();*/
		TransferAtributo transfer = new TransferAtributo(c);
		transfer.setIdAtributo(id);
		transfer.setNombre(nombre);
		transfer.setCompuesto(compuesto);
		transfer.setNotnull(notnull);
		transfer.setUnique(unique);
		transfer.setDominio(dominio);
		transfer.setListaComponentes(listaC);
		transfer.setPosicion(posicion);
		transfer.setMultivalorado(multivalorado);
		transfer.setListaRestricciones(listaR);
		transfer.setEntidad_origenID(Integer.parseInt(dameValorDelElemento(dameNodoPedidoDeAtributo(nodo,"entidadOrigin_ID"))));
		transfer.setEntidad_origenName(dameValorDelElemento(dameNodoPedidoDeAtributo(nodo,"entidadOrigin_Name")));
		
		// Lo devolvemos
		return transfer;
	}

	@SuppressWarnings("unchecked")
	private Vector nodoListaAObjetoLista(Node nodo,String tipoLista){
		//tipoLista puede ser : SubAtributo y Valor 
		//Resultado que devolveremos
		Vector lista = new Vector();
		// Sacamos la lista de hijos
		NodeList LD = nodo.getChildNodes();
		// Buscamos en la lista los nodos del tipo que queremos (tipolista)
		int cont = 0;
		while(cont<LD.getLength()){
			Node aux = LD.item(cont);
			if (aux.getNodeName()==tipoLista){
				lista.addElement(dameValorDelElemento(aux));
			}
			cont++;
		}
		// Devolvemos la lista
		return lista;
	}
	private String dameValorDelElemento(Node elemento) {
		return elemento.getFirstChild().getNodeValue().toString();
	}

	private void ponValorAElemento(Node elemento, String valor) {
		elemento.getFirstChild().setNodeValue(valor);
	}

	private int dameAtributo(Node Atributo) {
		return Integer.parseInt(Atributo.getAttributes().item(0).getNodeValue());
	}

	private Node dameNodoPedidoDeAtributo(Node Atributo, String elemento) {
		Node nodoBuscado = null;
		// Generamos los hijos de Atributo
		NodeList hijos = Atributo.getChildNodes();
		// Buscamos hasta encontar un elemento "elemento"
		int cont = 0;
		Node aux = hijos.item(0);
		if (aux!=null)
		while (!aux.getNodeName().equals((elemento))&&(cont<hijos.getLength())) {
			aux = hijos.item(cont++);
		}
		// Ya tenemos en aux el nodoBuscado
		nodoBuscado = aux;
		// Lo devolvemos
		return nodoBuscado;
	}

	private Point2D damePunto(String posicion){
		Point2D p = new Point2D.Double();
		String primero,segundo;
		int coma =posicion.indexOf(",");
		primero = posicion.substring(0,coma);
		segundo = posicion.substring(coma+1, posicion.length());
		p.setLocation(Double.parseDouble(primero),Double.parseDouble(segundo));
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
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(
					null,
					Lenguaje.text(Lenguaje.ERROR)+":\n" +
					Lenguaje.text(Lenguaje.UNESPECTED_XML_ERROR)+" \""+ path+".xml\"",
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
		this.path = this.path.replace("%20"," ");
		FileWriter f = null;
		/*debido a que la funcion FileWriter da un error de acceso
		 * de vez en cuando, forzamos su ejecucion hasta que funcione correctamente*/
		boolean centinela=true;
		while (centinela==true){
			try {
				f = new FileWriter(this.path);
				centinela =false;
			} catch (IOException e) {
				centinela=true;
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

