package modelo.servicios;

import java.util.Iterator;
import java.util.Vector;

import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import persistencia.DAOAtributos;
import persistencia.DAOEntidades;
import persistencia.DAORelaciones;
import persistencia.EntidadYAridad;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ServiciosEntidades {
	private Controlador controlador;
	
	public void ListaDeEntidades(){
		DAOEntidades dao = new DAOEntidades(this.controlador.getPath());
		Vector <TransferEntidad> lista_entidades = dao.ListaDeEntidades();
		controlador.mensajeDesde_SE(TC.SE_ListarEntidades_HECHO, lista_entidades);
	}
	
	/* Anadir Entidad
	 * Parametros: un TransferEntidad que contiene el nombre de la nueva entidad y la posicion donde debe ir dibujado.
	 * Devuelve: La entidad en un TransferEntidad y el mensaje -> SE_InsertarEntidad_HECHO
	 * Condiciones:
	 * Si el nombre es vacio -> SE_InsertarEntidad_ERROR_NombreDeEntidadEsVacio
	 * Si el nombre ya existe -> SE_InsertarEntidad_ERROR_NombreDeEntidadYaExiste
	 * Si al usar el DAOEntidades se produce un error -> SE_InsertarEntidad_ERROR_DAO
	 */
	public void anadirEntidad(TransferEntidad te){
		if (te.getNombre().isEmpty()){
			controlador.mensajeDesde_SE(TC.SE_InsertarEntidad_ERROR_NombreDeEntidadEsVacio, null);
			return;
		}
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		for (Iterator it = lista.iterator(); it.hasNext(); ){
			TransferEntidad elem_te = (TransferEntidad)it.next();
			if (elem_te.getNombre().toLowerCase().equals(te.getNombre().toLowerCase())){
				controlador.mensajeDesde_SE(TC.SE_InsertarEntidad_ERROR_NombreDeEntidadYaExiste,te);
				return;
			}
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> listaR = daoRelaciones.ListaDeRelaciones();
		for (Iterator it = listaR.iterator(); it.hasNext(); ){
			TransferRelacion elem_tr = (TransferRelacion)it.next();
			if (elem_tr.getNombre().toLowerCase().equals(te.getNombre().toLowerCase())){
				controlador.mensajeDesde_SE(TC.SE_InsertarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion,te);
				return;
			}
		}
		//Aquí se añade la entidad
		int id = daoEntidades.anadirEntidad(te);
		if (id==-1)	controlador.mensajeDesde_SE(TC.SE_InsertarEntidad_ERROR_DAO,null);
//		else{
//
//			te.setIdEntidad(id);
////			controlador.mensajeDesde_SE(TC.SE_InsertarEntidad_HECHO, daoEntidades.consultarEntidad(te));
//		}
	}
	
	/* Se puede Anadir Entidad
	 * Realiza las comprobaciones oportunas para ver si se puede introducir una entidad pero NO la inserta, simplemente
	 * devuelve true o false al controlador indicando si se puede realizar la acción.
	 * Parametros: un TransferEntidad que contiene el nombre de la nueva entidad y la posicion donde debe ir dibujado.
	 * Devuelve: La entidad en un TransferEntidad y el mensaje -> SE_InsertarEntidad_HECHO
	 * Condiciones:
	 * Si el nombre es vacio -> SE_InsertarEntidad_ERROR_NombreDeEntidadEsVacio
	 * Si el nombre ya existe -> SE_InsertarEntidad_ERROR_NombreDeEntidadYaExiste
	 * Si al usar el DAOEntidades se produce un error -> SE_InsertarEntidad_ERROR_DAO
	 */
	public boolean SePuedeAnadirEntidad(TransferEntidad te){
		if (te.getNombre().isEmpty()){
			controlador.mensajeDesde_SE(TC.SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadEsVacio, null);
			return false;
		}
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		for (Iterator it = lista.iterator(); it.hasNext(); ){
			TransferEntidad elem_te = (TransferEntidad)it.next();
			if (elem_te.getNombre().toLowerCase().equals(te.getNombre().toLowerCase())){
				controlador.mensajeDesde_SE(TC.SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadYaExiste,te);
				return false;
			}
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> listaR = daoRelaciones.ListaDeRelaciones();
		for (Iterator it = listaR.iterator(); it.hasNext(); ){
			TransferRelacion elem_tr = (TransferRelacion)it.next();
			if (elem_tr.getNombre().toLowerCase().equals(te.getNombre().toLowerCase())){
				controlador.mensajeDesde_SE(TC.SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion,te);
				return false;
			}
		}
		return true;
	}
	
	
	
	/* 
	 * Renombrar una en entidad
	 * -> Recibe la entidad y el nuevo nombre
	 */
	
	public void renombrarEntidad(Vector v){
		TransferEntidad te = (TransferEntidad) v.get(0);
		String nuevoNombre = (String) v.get(1);
		String antiguoNombre = te.getNombre();
		// Si nombre es vacio -> ERROR
		if (nuevoNombre.isEmpty()){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_NombreDeEntidadEsVacio, v);
			return;
		}
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		for (Iterator it = lista.iterator(); it.hasNext(); ){
			TransferEntidad elem_te = (TransferEntidad)it.next();
			if (elem_te.getNombre().toLowerCase().equals(nuevoNombre.toLowerCase()) && (elem_te.getIdEntidad() != te.getIdEntidad())){
				controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_NombreDeEntidadYaExiste,v);
				return;
			}
		}
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> listaR = daoRelaciones.ListaDeRelaciones();
		if (listaR == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAORelaciones,v);
			return;
		}
		for (Iterator it = listaR.iterator(); it.hasNext(); ){
			TransferRelacion elem_tr = (TransferRelacion)it.next();
			if (elem_tr.getNombre().toLowerCase().equals(nuevoNombre.toLowerCase())){
				controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion,te);
				return;                        
			}
		}
		te.setNombre(nuevoNombre);
		if (daoEntidades.modificarEntidad(te) == false){
			te.setNombre(antiguoNombre);
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades, v);
		}
		else{
			v.add(antiguoNombre);
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_HECHO, v);
		}
			
		return;
	}
	
	/* Debilitar/Fortalecer una entidad
	 * -> Entidad a la que hay que voltear su caracter debil
	 */
	public void debilitarEntidad(TransferEntidad te){
		te.setDebil(!te.isDebil());
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		if (daoEntidades.modificarEntidad(te) == false){
			te.setDebil(!te.isDebil());
			controlador.mensajeDesde_SE(TC.SE_DebilitarEntidad_ERROR_DAOEntidades, te);
		}
		else controlador.mensajeDesde_SE(TC.SE_DebilitarEntidad_HECHO, te);
		return;
	}
	
	public boolean esDebil(int i){
		DAOEntidades dao = new DAOEntidades(this.controlador.getPath());
		Vector <TransferEntidad> listaentidades = dao.ListaDeEntidades();
		for (int j=0; j<listaentidades.size(); j++){
			if (listaentidades.get(j).getIdEntidad()==i) 
				return listaentidades.get(j).isDebil();
		}
		return false;
	}
	

	/*
	 * Anadir un atributo a una entidad
	 * -> en v viene la entidad (pos 0) y el atributo (pos 1)
	 */
	public void anadirAtributo(Vector v){
		TransferEntidad te = (TransferEntidad) v.get(0);
		TransferAtributo ta = (TransferAtributo) v.get(1);
		// Si nombre de atributo es vacio -> ERROR
		if (ta.getNombre().isEmpty()){ this.controlador.mensajeDesde_SE(TC.SE_AnadirAtributoAEntidad_ERROR_NombreDeAtributoVacio, v); return; }
		
		// Si nombre de atributo ya existe en esa entidad-> ERROR
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		Vector<TransferAtributo> lista = daoAtributos.ListaDeAtributos(); //lista de todos los atributos
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_AnadirAtributoAEntidad_ERROR_DAOAtributos,v);
			return;
		}
		// for (int i=0; i<te.getListaAtributos().size();i++)
		// 	if(daoAtributos.nombreDeAtributo((Integer.parseInt((String)te.getListaAtributos().get(i)))).toLowerCase().equals(ta.getNombre().toLowerCase())){ 
		// 		controlador.mensajeDesde_SE(TC.SE_AnadirAtributoAEntidad_ERROR_NombreDeAtributoYaExiste,v);
		// 		return;
		// 	}
		
		// Si hay tamano y no es un entero positivo -> ERROR
		if(v.size()==3){
			try{
				int tamano = Integer.parseInt((String) v.get(2));
				if(tamano<1){
					this.controlador.mensajeDesde_SE(TC.SE_AnadirAtributoAEntidad_ERROR_TamanoEsNegativo, v); return;
				}			
			}
			catch(Exception e){
				this.controlador.mensajeDesde_SE(TC.SE_AnadirAtributoAEntidad_ERROR_TamanoNoEsEntero, v); return;
			}
		}
		// Creamos el atributo
		ta.setPosicion(te.nextAttributePos(ta.getPosicion()));
		int idNuevoAtributo = daoAtributos.anadirAtributo(ta);
		if(idNuevoAtributo == -1){this.controlador.mensajeDesde_SE(TC.SE_AnadirAtributoAEntidad_ERROR_DAOAtributos, v); return; }
		// Anadimos el atributo a la lista de atributos de la entidad
		ta.setIdAtributo(idNuevoAtributo);
		te.getListaAtributos().add(Integer.toString(idNuevoAtributo));
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		if (!daoEntidades.modificarEntidad(te)){
			this.controlador.mensajeDesde_SE(TC.SE_AnadirAtributoAEntidad_ERROR_DAOEntidades, v);
			return;
		}
		
		// Si todo ha ido bien devolvemos al controlador la entidad modificada y el nuevo atributo
		this.controlador.mensajeDesde_SE(TC.SE_AnadirAtributoAEntidad_HECHO, v); 
	}

	
	/* Eliminar entidad
	 * Parametros: el TransferEntidad que contiene la entidad que se desea eliminar
	 * Devuelve: Un TransferEntidad que contiene la entidad eliminada y el mensaje -> SE_EliminarEntidad_HECHO
	 * Condiciones:
	 * Se se produce un error al usar el DAOEntidades -> SE_EliminarEntidad_ERROR_DAOEntidades 
	 */
	public void eliminarEntidad(TransferEntidad te){
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		// Eliminamos la entidad
		if (daoEntidades.borrarEntidad(te) == false)
			controlador.mensajeDesde_SE(TC.SE_EliminarEntidad_ERROR_DAOEntidades, te);
		else{
			Vector<TransferRelacion> vectorRelacionesModificadas = this.eliminaRefererenciasAEntidad(te);
			Vector<Object> vectorEntidadEliminadaYvectorRelacionesModificadas = new Vector<Object>();
			vectorEntidadEliminadaYvectorRelacionesModificadas.add(te);
			vectorEntidadEliminadaYvectorRelacionesModificadas.add(vectorRelacionesModificadas);
			controlador.mensajeDesde_SE(TC.SE_EliminarEntidad_HECHO, vectorEntidadEliminadaYvectorRelacionesModificadas);
		}
		return;
	}
	
	/*
	 * Unicamente una entidad puede estar referenciada en las relaciones que hay en el sistema. Puede darse
	 * el caso de que una entidad no este referenciada (la entidad esta sola). En este caso al eliminar las
	 * referencias no se modifica ningun elemento del sistema (devolveremos null).
	 * Por el contrario, si esta referenciada, puede estarlo en varias relaciones. Para cada una de las
	 * relaciones la modificaremos quitando la referencia, la persistimos.
	 * Deolveremos un vector de relaciones modificadas. Cuando no este referenciada, el vector estara vacio.
	 */
	private Vector<TransferRelacion> eliminaRefererenciasAEntidad(TransferEntidad te){
		Vector<TransferRelacion> vectorRelaciones = new Vector<TransferRelacion>();
		int idEntidad = te.getIdEntidad();
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector<TransferRelacion> listaRelaciones = daoRelaciones.ListaDeRelaciones();
		int contListaRelaciones = 0;
		while (contListaRelaciones<listaRelaciones.size()){
			// Obtenemos la relacion
			TransferRelacion tr = listaRelaciones.get(contListaRelaciones);
			// Obtenemos la lista de entidades y aridades
			Vector listaEntidadesYAridades = tr.getListaEntidadesYAridades();
			// Recorremos la lista
			int contListaEntidadesYAridades = 0;
			boolean referenciada = false;
			while (contListaEntidadesYAridades<listaEntidadesYAridades.size() && !referenciada){
				EntidadYAridad eya = (EntidadYAridad) listaEntidadesYAridades.get(contListaEntidadesYAridades);
				// Si esta referenciad
				if (eya.getEntidad()==idEntidad){
					referenciada = true;
					listaEntidadesYAridades.remove(contListaEntidadesYAridades);
				}
				contListaEntidadesYAridades++;
			}
			// Si estaba referenciada la moficamos en la persistecia
			if (referenciada){
				daoRelaciones = new DAORelaciones(this.controlador.getPath());
				daoRelaciones.modificarRelacion(tr);
				vectorRelaciones.add(tr);
			}
			contListaRelaciones++;
		}
		return vectorRelaciones;
	}
	
	public void anadirRestriccion(Vector v){
		TransferEntidad te = (TransferEntidad) v.get(0);
		String restriccion = (String) v.get(1);
		
		if (restriccion.isEmpty()) return;
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		
		Vector<String> vRestricciones = te.getListaRestricciones();
		vRestricciones.add(restriccion);
		te.setListaRestricciones(vRestricciones);

		if (daoEntidades.modificarEntidad(te) == false)
			controlador.mensajeDesde_SE(TC.SE_AnadirRestriccionAEntidad_HECHO, v);
		
		return;
	}
	
	public void quitarRestriccion(Vector v){
		TransferEntidad te = (TransferEntidad) v.get(0);
		String restriccion = (String) v.get(1);
		
		if (restriccion.isEmpty()) return;
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
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
		
		if (daoEntidades.modificarEntidad(te))
			controlador.mensajeDesde_SE(TC.SE_QuitarRestriccionAEntidad_HECHO, v);
		
		return;
	}
	
	public void setRestricciones(Vector v) {
		Vector restricciones = (Vector) v.get(0);
		TransferEntidad te = (TransferEntidad) v.get(1);
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		te.setListaRestricciones(restricciones);
		if (daoEntidades.modificarEntidad(te))
			controlador.mensajeDesde_SE(TC.SE_setRestriccionesAEntidad_HECHO, v);
		return;		
	}

	public void anadirUnique(Vector v){
		TransferEntidad te = (TransferEntidad) v.get(0);
		String unique = (String) v.get(1);
		
		// Si nombre es vacio -> ERROR
		if (unique.isEmpty()) return;
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		
		Vector<String> vUniques = te.getListaUniques();
		vUniques.add(unique);
		te.setListaUniques(vUniques);
				
		//te.setNombre(nuevoNombre);
		if (daoEntidades.modificarEntidad(te))
			controlador.mensajeDesde_SE(TC.SE_AnadirUniqueAEntidad_HECHO, v);
		return;
	}
	
	public void quitarUnique(Vector v){
		TransferEntidad te = (TransferEntidad) v.get(0);
		String unique = (String) v.get(1);
		
		if (unique.isEmpty()) return;
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		
		Vector<String> vUniques = te.getListaUniques();
		int i =0;
		boolean encontrado = false;
		while(i<vUniques.size() && !encontrado){
			if(vUniques.get(i).equals(unique)){
				vUniques.remove(i);
				encontrado = true;
			}
			i++;
		}
		te.setListaUniques(vUniques);
		
		if (daoEntidades.modificarEntidad(te))
			controlador.mensajeDesde_SE(TC.SE_QuitarUniqueAEntidad_HECHO, v);
		return;
	}
	
	public void setUniques(Vector v) {
		Vector uniques = (Vector) v.get(0);
		TransferEntidad te = (TransferEntidad) v.get(1);
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		te.setListaUniques(uniques);
		if (daoEntidades.modificarEntidad(te))
			controlador.mensajeDesde_SE(TC.SE_setUniquesAEntidad_HECHO, v);
		
		return;		
	}
	
	/*
	 * Quitar/poner un Unique unitario a la entidad
	 * */
	public void setUniqueUnitario(Vector v) {
		TransferEntidad te = (TransferEntidad) v.get(0);
		TransferAtributo ta= (TransferAtributo) v.get(1);
		Vector uniques = te.getListaUniques();
		Vector uniquesCopia= new Vector();; 
		boolean encontrado=false;
		int i=0;
		while(i<uniques.size()){
			if(((TransferAtributo) uniques.get(i)).getNombre().equals(ta.getNombre())) encontrado=true;
			else uniquesCopia.add(uniques.get(i));			
			i++;
		}
		if(!encontrado)
			uniquesCopia.add(ta.getNombre());
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		te.setListaUniques(uniquesCopia);
		if (daoEntidades.modificarEntidad(te))
			controlador.mensajeDesde_SE(TC.SE_setUniqueUnitarioAEntidad_HECHO, v);
		
		return;		
	}
	
	public void eliminarReferenciasUnitario(Vector v) {
		TransferEntidad te = (TransferEntidad) v.get(0);
		TransferAtributo ta= (TransferAtributo) v.get(1);
		Vector uniques = te.getListaUniques();
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
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		te.setListaUniques(uniquesCopia);
		if (daoEntidades.modificarEntidad(te))
			controlador.mensajeDesde_SE(TC.SE_setUniqueUnitarioAEntidad_HECHO, v);
		
		return;		
	}
	
	public void renombraUnique(Vector v) {
		TransferEntidad te = (TransferEntidad) v.get(0);
		TransferAtributo ta= (TransferAtributo) v.get(1);
		String antiguoNombre = (String) v.get(2);
		Vector uniques = te.getListaUniques();
		Vector uniquesCopia= new Vector();; 
		int i=0;
		while(i<uniques.size()){
			if(((TransferAtributo) uniques.get(i)).getNombre().contains(antiguoNombre)){
				String s = uniques.get(i).toString();
				s= s.replaceAll(antiguoNombre, ta.getNombre());
				uniquesCopia.add(s);
			}else uniquesCopia.add(uniques.get(i));
			i++;
		}
		
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector<TransferEntidad> lista = daoEntidades.ListaDeEntidades();
		if (lista == null){
			controlador.mensajeDesde_SE(TC.SE_RenombrarEntidad_ERROR_DAOEntidades,v);
			return;
		}
		te.setListaUniques(uniquesCopia);
		if (daoEntidades.modificarEntidad(te))
			controlador.mensajeDesde_SE(TC.SE_setUniqueUnitarioAEntidad_HECHO, v);
		return;		
	}

	/*
 	* Mover una entidad (cambiar su posicion)
 	*/
	public void moverPosicionEntidad(TransferEntidad te){
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		if(daoEntidades.modificarEntidad(te) == false)
			controlador.mensajeDesde_SE(TC.SE_MoverPosicionEntidad_ERROR_DAOEntidades, te);
		else
			controlador.mensajeDesde_SE(TC.SE_MoverPosicionEntidad_HECHO, te);
	}
	
	public boolean tieneAtributo(TransferEntidad te, TransferAtributo ta){
		for (int i=0; i<te.getListaAtributos().size(); i++){
			if(Integer.parseInt((String) te.getListaAtributos().get(i))==ta.getIdAtributo())
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
