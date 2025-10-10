package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TransferRelacion;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;


@SuppressWarnings({ "rawtypes", "serial" })
public class GUI_InsertarRelacion extends Parent_GUI{

	private Point2D posicionRelacion;
	private Controlador controlador;
	private JLabel explicacion;
	private JTextField cajaNombre = this.getCajaNombre(25, 50);
	private JButton botonInsertar;

	public GUI_InsertarRelacion() {
		initComponents();
	}

	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(Lenguaje.text(Lenguaje.INSERT_RELATION));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setModal(true);
		setResizable(false);
		getContentPane().setLayout(null);
		cajaNombre.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==10){botonInsertarActionPerformed(null);}
				else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		{
			botonInsertar = botonInsertar(150, 90);
			getContentPane().add(botonInsertar);
			botonInsertar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonInsertarActionPerformed(evt);
				}
			});
			botonInsertar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonInsertarActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			botonInsertar.setMnemonic(Lenguaje.text(Lenguaje.INSERT).charAt(0));
		}
		{
			getContentPane().add(cajaNombre);
		}
		{
			explicacion = new JLabel();
			getContentPane().add(explicacion);
			explicacion.setText(Lenguaje.text(Lenguaje.NAME));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 17, 67, 21);
			explicacion.setFont(theme.font());
			explicacion.setFocusable(false);
		}
		this.setSize(280, 160);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}
	
	/*
	 * Oyentes de los botones
	 */
	private void botonInsertarActionPerformed(java.awt.event.ActionEvent evt) {                                            
		// Generamos el transfer que mandaremos al controlador
		TransferRelacion tr = new TransferRelacion();
		tr.setPosicion(this.getPosicionRelacion());
		tr.setNombre(this.cajaNombre.getText());
		tr.setListaAtributos(new Vector());
		tr.setListaEntidadesYAridades(new Vector());
		tr.setListaRestricciones(new Vector());
		tr.setListaUniques(new Vector());
		tr.setTipo("Normal");
		// Mandamos mensaje + datos al controlador
		this.getControlador().mensajeDesde_GUI(TC.GUIInsertarRelacion_Click_BotonInsertar, tr);
	}                                           

	private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {                                              
		this.setInactiva();
	}                   
	public void keyPressed( KeyEvent e ) {
		switch (e.getKeyCode()){
			case 27: {
				this.setInactiva();
				break;
			}
			case 10:{
				this.botonInsertarActionPerformed(null);
				break;
			}
		}
	}
	
	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		this.centraEnPantalla();
		this.cajaNombre.setText("");
		SwingUtilities.invokeLater(doFocus);
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
	 * Getters y Setters
	 */

	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}

	public Point2D getPosicionRelacion() {
		return posicionRelacion;
	}

	public void setPosicionRelacion(Point2D posicionRelacion) {
		this.posicionRelacion = posicionRelacion;
	}
	
}
