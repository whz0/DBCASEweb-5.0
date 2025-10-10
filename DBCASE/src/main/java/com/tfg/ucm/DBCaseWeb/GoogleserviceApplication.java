package com.tfg.ucm.DBCaseWeb;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import controlador.Controlador;
import controlador.TC;
import modelo.servicios.DataAtributoEntidadOrigen;
import modelo.servicios.GeneradorEsquema;
import modelo.transfers.DataAttribute;
import modelo.transfers.Edge;
import modelo.transfers.Generic;
import modelo.transfers.Node;
import modelo.transfers.TipoDominio;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferConexion;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import modelo.conectorDBMS.FactoriaConectores;
import java.util.UUID;

@Controller
@RestController
@SpringBootApplication
public class GoogleserviceApplication {

	/*
	 * Return traductions
	 * CONTROLADOR ENCARGADO DE CONECTAR VISTA WEB CON APLICACION JAVA.
	 
		DBASEWEB V.3

	*/
	@Autowired
	private	MessageSource messageSource ;

   public static void main(String[] args) {
      SpringApplication.run(GoogleserviceApplication.class, args);
   }
   @RequestMapping(value = "/user")
   public Principal user(Principal principal) {
      return principal;
   }
//test
   @GetMapping("/lang")
   public RedirectView redirectCookieLanguage(RedirectAttributes attributes, HttpServletResponse response,@RequestParam String lang) {
	   Cookie cookie = new Cookie("language", lang);
       response.addCookie(cookie);
       attributes.addAttribute("idioma", lang);
       return new RedirectView("inicio");
   }

   @GetMapping("/theme")
   public RedirectView redirectCookieTheme(RedirectAttributes attributes, HttpServletResponse response,@RequestParam String theme) {
	   Cookie cookie = new Cookie("theme", theme);
       response.addCookie(cookie);
       return new RedirectView("inicio");
   }

   @GetMapping("/")
   public RedirectView redirectCookie(Principal principal, RedirectAttributes attributes, @CookieValue(name = "language", defaultValue = "es") String lang) {
	   if(principal != null) {
		   attributes.addAttribute("idioma", lang);
		   return new RedirectView("inicio");
	   }else {
		   return new RedirectView("index");
	   }

   }

   @RequestMapping(value = "/index")
   public ModelAndView login(Model model, Principal principal) {
	   ModelAndView mav = new ModelAndView();
	   if(principal != null) {
		   mav.setViewName("inicio");
		   model.addAttribute("perfil", principal); 	
	   }
		else
		   mav.setViewName("index");
		return mav;
   }

   @RequestMapping(value="/inicio", method=RequestMethod.GET)
	public ModelAndView inicio(Model model ,Principal principal, @CookieValue(name = "language", defaultValue = "es") String lang, @CookieValue(name = "theme", defaultValue = "dark") String theme) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("inicio");
		model.addAttribute("perfil", principal);
		model.addAttribute("theme", theme);
		model.addAttribute("language", lang);
		return mav;
	}

   // METODO ENCARGADO DE REALIZAR LA COMPROBACION DE CONEXION POSIBLE AL DBMS
	@RequestMapping(value = "/checkConnection", method = RequestMethod.POST)
	public String checkConnection(@RequestBody Generic r, HttpServletRequest req, HttpServletResponse resp) {
		//INFORMACION OBTENIDA DESDE LLAMADA AJAX VINCULADAS AL BOTON CHECK CONNECTION 
		String server=r.getData1(), port=r.getData2(), database=r.getData3(), user=r.getData4(), password=r.getData5(),tipo=r.getData6();
		String connectionString = "";
					
		switch(Integer.parseInt(tipo)){
			case (FactoriaConectores.CONECTOR_MYSQL):
				connectionString += "jdbc:mysql://";
				break;
			case (FactoriaConectores.CONECTOR_ORACLE):
				connectionString += "";
				break;
			case (FactoriaConectores.CONECTOR_MSACCESS_ODBC):
				connectionString += "jdbc:odbc:";
				break;
		}
		
		connectionString += server;
		if (!port.equalsIgnoreCase("")) connectionString += ":" + port;
		
		if (Integer.parseInt(tipo) != FactoriaConectores.CONECTOR_ORACLE) connectionString += "/";
		else connectionString += "#" + database;
		
		if (Integer.parseInt(tipo) == FactoriaConectores.CONECTOR_MSACCESS_MDB)
			connectionString = database;
		
		// Probar conexion
		TransferConexion tc = new TransferConexion(Integer.parseInt(tipo), connectionString, false, database, user, password); 
		Controlador c = null;
		try {
			c = new Controlador("debug");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.setTheServiciosSistema(new GeneradorEsquema(this.messageSource));

		Generic res = new Generic();
		String[] resData = c.getTheServiciosSistema().compruebaConexionNew(tc);
		res.setData1(resData[0].trim());
		res.setData2(resData[1]);

		return new Gson().toJson(res);
		//c.mensajeDesde_GUI(TC.GUIConexionDBMS_PruebaConexion, tc);
		//return "";
	}

	//METODO ENCARGADO DE EJECUTAR LAS QUERIES LAS CUALES VIENEN DESDE LA VISTA.
@RequestMapping(value = "/executeQueries", method = RequestMethod.POST)
	public String executeQueries(@RequestBody Generic r, HttpServletRequest req, HttpServletResponse resp) {
		//INFORMACION OBTENIDA DESDE LLAMADA AJAX VINCULADAS AL BOTON EXECUTE 
		String server=r.getData1(), port=r.getData2(), database=r.getData3(), user=r.getData4(), password=r.getData5(), tipo=r.getData6(),sql=r.getData7();
		String connectionString = "";
					
		switch(Integer.parseInt(tipo)){
			case (FactoriaConectores.CONECTOR_MYSQL):
				connectionString += "jdbc:mysql://";
				break;
			case (FactoriaConectores.CONECTOR_ORACLE):
				connectionString += "";
				break;
			case (FactoriaConectores.CONECTOR_MSACCESS_ODBC):
				connectionString += "jdbc:odbc:";
				break;
		}
		
		connectionString += server;
		if (!port.equalsIgnoreCase("")) connectionString += ":" + port;
		
		if (Integer.parseInt(tipo) != FactoriaConectores.CONECTOR_ORACLE) connectionString += "/";
		else connectionString += "#" + database;
		
		if (Integer.parseInt(tipo) == FactoriaConectores.CONECTOR_MSACCESS_MDB)
			connectionString = database;
		
		// Probar conexion
		TransferConexion tc = new TransferConexion(Integer.parseInt(tipo), connectionString, false, database, user, password); 
		Controlador c = null;
		try {
			c = new Controlador("debug");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		c.setTheServiciosSistema(new GeneradorEsquema(this.messageSource));

		Generic res = new Generic();
		sql = getText(sql);
		c.getTheServiciosSistema().ejecutarScriptEnDBMS_new(tc, sql);
		String[] resData = c.getTheServiciosSistema().compruebaConexionNew(tc);
		res.setData1(resData[0].trim());                                                                                                                                                                                                                                 
		res.setData2(resData[1]);

		return new Gson().toJson(res);
		//c.mensajeDesde_GUI(TC.GUIConexionDBMS_PruebaConexion, tc);
		//return "";
	}

	//METODO ENCARGADO DE LIMPIAR LAS QUERIES QUE VIENEN DE LA VISTA QUITANDO LOS DIVS, ASI COMO LOS CARACTERES INVALIDOS PARA LA UTILIZACION DE LAS QUERIES EN LA UTILIZACION EN LA INSERCION EN EL DBMS
	public String getText(String rawText) {
		String text = rawText;
		text = text.replaceAll("(?s)<div class=\"warning\">.*?</div>", "");
		text = text.replaceAll("\n","");
		text = text.replaceAll("<h2>","\n#");
		text = text.replaceAll("<p class=\"h5 text-dark font-weight-bold\">","\n#");
		text = text.replaceAll("</h2>", "\n");
		text = text.replaceAll("</p>", "\n");
		text = text.replaceAll("&gt;",">");
		text = text.replaceAll("&lt;","<");
		text = text.replaceAll("(?s)<!--.*?-->", "");
		text = text.replaceAll("&#186;", "º");
		text = text.replaceAll("&#199;", "Ç");
		text = text.replaceAll("&#191;", "¿");
		text = text.replaceAll("&#231;", "ç");
		text = text.replaceAll("&#193;", "Á");
		text = text.replaceAll("&#194;", "Â");
		text = text.replaceAll("&#195;", "Ã");
		text = text.replaceAll("&#201;", "É");
		text = text.replaceAll("&#202;", "Ê");
		text = text.replaceAll("&#205;", "Í");
		text = text.replaceAll("&#212;", "Ô");
		text = text.replaceAll("&#213;", "Õ");
		text = text.replaceAll("&#211;", "Ó");
		text = text.replaceAll("&#218;", "Ú");
		text = text.replaceAll("&#225;", "á");
		text = text.replaceAll("&#226;", "â");
		text = text.replaceAll("&#227;", "ã");
		text = text.replaceAll("&#233;", "é");
		text = text.replaceAll("&#234;", "ê");
		text = text.replaceAll("&#237;", "í");
		text = text.replaceAll("&#244;", "ô");
		text = text.replaceAll("&#245;", "õ");
		text = text.replaceAll("&#243;", "ó");
		text = text.replaceAll("&#250;", "ú");
		text = text.replaceAll("<strong>", " ");
		text = text.replaceAll("</strong>", " ");
		text = text.replaceAll("\\<.*?>","");
		text = text.trim();
		text = text.replaceAll("\\v\\v"," ");
		text = text.replaceAll("[ ]{2,}"," ");
		text = text.replaceAll(" ;",";");
		text = text.replaceAll(" \\)","\\)");
		text = text.replaceAll(" ,",",");
		text = text.replaceAll("#[^\n]*","");
		text = text.replaceAll("\n","");
		text = text.replaceAll(";",";\n");
		return text;
	}
	
	public String getInstrucciones(String rawText) {
		String text = rawText;
		text = text.replaceAll("#[^\n]*","");
		text = text.replaceAll("\n","");
		text = text.replaceAll(";",";\n");
		return text;
	}

	/*Esta funcionalidad  RECIBE TODA LA INFORMACIÓN DE LA VISTA Y LA PARSEA PARA ADAPTARLA AL CONTROLADOR JAVA.*/
   	@RequestMapping(value = "/generateData", method = RequestMethod.POST)
	public String generateData(@RequestBody Generic r, HttpServletRequest req, HttpServletResponse resp) {

		Gson gson = new Gson();
		//Node model = gson.fromJson(r.getData1(),Node.class);

		Type tipoNode = new TypeToken<List<Node>>(){}.getType();
		Type tipoEdge = new TypeToken<List<Edge>>(){}.getType();
		
			

		List<Node> nodes = new ArrayList<>();
		List<Edge> edges = new ArrayList<>();

		// OBTENEMOS LOS ELEMENTOS DEL ELEMENTO AGREGACION 					// TODO: Modificar el parseado de las agregaciones (se genera vacio pero sin errores)
		List<Node> nodesAltoNivel = gson.fromJson(r.getData3(), tipoNode);
		List<Edge> edgesAltoNivel = gson.fromJson(r.getData4(), tipoEdge);

		HashMap<Integer,DataAtributoEntidadOrigen> mapaAgregacion_nodosNombres = new HashMap<>();
		System.out.println("/generateData JAVA");
		Controlador c = null;
		try {
			c = new Controlador("debug");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Controlador Debug");
		//USAMOS ESTE METODO PARA PODER CONSERVAR LOS NOMBRES DE LAS ENTIDADES DENTRO DE LA AGREGACION
		cargarHashDeNombres(mapaAgregacion_nodosNombres,nodes,nodesAltoNivel,edges,edgesAltoNivel);

		System.out.println("cargarHashDeNombres");

		if(!nodesAltoNivel.isEmpty() & !edgesAltoNivel.isEmpty()){
			nodes = gson.fromJson(r.getData1(), tipoNode);
			edges = gson.fromJson(r.getData2(), tipoEdge);


			System.out.println("/BUSCAMOS LA IMAGEN...");
			//BUSCAMOS LA IMAGEN DE ALTO NIVEL PARA ASIGNARLE UNA PRIMARY KEY COMPUESTA POR TODAS PRIMARY KEYS INTERNAS EN LA AGREGACION
			String lblEntidadAltoNivel = "";
			for(int nodeIdx = 0; nodeIdx < nodesAltoNivel.size(); nodeIdx++){
				if(nodesAltoNivel.get(nodeIdx).getDataAttribute() != null && nodesAltoNivel.get(nodeIdx).getDataAttribute().isPrimaryKey()){
					Node altoNivelPrimaryKey = new Node();
					DataAttribute altoNivelDataAttribute = new DataAttribute();
					//CREAMOS LA CLAVE PRIMARIA
					System.out.println("CREAMOS LA CLAVE PRIMARIA");
					altoNivelDataAttribute.setComposite(false);
					altoNivelDataAttribute.setDomain("varchar");
					altoNivelDataAttribute.setMultivalued(false);
					altoNivelDataAttribute.setNotNull(false);
					altoNivelDataAttribute.setPrimaryKey(true);
					altoNivelDataAttribute.setSize("120");
					altoNivelDataAttribute.setUnique(false);

					//altoNivelPrimaryKey.setId(999998-nodeIdx);
					altoNivelPrimaryKey.setId(nodesAltoNivel.get(nodeIdx).getId());
					altoNivelPrimaryKey.setLabel(nodesAltoNivel.get(nodeIdx).getLabel());
					altoNivelPrimaryKey.setPhysics(false);
					altoNivelPrimaryKey.setShape("ellipse");
					altoNivelPrimaryKey.setStrong(false);
					altoNivelPrimaryKey.setDataAttribute(altoNivelDataAttribute);
					altoNivelPrimaryKey.setId_origin(nodesAltoNivel.get(nodeIdx).getId());

					
					nodes.add(altoNivelPrimaryKey);

					System.out.println("CREAMOS LA RELACIóN ENTRE LA CLAVE CREADA Y LA ENTIDAD");
					//  CREAMOS LA RELACIóN ENTRE LA CLAVE CREADA Y LA ENTIDAD
					Edge altoNivelPrimaryKeyEdge = new Edge();


					altoNivelPrimaryKeyEdge.setFrom(nodesAltoNivel.get(nodeIdx).getSuperEntity());
					altoNivelPrimaryKeyEdge.setId(UUID.randomUUID().toString());
					altoNivelPrimaryKeyEdge.setTo(nodesAltoNivel.get(nodeIdx).getId());
					System.out.println("idAgregacion: "+ nodesAltoNivel.get(nodeIdx).getSuperEntity() +" - id: " + nodesAltoNivel.get(nodeIdx).getId());
					edges.add(altoNivelPrimaryKeyEdge);

					// if(!lblEntidadAltoNivel.equals(""))
					// 	lblEntidadAltoNivel += "_";

					// lblEntidadAltoNivel += nodesAltoNivel.get(nodeIdx).getLabel();
				}

			}
			int parar =2;
		 
		}
		else{
			System.out.println(r.getData1().toString());
			nodes = gson.fromJson(r.getData1().toString(), tipoNode);
			edges = gson.fromJson(r.getData2().toString(), tipoEdge);

		}
		//LLAMAMOS 2 VECES AL GENERATEESQUEMA, LA PRIMERA DE ELLAS CON LA
		//INFORMACION DE LA AGREAGACION YLA SEGUNDA DE ELLA CON LOS ELEMENTOS 
		//EXTERNOS A ESTA AGREGACION, RECORDAR QUE SE MANEJA EL MISMO CONTROLADOR
		//POR TANTO LOS OBJETOS QUE SE CREAN EN AMBAS LLAMAS SE MANTIENEN PERSISTENTES EN 
		//LA EJECUCIÓN.

		generateEsquema(nodes,edges,c,false, mapaAgregacion_nodosNombres);	// Genera esquema sin agregación
		return generateEsquema(nodesAltoNivel,edgesAltoNivel,c,true,mapaAgregacion_nodosNombres);	// Genera esq agregación
	}

	public String generateEsquema(List<Node>  nodes,List<Edge>  edges,Controlador c, Boolean execute,HashMap<Integer,DataAtributoEntidadOrigen> mapaEntidadesAgreagacionNombres){
		
		HashMap dataParseada = new HashMap<Integer,Object>();

		//LIMPIAMOS LABELS DE CARACTERES QUE PUEDEN OCASIONAR PROBLEMAS.
		for (int k = 0; k < nodes.size(); k++) {
			if(mapaEntidadesAgreagacionNombres.containsKey(nodes.get(k).getId()))
				nodes.get(k).setId_origin(mapaEntidadesAgreagacionNombres.get(nodes.get(k).getId()).getIdEntidad());
			if(nodes.get(k).getLabel().contains("\n")) {
				String auxText= nodes.get(k).getLabel();
				String nameCleaned[]= auxText.split("\n");
				nodes.get(k).setLabel(nameCleaned[0]);
			}

		}
		
		
		//CREAMOS LOS TRANSFER DEPENDIENDO DEL TIPO DE FORMA DEL ELEMENTO PODEMOS SABER QUE TRANSFER CORRSPONDE A CADA UNO DE ELLOS
		for (int i = 0; i < nodes.size(); i++) {
			switch (nodes.get(i).getShape()) {
				case "box": //ENTIDAD
					TransferEntidad entityTransf = new TransferEntidad();
					entityTransf.setIdEntidad(nodes.get(i).getId());
					entityTransf.setPosicion(new Point2D.Float(0, (float) 1.0));
					entityTransf.setNombre(nodes.get(i).getLabel());
					entityTransf.setDebil(nodes.get(i).isWeak());
					entityTransf.setListaAtributos(new Vector());
					entityTransf.setListaClavesPrimarias(new Vector());
					entityTransf.setListaRestricciones(new Vector());
					entityTransf.setListaUniques(new Vector());
					c.mensajeDesde_GUI(TC.GUIInsertarEntidad_Click_BotonInsertar, entityTransf);
					//dataParseada.add(entityTransf);
					dataParseada.put(nodes.get(i).getId(),entityTransf);
					break;

				case "ellipse": //ATRIBUTO
					TransferAtributo attributeTransf = new TransferAtributo(c);

					attributeTransf.setNombre(nodes.get(i).getLabel());

					double x = 1.0;
					double y = 1.0;
					attributeTransf.setPosicion(new Point2D.Double(x,y));
					attributeTransf.setListaComponentes(new Vector());
					attributeTransf.setClavePrimaria(nodes.get(i).getDataAttribute().isPrimaryKey());
					attributeTransf.setCompuesto(nodes.get(i).getDataAttribute().isComposite());
					
					if(nodes.get(i).getId_origin()!=-1 && mapaEntidadesAgreagacionNombres.containsKey(nodes.get(i).getId_origin()))
						attributeTransf.setEntidad_origenName(mapaEntidadesAgreagacionNombres.get(nodes.get(i).getId_origin()).getNameEntidad());
					else
						attributeTransf.setEntidad_origenName("Entidad_no_encontrada");
					
					// Si esta seleccionado la opcion multivalorado
					attributeTransf.setMultivalorado(nodes.get(i).getDataAttribute().isMultivalued());
					//Unique y Notnull
					attributeTransf.setNotnull(nodes.get(i).getDataAttribute().isNotNull());
					//ponemos unique a false, ya que en caso de ser Unique se hace la llamada abajo.
					attributeTransf.setUnique(nodes.get(i).getDataAttribute().isUnique());
					TipoDominio dominio;
					String dom;
					dom=(TipoDominio.VARCHAR).toString();
					attributeTransf.setDominio(dom);
					attributeTransf.setListaRestricciones(new Vector());

					//dataParseada.add(attributeTransf);
					dataParseada.put(nodes.get(i).getId(), attributeTransf);
					break;

				case "diamond": //RELACION
					TransferRelacion relationTransf = new TransferRelacion();
					relationTransf.setNombre(nodes.get(i).getLabel());
					relationTransf.setTipo(nodes.get(i).getTypeNode());
					//relationTransf.setTipo("Normal");
					relationTransf.setRol(null);
					relationTransf.setListaEntidadesYAridades(new Vector());
					relationTransf.setListaAtributos(new Vector());
					relationTransf.setListaRestricciones(new Vector());
					relationTransf.setListaUniques(new Vector());
					relationTransf.setPosicion(new Point2D.Float(0, (float) 1.0));
					relationTransf.setVolumen(0);
					relationTransf.setFrecuencia(0);
					relationTransf.setOffsetAttr(0);
					c.mensajeDesde_GUI(TC.GUIInsertarRelacion_Click_BotonInsertar, relationTransf);

					//ataParseada.add(relationTransf);
					dataParseada.put(nodes.get(i).getId(), relationTransf);
					break;

				case "triangleDown": //IS_A
					TransferRelacion relationTransfIsA = new TransferRelacion();
					relationTransfIsA.setNombre(nodes.get(i).getLabel());
					relationTransfIsA.setTipo("IsA");
					relationTransfIsA.setRol(null);
					relationTransfIsA.setListaEntidadesYAridades(new Vector());
					relationTransfIsA.setListaAtributos(new Vector());
					relationTransfIsA.setListaRestricciones(new Vector());
					relationTransfIsA.setListaUniques(new Vector());
					relationTransfIsA.setPosicion(new Point2D.Float(0, (float) 1.0));
					relationTransfIsA.setVolumen(0);
					relationTransfIsA.setFrecuencia(0);
					relationTransfIsA.setOffsetAttr(0);
					c.mensajeDesde_GUI(TC.GUIInsertarRelacionIsA_Click_BotonInsertar, relationTransfIsA);

					//ataParseada.add(relationTransf);
					dataParseada.put(nodes.get(i).getId(), relationTransfIsA);
					break;
			}
		}

		//LOS EDGES NOS INDICAN LAS RELACIONES ENTRE ELEMENTOS, PROCEDEMOS A ENLAZARLOS TENIENDO EN CUENTA ESTE ARRAY CON LOS DATOS
		for (int j = 0; j < edges.size(); j++) {
			edges.get(j).updateLabelFromName();
			//RELACION ENTRE ENTIDAD Y ATRIBUTO
			if((dataParseada.get(edges.get(j).getFrom()) instanceof TransferEntidad) && (dataParseada.get(edges.get(j).getTo()) instanceof TransferAtributo)) {

				// Mandamos la entidad, el nuevo atributo y si hay tamano tambien
				Vector<Object> v = new Vector<Object>();
				String tamano = "";
				TransferEntidad te = (TransferEntidad)dataParseada.get(edges.get(j).getFrom());
				TransferAtributo ta = (TransferAtributo)dataParseada.get(edges.get(j).getTo());
				v.add(te);
				v.add(ta);
				if (!tamano.isEmpty()) v.add(tamano);
				c.mensajeDesde_GUI(TC.GUIAnadirAtributoEntidad_Click_BotonAnadir, v);
				Vector<Object> v1= new Vector<Object>();
				TransferAtributo clon_atributo2 = ta.clonar();
				clon_atributo2.setClavePrimaria(false);
				v1.add(clon_atributo2);
				v1.add(te);

				if (ta.isClavePrimaria()) 
					c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarClavePrimariaAtributo,v1); 

			}
			//RELACION ENTRE RELACION Y ATRIBUTO
			else if((dataParseada.get(edges.get(j).getFrom()) instanceof TransferRelacion) && (dataParseada.get(edges.get(j).getTo()) instanceof TransferAtributo)) {
				Vector<Object> v = new Vector<Object>();
				String tamano = "";
				TransferAtributo ta = (TransferAtributo)dataParseada.get(edges.get(j).getTo());
				TransferRelacion tRelation = (TransferRelacion)dataParseada.get(edges.get(j).getFrom());
				v.add(tRelation);
				v.add(ta);
				if (!tamano.isEmpty()) v.add(tamano);
				c.mensajeDesde_GUI(TC.GUIAnadirAtributoRelacion_Click_BotonAnadir, v);
				Vector<Object> v1= new Vector<Object>();
				TransferAtributo clon_atributo2 = ta.clonar();
				clon_atributo2.setClavePrimaria(false);
				v1.add(clon_atributo2);
				v1.add(tRelation);

				if (ta.isClavePrimaria())
					c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarClavePrimariaAtributo,v1);
			}
			//RELACION ENTRE RELACION Y ENTIDAD
			else if(edges.get(j).getType() == null &&  (dataParseada.get(edges.get(j).getFrom()) instanceof TransferRelacion) && (dataParseada.get(edges.get(j).getTo()) instanceof TransferEntidad)) {
				Vector<Object> vData= new Vector<Object>();

				TransferEntidad teB = (TransferEntidad)dataParseada.get(edges.get(j).getTo());
				TransferRelacion tRelation = (TransferRelacion)dataParseada.get(edges.get(j).getFrom());

				TransferEntidad clon_entidad = teB.clonar(); // transfer entidad B
				TransferRelacion clon_rel= tRelation.clonar();

				vData.add(tRelation);
				vData.add(clon_entidad);
				vData.add(edges.get(j).getLabelFrom().toLowerCase());
				vData.add(edges.get(j).getLabelTo().toLowerCase());
				vData.add(edges.get(j).getLabel());

				c.mensajeDesde_GUI(TC.GUIAnadirEntidadARelacion_ClickBotonAnadir, vData);
			}
			//RELACIONAMOS LAS RELACIONES IS_A con las entidades correspondientes habrá que parsear el tipo si es child or parent.
			else if(edges.get(j).getType() != null &&  (dataParseada.get(edges.get(j).getFrom()) instanceof TransferRelacion) && (dataParseada.get(edges.get(j).getTo()) instanceof TransferEntidad)) {

				Vector<Object> vData= new Vector<Object>();
				TransferEntidad teB = (TransferEntidad)dataParseada.get(edges.get(j).getTo());
				TransferRelacion tRelation = (TransferRelacion)dataParseada.get(edges.get(j).getFrom());
				TransferEntidad clon_entidad = teB.clonar(); // transfer entidad B

				vData.add(tRelation);
				vData.add(clon_entidad);

				if(edges.get(j).getType().contains("parent"))
					c.mensajeDesde_GUI(TC.GUIEstablecerEntidadPadre_ClickBotonAceptar, vData);

				else // para los hijos
					c.mensajeDesde_GUI(TC.GUIAnadirEntidadHija_ClickBotonAnadir,vData);

			}
		}

		String respuesta ="";
		// ASIGNAR A ENTIDAD UNA RELACION ==//
		if(execute){
			//CUANDO TENEMOS LAS ENTIDAD CREADAS Y ENLAZADAS PROCEDEMOS A LLAMAR AL METODO GENERAMODELORELACIONAL, QUE PROCEDERÁ A EJECUTAR LA LOGICA
			GeneradorEsquema testGen = new GeneradorEsquema(messageSource);
			testGen.setControlador(c);
			respuesta = testGen.generaModeloRelacional_v3("default",false);
		}
		return respuesta;
	}

	public String updateLabelDom(String lblDom,String size){
		switch(lblDom.toUpperCase()){
			case "VARCHAR":
				return  lblDom.toUpperCase()+"("+size+")";
			default:
				return lblDom.toUpperCase();
		}
	}

	//GENERA LAS QUERIES DEPENDIENDO DEL TIPO SELECCIONADO MYSQL, ACCESS, ETC  
	public String generateEsquemaScriptSQL(List<Node>  nodes,List<Edge>  edges,String lblPhySchema,String idxPhySchema,Controlador c, Boolean execute,HashMap<Integer,DataAtributoEntidadOrigen> mapaEntidadesAgreagacionNombres){
		HashMap dataParseada = new HashMap<Integer,Object>(); 
	// SE LIMPIA LA CADENA.
	for (int k = 0; k < nodes.size(); k++) {
		if(nodes.get(k).getLabel().contains("\n")) {
			String auxText= nodes.get(k).getLabel();
			String nameCleaned[]= auxText.split("\n");
			nodes.get(k).setLabel(nameCleaned[0]);
		}

	}

	//PROCEDEMOS A CREAR LOS ELEMENTOS DEPENDIENDO DEL TIPO, DIFERENCIANDOLOS POR LA FORMA DE ELLAS
	for (int i = 0; i < nodes.size(); i++) {
		switch (nodes.get(i).getShape()) {
			case "box":
				TransferEntidad entityTransf = new TransferEntidad();
				entityTransf.setIdEntidad(nodes.get(i).getId());
				entityTransf.setPosicion(new Point2D.Float(0, (float) 1.0));
				entityTransf.setNombre(nodes.get(i).getLabel());
				entityTransf.setDebil(nodes.get(i).isWeak());
				entityTransf.setListaAtributos(new Vector());
				entityTransf.setListaClavesPrimarias(new Vector());
				entityTransf.setListaRestricciones(new Vector());
				entityTransf.setListaUniques(new Vector());
				c.mensajeDesde_GUI(TC.GUIInsertarEntidad_Click_BotonInsertar, entityTransf);
				//dataParseada.add(entityTransf);
				dataParseada.put(nodes.get(i).getId(),entityTransf);
				break;

			case "ellipse":
				TransferAtributo attributeTransf = new TransferAtributo(c);

				attributeTransf.setNombre(nodes.get(i).getLabel());

				double x = 1.0;
				double y = 1.0;
				attributeTransf.setPosicion(new Point2D.Double(x,y));
				attributeTransf.setListaComponentes(new Vector());
				attributeTransf.setClavePrimaria(nodes.get(i).getDataAttribute().isPrimaryKey());
				attributeTransf.setCompuesto(nodes.get(i).getDataAttribute().isComposite());
				// Si esta seleccionado la opcion multivalorado
				attributeTransf.setMultivalorado(nodes.get(i).getDataAttribute().isMultivalued());
				//Unique y Notnull
				attributeTransf.setNotnull(nodes.get(i).getDataAttribute().isNotNull());
				//ponemos unique a false, ya que en caso de ser Unique se hace la llamada abajo.
				attributeTransf.setUnique(nodes.get(i).getDataAttribute().isUnique());
				TipoDominio dominio;
				String dom;
				dom=(nodes.get(i).getDataAttribute().getDomain()).toString();
				//dom=(TipoDominio.VARCHAR).toString();
				//attributeTransf.setDominio(dom);
				attributeTransf.setDominio(dom);
				attributeTransf.setListaRestricciones(new Vector());
				dataParseada.put(nodes.get(i).getId(), attributeTransf);
				break;

			case "diamond":
				TransferRelacion relationTransf = new TransferRelacion();
				relationTransf.setNombre(nodes.get(i).getLabel());
				relationTransf.setTipo(nodes.get(i).getTypeNode());
				//relationTransf.setTipo("Normal");
				relationTransf.setRol(null);
				relationTransf.setListaEntidadesYAridades(new Vector());
				relationTransf.setListaAtributos(new Vector());
				relationTransf.setListaRestricciones(new Vector());
				relationTransf.setListaUniques(new Vector());
				relationTransf.setPosicion(new Point2D.Float(0, (float) 1.0));
				relationTransf.setVolumen(0);
				relationTransf.setFrecuencia(0);
				relationTransf.setOffsetAttr(0);
				c.mensajeDesde_GUI(TC.GUIInsertarRelacion_Click_BotonInsertar, relationTransf);

				dataParseada.put(nodes.get(i).getId(), relationTransf);
				break;

			case "triangleDown":
				TransferRelacion relationTransfIsA = new TransferRelacion();
				relationTransfIsA.setNombre(nodes.get(i).getLabel());
				relationTransfIsA.setTipo("IsA");
				relationTransfIsA.setRol(null);
				relationTransfIsA.setListaEntidadesYAridades(new Vector());
				relationTransfIsA.setListaAtributos(new Vector());
				relationTransfIsA.setListaRestricciones(new Vector());
				relationTransfIsA.setListaUniques(new Vector());
				relationTransfIsA.setPosicion(new Point2D.Float(0, (float) 1.0));
				relationTransfIsA.setVolumen(0);
				relationTransfIsA.setFrecuencia(0);
				relationTransfIsA.setOffsetAttr(0);
				c.mensajeDesde_GUI(TC.GUIInsertarRelacionIsA_Click_BotonInsertar, relationTransfIsA);

				dataParseada.put(nodes.get(i).getId(), relationTransfIsA);
				break;
		}
	}

	//CREA LAS RELACIONES ENTRE LOS ELEMENTOS CREADOS.
	for (int j = 0; j < edges.size(); j++) {
		edges.get(j).updateLabelFromName();
		//RELACION ENTRE ENTIDAD Y ATRIBUTO
		if((dataParseada.get(edges.get(j).getFrom()) instanceof TransferEntidad) && (dataParseada.get(edges.get(j).getTo()) instanceof TransferAtributo)) {
			// Mandamos la entidad, el nuevo atributo y si hay tamano tambien
			Vector<Object> v = new Vector<Object>();
			String tamano = "";
			TransferEntidad te = (TransferEntidad)dataParseada.get(edges.get(j).getFrom());
			TransferAtributo ta = (TransferAtributo)dataParseada.get(edges.get(j).getTo());
			v.add(te);
			v.add(ta);
			if (!tamano.isEmpty()) v.add(tamano);
			c.mensajeDesde_GUI(TC.GUIAnadirAtributoEntidad_Click_BotonAnadir, v);
			Vector<Object> v1= new Vector<Object>();
			TransferAtributo clon_atributo2 = ta.clonar();
			clon_atributo2.setClavePrimaria(false);
			v1.add(clon_atributo2);
			v1.add(te);

			if (ta.isClavePrimaria()) 
				c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarClavePrimariaAtributo,v1);

		}
		//RELACION ENTRE RELACION Y ATRIBUTO
		else if((dataParseada.get(edges.get(j).getFrom()) instanceof TransferRelacion) && (dataParseada.get(edges.get(j).getTo()) instanceof TransferAtributo)) {

			Vector<Object> v = new Vector<Object>();
			String tamano = "";
			TransferAtributo ta = (TransferAtributo)dataParseada.get(edges.get(j).getTo());
			TransferRelacion tRelation = (TransferRelacion)dataParseada.get(edges.get(j).getFrom());
			v.add(tRelation);
			v.add(ta);
			if (!tamano.isEmpty()) v.add(tamano);
			c.mensajeDesde_GUI(TC.GUIAnadirAtributoRelacion_Click_BotonAnadir, v);
			Vector<Object> v1= new Vector<Object>();
			TransferAtributo clon_atributo2 = ta.clonar();
			clon_atributo2.setClavePrimaria(false);
			v1.add(clon_atributo2);
			v1.add(tRelation);

			if (ta.isClavePrimaria())
				c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarClavePrimariaAtributo,v1);

		}
		//RELACION ENTRE RELACION Y ENTIDAD
		else if(edges.get(j).getType() == null &&  (dataParseada.get(edges.get(j).getFrom()) instanceof TransferRelacion) && (dataParseada.get(edges.get(j).getTo()) instanceof TransferEntidad)) {
			Vector<Object> vData= new Vector<Object>();

			TransferEntidad teB = (TransferEntidad)dataParseada.get(edges.get(j).getTo());
			TransferRelacion tRelation = (TransferRelacion)dataParseada.get(edges.get(j).getFrom());

			TransferEntidad clon_entidad = teB.clonar(); // transfer entidad B
			TransferRelacion clon_rel= tRelation.clonar();

			vData.add(tRelation);
			vData.add(clon_entidad);
			if(edges.get(j).getLabelFrom() == null)vData.add("");
			else {
			vData.add(edges.get(j).getLabelFrom().toLowerCase());
			}
			if(edges.get(j).getLabelTo() == null)vData.add("");
			else {
			vData.add(edges.get(j).getLabelTo().toLowerCase());
			}
			if(edges.get(j).getLabel()== null)vData.add(" ");
			else {
			vData.add(edges.get(j).getLabel());
			}

			c.mensajeDesde_GUI(TC.GUIAnadirEntidadARelacion_ClickBotonAnadir, vData);
		}
		//RELACIONAMOS LAS RELACIONES IS_A con las entidades correspondientes habrá que parsear el tipo si es child or parent.
		else if(edges.get(j).getType() != null &&  (dataParseada.get(edges.get(j).getFrom()) instanceof TransferRelacion) && (dataParseada.get(edges.get(j).getTo()) instanceof TransferEntidad)) {

			Vector<Object> vData= new Vector<Object>();
			TransferEntidad teB = (TransferEntidad)dataParseada.get(edges.get(j).getTo());
			TransferRelacion tRelation = (TransferRelacion)dataParseada.get(edges.get(j).getFrom());
			TransferEntidad clon_entidad = teB.clonar(); // transfer entidad B

			vData.add(tRelation);
			vData.add(clon_entidad);

			if(edges.get(j).getType().contains("parent"))
				c.mensajeDesde_GUI(TC.GUIEstablecerEntidadPadre_ClickBotonAceptar, vData);

			else // para los hijos
				c.mensajeDesde_GUI(TC.GUIAnadirEntidadHija_ClickBotonAnadir,vData);

		}
		else {
			//System.err.println("problemas al parseo de las relaciones");
		}
	}

	String respuesta ="";
		// ASIGNAR A ENTIDAD UNA RELACION ==//
		if(execute){
			GeneradorEsquema testGen = new GeneradorEsquema(messageSource);
			TransferConexion tc = new TransferConexion(Integer.parseInt(idxPhySchema),lblPhySchema,false,"","","");
			testGen.setControlador(c);
			String example = testGen.generaModeloRelacional_v3(tc.getRuta(),true);
			//SOBRE EL ESQUEMA LOGICO YA GENERADO, SE DEBE EJECUTAR EL SIGUIENTE METODO QUE OBTIENE LAS QUERIES PARA EL ESQUEMA FISICO
			respuesta = testGen.generaScriptSQL(tc);
		}
		return respuesta; 
	}

	@RequestMapping(value = "/generateDataScriptSQL", method = RequestMethod.POST)
	public String generateDataScriptSQL(@RequestBody Generic r, HttpServletRequest req, HttpServletResponse resp) {


		Gson gson = new Gson();

		Type tipoNode = new TypeToken<List<Node>>(){}.getType();
		Type tipoEdge = new TypeToken<List<Edge>>(){}.getType();

		List<Node> nodes = gson.fromJson(r.getData1(), tipoNode);
		List<Edge> edges = gson.fromJson(r.getData2(), tipoEdge);
		String lblPhySchema = r.getData5();
		String idxPhySchema = r.getData6();

		List<Node> nodesAltoNivel = gson.fromJson(r.getData3(), tipoNode);
		List<Edge> edgesAltoNivel = gson.fromJson(r.getData4(), tipoEdge);

		HashMap<Integer,DataAtributoEntidadOrigen> mapaAgregacion_nodosNombres = new HashMap<>();

		Controlador c = null;
		try {
			c = new Controlador("debug");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cargarHashDeNombres(mapaAgregacion_nodosNombres,nodes,nodesAltoNivel,edges,edgesAltoNivel);

		if(!nodesAltoNivel.isEmpty() & !edgesAltoNivel.isEmpty()){
			nodes = gson.fromJson(r.getData1(), tipoNode);
			edges = gson.fromJson(r.getData2(), tipoEdge);

			//BUSCAMOS LA IMAGEN DE ALTO NIVEL PARA ASIGNARLE UNA PRIMARY KEY COMPUESTA POR TODAS PRIMARY KEYS INTERNAS EN LA AGREGACION
			String lblEntidadAltoNivel = "";
			for(int nodeIdx = 0; nodeIdx < nodesAltoNivel.size(); nodeIdx++){
				if(nodesAltoNivel.get(nodeIdx).getDataAttribute() != null && nodesAltoNivel.get(nodeIdx).getDataAttribute().isPrimaryKey()){
					Node altoNivelPrimaryKey = new Node();
					DataAttribute altoNivelDataAttribute = new DataAttribute();
					//CREAMOS LA CLAVE PRIMARIA
					altoNivelDataAttribute.setComposite(false);
					altoNivelDataAttribute.setDomain("varchar");
					altoNivelDataAttribute.setMultivalued(false);
					altoNivelDataAttribute.setNotNull(false);
					altoNivelDataAttribute.setPrimaryKey(true);
					altoNivelDataAttribute.setSize("20");
					altoNivelDataAttribute.setUnique(false);

					altoNivelPrimaryKey.setId(nodesAltoNivel.get(nodeIdx).getId());
					altoNivelPrimaryKey.setLabel(nodesAltoNivel.get(nodeIdx).getLabel());
					altoNivelPrimaryKey.setPhysics(false);
					altoNivelPrimaryKey.setShape("ellipse");
					altoNivelPrimaryKey.setStrong(false);
					altoNivelPrimaryKey.setDataAttribute(altoNivelDataAttribute);

					nodes.add(altoNivelPrimaryKey);

					//  CREAMOS LA RELACIóN ENTRE LA CLAVE CREADA Y LA ENTIDAD
					Edge altoNivelPrimaryKeyEdge = new Edge();

					altoNivelPrimaryKeyEdge.setFrom(nodesAltoNivel.get(nodeIdx).getSuperEntity());
					altoNivelPrimaryKeyEdge.setId(UUID.randomUUID().toString());
					altoNivelPrimaryKeyEdge.setTo(nodesAltoNivel.get(nodeIdx).getId());

					edges.add(altoNivelPrimaryKeyEdge); 
				}

			} 
		}
		else{
			nodes = gson.fromJson(r.getData1(), tipoNode);
			edges = gson.fromJson(r.getData2(), tipoEdge);

			return generateEsquemaScriptSQL(nodes,edges,lblPhySchema,idxPhySchema,c,true,mapaAgregacion_nodosNombres);
		}
		
		generateEsquemaScriptSQL(nodes,edges,lblPhySchema,idxPhySchema,c,false,mapaAgregacion_nodosNombres);
		return generateEsquemaScriptSQL(nodesAltoNivel,edgesAltoNivel,lblPhySchema,idxPhySchema,c,true,mapaAgregacion_nodosNombres) ;

	
	}

	//METODO ENCARGADO DE CARGAR UN HASHMAP DE ATRIBUTOS CLAVE PARA NO PERDER LOS NOMBRES DE REFERENCIA, QUE TIENE COMO CLAVE EL ID DEL ELEMENTO Y COMO VALUE EL NOMBRE DEL ATRIBUTO
	void cargarHashDeNombres(HashMap<Integer,DataAtributoEntidadOrigen> mapaAgregacion_nodosNombres,List<Node> nodes,List<Node> nodesAltoNivel,List<Edge> edges,List<Edge> edgesAltoNivel){

		HashMap<Integer,String> nombresEntidades = new HashMap<>();
		//GENERIC --> SE USARA PARA que el valor del primer atributo sea el nombre y el segundo el id para que 
		for(int nodeIdx_fueraAgregacion = 0; nodeIdx_fueraAgregacion < nodes.size(); nodeIdx_fueraAgregacion++){

			nombresEntidades.put(nodes.get(nodeIdx_fueraAgregacion).getId(),nodes.get(nodeIdx_fueraAgregacion).getLabel());

			if(nodes.get(nodeIdx_fueraAgregacion).getShape().equals("ellipse") && nodes.get(nodeIdx_fueraAgregacion).getDataAttribute().isPrimaryKey())
				if(!mapaAgregacion_nodosNombres.containsKey(nodes.get(nodeIdx_fueraAgregacion).getId()))
					mapaAgregacion_nodosNombres.put(nodes.get(nodeIdx_fueraAgregacion).getId(),new DataAtributoEntidadOrigen(nodes.get(nodeIdx_fueraAgregacion).getId(),-1, nodes.get(nodeIdx_fueraAgregacion).getLabel()));
				
		}

		for(int nodeIdx_dentroAgregacion = 0; nodeIdx_dentroAgregacion < nodesAltoNivel.size(); nodeIdx_dentroAgregacion++){

			nombresEntidades.put(nodesAltoNivel.get(nodeIdx_dentroAgregacion).getId(),nodesAltoNivel.get(nodeIdx_dentroAgregacion).getLabel());

			if(nodesAltoNivel.get(nodeIdx_dentroAgregacion).getShape().equals("ellipse") && nodesAltoNivel.get(nodeIdx_dentroAgregacion).getDataAttribute().isPrimaryKey())
				if(!mapaAgregacion_nodosNombres.containsKey(nodesAltoNivel.get(nodeIdx_dentroAgregacion).getId()))
					mapaAgregacion_nodosNombres.put(nodesAltoNivel.get(nodeIdx_dentroAgregacion).getId(),new DataAtributoEntidadOrigen(nodesAltoNivel.get(nodeIdx_dentroAgregacion).getId(),-1, nodesAltoNivel.get(nodeIdx_dentroAgregacion).getLabel()));
				
		}

		for(int edgeIdx_fueraAgregacion = 0; edgeIdx_fueraAgregacion < edges.size(); edgeIdx_fueraAgregacion++){
			if(mapaAgregacion_nodosNombres.containsKey(edges.get(edgeIdx_fueraAgregacion).getTo())){
				mapaAgregacion_nodosNombres.get(edges.get(edgeIdx_fueraAgregacion).getTo()).setIdEntidad(edges.get(edgeIdx_fueraAgregacion).getFrom());
				mapaAgregacion_nodosNombres.get(edges.get(edgeIdx_fueraAgregacion).getTo()).setNameEntidad(nombresEntidades.get(edges.get(edgeIdx_fueraAgregacion).getFrom()));
			}
		}

		for(int edgeIdx_dentroAgregacion = 0; edgeIdx_dentroAgregacion < edgesAltoNivel.size(); edgeIdx_dentroAgregacion++){
			if(mapaAgregacion_nodosNombres.containsKey(edgesAltoNivel.get(edgeIdx_dentroAgregacion).getTo())){
				mapaAgregacion_nodosNombres.get(edgesAltoNivel.get(edgeIdx_dentroAgregacion).getTo()).setIdEntidad(edgesAltoNivel.get(edgeIdx_dentroAgregacion).getFrom());

				mapaAgregacion_nodosNombres.get(edgesAltoNivel.get(edgeIdx_dentroAgregacion).getTo()).setNameEntidad(nombresEntidades.get(edgesAltoNivel.get(edgeIdx_dentroAgregacion).getFrom()));

			}
		} 
	}
 

   @GetMapping("/logout")
   public RedirectView redirectWithUsingRedirectView(RedirectAttributes attributes) {
       return new RedirectView("/");
   }

   /**
    * FIle upload read
    * @param file
    * @return
    * @throws IOException
    */

   @RequestMapping(value = "/readFile", method = RequestMethod.POST)
   public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
	   String fileRead = "";
	   Pattern pat = Pattern.compile(".*dbw");
	   HashMap<Integer, String> result = new HashMap<Integer, String>();

	   if(!file.isEmpty()) {
	  		Matcher mat = pat.matcher(file.getOriginalFilename());
		   if (mat.matches()) {
			   byte[] bytes = file.getBytes();
			   fileRead = new String(bytes, StandardCharsets.UTF_8);
			   result.put(1, fileRead);
		   } else {
			   result.put(0, messageSource.getMessage("textos.extensionFail", null, LocaleContextHolder.getLocale()));
		   }
	  }else{
		  result.put(0, messageSource.getMessage("textos.fileInvalid", null, LocaleContextHolder.getLocale()));
	  }

	   ObjectMapper objectMapper = new ObjectMapper();
	   String json = objectMapper.writeValueAsString(result);

      return json;
   }
}