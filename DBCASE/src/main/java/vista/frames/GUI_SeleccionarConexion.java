package vista.frames;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import controlador.ConfiguradorInicial;
import controlador.Controlador;
import controlador.TC;
import modelo.conectorDBMS.FactoriaConectores;
import modelo.transfers.TransferConexion;
import vista.componentes.CustomCellEditor;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings("serial")
public class GUI_SeleccionarConexion extends Parent_GUI{

	private Controlador controlador;
	private TransferConexion _conexion;
	private JScrollPane jScrollPane1;
	private JButton botonNueva;
	private JButton botonAceptar;
	private JButton botonCancelar;
	private JTable tablaConjuntos;
	private JButton botonBorrar;
	private JButton botonEditar;

	public GUI_SeleccionarConexion() {
		initComponents();
	}

	private void initComponents() {

	setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setResizable(false);
		setModal(true);
		getContentPane().setLayout(null);
		this.setSize(400,350);
		{
			jScrollPane1 = new JScrollPane();
			getContentPane().add(jScrollPane1);
			jScrollPane1.setBounds(0, 0, 340, 250);
		}
		{
			botonAceptar = boton(270, 275,Lenguaje.text(Lenguaje.CONNECT));
			getContentPane().add(botonAceptar);
			botonAceptar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonAceptarActionPerformed(evt);
				}
			});
			botonAceptar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10) botonCancelarActionPerformed(null);
					else if(e.getKeyCode()==27) botonCancelarActionPerformed(null);
					else if(e.getKeyCode()==37) botonEditar.grabFocus();
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		{
			botonCancelar = boton(10, 275,Lenguaje.text(Lenguaje.CANCEL));
			getContentPane().add(botonCancelar);
			botonCancelar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonCancelarActionPerformed(evt);
				}
			});
		}
		{
			botonEditar = boton(140, 275,Lenguaje.text(Lenguaje.EDIT));
			getContentPane().add(botonEditar);
			botonEditar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonEditarActionPerformed(evt);
				}
			});
			botonEditar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonCancelarActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
					else if(e.getKeyCode()==39){botonAceptar.grabFocus();}
					else if(e.getKeyCode()==37){botonBorrar.grabFocus();}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		{
			botonNueva = new JButton();
			botonNueva.setFont(theme.font());
			getContentPane().add(botonNueva);
			botonNueva.setText("+");
			botonNueva.setBounds(350, 10, 45, 45);
			botonNueva.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {}
				@Override
				public void keyReleased(KeyEvent e) {}
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
					else if(e.getKeyCode()==39){botonBorrar.grabFocus();}
				}
			});
			botonNueva.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonNuevaActionPerformed(evt);
				}
			});
		}
		{
			botonBorrar = new JButton();
			botonBorrar.setFont(theme.font());
			getContentPane().add(botonBorrar);
			botonBorrar.setText("-");
			botonBorrar.setBounds(350, 55, 45, 45);
			botonBorrar.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {}
				@Override
				public void keyReleased(KeyEvent e) {}
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==27) botonCancelarActionPerformed(null);
					else if(e.getKeyCode()==37) botonNueva.grabFocus();
				}
			});
			botonBorrar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonBorrarActionPerformed(evt);
				}
			});
		}
		this.addKeyListener(this);
		this.addMouseListener(this);
		TableModel tablaModel = new DefaultTableModel(new String[][] {{""}},new String[]{ Lenguaje.text(Lenguaje.EXISTING_CONN)+":"});
		tablaConjuntos = new JTable(tablaModel) {
			@Override
			public boolean isCellEditable(int row, int column){
		      return false;
		    }
		};
		jScrollPane1.setViewportView(tablaConjuntos);
		tablaConjuntos.setFont(theme.font());
		tablaConjuntos.setBackground(theme.background());
		DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
		    @Override
		    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
		        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,row, column);
		        setFont(theme.font());
		        return this;
		    }
		};
        setForeground(theme.background());
        tablaConjuntos.setDefaultEditor(Object.class, CustomCellEditor.make());
        tablaConjuntos.getColumnModel().getColumn(0).setCellRenderer(r);
        tablaConjuntos.setRowHeight(25);
        tablaConjuntos.getTableHeader().setReorderingAllowed(false);//columnas fijadas
		this.addKeyListener(this);
	}

	/*
	 * Oyentes de los botones 
	 */
	private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {                                              
		String elegido = getElementoSeleccionado();		
		if (elegido == null){
			JOptionPane.showMessageDialog(null, 
				Lenguaje.text(Lenguaje.CHOOSE_CONN), 
				Lenguaje.text(Lenguaje.ERROR),0);
			return;
		}
		elegido = elegido.substring(0, elegido.indexOf("(") - 1);
		
		ConfiguradorInicial config = new ConfiguradorInicial();
		config.leerFicheroConfiguracion();
		
		TransferConexion tc = config.obtenConexion(elegido);
		
		// Conectar a base de datos
		String connectionString = "";
		
		switch(tc.getTipoConexion()){
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
		
		int dosPuntos = tc.getRuta().indexOf(":");
		if ( dosPuntos > 0 && dosPuntos == tc.getRuta().length() - 1)
			connectionString += tc.getRuta().substring(0, dosPuntos);
		else connectionString += tc.getRuta();
		
		if (_conexion.getTipoConexion() != FactoriaConectores.CONECTOR_ORACLE) connectionString += "/";
		else connectionString += "#" + tc.getDatabase();
		
		if (this._conexion.getTipoConexion() == FactoriaConectores.CONECTOR_MSACCESS_MDB)
			connectionString = tc.getDatabase();
		
		tc.setRuta(connectionString);
		
		// Enviar datos para la conexión
		controlador.mensajeDesde_GUI(TC.GUIConfigurarConexionDBMS_Click_BotonEjecutar, tc);
		this.setInactiva();
	}                                       
		
	private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {                                              
		this.setInactiva();
	}
	
	private void botonNuevaActionPerformed(ActionEvent evt) {
		controlador.getTheGUIConfigurarConexionDBMS().setConexion(_conexion);
		controlador.getTheGUIConfigurarConexionDBMS().setActiva(true, "", null);
	}
	
	private void botonBorrarActionPerformed(ActionEvent evt) {
		// <connection name="Conexion 3" type="0" path="localhost" database="test2" user="root" password="123456"  /> 
		String elegido = getElementoSeleccionado();		
		if (elegido == null){
			JOptionPane.showMessageDialog(null, 
				Lenguaje.text(Lenguaje.CHOOSE_CONN), 
				Lenguaje.text(Lenguaje.ERROR), 0);
			return;
		}
		elegido = elegido.substring(0, elegido.indexOf("(") - 1);
		ConfiguradorInicial config = new ConfiguradorInicial();
		config.leerFicheroConfiguracion();
		config.quitaConexion(elegido);
		config.guardarFicheroCofiguracion();
		rellenaTabla();
	}
	
	private void botonEditarActionPerformed(ActionEvent evt) {
		String elegido = getElementoSeleccionado();		
		if (elegido == null){
			JOptionPane.showMessageDialog(null, 
				Lenguaje.text(Lenguaje.CHOOSE_CONN), 
				Lenguaje.text(Lenguaje.ERROR), 0);
			return;
		}
		elegido = elegido.substring(0, elegido.indexOf("(") - 1);
		
		ConfiguradorInicial config = new ConfiguradorInicial();
		config.leerFicheroConfiguracion();
		TransferConexion tc = config.obtenConexion(elegido);
		
		controlador.getTheGUIConfigurarConexionDBMS().setActiva(false, elegido, tc);
	}

	private String getElementoSeleccionado() {
		int fila = tablaConjuntos.getSelectedRow();
		int col = tablaConjuntos.getSelectedColumn();

		String elegido = null;
		try {
			elegido = tablaConjuntos.getModel().getValueAt(fila, col).toString();	
		}catch(ArrayIndexOutOfBoundsException e) {
			return null;
		}
		return elegido;
	}
	
	public void rellenaTabla() {
		// Obtener las conexiones a mostrar
		ConfiguradorInicial config = new ConfiguradorInicial();
		config.leerFicheroConfiguracion();
		
		Hashtable<String, TransferConexion> conexiones;
		conexiones = config.obtenConexiones();
		
		String[][] valores = new String[conexiones.size()][1];
		
		Enumeration<String> keys = conexiones.keys();
		
		int i = 0;
		while (keys.hasMoreElements()){
			String nombre = keys.nextElement();
			TransferConexion conexion = conexiones.get(nombre);
			String usuario = conexion.getUsuario();
			String database = conexion.getDatabase();
			String dbms = FactoriaConectores.obtenerTodosLosConectores().get(conexion.getTipoConexion());
			
			// Añadir a la lista el nombre de cada conexión
			valores[i][0] = nombre + " (" + usuario + "@" + database + ", " + dbms + ")";
			i ++;
		}
		TableModel tablaModelo = new DefaultTableModel(valores, new String[] {Lenguaje.text(Lenguaje.TABLE_TITLE)});
		tablaConjuntos.setModel(tablaModelo);
	}
	
			
	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		this.centraEnPantalla();
		this.setTitle(Lenguaje.text(Lenguaje.SELECT_CONNECTION));
		rellenaTabla();
		SwingUtilities.invokeLater(doFocus);
		this.setVisible(true);
	}
	
	private Runnable doFocus = new Runnable() {
	     public void run() {
	         botonNueva.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
		this.dispose();
	}

	public void keyPressed( KeyEvent e ) {
		switch (e.getKeyCode()){
			case 27: {
				this.setInactiva();
				break;
			}
			case 10:{
				this.botonAceptarActionPerformed(null);
				break;
			}
		}
	}
		
	/*
	 * Getters y Setters
	 */
	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}
	
	public void setConexion(TransferConexion con){
		this._conexion = con;
	}
}
