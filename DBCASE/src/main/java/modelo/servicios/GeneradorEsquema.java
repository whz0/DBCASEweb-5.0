package modelo.servicios;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import controlador.Controlador;
import controlador.TC;
import modelo.conectorDBMS.ConectorDBMS;
import modelo.conectorDBMS.FactoriaConectores;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferConexion;
import modelo.transfers.TransferDominio;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import persistencia.DAOAtributos;
import persistencia.DAODominios;
import persistencia.DAOEntidades;
import persistencia.DAORelaciones;
import persistencia.EntidadYAridad;
import vista.componentes.MyFileChooser;

@SuppressWarnings({"unchecked","rawtypes"})
public class GeneradorEsquema {
	protected Controlador controlador;
	//atributos para la generacion de los modelos
	private String sqlHTML="";
	private String mr="";
	private Boolean hayIR = false;
	
    private MessageSource msgSrc;
    
    private Locale loc = LocaleContextHolder.getLocale();
	private TransferConexion conexionScriptGenerado = null;
	private RestriccionesPerdidas restriccionesPerdidas = new RestriccionesPerdidas();
	//aqui se almacenaran las tablas ya creadas, organizadas por el id de la entidad /relacion (clave) y con el objeto tabla como valor.
	private Hashtable<Integer,Tabla> tablasEntidades=new Hashtable<Integer,Tabla>();
	private Hashtable<Integer,Tabla> tablasRelaciones=new Hashtable<Integer,Tabla>();
	private Vector<Tabla> tablasMultivalorados=new Vector<Tabla>();
	private Hashtable<Integer,Enumerado> tiposEnumerados = new Hashtable<Integer,Enumerado>();
	private ValidadorBD validadorBD;
	
	public GeneradorEsquema(MessageSource messageSource) {
		this.msgSrc = messageSource;
	}
	
	public GeneradorEsquema() {
	}
	
	protected String getTraduction(String textLang) {
		return this.msgSrc.getMessage(textLang, null, this.loc)+"";
	}
	
	protected boolean estaEnVectorDeEnteros(Vector sinParam, int valor){
		int i=0;
		boolean encontrado=false;
		int elem=0;
		while(i<sinParam.size() && !encontrado){
			elem= this.objectToInt(sinParam.elementAt(i));
			if(elem==valor) encontrado = true;
			i++;
		}
		return encontrado;
	}

	//metodos de recorrido de los daos para la creacion de las tablas.
	private void generaTablasEntidades(){
		DAOEntidades daoEntidades= new DAOEntidades(controlador.getPath());
		Vector<TransferEntidad> entidades= daoEntidades.ListaDeEntidades();

		//recorremos las entidades generando las tablas correspondientes.
		for (int i=0;i<entidades.size();i++){
			Vector<TransferAtributo>multivalorados=new Vector<TransferAtributo>();
			TransferEntidad te=entidades.elementAt(i);
			// No se añaden a la tabla las agregaciones
			if(te.getIdEntidad() >=1000){
				Tabla tabla = new Tabla(te.getNombre(),te.getListaRestricciones(), controlador);
				Vector<TransferAtributo> atribs=this.dameAtributosEnTransfer(te.getListaAtributos());
				for(String rest : (Vector<String>)te.getListaRestricciones())
					restriccionesPerdidas.add(new restriccionPerdida(te.getNombre(), rest, restriccionPerdida.TABLA));
				//recorremos los atributos aniadiendolos a la tabla
				for (int j=0;j<atribs.size();j++){
					TransferAtributo ta=atribs.elementAt(j);
					if(ta.getUnique())
						restriccionesPerdidas.add(new restriccionPerdida(te.getNombre(), ta+" "+this.msgSrc.getMessage("textosId.isUnique", null, this.loc), restriccionPerdida.TABLA));
					if (ta.getCompuesto())
						tabla.aniadeListaAtributos(this.atributoCompuesto(ta, te.getNombre(),""),te.getListaRestricciones(),tiposEnumerados);
					else if (ta.isMultivalorado()) multivalorados.add(ta);
					else{
						tabla.aniadeAtributo(ta.getNombre(), ta.getDominio(),te.getNombre(), tiposEnumerados,ta.getListaRestricciones(), ta.getUnique(), ta.getNotnull());
						for(String rest : (Vector<String>)ta.getListaRestricciones())
							restriccionesPerdidas.add(new restriccionPerdida(te.getNombre(), rest, restriccionPerdida.TABLA));
					}
				}
				// Anadimos las claves a la relacion

				//aniadimos las claves primarias o logeneraTablasEntidades discriminantes si la entidad es debil.
				Vector<TransferAtributo> claves=this.dameAtributosEnTransfer(te.getListaClavesPrimarias());
				for (int c=0;c<claves.size();c++){
					TransferAtributo ta=claves.elementAt(c);
					if (ta.isMultivalorado()) multivalorados.add(ta);
					else
						if (ta.getCompuesto())
							tabla.aniadeListaClavesPrimarias(this.atributoCompuesto(ta,te.getNombre(),""));

						else //si es normal, lo aniadimos como clave primaria.
							tabla.aniadeClavePrimaria(ta.getNombre(),ta.getDominio(),te.getNombre(),ta.getEntidad_origenName());
				}

				//aniadimos a las tablas del sistema.
				tablasEntidades.put(te.getIdEntidad(),tabla);
				//tratamos los multivalorados que hayan surgido en el proceso.
				for(int mul=0;mul<multivalorados.size();mul++){
					TransferAtributo multi=multivalorados.elementAt(mul);
					this.atributoMultivalorado(multi, te.getIdEntidad());
				}

				// Establecimiento de uniques
				Vector<String> listaUniques = te.getListaUniques();
				for (int m = 0; m < listaUniques.size(); m++) tabla.getUniques().add(listaUniques.get(m));
			}
		}
	}
	
	
	private void generaTablasRelaciones(String sqlType) {
		DAORelaciones daoRelaciones = new DAORelaciones(controlador.getPath());
		Vector<TransferRelacion> relaciones = daoRelaciones.ListaDeRelaciones();

		// recorremos las relaciones creando sus tablas, en funcion de su tipo.
		for (int i = 0; i < relaciones.size(); i++) {
			TransferRelacion tr = relaciones.elementAt(i);
			Vector<TransferAtributo> multivalorados = new Vector<TransferAtributo>();
			// si es una relacion normal, aniadiremos los atributos propios y las
			// claves de las entidades implicadas.
			Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
			for(String rest : (Vector<String>)tr.getListaRestricciones())
				restriccionesPerdidas.add(new restriccionPerdida(tr.getNombre(), rest, restriccionPerdida.TABLA));
			if (tr.getTipo().equalsIgnoreCase("Normal")) {
				// creamos la tabla
				Tabla tabla = new Tabla(tr.getNombre(), tr.getListaRestricciones(), controlador);
				// aniadimos los atributos propios.
				Vector<TransferAtributo> ats = this.dameAtributosEnTransfer(tr.getListaAtributos());
				for (int a = 0; a < ats.size(); a++) {
					TransferAtributo ta = ats.elementAt(a);
					if(ta.getUnique()) 
						restriccionesPerdidas.add(new restriccionPerdida(tr.getNombre(), ta+" "+this.msgSrc.getMessage("textosId.isUnique", null, this.loc), restriccionPerdida.TABLA));
					if (ta.getCompuesto())
						tabla.aniadeListaAtributos(this.atributoCompuesto(ta, tr.getNombre(), ""), ta.getListaRestricciones(), tiposEnumerados);
					else if (ta.isMultivalorado()) multivalorados.add(ta);
					else { 
						tabla.aniadeAtributo(ta.getNombre(), ta.getDominio(), tr.getNombre(), tiposEnumerados, ta.getListaRestricciones(), ta.getUnique(), ta.getNotnull());
						for(String rest : (Vector<String>)ta.getListaRestricciones())
							restriccionesPerdidas.add(new restriccionPerdida(tr.getNombre(), rest, restriccionPerdida.TABLA));
					}
				}

				// TRATAMIENTO DE ENTIDADES
				// Comprobar si todas las entidades estan con relacion 0..1 o 1..1
				boolean soloHayUnos = true;
				int k = 0;
				while (soloHayUnos && k<veya.size()){
					EntidadYAridad eya = veya.get(k);
					if (eya.getFinalRango() <= 1) k++;
					else soloHayUnos = false;
				}
				
				//Para cada entidad...
				boolean esLaPrimeraDel1a1 = true;
				for (int m = 0; m < veya.size(); m++){
					// Aniadir su clave primaria a la relacion (es clave foranea)
					EntidadYAridad eya = veya.elementAt(m);
					Tabla ent = tablasEntidades.get(eya.getEntidad());
					Vector<String[]> previasPrimarias;
					if (ent.getPrimaries().isEmpty()) 
						previasPrimarias = ent.getAtributos();
					else 
						previasPrimarias = ent.getPrimaries();
					
					//...pero antes renombrarla con el rol
					Vector<String[]> primarias = new Vector<String[]>();
					Vector<String> primariasEntidades = new Vector<>();
					String[] referenciadas = new String[previasPrimarias.size()];
					boolean tieneRol = false;
					for (int q=0; q<previasPrimarias.size(); q++){
						String[] clave = new String[5];
						clave[3]="0";
						clave[4]=eya.getPrincipioRango()==0?"0":"1";
						if (!eya.getRol().trim().equals("")){
							clave[0] = eya.getRol() + "_" + previasPrimarias.get(q)[0];
							tieneRol = true;
						}
						else clave[0] = previasPrimarias.get(q)[0];


						clave[1] = previasPrimarias.get(q)[1];
						clave[2] = previasPrimarias.get(q)[2];
						primarias.add(clave);
						
						/*if(previasPrimarias.get(q)[2].equals("agregacion"))
							primariasEntidades.add(previasPrimarias.get(q)[3]);
						else*/
							primariasEntidades.add(previasPrimarias.get(q)[2]);

						referenciadas[q] = previasPrimarias.get(q)[0];
					}
					
					tabla.aniadeListaAtributos(primarias, tr.getListaRestricciones(), tiposEnumerados);
					tabla.aniadeListaClavesForaneas(primarias, primariasEntidades, referenciadas, tieneRol);
					
					// Si es 0..1 o 1..1 poner como clave
					if (eya.getFinalRango() > 1) tabla.aniadeListaClavesPrimarias(primarias);
					else{
						if (soloHayUnos && esLaPrimeraDel1a1){
							tabla.aniadeListaClavesPrimarias(primarias);
							esLaPrimeraDel1a1 = false;
						}else if (soloHayUnos){
							for(String[] clave : (Vector<String[]>)ent.getPrimaries())
								restriccionesPerdidas.add(
										new restriccionPerdida(ent.getNombreTabla()+"_"+clave[0], tr.getNombre(), restriccionPerdida.CANDIDATA));
							String uniques = "";
							for (int q = 0; q < primarias.size(); q++){
								if (q == 0) uniques += primarias.get(q)[0];
								else uniques += ", " + primarias.get(q)[0];
							}
							uniques += "#" + ent.getNombreTabla();
							tabla.getUniques().add(uniques);
						}else if(eya.getPrincipioRango() == 1 && eya.getFinalRango() == Integer.MAX_VALUE)
							for(String[] clave : (Vector<String[]>)ent.getPrimaries())
								restriccionesPerdidas.add(
										new restriccionPerdida(ent.getNombreTabla()+"_"+clave[0], tr.getNombre(), restriccionPerdida.CANDIDATA));
									//	new restriccionPerdida(clave[0], tr.getNombre(), restriccionPerdida.CANDIDATA));
					}
					//crea las restricciones perdidas (cuando rangoIni > 1 o rangoFin < N) || rangoIni == 1
					if((eya.getPrincipioRango() > 0 && eya.getFinalRango() < Integer.MAX_VALUE && eya.getFinalRango() >1)||eya.getPrincipioRango()==1) {
						Tabla aux = tabla.creaClonSinAmbiguedadNiEspacios(sqlType);
						boolean recurs = false;
						Vector<String[]> a = tabla.getPrimaries();
						for(int j=0;j<a.size();j++) {
							if(j+1==a.size() && j>0) {
								if(a.get(j)[2].equals(a.get(j-1)[2])) {
									if(a.get(j)[0].split("_").length>1 && a.get(j)[0].split("_")[1].equals(a.get(j-1)[0].split("_")[1])) {
										Vector<String[]> b = new Vector<String[]>();
										b.add(a.get(j));
										aux.setPrimaries(b);
										recurs=true;
									}
								}
							}
						}
						restriccionesPerdidas.add(
									new restriccionPerdida(recurs?aux.restriccionIR(true,ent.getNombreTabla(),sqlType):tabla.restriccionIR(true,ent.getNombreTabla(),sqlType),ent.restriccionIR(false, "",sqlType), 
										eya.getPrincipioRango(), eya.getFinalRango(), restriccionPerdida.TOTAL));
					}
				}
				tablasRelaciones.put(tr.getIdRelacion(), tabla);
				for (int mul = 0; mul < multivalorados.size(); mul++) {
					TransferAtributo multi = multivalorados.elementAt(mul);
					this.atributoMultivalorado(multi, tr.getIdRelacion());
				}
			}

			// si no es normal
			else
			// si es del tipo IsA, actualizamos aniadiendo la clave del padre a
			// las tablas hijas.
			if (tr.isIsA()) {

				/*
				 * recorremos todas las entidades asociadas a la relacion.
				 * sabemos ademas, por criterios del disenio, que la primera
				 * entidad es siempre padre.
				 */
				EntidadYAridad padre = veya.firstElement();
				for (int e = 1; e < veya.size(); e++) {
					EntidadYAridad hija = veya.elementAt(e);
					// aniadimos la informacion de clave a las tablas hijas,
					// buscandolas en el sistema.
					tablasEntidades.get(hija.getEntidad()).aniadeListaAtributos(tablasEntidades.get(padre.getEntidad())
									.getPrimaries(), tr.getListaRestricciones(), tiposEnumerados);
					
					tablasEntidades.get(hija.getEntidad()).aniadeListaClavesPrimarias(
									tablasEntidades.get(padre.getEntidad()).getPrimaries());
					
					Vector<String[]> clavesPadre = tablasEntidades.get(padre.getEntidad()).getPrimaries();
					String[] referenciadas = new String[clavesPadre.size()];
					for (int q=0; q<clavesPadre.size(); q++){
						referenciadas[q] = clavesPadre.get(q)[0];
					}
					
					tablasEntidades.get(hija.getEntidad())
							.aniadeListaClavesForaneas(
									tablasEntidades.get(padre.getEntidad()).getPrimaries(),
									tablasEntidades.get(padre.getEntidad()).getNombreTabla(),referenciadas, false);
				}

			}
			// si es de tipo debil
			else {
				/*
				 * buscamos la entidad debil, que ya tiene tabla y le aniadimos
				 * los atributos de las entidades fuertes de las que dependa.
				 * Ademas los pondremos como claves foraneas. Contaremos, las
				 * entidades fuertes y las debiles que aparezcan, pues este sera
				 * el criterio a seguir a la hora de reasignar las claves.
				 */
				DAOEntidades daoEntidades = new DAOEntidades(controlador.getPath());
				Vector<TransferEntidad> fuertes = new Vector<TransferEntidad>();
				Vector<TransferEntidad> debiles = new Vector<TransferEntidad>();
				for (int s = 0; s < veya.size(); s++) {
					TransferEntidad aux = new TransferEntidad();
					EntidadYAridad eya = veya.elementAt(s);
					aux.setIdEntidad(eya.getEntidad());
					aux = daoEntidades.consultarEntidad(aux);
					if (aux.isDebil()) debiles.add(aux);
					else fuertes.add(aux);
				}
				// ahora recorremos las fuertes, sacando sus claves y
				// metiendolas en las debiles.
				for (int f = 0; f < fuertes.size(); f++) {
					TransferEntidad fuerte = fuertes.elementAt(f);
					Tabla tFuerte = tablasEntidades.get(fuerte.getIdEntidad());
					for (int d = 0; d < debiles.size(); d++) {
						TransferEntidad debil = debiles.elementAt(d);
						Tabla tDebil = tablasEntidades.get(debil.getIdEntidad());
						tDebil.aniadeListaAtributos(tFuerte.getPrimaries(),fuerte.getListaRestricciones(),tiposEnumerados);
						Vector<String[]> clavesFuerte = tFuerte.getPrimaries();
						String[] referenciadas = new String[clavesFuerte.size()];
						for (int q=0; q<clavesFuerte.size(); q++) referenciadas[q] = clavesFuerte.get(q)[0];
						tDebil.aniadeListaClavesForaneas(tFuerte.getPrimaries(),tFuerte.getNombreTabla(), referenciadas,false);
						tDebil.aniadeListaClavesPrimarias(tFuerte.getPrimaries());
					}
				}
			}
		}
	}
	
	private void generaTiposEnumerados(){
		DAODominios daoDominios= new DAODominios(controlador.getPath());
		Vector<TransferDominio> dominios = daoDominios.ListaDeDominios();

		//recorremos los dominios creando sus tipos enumerados
		for (int i=0;i<dominios.size();i++){
			TransferDominio td=dominios.elementAt(i);
			Enumerado enu = new Enumerado(td.getNombre(), td.getTipoBase());
			// Obtener todos sus posibles valores
			Vector<String> valores = td.getListaValores();
			for (int k=0; k<valores.size(); k++) enu.anadeValor(valores.get(k));
			// Insertar en la tabla Hash
			tiposEnumerados.put(td.getIdDominio(), enu);
		}
	}

	public void reset(){
		tablasEntidades.clear();
		tablasRelaciones.clear();
		tablasMultivalorados.clear();
		tiposEnumerados.clear();
		
		conexionScriptGenerado = null;
		sqlHTML="";
	}

	public String generaScriptSQL(TransferConexion conexion){
		reset(); 
		StringBuilder warnings = new StringBuilder();
		if (!validadorBD.validaBaseDeDatos(true, warnings)) return "";
		// Eliminar tablas anteriores, pero recordar que el modelo a ha sido validado
		reset();
		conexionScriptGenerado = conexion;
		
		// Cabeceras de los documentos
		sqlHTML=warnings.toString();
	
		// Creamos las tablas
		generaTiposEnumerados();
		generaTablasEntidades();
		generaTablasRelaciones(conexion.getRuta());
		//sacamos el codigo de cada una de ellas recorriendo las hashtables e imprimiendo.
		creaTablas(conexion);
		creaEnums(conexion);
		ponClaves(conexion);	
		ponRestricciones(conexion);
		return sqlHTML;
	}

	public void exportarCodigo(String text, boolean sql){
		if (!validadorBD.validaBaseDeDatos(false, new StringBuilder())){
			JOptionPane.showMessageDialog(null,
					this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
					this.msgSrc.getMessage("textosId.scriptError", null, this.loc),
					this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
					JOptionPane.PLAIN_MESSAGE);
				return;
			}
		if (text.isEmpty()){
			JOptionPane.showMessageDialog(null,
				this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
				this.msgSrc.getMessage("textosId.mustGenerateScript", null, this.loc),
				this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
				JOptionPane.PLAIN_MESSAGE);
			return;
		}
		text="# "+this.msgSrc.getMessage("textosId.scriptGenerated", null, this.loc)+"\n"+
		(sql?"# "+this.msgSrc.getMessage("textosId.syntax", null, this.loc) + ": " +conexionScriptGenerado.getRuta()+ "\n\n":"")+text;
		// Si ya se ha generado el Script
		MyFileChooser jfc = new MyFileChooser();
		jfc.setDialogTitle(this.msgSrc.getMessage("textosId.dbcase", null, this.loc));
		jfc.setCurrentDirectory(new File(System.getProperty("user.dir")+"/projects"));
		jfc.setFileFilter(new FileNameExtensionFilter("Text", "txt"));
		if(sql)jfc.setFileFilter(new FileNameExtensionFilter(this.msgSrc.getMessage("textosId.sqlFiles", null, this.loc), "sql"));
		int resul = jfc.showSaveDialog(null);
		if (resul == 0){
			File ruta = jfc.getSelectedFile();
			String filePath = ruta.getAbsolutePath();
			if(jfc.getFileFilter().getDescription().equals("Text") && !filePath.endsWith(".txt")) 
			    ruta = new File(filePath + ".txt");
			else if(jfc.getFileFilter().getDescription().equals("SQL Files") && !filePath.endsWith(".sql")) 
			    ruta = new File(filePath + ".sql");
			try {
				FileWriter file = new FileWriter(ruta);
				file.write(text);
				file.close();
				JOptionPane.showMessageDialog(
						null,
						this.msgSrc.getMessage("textosId.info", null, this.loc)+"\n"+    
						this.msgSrc.getMessage("textosId.okFile", null, this.loc)+"\n" +
						this.msgSrc.getMessage("textosId.file", null, this.loc)+": "+ruta,
						this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
						JOptionPane.PLAIN_MESSAGE);

			} catch (IOException e) {
				JOptionPane.showMessageDialog(null,
						this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
						this.msgSrc.getMessage("textosId.scriptError", null, this.loc),
						this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
						JOptionPane.PLAIN_MESSAGE);
			}	
		}
	}
	
	public Vector<TransferConexion> obtenerTiposDeConexion(){
		Vector<String> nombres = FactoriaConectores.obtenerTodosLosConectores();
		Vector<TransferConexion> conexiones;
		conexiones = new Vector<TransferConexion>();
		conexiones.clear();
		for (int i = 0; i < nombres.size(); i++)
			conexiones.add(new TransferConexion(i, nombres.get(i)));
		return conexiones;
	}


	public String[] ejecutarScriptEnDBMS_new(TransferConexion tc, String sql) {

		// Comprobaciones previas
		// if (tc.getTipoConexion() != conexionScriptGenerado.getTipoConexion()) {
		// 	return new String[] { this.msgSrc.getMessage("textosId.warning", null, this.loc)+".\n" +
		// 			this.msgSrc.getMessage("textosId.scriptGeneratedFor", null, this.loc)+": \n" +
		// 			"     " + conexionScriptGenerado.getRuta() + " \n" +
		// 			this.msgSrc.getMessage("textosId.conexionTypeIs", null, this.loc)+": \n" + 
		// 			"     " + tc.getRuta() + "\n" +
		// 			this.msgSrc.getMessage("textosId.possibleErrorScript", null, this.loc)+" \n" +
		// 			this.msgSrc.getMessage("textosId.shouldGenerateScript", null, this.loc)+" \n" +
		// 			this.msgSrc.getMessage("textosId.ofConexion", null, this.loc)+"\n"+
		// 			this.msgSrc.getMessage("textosId.continueAnyway", null, this.loc),String.valueOf(JOptionPane.CANCEL_OPTION)};
		// }
		
		// Ejecutar en DBMS
		System.out.println("Datos de conexion a la base de datos");
		System.out.println("------------------------------------");
		System.out.println("DBMS: " + tc.getRuta() + "(" + tc.getTipoConexion() + ")");
		System.out.println("Usuario: " + tc.getUsuario());
		// System.out.println("Password: " + tc.getPassword());
		
		System.out.println("Intentando conectar...");
		ConectorDBMS conector = FactoriaConectores.obtenerConector(tc.getTipoConexion());
		try {
			conector.abrirConexion(tc.getRuta(), tc.getUsuario(), tc.getPassword());
		} catch (SQLException e) {
			// Avisar por consola
			System.out.println("ERROR: No se pudo abrir una conexion con la base de datos");
			System.out.println("MOTIVO");
			System.out.println(e.getMessage());
			
			// Avisar por GUI
			return new String[] {
					this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
					this.msgSrc.getMessage("textosId.noDBConexion", null, this.loc)+" \n" +
					this.msgSrc.getMessage("textosId.reason", null, this.loc)+": \n" + e.getMessage(),
					this.msgSrc.getMessage("textosId.dbcase", null, this.loc), String.valueOf(JOptionPane.PLAIN_MESSAGE)};
		}
		String ordenActual = null;
		try {
			// Crear la base de datos
			conector.usarDatabase(tc.getDatabase());
			// Ejecutar cada orden
			
			String[] orden = sql.split(";");
			for (int i=0; i < orden.length; i++){
				if ((orden[i] != null) && (!orden[i].trim().equals("")) && (!orden[i].trim().equals("\n"))){
					ordenActual = orden[i].trim() + ";";
					
					// Eliminar los comentarios y lineas en blanco
					if (ordenActual.startsWith("--") && !ordenActual.contains("\n")) continue;
					while (ordenActual.startsWith("--") || ordenActual.startsWith("\n"))
						ordenActual = ordenActual.substring(ordenActual.indexOf("\n") + 1);
					// Ejecutar la orden
					conector.ejecutarOrden(ordenActual);	
				}
			}
		} catch (SQLException e) {	
			// Avisar por GUI
			return new String[] {
					this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
					this.msgSrc.getMessage("textosId.cantExecuteScript", null, this.loc)+" \n" +
					this.msgSrc.getMessage("textosId.enquiryError", null, this.loc)+": \n" + ordenActual + "\n" + 
					this.msgSrc.getMessage("textosId.reason", null, this.loc)+": \n" + e.getMessage(), String.valueOf(JOptionPane.PLAIN_MESSAGE)};
			
		}
		try {
			conector.cerrarConexion();
		} catch (SQLException e) {
			return new String[] {
					this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
					this.msgSrc.getMessage("textosId.cantCloseConexion", null, this.loc)+" \n" +
					this.msgSrc.getMessage("textosId.reason", null, this.loc)+" \n" + e.getMessage(),
					String.valueOf(JOptionPane.PLAIN_MESSAGE)};
		}
		System.out.println("Conexion cerrada correctamente");
		return new String[] {
				this.msgSrc.getMessage("textosId.info", null, this.loc)+"\n" +
				this.msgSrc.getMessage("textosId.okScriptExecut", null, this.loc),
				String.valueOf(JOptionPane.PLAIN_MESSAGE)};
	}

	public void ejecutarScriptEnDBMS(TransferConexion tc, String sql) {

		// Comprobaciones previas
		if (tc.getTipoConexion() != conexionScriptGenerado.getTipoConexion()) {
			int respuesta = JOptionPane.showConfirmDialog(null,
					this.msgSrc.getMessage("textosId.warning", null, this.loc)+".\n" +
					this.msgSrc.getMessage("textosId.scriptGeneratedFor", null, this.loc)+": \n" +
					"     " + conexionScriptGenerado.getRuta() + " \n" +
					this.msgSrc.getMessage("textosId.conexionTypeIs", null, this.loc)+": \n" + 
					"     " + tc.getRuta() + "\n" +
					this.msgSrc.getMessage("textosId.possibleErrorScript", null, this.loc)+" \n" +
					this.msgSrc.getMessage("textosId.shouldGenerateScript", null, this.loc)+" \n" +
					this.msgSrc.getMessage("textosId.ofConexion", null, this.loc)+"\n"+
					this.msgSrc.getMessage("textosId.continueAnyway", null, this.loc),
					this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (respuesta == JOptionPane.CANCEL_OPTION) return;
		}
		
		// Ejecutar en DBMS
		System.out.println("Datos de conexion a la base de datos");
		System.out.println("------------------------------------");
		System.out.println("DBMS: " + tc.getRuta() + "(" + tc.getTipoConexion() + ")");
		System.out.println("Usuario: " + tc.getUsuario());
		// System.out.println("Password: " + tc.getPassword());
		
		System.out.println("Intentando conectar...");
		ConectorDBMS conector = FactoriaConectores.obtenerConector(tc.getTipoConexion());
		try {
			conector.abrirConexion(tc.getRuta(), tc.getUsuario(), tc.getPassword());
		} catch (SQLException e) {
			// Avisar por consola
			System.out.println("ERROR: No se pudo abrir una conexion con la base de datos");
			System.out.println("MOTIVO");
			System.out.println(e.getMessage());
			
			// Avisar por GUI
			JOptionPane.showMessageDialog(null,
					this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
					this.msgSrc.getMessage("textosId.noDBConexion", null, this.loc)+" \n" +
					this.msgSrc.getMessage("textosId.reason", null, this.loc)+": \n" + e.getMessage(),
					this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
					JOptionPane.PLAIN_MESSAGE);
			// Terminar
			return;
		}
		String ordenActual = null;
		try {
			// Crear la base de datos
			conector.usarDatabase(tc.getDatabase());
			// Ejecutar cada orden
			
			String[] orden = sql.split(";");
			for (int i=0; i < orden.length; i++){
				if ((orden[i] != null) && (!orden[i].trim().equals("")) && (!orden[i].trim().equals("\n"))){
					ordenActual = orden[i].trim() + ";";
					
					// Eliminar los comentarios y lineas en blanco
					if (ordenActual.startsWith("--") && !ordenActual.contains("\n")) continue;
					while (ordenActual.startsWith("--") || ordenActual.startsWith("\n"))
						ordenActual = ordenActual.substring(ordenActual.indexOf("\n") + 1);
					// Ejecutar la orden
					conector.ejecutarOrden(ordenActual);	
				}
			}
		} catch (SQLException e) {	
			// Avisar por GUI
			JOptionPane.showMessageDialog(null,
					this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
					this.msgSrc.getMessage("textosId.cantExecuteScript", null, this.loc)+" \n" +
					this.msgSrc.getMessage("textosId.enquiryError", null, this.loc)+": \n" + ordenActual + "\n" + 
					this.msgSrc.getMessage("textosId.reason", null, this.loc)+": \n" + e.getMessage(),
					this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
					JOptionPane.PLAIN_MESSAGE);
			
			// Terminar
			return;
		}
		try {
			conector.cerrarConexion();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null,
					this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
					this.msgSrc.getMessage("textosId.cantCloseConexion", null, this.loc)+" \n" +
					this.msgSrc.getMessage("textosId.reason", null, this.loc)+" \n" + e.getMessage(),
					this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
					JOptionPane.PLAIN_MESSAGE);
			return;
		}
		System.out.println("Conexion cerrada correctamente");
		JOptionPane.showMessageDialog(null,
				this.msgSrc.getMessage("textosId.info", null, this.loc)+"\n" +
				this.msgSrc.getMessage("textosId.okScriptExecut", null, this.loc),
				this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
				JOptionPane.PLAIN_MESSAGE);
	}
	
	private void creaTablas(TransferConexion conexion){
		sqlHTML +="<div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.tables", null, this.loc)+"</p>";

		String tablasEntidad = "";
		String tablasEntidadHTML = "";
//cambiado
		Iterator tablasE=tablasEntidades.values().iterator();
		while (tablasE.hasNext()){
			Tabla t =(Tabla)tablasE.next();
			if(!t.getNombreTabla().equals("agregacion")){
				if (esPadreEnIsa(t,conexion.getRuta())){
					tablasEntidadHTML = t.codigoHTMLCreacionDeTabla(conexion) + tablasEntidadHTML;
					tablasEntidad = t.codigoEstandarCreacionDeTabla(conexion) + tablasEntidad;
				}else{
					tablasEntidadHTML+=t.codigoHTMLCreacionDeTabla(conexion);
					tablasEntidad+=t.codigoEstandarCreacionDeTabla(conexion);
				}
			}
		}
		sqlHTML += tablasEntidadHTML;
		Iterator tablasM=tablasMultivalorados.iterator();
		while (tablasM.hasNext()){
			Tabla t =(Tabla)tablasM.next();
			if(!t.getNombreTabla().equals("agregacion"))
				sqlHTML+=t.codigoHTMLCreacionDeTabla(conexion);
		}
		Iterator tablasR=tablasRelaciones.values().iterator();
		while (tablasR.hasNext()){
			Tabla t =(Tabla)tablasR.next();
			if(!t.getNombreTabla().equals("agregacion"))
				sqlHTML+=t.codigoHTMLCreacionDeTabla(conexion);
		}
		sqlHTML+="<p></p></div>";
	}
	
	private boolean esPadreEnIsa(Tabla tabla,String sqlType){
		boolean encontrado = false;
		DAORelaciones daoRelaciones = new DAORelaciones(controlador.getPath());
		Vector<TransferRelacion> relaciones = daoRelaciones.ListaDeRelaciones();

		// recorremos las relaciones buscando las isa
		int i = 0;
		while (i < relaciones.size() && !encontrado) {
			TransferRelacion tr = relaciones.elementAt(i);

			if (tr.isIsA()) {
				// Obtener ID del padre
				Vector<EntidadYAridad> veya = tr.getListaEntidadesYAridades();
				int  idPadre = veya.firstElement().getEntidad();
				
				DAOEntidades daoEntidades= new DAOEntidades(controlador.getPath());
				TransferEntidad te = new TransferEntidad();
				te.setIdEntidad(idPadre);
				te = daoEntidades.consultarEntidad(te);
				
				Tabla t = new Tabla(te.getNombre(), te.getListaRestricciones(), controlador);
				t = t.creaClonSinAmbiguedadNiEspacios(sqlType);
				encontrado = t.getNombreTabla().replace("`","").equalsIgnoreCase(tabla.getNombreTabla());
			}
			i++;
		}
		return encontrado;
	}
	
	private void creaEnums(TransferConexion conexion){
		sqlHTML+="<div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.typesSection", null, this.loc)+"</p>";
		
		Iterator<Enumerado> tablasD=tiposEnumerados.values().iterator();
		while (tablasD.hasNext()){
			Enumerado e =tablasD.next();
			sqlHTML+=e.codigoHTMLCreacionDeEnum(conexion);
		}
		sqlHTML+="<p></p></div>";
	}
	
	private void ponRestricciones(TransferConexion conexion){
		sqlHTML+="<div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.constraintsSection", null, this.loc)+"</p>";
		
		Iterator tablasE=tablasEntidades.values().iterator();
		while (tablasE.hasNext()){
			Tabla t =(Tabla)tablasE.next();
			if(!t.getNombreTabla().equals("agregacion"))
				sqlHTML += t.codigoHTMLRestriccionesDeTabla(conexion);
			
		}
		
		// Escribir restricciones de relacion
		Iterator tablasR=tablasRelaciones.values().iterator();
		while (tablasR.hasNext()){
			Tabla t =(Tabla)tablasR.next();
			if(!t.getNombreTabla().equals("agregacion"))
				sqlHTML += t.codigoHTMLRestriccionesDeTabla(conexion);
		}
		
		// Escribir restricciones de atributo
		Iterator tablasA=tablasMultivalorados.iterator();
		while (tablasA.hasNext()){
			Tabla t =(Tabla)tablasA.next();
			if(!t.getNombreTabla().equals("agregacion"))
				sqlHTML += t.codigoHTMLRestriccionesDeTabla(conexion);
		}
		sqlHTML+="<p></p></div>";
	}
	
	private void ponClaves(TransferConexion conexion){
		sqlHTML+="<div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.keysSection", null, this.loc)+"</p>";
		
		String restEntidad = "";
		String restEntidadHTML = "";

		Iterator tablasE=tablasEntidades.values().iterator();
		while (tablasE.hasNext()){
			Tabla t =(Tabla)tablasE.next();
			if(!t.getNombreTabla().equals("agregacion")){
				if (esPadreEnIsa(t,conexion.getRuta() ) || t.getForeigns().size()==0){
					restEntidadHTML = t.codigoHTMLClavesDeTabla(conexion) + restEntidadHTML;
					restEntidad = t.codigoEstandarClavesDeTabla(conexion) + restEntidad;
				}else{
					restEntidadHTML+=t.codigoHTMLClavesDeTabla(conexion);
					restEntidad+=t.codigoEstandarClavesDeTabla(conexion);
				}
			}
		}
		
		sqlHTML += restEntidadHTML;
		
		Iterator tablasR=tablasRelaciones.values().iterator();
		while (tablasR.hasNext()){
			Tabla t =(Tabla)tablasR.next();
			sqlHTML+=t.codigoHTMLClavesDeTabla(conexion);
		}
		
		Iterator tablasM=tablasMultivalorados.iterator();
		while (tablasM.hasNext()){
			Tabla t =(Tabla)tablasM.next();
			sqlHTML+=t.codigoHTMLClavesDeTabla(conexion);
		}
		sqlHTML+="<p></p></div>";
	}
	
	public String restriccionesPerdidas() {
		return restriccionesPerdidas.toString(this.msgSrc.getMessage("textosId.candidateKeys", null, this.loc),this.msgSrc.getMessage("textosId.writeNumbersRelation", null, this.loc),this.msgSrc.getMessage("textosId.constraintsSection", null, this.loc));
	}
	
	public String restriccionesIR() {
		String mr= "";
		mr+=generaIR(tablasEntidades.values().iterator());
		mr+=generaIR(tablasRelaciones.values().iterator());
		mr+=generaIR(tablasMultivalorados.iterator());
		if(!mr.isEmpty())this.hayIR = true;
		return mr;
	}
	
	/*
	 * Genera las restricciones de integridadIR dado un iterador de tabla
	 * */
	private String generaIR(Iterator<Tabla> tabla) {
		String code= "";
		while (tabla.hasNext()){
			Tabla t =(Tabla)tabla.next();
			Vector<String[]> foreigns = t.getForeigns();
			Vector<String[]> primaries = t.getPrimaries();
			boolean abierto = false;
			String claves="", valores = "";
			for (int j=0;j<foreigns.size();j++) {
				if(!abierto) {
					code+="<p>";
					abierto=true;
				}

				claves+= t.getNombreTabla()+"."+foreigns.elementAt(j)[0];
				valores+=foreigns.elementAt(j)[2];
				if(foreigns.size()-j>1) {
					if(foreigns.elementAt(j+1)[3]!=foreigns.elementAt(j)[3] || foreigns.elementAt(j+1)[2].equals(foreigns.elementAt(j)[2])) {
						code+=claves+" -> "+valores+"</p>";
						abierto = false;claves="";valores="";
					}
					else {
						claves+=", ";
						valores+=", ";
					}
				}else {
					code+=claves+" -> "+valores+"</p>";
					abierto = false;claves="";valores="";
				}
			}
			
		}
		return code;
	}
	
	public void generaModeloRelacional(){
		reset();
		StringBuilder warnings = new StringBuilder();
		if (!validadorBD.validaBaseDeDatos(true, warnings)) return;
		restriccionesPerdidas = new RestriccionesPerdidas();
		generaTablasEntidades();
		generaTablasRelaciones("default");
		mr = warnings.toString();
		mr += "<div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.relations", null, this.loc)+"</p>";
		Iterator tablasE = tablasEntidades.values().iterator();
		while (tablasE.hasNext()){
			Tabla t =(Tabla)tablasE.next();
			mr+=t.modeloRelacionalDeTabla(true,"DEFAULT",true);
		}
		
		Iterator tablasR = tablasRelaciones.values().iterator();
		while (tablasR.hasNext()){
			Tabla t =(Tabla)tablasR.next();
			mr+=t.modeloRelacionalDeTabla(true,"DEFAULT",true);
		}
		
		Iterator tablasM = tablasMultivalorados.iterator();
		while (tablasM.hasNext()){
			Tabla t =(Tabla)tablasM.next();
			mr+=t.modeloRelacionalDeTabla(true,"DEFAULT",true);
		}
		mr += "<p></p></div><div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.ric", null, this.loc)+"</p>";
		mr += restriccionesIR();
		mr += "<p></p></div><div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.lostConstr", null, this.loc)+"</p>";
		mr += restriccionesPerdidas();
		mr += "<p></p></div>";
		controlador.mensajeDesde_SS(TC.SS_GeneracionModeloRelacional,mr);
	}

	//metodos auxiliares.
	/**
	 * Devuelve la lista de atributos con sus caracteristicas de manera recursiva profundizando en los atributos compuestos.
	 * @param ta El atributo compuesto a tratar.
	 * @param nombreEntidad el nombre de la entidad de la que proviene.
	 * @param procedencia es la cadena de nombres de los atributos padre. 
	 */
	private Vector<String[]> atributoCompuesto(TransferAtributo ta,String nombreEntidad,String procedencia) {		
		Vector<TransferAtributo> subs=this.dameAtributosEnTransfer(ta.getListaComponentes());	
		Vector <String[]> lista = new Vector<String[]>();
	
		for (int i=0; i<subs.size();i++){
			TransferAtributo aux=subs.elementAt(i);
			if (aux.getCompuesto()){
				//caso recursivo
				String p="";
				if (procedencia!="") p=procedencia+ta.getNombre()+"_";
				else p=ta.getNombre()+"_";
				lista.addAll((Collection)this.atributoCompuesto(aux, nombreEntidad,p));					
			}
			else{
				//caso base
			String[]trio = new String[3];
			trio[0]=procedencia+ta.getNombre()+"_"+aux.getNombre();
		    trio[1]=aux.getDominio();
		    trio[2]=nombreEntidad;
			lista.add(trio);
			}
		}
		return lista;
	}

	/**
	 * 
	 * @param ta El atributo multivalorado en cuestion
	 * @param idEntidad El identificador de la entidad a la que pertenece.
	 */
	private void atributoMultivalorado(TransferAtributo ta, int idEntidad){
		// sacamos la tabla de la entidad propietaria del atributo.
		Tabla tablaEntidad = tablasEntidades.get(idEntidad);
		
		//creamos la tabla.
		Tabla tablaMulti = new Tabla(tablaEntidad.getNombreTabla() + "_" + ta.getNombre(), ta.getListaRestricciones(), controlador);

		// aniadimos el campo del atributo, incluso teniendo en cuenta que sea
		// compuesto.
		if (ta.getCompuesto())
			tablaMulti.aniadeListaAtributos(this.atributoCompuesto(ta,
					tablaEntidad.getNombreTabla(), ""),ta.getListaRestricciones(), tiposEnumerados);
		else tablaMulti.aniadeAtributo(ta.getNombre(), ta.getDominio(),
					tablaEntidad.getNombreTabla(), tiposEnumerados, ta.getListaRestricciones(),ta.getUnique(), ta.getNotnull());
		tablaMulti.aniadeListaAtributos(tablaEntidad.getPrimaries(), ta.getListaRestricciones(),tiposEnumerados);
		
		Vector<String[]> clavesEntidad = tablaEntidad.getPrimaries();
		String[] referenciadas = new String[clavesEntidad.size()];
		for (int q=0; q<clavesEntidad.size(); q++) referenciadas[q] = clavesEntidad.get(q)[0];
		
		tablaMulti.aniadeListaClavesForaneas(tablaEntidad.getPrimaries(),tablaEntidad.getNombreTabla(), referenciadas, false);
		tablasMultivalorados.add(tablaMulti);
		for(String rest : (Vector<String>)ta.getListaRestricciones())
			restriccionesPerdidas.add(new restriccionPerdida(tablaMulti.getNombreTabla(), rest, restriccionPerdida.TABLA));
	}

	protected int objectToInt(Object ob){
		return Integer.parseInt((String)ob);
	}

	protected  Vector<TransferAtributo> dameAtributosEnTransfer(Vector sinParam){
		DAOAtributos daoAtributos= new DAOAtributos(controlador);
		Vector<TransferAtributo> claves= new Vector<TransferAtributo>(); 
		TransferAtributo aux = new TransferAtributo(controlador);
		for (int i=0; i<sinParam.size();i++){
			aux.setIdAtributo(this.objectToInt(sinParam.elementAt(i)));
			aux=daoAtributos.consultarAtributo(aux);
			claves.add(aux);
		}
		return claves;	
	}	

	public String[] compruebaConexionNew(TransferConexion tc){
		System.out.println("Datos de conexion a la base de datos");
		System.out.println("------------------------------------");
		System.out.println("DBMS: " + tc.getRuta() + "(" + tc.getTipoConexion() + ")");
		System.out.println("Usuario: " + tc.getUsuario());
		System.out.println("Intentando conectar...");
		ConectorDBMS conector = FactoriaConectores.obtenerConector(tc.getTipoConexion());
		try {
			conector.abrirConexion(tc.getRuta(), tc.getUsuario(), tc.getPassword());
			conector.cerrarConexion();
		} catch (SQLException e) { 
			return new String[] {this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
				this.msgSrc.getMessage("textosId.noDBConexion", null, this.loc)+" \n" +
				this.msgSrc.getMessage("textosId.reason", null, this.loc)+": \n" + e.getMessage(),"0"};
		}
		return new String[] {this.msgSrc.getMessage("textosId.okScriptExecut", null, this.loc),"1"};
	}

	public void compruebaConexion(TransferConexion tc){
		System.out.println("Datos de conexion a la base de datos");
		System.out.println("------------------------------------");
		System.out.println("DBMS: " + tc.getRuta() + "(" + tc.getTipoConexion() + ")");
		System.out.println("Usuario: " + tc.getUsuario());
		System.out.println("Intentando conectar...");
		ConectorDBMS conector = FactoriaConectores.obtenerConector(tc.getTipoConexion());
		try {
			conector.abrirConexion(tc.getRuta(), tc.getUsuario(), tc.getPassword());
			conector.cerrarConexion();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null,this.msgSrc.getMessage("textosId.error", null, this.loc)+".\n" +
				this.msgSrc.getMessage("textosId.noDBConexion", null, this.loc)+" \n" +
				this.msgSrc.getMessage("textosId.reason", null, this.loc)+": \n" + e.getMessage(),
				this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
				JOptionPane.PLAIN_MESSAGE);
			return;
		}
		JOptionPane.showMessageDialog(null,
			this.msgSrc.getMessage("textosId.okScriptExecut", null, this.loc),
			this.msgSrc.getMessage("textosId.dbcase", null, this.loc),
			JOptionPane.PLAIN_MESSAGE);
		return;
	}

	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
		this.validadorBD = new ValidadorBD(this.msgSrc);//this.validadorBD = ValidadorBD.getInstancia();
		this.validadorBD.setControlador(controlador);
	}
	/*
		DBASEWEB V.3
		David Conde Cubas 
	*/
	//
	//SQLTYPE --> INDICA EL TIPO DE LANGUAGE SQL SELECCIONADO EN LA VISTA
	//SCRIPTSQL --> SI ES TRUE INDICA QUE ES UN ESQUEMA FISICO, SE DEBEN OCULTAR LOS ASTERISCOS Y EJECUTAR EL METODO QUE OBTIENE LAS QUERIES DESDE EL DIAGRAMA EJECUTADO.
	public String generaModeloRelacional_v3(String sqlType,boolean scriptSQL) {
		reset();
		StringBuilder warnings = new StringBuilder();
		//comprueba los errores posibles en el diagrama (lña mayoria de ellos ya se comprueban en vista igualmente)
		if (!validadorBD.validaBaseDeDatos(true, warnings))
			return warnings.toString();
		
		//ESTO GENERA UN CODIGO HTML QUE SE DEVUELVE A LA VISTA PARA PROCEDER INSERTARLO EN UN DIV
		restriccionesPerdidas = new RestriccionesPerdidas();
		generaTablasEntidades();
		generaTablasRelaciones(sqlType);
		mr = warnings.toString();
		mr += "<div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.relations", null, this.loc)+"</p>";
		Iterator tablasE = tablasEntidades.values().iterator();
		while (tablasE.hasNext()) {
			Tabla t = (Tabla) tablasE.next();
			mr += t.modeloRelacionalDeTabla(true,sqlType, scriptSQL);
		}

		Iterator tablasR = tablasRelaciones.values().iterator();
		while (tablasR.hasNext()) {
			Tabla t = (Tabla) tablasR.next();
			mr += t.modeloRelacionalDeTabla(true,sqlType, scriptSQL);
		}

		Iterator tablasM = tablasMultivalorados.iterator();
		while (tablasM.hasNext()) {
			Tabla t = (Tabla) tablasM.next();
			mr += t.modeloRelacionalDeTabla(true,sqlType, scriptSQL);
		}
		mr += "<p></p></div><div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.ric", null, this.loc)+"</p>";
		mr += restriccionesIR();
		mr += "<p></p></div><div class='pl-1 pt-1 pr-1 alert alert-light'><p class='h5 text-dark font-weight-bold'>"+this.msgSrc.getMessage("textosId.lostConstr", null, this.loc)+"</p>";
		mr += restriccionesPerdidas();
		mr += "<p></p></div>";
		
		//MR CONTIENE EL RESULTADO FINAL DE LA EJECUCIÓN
		return mr;
	}
}