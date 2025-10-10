package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TransferRelacion;
import vista.componentes.GUIPanels.ReportPanel;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings("serial")
public class GUI_QuitarEntidadPadre extends Parent_GUI {
	private Controlador controlador;
	private TransferRelacion relacion;
	private JLabel pregunta;
	private ReportPanel explicacion;
	private JButton botonNo;
	private JButton botonSi;

	public GUI_QuitarEntidadPadre() {
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(null);
		setTitle(Lenguaje.text(Lenguaje.QUIT_PARENT_ENTITY));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setModal(true);
		setResizable(false);
		this.setSize(400, 300);
		{
			explicacion = new ReportPanel();
			explicacion.setBorder(null);
			getContentPane().add(explicacion);
			explicacion.setEditable(false);
			explicacion.setText("<p>"+Lenguaje.text(Lenguaje.EXPLICATION_QUIT_PARENT)+"</p>");
			explicacion.setBounds(0, 0, 400, 150);
			explicacion.setFont(theme.font());
			explicacion.setOpaque(false);
			explicacion.setFocusable(false);
		}
		{
			pregunta = new JLabel();
			pregunta.setFont(theme.font());
			getContentPane().add(pregunta);
			pregunta.setText(Lenguaje.text(Lenguaje.DO_YOU_WISH_QUIT_PARENT));
			pregunta.setBounds(25, 160, 400, 30);
			pregunta.setOpaque(false);
			pregunta.setFocusable(false);
		}
		{
			botonSi = boton(250, 210,Lenguaje.text(Lenguaje.YES));
			getContentPane().add(botonSi);
			botonSi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonSiActionPerformed(evt);
				}
			});
			botonSi.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonSiActionPerformed(null);}
					else if(e.getKeyCode()==27){botonNoActionPerformed(null);}
					else if(e.getKeyCode()==39){botonNo.grabFocus();}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			botonSi.setMnemonic(Lenguaje.text(Lenguaje.YES).charAt(0));
		}
		{
			botonNo = boton(130, 210,Lenguaje.text(Lenguaje.NO));
			getContentPane().add(botonNo);
			botonNo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonNoActionPerformed(evt);
				}
			});
			botonNo.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonNoActionPerformed(null);}
					else if(e.getKeyCode()==27){botonNoActionPerformed(null);}
					else if(e.getKeyCode()==37){botonSi.grabFocus();}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			botonNo.setMnemonic(Lenguaje.text(Lenguaje.NO).charAt(0));
		}
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	/*
	 * Oyentes de los botones
	 */
	private void botonNoActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
	}

	private void botonSiActionPerformed(java.awt.event.ActionEvent evt) {
		this.controlador.mensajeDesde_GUI(TC.GUIQuitarEntidadPadre_ClickBotonSi, this.getRelacion());
	}

	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		// Si no esta establecida la entidad padre lanzamos el error 
		if(this.getRelacion().getListaEntidadesYAridades().isEmpty())
			JOptionPane.showMessageDialog(
				null, (Lenguaje.text(Lenguaje.ERROR))+"\n" +
				(Lenguaje.text(Lenguaje.IMPOSIBLE_QUIT_PARENT))+"\n" +
				(Lenguaje.text(Lenguaje.NO_FATHER))+"\n",
				Lenguaje.text(Lenguaje.QUIT_PARENT_ENTITY),
				JOptionPane.PLAIN_MESSAGE);
		else{
			this.centraEnPantalla();
			SwingUtilities.invokeLater(doFocus);
			this.setVisible(true);
		}	
	}
	
	private Runnable doFocus = new Runnable() {
	     public void run() {
	        botonSi.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}
	
	public void keyPressed( KeyEvent e ) {
		switch (e.getKeyCode()){
			case 27: {
				this.setInactiva();
				break;
			}
			case 10:{
				this.botonSiActionPerformed(null);
				break;
			}
		}
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

	public TransferRelacion getRelacion() {
		return relacion;
	}

	public void setRelacion(TransferRelacion relacion) {
		this.relacion = relacion;
	}
}