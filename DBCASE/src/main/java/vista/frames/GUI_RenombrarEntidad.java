package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TransferEntidad;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings("serial")
public class GUI_RenombrarEntidad extends Parent_GUI {

	private Controlador controlador;
	private TransferEntidad entidad;
	private JButton botonRenombrar;
	private JTextField cajaNombre = this.getCajaNombre(25, 40);
	private JLabel explicacion;

	public GUI_RenombrarEntidad() {
		initComponents();
	}

	private void initComponents() {
		setTitle(Lenguaje.text(Lenguaje.RENAME_ENTITY_DBDT));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		getContentPane().setLayout(null);
		this.setSize(300, 170);
		cajaNombre.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==10){botonRenombrarActionPerformed(null);}
				else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		{
			botonRenombrar = boton(120, 90,Lenguaje.text(Lenguaje.RENAME));
			getContentPane().add(botonRenombrar);
			botonRenombrar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonRenombrarActionPerformed(evt);
				}
			});
			botonRenombrar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10) botonRenombrarActionPerformed(null);
					else if(e.getKeyCode()==27) botonCancelarActionPerformed(null);
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		getContentPane().add(cajaNombre);
		{
			explicacion = new JLabel();
			getContentPane().add(explicacion);
			explicacion.setFont(theme.font());
			explicacion.setText(Lenguaje.text(Lenguaje.WRITE_NEW_ENTITY_NAME));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 10, 236, 25);
			explicacion.setFocusable(false);
		}
		this.addMouseListener(this);
		this.addKeyListener(this);
	}
	
	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		this.cajaNombre.setText(this.getEntidad().getNombre());
		SwingUtilities.invokeLater(doFocus);
		this.centraEnPantalla();
		this.setVisible(true);
	}
	
	private Runnable doFocus = new Runnable() {
	     public void run() {
	         cajaNombre.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}

	/*
	 * Oyentes de los botones
	 */
	private void botonRenombrarActionPerformed(java.awt.event.ActionEvent evt) {                                               
		// Datos que mandamos al controlador
		Vector<Object> v = new Vector<Object>();
		v.add(this.getEntidad());
		v.add(this.cajaNombre.getText());
		// Mandamos mensaje + datos al controlador
		this.getControlador().mensajeDesde_GUI(TC.GUIRenombrarEntidad_Click_BotonRenombrar, v);
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
				this.botonRenombrarActionPerformed(null);
				break;
			}
		}
	} 

	/*
	 * Getters y Setters
	 */
	public TransferEntidad getEntidad() {
		return entidad;
	}

	public void setEntidad(TransferEntidad entidad) {
		this.entidad = entidad;
	}
	
	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}
}
