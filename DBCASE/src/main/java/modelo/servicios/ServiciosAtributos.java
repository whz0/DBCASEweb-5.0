package modelo.servicios;

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

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ServiciosAtributos {

	private Controlador controlador;

	public void ListaDeAtributos(){
		DAOAtributos dao = new DAOAtributos(this.controlador);
		Vector <TransferAtributo> lista_atributos = dao.ListaDeAtributos();
		controlador.mensajeDesde_SA(TC.SA_ListarAtributos_HECHO, lista_atributos);
	}
	
	public Vector<TransferAtributo> DevuelveListaDeAtributos(){
		DAOAtributos dao = new DAOAtributos(this.controlador);
		Vector <TransferAtributo> lista_atributos = dao.ListaDeAtributos();
		controlador.mensajeDesde_SA(TC.SA_ListarAtributos_HECHO, lista_atributos);
		return lista_atributos;
	}
	

	/* Añadir atributo
	 * -> en v viene el atributo padre (pos 0) y el atributo hijo (pos 1)
	 */
	
	public void anadirAtributo(Vector v){
		TransferAtributo tap = (TransferAtributo) v.get(0);
		TransferAtributo tah = (TransferAtributo) v.get(1);
		// Si nombre de atributo hijo es vacio -> ERROR
		if (tah.getNombre().isEmpty()){
			this.controlador.mensajeDesde_SA(TC.SA_AnadirSubAtributoAtributo_ERROR_NombreDeAtributoVacio, v);
			return; 
		}
		// Si nombre de atributo ya existe en esa entidad-> ERROR
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		Vector<TransferAtributo> lista = daoAtributos.ListaDeAtributos(); //lista de todos los atributos
		if (lista == null){
			controlador.mensajeDesde_SA(TC.SA_AnadirSubAtributoAtributo_ERROR_DAOAtributosHijo,v);
			return;
		}
		for (int i=0; i<tap.getListaComponentes().size();i++)
			if(daoAtributos.nombreDeAtributo((Integer.parseInt((String)tap.getListaComponentes().get(i)))).toLowerCase().equals(tah.getNombre().toLowerCase())){ 
				controlador.mensajeDesde_SA(TC.SA_AnadirSubAtributoAtributo_ERROR_NombreDeAtributoYaExiste,v);
				return;
			}
		
		
		// Si hay tamano y no es un entero positivo -> ERROR
		if(v.size()==3){
			try{
				int tamano = Integer.parseInt((String) v.get(2));
				if(tamano<1){
					this.controlador.mensajeDesde_SA(TC.SA_AnadirSubAtributoAtributo_ERROR_TamanoEsNegativo, v); return;
				}			
			}
			catch(Exception e){
				this.controlador.mensajeDesde_SA(TC.SA_AnadirSubAtributoAtributo_ERROR_TamanoNoEsEntero, v); return;
			}
		}
		// Creamos el atributo
		//DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		int idNuevoAtributo = daoAtributos.anadirAtributo(tah);
		if(idNuevoAtributo == -1){
			this.controlador.mensajeDesde_SA(TC.SA_AnadirSubAtributoAtributo_ERROR_DAOAtributosHijo, v);
			return; 
		}
		// Anadimos el atributo a la lista de subatributos del atributo
		tah.setIdAtributo(idNuevoAtributo);
		tap.getListaComponentes().add(Integer.toString(idNuevoAtributo));
		if (!daoAtributos.modificarAtributo(tap)){
			this.controlador.mensajeDesde_SA(TC.SA_AnadirSubAtributoAtributo_ERROR_DAOAtributosPadre, v);
			return;
		}
		// Si todo ha ido bien devolvemos al controlador la el atributo padre modificado y el nuevo atributo
		this.controlador.mensajeDesde_SA(TC.SA_AnadirSubAtributoAtributo_HECHO, v);
	}


	/*
	 * Eliminar atributo
	 * Parametros: recibe un transfer atributo del con el atributo a eliminar
	 * Devuelve:
	 * el transfer atributo que contiene el atributo eliminado y el mensaje SA_EliminarAtributo_HECHO
	 * Condiciones:
	 * Si es un atributo compuesto hay que eliminar tambien sus subatributos.
	 * Si se produce un error al usar el DAOAtributos ->  SA_EliminarAtributo_ERROR_DAOAtributos
	 * Hay que comprobar primero que el atributo que viene en el transfer exista con un consultar
	 */
	public void eliminarAtributo (TransferAtributo ta){
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		ta = daoAtributos.consultarAtributo(ta);
		if (ta == null){
			//el atributo puede haber sido eliminado al eliminar la entidad o relacion a la que pertenecía
			//al hacer una eliminación de múltiples nodos 
			//controlador.mensajeDesde_SA(TC.SA_EliminarAtributo_ERROR_DAOAtributos, ta);
			return;
		}
		// Si no es compuesto
		if (!ta.getCompuesto()){
			if (daoAtributos.borrarAtributo(ta) == false)
				controlador.mensajeDesde_SA(TC.SA_EliminarAtributo_ERROR_DAOAtributos, ta);
			else{
				Transfer elem_mod = this.eliminaRefererenciasAlAtributo(ta);
				Vector<Transfer> vectorAtributoYElemMod = new Vector<Transfer>();
				vectorAtributoYElemMod.add(ta);
				vectorAtributoYElemMod.add(elem_mod);
				controlador.mensajeDesde_SA(TC.SA_EliminarAtributo_HECHO, vectorAtributoYElemMod);
			}
		}
		/*
		 * Si es compuesto...
		 * 1.- Obtenemos sus subatributos
		 * 2.- Los eliminamos recursivamente
		 * 3.- Mandamos al controlador el atributo que se ha eliminado y el elem modificado tras
		 * 	   tras usar el metodo elimina referencias.
		 */

		else{
			Vector lista_idSubatributos = ta.getListaComponentes();
			int cont = 0;
			while (cont < lista_idSubatributos.size()){
				int idAtributoHijo = Integer.parseInt((String) lista_idSubatributos.get(cont));
				TransferAtributo ta_hijo = new TransferAtributo(controlador);
				ta_hijo.setIdAtributo(idAtributoHijo);
				this.eliminarAtributo(ta_hijo);
				cont++;
			}
			// Ya estan eliminados todos sus subatributos. Ponemos compuesto a falso y eliminamos
			ta.setCompuesto(false);
			daoAtributos = new DAOAtributos(controlador);
			daoAtributos.modificarAtributo(ta);
			System.out.println("asas");
			this.eliminarAtributo(ta);
		}
	}



	/* 
	 * Este metodo sirve para eliminar las referencias que hay hacia un atributo. Las referencias
	 * pueden provenir de 3 lugares: de una entidad, de una relacion o de otro atibuto (si es hijo
	 * de un atributo compuesto). El metodo moficara el elemento (uno de estos 3) que lo referencia
	 * y lo devolvera en un transfer para comunicar la modificacion al controlador.
	 */
	private Transfer eliminaRefererenciasAlAtributo(TransferAtributo ta){
		// Obtenemos el identificador del atributo
		int idAtributo = ta.getIdAtributo();
		boolean enEntidad = false;
		boolean enRelacion = false;
		boolean enAtributo = false;

		// Buscamos si esta en entidades
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector listaEntidades = daoEntidades.ListaDeEntidades();
		int j = 0;
		while (j < listaEntidades.size()&& !enEntidad){
			// Obetenemos la entidad y la lista de atributos de la entidad
			TransferEntidad te = (TransferEntidad) listaEntidades.get(j);
			Vector listaAtributos = te.getListaAtributos();
			int k = 0;
			while (k < listaAtributos.size()&& !enEntidad){
				int id_posible = Integer.parseInt((String) listaAtributos.get(k));
				if (idAtributo == id_posible){
					// Es un atributo de una entidad
					enEntidad=true;
					// Lo quitamos de la lista
					listaAtributos.remove(k);
					// Si esta en la lista de claves primarias la quitamos
					Vector listaClaves = te.getListaClavesPrimarias();
					int l = 0;
					while (l < listaClaves.size()){
						int id_clave = Integer.parseInt((String) listaClaves.get(l));
						if (id_clave == idAtributo){
							listaClaves.remove(l);
							te.setListaClavesPrimarias(listaClaves);
						}
						l++;
					}
					// Modificamos en la persistencia la entidad y lo devolvemos
					daoEntidades.modificarEntidad(te);
					return te;
				}
				k++;
			}
			j++;
		}
		// Buscamos si esta en relaciones
		// Sabemos que no puede estar en una entidad y relacion a la vez.
		if(!enEntidad){
			DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
			Vector listaRelaciones = daoRelaciones.ListaDeRelaciones();
			j = 0;
			while (j < listaRelaciones.size()&& !enRelacion){
				// Obetenemos la relacion y la lista de atributos de la relacion
				TransferRelacion tr = (TransferRelacion) listaRelaciones.get(j);
				Vector listaAtributos = tr.getListaAtributos();
				int k = 0;
				while (k < listaAtributos.size()&& !enRelacion){
					int id_posible = Integer.parseInt((String) listaAtributos.get(k));
					if (idAtributo == id_posible){
						// Es un atributo de una relacion
						enRelacion = true;
						// Lo quitamos de la lista
						listaAtributos.remove(k);
						// Modificamos en la persistencia la relacion y la devolvemos
						daoRelaciones.modificarRelacion(tr);
						return tr;
					}
					k++;
				}
				j++;
			}
		}

		if (!enEntidad && !enRelacion){
			// Buscamos si esta en atributos, es decir es un subatributo de un compuesto.
			DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
			Vector listaAtributos = daoAtributos.ListaDeAtributos();
			j = 0;
			while (j < listaAtributos.size()&& !enAtributo){
				// Obetenemos el atributo y la lista de subatributos del atributo
				TransferAtributo ta_padre = (TransferAtributo) listaAtributos.get(j);
				Vector listaSubatributos = ta_padre.getListaComponentes();
				int k = 0;
				while (k < listaSubatributos.size()&& !enAtributo){
					int id_posible = Integer.parseInt((String) listaSubatributos.get(k));
					if (idAtributo == id_posible){
						// Es un subatributo de un atributo
						enAtributo = true;
						// Lo quitamos de la lista de componentes
						listaSubatributos.remove(k);
						// Modificamos en la persistencia el atributo y lo devolvemos
						daoAtributos.modificarAtributo(ta_padre);
						return ta_padre;
					}
					k++;
				}
				j++;
			}
		}
		// Si devuelve null es que el atributo no esta referenciado (ERROR!)
		return null;
	}


	/*
	 * Renombrar atributo
	 * -> Recibe el atributo y el nuevo nombre
	 */
	public void renombrarAtributo(Vector v){
		TransferAtributo ta = (TransferAtributo) v.get(0);
		String nuevoNombre = (String) v.get(1);
		String antiguoNombre = ta.getNombre();
		// Si el nombre es vacio -> ERROR
		if (nuevoNombre.isEmpty()){
			controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_ERROR_NombreDeAtributoEsVacio, v);
			return;
		}
		int idAtributo = ta.getIdAtributo();
		boolean encontrado=false;
		// Buscamos si esta en entidades
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		Vector listaEntidades = daoEntidades.ListaDeEntidades();
		int j = 0;
		while (j < listaEntidades.size()&&!encontrado){
			// Obetenemos la entidad y la lista de atributos de la entidad
			TransferEntidad te = (TransferEntidad) listaEntidades.get(j);
			Vector listaAtributos = te.getListaAtributos();
			int k = 0;
			while (k < listaAtributos.size()){
				int id_posible = Integer.parseInt((String) listaAtributos.get(k));
				if (idAtributo == id_posible){
					// Es un atributo de una entidad
					encontrado=true;
					// Si nombre de atributo ya existe en esa entidad-> ERROR
					DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
					for (int i=0; i<te.getListaAtributos().size();i++)
						if(daoAtributos.nombreDeAtributo((Integer.parseInt((String)te.getListaAtributos().get(i)))).toLowerCase().equals(nuevoNombre.toLowerCase())
								&& i!=k){ 
							controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_ERROR_NombreDeAtributoYaExiste,v);
							return;
						}
				}
				k++;
			}
			j++;
		}
		// Buscamos si esta en relaciones
		// Sabemos que no puede estar en una entidad y relacion a la vez.
		DAORelaciones daoRelaciones = new DAORelaciones(this.controlador.getPath());
		Vector listaRelaciones = daoRelaciones.ListaDeRelaciones();
		j = 0;
		while (j < listaRelaciones.size()&&!encontrado){
			// Obetenemos la relacion y la lista de atributos de la relacion
			TransferRelacion tr = (TransferRelacion) listaRelaciones.get(j);
			Vector listaAtributos = tr.getListaAtributos();
			int k = 0;
			while (k < listaAtributos.size()){
				int id_posible = Integer.parseInt((String) listaAtributos.get(k));
				if (idAtributo == id_posible){
					// Es un atributo de una relacion
					encontrado=true;
					
					// Si nombre de atributo ya existe en esa entidad-> ERROR
					DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
					for (int i=0; i<tr.getListaAtributos().size();i++)
						if(daoAtributos.nombreDeAtributo((Integer.parseInt((String)tr.getListaAtributos().get(i)))).toLowerCase().equals(nuevoNombre.toLowerCase())
								&& i!=k){ 
							controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_ERROR_NombreDeAtributoYaExiste,v);
							return;
						}
				}
				k++;
			}
			j++;
		}
		// Buscamos si esta en atributos, es decir es un subatributo de un compuesto.
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		Vector listaAtributos = daoAtributos.ListaDeAtributos();
		j = 0;
		while (j < listaAtributos.size()){
			// Obetenemos el atributo y la lista de subatributos del atributo
			TransferAtributo ta_padre = (TransferAtributo) listaAtributos.get(j);
			Vector listaSubatributos = ta_padre.getListaComponentes();
			int k = 0;
			while (k < listaSubatributos.size() &&!encontrado){
				int id_posible = Integer.parseInt((String) listaSubatributos.get(k));
				if (idAtributo == id_posible){
					// Es un subatributo de un atributo
					encontrado=true;
					// Si nombre de atributo ya existe en esa entidad-> ERROR
					for (int i=0; i<listaSubatributos.size();i++)
						if(daoAtributos.nombreDeAtributo((Integer.parseInt((String)listaSubatributos.get(i)))).toLowerCase().equals(nuevoNombre.toLowerCase())
								&& i!=k){ 
							controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_ERROR_NombreDeAtributoYaExiste,v);
							return;
						}
				}
				k++;
			}
			j++;
		}
		// Modificamos el atributo
		ta.setNombre(nuevoNombre);
		if (daoAtributos.modificarAtributo(ta) == false){
			ta.setNombre(antiguoNombre);
			controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_ERROR_DAOAtributos, v);
		}
		else{
			v.add(antiguoNombre);
			controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_HECHO, v);
		}	
		return;		
	}
	
	/*
	 * Editar dominio de atributo
	 * -> Recibe v con el atributo, el nuevo dominio y si tiene tamano el tamano
	 */

	public void editarDomnioAtributo(Vector<Object> v){
		TransferAtributo ta = (TransferAtributo) v.get(0);
		String nuevoDominio = (String) v.get(1);
		// Si tiene tamano comprobamos que es correcto
		if(v.size()==3){
			try{
				int tamano = Integer.parseInt((String) v.get(2));
				if(tamano<1){
					this.controlador.mensajeDesde_SA(TC.SA_EditarDominioAtributo_ERROR_TamanoEsNegativo, ta); return;
				}			
			}
			catch(Exception e){
				this.controlador.mensajeDesde_SA(TC.SA_EditarDominioAtributo_ERROR_TamanoNoEsEntero, ta); return;
			}
		}
		// Modificamos el atributo
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		ta.setDominio(nuevoDominio);
		if (daoAtributos.modificarAtributo(ta) == false)
			controlador.mensajeDesde_SA(TC.SA_EditarDominioAtributo_ERROR_DAOAtributos, ta);
		else
			controlador.mensajeDesde_SA(TC.SA_EditarDominioAtributo_HECHO, ta);
		return;
	}


	/*
	 * Editar compuesto de un atributo
	 * -> Hay que voltear el valor de compuesto
	 */

	public void editarCompuestoAtributo(TransferAtributo ta){
		// Modificamos el atributo
		ta.setCompuesto(!ta.getCompuesto());
		// Ponemos su dominio a null si es compuesto
		if (ta.getCompuesto()) ta.setDominio("null");
		// Persistimos
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		if (daoAtributos.modificarAtributo(ta) == false)
			controlador.mensajeDesde_SA(TC.SA_EditarCompuestoAtributo_ERROR_DAOAtributos, ta);
		else
			controlador.mensajeDesde_SA(TC.SA_EditarCompuestoAtributo_HECHO, ta);
		return;
	}


	/*
	 * Editar multivalorado de un atributo
	 * -> Hay que voltear el valor de multuvalorado
	 */

	public void editarMultivaloradoAtributo(TransferAtributo ta){
		// Modificamos el atributo
		ta.setMultivalorado(!ta.isMultivalorado());
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		if (daoAtributos.modificarAtributo(ta) == false)
			controlador.mensajeDesde_SA(TC.SA_EditarMultivaloradoAtributo_ERROR_DAOAtributos, ta);
		else
			controlador.mensajeDesde_SA(TC.SA_EditarMultivaloradoAtributo_HECHO, ta);
		return;
	}

	public void editarNotNullAtributo(TransferAtributo ta){
		// Modificamos el atributo
		ta.setNotnull(!ta.getNotnull());
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		if (daoAtributos.modificarAtributo(ta) == false)
			controlador.mensajeDesde_SA(TC.SA_EditarNotNullAtributo_ERROR_DAOAtributos, ta);
		else
			controlador.mensajeDesde_SA(TC.SA_EditarNotNullAtributo_HECHO, ta);
		return;
	}
	
	public void editarUniqueAtributo(TransferAtributo ta){
		// Modificamos el atributo
		ta.setUnique(!ta.getUnique());
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		if (daoAtributos.modificarAtributo(ta) == false)
			controlador.mensajeDesde_SA(TC.SA_EditarUniqueAtributo_ERROR_DAOAtributos, ta);
		else
			controlador.mensajeDesde_SA(TC.SA_EditarUniqueAtributo_HECHO, ta);
		return;
	}
	
	public void anadirRestriccion(Vector v){
		TransferAtributo ta = (TransferAtributo) v.get(0);
		String restriccion = (String) v.get(1);
		// Si nombre es vacio -> ERROR
		if (restriccion.isEmpty()) return;
		
		DAOAtributos daoAtributoes = new DAOAtributos(this.controlador);
		Vector<TransferAtributo> lista = daoAtributoes.ListaDeAtributos();
		if (lista == null){
			controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_ERROR_DAOAtributos,v);
			return;
		}
		
		Vector<String> vRestricciones = ta.getListaRestricciones();
		vRestricciones.add(restriccion);
		ta.setListaRestricciones(vRestricciones);
		
		if (daoAtributoes.modificarAtributo(ta) != false)
			controlador.mensajeDesde_SA(TC.SA_AnadirRestriccionAAtributo_HECHO, v);
		
		return;
	}
	
	public void quitarRestriccion(Vector v){
		TransferAtributo te = (TransferAtributo) v.get(0);
		String restriccion = (String) v.get(1);
		
		// Si nombre es vacio -> ERROR
		if (restriccion.isEmpty()) return;
		
		DAOAtributos daoAtributoes = new DAOAtributos(this.controlador);
		Vector<TransferAtributo> lista = daoAtributoes.ListaDeAtributos();
		if (lista == null){
			controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_ERROR_DAOAtributos,v);
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
		
		if (daoAtributoes.modificarAtributo(te) != false)
			controlador.mensajeDesde_SA(TC.SA_QuitarRestriccionAAtributo_HECHO, v);			
		return;
	}
	
	public void setRestricciones(Vector v) {
		Vector restricciones = (Vector) v.get(0);
		TransferAtributo ta = (TransferAtributo) v.get(1);
		
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		Vector<TransferAtributo> lista = daoAtributos.ListaDeAtributos();
		if (lista == null){
			controlador.mensajeDesde_SA(TC.SA_RenombrarAtributo_ERROR_DAOAtributos,v);
			return;
		}
		ta.setListaRestricciones(restricciones);
		if (daoAtributos.modificarAtributo(ta) != false)
			controlador.mensajeDesde_SA(TC.SA_setRestriccionesAAtributo_HECHO, v);
		
		return;		
	}
	
	/*
	 * Mover un atributo (cambiar su posicion)
	 */
	public void moverPosicionAtributo(TransferAtributo ta){
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		if(daoAtributos.modificarAtributo(ta) == false)
			controlador.mensajeDesde_SA(TC.SA_MoverPosicionAtributo_ERROR_DAOAtributos, ta);
		else
			controlador.mensajeDesde_SA(TC.SA_MoverPosicionAtributo_HECHO, ta);
	}

	/**
	 * Metodo que pone/quita el atributo ta como clave primaria de la entidad a la que pertence
	 * En el vector viene el atributo (pos 0) y la entidad (pos 1)
	 * Hay que negar el valor de esClavePrimaria del atributo
	 */
	public void editarClavePrimariaAtributo(Vector<Transfer> vectorDeTransfer){
		TransferAtributo ta = (TransferAtributo) vectorDeTransfer.get(0);
		TransferEntidad te = (TransferEntidad) vectorDeTransfer.get(1);
		// Si era clave primaria
		if(ta.isClavePrimaria()){
			te.getListaClavesPrimarias().remove(String.valueOf(ta.getIdAtributo()));
			ta.setClavePrimaria(false);
			
		}
		// Si no era clave primaria
		else{
			te.getListaClavesPrimarias().add(String.valueOf(ta.getIdAtributo()));
			ta.setClavePrimaria(true);
			ta.setNotnull(false);
			ta.setMultivalorado(false);
			ta.setUnique(false);
		}
		// Persistimos la entidad y devolvemos el mensaje
		DAOEntidades daoEntidades = new DAOEntidades(this.controlador.getPath());
		if (daoEntidades.modificarEntidad(te) == false)
			controlador.mensajeDesde_SA(TC.SA_EditarClavePrimariaAtributo_ERROR_DAOEntidades,vectorDeTransfer);
		else controlador.mensajeDesde_SA(TC.SA_EditarClavePrimariaAtributo_HECHO,vectorDeTransfer);
	}
	public String getNombreAtributo(int id){
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		return daoAtributos.nombreDeAtributo(id); 
	}
	public boolean idUnique(int id){
		DAOAtributos daoAtributos = new DAOAtributos(this.controlador);
		return daoAtributos.uniqueAtributo(id); 
	}
	public Controlador getControlador() {
		return controlador;
	}
	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}
}
