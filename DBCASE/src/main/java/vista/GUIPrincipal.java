package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TipoDominio;
import modelo.transfers.Transfer;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferConexion;
import modelo.transfers.TransferDominio;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import vista.componentes.ArbolDominiosRender;
import vista.componentes.ArbolElementosRender;
import vista.componentes.MyComboBoxRenderer;
import vista.componentes.MyMenu;
import vista.componentes.GUIPanels.ReportPanel;
import vista.componentes.GUIPanels.TablaVolumenes;
import vista.componentes.GUIPanels.addTransfersPanel;
import vista.diagrama.PanelGrafo;
import vista.diagrama.PanelThumbnail;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;
import vista.tema.Theme;

@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class GUIPrincipal extends JFrame implements WindowListener, KeyListener{
	private Controlador c;
	private TransferConexion conexionActual = null;
	private boolean scriptGeneradoCorrectamente = false;
	private Vector<TransferConexion> listaConexiones;
	private Vector<TransferEntidad> listaEntidades;
	private Vector<TransferAtributo> listaAtributos;
	private Vector<TransferRelacion> listaRelaciones;
	private Vector<TransferDominio> listaDominios;
	private TablaVolumenes tablaVolumenes;
	private JPanel panelTablas;
	private JButton botonLimpiarPantalla;
	private PanelThumbnail panelGrafo;
	private JTree arbolElems;
	private JTree arbolDom;
	private JScrollPane panelArbolElems;
	private JPanel panelInfo;
	private PanelGrafo panelDiseno;
	private JScrollPane panelArbolDom;
	private JPanel panelDom;
	private JComboBox cboSeleccionDBMS;
	private JButton botonExportarArchivo;
	private JButton botonScriptSQL;
	private JButton botonModeloRelacional;
	private JButton botonValidar;
	private JButton botonEjecutarEnDBMS;
	private JSplitPane splitDisenoInfo;
	private JPanel panelGeneracion;
	private JPanel panelDiagrama;
	private MyMenu barraDeMenus;
	private static JPopupMenu popup;
	private Theme theme = Theme.getInstancia();
	private JScrollPane scrollPanelTablas;
	private JTabbedPane infoTabPane;
	protected ReportPanel codigoText;
	protected ReportPanel modeloText;
	private Perspectiva dealer;
	
	/*
	 * Activar y desctivar la ventana
	 */
	public void setActiva(int modo){
		//Miniatura de aplicacion para macOS
		if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0)
			com.apple.eawt.Application.getApplication().setDockIconImage(new ImageIcon(getClass().getResource( "/vista/imagenes/DBCase_logo.png" )).getImage());
		Toolkit tk = Toolkit.getDefaultToolkit();
		int xSize = ((int) tk.getScreenSize().getWidth());
		int ySize = ((int) tk.getScreenSize().getHeight());
		
		this.panelDiagrama = new JPanel();
		this.panelGeneracion = new JPanel();
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ObtenDBMSDisponibles, null);
		conexionActual = listaConexiones.get(0);
		
		setLookAndFeel();
		initComponents();
		changeFont(this,theme.font());
		dealer = new Perspectiva(this.getContentPane(), panelDiagrama, panelGeneracion, infoTabPane);
		setModoVista(modo);
		
    	pack();
    	setLocationRelativeTo(null);
    	setPreferredSize(new Dimension(xSize, ySize));
    	setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
    	setVisible(true);
	}
	
	public void reiniciar(){
		int modo = dealer.getPanelsMode();
		this.panelDiagrama = new JPanel();
		this.panelGeneracion = new JPanel();
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ObtenDBMSDisponibles, null);
		conexionActual = listaConexiones.get(0);
		setLookAndFeel();
		initComponents();
		dealer = new Perspectiva(this.getContentPane(), panelDiagrama, panelGeneracion, infoTabPane);
		setModoVista(modo);
    	loadInfo();
    	c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_IniciaFrames, null);
    	c.getTheGUIAnadirAtributo().setListaDominios(getListaDominios());
	}
	
	public static void changeFont (Component component, Font font){
	    component.setFont(font);
	    if (component instanceof Container)
	        for(Component child : ((Container) component).getComponents())
	        	changeFont (child, font);
	}
	
	private void initComponents() {
		try{
			c.getPath();
			this.setTitle(c.getTitle());
			this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			initMenu();
			initDiagrama();
			initCodes();
			this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
			this.addWindowListener(this);
	        this.addKeyListener(this);
		}catch(Exception e) {e.printStackTrace();}
		popup = new JPopupMenu();
	}
	
	private void initMenu() {
		barraDeMenus = new MyMenu(c);
		setJMenuBar(barraDeMenus);
	}//initMenu
	
	private void initDiagrama() {
		BorderLayout panelDiagramaLayout = new BorderLayout();
		panelDiagrama.setLayout(panelDiagramaLayout);
		
		splitDisenoInfo = new JSplitPane();
		panelDiagrama.add(splitDisenoInfo, BorderLayout.CENTER);
		splitDisenoInfo.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitDisenoInfo.setOneTouchExpandable(false);
		
		infoTabPane = new JTabbedPane();
		infoTabPane.setFont(theme.font());
		JSplitPane splitTabMapa = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitTabMapa.add(infoTabPane, JSplitPane.RIGHT);
		infoTabPane.setFocusable(false);
	
		// Actualizacion de listas y creacion del grafo
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ActualizameLaListaDeEntidades, null);
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ActualizameLaListaDeAtributos, null);
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ActualizameLaListaDeRelaciones, null);
		panelDiseno = new PanelGrafo(listaEntidades,listaAtributos,listaRelaciones);
		panelDiseno.setControlador(this.getControlador());

		panelInfo = new JPanel();
		panelInfo.setLayout(new BorderLayout());
		panelInfo.addMouseListener(mls);
		infoTabPane.addTab(Lenguaje.text(Lenguaje.ELEMENTS), null, panelInfo ,null);
		
		panelArbolElems = new JScrollPane();
		panelArbolElems.setBackground(theme.background());
		panelInfo.add(panelArbolElems, BorderLayout.CENTER);
		panelArbolElems.setBorder(null);
		panelArbolElems.setVisible(false);

		panelDom = new JPanel();
		panelDom.setLayout(new BorderLayout());
		panelDom.addMouseListener(ml);
		infoTabPane.addTab(Lenguaje.text(Lenguaje.DOM_PANEL), null, panelDom ,null);
		
		panelArbolDom = new JScrollPane();
		panelArbolDom.setBackground(theme.background());
		panelDom.add(panelArbolDom, BorderLayout.CENTER);
		panelArbolDom.setBorder(null);
		panelArbolDom.setVisible(false);
		
		JButton nuevoDom = new JButton(Lenguaje.text(Lenguaje.NEW));
		nuevoDom.setFont(theme.font());
		JPanel panelBoton = new JPanel();
		panelBoton.setBackground(theme.background());
		nuevoDom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_CrearDominio, 0);
			}
		});		
		nuevoDom.setFocusable(false);
		panelBoton.add(nuevoDom);
		panelDom.add(panelBoton, BorderLayout.NORTH);
		this.actualizaArbolDominio(null);
		
		panelTablas = new JPanel();
		panelTablas.setLayout(new BorderLayout());
		panelTablas.setBackground(theme.background());
		tablaVolumenes = new TablaVolumenes();
		tablaVolumenes.setFont(theme.font());
		tablaVolumenes.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				c.mensajeDesde_PanelDiseno(TC.PanelDiseno_ActualizarDatosEnTablaDeVolumenes, e);
		}});
		tablaVolumenes.setBackground(theme.background());
		scrollPanelTablas = new JScrollPane(tablaVolumenes);
		scrollPanelTablas.setBackground(theme.background());
		panelTablas.add(scrollPanelTablas);
		/*
		 * FUNCIONALIDAD POR DESARROLLAR
		 * infoTabPane.addTab(Lenguaje.text(Lenguaje.ANALYSIS), null, panelTablas ,null);
		*/
		panelGrafo = new PanelThumbnail(panelDiseno);
		splitTabMapa.add(panelGrafo, JSplitPane.LEFT);
		JPanel diagrama = new JPanel();
		diagrama.setLayout(new BorderLayout());
		diagrama.add(panelDiseno);
		
		JPanel tituloDiseno = new JPanel();
		tituloDiseno.setLayout(new BorderLayout());
		JLabel title = new JLabel("<html><span style='font-size:20px'>"+Lenguaje.text(Lenguaje.CONC_MODEL)+"</span></html>");
		title.setHorizontalAlignment(JTextField.CENTER);
		title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
		tituloDiseno.add(title, BorderLayout.CENTER);
		Vector<Transfer> listaTransfers = new Vector<Transfer>();
		listaTransfers.addAll(listaEntidades);
		listaTransfers.addAll(listaRelaciones);
		addTransfersPanel botonesAnadir = new addTransfersPanel(c, listaTransfers);
		/*Listener del tamano del panel*/
		panelDiseno.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentResized(ComponentEvent e) {
				botonesAnadir.setDiagramWidth(e.getComponent().getSize().width);
			}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
		});
		
		splitDisenoInfo.setBorder(null);
		splitDisenoInfo.setResizeWeight(0.2);
		diagrama.add(botonesAnadir, BorderLayout.WEST);
		diagrama.add(tituloDiseno, BorderLayout.NORTH);
		splitDisenoInfo.add(diagrama, JSplitPane.LEFT);
		splitDisenoInfo.add(splitTabMapa,JSplitPane.RIGHT);
		splitDisenoInfo.setResizeWeight(1);
	}
	
	private void initCodes() {
		modeloText = new ReportPanel();
		BorderLayout panelGeneracionLayout = new BorderLayout();
		panelGeneracion.setLayout(panelGeneracionLayout);
		JSplitPane codesSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		codesSplit.setBorder(null);
		codesSplit.setEnabled(true);
		codesSplit.setResizeWeight(0.5);
		panelGeneracion.add(codesSplit, BorderLayout.CENTER);
		JPanel modeloPanel = new JPanel();
		modeloPanel.setBackground(theme.background());
		modeloPanel.setLayout(new BorderLayout());
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.X_AXIS));
		JLabel text = new JLabel("<html><span style='font-size:20px'>"+Lenguaje.text(Lenguaje.LOGIC_MODEL)+"</span></html>");
		JButton generaModelo = new JButton(Lenguaje.text(Lenguaje.GENERATE));
		generaModelo.setFont(theme.font());
		generaModelo.setToolTipText("Genera el modelo relacional a partir del diagrama entidad relacion.");
		generaModelo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				botonModeloRelacionalActionPerformed(evt);
			}
		});
		JButton exportarModelo = new JButton(Lenguaje.text(Lenguaje.SAVE_AS));
		exportarModelo.setFont(theme.font());
		exportarModelo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				botonExportarArchivoActionPerformed(evt,false);
			}
		});
		textPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
		textPanel.add(text);
		textPanel.add(generaModelo);
		textPanel.add(exportarModelo);
		modeloPanel.add(textPanel, BorderLayout.NORTH);		
		modeloPanel.add(modeloText.getPanel(), BorderLayout.CENTER);
		
		/***********************/
		
		JPanel codePanel = new JPanel();
		codePanel.setBackground(theme.background());
		codePanel.setLayout(new BorderLayout());
		
		cboSeleccionDBMS = new JComboBox();
		for (int i=0; i < listaConexiones.size(); i++)
			cboSeleccionDBMS.insertItemAt(listaConexiones.get(i).getRuta(),listaConexiones.get(i).getTipoConexion());
		
		cboSeleccionDBMS.setSelectedIndex(0);
		cboSeleccionDBMS.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox cbo = (JComboBox) e.getSource();
				cambiarConexion((String)cbo.getSelectedItem());// Cambiar la conexionActual
			}
		});
		cboSeleccionDBMS.setMaximumSize(new Dimension(500,40));
		cboSeleccionDBMS.setFont(theme.font());
		cboSeleccionDBMS.setRenderer(new MyComboBoxRenderer());
		JPanel textPanel2 = new JPanel();
		textPanel2.setLayout(new BoxLayout(textPanel2,BoxLayout.X_AXIS));
		JLabel text2 = new JLabel("<html><span style='font-size:20px'>"+Lenguaje.text(Lenguaje.PHYS_MODEL)+"</span></html>");
		JButton generaCodigo = new JButton(Lenguaje.text(Lenguaje.GENERATE));
		generaCodigo.setFont(theme.font());
		generaCodigo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				botonScriptSQLActionPerformed(evt);
			}
		});
		JButton exportarCodigo = new JButton(Lenguaje.text(Lenguaje.SAVE_AS));
		exportarCodigo.setFont(theme.font());
		exportarCodigo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				botonExportarArchivoActionPerformed(evt,true);
			}
		});
		JButton ejecutarCodigo = new JButton(Lenguaje.text(Lenguaje.EXECUTE));
		ejecutarCodigo.setFont(theme.font());
		ejecutarCodigo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				botonEjecutarEnDBMSActionPerformed(evt);
			}
		});
		JPanel accionesCodigo = new JPanel();
		accionesCodigo.setLayout(new BoxLayout(accionesCodigo,BoxLayout.Y_AXIS));
		textPanel2.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
		textPanel2.add(text2);
		accionesCodigo.add(cboSeleccionDBMS);
		JPanel botonesCodigo = new JPanel();
		botonesCodigo.setLayout(new BoxLayout(botonesCodigo,BoxLayout.X_AXIS));
		accionesCodigo.add(botonesCodigo);
		botonesCodigo.add(generaCodigo);
		botonesCodigo.add(exportarCodigo);
		botonesCodigo.add(ejecutarCodigo);
		textPanel2.add(accionesCodigo);
		codePanel.add(textPanel2, BorderLayout.NORTH);
		
		codesSplit.add(modeloPanel,JSplitPane.TOP);
		codesSplit.add(codePanel,JSplitPane.BOTTOM);
				
		codigoText = new ReportPanel();

		codePanel.add(codigoText.getPanel(), BorderLayout.CENTER);
	}
	
	/*
	 * Oyentes de la toolbar de generacion
	 */
	private void botonModeloRelacionalActionPerformed(ActionEvent evt) {
		new Thread(new Runnable(){
			public void run() {
				c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_BotonGenerarModeloRelacional, null);
				modeloText.goToTop();
			}
		}).start();
	}

	private void botonScriptSQLActionPerformed(ActionEvent evt) {
		Thread hilo = new Thread(new Runnable(){
			public void run() {
				conexionActual.setDatabase("");
				c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_BotonGenerarScriptSQL, conexionActual);
				
				// Restaurar el sistema
				conexionActual.setDatabase("");
				codigoText.goToTop();
			}
		});
		hilo.start();
	}
	/*
	 * boolean texto:
	 * false: panel de modelo
	 * true: panel de codigo
	 * */
	private void botonExportarArchivoActionPerformed(ActionEvent evt, boolean sql) {
		Thread hilo = new Thread(new Runnable(){
			public void run() {
				if(sql) c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_BotonGenerarArchivoScriptSQL, codigoText.getText());
				else c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_BotonGenerarArchivoModelo, modeloText.getText());
			}
		});
		hilo.start();
	}

	private void botonEjecutarEnDBMSActionPerformed(ActionEvent evt) {
		Thread hilo = new Thread(new Runnable(){
			public void run() {
				// Comprobar si hay codigo
				if (!scriptGeneradoCorrectamente){
					JOptionPane.showMessageDialog(null,
						Lenguaje.text(Lenguaje.ERROR)+".\n" +
						Lenguaje.text(Lenguaje.MUST_GENERATE_SCRIPT_EX),
						Lenguaje.text(Lenguaje.DBCASE),
						JOptionPane.PLAIN_MESSAGE);
					return;
				}
				
				// Ejecutar en DBMS
				TransferConexion tc = new TransferConexion(
						cboSeleccionDBMS.getSelectedIndex(),
						cboSeleccionDBMS.getSelectedItem().toString());
				
				c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_BotonEjecutarEnDBMS, tc);
			}
		});
		hilo.start();
	}
	
	/*
	 * OYENTES DE TECLADO
	 * */
	public void keyPressed( KeyEvent e ) {} 
	public void keyReleased(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
	
	/*
	 * Mensajes que le manda el controlador a la GUIPrincipal
	 */
	public void mensajesDesde_Controlador(TC mensaje, Object datos){
		switch(mensaje){
		case Controlador_InsertarEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.anadirNodo(te);
			loadInfo();
			break;
		}
		case Controlador_RenombrarEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_DebilitarEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_EliminarEntidad:{
			TransferEntidad te = (TransferEntidad) ((Vector)datos).get(0);
			Vector<TransferRelacion> vectorRelacionesModificadas = (Vector<TransferRelacion>) ((Vector)datos).get(1);
			panelDiseno.eliminaNodo(te);
			int cont = 0;
			while (cont < vectorRelacionesModificadas.size()){
				TransferRelacion tr = vectorRelacionesModificadas.get(cont);
				panelDiseno.ModificaValorInterno(tr);
				cont++;
			}
			loadInfo();
			break;
		}
		case Controlador_AnadirRestriccionEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_QuitarRestriccionEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_setRestriccionesEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_AnadirRestriccionRelacion:{
			TransferRelacion te = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_QuitarRestriccionRelacion:{
			TransferRelacion te = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_setRestriccionesRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;	
		}
		case Controlador_AnadirRestriccionAtributo:{
			TransferAtributo te = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_QuitarRestriccionAtributo:{
			TransferAtributo te = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_setRestriccionesAtributo:{
			TransferAtributo te = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_AnadirUniqueEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_QuitarUniqueEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_setUniquesEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_setUniqueUnitarioEntidad:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;	
		}
		case Controlador_AnadirUniqueRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;	
		}
		case Controlador_QuitarUniqueRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;	
		}
		case Controlador_setUniquesRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;	
		}
		case Controlador_setUniqueUnitarioRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;	
		}
		case Controlador_EliminarAtributo:{
			Vector<Transfer> vectorAtributoYElemMod = (Vector<Transfer>)datos;
			TransferAtributo ta = (TransferAtributo) vectorAtributoYElemMod.get(0);
			Transfer t_elemMod = (Transfer) vectorAtributoYElemMod.get(1);
			panelDiseno.eliminaNodo(ta);
			panelDiseno.ModificaValorInterno(t_elemMod);
			loadInfo();
			break;
		}
		case Controlador_AnadirAtributoAEntidad:{
			Vector<Transfer> vt = (Vector<Transfer>) datos;
			TransferEntidad te = (TransferEntidad) vt.get(0);
			TransferAtributo ta = (TransferAtributo) vt.get(1);
			panelDiseno.anadirNodo(ta);
			panelDiseno.ModificaValorInterno(te);
			break;
		}
		case Controlador_RenombrarAtributo:{
			TransferAtributo ta = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(ta);
			break;
		}
		case Controlador_EditarDominioAtributo:{
			TransferAtributo ta = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(ta);
			break;	
		}
		case Controlador_EditarCompuestoAtributo:{
			TransferAtributo ta = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(ta);
			break;
		}
		case Controlador_EditarMultivaloradoAtributo:{
			TransferAtributo ta = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(ta);
			break;
		}
		case Controlador_EditarNotNullAtributo:{
			TransferAtributo ta = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(ta);
			break;
		}
		case Controlador_EditarUniqueAtributo:{
			TransferAtributo ta = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(ta);
			break;
		}

		case Controlador_AnadirSubAtributoAAtributo:{
			Vector<Transfer> vt = (Vector<Transfer>) datos;
			TransferAtributo ta_padre = (TransferAtributo) vt.get(0);
			TransferAtributo ta_hijo = (TransferAtributo) vt.get(1);
			panelDiseno.anadirNodo(ta_hijo);
			panelDiseno.ModificaValorInterno(ta_padre);
			break;	
		}

		case Controlador_InsertarRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.anadirNodo(tr);
			break;
		}

		case Controlador_MoverEntidad_HECHO:{
			TransferEntidad te = (TransferEntidad) datos;
			panelDiseno.ModificaValorInterno(te);
			break;
		}
		case Controlador_MoverAtributo_HECHO:{
			TransferAtributo ta = (TransferAtributo) datos;
			panelDiseno.ModificaValorInterno(ta);
			break;
		}
		case Controlador_MoverRelacion_HECHO:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_RenombrarRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;	
		}
		case Controlador_DebilitarRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_EliminarRelacion:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			loadInfo();
			break;	
		}
		case Controlador_EditarClavePrimariaAtributo:{
			Vector<Transfer> vt = (Vector<Transfer>) datos;
			TransferAtributo ta = (TransferAtributo) vt.get(0);
			TransferEntidad te = (TransferEntidad) vt.get(1);
			panelDiseno.ModificaValorInterno(ta);
			panelDiseno.ModificaValorInterno(te);
			break;
		}
		case Controlador_EstablecerEntidadPadre:{
			Vector<Transfer> vt = (Vector<Transfer>) datos;
			TransferRelacion tr = (TransferRelacion) vt.get(0);
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_QuitarEntidadPadre:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_AnadirEntidadHija:{
			Vector<Transfer> vt = (Vector<Transfer>) datos;
			TransferRelacion tr = (TransferRelacion) vt.get(0);
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_QuitarEntidadHija:{
			Vector<Transfer> vt = (Vector<Transfer>) datos;
			TransferRelacion tr = (TransferRelacion) vt.get(0);
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_EliminarRelacionIsA:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.eliminaNodo(tr);
			loadInfo();
			break;
		}
		case Controlador_EliminarRelacionNormal:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.eliminaNodo(tr);
			loadInfo();
			break;
		}
		case Controlador_InsertarRelacionIsA:{
			TransferRelacion tr = (TransferRelacion) datos;
			panelDiseno.anadirNodo(tr);
			break;
		}
		case Controlador_AnadirEntidadARelacion:{
			Vector v = (Vector) datos;
			TransferRelacion tr = (TransferRelacion) v.get(0);
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_QuitarEntidadARelacion:{
			Vector v = (Vector) datos;
			TransferRelacion tr = (TransferRelacion) v.get(0);
			panelDiseno.ModificaValorInterno(tr);
			break; 
		}
		case Controlador_EditarCardinalidadEntidad:{
			Vector v = (Vector) datos;
			TransferRelacion tr = (TransferRelacion) v.get(0);
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_CardinalidadUnoUno:{
			Vector v = (Vector) datos;
			panelDiseno.ModificaValorInterno1a1(v);
			break;
		}
		
		case Controlador_AnadirAtributoARelacion:{
			Vector<Transfer> v = (Vector<Transfer>) datos;
			TransferRelacion tr = (TransferRelacion) v.get(0);
			TransferAtributo ta = (TransferAtributo) v.get(1);
			panelDiseno.anadirNodo(ta);
			panelDiseno.ModificaValorInterno(tr);
			break;
		}
		case Controlador_MostrarDatosEnPanelDeInformacion:{
			this.panelArbolElems.setVisible(true);
			this.arbolElems = (JTree) datos;
			this.arbolElems.addMouseListener(mls);
		    this.arbolElems.setCellRenderer(new ArbolElementosRender());
			this.arbolElems.setFont(theme.font());
			this.arbolElems.setBackground(theme.background());
			this.panelArbolElems.setViewportView(arbolElems);
			this.repaint();
			break;
		}
		case Controlador_MostrarDatosEnTablaDeVolumenes:{
			this.tablaVolumenes.refresh((String[][]) datos);
			break;
		}
		case Controlador_ActualizarDatosEnTablaDeVolumenes:{
			this.panelDiseno.refreshTables((TableModelEvent) datos);
			break;
		}
		case Controlador_LimpiarPanelDominio:{
			if (this.arbolDom != null) this.panelArbolDom.setVisible(false);
			break;
		}
		case Controlador_MostrarDatosEnPanelDominio:{
			this.panelArbolDom.setVisible(true);
			this.arbolDom = (JTree) datos;
			this.arbolDom.setFont(theme.font());
			this.arbolDom.setBackground(theme.background());
			this.panelArbolDom.setViewportView(arbolDom);
			this.repaint();
			break;
		}
		case Controlador_InsertarDominio:{
			TransferDominio td = (TransferDominio) datos;
			String nombre = td.getNombre();
			this.actualizaArbolDominio(nombre);
			break;
		}
		default: break;
		}
	}

	/*
	 * Getters y Setters
	 */
	public Controlador getControlador() {
		return c;
	}

	public void setControlador(Controlador controlador) {
		this.c = controlador;
	}
	
	public void setScriptGeneradoCorrectamente(boolean valor){
		scriptGeneradoCorrectamente = valor;
	}

	public Vector getListaConexiones() {
		return listaConexiones;
	}
	
	public void setListaConexiones(Vector<TransferConexion> listaConexiones) {
		this.listaConexiones = listaConexiones;
	}

	public Vector getListaEntidades() {
		return listaEntidades;
	}

	public void setListaEntidades(Vector<TransferEntidad> listaEntidades) {
		this.listaEntidades = listaEntidades;
	}
	
	public Vector getListaAtributos() {
		return listaAtributos;
	}

	public void setListaAtributos(Vector<TransferAtributo> listaAtributos) {
		this.listaAtributos = listaAtributos;
	}

	public Vector getListaRelaciones() {
		return listaRelaciones;
	}

	public void setListaRelaciones(Vector<TransferRelacion> listaRelaciones) {
		this.listaRelaciones = listaRelaciones;
	}

	public Vector getListaDominios() {
		return listaDominios;
	}

	public TransferConexion getConexionActual(){
		return conexionActual;
	}
	
	public void setListaDominios(Vector<TransferDominio> listaDominios) {
		this.listaDominios = listaDominios;
	}
	
	public void escribeEnModelo(String mensaje){
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run() {
					modeloText.setText(mensaje);
				}});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	
	public void escribeEnCodigo(String mensaje){
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {codigoText.setText(mensaje);}
		});} 
		catch (InterruptedException e) {e.printStackTrace();} 
		catch (InvocationTargetException e) {e.printStackTrace();}
	}
	
	/*
	 * ARBOL DOMINIOS
	 */
	public void actualizaArbolDominio(String expandir){
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ActualizameLaListaDeDominios, null);
		this.panelArbolDom.setVisible(true);
		this.arbolDom = generaArbolDominio(this.listaDominios, expandir);
		this.panelArbolDom.setViewportView(arbolDom);
		this.repaint();
	}
	
	private JTree generaArbolDominio(Vector<TransferDominio> listaDominios, String expandir){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(Lenguaje.text(Lenguaje.DOM_TREE_CREATED_DOMS));
		for (Iterator<TransferDominio> it = listaDominios.iterator(); it.hasNext();){
			TransferDominio td = it.next();
			//Nombre
			DefaultMutableTreeNode nodoNombre = new DefaultMutableTreeNode(td.getNombre());
			root.add(nodoNombre);
			//TipoBase
			if(!esDominioDefecto(nodoNombre.toString()))
				nodoNombre.add(new DefaultMutableTreeNode(Lenguaje.text(Lenguaje.DOM_TREE_TYPE)+" \""+td.getTipoBase()+"\""));
			else nodoNombre.setAllowsChildren(false);
			// Valores
			if (td.getListaValores()!=null && td.getListaValores().size()>0){
				DefaultMutableTreeNode nodo_valores = new DefaultMutableTreeNode(Lenguaje.text(Lenguaje.DOM_TREE_VALUES));
				nodoNombre.add(nodo_valores);
				Vector lista = td.getListaValores();
				for (int cont=0; cont<lista.size(); cont++ )
					nodo_valores.add(new DefaultMutableTreeNode(lista.get(cont)));
			}
		}
		
		JTree arbolDom = new JTree(root);
		arbolDom.setRootVisible(false);
		arbolDom.setShowsRootHandles(true);
		arbolDom.setToggleClickCount(1);
		arbolDom.addMouseListener(ml);
		arbolDom.setBackground(theme.background());
	    arbolDom.setCellRenderer(new ArbolDominiosRender());
		// Expandimos todas las ramas
		 for(int cont=0; cont<arbolDom.getRowCount(); cont++){
			 try{ 
				 if (arbolDom.getPathForRow(cont).getLastPathComponent().toString().contains(Lenguaje.text(Lenguaje.DOMAIN)+" "+'"'+expandir+'"')){ 
					 arbolDom.expandRow(cont);
					 }
				 else if(arbolDom.getPathForRow(cont).getParentPath().getLastPathComponent().toString().contains(Lenguaje.text(Lenguaje.DOMAIN)+" "+'"'+expandir+'"')){
					 arbolDom.expandRow(cont);
				 }
			 }catch(Exception e){}
		 }
		return arbolDom;
	}
	
	/*
	 * LISTENER DEL ARBOL DOMINIOS
	 * */
	MouseListener ml = new MouseAdapter() {
	     @Override
		public void mousePressed(MouseEvent e) {
	         int selRow = arbolDom.getRowForLocation(e.getX(), e.getY());
	         TreePath selPath = arbolDom.getPathForLocation(e.getX(), e.getY());
	         if(selRow != -1) {
	        	 if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
	        	     muestraMenu(e, selPath);
	             }
	        	 else{
		        	 getPopUp().setVisible(false);
		         }
	         }
	         else{
	        	 if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
	        		popup.removeAll();
					JMenuItem m4 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_ADD));
					m4.setFont(theme.font());
					m4.setForeground(theme.fontColor());
					m4.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							getPopUp().setVisible(false);
							getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_CrearDominio, null);
							actualizaArbolDominio(null);
						}
					});
					popup.add(m4);
		        	popup.setLocation(e.getLocationOnScreen());
		        	getPopUp().setVisible(true);
	             }
	        	 else{
		        	 getPopUp().setVisible(false);
		         }
	         }
	     }
	     
		 private void muestraMenu(MouseEvent e, TreePath selPath) {
			popup.removeAll();
			if(selPath.getPathCount()==2){//Nodo principal de un dominio
				String nombre= selPath.getLastPathComponent().toString();
				if(!esDominioDefecto(nombre)) {
					//buscamos el transfer
					
					nombre= nombre.replace('"', '*');
					nombre= nombre.replaceAll(Lenguaje.text(Lenguaje.DOMAIN)+" ", "");
					nombre= nombre.replace("*", "");
					
					int index=-1;
					for (int i=0; i<listaDominios.size();i++)
						if(listaDominios.get(i).getNombre().equals(nombre)) index=i;
	
					TransferDominio dominio = listaDominios.get(index);
					JMenuItem m1 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_RENAME));
					m1.setFont(theme.font());
					m1.setForeground(theme.fontColor());
					m1.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							TransferDominio clon_dominio = dominio.clonar();
							getPopUp().setVisible(false);
							getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_RenombrarDominio,clon_dominio);
							actualizaArbolDominio(clon_dominio.getNombre());
						}
					});
					JMenuItem m2 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_DELETE));
					m2.setFont(theme.font());
					m2.setForeground(theme.fontColor());
					m2.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							TransferDominio clon_dominio = dominio.clonar();
							getPopUp().setVisible(false);
							getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarDominio,clon_dominio);
							actualizaArbolDominio(null);
						}
					});
					JMenuItem m3 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_MODIFY));
					m3.setFont(theme.font());
					m3.setForeground(theme.fontColor());
					m3.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							TransferDominio clon_dominio = dominio.clonar();
							getPopUp().setVisible(false);
							getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_ModificarDominio,clon_dominio);
							actualizaArbolDominio(clon_dominio.getNombre());
						}
					});
					popup.add(m1);
					popup.addSeparator();
					popup.add(m2);
					popup.addSeparator();
					popup.add(m3);
				}
			}
			else if(selPath.getPathCount()==1){//Nodo "Dominios"
				JMenuItem m4 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_ADD));
				m4.setFont(theme.font());
				m4.setForeground(theme.fontColor());
				m4.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getPopUp().setVisible(false);
						getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_CrearDominio, null);
						actualizaArbolDominio(null);
					}
				});
				popup.add(m4);
			}
			else if(selPath.getLastPathComponent().toString().startsWith(Lenguaje.text(Lenguaje.DOM_TREE_TYPE)+" ") && (selPath.getPathCount()==3)){
				//buscamos el transfer
				String nombre= selPath.getParentPath().getLastPathComponent().toString();
				if(!esDominioDefecto(nombre)) {
					nombre= nombre.replace('"', '*');
					nombre= nombre.replaceAll(Lenguaje.text(Lenguaje.DOMAIN)+" ", "");
					nombre= nombre.replace("*", "");
					
					int index=-1;
					for (int i=0; i<listaDominios.size();i++)
						if(listaDominios.get(i).getNombre().equals(nombre)) index=i;
					
					TransferDominio dominio = listaDominios.get(index);
					JMenuItem m5 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_MODIFY));
					m5.setFont(theme.font());
					m5.setForeground(theme.fontColor());
					m5.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							TransferDominio clon_dominio = dominio.clonar();
							getPopUp().setVisible(false);
							getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_ModificarDominio,clon_dominio);
							actualizaArbolDominio(clon_dominio.getNombre());
						}
					});
					popup.add(m5);
				}
			}
			else if(selPath.getLastPathComponent().toString().equals(Lenguaje.text(Lenguaje.DOM_TREE_TYPE)) && (selPath.getPathCount()==3)){
				//buscamos el transfer
				String nombre= selPath.getParentPath().getLastPathComponent().toString();
				nombre= nombre.replace('"', '*');
				nombre= nombre.replaceAll(Lenguaje.text(Lenguaje.DOMAIN)+" ", "");
				nombre= nombre.replace("*", "");
				
				int index=-1;
				for (int i=0; i<listaDominios.size();i++)
					if(listaDominios.get(i).getNombre().equals(nombre)) index=i;
				
				TransferDominio dominio = listaDominios.get(index);
				JMenuItem m6 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_MODIFY));
				m6.setFont(theme.font());
				m6.setForeground(theme.fontColor());
				m6.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TransferDominio clon_dominio = dominio.clonar();
						getPopUp().setVisible(false);
						getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_ModificarDominio,clon_dominio);
						actualizaArbolDominio(clon_dominio.getNombre());
					}
				});
				popup.add(m6);
				popup.addSeparator();
				JMenuItem m8 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_IN_ORDER));
				m8.setFont(theme.font());
				m8.setForeground(theme.fontColor());
				m8.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TransferDominio clon_dominio = dominio.clonar();
						getPopUp().setVisible(false);
						getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_OrdenarValoresDominio,clon_dominio);
						actualizaArbolDominio(clon_dominio.getNombre());
					}
				});
				popup.add(m8);
			}
			else if(selPath.getParentPath().getLastPathComponent().toString().equals(Lenguaje.text(Lenguaje.DOM_TREE_VALUES)) && (selPath.getPathCount()==4)){
				//buscamos el transfer
				String nombre= selPath.getParentPath().getParentPath().getLastPathComponent().toString();
				nombre= nombre.replace('"', '*');
				nombre= nombre.replaceAll(Lenguaje.text(Lenguaje.DOMAIN)+" ", "");
				nombre= nombre.replace("*", "");
				
				int index=-1;
				for (int i=0; i<listaDominios.size();i++)
					if(listaDominios.get(i).getNombre().equals(nombre)) index=i;
				TransferDominio dominio = listaDominios.get(index);
				JMenuItem m7 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_MODIFY));
				m7.setFont(theme.font());
				m7.setForeground(theme.fontColor());
				m7.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TransferDominio clon_dominio = dominio.clonar();
						getPopUp().setVisible(false);
						getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_ModificarDominio,clon_dominio);
						actualizaArbolDominio(clon_dominio.getNombre());
					}
				});
				popup.add(m7);
				popup.addSeparator();
				JMenuItem m8 = new JMenuItem(Lenguaje.text(Lenguaje.DOM_MENU_IN_ORDER));
				m8.setFont(theme.font());
				m8.setForeground(theme.fontColor());
				m8.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TransferDominio clon_dominio = dominio.clonar();
						getPopUp().setVisible(false);
						getControlador().mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_OrdenarValoresDominio,clon_dominio);
						actualizaArbolDominio(clon_dominio.getNombre());
					}
				});
				popup.add(m8);
			}		
			popup.setLocation(e.getLocationOnScreen());
			popup.setVisible(true);
		}
	 };
	 
	 private boolean esDominioDefecto(String name) {
		 for(TipoDominio t:TipoDominio.values())
			 if(t.toString().equals(name)) return true;
		 return false;
	 }
	 
	/*
	* LISTENER DEL ARBOL INFORMACION
	* */
	MouseListener mls = new MouseAdapter() {
	    @Override
		public void mousePressed(MouseEvent e){
	         int selRow = arbolElems.getRowForLocation(e.getX(), e.getY());
	         TreePath selPath = arbolElems.getPathForLocation(e.getX(), e.getY());
	         if(selRow != -1) 
	        	 if (javax.swing.SwingUtilities.isRightMouseButton(e)) muestraMenu(e, selPath);
	        	 else getPopUp().setVisible(false);
	         else getPopUp().setVisible(false);
     }
    
	private void muestraMenu(MouseEvent e, TreePath selPath) {
		popup.removeAll();
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ActualizameLaListaDeEntidades, null);
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ActualizameLaListaDeAtributos, null);
		c.mensajeDesde_GUIPrincipal(TC.GUIPrincipal_ActualizameLaListaDeRelaciones, null);
		
		//if(selPath.getPathCount()==1){//Nodo The entity, the attribute, the relation...
			String nombre= selPath.getPathComponent(0).toString();
			if(nombre.contains(Lenguaje.text(Lenguaje.ENTITY))){
				//buscamos el transfer
				nombre= nombre.replace('"', '*');
				nombre= nombre.replaceAll(Lenguaje.text(Lenguaje.THE_ENTITY)+" ", "");
				nombre= nombre.replace("*", "");
				
				int index=-1;
				
				for (int i=0; i<listaEntidades.size();i++){
					if(listaEntidades.get(i).getNombre().equals(nombre)){
					 index=i;	
					}
					
				}
				final TransferEntidad entidad = listaEntidades.get(index);
				
				// Anadir un atributo a una entidad
				JMenuItem j3 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_ATTRIBUTE));
				j3.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferEntidad clon_entidad = entidad.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirAtributoEntidad,clon_entidad);
					}	
				});
				popup.add(j3);
				popup.add(new JSeparator());
				// Renombrar la entidad
				JMenuItem j1 = new JMenuItem(Lenguaje.text(Lenguaje.RENAME_ENTITY));
				j1.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferEntidad clon_entidad = entidad.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_RenombrarEntidad,clon_entidad);
						
					}	
				});
				popup.add(j1);
				// Eliminar una entidad 
				JMenuItem j4 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE_ENT));
				j4.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferEntidad clon_entidad = entidad.clonar();
							Vector<Object> v = new Vector<Object>();
							v.add(clon_entidad);
							v.add(true);
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarEntidad,v);
						}	
				});
				popup.add(j4);
				popup.add(new JSeparator());
				//Añadir restricciones			
				JMenuItem j5 = new JMenuItem(Lenguaje.text(Lenguaje.RESTRICTIONS));
				j5.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferEntidad clon_entidad = entidad.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirRestriccionAEntidad,clon_entidad);
					}	
				});
				popup.add(j5);
				//Añadir restricciones	Unique		
				JMenuItem j6 = new JMenuItem(Lenguaje.text(Lenguaje.TABLE_UNIQUE));
				j6.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferEntidad clon_entidad = entidad.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_TablaUniqueAEntidad,clon_entidad);
					}	
				});
				popup.add(j6);
			
					
			//ATRIBUTO		
			}else if(nombre.contains(Lenguaje.text(Lenguaje.ATTRIBUTE))){
				//buscamos el transfer
				nombre= nombre.replace('"', '*');
				nombre= nombre.replaceAll(Lenguaje.text(Lenguaje.THE_ATTRIBUTE)+" ", "");
				nombre= nombre.replace("*", "");
				int ind = nombre.indexOf("(", 0);
				nombre = nombre.substring(0,ind-1);
				// y el transfer de la entidad, relacion o atributo compuesto a la que pertenece
				String nombreE = selPath.getPathComponent(0).toString();
				int ind1, ind2;
				ind1= nombreE.indexOf("(",0);
				ind2= nombreE.indexOf(")",0);
				nombreE= nombreE.substring(ind1+1, ind2);
				
				
				int pertenece=-1; //0 entidad, 1 relacion, 2 atributo compuesto.
				if (nombreE.contains("ent: ")){
					pertenece=0;
					nombreE=nombreE.replace("ent: ", "");  /*TRADUCIR*/
					nombreE= nombreE.replace('"', '*');
					nombreE= nombreE.replace("*", "");
				}else if(nombreE.contains("rel: ")){
					pertenece=1;
					nombreE=nombreE.replace("rel: ", "");  /*TRADUCIR*/
					nombreE= nombreE.replace('"', '*');
					nombreE= nombreE.replace("*", "");
				}else if(nombreE.contains("attr: ")){
					pertenece=2;
					nombreE=nombreE.replace("attr: ", "");  /*TRADUCIR*/
				}
				
				int numAtributo=-1;
				int index=-1;
				int idAtributo=-1;
									
				if(pertenece==0){
					for (int i=0; i<listaEntidades.size();i++){
						if(listaEntidades.get(i).getNombre().equals(nombreE)){
							index=i;
						}
					}
					final TransferEntidad entidad = listaEntidades.get(index);
					for (int i=0; i<listaAtributos.size();i++){
						if(listaAtributos.get(i).getNombre().equals(nombre) && 
								entidad.getListaAtributos().contains(Integer.toString((listaAtributos.get(i).getIdAtributo())))){
							numAtributo=i;
							idAtributo=listaAtributos.get(i).getIdAtributo();
						}
					}
				}else if(pertenece==1){
					for (int i=0; i<listaRelaciones.size();i++){
						if(listaRelaciones.get(i).getNombre().equals(nombreE)){
							index=i;
						}
					}
					final TransferRelacion relacion = listaRelaciones.get(index);
					for (int i=0; i<listaAtributos.size();i++){
						if(listaAtributos.get(i).getNombre().equals(nombre) && 
								relacion.getListaAtributos().contains(Integer.toString((listaAtributos.get(i).getIdAtributo())))){
							numAtributo=i;	
						}
					}
				}else if(pertenece==2){
					for (int i=0; i<listaAtributos.size();i++){
						if(listaAtributos.get(i).getIdAtributo() == Integer.parseInt(nombreE)){
							index=i;
						}
					}
					final TransferAtributo atributoC = listaAtributos.get(index);
					for (int i=0; i<listaAtributos.size();i++){
						if(listaAtributos.get(i).getNombre().equals(nombre) && 
								atributoC.getListaComponentes().contains(Integer.toString((listaAtributos.get(i).getIdAtributo())))){
							numAtributo=i;	
						}
					}
				}
				
				final TransferAtributo atributo = listaAtributos.get(numAtributo);
				if (pertenece==0)//si es atributo de entidad miramos si es clave primaria
					for (int j=0; j<listaEntidades.get(index).getListaClavesPrimarias().size();j++){ 
						if(Integer.parseInt(listaEntidades.get(index).getListaClavesPrimarias().get(j).toString())==idAtributo){
							atributo.setClavePrimaria(true);
						}
					}
				// Editar el dominio del atributo
				JMenuItem j2 = new JMenuItem(Lenguaje.text(Lenguaje.EDIT_DOMAIN));
				j2.setFont(theme.font());
				j2.setForeground(theme.fontColor());
				j2.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferAtributo clon_atributo = atributo.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarDominioAtributo,clon_atributo);
					}	
				});
				popup.add(j2);
				
				// Renombrar un atributo
				JMenuItem j1 = new JMenuItem(Lenguaje.text(Lenguaje.RENAME_ATTRIB));
				j1.setFont(theme.font());
				j1.setForeground(theme.fontColor());
				j1.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferAtributo clon_atributo = atributo.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_RenombrarAtributo,clon_atributo);
					}	
				});
				popup.add(j1);

				// Eliminar un atributo
				JMenuItem j7 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE_ATTRIB));
				j7.setFont(theme.font());
				j7.setForeground(theme.fontColor());
				j7.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferAtributo clon_atributo = atributo.clonar();
						Vector<Object> v = new Vector<Object>();
						v.add(clon_atributo);
						v.add(true);
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarAtributo,v);
					}	
				});
				popup.add(j7);					
				popup.add(new JSeparator());

				// Establecer clave primaria
				// Solamente estara activo cuando sea un atributo directo de una entidad
				final TransferEntidad ent = esAtributoDirecto(atributo);
				if (ent != null){
					JCheckBoxMenuItem j6 = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.IS_PRIMARY_KEY)+" \""+ent.getNombre()+"\"");
					if (atributo.isClavePrimaria()) j6.setSelected(true);
					j6.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferAtributo clon_atributo = atributo.clonar();
							TransferEntidad clon_entidad = ent.clonar();
							Vector<Transfer> vector = new Vector<Transfer>();
							vector.add(clon_atributo);
							vector.add(clon_entidad);
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarClavePrimariaAtributo,vector);	
						}	
					});
					popup.add(j6);
				}
				
				// Es un atributo compuesto
				JCheckBoxMenuItem j3 = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.COMPOSED));
				final boolean notnul= atributo.getNotnull();
				final boolean unique = atributo.getUnique();
				if (atributo.getCompuesto()) j3.setSelected(true);
				else j3.setSelected(false);
				j3.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferAtributo clon_atributo = atributo.clonar();
						if (notnul){
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarNotNullAtributo,clon_atributo);
						}
						if (unique){
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarUniqueAtributo,clon_atributo);
						}
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarCompuestoAtributo,clon_atributo);	
					}	
				});
				popup.add(j3);
				
				// Si es compuesto
				if (atributo.getCompuesto()){
					JMenuItem j4 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_SUBATTRIBUTE));
					j4.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferAtributo clon_atributo = atributo.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirSubAtributoAAtributo,clon_atributo);
						}	
					});
					popup.add(j4);
				}
				//popup.add(new JSeparator());

				// Es un atributo NotNull
				if(!atributo.getCompuesto() && !atributo.isClavePrimaria()){
					JCheckBoxMenuItem j3a = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.NOT_NULL));
					if (atributo.getNotnull()) j3a.setSelected(true);
					else j3a.setSelected(false);
					j3a.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferAtributo clon_atributo = atributo.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarNotNullAtributo,clon_atributo);	
						}	
					});
					popup.add(j3a);
					//popup.add(new JSeparator());
				}
				// Es un atributo Unique
				if(!atributo.getCompuesto() && !atributo.isClavePrimaria()){
				JCheckBoxMenuItem j3b = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.UNIQUE));
				if (atributo.getUnique()) j3b.setSelected(true);
				else j3b.setSelected(false);
				j3b.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferAtributo clon_atributo = atributo.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarUniqueAtributo,clon_atributo);	
					}	
				});
				popup.add(j3b);
				//popup.add(new JSeparator());
				}
				
				// Es un atributo multivalorado
				if( !atributo.isClavePrimaria()){
					JCheckBoxMenuItem j5 = new JCheckBoxMenuItem(Lenguaje.text(Lenguaje.IS_MULTIVALUATED));
					if (atributo.isMultivalorado()) j5.setSelected(true);
					else j5.setSelected(false);
					j5.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferAtributo clon_atributo = atributo.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarMultivaloradoAtributo,clon_atributo);	
						}	
					});
					popup.add(j5);
					//popup.add(new JSeparator());
				}
				
				
				popup.add(new JSeparator());
				//Añadir restricciones			
				JMenuItem j8 = new JMenuItem(Lenguaje.text(Lenguaje.RESTRICTIONS));
				j8.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferAtributo clon_atributo = atributo.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirRestriccionAAtributo,clon_atributo);
					}	
				});
				popup.add(j8);
							
				
			}else if(nombre.contains(Lenguaje.text(Lenguaje.RELATION)) || 
					 nombre.contains(Lenguaje.text(Lenguaje.RELATION).toLowerCase())){
				//buscamos el transfer
				nombre= nombre.replace('"', '*');
				nombre= nombre.replaceAll(Lenguaje.text(Lenguaje.THE_RELATION)+" ", "");
				nombre= nombre.replace("*", "");
				
				int index=-1;
				for (int i=0; i<listaRelaciones.size();i++){
					if(listaRelaciones.get(i).getNombre().equals(nombre)){
					 index=i;	
					}
				}
				
				if (index == -1){
					nombre= nombre.substring(nombre.indexOf('(')+1, nombre.length()-1);
					int indice = Integer.parseInt(nombre);
					for (int i=0; i<listaRelaciones.size();i++){
						if(listaRelaciones.get(i).getIdRelacion()==indice){
						 index=i;	
						}
					}
				}
				
				final TransferRelacion relacion= listaRelaciones.get(index);
									
				if (!(relacion.getTipo().equals("IsA"))){
					// Anadir una entidad
					JMenuItem j3 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_ENT));
					j3.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferRelacion clon_relacion = relacion.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirEntidadARelacion,clon_relacion);	
						}	
					});
					popup.add(j3);

					// Quitar una entidad
					JMenuItem j4 = new JMenuItem(Lenguaje.text(Lenguaje.REMOVE_ENTITY));
					j4.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferRelacion clon_relacion = relacion.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_QuitarEntidadARelacion,clon_relacion);	
						}	
					});
					popup.add(j4);

					// Editar la aridad de una entidad
					JMenuItem j5 = new JMenuItem(Lenguaje.text(Lenguaje.EDIT_CARD_ROL));
					j5.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferRelacion clon_relacion = relacion.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarCardinalidadEntidad,clon_relacion);	
						}	
					});
					popup.add(j5);
					popup.add(new JSeparator());

					// Anadir un atributo a la relacion
					JMenuItem j6 = new JMenuItem(Lenguaje.text(Lenguaje.ADD_ATTRIBUTE));
					if (relacion.getTipo().equals("Debil"))
						j6.setEnabled(false);
					else{
						j6.setEnabled(true);
						j6.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(ActionEvent e) {
								popup.setVisible(false);
								TransferRelacion clon_relacion = relacion.clonar();
								c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirAtributoRelacion,clon_relacion);	
							}	
						});
					}
					popup.add(j6);	
					popup.add(new JSeparator());
					
					// Renombrar la relacion
					JMenuItem j1 = new JMenuItem(Lenguaje.text(Lenguaje.RENAME_RELATION));
					j1.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferRelacion clon_relacion = relacion.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_RenombrarRelacion,clon_relacion);	
						}	
					});
					popup.add(j1);
					
					// Eliminar la relacion
						JMenuItem j7 = new JMenuItem(Lenguaje.text(Lenguaje.DELETE_REL));
						j7.addActionListener(new java.awt.event.ActionListener() {
							public void actionPerformed(ActionEvent e) {
								popup.setVisible(false);
								TransferRelacion clon_relacion = relacion.clonar();
								Vector<Object> v = new Vector<Object>();
								v.add(clon_relacion);
								v.add(true);
								c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarRelacionNormal,v);	
							}	
						});
						popup.add(j7);				
						popup.add(new JSeparator());

					//Añadir restricciones			
					JMenuItem j8 = new JMenuItem(Lenguaje.text(Lenguaje.RESTRICTIONS));
					j8.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferRelacion clon_relacion = relacion.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirRestriccionARelacion,clon_relacion);
						}	
					});
					popup.add(j8);
					//Añadir restricciones	Unique		
					JMenuItem j9 = new JMenuItem(Lenguaje.text(Lenguaje.TABLE_UNIQUE));
					j9.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferRelacion clon_relacion = relacion.clonar();
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_TablaUniqueARelacion,clon_relacion);
						}	
					});
					popup.add(j9);
					
				} else {// if no Isa
				
				popup.add(new JMenu().add(new AbstractAction(Lenguaje.text(Lenguaje.SET_PARENT_ENT)){
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferRelacion clon_relacion = relacion.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EstablecerEntidadPadre,clon_relacion);
					}
				}));
				popup.add(new JMenu().add(new AbstractAction(Lenguaje.text(Lenguaje.REMOVE_PARENT_ENT)){
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferRelacion clon_relacion = relacion.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_QuitarEntidadPadre,clon_relacion);
					}
				}));

				popup.add(new JSeparator());

				popup.add(new JMenu().add(new AbstractAction(Lenguaje.text(Lenguaje.ADD_CHILD_ENT)){
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferRelacion clon_relacion = relacion.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_AnadirEntidadHija,clon_relacion);
					}
				}));

				popup.add(new JMenu().add(new AbstractAction(Lenguaje.text(Lenguaje.REMOVE_CHILD_ENT)){
					public void actionPerformed(ActionEvent e) {
						popup.setVisible(false);
						TransferRelacion clon_relacion = relacion.clonar();
						c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_QuitarEntidadHija,clon_relacion);
					}
				}));

				popup.add(new JSeparator());
				//Eliminal la relacion
				
					popup.add(new JMenu().add(new AbstractAction(Lenguaje.text(Lenguaje.DELETE_REL)){
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
							TransferRelacion clon_relacion = relacion.clonar();
							Vector<Object> v = new Vector<Object>();
							v.add(clon_relacion);
							v.add(true);
							c.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EliminarRelacionIsA,v);
						}
					}));
			}
		}	
		popup.setLocation(e.getLocationOnScreen());
		popup.setVisible(true);
	}
	};
	private TransferEntidad esAtributoDirecto(TransferAtributo ta){
		//Collection<TransferEntidad> listaEntidades = this.entidades.values();
		for (Iterator<TransferEntidad> it = listaEntidades.iterator(); it.hasNext();){
			TransferEntidad te = it.next();
			if (te.getListaAtributos().contains(String.valueOf(ta.getIdAtributo()))) return te;
		}
		return null;
	}
 
	public void activaBotones(){
		botonLimpiarPantalla.setEnabled(true);
		botonValidar.setEnabled(true);
		botonModeloRelacional.setEnabled(true);
		botonScriptSQL.setEnabled(true);
		botonExportarArchivo.setEnabled(true);
		botonEjecutarEnDBMS.setEnabled(true);
	}
		
	public GUIPrincipal getThePrincipal(){
		return this;
	}
	public JPopupMenu getPopUp(){
		return popup;
	}
	
	public PanelGrafo getPanelDiseno(){
		return panelDiseno;
	}

	public void cambiarConexion(String nombreConexion) {
		if(listaConexiones==null) return;
		// Obtener conexión indicada
		TransferConexion tc = null;
		int i=0;
		boolean encontrado = false;
		while (!encontrado && i<listaConexiones.size()){
			tc = listaConexiones.get(i);
			encontrado = tc.getRuta().equalsIgnoreCase(nombreConexion);
			i++;
		}
		// Cambiar conexión actual
		conexionActual = tc;
	}

	public void imprimir() {
		this.panelDiseno.printGraph();
	}
	@Override
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowClosing(WindowEvent e) {
		this.c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_Salir, null);
	}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	
	public void loadInfo() {
		c.mensajeDesde_PanelDiseno(TC.PanelDiseno_MostrarDatosEnPanelDeInformacion, getPanelDiseno().generaArbolInformacion());
		c.mensajeDesde_PanelDiseno(TC.PanelDiseno_MostrarDatosEnTablaDeVolumenes, getPanelDiseno().generaTablaVolumenes());
	}

	public int getPanelsMode() {
		return this.dealer.getPanelsMode();
	}
	
	public void modoProgramador() {
		if(this.dealer.getPanelsMode() == 2) return;
		this.dealer.modoProgramador();
		this.barraDeMenus.setModoVista(getPanelsMode());
	}
	
	public void modoDiseno() {
		if(this.dealer.getPanelsMode() == 1) return;
		this.dealer.modoDiseno();
		this.barraDeMenus.setModoVista(getPanelsMode());
	}
	
	public void modoVerTodo() {
		if(this.dealer.getPanelsMode() == 0) return;
		this.dealer.modoVerTodo();
		this.barraDeMenus.setModoVista(getPanelsMode());
	}
	
	public void setModoVista(int modo) {
		this.barraDeMenus.setModoVista(modo);
		if(modo==0)dealer.modoVerTodo();
		else if(modo==1)dealer.modoDiseno();
		else if(modo==2)dealer.modoProgramador();
	}

	public String getInstrucciones() {
		return codigoText.getInstrucciones();
	}
	
	private void setLookAndFeel(){
		UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
		UIManager.put("Button.font",  theme.font());
		UIManager.put("ToggleButton.font",  theme.font());
		UIManager.put("RadioButton.font",  theme.font());
		UIManager.put("CheckBox.font",  theme.font());
		UIManager.put("ColorChooser.font",  theme.font());
		UIManager.put("ComboBox.font",  theme.font());
		UIManager.put("Label.font",  theme.font());
		UIManager.put("List.font",  theme.font());
		UIManager.put("MenuBar.font",  theme.font());
		UIManager.put("MenuItem.font",  theme.font());
		UIManager.put("RadioButtonMenuItem.font",  theme.font());
		UIManager.put("CheckBoxMenuItem.font",  theme.font());
		UIManager.put("Menu.font",  theme.font());
		UIManager.put("PopupMenu.font",  theme.font());
		UIManager.put("OptionPane.font",  theme.font());
		UIManager.put("Panel.font",  theme.font());
		UIManager.put("ProgressBar.font",  theme.font());
		UIManager.put("ScrollPane.font",  theme.font());
		UIManager.put("Viewport.font",  theme.font());
		UIManager.put("TabbedPane.font",  theme.font());
		UIManager.put("Table.font",  theme.font());
		UIManager.put("TableHeader.font",  theme.font());
		UIManager.put("TextField.font",  theme.font());
		UIManager.put("PasswordField.font",  theme.font());
		UIManager.put("TextArea.font",  theme.font());
		UIManager.put("TextPane.font",  theme.font());
		UIManager.put("EditorPane.font",  theme.font());
		UIManager.put("TitledBorder.font",  theme.font());
		UIManager.put("ToolBar.font",  theme.font());
		UIManager.put("ToolTip.font",  theme.font());
		UIManager.put("Tree.font",  theme.font());
		UIManager.put("nimbusBase", theme.main());
		UIManager.put("control", theme.control());
		UIManager.put("nimbusSelectionBackground", theme.SelectionBackground());
		UIManager.put("text", theme.fontColor());
		UIManager.put("nimbusSelectedText", theme.fontColor());
		UIManager.put("nimbusFocus", theme.SelectionBackground());
		UIManager.put("menu", theme.background());
		UIManager.put("menuText", theme.background());
		UIManager.put("nimbusBlueGrey", theme.background());
		UIManager.put("nimbusBorder", theme.background());
		UIManager.put("nimbusSelection", theme.SelectionBackground());
		UIManager.put("Tree.collapsedIcon", false);
		UIManager.put("Tree.expandedIcon", false);
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) 
            if ("Nimbus".equals(info.getName())) {
            	try { javax.swing.UIManager.setLookAndFeel(info.getClassName());} 
            	catch (ClassNotFoundException | InstantiationException | IllegalAccessException| UnsupportedLookAndFeelException e) {e.printStackTrace();}
            break;
        }
	}
}