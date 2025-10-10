package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import controlador.ConfiguradorInicial;
import controlador.Controlador;
import controlador.TC;
import modelo.conectorDBMS.FactoriaConectores;
import modelo.transfers.TransferConexion;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import vista.componentes.MyFileChooser;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings("serial")
public class GUI_Conexion extends Parent_GUI{
 
	private Controlador controlador;
	private Vector<TransferEntidad> listaEntidades;
	private boolean _crear;
	private JLabel usuario;
	private JTextField cajaPuerto;
	private JLabel password;
	private JTextField cajaBase;
	private JButton botonAnadir;
	private JTextField cajaServer;
	private JLabel puerto;
	private JLabel server;
	private TransferRelacion relacion;
	private JLabel base;
	private JTextField cajaUsuario;
	private JButton btnExaminar;
	private JButton btnComprobar;
	private JButton btnPista;
	private JTextField textoNombre;
	private JLabel nombre;
	private JPasswordField cajaPass;
	protected TransferConexion _conexion;

	public GUI_Conexion() {
		initComponents();
	}

	private void initComponents() {
		setTitle(Lenguaje.text(Lenguaje.SHAPE_DBMS));
        this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setModal(true);
        getContentPane().setLayout(null);
        getContentPane().add(getserver());
        getContentPane().add(getpuerto());
        getContentPane().add(getcajaServer());
        getContentPane().add(getcajaPuerto());
        getContentPane().add(getusuario());
        getContentPane().add(getbase());
        getContentPane().add(getcajaUsuario());
        getContentPane().add(getBotonAnadir());
        getContentPane().add(getcajaBase());
        getContentPane().add(getpassword());
        getContentPane().add(getCajaPass());
        getContentPane().add(getNombre());
        getContentPane().add(getTextoNombre());
        getContentPane().add(getBtnPista());
        getContentPane().add(getBtnComprobar());
        getContentPane().add(getBtnExaminar());
        this.setSize(550, 350);
        this.addMouseListener(this);
		this.addKeyListener(this);
    }

	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(boolean crear, String nombre, TransferConexion tc){
		this._crear = crear;
		if (crear) {
			this.cajaPass.setText("");
			this.textoNombre.setText("");
			this.textoNombre.setEnabled(true);
			this.cajaServer.setText("");
			this.cajaPuerto.setText("");
			this.cajaUsuario.setText("");
			this.cajaBase.setText("");
			if (_conexion.getTipoConexion() == FactoriaConectores.CONECTOR_MSACCESS_MDB){
				this.server.setEnabled(false);
				this.puerto.setEnabled(false);
				this.usuario.setEnabled(false);
				this.password.setEnabled(false);
				this.cajaServer.setEnabled(false);
				this.cajaPuerto.setEnabled(false);
				this.cajaBase.setEnabled(true);
				this.btnExaminar.setVisible(true);
				this.btnExaminar.setEnabled(true);
				this.cajaUsuario.setEnabled(false);
				this.cajaPass.setEnabled(false);
			}else{
				this.server.setEnabled(true);
				this.puerto.setEnabled(true);
				this.usuario.setEnabled(true);
				this.password.setEnabled(true);
				this.cajaServer.setEnabled(true);
				this.cajaPuerto.setEnabled(true);
				this.cajaBase.setEnabled(true);
				this.btnExaminar.setVisible(false);
				this.btnExaminar.setEnabled(false);
				this.cajaUsuario.setEnabled(true);
				this.cajaPass.setEnabled(true);
			}
		} else {
			
			// EDITAR
			this.textoNombre.setText(nombre);
			this.textoNombre.setEnabled(false);
			
			if (tc.getTipoConexion() == FactoriaConectores.CONECTOR_MSACCESS_MDB){
				this.server.setEnabled(false);
				this.puerto.setEnabled(false);
				this.usuario.setEnabled(false);
				this.password.setEnabled(false);
				
				this.cajaServer.setEnabled(false);
				this.cajaServer.setText("");
				this.cajaPuerto.setEnabled(false);
				this.cajaPuerto.setText("");
				this.cajaBase.setEnabled(true);
				this.cajaBase.setText(tc.getRuta());
				this.cajaUsuario.setEnabled(false);
				this.cajaUsuario.setText("");
				this.cajaPass.setEnabled(false);
				this.cajaPass.setText("");
			}else{
				String server = tc.getRuta();
				String port = "";
				if (server.lastIndexOf(":") > 0){
					port = server.substring(server.indexOf(":")+1);
					server = server.substring(0, server.indexOf(":"));
				}
				this.cajaBase.setText(tc.getDatabase());
				this.cajaBase.setEnabled(true);
				this.cajaServer.setText(server);
				this.cajaServer.setEnabled(true);
				this.cajaPuerto.setText(port);
				this.cajaPuerto.setEnabled(true);
				this.cajaUsuario.setText(tc.getUsuario());
				this.cajaUsuario.setEnabled(true);
				this.cajaPass.setText(tc.getPassword());
				this.cajaPass.setEnabled(true);
			}
			_conexion = tc;
		}
		
		this.centraEnPantalla();
		SwingUtilities.invokeLater(doFocus);
		this.setVisible(true);	
	}
	
	private Runnable doFocus = new Runnable() {
	     public void run() {
	         cajaServer.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}

	/*
	 * Oyentes de los botones
	 */

	private void botonConnectActionPerformed(java.awt.event.ActionEvent evt) {
		// Extraer datos
		String txtServer = this.cajaServer.getText();
		String txtPuerto = this.cajaPuerto.getText();
		String txtDatabase = this.cajaBase.getText();
		String txtUsuario = this.cajaUsuario.getText();
		String txtPassword = new String(this.cajaPass.getPassword());
		
		if (_crear){
			ConfiguradorInicial conf = new ConfiguradorInicial();
			conf.leerFicheroConfiguracion();
			if (!conf.estaDisponibleNombreConexion(textoNombre.getText())){
				JOptionPane.showMessageDialog(
						null, (Lenguaje.text(Lenguaje.ERROR))+"\n" +
						"Ya existe una conexión con ese nombre", (Lenguaje.text(Lenguaje.DBDT)),
						JOptionPane.PLAIN_MESSAGE);
				return;
			}
			
			boolean faltanDatos = false;
			faltanDatos &= txtDatabase.equals("");
			faltanDatos &= cajaBase.getText().equalsIgnoreCase("");
			if (faltanDatos){
				// Notificar error
				JOptionPane.showMessageDialog(
					null,(Lenguaje.text(Lenguaje.ERROR))+"\n" +
					(Lenguaje.text(Lenguaje.INFORMATION_INCOMPLETE)),
					(Lenguaje.text(Lenguaje.DBDT)),JOptionPane.PLAIN_MESSAGE);
				return;
			}else{
				// Guardar conexion
				TransferConexion guardada = new TransferConexion(			
						this._conexion.getTipoConexion(), 
						txtServer + ":" + txtPuerto,
						false,
						txtDatabase,
						txtUsuario,
						txtPassword);
				conf.anadeConexion(textoNombre.getText(), guardada);
				conf.guardarFicheroCofiguracion();
			}
			
			controlador.getTheGuiSeleccionarConexion().rellenaTabla();
			
			this.setInactiva();
		}else{
			
			// EDITAR
			// Comprobar datos
			boolean faltanDatos = false;
			faltanDatos &= txtDatabase.equals("");
			faltanDatos &= cajaBase.getText().equalsIgnoreCase("");
			if (faltanDatos){
				// Notificar error
				JOptionPane.showMessageDialog(
					null,(Lenguaje.text(Lenguaje.ERROR))+"\n" +
					(Lenguaje.text(Lenguaje.INFORMATION_INCOMPLETE)),
					(Lenguaje.text(Lenguaje.DBDT)),
					JOptionPane.PLAIN_MESSAGE);
				return;
				
			}else{
				// Guardar conexion
				TransferConexion guardada = new TransferConexion(			
						this._conexion.getTipoConexion(), 
						txtServer + ":" + txtPuerto,
						false,
						txtDatabase,
						txtUsuario,
						txtPassword);
				
				ConfiguradorInicial conf = new ConfiguradorInicial();
				conf.leerFicheroConfiguracion();
				conf.quitaConexion(textoNombre.getText());
				conf.anadeConexion(textoNombre.getText(), guardada);
				conf.guardarFicheroCofiguracion();
			}
			controlador.getTheGuiSeleccionarConexion().rellenaTabla();
			this.setInactiva();
		}
	}

	private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
	}
	public void keyPressed( KeyEvent e ) {
		switch (e.getKeyCode()){
			case 27: {
				this.setInactiva();
				break;
			}
			case 10:{
				this.botonConnectActionPerformed(null);
				break;
			}
		}
	} 
	
	//Oyente para todos los elementos
	private KeyListener general = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==10){botonConnectActionPerformed(null);}
			if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	/*
	 * Interfaz
	 */
	private JLabel getserver() {
		if(server == null) {
			server = new JLabel();
			server.setText(Lenguaje.text(Lenguaje.SERVER));
			server.setFont(theme.font());
			server.setOpaque(false);
			server.setBounds(25, 65, 100, 25);
			server.setFocusable(false);
		}
		return server;
	}
		
	
	private JLabel getpuerto() {
		if(puerto == null) {
			puerto = new JLabel();
			puerto.setFont(theme.font());
			puerto.setText(Lenguaje.text(Lenguaje.PORT));
			puerto.setBounds(25, 105, 100, 25);
		}
		return puerto;
	}
	
	private JTextField getcajaServer() {
		if(cajaServer == null) {
			cajaServer = new JTextField();
			cajaServer.setBounds(180, 65, 320, 30);
			cajaServer.setFont(theme.font());
			cajaServer.setForeground(theme.labelFontColorDark());
			cajaServer.addKeyListener(general);
		}
		return cajaServer;
	}
	
	private JTextField getcajaPuerto() {
		if(cajaPuerto == null) {
			cajaPuerto = new JTextField();
			cajaPuerto.setBounds(180, 105, 320, 30);
			cajaPuerto.setFont(theme.font());
			cajaPuerto.setForeground(theme.labelFontColorDark());
			cajaPuerto.addKeyListener(general);
		}
		return cajaPuerto;
	}
	
	private JLabel getusuario() {
		if(usuario == null) {
			usuario = new JLabel();
			usuario.setFont(theme.font());
			usuario.setText(Lenguaje.text(Lenguaje.USER));
			usuario.setBounds(25, 185, 100, 25);
		}
		return usuario;
	}
	
	private JLabel getbase() {
		if(base == null) {
			base = new JLabel();
			base.setText(Lenguaje.text(Lenguaje.DATA_BASE));
			base.setFont(theme.font());
			base.setOpaque(false);
			base.setBounds(25, 145, 150, 25);
			base.setFocusable(false);
		}
		return base;
	}
	
	private JTextField getcajaUsuario() {
		if(cajaUsuario == null) {				
			cajaUsuario = new JTextField();
			cajaUsuario.setFont(theme.font());
			cajaUsuario.setForeground(theme.labelFontColorDark());
			cajaUsuario.setBounds(180, 185, 320, 30);
		}
		cajaUsuario.addKeyListener(general);
		return cajaUsuario;
	}
		
	private JTextField getcajaBase() {
		if(cajaBase == null) {
			cajaBase = new JTextField();
			cajaBase.setFont(theme.font());
			cajaBase.setForeground(theme.labelFontColorDark());
			cajaBase.setBounds(180, 145, 320, 30);
		}
		return cajaBase;
	}
	
	private JLabel getpassword() {
		if(password == null) {
			password = new JLabel();
			password.setText(Lenguaje.text(Lenguaje.PASSWORD));
			password.setFont(theme.font());
			password.setBounds(25, 225, 100, 25);
		}
		return password;
	}
	
	private JPasswordField getCajaPass() {
		if(cajaPass == null) {
			cajaPass = new JPasswordField();
			cajaPass.setFont(theme.font());
			cajaPass.setForeground(theme.labelFontColorDark());
			cajaPass.setBounds(180, 225, 320, 30);
		}
		return cajaPass;
	}

	public void setConexion(TransferConexion con){
		this._conexion = con;
	}
	
	private JLabel getNombre() {
		if(nombre == null) {
			nombre = new JLabel();
			nombre.setText(Lenguaje.text(Lenguaje.NAME));
			nombre.setFont(theme.font());
			nombre.setBounds(25, 25, 100, 25);
		}
		return nombre;
	}
	
	private JTextField getTextoNombre() {
		if(textoNombre == null) {
			textoNombre = new JTextField();
			textoNombre.setFont(theme.font());
			textoNombre.setForeground(theme.labelFontColorDark());
			textoNombre.setBounds(180, 25, 320, 30);
		}
		return textoNombre;
	}
	
	private JButton getBtnPista() {
		if(btnPista == null) btnPista = boton(25, 275,Lenguaje.text(Lenguaje.HINT));
		
		btnPista.addActionListener(new ActionListener() {
			// Completar los cuadros con información de ejemplo
			public void actionPerformed(ActionEvent evt) {
				switch(_conexion.getTipoConexion()){
				case FactoriaConectores.CONECTOR_MYSQL:
					cajaServer.setText("localhost");
					cajaPuerto.setText("");
					cajaBase.setText("test");
					cajaUsuario.setText("root");
					cajaPass.setText("");
					break;
				case FactoriaConectores.CONECTOR_MSACCESS_MDB:
					cajaServer.setText("");
					cajaPuerto.setText("");
					cajaBase.setText("c:\\hlocal\\mydatabase.mdb");
					cajaUsuario.setText("");
					cajaPass.setText("");
					break;
				case FactoriaConectores.CONECTOR_ORACLE:
					cajaServer.setText("tania.fdi.ucm.es");
					cajaPuerto.setText("1521");
					cajaBase.setText("db003");
					cajaUsuario.setText("ges00");
					cajaPass.setText("");
					break;
				}
			}
		});
		
		return btnPista;
	}
	
	private JButton getBotonAnadir() {
		if(botonAnadir == null) {
			botonAnadir = boton(450, 275,Lenguaje.text(Lenguaje.ACCEPT));
			botonAnadir.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonConnectActionPerformed(evt);
				}
			});
			botonAnadir.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonConnectActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return botonAnadir;
	}
	
	private JButton getBtnComprobar() {
		if(btnComprobar == null) btnComprobar = boton(140, 275,Lenguaje.text(Lenguaje.TEST_DATA));
		
		btnComprobar.addActionListener(new ActionListener() {
			// Comprueba si los datos de conexión son correctos
			public void actionPerformed(ActionEvent evt) {
				// Extraer datos
				String txtServer = cajaServer.getText();
				String txtPuerto = cajaPuerto.getText();
				String txtDatabase = cajaBase.getText();
				String txtUsuario = cajaUsuario.getText();
				String txtPassword = new String(cajaPass.getPassword());
				
				// Comprobar datos
				boolean faltanDatos = false;
				faltanDatos &= txtDatabase.equals("");
				faltanDatos &= cajaBase.getText().equalsIgnoreCase("");
				if (faltanDatos){
					// Notificar error
					JOptionPane.showMessageDialog(
						null,
						(Lenguaje.text(Lenguaje.ERROR))+"\n" +
						(Lenguaje.text(Lenguaje.INFORMATION_INCOMPLETE)),
						(Lenguaje.text(Lenguaje.DBDT)),
						JOptionPane.PLAIN_MESSAGE);
					return;
					
				}else{
					// Generar la connectionString
					String connectionString = "";
					
					switch(_conexion.getTipoConexion()){
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
					
					connectionString += txtServer;
					if (!txtPuerto.equalsIgnoreCase("")) connectionString += ":" + txtPuerto;
					
					if (_conexion.getTipoConexion() != FactoriaConectores.CONECTOR_ORACLE) connectionString += "/";
					else connectionString += "#" + txtDatabase;
					
					if (_conexion.getTipoConexion() == FactoriaConectores.CONECTOR_MSACCESS_MDB)
						connectionString = _conexion.getDatabase();
					
					// Probar conexion
					TransferConexion con = new TransferConexion(			
							_conexion.getTipoConexion(), connectionString,false,txtDatabase,txtUsuario,txtPassword);
					
					controlador.mensajeDesde_GUI(TC.GUIConexionDBMS_PruebaConexion, con);
				}
			}
		});
		
		return btnComprobar;
	}
	
	private JButton getBtnExaminar() {
		if(btnExaminar == null) btnExaminar = boton(558, 131,Lenguaje.text(Lenguaje.EXPLORE));
		
		btnExaminar.addActionListener(new ActionListener() {
			// Comprueba si los datos de conexión son correctos
			public void actionPerformed(ActionEvent evt) {
				MyFileChooser chooser = new MyFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter("Access databases (*.mdb)", "mdb");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(null);
			    if(returnVal == MyFileChooser.APPROVE_OPTION) {
			       cajaBase.setText(chooser.getSelectedFile().getPath());
			    }
			}
		});
		return btnExaminar;
	}
	
	/*
	 * Getters y setters
	 */
	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}

	public Vector<TransferEntidad> getListaEntidades() {
		return listaEntidades;
	}

	public void setListaEntidades(Vector<TransferEntidad> listaEntidades) {
		this.listaEntidades = listaEntidades;
	}

	public TransferRelacion getRelacion() {
		return relacion;
	}

	public void setRelacion(TransferRelacion relacion) {
		this.relacion = relacion;
	}
}

