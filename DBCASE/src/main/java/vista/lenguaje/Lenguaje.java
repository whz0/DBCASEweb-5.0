package vista.lenguaje;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Clase encargada de la internacionalización de la aplicación. Parsea ficheros .lng para 
 * obtener los textos de la aplicación.
 * 
 * Los ficheros .lng son ficheros de texto plano con el siguiente formato:
 * 
 * Nombre del lenguaje
 * Mensaje1=Texto del mensaje1 (hasta el salto de línea)
 * Mensaje2=Texto del mensaje2, igual que el otro
 * ...
 * MensajeN=Texto del mensajeN
 * 
 * Las líneas que comienzan por # son ignoradas (comentarios).
 */
public class Lenguaje {
	// --- --- --- RUTAS DE LOS FICHEROS --- --- ---
	private static final String CARPETA = "/vista/lenguaje/";
	private static final String INDICE = "index.txt";
	private static final String DEFAULT = "default.lng";
	
	// --- --- --- CAMPOS DE TEXTO --- --- ---
	// Titulo de la aplicación
	public static final int DBCASE = 1;
	//public static final int DBDT= 2;
	
	//--- --- --- MENSAJES GENERALES --- --- ---
	public static final int SELECT = 11;
	public static final int YES = 12;
	public static final int NO = 13;
	public static final int INFO = 14;
	public static final int ERROR = 15;
	public static final int WISH_CONTINUE =16; //¿Desea continuar?
	public static final int CANCEL = 17;
	public static final int INSERT = 18;
	public static final int SUCCESS = 19;
	public static final int WARNING = 20;
	public static final int EXIT = 21;
	public static final int EXECUTE = 22;
	public static final int EDIT = 23;
	public static final int ENTITY =24; //"Entidad"
	public static final int RELATION =25; //"Relación"
	public static final int ATTRIBUTE =26; //"Atributo"
	public static final int ISA_RELATION =27; //"Relación IsA"
	public static final int ENTITIES =28; //"Entidades"
	public static final int CARDINALITY =29; //"Cardinalidad"
	public static final int ATTRIBUTES =30; //"Atributos"
	public static final int COMPOSED =31; //"Compuesto"
	public static final int DOMAIN =32; //"Dominio"
	public static final int PRIMARY_KEYS =33; //"Claves primarias"
	public static final int DELETE =34; //"Eliminar"
	public static final int REMOVE =35;
	public static final int RENAME = 36;
	public static final int NEW = 37;
	public static final int OPEN = 38;
	public static final int CLOSE = 39;
	public static final int SAVE = 40;
	public static final int SAVE_AS = 41;
	public static final int NAME = 42;
	public static final int TYPE = 43;
	public static final int VALUES = 44;
	public static final int ACCEPT = 45;
	public static final int CONNECT = 46;
	public static final int NEW2=47;
	
	//--- --- --- VENTANA DE SELECCION DEL WORKSPACE 10XX --- --- --- 
	public static final int SELECT_WORKSPACE = 1001;
	public static final int ERROR_CREATING_FILE = 1007;
	public static final int ERROR_TEMP_FILE = 1008; //No se ha podido crear el fichero temporal
	public static final int WRONG_FILE = 1009;//El fichero seleccionado no es de esta aplicación
	public static final int NOT_EXIST_FILE = 1010;//El fichero no existe

	//--- --- --- CONTROLADOR 11XX--- --- ---
	//public static final int WORKSPACE_CHANGED = 1101;//Se ha cambiado satisfactoriamente el directorio de trabajo.
	public static final int WORKSPACE_IS = 1102;//El directorio actual de trabajo es:
	public static final int INITIAL_ERROR = 1104;//Se ha producido un error en la creacion inicial
	public static final int OF_XMLFILES = 1105;//de los ficheros XML en el directorio de trabajo:
	//nombres ventanas
	public static final int DELETE_ENTITY = 1107;//DBDT: Eliminar entidad
	public static final int DELETE_ATTRIBUTE =1108;//DBDT: Eliminar atributo
	public static final int DELETE_DOMAIN = 1109;//DBDT: Eliminar dominio
	public static final int ADD_ENTITY_RELATION = 1110;//DBDT: Añadir una entidad a la relación
	public static final int DELETE_ISA_RELATION = 1111;//DBDT: Eliminar relación IsA
	public static final int DELETE_RELATION = 1157;//DBDT: Eliminar relación
	//advertencias
	public static final int DELETE_ATTRIBUTES_WARNING = 1112;//Además se eliminarán todos sus atributos.
	public static final int REMOVE_FROM_SYSTEM=1114;//se eliminará definitivamente del sistema.
	public static final int THE_ATTRIBUTE=1115;//El atributo
	public static final int WEAK_RELATION=1116;//Si debilita la relación 
	public static final int DELETE_ATTRIBUTES_WARNING2=1117;// se eliminarán todos sus atributos.
	public static final int MODIFYING_CARDINALITY=1118;//, la cardinalidad de su entidad débil será modificada a 1..1.\n
	public static final int MODIFY_ATTRIBUTE=1119;//Si cambia el atributo compuesto 
	public static final int DELETE_ATTRIBUTES_WARNING3=1120;// a simple se eliminarán todos sus subatributos.
	public static final int ISA_RELATION_DELETE=1121;//La relación IsA seleccionada será eliminada del sistema.
	public static final int THE_DOMAIN=1122;//El dominio 
	public static final int MODIFYING_ATTRIBUTES_WARNING4=1123;//Todos los atributos con dicho dominio serán modificados.
	//errores
	public static final int RELATION_WEAK_ENTITIES=1124;//La relación tiene más de una entidad débil.
	public static final int NO_ATTRIBUTES_RELATION=1125;//No se puede añadir ningún atributo a la relación.
	public static final int ALREADY_WEAK_ENTITY=1126;//Ya existe una entidad débil en esta relación.
	public static final int IS_WEAK=1128;// es una relación débil.
	public static final int INCORRECT_VALUE=1129;//Algún valor no es correcto.	
	//error de acceso
	public static final int ENTITIES_FILE_ERROR=1130;//Se ha producido un error en el acceso al fichero de entidades. 
	public static final int RELATIONS_FILE_ERROR=1131;//Se ha producido un error en el acceso al fichero de relaciones.
	public static final int ATTRIBUTES_FILE_ERROR=1132;//Se ha producido un error en el acceso al fichero de atributos.
	public static final int DOMAINS_FILE_ERROR=1133;//Se ha producido un error en el acceso al fichero de dominios.
	public static final int EMPTY_ATTRIB_NAME=1134;//El nombre del atriburo es vacío. 
	public static final int EMPTY_DOM_NAME=1135;//El nombre del dominio es vacío.
	public static final int EMPTY_ENT_NAME=1136;//El nombre de la entidad es vacío.
	public static final int EMPTY_REL_NAME=1137;//El nombre de la relación es vacío.
	public static final int EMPTY_SUBATTR_NAME=1138;//El nombre del subatributo es vacío.
	public static final int EMPTY_TYPE_NAME=1139;//El nuevo tipo es vacío.
	public static final int EMPTY_VALUES=1140;//El campo 'valores' es vacío.
	public static final int REPEATED_ENT_NAME=1141;//Existe una entidad en el sistema con el mismo nombre.
	public static final int REPEATED_ATTRIB_NAME=1142;//Ya existe un atributo con ese nombre en la entidad.
	public static final int REPEATED_ATTRIB_NAME_REL=1143;//Ya existe un atributo en la relación con ese nombre.		
	public static final int REPEATED_REL_NAME=1144;//Existe una relación en el sistema con el mismo nombre.
	public static final int REPEATED_ROL_NAME=1145;//Existe otro rol en la relación con el mismo nombre.
	public static final int REPEATED_SUBATR_NAME=1146;//Existe otro subatributo con ese nombre.
	public static final int REPEATED_DOM_NAME=1147;//Existe otro dominio en el sistema con el mismo nombre.
			
	public static final int INCORRECT_SIZE1=1148;//El tamano del dominio del atributo no es un valor entero positivo.
	public static final int INCORRECT_SIZE2=1149;//El tamano del dominio del atributo es nulo o negativo.
	public static final int INCORRECT_SIZE3=1150;//El tamaño del atributo no es un valor entero positivo. 
	public static final int INCORRECT_CARDINALITY1=1151;//El valor de inicio no es un número entero positivo o 'n'.
	public static final int INCORRECT_CARDINALITY2=1152;//El valor de inicio es un número negativo.
	public static final int INCORRECT_CARDINALITY3=1153;//El valor de fin no es un número entero positivo o 'n'.
	public static final int INCORRECT_CARDINALITY4=1154;//El valor de fin es un número negativo.
	public static final int INCORRECT_CARDINALITY5=1155;//El valor de inicio es mayor que el de fin.
	public static final int NECESARY_ROL=1156;//Debe asignar un rol a la entidad.
	
	//public static final int SUCCESS_SAVE = 1158;//Proyecto guardado con éxito
	public static final int WISH_SAVE = 1159;//¿Desea guardar el proyecto actual?
	
	public static final int IF_WEAK_ENTITY = 1160;//Si debilita la entidad		
	//Advertencias
	public static final int WARNING_DELETE_WEAK_ENTITY=1161;//Se eliminará también la entidad débil asociada
	public static final int WARNING_DELETE_WEAK_RELATION=1162;//Se eliminará también la relación débil asociada
	public static final int REPEATED_ENTITY_REL=1163;
	
//--- --- --- MENSAJES DE LA GUI --- --- --- 
	//Ventana GUI_InsertaEntidad 12XX
	public static final int INSERT_ENTITY = 1200;
	public static final int WRITE_ENTITY_NAME = 1201;
	public static final int WEAK_ENTITY = 1202;
	public static final int SELECT_STRONG_ENTITY= 1204;
	public static final int WRITE_RELATION_WEAK= 1205;
	public static final int CREATE_STRONG_ENTITY = 1206;
	
	//Ventana GUI_InsertarRelacion 13XX
	public static final int INSERT_RELATION = 1300;
	public static final int WRITE_RELATION_NAME = 1301;
	
	//Ventana de GUI_AnadirAtributoEntidad  y GUI_AnadirAtributoRelacion 14xx
	public static final int INSERT_ATTRIBUTE = 1400;
	public static final int COMPOSITE_ATTRIBUTE = 1403;
	public static final int PRIMARY_KEY_ATTRIBUTE = 1404;
	public static final int NOT_NULL_ATTRIBUTE = 1405;
	public static final int UNIQUE_ATTRIBUTE = 1406;
	public static final int VALUE_ATTRIBUTE = 1407;
	public static final int DOMAIN_ATTRIBUTE = 1408;
	public static final int SIZE_ATTRIBUTE = 1409;
	
	//Ventana de GUI_AnadirSubAtributoAtributo 15xx
	public static final int INSERT_NEW_SUBATTRIBUTE = 1500;
	
	//Ventana de GUI_AnadirEntidadARelacion 17xx
	public static final int INSERT_NEW_ENTITY_TO_RELATION = 1700;
	public static final int IMPOSIBLE_TO_INSERT_ENTITY = 1701;
	public static final int NO_ENTITY = 1702;
	public static final int SELECT_ENTITY = 1703;
	public static final int WRITE_NUMBERS_RELATION = 1704;
	public static final int THE = 1705;
	public static final int TO = 1706;
	public static final int WRITE_ROLL = 1707;
	public static final int LABEL1A1=1708;
	public static final int LABEL1AN=1709;
	public static final int LABELNAN=1710;
	
	//Ventana de GUI_AnadirEntidadHija 18xx
	public static final int INSERT_NEW_DAUGTHER = 1800;
	public static final int IMPOSIBLE_TO_INSERT_DAUGHTER = 1801;
	public static final int NO_FATHER = 1802;
	public static final int SELECT_ENTITY_DAUTHTER = 1803;
	public static final int NO_ENTITIES_AVAILABLES =1804;
	
	//Ventana de GUI_ConfigurarConexionDBMS 19xx
	public static final int SHAPE_DBMS = 1900;
	public static final int EXPLANATION_DBMS1 = 1901;
	public static final int EXPLANATION_DBMS2 = 1902;
	public static final int EXPLANATION_DBMS3 = 1903;
	public static final int STRING_CONNECTION = 1904;
	public static final int USER = 1905;
	public static final int PASSWORD = 1906;
	public static final int INFORMATION_INCOMPLETE = 1907;
	public static final int DBDT = 1908;
	
	//Ventana de GUI_EditarCardinalidadEntidad 20xx
	public static final int EDIT_ARITY_AND_ROLLE = 2000;
	public static final int IMPOSIBLE_EDIT_ROLLE = 2001;
	public static final int NO_ENTITIES_IN_RELATION = 2002;
	public static final int EDIT_ARITY = 2003;
	public static final int SELECT_ENTITY_TO_CHANGE = 2004;
	public static final int WRITE_NEW_ARITY = 2005;
	public static final int WRITE_NEW_ROLLE = 2006;
	public static final int IF_ENTITY_HAS_ROLLE = 2007;
	
	//Ventana de GUI_EditarDominioAtributo 21xx
	public static final int EDIT_DOMAIN_ATTRIBUTE = 2100;
	public static final int IMPOSIBLE_EDIT_DOMAIN = 2101;
	public static final int COMPLEX_ATTRIBUTE = 2102;
	public static final int SELECT_DOMAIN_FOR_ATTRIBUTE = 2103;
	
	//Ventana de GUI_EstablecerEntidadPadre 23xx
	public static final int SET_PARENT_ENTITY = 2300;
	public static final int IMPOSIBLE_SET_PARENT = 2301;
	public static final int OTHER_PARENT = 2302;
	public static final int IMPOSIBLE_SET_PARENT_IN_ISA = 2303;
	public static final int SELECT_PARENT_ENTITY = 2304;
	
	//Ventana de GUI_InsertarDominio 24xx
	public static final int INSERT_NEW_DOMAIN = 2400;
	//public static final int WRITE_NAME_DOMAIN = 2401;
	//public static final int CHOOSE_VALUE_AND_TYPE = 2402;
	
	//Ventana de GUI_ModificarDominio 25xx
	public static final int EDIT_DOMAIN_GUI = 2500;
	//public static final int CHANGE_DOMAIN = 2501;
	//public static final int REMEMBER_DOMAIN = 2502;
	
	//Ventana de GUI_QuitarEntidadRelacion 26xx
	public static final int QUIT_ENTITY = 2600;
	public static final int IMPOSIBLE_QUIT_ENTITY = 2601;
	public static final int SELECT_ENTITY_TO_QUIT = 2602;
	
	//Ventana GUI_QuitarEntidadHija 27xx
	public static final int QUIT_DAUGHTER_ENTITY = 2700;
	public static final int IMPOSIBLE_QUIT_DAUGHTER_ENTITY = 2701;
	public static final int NO_DAUGHTER_ENTITY = 2702;
	public static final int SELECT_DAUGHTER_TO_QUIT = 2703;
	
	//Ventana GUI_QuitarEntidadPadre 28xx
	public static final int QUIT_PARENT_ENTITY = 2800;
	public static final int EXPLICATION_QUIT_PARENT = 2801;
	public static final int DO_YOU_WISH_QUIT_PARENT = 2802;
	public static final int IMPOSIBLE_QUIT_PARENT = 2803;
	
	//Ventana GUI_RenombrarAtributo 29xx
	public static final int RENAME_ATTRIBUTE = 2900;
	public static final int WRITE_NAME_ATTRIBUTE_SELECTED = 2901;
	
	//Ventana GUI_RenombrarDominio 30xx
	public static final int RENAME_DOMAIN = 3000;
	public static final int WRITE_NEW_DOMAIN_NAME = 3001;
	
	//GUI_RenombrarEntidad  31xx
	public static final int RENAME_ENTITY_DBDT = 3100;
	public static final int WRITE_NEW_ENTITY_NAME = 3101;
	
	//GUI_RenombrarRelation 32xx
	public static final int RENAME_RELATION_DBDT = 3200;
	public static final int EXPLICATION_RENAME_RELATION1 = 3201;
	
	//GUI_Conexion 33xx
	public static final int SERVER = 3300; 
	public static final int PORT = 3301;
	public static final int DATA_BASE = 3302;
	public static final int CREATE_DATA_BASE = 3303;
	public static final int USER_DATA_BASE = 3304;
	public static final int PASSWORD_DATA_BASE = 3305;
	
	//GUI_SeleccionarConexion
	public static final int SELECT_CONNECTION = 3306;		
	
	//SERVICIOS 16xx
	public static final int EMPTY_VALUE = 1600;//"Uno de los valores es vacío."
	public static final int INCORRECT_NUMBER  = 1601;
	public static final int INCORRECT_VALUE_EX  = 1602;//"Uno de los valores no es correcto. Ejemplos:"	
	public static final int INCORRECT_BIT_VALUE  = 1603;//"Uno de los valores no es correcto. Los únicos valores aceptables son 0 y 1"
	public static final int QUOTATION_MARKS  = 1604;//"Introduzca los valores entre comillas simples"
	public static final int INCORRECT_DATE  = 1605;//"Uno de los valores no es correcto. Introduzca valores de la forma 'aaaammdd'"
	//servicios sistema
	public static final int RATIFYING = 1606;//"Validando "
	public static final int RATIFYING_ATTRIB_DOMAIN = 1607;//"Validando el dominio del atributo"
	public static final int COMPOSED_ATTRIBUTE = 1608;//"El atributo es compuesto y no puede tener dominio."
	public static final int CORRECT_DOMAIN = 1609;//"El dominio del atributo es correcto."
	public static final int NO_DOMAIN = 1610;//"El atributo no tiene dominio."
	public static final int UNKNOWN_DOMAIN = 1611;//"El dominio del atributo no esta en el sistema."
	public static final int RATIFYING_ATTRIBUTE = 1612;//"Validando la exclusividad del atributo"
	public static final int ONE_ATTRIB_SUBATTRIB = 1613;//" es subatributo de un solo atributo."
	public static final int MANY_ATTRIB_SUBATTRIB = 1614;//" es subAtributo de más de un atributo."
	public static final int ONE_ENTITY = 1615;//" pertenece a una sola entidad."
	public static final int MANY_ENTITIES = 1616;//" pertenece a más de una entidad."
	public static final int RATIFYING_CHILDREN = 1617;//"Validando hijos atributo compuesto."
	public static final int NO_SUBATTRIB = 1618;//"El atributo no tiene subatributos."
	public static final int ONE_SUBATTRIB = 1619;//"El atributo solo tiene un subatributo."
	public static final int RATIFYING_PRIMARYKEYS = 1620;//"Validando claves primarias de la entidad."
	public static final int NOKEY_WEAK_ENTITY = 1621;//"La entidad es debil y carece de atributo discriminante (poner clave primaria)."
	public static final int NOKEY_ENTITY_RELATION = 1622;//"La entidad está vinculada a una relación y no tiene clave primaria."
	public static final int ALL_CHILDREN_KEYS = 1623;//" El atributo es compuesto y todas sus hojas son claves. También se puede señalar como clave al atributo padre."
	public static final int NO_ATTRIB_KEY = 1624;//	" La o las claves de la entidad no forman parte de sus atributos."
	public static final int MULTIVALUE_KEY = 1625;//	" es multivalorado y no puede ser clave."
	public static final int ISA_PARENT = 1626;//	" La entidad es una hija en una relacion IsA, puede heredar la clave."
	public static final int NO_PRIMARY_KEY = 1627;//	" La entidad no tiene clave primaria."
	public static final int CORRECT_KEYS = 1628;//	" Las claves de la entidad son correctas."
	public static final int RATIFYING_ATTRIB_NAMES = 1629;//	"Validando nombres de los atributos de la entidad"
	public static final int NO_ATTRIB = 1630;//	" no tiene atributos."
	public static final int ATTRIB_NAME = 1631;//	" El nombre del atributo "
	public static final int IS_REPEATED_IN_ENTITY = 1632;//	" está repetido en la entidad "
	public static final int ATTRIB_NAMES = 1633;//	" Los nombres de los atributos de la entidad "
	public static final int ARE_CORRECT = 1634;//	" son correctos."
	public static final int ENT_PARENT_IN = 1635;//	" La entidad es padre en "
	public static final int ISA_RELATIONS = 1636;//	" relaciones IsA"
	public static final int NO_WEAK_ENT_RELAT = 1637;//	" Esta relación no puede tener entidades debiles."
	public static final int NO_PARENT_REL = 1638;//	" La relación carece de padre."
	public static final int NO_CHILD_RELATION = 1639;//	" La relación necesita al menos un hijo."
	public static final int OK_RELATION = 1640;//	" La relación tiene padre y al menos un hijo."
	public static final int IS_NORMAL_TYPE = 1641;//	" es de tipo Normal"
	public static final int NO_ENT_RELATION = 1643;//	" La relación carece de entidades."
	public static final int ONE_ENT_REL = 1644;//" La relación sólo relaciona una entidad."
	public static final int MANY_ENT_REL = 1645;//" La relación asocia dos o más entidades."
	public static final int IS_WEAK_TYPE = 1646;//	" es de tipo Débil"
	public static final int ONE_ENT_WEAK_REL = 1648;//	"La relación es débil y necesita mas de una entidad."
	public static final int NO_WEAK_ENT_REL = 1649;//	" La relación no posee ninguna entidad débil."
	public static final int MANY_WEAK_ENT_REL = 1650;//	" La relación sólo puede tener una entidad débil."
	public static final int NO_STRONG_ENT_REL = 1651;//	" La relación no posee ninguna entidad fuerte."
	public static final int OK_ENT_REL = 1652;//	" La relación posee las entidades adecuadas."
			
	public static final int RATIFYING_DOMAIN = 1653;//	"Validando la unicidad del dominio"
	public static final int REPEATED_DOM_NAMES = 1654;//	"Hay varios dominios con el mismo nombre."
	public static final int OK_DOMAIN = 1655;//"El dominio es único."
	public static final int RATIFYING_DOMAIN_VALUES = 1656;//"Validando valores del dominio"
	public static final int NO_VALUE_DOM = 1657;//"El dominio no tiene ningún valor."
	public static final int THE_VALUE = 1658;//"El valor "
	public static final int IS_REPEATED = 1659;//" está indicado varias veces."
	public static final int OK_DOM_VALUES = 1660;//"Los valores del dominio no se repiten."
	public static final int RATIFYING_DOM_USE = 1661;//"Validando uso del dominio"
	public static final int USE_DOM = 1662;//"El dominio está usado en el diseño."
	public static final int NO_USE_DOM = 1663;//"El dominio no está usado en el diseño."
			
	public static final int RATIFYING_DB = 1664;//"INICIANDO LA VALIDACIÓN DE LA BASE DE DATOS"
	public static final int RATIFYING_DOMAINS = 1665;//"VALIDANDO DOMINIOS"
	public static final int RATIFYING_ATTRIBUTES = 1666;//"VALIDANDO ATRIBUTOS"
	public static final int RATIFYING_ENTITIES = 1667;//"VALIDANDO ENTIDADES"
	public static final int RATIFYING_RELATIONS = 1668;//"VALIDANDO RELACIONES"
	public static final int RATIFY_SUCCESS = 1669;//"VALIDACIÓN REALIZADA CON ÉXITO"
	public static final int RATIFY_ERROR = 1670;//"ERROR EN EL PROCESO. VALIDACIÓN INTERRUMPIDA"
	
	public static final int MUST_RATIFY_MODEL = 1671;//"Debe validar satisfactoriamente el modelo antes de generar el script."
	public static final int DESIGN_SQL_CODE = 1672;//"CÓDIGO SQL DEL DISEÑO"
	public static final int MUST_GENERATE_SCRIPT = 1673;//"Debe generar el Script SQL antes de exportarlo."
	public static final int SQL_FILES = 1674;//"Ficheros SQL"
	public static final int OK_FILE = 1675;//"Se ha generado satisfactoriamente el fichero."
	public static final int FILE = 1676;//"Fichero"
	public static final int SCRIPT_ERROR = 1677;//"Se ha producido un error al volcar el Script en el fichero."
	public static final int SCRIPT_GENERATED_FOR = 1678;//"El script SQL fue generado para ejecutarse en un DBMS de tipo"
	public static final int CONEXION_TYPE_IS = 1679;//"pero el tipo de conexión actual es"
	public static final int POSSIBLE_ERROR_SRIPT = 1680;//"Es posible que el script no se ejecute correctamente."
	public static final int SHOULD_GENERATE_SCRIPT = 1681;//"Para evitar este problema, genere de nuevo el script para el tipo"
	public static final int OF_CONEXION = 1682;//"de conexión actual."
	public static final int CONTINUE_ANYWAY = 1683;//"¿Desea continuar de todas maneras?"
	public static final int NO_DB_CONEXION = 1684;//"No se pudo abrir una conexión con la base de datos."
	public static final int REASON = 1685;//"el motivo es el siguiente"
	public static final int CANT_EXECUTE_SCRIPT = 1686;//"No se pudo ejecutar el script."
	public static final int ENQUIRY_ERROR = 1687;//"El error se produjo en la consulta"
	public static final int CANT_CLOSE_CONEXION = 1688;//"No se pudo cerrar la conexión."
	public static final int OK_SCRIPT_EXECUT = 1689;//"El script se ha ejecutado correctamente."
	
	public static final int ANALYSIS = 1690;//"SECCIÓN DE CREACIÓN DE TABLAS"
	public static final int TYPES_SECTION = 1691;//"SECCIÓN DE CREACIÓN DE TIPOS ENUERADOS"
	public static final int KEYS_SECTION = 1692;//"SECCIÓN DE ESTABLECIMIENTO DE CLAVES"
	public static final int CONSTRAINTS_SECTION = 1695;
	
	public static final int MUST_VALIDATE_MODEL = 1693;//"Debe validar satisfactoriamente el modelo antes de generar el modelo relacional."
	public static final int RELATIONAL_MODEL_GENERATED = 1694;//"MODELO RELACIONAL GENERADO"
	
	//Persistencia 22XX
	public static final int UNESPECTED_XML_ERROR = 2200;
	public static final int ENT_ARITY = 2201;// "Entidad y aridad: ";
	public static final int ROL = 2202;//"Rol "
	public static final int ID_ENT = 2203;//", Id de entidad "
	
	//Presentacion.Grafo 40XX
	public static final int DELETE_ALL_NODES  = 4001;//"Todos los nodos seleccionados serán eliminados definitivamente del sistema."
	public static final int DBCASE_DELETE  = 4002;//"DBCASE: Eliminar"
	public static final int NO_ENTITY_INVOLVED  = 4003;//"No interviene ninguna entidad"
	public static final int PARENT_ENTITY  = 4004;//"Entidad padre"
	public static final int NO_CHILDS_ENTITIES  = 4005;//"No tiene entidades hijas"
	public static final int CHILDREN_ENTITIES  = 4006;//"Entidades hijas"
	public static final int IS_MULTIVALUATED = 4009;//	"Es un atributo multivalorado"
	public static final int IS_NOT_NULL  = 4010;//	"Es un atributo no nulo"
	public static final int IS_UNIQUE  = 4011;//	"Es un atributo único"
	public static final int IS_COMPOSED  = 4012;//	"Es un atributo compuesto"
	public static final int NO_COMPONENTS  = 4013;//	"No tiene componentes"
	public static final int COMPONENTS  = 4014;//	"Componentes"
	public static final int IS_WEAK_ENT  = 4015;//	"Es una entidad debil"
	public static final int ADD_ENTITY = 4016;//"Insertar una nueva entidad"
	public static final int ADD_RELATION = 4017;//"Insertar una nueva relación"
	public static final int ADD_ISARELATION = 4018;//"Insertar una nueva relación IsA"
	public static final int ADD_DOMAIN = 4019;//"Crear un nuevo dominio"
	public static final int RENAME_ENTITY = 4020;//"Renombrar la entidad"
	public static final int ADD_ATTRIBUTE = 4022;//"Añadir un nuevo atributo"
	public static final int DELETE_ENT = 4023;//"Eliminar la entidad"
    public static final int RENAME_ATTRIB = 4024;//"Renombrar el atributo"
	public static final int EDIT_DOMAIN = 4025;//"Editar el dominio"
	public static final int ADD_SUBATTRIBUTE = 4026;//"Añadir un subatributo"
	public static final int NOT_NULL = 4027;//"Not Null"
	public static final int UNIQUE = 4028;//"Unique"
	public static final int IS_PRIMARY_KEY = 4030;//"Es clave primaria de la entidad"
	public static final int DELETE_ATTRIB = 4031;//"Eliminar el atributo"
	public static final int SET_PARENT_ENT = 4032;//"Establecer entidad padre"
	public static final int REMOVE_PARENT_ENT = 4033;//"Quitar entidad padre"
	public static final int ADD_CHILD_ENT = 4034;//"Añadir entidad hija"
	public static final int REMOVE_CHILD_ENT = 4035;//	"Quitar entidad hija"
	public static final int DELETE_REL = 4036;//"Eliminar la relación"
	public static final int RENAME_RELATION = 4037;//	"Renombrar la relación"
	public static final int ADD_ENT = 4039;//	"Añadir una entidad"
	public static final int REMOVE_ENTITY = 4040;//	"Quitar una entidad"
	public static final int EDIT_CARD_ROL = 4041;//	"Editar la cardinalidad o el rol de una entidad"
	
	// GUIPrincipal + utilidades.AccionMenu 41XX
	public static final int DOM_MENU_RENAME = 4101;//"Renombrar"
	public static final int DOM_MENU_DELETE = 4102;//"Eliminar"
	public static final int DOM_MENU_MODIFY = 4103;//"Modificar dominio"
	public static final int DOM_MENU_ADD = 4104;//"Añadir nuevo dominio"
	public static final int DOM_MENU_IN_ORDER = 4105;//"Ordenar valores"
	public static final int DOM_TREE_CREATED_DOMS = 4106;//"Dominios creados"
	public static final int DOM_TREE_TYPE = 4107;//"tipo base"
	public static final int DOM_TREE_VALUES = 4109;//"Valores"
	
	public static final int SYSTEM = 4110;//"Sistema"
	public static final int EXIT_MINCASE = 4112;//"Salir"
	public static final int OPTIONS = 4113;//"Opciones"
	public static final int CURRENT_DBMS = 4114;//"Gestor de Base de Datos actual"
	public static final int SELECT_LANGUAGE = 4136; //"Lenguaje"
	public static final int EXPORT_DIAGRAM = 4115;//"Exportar el diagrama E/R como archivo de imagen"
	public static final int PRINT_DIAGRAM = 4116;//"Imprimir el diagrama E/R"
	public static final int HELP = 4117;//"Ayuda"
	public static final int CONTENTS = 4118;//"Contenidos"
	public static final int ABOUT = 4119;//"Acerca de DBDT"
	public static final int ER_MODEL = 4120;//"Diagrama Entidad Relación"
	public static final int INF_PANEL = 4121;//"Panel de información"
	public static final int DOM_PANEL = 4122;//"Panel dominios"
	public static final int GRAPH_PANEL = 4123;//"Panel grafo"
	public static final int EVENT_PANEL = 4124;//"Panel de sucesos"
	public static final int SHOW_EVENTS_PANEL = 4160;
	public static final int CODE_GENERATION = 4125;//"Generación de código"
	public static final int CLEAN = 4126;//"Limpiar el area de texto"
	public static final int VALIDATE_DESIGN = 4127;//"Validar el diseño realizado"
	public static final int GENERATE_REL_MODEL = 4128;//"Generar el Modelo Relacional del diseño realizado"
	public static final int GENERATE_SQL_CODE = 4129;//"Generar el código SQL del diseño realizado"
	public static final int EXPORT_SQL_FILE = 4130;//"Exportar el Script SQL a un archivo"
	public static final int EXECUTE_SCRIPT = 4131;//"Ejecutar el script SQL en un DBMS"
	public static final int MUST_GENERATE_SCRIPT_EX = 4133;//"Debe generar el Script SQL antes de ejecutarlo."
	public static final int JPEG_FILES = 4134;//"Ficheros JPEG"
	public static final int OK_EXPORT = 4135;//"Se ha exportado satisfactoriamente el diagrama al fichero"
	
	public static final int TOOL_FOR_DESING=4137;
	public static final int SS_II=4138;
	public static final int COLLEGE=4139;
	public static final int UNIVERSITY=4140;
	public static final int DIRECTOR=4141; 
	public static final int TEACHER_NAME=4142;
	public static final int AUTHORS=4143;
	public static final int AUTHOR1=4144;
	public static final int AUTHOR2=4145;
	public static final int AUTHOR3=4146;				
	public static final int BASED=4147;
	public static final int CONTACT=4148;
	public static final int DBCASE_LABEL=4149;
	public static final int DB_CASE_TOOL=4150;
	
	//Utilidades.conectorDBMS
	public static final int NO_CONECTOR =4200;//"Error: No se encuentra el conector JDBC"
	public static final int TABLE_TITLE =4202;
	public static final int TEST_DATA =4203;
	public static final int CLEAN_FIELDS =4204;
	public static final int HINT =4205;
	public static final int EXPLORE =4206;
	public static final int EXISTING_CONN =4207;
	public static final int CHOOSE_CONN =4208;
	
	//Mas cosas
	public static final int RESTRICTIONS =4300;
	public static final int ADD_RESTRICTIONS =4301;
	public static final int TABLE_UNIQUE =4302;
	public static final int ADD_UNIQUE =4303;
	public static final int UNIQUE_ERROR =4304;
	public static final int ERROR_TABLE =4305;
	public static final int CONC_MODEL =4306;
	public static final int LOGIC_MODEL =4307;
	public static final int PHYS_MODEL =4308;
	public static final int THEME =4309;
	public static final int GENERATE =4310;
	public static final int ELEMENTS =4311;
	public static final int VIEW = 4312;
	public static final int TABLE = 4313;
	public static final int VOLUME = 4314;
	public static final int FREQ = 4315;
	public static final int CANDIDATE_KEYS = 4316;
	public static final int TABLE_CONSTR = 4317;
	public static final int RELATIONS = 4318;
	public static final int RIC = 4319;
	public static final int LOST_CONSTR = 4320;
	public static final int THE_ATRIBUTE = 4321;
	public static final int THE_RELATION = 4322;
	public static final int THE_ENTITY = 4323;
	public static final int EMPTY_DIAGRAM = 4324;
	public static final int SCRIPT_GENERATED = 4325;
	public static final int SYNTAX = 4326;
	public static final int BASED1 = 4327;
	public static final int BASED1A1 = 4328;
	public static final int BASED1A2 = 4329;
	public static final int BASED1A3 = 4330;
	public static final int BASED2 = 4331;
	public static final int BASED2A1 = 4332;
	public static final int BASED2A2 = 4333;
	public static final int BASED2A3 = 4334;
	public static final int BASED2P = 4335;
	public static final int AUTHOR = 4336;
	public static final int TABLES = 4337;
	public static final int NULLATTR = 4338;
	public static final int PART_TOTAL = 4339;
	public static final int CARDMINDE = 4340;
	public static final int YCARDMINDE = 4341;
	public static final int CARDMAXDE = 4342;
	public static final int YCARDMAXDE = 4343;
	public static final int YMAXDE = 4344;
	public static final int OF = 4345;
	public static final int FOR = 4346;
		
	// Mensaje por defecto
	private static String notExistingMessage = "Asked message does not exist";
	
	// --- --- --- ATRIBUTOS --- --- ---
	/**
	 * Indica en qué idioma están actualmente los campos de texto.
	 */
	private static String _idiomaActual = null;
	/**
	 * Almacena las rutas de los distintos lenguajes permitidos.
	 */
	private static Hashtable<String, String> _lenguajes = new Hashtable<String, String>();
	/**	
	 * Almacena los distintos campos de texto. La clave es el nombre del campo, y el valor
	 * su traducción en el lenguaje actual
	 */
	private static Hashtable<String, String> _textos = new Hashtable<String, String>();
	
	// --- --- --- ACCESORES / MUTADORES --- --- ---
	/**
	 * Muestra en qué idioma están actualmente los campos de texto.
	 * 
	 * @return null si el método cargaLenguaje aún no ha sido invocado.
	 */
	public static String getIdiomaActual(){
		return _idiomaActual;
	}

	/**
	 * Muestra todos los lenguajes disponibles en la aplicación
	 */
	public static Vector<String> obtenLenguajesDisponibles(){
		Vector<String> sol = new Vector<String>();
		Enumeration<String> lengs = _lenguajes.keys();
		while (lengs.hasMoreElements()) sol.add(lengs.nextElement());
		return sol;
	}
	
	// --- --- --- METODOS --- --- ---
	/**
	 * Inspecciona el fichero index.txt, alojado en la carpeta languages del proyecto, 
	 * para obtener la ruta de todos los ficheros .lng alojados en la carpeta.
	 */
	public static void encuentraLenguajes(){
		// Leer el indice
		Object o = new Object();
		InputStream input = o.getClass().getResourceAsStream(Lenguaje.CARPETA + Lenguaje.INDICE);
		BufferedReader datos = new BufferedReader(new InputStreamReader(input));
		try {
			// Analizar su contenido
			String linea = datos.readLine();
			while (linea != null){
				// Comprobar que no es vacía ni comentario
				if (linea.indexOf("=")>0 && !linea.startsWith("#")){
					// Extraer nombre del fichero
					String fich = linea.substring(0, linea.indexOf("="));
					String nombre = linea.substring(linea.indexOf("=") + 1);
					nombre = corrigeCaracteres(nombre);
					_lenguajes.put(nombre, fich);
				}
				// Incrementar
				linea = datos.readLine();
			}
		} catch (Exception e) {
			System.out.println("El fichero índice de idiomas no se encuentra o no tiene el formato correcto");
			System.exit(-1);
		}
	}
	
	/**
	 * Sobreescribe los valores de los campos de texto con el lenguaje indicado
	 * 
	 * @param nombreLenguaje
	 */
	public static void cargaLenguaje (String nombreLenguaje){	
		cargaLng( _lenguajes.get(nombreLenguaje));
	}
	
	/**
	 * Lee el fichero .lng asociado al lenguaje por defecto
	 */
	public static void cargaLenguajePorDefecto (){
		cargaLng(Lenguaje.DEFAULT);
	}
	
	/**
	 * Lee el fichero .lng dado, y sobreescribe los valores de los campos de texto.
	 * 
	 * @param fichero Fichero a cargar.
	 */
	private static void cargaLng(String fichero){
		// Abrir el fichero
		// Leer el indice
		Object o = new Object();
		InputStream input = o.getClass().getResourceAsStream(Lenguaje.CARPETA + fichero);
		BufferedReader datos = new BufferedReader(new InputStreamReader(input));
		
		try {
			// Analizar su contenido
			String linea = datos.readLine();
			boolean idiomaLeido = false;
			while (linea != null){
				// Comprobar que no es comentario
				if (!linea.startsWith("#")){
					// Si es la primera linea...
					if (!idiomaLeido){
						// Poner idioma actual
						_idiomaActual = linea;
						idiomaLeido = true;
					} else if(linea.indexOf("=") > 0) {
						// Añadir la frase a la biblioteca
						String[] parte = linea.split("=");
						_textos.put(parte[0], parte[1]);
					}
				}
				// Incrementar
				linea = datos.readLine();
			}
		} catch (Exception e) {
			System.out.println("El fichero de idioma " + fichero + " no se encuentra o no tiene el formato correcto");
			System.exit(-1);
		}
	}
	
	/**
	 * Muestra el mensaje solicitado, en el idioma actual.
	 * @param tipoMensaje
	 * @return
	 */
	public static String text(int tipoMensaje) {
		String texto;
		
		switch (tipoMensaje){
		case DBCASE: texto = _textos.get("dbcase"); break;
		case SELECT: texto = _textos.get("select"); break;
		case YES: texto = _textos.get("yes"); break;
		case NO: texto = _textos.get("no"); break;
		case CANCEL: texto = _textos.get("cancel");break;
		case INSERT: texto = _textos.get("insert");break;
		case INFO: texto = _textos.get("info");break;
		case ERROR: texto = _textos.get("error");break;
		case SUCCESS: texto = _textos.get("success");break;
		case WARNING: texto = _textos.get("warning");break;
		case WISH_CONTINUE: texto = _textos.get("wishContinue");break;
		case EXIT: texto = _textos.get("exit");break;
		case EXECUTE: texto = _textos.get("execute");break;
		case EDIT: texto = _textos.get("edit");break;
		case REMOVE: texto = _textos.get("remove");break;
		case ENTITY: texto = _textos.get("entity"); break;//"Entidad"
		case RELATION: texto = _textos.get("relation"); break;//"Relación"
		case ATTRIBUTE: texto = _textos.get("attribute"); break;//"Atributo"
		case ISA_RELATION : texto = _textos.get("isaRelation"); break;//"Relación IsA"
		case ENTITIES: texto = _textos.get("entities"); break;//"Entidades"
		case CARDINALITY: texto = _textos.get("cardinality"); break;//"Cardinalidad"
		case ATTRIBUTES: texto = _textos.get("attributes"); break;//"Atributos"
		case COMPOSED: texto = _textos.get("composed"); break;//"Compuesto"
		case DOMAIN: texto = _textos.get("domain"); break;//"Dominio"
		case PRIMARY_KEYS: texto = _textos.get("primaryKeys"); break;//"Claves primarias"
		case DELETE: texto = _textos.get("delete"); break;//"Eliminar"
		case RENAME:texto = _textos.get("rename"); break;//Renombrar
		case NEW: texto = _textos.get("new"); break;
		case OPEN: texto = _textos.get("open"); break;
		case CLOSE: texto = _textos.get("close"); break;
		case SAVE: texto = _textos.get("save"); break;
		case SAVE_AS: texto = _textos.get("saveAs"); break;
		case NAME: texto = _textos.get("name"); break;// NAME = 42;
		case TYPE: texto = _textos.get("type"); break;//public static final int TYPE = 43;
		case VALUES: texto = _textos.get("values"); break;//public static final int VALUES = 44;
		case ACCEPT: texto = _textos.get("accept"); break;
		case NEW2:texto = _textos.get("new2");break;
		
		case SELECT_WORKSPACE: texto = _textos.get("selectWorkspace"); break;
		case ERROR_CREATING_FILE: texto = _textos.get("errorCreatingFile"); break;
		
		case ERROR_TEMP_FILE: texto = _textos.get("errorTempFile"); break;//No se ha podido crear el fichero temporal
		case WRONG_FILE: texto = _textos.get("wrongFile"); break;//El fichero seleccionado no es de esta aplicación
		case NOT_EXIST_FILE: texto = _textos.get("notExistFile"); break;//El fichero no existe
		
		case WORKSPACE_IS: texto = _textos.get("workspaceIs"); break;//El directorio actual de trabajo es:
		case INITIAL_ERROR: texto = _textos.get("initialError"); break;//Se ha producido un error en la creacion inicial
		case OF_XMLFILES: texto = _textos.get("ofXMLfiles"); break;//de los ficheros XML en el directorio de trabajo:
		case DELETE_ENTITY: texto = _textos.get("deleteEntity"); break;//DBDT: Eliminar entidad
		case DELETE_ATTRIBUTE: texto = _textos.get("deleteAttribute"); break;//DBDT: Eliminar atributo
		case DELETE_DOMAIN: texto = _textos.get("deleteDomain"); break;//DBDT: Eliminar dominio
		case ADD_ENTITY_RELATION: texto = _textos.get("addEntityRelation"); break;//DBDT: Añadir una entidad a la relación
		case DELETE_ISA_RELATION: texto = _textos.get("deleteIsaRelation"); break;//DBDT: Eliminar relación IsA
		case DELETE_RELATION: texto = _textos.get("deleteRelation"); break;//DBDT: Eliminar relación
		case DELETE_ATTRIBUTES_WARNING: texto = _textos.get("deleteAttributes"); break;
		case REMOVE_FROM_SYSTEM: texto = _textos.get("removeFromSystem"); break;
		case THE_ATTRIBUTE: texto = _textos.get("attribute"); break;
		case WEAK_RELATION: texto = _textos.get("weakRelation"); break; 
		case DELETE_ATTRIBUTES_WARNING2: texto = _textos.get("deleteAttributes2"); break;
		case MODIFYING_CARDINALITY: texto = _textos.get("modifyingCardinality"); break;
		case MODIFY_ATTRIBUTE: texto = _textos.get("modifyAttribute"); break; 
		case DELETE_ATTRIBUTES_WARNING3: texto = _textos.get("deleteAttributes3"); break;
		case ISA_RELATION_DELETE: texto = _textos.get("deleteIsaRelation"); break;
		case MODIFYING_ATTRIBUTES_WARNING4: texto = _textos.get("modifyingAttributesWarning"); break;
		case RELATION_WEAK_ENTITIES: texto = _textos.get("relationWeakEntities"); break;
		case NO_ATTRIBUTES_RELATION: texto = _textos.get("noAttributesRelation"); break;
		case ALREADY_WEAK_ENTITY: texto = _textos.get("alreadyWeakEntity"); break;
		case IS_WEAK: texto = _textos.get("isWeak"); break;
		case INCORRECT_VALUE: texto = _textos.get("incorrectValue"); break;	
		case ENTITIES_FILE_ERROR: texto = _textos.get("entitiesFileError"); break;//Se ha producido un error en el acceso al fichero de entidades. 
		case RELATIONS_FILE_ERROR: texto = _textos.get("relationsFileError"); break;//Se ha producido un error en el acceso al fichero de relaciones.
		case ATTRIBUTES_FILE_ERROR: texto = _textos.get("attributesFileError"); break;//Se ha producido un error en el acceso al fichero de atributos.
		case DOMAINS_FILE_ERROR: texto = _textos.get("domainsFileError"); break;//Se ha producido un error en el acceso al fichero de dominios.
		case EMPTY_ATTRIB_NAME: texto = _textos.get("emptyAttribName"); break;//El nombre del atriburo es vacío. 
		case EMPTY_DOM_NAME: texto = _textos.get("emptyDomName"); break;//El nombre del dominio es vacío.
		case EMPTY_ENT_NAME: texto = _textos.get("emptyEntName"); break;//El nombre de la entidad es vacío.
		case EMPTY_REL_NAME: texto = _textos.get("emptyRelName"); break;//El nombre de la relación es vacío.
		case EMPTY_SUBATTR_NAME: texto = _textos.get("emptySubattribName"); break;//El nombre del subatributo es vacío.
		case EMPTY_TYPE_NAME: texto = _textos.get("emptyTypeName"); break;//El nuevo tipo es vacío.
		case EMPTY_VALUES: texto = _textos.get("emptyValues"); break;//El campo 'valores' es vacío.
		case REPEATED_ENT_NAME: texto = _textos.get("repeatedEntName"); break;//Existe una entidad en el sistema con el mismo nombre.
		case REPEATED_ATTRIB_NAME: texto = _textos.get("repeatedAttribName"); break;//Ya existe un atributo con ese nombre en la entidad.
		case REPEATED_ATTRIB_NAME_REL: texto = _textos.get("repeatedAttribNameRel"); break;//Ya existe un atributo en la relación con ese nombre.		
		case REPEATED_REL_NAME: texto = _textos.get("repeatedRelName"); break;//Existe una relación en el sistema con el mismo nombre.
		case REPEATED_ROL_NAME: texto = _textos.get("repeatedRolName"); break;//Existe otro rol en la relación con el mismo nombre.
		case REPEATED_SUBATR_NAME: texto = _textos.get("repeatedSubatrName"); break;//Existe otro subatributo con ese nombre.
		case REPEATED_DOM_NAME: texto = _textos.get("repeatedDomName"); break;//Existe otro dominio en el sistema con el mismo nombre.
		case INCORRECT_SIZE1: texto = _textos.get("incorrectSize1"); break;//El tamano del dominio del atributo no es un valor entero positivo.
		case INCORRECT_SIZE2: texto = _textos.get("incorrectSize2"); break;//El tamano del dominio del atributo es nulo o negativo.
		case INCORRECT_SIZE3: texto = _textos.get("incorrectSize3"); break;//El tamaño del atributo no es un valor entero positivo. 
		case INCORRECT_CARDINALITY1: texto = _textos.get("incorrectCardinality1"); break;//El valor de inicio no es un número entero positivo o 'n'.
		case INCORRECT_CARDINALITY2: texto = _textos.get("incorrectCardinality2"); break;//El valor de inicio es un número negativo.
		case INCORRECT_CARDINALITY3: texto = _textos.get("incorrectCardinality3"); break;//El valor de fin no es un número entero positivo o 'n'.
		case INCORRECT_CARDINALITY4: texto = _textos.get("incorrectCardinality4"); break;//El valor de fin es un número negativo.
		case INCORRECT_CARDINALITY5: texto = _textos.get("incorrectCardinality5"); break;//El valor de inicio es mayor que el de fin.
		case NECESARY_ROL: texto = _textos.get("necesaryRol"); break;//Debe asignar un rol a la entidad.
		//case SUCCESS_SAVE: texto = _textos.get("successFile"); break;
		case WISH_SAVE: texto = _textos.get("wishSave"); break;
		case IF_WEAK_ENTITY:texto =_textos.get("weakEntity");break;	
		case WARNING_DELETE_WEAK_ENTITY:texto = _textos.get("warningDeleteWeakEntity");break;
		case WARNING_DELETE_WEAK_RELATION:texto = _textos.get("warningDeleteWeakRelation");break;
		case REPEATED_ENTITY_REL:texto = _textos.get("repeatedEntyRel");break;
		//SERVICIOS
		case EMPTY_VALUE: texto = _textos.get("emptyValue"); break;//"Uno de los valores es vacío."
		case INCORRECT_NUMBER: texto = _textos.get("incorrectNumber"); break;//"Uno de los valores no es correcto. Introduzca valores numéricos enteros"
		case INCORRECT_VALUE_EX: texto = _textos.get("incorrectValueEx"); break;//"Uno de los valores no es correcto. Ejemplos:"
		case INCORRECT_BIT_VALUE: texto = _textos.get("incorrectBitValue"); break;//"Uno de los valores no es correcto. Los únicos valores aceptables son 0 y 1"
		case QUOTATION_MARKS: texto = _textos.get("quotationMarks"); break;//"Introduzca los valores entre comillas simples"
		case INCORRECT_DATE: texto = _textos.get("incorrectDate"); break;//"Uno de los valores no es correcto. Introduzca valores de la forma 'aaaammdd'"
		//SERVICIOS SISTEMA
		case RATIFYING: texto = _textos.get("ratifying"); break;//"Validando "
		case RATIFYING_ATTRIB_DOMAIN: texto = _textos.get("ratifyinAttribDomain"); break;//"Validando el dominio del atributo"
		case COMPOSED_ATTRIBUTE: texto = _textos.get("composedAttribute"); break;//"El atributo es compuesto y no puede tener dominio."
		case CORRECT_DOMAIN: texto = _textos.get("correctDomain"); break;//"El dominio del atributo es correcto."
		case NO_DOMAIN: texto = _textos.get("noDomain"); break;//"El atributo no tiene dominio."
		case UNKNOWN_DOMAIN: texto = _textos.get("unkownDomain"); break;//"El dominio del atributo no esta en el sistema."
		case RATIFYING_ATTRIBUTE: texto = _textos.get("ratifyingAttribute"); break;//"Validando la exclusividad del atributo"
		case ONE_ATTRIB_SUBATTRIB: texto = _textos.get("oneAttribSubattrib"); break;//" es subatributo de un solo atributo."
		case MANY_ATTRIB_SUBATTRIB: texto = _textos.get("manyAttribSubattrib"); break;//" es subAtributo de más de un atributo."
		case ONE_ENTITY: texto = _textos.get("oneEntity"); break;//" pertenece a una sola entidad."
		case MANY_ENTITIES: texto = _textos.get("manyEntities"); break;//" pertenece a más de una entidad."
		case RATIFYING_CHILDREN: texto = _textos.get("ratifyingChildren"); break;//"Validando hijos atributo compuesto."
		case NO_SUBATTRIB: texto = _textos.get("noSubattrib"); break;//"El atributo no tiene subatributos."
		case ONE_SUBATTRIB: texto = _textos.get("oneSubattrib"); break;//"El atributo solo tiene un subatributo."
		case RATIFYING_PRIMARYKEYS: texto = _textos.get("ratifyingPrimaryKeys"); break;//"Validando claves primarias de la entidad."
		case NOKEY_WEAK_ENTITY: texto = _textos.get("noKeyWeakEntity"); break;//"La entidad es debil y carece de atributo discriminante (poner clave primaria)."
		case NOKEY_ENTITY_RELATION: texto = _textos.get("noKeyEntRel"); break;//"La entidad está vinculada a una relación y no tiene clave primaria."
		case ALL_CHILDREN_KEYS: texto = _textos.get("allChildrenKey"); break;//" El atributo es compuesto y todas sus hojas son claves. También se puede señalar como clave al atributo padre."
		case NO_ATTRIB_KEY: texto = _textos.get("noAttribKey"); break;//	" La o las claves de la entidad no forman parte de sus atributos."
		case MULTIVALUE_KEY: texto = _textos.get("multivalueKey"); break;//	" es multivalorado y no puede ser clave."
		case ISA_PARENT: texto = _textos.get("isaParent"); break;//	" La entidad es una hija en una relacion IsA, puede heredar la clave."
		case NO_PRIMARY_KEY: texto = _textos.get("noPrimaryKey"); break;//	" La entidad no tiene clave primaria."
		case CORRECT_KEYS: texto = _textos.get("correctKeys"); break;//	" Las claves de la entidad son correctas."
		case RATIFYING_ATTRIB_NAMES: texto = _textos.get("ratifyingAttribNames"); break;//	"Validando nombres de los atributos de la entidad"
		case NO_ATTRIB: texto = _textos.get("noAttrib"); break;//	" no tiene atributos."
		case ATTRIB_NAME: texto = _textos.get("attribName"); break;//	" El nombre del atributo "
		case IS_REPEATED_IN_ENTITY: texto = _textos.get("isRepeatedInEnt"); break;//	" está repetido en la entidad "
		case ATTRIB_NAMES: texto = _textos.get("attribNames"); break;//	" Los nombres de los atributos de la entidad "
		case ARE_CORRECT: texto = _textos.get("areCorrect"); break;//	" son correctos."
		case ENT_PARENT_IN: texto = _textos.get("entParentIn"); break;//	" La entidad es padre en "
		case ISA_RELATIONS: texto = _textos.get("isaRelations"); break;//	" relaciones IsA"
		case NO_WEAK_ENT_RELAT: texto = _textos.get("noWeakEntRel"); break;//	" Esta relación no puede tener entidades debiles."
		case NO_PARENT_REL: texto = _textos.get("noParentRel"); break;//	" La relación carece de padre."
		case NO_CHILD_RELATION: texto = _textos.get("noChildRel"); break;//	" La relación necesita al menos un hijo."
		case OK_RELATION: texto = _textos.get("okRelation"); break;//	" La relación tiene padre y al menos un hijo."
		case IS_NORMAL_TYPE: texto = _textos.get("isNormalType"); break;//	" es de tipo Normal"
		case NO_ENT_RELATION: texto = _textos.get("noEntRelation"); break;//	" La relación carece de entidades."
		case ONE_ENT_REL: texto = _textos.get("oneEntRel"); break;//" La relación sólo relaciona una entidad."
		case MANY_ENT_REL: texto = _textos.get("manyEntRel"); break;//" La relación asocia dos o más entidades."
		case IS_WEAK_TYPE: texto = _textos.get("isWeakType"); break;//	" es de tipo Débil"
		case ONE_ENT_WEAK_REL: texto = _textos.get("oneEntWeakRel"); break;//	"La relación es débil y necesita mas de una entidad."
		case NO_WEAK_ENT_REL: texto = _textos.get("noWeakEntRel"); break;//	" La relación no posee ninguna entidad débil."
		case MANY_WEAK_ENT_REL: texto = _textos.get("manyWeakEntRel"); break;//	" La relación sólo puede tener una entidad débil."
		case NO_STRONG_ENT_REL: texto = _textos.get("noStrongEntRel"); break;//	" La relación no posee ninguna entidad fuerte."
		case OK_ENT_REL: texto = _textos.get("okEntRel"); break;//	" La relación posee las entidades adecuadas."
				
		case RATIFYING_DOMAIN: texto = _textos.get("ratifyingDomain"); break;//	"Validando la unicidad del dominio"
		case REPEATED_DOM_NAMES: texto = _textos.get("repeatedDomNames"); break;//	"Hay varios dominios con el mismo nombre."
		case OK_DOMAIN: texto = _textos.get("okDomain"); break;//"El dominio es único."
		case RATIFYING_DOMAIN_VALUES : texto = _textos.get("ratifyingDomainValues"); break;//"Validando valores del dominio"
		case NO_VALUE_DOM: texto = _textos.get("noValueDom"); break;//"El dominio no tiene ningún valor."
		case THE_VALUE: texto = _textos.get("theValue"); break;//"El valor "
		case IS_REPEATED: texto = _textos.get("isRepeated"); break;//" está indicado varias veces."
		case OK_DOM_VALUES: texto = _textos.get("okDomValues"); break;//"Los valores del dominio no se repiten."
		case RATIFYING_DOM_USE: texto = _textos.get("ratifyingDomUse"); break;//"Validando uso del dominio"
		case USE_DOM: texto = _textos.get("useDom"); break;//"El dominio está usado en el diseño."
		case NO_USE_DOM: texto = _textos.get("noUseDom"); break;//"El dominio no está usado en el diseño."
				
		case RATIFYING_DB: texto = _textos.get("ratifyingDB"); break;//"INICIANDO LA VALIDACIÓN DE LA BASE DE DATOS"
		case RATIFYING_DOMAINS: texto = _textos.get("ratifyingDomains"); break;//"VALIDANDO DOMINIOS"
		case RATIFYING_ATTRIBUTES: texto = _textos.get("ratifyingAttributes"); break;//"VALIDANDO ATRIBUTOS"
		case RATIFYING_ENTITIES: texto = _textos.get("ratifyingEntities"); break;//"VALIDANDO ENTIDADES"
		case RATIFYING_RELATIONS: texto = _textos.get("ratifyingRelations"); break;//"VALIDANDO RELACIONES"
		case RATIFY_SUCCESS: texto = _textos.get("ratifySuccess"); break;//"VALIDACIÓN REALIZADA CON ÉXITO"
		case RATIFY_ERROR: texto = _textos.get("ratifyingError"); break;//"ERROR EN EL PROCESO. VALIDACIÓN INTERRUMPIDA"
		
		case MUST_RATIFY_MODEL: texto = _textos.get("mustRatifyModel"); break;//"Debe validar satisfactoriamente el modelo antes de generar el script."
		case DESIGN_SQL_CODE: texto = _textos.get("designSqlCode"); break;//"CÓDIGO SQL DEL DISEÑO"
		case MUST_GENERATE_SCRIPT: texto = _textos.get("mustGenerateScript"); break;//"Debe generar el Script SQL antes de exportarlo."
		case SQL_FILES: texto = _textos.get("sqlFiles"); break;//"Ficheros SQL"
		case OK_FILE: texto = _textos.get("okFile"); break;//"Se ha generado satisfactoriamente el fichero."
		case FILE: texto = _textos.get("file"); break;//"Fichero"
		case SCRIPT_ERROR: texto = _textos.get("scriptError"); break;//"Se ha producido un error al volcar el Script en el fichero."
		case SCRIPT_GENERATED_FOR: texto = _textos.get("scriptGeneratedFor"); break;//"El script SQL fue generado para ejecutarse en un DBMS de tipo"
		case CONEXION_TYPE_IS: texto = _textos.get("conexionTypeIs"); break;//"pero el tipo de conexión actual es"
		case POSSIBLE_ERROR_SRIPT: texto = _textos.get("possibleErrorScript"); break;//"Es posible que el script no se ejecute correctamente."
		case SHOULD_GENERATE_SCRIPT: texto = _textos.get("shouldGenerateScript"); break;//"Para evitar este problema, genere de nuevo el script para el tipo"
		case OF_CONEXION: texto = _textos.get("ofConexion"); break;//"de conexión actual."
		case CONTINUE_ANYWAY: texto = _textos.get("continueAnyway"); break;//"¿Desea continuar de todas maneras?"
		case NO_DB_CONEXION: texto = _textos.get("noDBConexion"); break;//"No se pudo abrir una conexión con la base de datos."
		case REASON: texto = _textos.get("reason"); break;//"el motivo es el siguiente"
		case CANT_EXECUTE_SCRIPT: texto = _textos.get("cantExecuteScript"); break;//"No se pudo ejecutar el script."
		case ENQUIRY_ERROR: texto = _textos.get("enquiryError"); break;//"El error se produjo en la consulta"
		case CANT_CLOSE_CONEXION: texto = _textos.get("cantCloseConexion"); break;//"No se pudo cerrar la conexión."
		case OK_SCRIPT_EXECUT: texto = _textos.get("okScriptExecut"); break;//"El script se ha ejecutado correctamente."
		
		case ANALYSIS : texto = _textos.get("analysis"); break;//"SECCIÓN DE CREACIÓN DE TABLAS"
		case TYPES_SECTION: texto = _textos.get("typesSection"); break;//"SECCIÓN DE CREACIÓN DE TIPOS ENUERADOS"
		case KEYS_SECTION: texto = _textos.get("keysSection"); break;//"SECCIÓN DE ESTABLECIMIENTO DE CLAVES"
		case CONSTRAINTS_SECTION: texto = _textos.get("constraintsSection"); break;
		
		case MUST_VALIDATE_MODEL: texto = _textos.get("mustValidateModel"); break;//"Debe validar satisfactoriamente el modelo antes de generar el modelo relacional."
		case RELATIONAL_MODEL_GENERATED: texto = _textos.get("relationalModelGenerated"); break;//"MODELO RELACIONAL GENERADO"
		
		//-----------PERSISTENCIA
		case UNESPECTED_XML_ERROR: texto = _textos.get("unespectedXMLerror"); break;//"Error inesperado en el fichero"
		case ENT_ARITY: texto = _textos.get("entArity"); break;// "Entidad y aridad: ";
		case ROL: texto = _textos.get("rol"); break;//"Rol "
		case ID_ENT: texto = _textos.get("idEnt"); break;//", Id de entidad "
		
		//--- --- --- GUI--- --- ---
		//GUI_InsertarEntidad
		case INSERT_ENTITY: texto = _textos.get("insertNewEntity"); break;
		case WEAK_ENTITY: texto = _textos.get("weakEntity");break;
		case WRITE_ENTITY_NAME: texto = _textos.get("writeEntityName");break;
		case SELECT_STRONG_ENTITY: texto = _textos.get("selectStrongEntity");break;
		case WRITE_RELATION_WEAK: texto = _textos.get("writeRelationWeak");break;
		case CREATE_STRONG_ENTITY: texto = _textos.get("createStrongEntity");break;
		
		//GUI_InsertarRelacion
		case INSERT_RELATION: texto = _textos.get("insertNewRelation"); break;
		case WRITE_RELATION_NAME: texto = _textos.get("writeRelationName");break;
		
		//GUI_AnadirAtributoEntidad y GUI_AnadirAtributoRelacion
		case INSERT_ATTRIBUTE: texto = _textos.get("insertNewAttribute"); break;
		case COMPOSITE_ATTRIBUTE: texto = _textos.get("compositeAttribute"); break;
		case PRIMARY_KEY_ATTRIBUTE: texto = _textos.get("primaryKeyAttribute"); break;
		case NOT_NULL_ATTRIBUTE: texto = _textos.get("notNullAttribute"); break;
		case UNIQUE_ATTRIBUTE: texto = _textos.get("uniqueAttribute"); break;
		case VALUE_ATTRIBUTE: texto = _textos.get("valueAttribute"); break;
		case DOMAIN_ATTRIBUTE: texto = _textos.get("domainAttribute"); break;
		case SIZE_ATTRIBUTE: texto = _textos.get("sizeAttribute"); break;
		
		//GUI_AnadirSubAtributoAtributo
		case INSERT_NEW_SUBATTRIBUTE: texto = _textos.get("insertNewSubAttribute"); break;
		
		//GUI_AnadirEntidadARelacion
		case INSERT_NEW_ENTITY_TO_RELATION: texto = _textos.get("insertNewEntityToRelation"); break;
		case IMPOSIBLE_TO_INSERT_ENTITY: texto = _textos.get("imposibleToInsertEntity"); break;
		case NO_ENTITY : texto = _textos.get("noEntities"); break;
		case SELECT_ENTITY: texto = _textos.get("selectEntity"); break;
		case WRITE_NUMBERS_RELATION: texto = _textos.get("writeNumbersRelation"); break;
		case THE: texto = _textos.get("the"); break;
		case TO: texto = _textos.get("to"); break;
		case WRITE_ROLL: texto = _textos.get("writeRoll"); break;
		case LABEL1A1: texto = _textos.get("label1a1");break;
		case LABEL1AN: texto = _textos.get("label1aN");break;
		case LABELNAN: texto=_textos.get("labelNaN");break;
		
		//GUI_AnadirEntidadHija
		case INSERT_NEW_DAUGTHER: texto = _textos.get("insertNewDaughter"); break;
		case IMPOSIBLE_TO_INSERT_DAUGHTER: texto = _textos.get("imposibleToInsertDaughter"); break;
		case NO_FATHER: texto = _textos.get("noFather"); break;
		case SELECT_ENTITY_DAUTHTER: texto = _textos.get("selectEntityDaughter"); break;
		case NO_ENTITIES_AVAILABLES:texto = _textos.get("noEntitiesAvailables"); break;
		
		//GUI_ConfigurarConexionDBMS
		case SHAPE_DBMS: texto = _textos.get("shapeDBMS"); break;
		case EXPLANATION_DBMS1: texto = _textos.get("explanationDBMS1"); break;
		case EXPLANATION_DBMS2: texto = _textos.get("explanationDBMS2"); break;
		case EXPLANATION_DBMS3: texto = _textos.get("explanationDBMS3"); break;
		case STRING_CONNECTION: texto = _textos.get("stringConnection"); break;
		case USER: texto = _textos.get("user"); break;
		case PASSWORD : texto = _textos.get("password"); break;
		case INFORMATION_INCOMPLETE : texto = _textos.get("informationIncomplete"); break;
		case DBDT: texto = _textos.get("dbdt"); break;
		case CONNECT: texto = _textos.get("connect"); break;
		
		//Ventana de GUI_EditarCardinalidadEntidad 
		case EDIT_ARITY_AND_ROLLE: texto = _textos.get("editArityAndRolle"); break;
		case IMPOSIBLE_EDIT_ROLLE: texto = _textos.get("imposibleToEditRolle"); break;
		case NO_ENTITIES_IN_RELATION: texto = _textos.get("noEntitiesInRelation"); break;
		case EDIT_ARITY: texto = _textos.get("editArity"); break;
		case SELECT_ENTITY_TO_CHANGE: texto = _textos.get("selectEntityToChange"); break;
		case WRITE_NEW_ARITY: texto = _textos.get("writeNewArity"); break;
		case WRITE_NEW_ROLLE: texto = _textos.get("writeNewRolle"); break;
		case IF_ENTITY_HAS_ROLLE: texto = _textos.get("ifEntityHasRolle"); break;
		
		//Ventana de GUI_EditarDominioAtributo
		case EDIT_DOMAIN_ATTRIBUTE: texto = _textos.get("editDomainAttribute"); break;
		case IMPOSIBLE_EDIT_DOMAIN: texto = _textos.get("imposibleEditDomain"); break;
		case COMPLEX_ATTRIBUTE: texto = _textos.get("complexAttribute"); break;
		case SELECT_DOMAIN_FOR_ATTRIBUTE: texto = _textos.get("selectDomainForAttribute"); break;
		
		//Ventana de GUI_EstablcerEntidadPadre
		case SET_PARENT_ENTITY: texto = _textos.get("setParentEntity"); break;
		case IMPOSIBLE_SET_PARENT: texto = _textos.get("imposibleSetParent"); break;
		case OTHER_PARENT: texto = _textos.get("otherParent"); break;
		case IMPOSIBLE_SET_PARENT_IN_ISA : texto = _textos.get("imposibleSetParenInISA"); break;
		case SELECT_PARENT_ENTITY: texto = _textos.get("selectParentEntity"); break;
		
		//Ventana de GUI_InsertarDominio 
		case INSERT_NEW_DOMAIN: texto = _textos.get("insertNewDomain"); break;
	//	case WRITE_NAME_DOMAIN: texto = _textos.get("writeNameDomain"); break;
	//	case CHOOSE_VALUE_AND_TYPE: texto = _textos.get("chooseValueAndType"); break;
		
		//Ventana de GUI_ModificarDominio 
		case EDIT_DOMAIN_GUI: texto = _textos.get("editDomain"); break;
		//case CHANGE_DOMAIN: texto = _textos.get("changeDomain"); break;
		//case REMEMBER_DOMAIN: texto = _textos.get("remember"); break;
		
		//Ventana de GUI_QuitarEntidadRelacion 
		case QUIT_ENTITY: texto = _textos.get("quitEntity"); break;
		case IMPOSIBLE_QUIT_ENTITY: texto = _textos.get("imposibleQuitEntity"); break;
		case SELECT_ENTITY_TO_QUIT : texto = _textos.get("selectEntityToQuit"); break;
		
		
		//Ventana GUI_QuitarEntidadARelacion 
		case QUIT_DAUGHTER_ENTITY: texto = _textos.get("quitDaughterEntity"); break;
		case IMPOSIBLE_QUIT_DAUGHTER_ENTITY: texto = _textos.get("imposibleQuitEntityDaughter"); break;
		case NO_DAUGHTER_ENTITY: texto = _textos.get("noDaughterEntity"); break;
		case SELECT_DAUGHTER_TO_QUIT: texto = _textos.get("selectDaughterToQuit"); break;
		
		//Ventana GUI_QuitarEntidadPadre 
		case QUIT_PARENT_ENTITY: texto = _textos.get("quitParentEntity"); break;
		case EXPLICATION_QUIT_PARENT: texto = _textos.get("explicationQuitParent"); break;
		case DO_YOU_WISH_QUIT_PARENT: texto = _textos.get("doYouWhisQuitParent"); break;
		case IMPOSIBLE_QUIT_PARENT: texto = _textos.get("imposibleQuitParent"); break;
		
		//Ventana GUI_RenombrarAtributo
		case RENAME_ATTRIBUTE: texto = _textos.get("renameAttribute"); break;
		case WRITE_NAME_ATTRIBUTE_SELECTED: texto = _textos.get("writeNameAttributeSelected"); break;
		
		//Ventana GUI_RenombrarDominio 
		case RENAME_DOMAIN: texto = _textos.get("renameDomain"); break;
		case WRITE_NEW_DOMAIN_NAME: texto = _textos.get("writeNewDomainName"); break;
		
		//GUI_RenombrarEntidad  
		case RENAME_ENTITY_DBDT: texto = _textos.get("renameEntityDBCase"); break;
		case WRITE_NEW_ENTITY_NAME: texto = _textos.get("writeNewEntityName"); break;
		
		//GUI_RenombrarRelation 
		case RENAME_RELATION_DBDT: texto = _textos.get("renameRelationDBCase"); break;
		case EXPLICATION_RENAME_RELATION1: texto = _textos.get("explicationRenameRelation1"); break;
		
		//GUI_Conexion 33xx		
		case SERVER: texto = _textos.get("server"); break;
		case PORT: texto = _textos.get("port"); break;
		case DATA_BASE: texto = _textos.get("dataBase"); break;
		case CREATE_DATA_BASE : texto = _textos.get("create"); break;
		case USER_DATA_BASE: texto = _textos.get("user"); break;
		case PASSWORD_DATA_BASE: texto = _textos.get("password"); break;
		
		//GUI_SeleccionarConexion
		case SELECT_CONNECTION: texto = _textos.get("selectConnection");break;
		
		//Presentacion.Grafo
		case DELETE_ALL_NODES: texto = _textos.get("deleteAllNodes"); break;//"Todos los nodos seleccionados serán eliminados definitivamente del sistema."
		case DBCASE_DELETE: texto = _textos.get("dbcaseDelete"); break;//"DBCASE: Eliminar"
		case NO_ENTITY_INVOLVED: texto = _textos.get("noEntityInvolved"); break;//"No interviene ninguna entidad"
		case PARENT_ENTITY: texto = _textos.get("parentEntity"); break;//"Entidad padre"
		case NO_CHILDS_ENTITIES: texto = _textos.get("noChildEntities"); break;//"No tiene entidades hijas"
		case CHILDREN_ENTITIES: texto = _textos.get("childrenEntities"); break;//"Entidades hijas"
		case IS_MULTIVALUATED: texto = _textos.get("isMultivaluated"); break;//"Es un atributo multivalorado"
		case IS_NOT_NULL: texto = _textos.get("isNotNull"); break;//	"Es un atributo no nulo"
		case IS_UNIQUE: texto = _textos.get("isUnique"); break;//	"Es un atributo único"
		case IS_COMPOSED: texto = _textos.get("isComposed"); break;//	"Es un atributo compuesto"
		case NO_COMPONENTS: texto = _textos.get("noComponents"); break;//	"No tiene componentes"
		case COMPONENTS: texto = _textos.get("components"); break;//	"Componentes"
		case IS_WEAK_ENT: texto = _textos.get("isWeakEnt"); break;//	"Es una entidad debil"
		case ADD_ENTITY: texto = _textos.get("addEntity"); break;//"Insertar una nueva entidad"
		case ADD_RELATION: texto = _textos.get("addRelation"); break;//"Insertar una nueva relación"
		case ADD_ISARELATION: texto = _textos.get("addIsaRelation"); break;//"Insertar una nueva relación IsA"
		case ADD_DOMAIN: texto = _textos.get("addDomain"); break;//"Crear un nuevo dominio"
		case RENAME_ENTITY: texto = _textos.get("renameEntity"); break;//"Renombrar la entidad"
		case ADD_ATTRIBUTE: texto = _textos.get("addAttribute"); break;//"Añadir un nuevo atributo"
		case DELETE_ENT: texto = _textos.get("deleteEnt"); break;//"Eliminar la entidad"
		case RENAME_ATTRIB: texto = _textos.get("renameAttrib"); break;//"Renombrar el atributo"
		case EDIT_DOMAIN: texto = _textos.get("editDomain"); break;//"Editar el dominio"
		case ADD_SUBATTRIBUTE: texto = _textos.get("addSubAttribute"); break;//"Añadir un subatributo"
		case NOT_NULL: texto = _textos.get("notNull"); break;//"Not Null"
		case UNIQUE: texto = _textos.get("Unique"); break;//"Unique"
		case IS_PRIMARY_KEY: texto = _textos.get("isPrimaryKey"); break;//"Es clave primaria de la entidad"
		case DELETE_ATTRIB: texto = _textos.get("deleteAttrib"); break;//"Eliminar el atributo"
		case SET_PARENT_ENT: texto = _textos.get("setParentEnt"); break;//"Establecer entidad padre"
		case REMOVE_PARENT_ENT: texto = _textos.get("removeParentEnt"); break;//"Quitar entidad padre"
		case ADD_CHILD_ENT: texto = _textos.get("addChilEnt"); break;//"Añadir entidad hija"
		case REMOVE_CHILD_ENT: texto = _textos.get("removeChildEnt"); break;//	"Quitar entidad hija"
		case DELETE_REL: texto = _textos.get("deleteRel"); break;//"Eliminar la relación"
		case RENAME_RELATION: texto = _textos.get("renameRelation"); break;//	"Renombrar la relación"
		case ADD_ENT: texto = _textos.get("addEnt"); break;//	"Añadir una entidad"
		case REMOVE_ENTITY: texto = _textos.get("removeEntity"); break;//	"Quitar una entidad"
		case EDIT_CARD_ROL: texto = _textos.get("editCardRol"); break;//	"Editar la cardinalidad o el rol de una entidad"
		
		// GUIPrincipal + utilidades.AccionMenu 
		case DOM_MENU_RENAME: texto = _textos.get("domMenuRename"); break;//"Renombrar"
		case DOM_MENU_DELETE: texto = _textos.get("domMenuDelete"); break;//"Eliminar"
		case DOM_MENU_MODIFY: texto = _textos.get("domMenuModify"); break;//"Modificar dominio"
		case DOM_MENU_ADD: texto = _textos.get("domMenuAdd"); break;//"Añadir nuevo dominio"
		case DOM_MENU_IN_ORDER: texto = _textos.get("domMenuRenameInOrder"); break;//"Ordenar valores"
		case DOM_TREE_CREATED_DOMS: texto = _textos.get("domTreeCreatedDoms"); break;//"Dominios creados"
		case DOM_TREE_TYPE: texto = _textos.get("domTreeTypes"); break;//"tipo base"
		case DOM_TREE_VALUES: texto = _textos.get("domTreeValues"); break;//"Valores"
		
		case DB_CASE_TOOL: texto = _textos.get("dbCaseTool"); break;
		case TOOL_FOR_DESING: texto = _textos.get("toolForDesing"); break;
		case SS_II: texto = _textos.get("ssii"); break;
		case COLLEGE: texto = _textos.get("college"); break;
		case UNIVERSITY: texto = _textos.get("university"); break;
		case DIRECTOR: texto = _textos.get("director"); break;
		case TEACHER_NAME: texto = _textos.get("teacherName"); break;
		case AUTHORS: texto = _textos.get("authors"); break;
		case AUTHOR1: texto = _textos.get("author1"); break;
		case AUTHOR2: texto = _textos.get("author2"); break;
		case AUTHOR3: texto = _textos.get("author3"); break;				
		case BASED: texto = _textos.get("based"); break;
		case CONTACT: texto = _textos.get("contact"); break;
		case DBCASE_LABEL: texto = _textos.get("dbcaseLabel"); break;
		
		case SYSTEM: texto = _textos.get("system"); break;//"Sistema"
		case EXIT_MINCASE: texto = _textos.get("exitMincase"); break;//"Salir"
		case OPTIONS: texto = _textos.get("options"); break;//"Opciones"
		case CURRENT_DBMS: texto = _textos.get("currentDBMS"); break;//"Gestor de Base de Datos actual"
		case SELECT_LANGUAGE: texto = _textos.get("selectLanguage"); break; // "Lenguaje"
		case EXPORT_DIAGRAM: texto = _textos.get("exportDiagram"); break;//"Exportar el diagrama E/R como archivo de imagen"
		case PRINT_DIAGRAM: texto = _textos.get("printDiagram"); break;//"Imprimir el diagrama E/R"
		case HELP: texto = _textos.get("help"); break;//"Ayuda"
		case CONTENTS: texto = _textos.get("contents"); break;//"Contenidos"
		case ABOUT: texto = _textos.get("about"); break;//"Acerca de DBDT"
		case ER_MODEL: texto = _textos.get("erModel"); break;//"Diagrama Entidad Relación"
		case CONC_MODEL: texto = _textos.get("concModel"); break;//"Esquema Conceptual"
		case LOGIC_MODEL: texto = _textos.get("logicModel"); break;//"Esquema Logico"
		case PHYS_MODEL: texto = _textos.get("physicModel"); break;//"Esquema Físico"
		case INF_PANEL: texto = _textos.get("infPanel"); break;//"Panel de información"
		case DOM_PANEL: texto = _textos.get("domPanel"); break;//"Panel dominios"
		case GRAPH_PANEL: texto = _textos.get("graphPanel"); break;//"Panel grafo"
		case EVENT_PANEL: texto = _textos.get("eventPanel"); break;//"Panel de sucesos"
		case SHOW_EVENTS_PANEL: texto = _textos.get("showEventsPanel"); break;//"Panel de sucesos"
		case CODE_GENERATION: texto = _textos.get("codeGeneration"); break;//"Generación de código"
		case CLEAN: texto = _textos.get("clean"); break;//"Limpiar el area de texto"
		case VALIDATE_DESIGN: texto = _textos.get("validateDesign"); break;//"Validar el diseño realizado"
		case GENERATE_REL_MODEL: texto = _textos.get("generateRelModel"); break;//"Generar el Modelo Relacional del diseño realizado"
		case GENERATE_SQL_CODE: texto = _textos.get("generateSQLmodel"); break;//"Generar el código SQL del diseño realizado"
		case EXPORT_SQL_FILE: texto = _textos.get("exportSQLfile"); break;//"Exportar el Script SQL a un archivo"
		case EXECUTE_SCRIPT: texto = _textos.get("executeScript"); break;//"Ejecutar el script SQL en un DBMS"
		case MUST_GENERATE_SCRIPT_EX: texto = _textos.get("mustGenerateScriptEx"); break;//"Debe generar el Script SQL antes de ejecutarlo."
		case JPEG_FILES: texto = _textos.get("jpegFiles"); break;//"Ficheros JPEG"
		case OK_EXPORT: texto = _textos.get("okExport"); break;//"Se ha exportado satisfactoriamente el diagrama al fichero"
		
		case NO_CONECTOR: texto = _textos.get("noConector"); break;//"Error: No se encuentra el conector JDBC"
		case TABLE_TITLE: texto = _textos.get("tableTitle"); break;
		case TEST_DATA: texto = _textos.get("testData"); break;
		case CLEAN_FIELDS: texto = _textos.get("cleanFields"); break;
		case HINT: texto = _textos.get("hint"); break;
		case EXPLORE: texto = _textos.get("explore"); break;
		case EXISTING_CONN: texto = _textos.get("existingConns"); break;
		case CHOOSE_CONN: texto = _textos.get("chooseConn"); break;
		
		case RESTRICTIONS: texto = _textos.get("restrictions"); break;
		case ADD_RESTRICTIONS: texto = _textos.get("addRestrictions"); break;
		case TABLE_UNIQUE: texto = _textos.get("tableUnique"); break;
		case ADD_UNIQUE: texto = _textos.get("addUnique"); break;
		case UNIQUE_ERROR: texto = _textos.get("uniqueError"); break;
		case ERROR_TABLE: texto = _textos.get("errorTable"); break;
		case THEME: texto = _textos.get("theme"); break;
		case GENERATE: texto = _textos.get("generate"); break;
		case ELEMENTS: texto = _textos.get("elements"); break;
		case VIEW: texto = _textos.get("view"); break;
		case TABLE: texto = _textos.get("table"); break;
		case VOLUME: texto = _textos.get("volume"); break;
		case FREQ: texto = _textos.get("freq"); break;
		case CANDIDATE_KEYS: texto = _textos.get("candidateKeys"); break;
		case TABLE_CONSTR: texto = _textos.get("tableconstraints"); break;
		case RELATIONS: texto = _textos.get("relations"); break;
		case RIC: texto = _textos.get("ric"); break;
		case LOST_CONSTR: texto = _textos.get("lostConstr"); break;
		case THE_ATRIBUTE: texto = _textos.get("theattribute"); break;
		case THE_RELATION: texto = _textos.get("therelation"); break;
		case THE_ENTITY: texto = _textos.get("theentity"); break;
		case THE_DOMAIN: texto = _textos.get("thedomain"); break;
		case EMPTY_DIAGRAM: texto = _textos.get("emptyDiagram"); break;
		case SCRIPT_GENERATED: texto = _textos.get("scriptGenerated"); break;
		case SYNTAX: texto = _textos.get("syntax"); break;
		case BASED1: texto = _textos.get("based1"); break;
		case BASED1A1: texto = _textos.get("based1a1"); break;
		case BASED1A2: texto = _textos.get("based1a2"); break;
		case BASED1A3: texto = _textos.get("based1a3"); break;
		case BASED2: texto = _textos.get("based2"); break;
		case BASED2A1: texto = _textos.get("based2a1"); break;
		case BASED2A2: texto = _textos.get("based2a2"); break;
		case BASED2A3: texto = _textos.get("based2a3"); break;
		case BASED2P: texto = _textos.get("based2p"); break;
		case AUTHOR: texto = _textos.get("author"); break;
		case TABLES: texto = _textos.get("tables"); break;
		case NULLATTR: texto = _textos.get("nullableAttr"); break;
		case PART_TOTAL: texto = _textos.get("partTotal"); break;
		case CARDMINDE: texto = _textos.get("cardMinde"); break;
		case YCARDMINDE: texto = _textos.get("yCardMinde"); break;
		case CARDMAXDE: texto = _textos.get("cardMaxde"); break;
		case YCARDMAXDE: texto = _textos.get("yCardMaxde"); break;
		case YMAXDE: texto = _textos.get("yMaxde"); break;
		case OF: texto = _textos.get("of"); break;
		case FOR: texto = _textos.get("for"); break;
		default: texto = Lenguaje.notExistingMessage;
		}
		return corrigeCaracteres(texto);
	}
	
	private static String corrigeCaracteres(String texto){
		try{
			if(texto!=null){
				texto=texto.replaceAll("Ã³","ó");
				texto=texto.replaceAll("Ã“","Ó");
				texto=texto.replaceAll("Ã±","ñ");
				texto=texto.replaceAll("Ã‘","Ñ");
				texto=texto.replaceAll("Ã¡","á");				
				texto=texto.replaceAll("Ã©","é");
				texto=texto.replaceAll("Ã‰","É");		
				texto=texto.replaceAll("Ãº","ú");				
				texto=texto.replaceAll("Â¿","¿");
				texto=texto.replaceAll("Ã­","í");
				texto=texto.replaceAll("Â´","´");
			}
		}
	    catch(Exception e){}		
		return texto;
	}
}
