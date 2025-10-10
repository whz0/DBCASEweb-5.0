package modelo.servicios;

import java.util.Iterator;
import java.util.Vector;

import controlador.Controlador;
import controlador.TC;
import modelo.transfers.Transfer;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import persistencia.DAOAtributos;
import persistencia.DAOEntidades;
import persistencia.DAORelaciones;
import persistencia.EntidadYAridad;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ServiciosRelaciones {

	private Controlador controlador;
	
	//Devuelve actualizada la lista de relaciones al controlador através de un mensaje
	public void ListaDeRelaciones(){
		// Creamos el DAO de relaciones
		DAORelaciones dao = new DAORelaciones(this.controlador.getPath());
		// Utilizando el DAO obtenemos la lista de Relaciones
		Vector <TransferRelacion> lista_relaciones = dao.ListaDeRelaciones();
		// Se lo devolvemos al controlador
		controlador.mensajeDesde_SR(TC.SR_ListarRelaciones_HECHO, lista_relaciones);
	}
	
	//Devuelve actualizada la lista de relaciones
	public Vector <TransferRelacion> ListaDeRelacionesNoVoid(){
		// Creamos el DAO de relaciones
		DAORelaciones dao = new DAORelaciones(this.controlador.getPath());
		// Utilizando el DAO obtenemos la lista de Relaciones
		Vector <TransferRelacion> lista_relaciones = dao.ListaDeRelaciones();
		// Se lo devolvemos al controlador
		return lista_relaciones;
	}
	
	
	
	/* Anadir Relacion
	 * Parametros: un TransferRelacion que contiene el nombre de la nueva Relacion y la posicion donde debe ir dibujada.
	 * Devuelve: La Relacion en un TransferRelacion y el mensaje -> SR_InsertarRelacion_HECHO
	 * Condiciones:
	 * Si el nombre es vacio -> SR_InsertarRelacion_ERROR_NombreDeRelacionEsVacio
	 * Si el nombre ya existe -> SR_InsertarRelacion_ERROR_NombreDeRelacionYaExiste
	 * Si al usar el DAORelaciones se produce un error -> SR_InsertarRelacion_ERROR_DAO
	 */
	public void anadirRelacion(TransferRelacion tr){
		if (tr.getNombre().isEmpty()){
			controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDeRelacionEsVacio, null);
			return;
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		for (Iterator it = lista.iterator(); it.hasNext(); ){
			TransferRelacion elem_tr = (TransferRelacion)it.next();
			if (elem_tr.getNombre().toLowerCase().equals(tr.getNombre().toLowerCase())){
				controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDeRelacionYaExiste,tr);
				return;
			}
		}
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> listaE = daoEntidades.ListaDeEntidades();
		for (Iterator it = listaE.iterator(); it.hasNext(); ){
			TransferEntidad elem_te = (TransferEntidad)it.next();
			if (elem_te.getNombre().toLowerCase().equals(tr.getNombre().toLowerCase())){
				controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDeRelacionYaExisteComoEntidad,tr);
				return;
			}
		}
		
		int id = daoRelaciones.anadirRelacion(tr);
		if (id==-1)	controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_DAORelaciones,tr);
		else{
			tr.setIdRelacion(id);
			controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_HECHO, daoRelaciones.consultarRelacion(tr));
		}
	}
	
	/* Se puede Anadir Relacion
	 * Parametros: un TransferRelacion que contiene el nombre de la nueva Relacion y la posicion donde debe ir dibujada.
	 * Devuelve: La Relacion en un TransferRelacion y el mensaje -> SR_InsertarRelacion_HECHO
	 * Condiciones:
	 * Si el nombre es vacio -> SR_InsertarRelacion_ERROR_NombreDeRelacionEsVacio
	 * Si el nombre ya existe -> SR_InsertarRelacion_ERROR_NombreDeRelacionYaExiste
	 * Si al usar el DAORelaciones se produce un error -> SR_InsertarRelacion_ERROR_DAO
	 */
	public boolean SePuedeAnadirRelacion(TransferRelacion tr){
		if (tr.getNombre().isEmpty()){
			controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDeRelacionEsVacio, null);
			return false;
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		for (Iterator it = lista.iterator(); it.hasNext(); ){
			TransferRelacion elem_tr = (TransferRelacion)it.next();
			if (elem_tr.getNombre().toLowerCase().equals(tr.getNombre().toLowerCase())){
				controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDeRelacionYaExiste,tr);
				return false;
			}
		}
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> listaE = daoEntidades.ListaDeEntidades();
		for (Iterator it = listaE.iterator(); it.hasNext(); ){
			TransferEntidad elem_te = (TransferEntidad)it.next();
			if (elem_te.getNombre().toLowerCase().equals(tr.getNombre().toLowerCase())){
				controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDeRelacionYaExisteComoEntidad,tr);
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Anadir una relacion IsA
	 */
	public void anadirRelacionIsA(TransferRelacion tr){
		tr.setNombre("IsA");
		tr.setTipo("IsA");
		tr.setListaAtributos(new Vector());
		tr.setListaEntidadesYAridades(new Vector());
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		int id = daoRelaciones.anadirRelacion(tr);
		if (id==-1)	controlador.mensajeDesde_SR(TC.SR_InsertarRelacionIsA_ERROR_DAORelaciones,tr);
		else{
			tr.setIdRelacion(id);
			controlador.mensajeDesde_SR(TC.SR_InsertarRelacionIsA_HECHO, tr);
		}
	}
		
	
	/*
	 *  Eliminar una relación IsA
	 */
	public void eliminarRelacionIsA(TransferRelacion tr){
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (daoRelaciones.borrarRelacion(tr) == false)
			controlador.mensajeDesde_SR(TC.SR_EliminarRelacionIsA_ERROR_DAORelaciones, tr);
		else{ 
			controlador.mensajeDesde_SR(TC.SR_EliminarRelacionIsA_HECHO, tr);
		}
	}
	
	/*
	 *  Eliminar una relación Normal
	 */
	public void eliminarRelacionNormal(TransferRelacion tr){
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (daoRelaciones.borrarRelacion(tr) == false)
			controlador.mensajeDesde_SR(TC.SR_EliminarRelacionNormal_ERROR_DAORelaciones, tr);
		else{ 
			controlador.mensajeDesde_SR(TC.SR_EliminarRelacionNormal_HECHO, tr);
		}
	}
	
	public void renombrarRelacion(TransferRelacion tr, String nuevoNombre){
		Vector<Object> v = new Vector<Object>();
		v.add(tr);
		v.add(nuevoNombre);
		v.add(tr.getNombre());
		
		// Si el nuevo nombre es vacio -> ERROR
		if (nuevoNombre.isEmpty()){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_NombreDeRelacionEsVacio, v);
			return;
		}
		// Si el nuevo nombre es IsA (o variantes) -> ERROR
		if (nuevoNombre.toLowerCase().equals("isa")){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_NombreIsA, v);
			return;
		}
		// Si hay una relacion que ya tiene el "nuevoNombre" -> ERROR
		DAORelaciones dao = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> listaRelaciones = dao.ListaDeRelaciones();
		int i = 0;
		TransferRelacion rel;
		while (i<listaRelaciones.size()){
			rel = listaRelaciones.get(i);
			if (rel.getNombre().toLowerCase().equals(nuevoNombre.toLowerCase())&& rel.getIdRelacion()!=tr.getIdRelacion()){
				controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_NombreDeRelacionYaExiste,v);
				return;
			}
			i++;
		}
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> listaE = daoEntidades.ListaDeEntidades();
		/*if (listaE == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAOEntidades,v);
			return;
		}*/
		for (Iterator it = listaE.iterator(); it.hasNext(); ){
			TransferEntidad elem_te = (TransferEntidad)it.next();
			if (elem_te.getNombre().toLowerCase().equals(nuevoNombre.toLowerCase())){
				controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_NombreDeRelacionYaExisteComoEntidad,tr);
				return;
			}
		}
		
		// Modificamos el nombre
		tr.setNombre(nuevoNombre);
		if (dao.modificarRelacion(tr)==false){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAORelaciones, v);	
		}
		else 
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_HECHO, v);
		return;
	}
	
	
	/*
	 * Debilitar relacion
	 * -> hay que invertir el valor de "debil"
	 */
	public void debilitarRelacion(TransferRelacion tr){
		String tipoViejo = tr.getTipo();
		if(tipoViejo.equals("Debil")) tr.setTipo("Normal");
		else tr.setTipo("Debil");
		
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (daoRelaciones.modificarRelacion(tr)==false){
			tr.setTipo(tipoViejo);
			controlador.mensajeDesde_SR(TC.SR_DebilitarRelacion_ERROR_DAORelaciones, tr);	
		}
		else 
			controlador.mensajeDesde_SR(TC.SR_DebilitarRelacion_HECHO, tr);
		return;
	}
	
	/*
	 *Devuelve cierto si la relación es débil y falso en caso contrario
	 */
	public boolean esDebil(TransferEntidad te){
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> listaRelaciones = daoRelaciones.ListaDeRelaciones();
		ServiciosEntidades sEntidades= new ServiciosEntidades();
		sEntidades.setControlador(controlador);
		for(int i=0; i< listaRelaciones.size(); i++){//para cada relación en el sistema
			TransferRelacion tr = listaRelaciones.get(i);
			if (tr.getTipo().equals("Debil")){//si esta relación es débil
				return true;
			}
		}
		return false;
	}
	
	public boolean tieneHermanoDebil(TransferEntidad te){
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> listaRelaciones = daoRelaciones.ListaDeRelaciones();
		ServiciosEntidades sEntidades= new ServiciosEntidades();
		sEntidades.setControlador(controlador);
		for(int i=0; i< listaRelaciones.size(); i++){//para cada relación en el sistema
			TransferRelacion tr = listaRelaciones.get(i);
			if (tr.getTipo().equals("Debil")){//si esta relación es débil
				Vector<EntidadYAridad> vectorEntidadesAridades = tr.getListaEntidadesYAridades();
				boolean tieneALaEntidad= false;
				boolean tieneAlgunaDebil=false;
				for(int j=0; j<vectorEntidadesAridades.size(); j++){
					int entidad =vectorEntidadesAridades.get(j).getEntidad();
					if(te.getIdEntidad()==entidad ){
						tieneALaEntidad=true;
					}
					if(sEntidades.esDebil(entidad)){
						tieneAlgunaDebil=true;
					}
				}
				if(tieneALaEntidad && tieneAlgunaDebil)
					return true;
			}
		}
		return false;
	}
	
	public int numEntidadesDebiles(TransferRelacion tr){
		Vector<EntidadYAridad> vectorEntidadesAridades = tr.getListaEntidadesYAridades();
		int numero=0;
		ServiciosEntidades sEntidades= new ServiciosEntidades();
		sEntidades.setControlador(controlador);
		for(int j=0; j<vectorEntidadesAridades.size(); j++){
			int entidad =vectorEntidadesAridades.get(j).getEntidad();
			if (sEntidades.esDebil(entidad))
				numero++;
		}
		return numero;
	}
	
	public int idEntidadDebil(TransferRelacion tr){
		Vector<EntidadYAridad> vectorEntidadesAridades = tr.getListaEntidadesYAridades();
		int entidad=0;
		ServiciosEntidades sEntidades= new ServiciosEntidades();
		sEntidades.setControlador(controlador);
		boolean encontrado= false;
		int j=0;
		while ((j<vectorEntidadesAridades.size())&&(!encontrado)){
			entidad =vectorEntidadesAridades.get(j).getEntidad();
			if (sEntidades.esDebil(entidad))
				encontrado = true;
			else
				j++;
		}
		return entidad;
	}
	/*
	 * Anadir un atributo a una relacion
	 * -> en v viene la relacion (pos 0) y el atributo (pos 1)
	 */
	public void anadirAtributo(Vector v){
		TransferRelacion tr = (TransferRelacion) v.get(0);
		TransferAtributo ta = (TransferAtributo) v.get(1);
		// Si nombre de atributo es vacio -> ERROR
		if (ta.getNombre().isEmpty()){ this.controlador.mensajeDesde_SR(TC.SR_AnadirAtributoARelacion_ERROR_NombreDeAtributoVacio, v); return; }
		// Si nombre de atributo ya existe en esa entidad-> ERROR
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		Vector<TransferAtributo> lista = daoAtributos.ListaDeAtributos(); //lista de todos los atributos
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_AnadirAtributoARelacion_ERROR_DAOAtributos,v);
			return;
		}
		for (int i=0; i<tr.getListaAtributos().size();i++)
			if(daoAtributos.nombreDeAtributo((Integer.parseInt((String)tr.getListaAtributos().get(i)))).toLowerCase().equals(ta.getNombre().toLowerCase())){ 
				controlador.mensajeDesde_SR(TC.SR_AnadirAtributoARelacion_ERROR_NombreDeAtributoYaExiste,v);
				return;
			}
		
		// Si hay tamano y no es un entero positivo -> ERROR
		if(v.size()==3){
			try{
				int tamano = Integer.parseInt((String) v.get(2));
				if(tamano<1){
					this.controlador.mensajeDesde_SR(TC.SR_AnadirAtributoARelacion_ERROR_TamanoEsNegativo, v); return;
				}			
			}
			catch(Exception e){
				this.controlador.mensajeDesde_SR(TC.SR_AnadirAtributoARelacion_ERROR_TamanoNoEsEntero, v); return;
			}
		}
		// Creamos el atributo
		ta.setPosicion(tr.nextAttributePos(ta.getPosicion()));
		//DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		int idNuevoAtributo = daoAtributos.anadirAtributo(ta);
		if(idNuevoAtributo == -1){this.controlador.mensajeDesde_SR(TC.SR_AnadirAtributoARelacion_ERROR_DAOAtributos, v); return; }
		// Anadimos el atributo a la lista de atributos de la relacion
		ta.setIdAtributo(idNuevoAtributo);
		tr.getListaAtributos().add(Integer.toString(idNuevoAtributo));
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (!daoRelaciones.modificarRelacion(tr)){this.controlador.mensajeDesde_SR(TC.SR_AnadirAtributoARelacion_ERROR_DAORelaciones, v); return; }
		// Si todo ha ido bien devolvemos al controlador la relacion modificada y el nuevo atributo
		this.controlador.mensajeDesde_SR(TC.SR_AnadirAtributoARelacion_HECHO, v); 
	}
	
	public void anadirRestriccion(Vector v){
		TransferRelacion tr = (TransferRelacion) v.get(0);
		String restriccion = (String) v.get(1);
		
		// Si nombre es vacio -> ERROR
		if (restriccion.isEmpty()){
			//controlador.mensajeDesde_SE(TC.SE_RenombrarRelacion_ERROR_NombreDeRelacionesVacio, v);
			return;
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAORelaciones,v);
			return;
		}
		
		Vector<String> vRestricciones = tr.getListaRestricciones();
		vRestricciones.add(restriccion);
		tr.setListaRestricciones(vRestricciones);
				
		//te.setNombre(nuevoNombre);
		if (daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_AnadirRestriccionARelacion_HECHO, v);
		return;
	}
	
	public void quitarRestriccion(Vector v){
		TransferRelacion te = (TransferRelacion) v.get(0);
		String restriccion = (String) v.get(1);
		
		// Si nombre es vacio -> ERROR
		if (restriccion.isEmpty()) return;
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAORelaciones,v);
			return;
		}
		
		Vector<String> vRestricciones = te.getListaRestricciones();
		int i =0;
		boolean encontrado = false;
		while(i<vRestricciones.size() && !encontrado){
			if(vRestricciones.get(i).equals(restriccion)){
				vRestricciones.remove(i);
				encontrado = true;
			}
			i++;
		}
		te.setListaRestricciones(vRestricciones);
		
		if (daoRelaciones.modificarRelacion(te))
			controlador.mensajeDesde_SR(TC.SR_QuitarRestriccionARelacion_HECHO, v);
		
		return;
	}
	
	public void setRestricciones(Vector v) {
		Vector restricciones = (Vector) v.get(0);
		TransferRelacion tr = (TransferRelacion) v.get(1);
		
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAORelaciones,v);
			return;
		}
		tr.setListaRestricciones(restricciones);
		if (daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_setRestriccionesARelacion_HECHO, v);
			
		return;		
	}
	
	public void anadirUnique(Vector v){
		TransferRelacion tr = (TransferRelacion) v.get(0);
		String unique = (String) v.get(1);
		
		// Si nombre es vacio -> ERROR
		if (unique.isEmpty()){
			//controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_NombreDeEntidadEsVacio, v);
			return;
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAOEntidades,v);
			return;
		}
		
		Vector<String> vUniques = tr.getListaUniques();
		vUniques.add(unique);
		tr.setListaUniques(vUniques);
				
		//te.setNombre(nuevoNombre);
		if (daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_AnadirUniqueARelacion_HECHO, v);
			
		return;
	}
	
	public void quitarUnique(Vector v){
		TransferRelacion tr = (TransferRelacion) v.get(0);
		String unique = (String) v.get(1);
		
		// Si nombre es vacio -> ERROR
		if (unique.isEmpty()){
			//controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_NombreDeEntidadEsVacio, v);
			return;
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAORelaciones,v);
			return;
		}
		
		Vector<String> vUniques = tr.getListaUniques();
		int i =0;
		boolean encontrado = false;
		while(i<vUniques.size() && !encontrado){
			if(vUniques.get(i).equals(unique)){
				vUniques.remove(i);
				encontrado = true;
			}
			i++;
		}
		tr.setListaUniques(vUniques);
		
		if (daoRelaciones.modificarRelacion(tr) == false){
			//te.setNombre(antiguoNombre);
			//controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades, v);
		}
		else{
			controlador.mensajeDesde_SR(TC.SR_QuitarUniqueARelacion_HECHO, v);
		}	
		return;
	}
	
	public void setUniques(Vector v) {
		Vector uniques = (Vector) v.get(0);
		TransferRelacion tr = (TransferRelacion) v.get(1);
		
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SR_RenombrarRelacion_ERROR_DAORelaciones,v);
			return;
		}
		tr.setListaUniques(uniques);
		if (daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_setUniquesARelacion_HECHO, v);
		return;		
	}
		
	/*
	 * Quitar/poner un Unique unitario a la entidad
	 * */
	public void setUniqueUnitario(Vector v) {
		TransferRelacion tr = (TransferRelacion) v.get(0);
		TransferAtributo ta= (TransferAtributo) v.get(1);
		Vector uniques = tr.getListaUniques();
		Vector uniquesCopia= new Vector();; 
		boolean encontrado=false;
		int i=0;
		while(i<uniques.size()){
			if(((TransferAtributo) uniques.get(i)).getNombre().equals(ta.getNombre())) encontrado=true;
			else uniquesCopia.add(uniques.get(i));
			i++;
		}
		if(!encontrado) uniquesCopia.add(ta.getNombre());
		
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAOEntidades,v);
			return;
		}
		tr.setListaUniques(uniquesCopia);
		if (daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_setUniqueUnitarioARelacion_HECHO, v);
		return;		
	}
	
	public void eliminarReferenciasUnitario(Vector v) {
		TransferRelacion tr = (TransferRelacion) v.get(0);
		TransferAtributo ta= (TransferAtributo) v.get(1);
		Vector uniques = tr.getListaUniques();
		Vector uniquesCopia= new Vector();
		int i=0;
		while(i<uniques.size()){
			if(uniques.get(i).toString().contains(ta.getNombre())){
				String s = uniques.get(i).toString();
				String text = ta.getNombre();
				int pos = s.indexOf(text);
				String s1 =s.substring(0,pos);
				String s2;
				if(s.indexOf(",",pos)>0){
					s2 = s.substring(s.indexOf(",",pos)+1,s.length());
					s= s1+s2;
				}else{
					s2 = "";
					if(s1.lastIndexOf(",")>0)
						s1= s1.substring(0,s.lastIndexOf(","));
					s= s1+s2;
				}
				s= s.replaceAll(" ", "");
				s= s.replaceAll(",",", ");
				uniquesCopia.add(s);
			}else uniquesCopia.add(uniques.get(i));
			i++;
		}
		
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAORelaciones,v);
			return;
		}
		tr.setListaUniques(uniquesCopia);
		if (daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_setUniqueUnitarioARelacion_HECHO, v);
		return;		
	}
	
	public void renombraUnique(Vector v) {
		TransferRelacion tr = (TransferRelacion) v.get(0);
		TransferAtributo ta= (TransferAtributo) v.get(1);
		String antiguoNombre = (String) v.get(2);
		Vector uniques = tr.getListaUniques();
		Vector uniquesCopia= new Vector();
		int i=0;
		while(i<uniques.size()){
			if(((TransferAtributo) uniques.get(i)).getNombre().contains(antiguoNombre)){
				String s = uniques.get(i).toString();
				s= s.replaceAll(antiguoNombre, ta.getNombre());
				uniquesCopia.add(s);
			}else uniquesCopia.add(uniques.get(i));
			i++;
		}
		
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> lista = daoRelaciones.ListaDeRelaciones();
		if (lista == null){
			controlador.mensajeDesde_SR(TC.SR_RenombrarRelacion_ERROR_DAORelaciones,v);
			return;
		}
		tr.setListaUniques(uniquesCopia);
		if (daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_setUniqueUnitarioARelacion_HECHO, v);
		return;
	}
	/*
 	* Mover una relacion (cambiar su posicion)
 	*/
	public void moverPosicionRelacion(TransferRelacion tr){
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if(daoRelaciones.modificarRelacion(tr) == false)
			controlador.mensajeDesde_SR(TC.SR_MoverPosicionRelacion_ERROR_DAORelaciones,tr);
		else
			controlador.mensajeDesde_SR(TC.SR_MoverPosicionRelacion_HECHO, tr);
	}
	
	
	/*
	 * Establecer la entidad padre en una relacion IsA
	 */
	public void establecerEntidadPadreEnRelacionIsA(Vector<Transfer> datos){
		TransferRelacion tr = (TransferRelacion) datos.get(0);
		TransferEntidad te = (TransferEntidad) datos.get(1);
		// Ponemos en la primera posicion de la lista de entidades la entidad padre
		Vector<EntidadYAridad> listaEntidades = tr.getListaEntidadesYAridades();
		tr.setRol("Padre");
		EntidadYAridad eya = new EntidadYAridad();
		eya.setEntidad(te.getIdEntidad());
		listaEntidades.add(0, eya);
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		String g;
		if (!daoRelaciones.modificarRelacion(tr))
			g="2";
			//controlador.mensajeDesde_SR(TC.SR_EstablecerEntidadPadre_ERROR_DAORelaciones, datos);
		//else controlador.mensajeDesde_SR(TC.SR_EstablecerEntidadPadre_HECHO, datos);
	}
	
	/*
	 * Quitar la entidad padre en una relacion IsA
	 * - Se quita la entidad padre y todas las hijas si las tiene
	 */
	public void quitarEntidadPadreEnRelacionIsA(TransferRelacion tr){
		tr.getListaEntidadesYAridades().removeAllElements();
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (!daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_QuitarEntidadPadre_ERROR_DAORelaciones, tr);
		else controlador.mensajeDesde_SR(TC.SR_QuitarEntidadPadre_HECHO, tr);
	}
	
	/*
	 * Anadir una entidad hija a una relacion IsA
	 */
	public void anadirEntidadHijaEnRelacionIsA(Vector<Transfer> datos){
		TransferRelacion tr = (TransferRelacion) datos.get(0);
		TransferEntidad te = (TransferEntidad) datos.get(1);
		// Anadimos la entidad hija a la lista de entidades la entidad padre
		Vector<EntidadYAridad> listaEntidades = tr.getListaEntidadesYAridades();
		EntidadYAridad eya = new EntidadYAridad();
		eya.setEntidad(te.getIdEntidad());
		listaEntidades.add(eya);
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (!daoRelaciones.modificarRelacion(tr))
			System.out.println("sda");
		// 	controlador.mensajeDesde_SR(TC.SR_AnadirEntidadHija_ERROR_DAORelaciones, datos);
		// else controlador.mensajeDesde_SR(TC.SR_AnadirEntidadHija_HECHO, datos);	
	}
	
		
	/*
	 * Quitar una entidad hija en una relacion IsA
	 */
	public void quitarEntidadHijaEnRelacionIsA(Vector<Transfer> datos){
		TransferRelacion tr = (TransferRelacion) datos.get(0);
		TransferEntidad te = (TransferEntidad) datos.get(1);
		// Quitamos la entidad hija de la lista de entidadesYaridades de la relacion
		Vector<EntidadYAridad> listaEntidades = tr.getListaEntidadesYAridades();
		int cont = 0;
		boolean salir = false;
		while(cont<listaEntidades.size() && !salir){
			EntidadYAridad eya = listaEntidades.get(cont);
			if(eya.getEntidad()==te.getIdEntidad()){
				listaEntidades.remove(cont);
				salir = true;
			}
			cont++;
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (!daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_QuitarEntidadHija_ERROR_DAORelaciones, datos);
		else controlador.mensajeDesde_SR(TC.SR_QuitarEntidadHija_HECHO, datos);	
	}
	
	/*
	 * Anadir una entidad a una relacion
	 */
	public void anadirEntidadARelacion(Vector v){
		// Sacamos los componentes del vector que sabemos que son correctos

		TransferRelacion tr = (TransferRelacion) v.get(0);
		TransferEntidad te = (TransferEntidad) v.get(1);
		String aux = (String)v.get(4);
		tr.setRol(aux);
		int idEntidad = te.getIdEntidad();
		//Comprobacion de que el rol que se va a asignar no está ya en esa relación
		for (Iterator it = tr.getListaEntidadesYAridades().iterator(); it.hasNext();){
			EntidadYAridad elem_tr = (EntidadYAridad)it.next();
			if(idEntidad == elem_tr.getEntidad()){
				if (elem_tr.getRol().toLowerCase().equals(tr.getRol().toLowerCase())){
					if(elem_tr.getRol().equals(""))
						controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDeRolNecesario,tr);
					else controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDelRolYaExiste,tr);
					return;
				}	
			}
		}
		// Obtenemos el inicio de rango. Si no es entero positivo o n -> ERROR y salimos
		String inicioEnCadena = (String) v.get(2);
		int inicioEnInt;
		//Obtenemos el final de rango. Si no es entero positivo o n -> ERROR y salimos
		String finalEnCadena = (String) v.get(3);
		int finalEnInt;
		//Si las cardinalidades son las dos vacías entonces se sobreentiende cardinalidad n a n
		if((inicioEnCadena.equals(""))&&(finalEnCadena.equals(""))){
			v.set(2, "n");
			inicioEnCadena = "n";
			v.set(3, "n");
			finalEnCadena = "n";
		}
		//Si no son ambas vacias, se comprueba que sean válidas
		if(inicioEnCadena.equals("n")) inicioEnInt = Integer.MAX_VALUE;	
		else{
			try{ inicioEnInt = Integer.parseInt(inicioEnCadena);}
			catch(Exception e){			
				controlador.mensajeDesde_SR(TC.SR_AnadirEntidadARelacion_ERROR_InicioNoEsEnteroOn, v); return;
			}
		}
		if(inicioEnInt<0){ controlador.mensajeDesde_SR(TC.SR_AnadirEntidadARelacion_ERROR_InicioEsNegativo, v); return;}
			
		if(finalEnCadena.equals("n")) finalEnInt = Integer.MAX_VALUE;
		else{
			try{ finalEnInt = Integer.parseInt(finalEnCadena);}
			catch(Exception e){	controlador.mensajeDesde_SR(TC.SR_AnadirEntidadARelacion_ERROR_FinalNoEsEnteroOn, v); return; }
		}
		if(finalEnInt<0){ controlador.mensajeDesde_SR(TC.SR_AnadirEntidadARelacion_ERROR_FinalEsNegativo, v); return; }
		// Aqui ya sabemos que los valores (individualmete) son correctos
		if(inicioEnInt>finalEnInt){	controlador.mensajeDesde_SR(TC.SR_AnadirEntidadARelacion_ERROR_InicioMayorQueFinal, v); return; }
		// Aqui ya sabemos que los valores (conjuntamente) son correctos
		Vector veya = tr.getListaEntidadesYAridades();
		EntidadYAridad eya = new EntidadYAridad();
		eya.setEntidad(idEntidad);
		eya.setPrincipioRango(inicioEnInt);
		eya.setFinalRango(finalEnInt);
		eya.setRol(tr.getRol());
		// Lo anadimos a la lista de entidades y aridades de la relacion y lo persistimos
		veya.add(eya);
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (!daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_AnadirEntidadARelacion_ERROR_DAORelaciones, v);
		return;
	}
	
	/*
	 * Editar la aridad de una entidad en una relacion
	 */
	public void editarAridadEntidad(Vector<Object> v){
		// Sacamos los componentes del vector que sabemos que son correctos
		TransferRelacion tr = (TransferRelacion) v.get(0);
		TransferEntidad te = (TransferEntidad) v.get(1);
		int idEntidad = te.getIdEntidad();
		String rol = (String)v.get(4);
		String rolViejo = (String)v.get(5);
		if(!rol.equals(rolViejo)){//Si he modificado el rol entonces compruebo que siga siendo válido
			//Comprobacion de que el rol que se va a asignar no está ya en esa relación
			for (Iterator it = tr.getListaEntidadesYAridades().iterator(); it.hasNext(); ){
				EntidadYAridad elem_tr = (EntidadYAridad)it.next();
					if(idEntidad == elem_tr.getEntidad()){
						if (elem_tr.getRol().toLowerCase().equals(rol.toLowerCase())){
							if(elem_tr.getRol().equals(""))
								controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDeRolNecesario,tr);
							else controlador.mensajeDesde_SR(TC.SR_InsertarRelacion_ERROR_NombreDelRolYaExiste,tr);
							return;
						}	
					}
			}	
		}

		// Obtenemos el inicio de rango. Si no es entero positivo o n -> ERROR y salimos
		String inicioEnCadena = (String) v.get(2);
		int inicioEnInt;
		if(inicioEnCadena.equals("n")) inicioEnInt = Integer.MAX_VALUE;
		else{
			try{ inicioEnInt = Integer.parseInt(inicioEnCadena);}
			catch(Exception e){	controlador.mensajeDesde_SR(TC.SR_EditarCardinalidadEntidad_ERROR_InicioNoEsEnteroOn, v); return;}
		}
		if(inicioEnInt<0){ controlador.mensajeDesde_SR(TC.SR_EditarCardinalidadEntidad_ERROR_InicioEsNegativo, v); return;}
		// Obtenemos el final de rango. Si no es entero positivo o n -> ERROR y salimos
		String finalEnCadena = (String) v.get(3);
		int finalEnInt;
		if(finalEnCadena.equals("n")) finalEnInt = Integer.MAX_VALUE;
		else{
			try{ finalEnInt = Integer.parseInt(finalEnCadena);}
			catch(Exception e){	controlador.mensajeDesde_SR(TC.SR_EditarCardinalidadEntidad_ERROR_FinalNoEsEnteroOn, v); return; }
		}
		if(finalEnInt<0){ controlador.mensajeDesde_SR(TC.SR_EditarCardinalidadEntidad_ERROR_FinalEsNegativo, v); return; }
		// Aqui ya sabemos que los valores (individualmete) son correctos
		if(inicioEnInt>finalEnInt){	controlador.mensajeDesde_SR(TC.SR_EditarCardinalidadEntidad_ERROR_InicioMayorQueFinal, v); return; }
		// Aqui ya sabemos que los valores (conjuntamente) son correctos
		Vector veya = tr.getListaEntidadesYAridades();
		int cont = 0;
		boolean salir = false;
		while(cont<veya.size() && !salir){
			EntidadYAridad eya = (EntidadYAridad) veya.get(cont);
			if((eya.getEntidad()==idEntidad) && (eya.getRol().equals(rolViejo))) salir = true;
			else cont++;
		}
		EntidadYAridad eya = (EntidadYAridad) veya.get(cont);
		eya.setPrincipioRango(inicioEnInt);
		eya.setFinalRango(finalEnInt);
		eya.setRol(rol);				
		veya.setElementAt(eya, cont);
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (!daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_EditarCardinalidadEntidad_ERROR_DAORelaciones, v);
		else controlador.mensajeDesde_SR(TC.SR_EditarCardinalidadEntidad_HECHO, v);
		return;
	}
	
	/*
	 * Forzar la aridad de una entidad en una relacion para que sea uno a uno
	 */
	public void aridadEntidadUnoUno(Vector v){
		controlador.mensajeDesde_SR(TC.SR_AridadEntidadUnoUno_HECHO, v);
		return;		
	} 
	/*
	 * Quitar una entidad de una relacion
	 */
	public void quitarEntidadARelacion(Vector<Object> datos){
		TransferRelacion tr = (TransferRelacion) datos.get(0);
		TransferEntidad te = (TransferEntidad) datos.get(1);
		String rol = (String)datos.get(2);
		// Quitamos la entidad de la lista de entidadesYaridades de la relacion
		Vector<EntidadYAridad> listaEntidades = tr.getListaEntidadesYAridades();
		int cont = 0;
		boolean salir = false;
		while(cont<listaEntidades.size() && !salir){
			EntidadYAridad eya = listaEntidades.get(cont);
			if(eya.getEntidad()==te.getIdEntidad()&& (eya.getRol().equals(rol))){			
				listaEntidades.remove(cont);
				salir = true;
			}
			cont++;
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		if (!daoRelaciones.modificarRelacion(tr))
			controlador.mensajeDesde_SR(TC.SR_QuitarEntidadARelacion_ERROR_DAORelaciones, datos);
		else controlador.mensajeDesde_SR(TC.SR_QuitarEntidadARelacion_HECHO, datos);
	}
	
	public boolean tieneAtributo(TransferRelacion tr, TransferAtributo ta){
		for (int i=0; i<tr.getListaAtributos().size(); i++){
			if(Integer.parseInt(((TransferAtributo) tr.getListaAtributos().get(i)).getNombre())==ta.getIdAtributo())
				return true;
		}
		return false;
	}
		
	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}
}
