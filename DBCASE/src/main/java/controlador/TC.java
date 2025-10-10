package controlador;

public enum TC {

	//---------------------------------------------------------------------------------
	// Mensajes desde la GUIPrincipal al controlador
	//---------------------------------------------------------------------------------
	// Obtención de DBMS
	GUIPrincipal_ObtenDBMSDisponibles,
	
	// Actualizaciones de listas
	GUIPrincipal_ActualizameLaListaDeEntidades,
	GUIPrincipal_ActualizameLaListaDeAtributos,
	GUIPrincipal_ActualizameLaListaDeRelaciones,
	GUIPrincipal_ActualizameLaListaDeDominios,
	//about
	GUI_Principal_ABOUT,
	// Click en la barra de menus y submenus
	GUI_Principal_Click_Submenu_Salir,
	GUI_Principal_Click_Imprimir,
	GUI_Principal_Click_ModoProgramador,
	GUI_Principal_Click_ModoDiseno,
	GUI_Principal_Click_ModoVerTodo,
	GUI_Principal_Click_Salir,
	//GUI_Principal_Click_Submenu_SeleccionarWorkSpace,
	GUI_Principal_Click_Submenu_Abrir,
	GUI_Principal_Click_Submenu_Guardar,
	GUI_Principal_Click_Submenu_Nuevo,
	GUI_Principal_Click_Submenu_GuardarComo,
	GUI_Principal_CambiarLenguaje,
	GUI_Principal_CambiarTema,
	// Generacion de codigo
	GUI_Principal_Click_BotonLimpiarPantalla,
	GUI_Principal_Click_BotonValidar,
	GUI_Principal_Click_BotonGenerarScriptSQL,
	GUI_Principal_Click_BotonGenerarArchivoScriptSQL,
	GUI_Principal_Click_BotonGenerarArchivoModelo,
	GUI_Principal_Click_BotonGenerarModeloRelacional,
	GUI_Principal_Click_BotonEjecutarEnDBMS,
	
	//---------------------------------------------------------------------------------
	// Mensaje desde la GUI_WorkSpace al Controlador
	//---------------------------------------------------------------------------------
	GUI_WorkSpace_PrimeraSeleccion,
	GUI_WorkSpace_Nuevo,
	GUI_WorkSpace_Click_Abrir,
	GUI_WorkSpace_Click_Guardar,
	GUI_WorkSpace_ClickBotonCancelar,
	GUI_WorkSpace_ERROR_CreacionFicherosXML,
	
	
	//---------------------------------------------------------------------------------
	// Mensajes desde las GUIs al controlador
	//---------------------------------------------------------------------------------
	
	// Entidades
	GUIRenombrarEntidad_Click_BotonRenombrar,
	GUIInsertarEntidad_Click_BotonInsertar,
	GUIInsertarEntidadDebil_Click_BotonInsertar,
	GUIInsertarEntidadDebil_Entidad_Relacion_Repetidos,
	GUIAnadirAtributoEntidad_Click_BotonAnadir,
	GUIAnadirAtributoEntidad_ActualizameLaListaDeDominios,
	GUIRenombrarAtributo_Click_BotonRenombrar,
	GUIEditarDominioAtributo_Click_BotonEditar,
	GUIEditarDominioAtributo_ActualizameLaListaDeDominios,
	GUIEditarCompuestoAtributo_Click_BotonAceptar,
	GUIEditarMultivaloradoAtributo_Click_BotonAceptar,
	GUIAnadirSubAtributoAtributo_Click_BotonAnadir,
	GUIEditarClavePrimariaAtributo_ActualizameListaEntidades,
	GUIEditarClavePrimariaAtributo_ActualizameListaAtributos,
	GUIEditarClavePrimariaAtributo_Click_BotonAceptar,
	GUIInsertarRelacion_Click_BotonInsertar,
	GUIInsertarRelacionIsA_Click_BotonInsertar,
	GUIInsertarRelacionDebil_Click_BotonInsertar,
	GUIRenombrarRelacion_Click_BotonRenombrar,
	GUIPonerRestriccionesAEntidad_Click_BotonAceptar,
	GUIPonerRestriccionesARelacion_Click_BotonAceptar,
	GUIPonerRestriccionesAAtributo_Click_BotonAceptar,
	GUIInsertarRestriccionAEntidad_Click_BotonAnadir,
	GUIQuitarRestriccionAEntidad_Click_BotonAnadir,
	GUIInsertarRestriccionAAtributo_Click_BotonAnadir,
	GUIQuitarRestriccionAAtributo_Click_BotonAnadir,
	GUIPonerUniquesAEntidad_Click_BotonAceptar,
	GUIPonerUniquesARelacion_Click_BotonAceptar,
	// Relaciones IsA
	GUIEstablecerEntidadPadre_ActualizameListaEntidades,
	GUIEstablecerEntidadPadre_ClickBotonAceptar,
	GUIQuitarEntidadPadre_ClickBotonSi,
	GUIAnadirEntidadHija_ActualizameListaEntidades,
	GUIAnadirEntidadHija_ClickBotonAnadir,
	GUIQuitarEntidadHija_ActualizameListaEntidades,
	GUIQuitarEntidadHija_ClickBotonQuitar,
	// Relaciones Normales
	GUIAnadirEntidadARelacion_ActualizameListaEntidades,
	GUIAnadirEntidadARelacion_ClickBotonAnadir,
	GUIQuitarEntidadARelacion_ActualizameListaEntidades,
	GUIQuitarEntidadARelacion_ClickBotonQuitar,
	GUIEditarCardinalidadEntidad_ActualizameListaEntidades,
	GUIEditarCardinalidadEntidad_ClickBotonEditar,
	GUIAnadirAtributoRelacion_Click_BotonAnadir,
	GUIAnadirAtributoRelacion_ActualizameLaListaDeDominios,
	GUIInsertarRestriccionARelacion_Click_BotonAnadir,
	GUIQuitarRestriccionARelacion_Click_BotonAnadir,
	// Dominios
	GUIInsertarDominio_Click_BotonInsertar,
	GUIRenombrarDominio_Click_BotonRenombrar,
	GUIModificarValoresDominio_Click_BotonEditar,
	GUIModificarDominio_Click_BotonEditar,
	
	GUIAnadirSubAtributoAtributo_ActualizameLaListaDeDominios,
	
	// Conexión a DBMS
	GUIConfigurarConexionDBMS_Click_BotonEjecutar,
	GUIConexionDBMS_PruebaConexion,	
	
	
	//---------------------------------------------------------------------------------
	// Mensajes desde el Panel de Diseño
	//---------------------------------------------------------------------------------
	// Fuera
	PanelDiseno_Click_InsertarEntidad,
	PanelDiseno_Click_InsertarAtributo,
	PanelDiseno_Click_InsertarRelacionNormal,
	PanelDiseno_Click_InsertarRelacionIsA,
	PanelDiseno_Click_CrearDominio,
	// Entidades
	PanelDiseno_Click_RenombrarEntidad,
	PanelDiseno_Click_DebilitarEntidad,
	PanelDiseno_Click_EliminarEntidad,
	PanelDiseno_Click_AnadirRestriccionAEntidad,
	PanelDiseno_Click_TablaUniqueAEntidad,
	PanelDiseno_Click_AnadirRestriccionAAtributo,
	PanelDiseno_Click_AnadirAtributoEntidad,
	PanelDiseno_Click_EliminarAtributo,
	PanelDiseno_Click_RenombrarAtributo,
	PanelDiseno_Click_EditarDominioAtributo,
	PanelDiseno_Click_EditarCompuestoAtributo,
	PanelDiseno_Click_EditarMultivaloradoAtributo,
	PanelDiseno_Click_EditarNotNullAtributo,
	PanelDiseno_Click_EditarUniqueAtributo,
	PanelDiseno_Click_ModificarUniqueAtributo,
	PanelDiseno_Click_EliminarReferenciasUniqueAtributo,
	PanelDiseno_Click_AnadirAtributoRelacion,
	PanelDiseno_Click_AnadirSubAtributoAAtributo,
	PanelDiseno_Click_EditarClavePrimariaAtributo,
	PanelDiseno_MoverEntidad,
	PanelDiseno_MoverAtributo,
	PanelDiseno_MoverRelacion,

	// Relaciones IsA
	PanelDiseno_Click_EstablecerEntidadPadre,
	PanelDiseno_Click_QuitarEntidadPadre,
	PanelDiseno_Click_AnadirEntidadHija,
	PanelDiseno_Click_QuitarEntidadHija,
	PanelDiseno_Click_EliminarRelacionIsA,
	// Relaciones Normales
	PanelDiseno_Click_RenombrarRelacion,
	PanelDiseno_Click_DebilitarRelacion,
	PanelDiseno_Click_EliminarRelacionNormal,
	PanelDiseno_Click_AnadirEntidadARelacion,
	PanelDiseno_Click_QuitarEntidadARelacion,
	PanelDiseno_Click_EditarCardinalidadEntidad,
	PanelDiseno_Click_AnadirRestriccionARelacion,
	PanelDiseno_Click_TablaUniqueARelacion,
	// Dominios
	PanelDiseno_Click_RenombrarDominio,
	PanelDiseno_Click_EliminarDominio,
	// Mensajes de pulsación referentes al panel de información
	PanelDiseno_LimpiarPanelInformacion,
	PanelDiseno_MostrarDatosEnPanelDeInformacion,
	// Mensajes desde AccionMenu
	PanelDiseno_Click_ModificarDominio,
	PanelDiseno_Click_OrdenarValoresDominio,

	//---------------------------------------------------------------------------------
	// Mensajes desde los Servicios de Entidades
	//---------------------------------------------------------------------------------
	// Insercion de entidades
	SE_InsertarEntidad_HECHO, 
	SE_InsertarEntidad_ERROR_NombreDeEntidadEsVacio,
	SE_InsertarEntidad_ERROR_NombreDeEntidadYaExiste,
	SE_InsertarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion,
	SE_InsertarEntidad_ERROR_DAO,
	SE_ComprobarInsertarEntidad_HECHO, 
	SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadEsVacio,
	SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadYaExiste,
	SE_ComprobarInsertarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion,
	SE_ComprobarInsertarEntidad_ERROR_DAO,
	// Renombrar entidades
	SE_RenombrarEntidad_HECHO,
	SE_RenombrarEntidad_ERROR_DAOEntidades,
	SE_RenombrarEntidad_ERROR_DAORelaciones,
	SE_RenombrarEntidad_ERROR_NombreDeEntidadEsVacio,
	SE_RenombrarEntidad_ERROR_NombreDeEntidadYaExiste,
	SE_RenombrarEntidad_ERROR_NombreDeEntidadYaExisteComoRelacion,
	// Modificar el caracter debil
	SE_DebilitarEntidad_HECHO,
	SE_DebilitarEntidad_ERROR_DAOEntidades,
	// Eliminacion de entidades
	SE_EliminarEntidad_HECHO,
	SE_EliminarEntidad_ERROR_DAORelaciones,
	SE_EliminarEntidad_ERROR_DAOEntidades,
	// Consultar entidad
	SE_ConsultarEntidad_HECHO,
	SE_ConsultarEntidad_ERROR_DAOAtributos,
	SE_ConsultarEntidad_ERROR_DAORelaciones,
	SE_ConsultarEntidad_ERROR_DAOEntidades,
	//Restricciones
	SE_AnadirRestriccionAEntidad_HECHO,
	SE_QuitarRestriccionAEntidad_HECHO,
	SE_setRestriccionesAEntidad_HECHO,
	//Uniques
	SE_AnadirUniqueAEntidad_HECHO,
	SE_QuitarUniqueAEntidad_HECHO,
	SE_setUniquesAEntidad_HECHO,
	SE_setUniqueUnitarioAEntidad_HECHO,
	// Listar entidades
	SE_ListarEntidades_HECHO,
	// Mover posicion Entidad en el panel de diseño
	SE_MoverPosicionEntidad_ERROR_DAOEntidades,
	SE_MoverPosicionEntidad_HECHO,
	// Anadir un atributo a una relacion
	SE_AnadirAtributoAEntidad_ERROR_NombreDeAtributoVacio,
	SE_AnadirAtributoAEntidad_ERROR_NombreDeAtributoYaExiste,
	SE_AnadirAtributoAEntidad_ERROR_TamanoEsNegativo,
	SE_AnadirAtributoAEntidad_ERROR_TamanoNoEsEntero,
	SE_AnadirAtributoAEntidad_ERROR_DAOAtributos,
	SE_AnadirAtributoAEntidad_ERROR_DAOEntidades,
	SE_AnadirAtributoAEntidad_HECHO,

	//---------------------------------------------------------------------------------
	// Mensajes desde los Servicios de Atributos
	//---------------------------------------------------------------------------------
	// Añadir atributo a entidad
	SA_AnadirAtributo_HECHO,
	SA_AnadirAtributo_ERROR_NombreDeAtributoEsVacio,
	SA_AnadirAtributo_ERROR_DAOAtributos,
	// Listar atributos
	SA_ListarAtributos_HECHO,
	// Eliminar atributo
	SA_EliminarAtributo_ERROR_DAOAtributos,
	SA_EliminarAtributo_HECHO,
	// Renombrar atributo
	SA_RenombrarAtributo_ERROR_NombreDeAtributoEsVacio,
	SA_RenombrarAtributo_ERROR_NombreDeAtributoYaExiste,
	SA_RenombrarAtributo_ERROR_DAOAtributos,
	SA_RenombrarAtributo_HECHO,
	// Editar domnio de atributo
	SA_EditarDominioAtributo_ERROR_DAOAtributos,
	SA_EditarDominioAtributo_ERROR_TamanoNoEsEntero,
	SA_EditarDominioAtributo_ERROR_TamanoEsNegativo,
	SA_EditarDominioAtributo_HECHO,
	// Editar el caracter de compuesto del atributo
	SA_EditarCompuestoAtributo_ERROR_DAOAtributos,
	SA_EditarCompuestoAtributo_HECHO,
	// Editar el caracter de multivalorado del atributo
	SA_EditarMultivaloradoAtributo_ERROR_DAOAtributos,
	SA_EditarMultivaloradoAtributo_HECHO,
	// Editar el caracter de not null del atributo
	SA_EditarNotNullAtributo_ERROR_DAOAtributos,
	SA_EditarNotNullAtributo_HECHO,
	// Editar el caracter de unique del atributo
	SA_EditarUniqueAtributo_ERROR_DAOAtributos,
	SA_EditarUniqueAtributo_HECHO,
	// Mover posicion Atributo en el panel de diseño
	SA_MoverPosicionAtributo_ERROR_DAOAtributos,
	SA_MoverPosicionAtributo_HECHO,
	// Editar atributo como clave primaria de una entidad
	SA_EditarClavePrimariaAtributo_ERROR_DAOEntidades,
	SA_EditarClavePrimariaAtributo_HECHO,
	// Añadir subatributo a un atributo
	SA_AnadirSubAtributoAtributo_ERROR_NombreDeAtributoVacio,
	SA_AnadirSubAtributoAtributo_ERROR_NombreDeAtributoYaExiste,
	SA_AnadirSubAtributoAtributo_ERROR_TamanoEsNegativo,
	SA_AnadirSubAtributoAtributo_ERROR_TamanoNoEsEntero,
	SA_AnadirSubAtributoAtributo_ERROR_DAOAtributosHijo,
	SA_AnadirSubAtributoAtributo_ERROR_DAOAtributosPadre,
	SA_AnadirSubAtributoAtributo_HECHO,
	//Restricciones
	SA_AnadirRestriccionAAtributo_HECHO,
	SA_QuitarRestriccionAAtributo_HECHO,
	SA_setRestriccionesAAtributo_HECHO,
	
	//---------------------------------------------------------------------------------
	// Mensajes desde los Servicios de Relaciones
	//---------------------------------------------------------------------------------
	// Listar relaciones
	SR_ListarRelaciones_HECHO,
	// Insertar relacion
	SR_InsertarRelacion_ERROR_NombreDeRelacionEsVacio,
	SR_InsertarRelacion_ERROR_NombreDeRelacionYaExiste,
	SR_InsertarRelacion_ERROR_NombreDeRelacionYaExisteComoEntidad,
	SR_InsertarRelacion_ERROR_NombreDelRolYaExiste,
	SR_InsertarRelacion_ERROR_NombreDeRolNecesario,
	SR_InsertarRelacion_ERROR_DAORelaciones,
	SR_InsertarRelacion_HECHO,
	// Eliminar relacion
	SR_EliminarRelacion_ERROR_DAORelaciones,
	SR_EliminarRelacion_HECHO,
	// Renombrar relacion
	SR_RenombrarRelacion_ERROR_NombreDeRelacionEsVacio,
	SR_RenombrarRelacion_ERROR_NombreDeRelacionYaExiste,
	SR_RenombrarRelacion_ERROR_NombreDeRelacionYaExisteComoEntidad,
	SR_RenombrarRelacion_ERROR_NombreIsA,
	SR_RenombrarRelacion_ERROR_DAORelaciones,
	SR_RenombrarRelacion_ERROR_DAOEntidades,
	SR_RenombrarRelacion_HECHO,
	// Mover posicion Relacion en el panel de diseño
	SR_MoverPosicionRelacion_ERROR_DAORelaciones,
	SR_MoverPosicionRelacion_HECHO,
	/*
	 * Relaciones IsA
	 */
	// Establecer Entidad padre en una relacion IsA
	SR_EstablecerEntidadPadre_ERROR_DAORelaciones,
	SR_EstablecerEntidadPadre_HECHO,
	// Quitar la entidad padre en una relacion IsA
	SR_QuitarEntidadPadre_ERROR_DAORelaciones,
	SR_QuitarEntidadPadre_HECHO,
	// Anadir entidad hija a una relacion isA
	SR_AnadirEntidadHija_ERROR_DAORelaciones,
	SR_AnadirEntidadHija_HECHO,
	// Quitar una entidad hija en una relacion IsA
	SR_QuitarEntidadHija_ERROR_DAORelaciones,
	SR_QuitarEntidadHija_HECHO,
	// Eliminar una relacion IsA
	SR_EliminarRelacionIsA_ERROR_DAORelaciones,
	SR_EliminarRelacionIsA_HECHO,
	// Insertar una relacion IsA
	SR_InsertarRelacionIsA_ERROR_DAORelaciones,
	SR_InsertarRelacionIsA_HECHO,
	/*
	 * Relaciones Normales
	 */
	// Eliminar una relacion Normal
	SR_EliminarRelacionNormal_ERROR_DAORelaciones,
	SR_EliminarRelacionNormal_HECHO,
	// Anadir una entidad a una relacion
	SR_AnadirEntidadARelacion_ERROR_InicioNoEsEnteroOn,
	SR_AnadirEntidadARelacion_ERROR_InicioEsNegativo,
	SR_AnadirEntidadARelacion_ERROR_FinalNoEsEnteroOn,
	SR_AnadirEntidadARelacion_ERROR_FinalEsNegativo,
	SR_AnadirEntidadARelacion_ERROR_InicioMayorQueFinal,
	SR_AnadirEntidadARelacion_ERROR_DAORelaciones,
	SR_AnadirEntidadARelacion_HECHO,
	// Quitar una entidad de una relacion
	SR_QuitarEntidadARelacion_ERROR_DAORelaciones,
	SR_QuitarEntidadARelacion_HECHO,
	// Debilitar una relacion
	SR_DebilitarRelacion_ERROR_DAORelaciones,
	SR_DebilitarRelacion_HECHO,
	// Editar aridad de una entidad en una relacion
	SR_EditarCardinalidadEntidad_ERROR_InicioNoEsEnteroOn,
	SR_EditarCardinalidadEntidad_ERROR_InicioEsNegativo,
	SR_EditarCardinalidadEntidad_ERROR_FinalNoEsEnteroOn,
	SR_EditarCardinalidadEntidad_ERROR_FinalEsNegativo,
	SR_EditarCardinalidadEntidad_ERROR_InicioMayorQueFinal,
	SR_EditarCardinalidadEntidad_ERROR_DAORelaciones,
	SR_EditarCardinalidadEntidad_HECHO,
	SR_AridadEntidadUnoUno_HECHO,
	// Anadir un atributo a una relacion
	SR_AnadirAtributoARelacion_ERROR_NombreDeAtributoVacio,
	SR_AnadirAtributoARelacion_ERROR_TamanoEsNegativo,
	SR_AnadirAtributoARelacion_ERROR_TamanoNoEsEntero,
	SR_AnadirAtributoARelacion_ERROR_DAOAtributos,
	SR_AnadirAtributoARelacion_ERROR_DAORelaciones,
	SR_AnadirAtributoARelacion_ERROR_NombreDeAtributoYaExiste,
	SR_AnadirAtributoARelacion_HECHO,

	// Restricciones
	SR_AnadirRestriccionARelacion_HECHO,
	SR_QuitarRestriccionARelacion_HECHO,
	SR_setRestriccionesARelacion_HECHO,
	//Uniques
	SR_AnadirUniqueARelacion_HECHO,
	SR_QuitarUniqueARelacion_HECHO,
	SR_setUniquesARelacion_HECHO,
	SR_setUniqueUnitarioARelacion_HECHO,
	
	//---------------------------------------------------------------------------------
	// Mensajes desde los Servicios de Dominios
	//---------------------------------------------------------------------------------
	// Insercion de dominios
	SD_InsertarDominio_HECHO, 
	SD_InsertarDominio_ERROR_NombreDeDominioEsVacio,
	SD_InsertarDominio_ERROR_NombreDeDominioYaExiste,
	SD_InsertarDominio_ERROR_ValorNoValido,
	SD_InsertarDominio_ERROR_DAO,
	// Renombrar dominios
	SD_RenombrarDominio_HECHO,
	SD_RenombrarDominio_ERROR_DAODominios,
	SD_RenombrarDominio_ERROR_NombreDeDominioEsVacio,
	SD_RenombrarDominio_ERROR_NombreDeDominioYaExiste,
	// Eliminacion de Dominio
	SD_EliminarDominio_HECHO,
	SD_EliminarDominio_ERROR_DAORelaciones,
	SD_EliminarDominio_ERROR_DAOEntidades,
	SD_EliminarDominio_ERROR_DAODominios,
	// Consultar dominio
	SD_ConsultarDominio_HECHO,
	SD_ConsultarDominio_ERROR_DAOAtributos,
	SD_ConsultarDominio_ERROR_DAORelaciones,
	SD_ConsultarDominio_ERROR_DAOEntidades,
	// Modificar dominio
	SD_ModificarTipoBaseDominio_ERROR_DAODominios,
	SD_ModificarTipoBaseDominio_ERROR_TipoBaseDominioEsVacio,
	SD_ModificarTipoBaseDominio_ERROR_ValorNoValido,
	SD_ModificarTipoBaseDominio_HECHO,
	// Modificar dominio
	SD_ModificarElementosDominio_ERROR_DAODominios,
	SD_ModificarElementosDominio_ERROR_ElementosDominioEsVacio,
	SD_ModificarElementosDominio_ERROR_ValorNoValido,
	SD_ModificarElementosDominio_HECHO,
	// Listar dominios
	SD_ListarDominios_HECHO,
	
	
	//---------------------------------------------------------------------------------
	// Mensajes desde los Servicios de Relaciones al Controlador
	//---------------------------------------------------------------------------------
	SS_ValidacionM,
	SS_ValidacionC,
	SS_GeneracionScriptSQL,
	SS_GeneracionArchivoScriptSQL,
	SS_GeneracionModeloRelacional,
	
	
	//---------------------------------------------------------------------------------
	// Mensajes desde el Controlador a la GUIPrincipal
	//---------------------------------------------------------------------------------
	Controlador_InsertarEntidad,
	Controlador_RenombrarEntidad,
	Controlador_DebilitarEntidad,
	Controlador_EliminarEntidad,
	Controlador_EliminarAtributo,
	Controlador_EliminarDominio,
	Controlador_AnadirAtributoAEntidad,
	Controlador_RenombrarAtributo,
	Controlador_InsertarRelacion,
	Controlador_EditarDominioAtributo,
	Controlador_EditarCompuestoAtributo,
	Controlador_EditarMultivaloradoAtributo,
	Controlador_EditarNotNullAtributo,
	Controlador_EditarUniqueAtributo,
	Controlador_MoverEntidad_ERROR,
	Controlador_MoverEntidad_HECHO,
	Controlador_MoverAtributo_ERROR,
	Controlador_MoverAtributo_HECHO,
	Controlador_MoverRelacion_ERROR,
	Controlador_MoverRelacion_HECHO,
	Controlador_MoverDominio_HECHO,
	Controlador_RenombrarRelacion,
	Controlador_AnadirAtributoARelacion,
	Controlador_EliminarRelacion,
	Controlador_AnadirSubAtributoAAtributo,
	Controlador_EditarClavePrimariaAtributo,
	Controlador_EstablecerEntidadPadre,
	Controlador_QuitarEntidadPadre,
	Controlador_AnadirEntidadHija,
	Controlador_QuitarEntidadHija,
	Controlador_EliminarRelacionIsA,
	Controlador_EliminarRelacionNormal,
	Controlador_InsertarRelacionIsA,
	Controlador_AnadirEntidadARelacion,
	Controlador_QuitarEntidadARelacion,
	Controlador_DebilitarRelacion,
	Controlador_EditarCardinalidadEntidad,
	Controlador_CardinalidadUnoUno,
	Controlador_MostrarDatosEnPanelDeInformacion,
	Controlador_LimpiarPanelDominio,
	Controlador_MostrarDatosEnPanelDominio,
	Controlador_InsertarDominio,
	Controlador_RenombrarDominio,
	Controlador_ModificarTipoBaseDominio,
	Controlador_ModificarValoresDominio,
	Controlador_AnadirRestriccionEntidad,
	Controlador_AnadirRestriccionRelacion,
	Controlador_AnadirRestriccionAtributo,
	Controlador_QuitarRestriccionEntidad,
	Controlador_QuitarRestriccionRelacion,
	Controlador_QuitarRestriccionAtributo,
	Controlador_setRestriccionesEntidad,
	Controlador_setRestriccionesAtributo,
	Controlador_setRestriccionesRelacion,
	Controlador_AnadirUniqueEntidad,
	Controlador_QuitarUniqueEntidad,
	Controlador_setUniquesEntidad,
	Controlador_setUniqueUnitarioEntidad,
	Controlador_AnadirUniqueRelacion,
	Controlador_QuitarUniqueRelacion,
	Controlador_setUniquesRelacion,
	Controlador_setUniqueUnitarioRelacion,
	PanelDiseno_MostrarDatosEnTablaDeVolumenes,
	Controlador_MostrarDatosEnTablaDeVolumenes,
	PanelDiseno_ActualizarDatosEnTablaDeVolumenes,
	Controlador_ActualizarDatosEnTablaDeVolumenes,
	GUI_Principal_NULLATTR,
	GUI_Principal_IniciaFrames,
}
