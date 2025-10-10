
package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JTextPane;

import vista.frames.Parent_GUI;
import vista.lenguaje.Lenguaje;

@SuppressWarnings("serial")
public class GUI_Pregunta extends Parent_GUI {

	private int respuesta;
	private JTextPane pregunta;
	private JButton botonNo;
	private JButton botonSi;
	private JButton botonCancelar;
	
	public GUI_Pregunta(){
		this.initComponents();
	}
		
	private void initComponents() {
		getContentPane().setLayout(null);
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setModal(true);
		setResizable(false);
		this.setSize(300, 160);
		{
			pregunta = new JTextPane();
			getContentPane().add(pregunta);
			pregunta.setForeground(theme.labelFontColorDark());
			pregunta.setFont(theme.font());
			pregunta.setBounds(10, 10, 280, 50);
			pregunta.setEditable(false);
			pregunta.setOpaque(false);
			pregunta.setFocusable(false);
		}
		{
			botonSi = boton(230, 90,Lenguaje.text(Lenguaje.YES));
			getContentPane().add(botonSi);
			botonSi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonSiActionPerformed(evt);
				}

			});
			botonSi.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10)botonSiActionPerformed(null);
					else if(e.getKeyCode()==27) botonNoActionPerformed(null);
					else if(e.getKeyCode()==39) botonNo.grabFocus();
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		{
			botonNo = boton(170, 90,Lenguaje.text(Lenguaje.NO));
			getContentPane().add(botonNo);
			botonNo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonNoActionPerformed(evt);
				}
			});
			botonNo.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonNoActionPerformed(null);}
					else if(e.getKeyCode()==27)botonNoActionPerformed(null);
					else if(e.getKeyCode()==37)botonSi.grabFocus();
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		{
			botonCancelar = boton(10, 90,Lenguaje.text(Lenguaje.CANCEL));
			getContentPane().add(botonCancelar);
			botonCancelar.setVisible(false);
			botonCancelar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonCancelarActionPerformed(evt);
				}
			});
			botonCancelar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonCancelarActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
					else if(e.getKeyCode()==37){botonNo.grabFocus();}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		this.addKeyListener(this);
	}

	public int setActiva(String mensaje, String titulo){
		return setActiva(mensaje, titulo, false);
	}
	public int setActiva(String mensaje, String titulo, boolean cancelar){
		pregunta.setText(mensaje);
		setTitle(titulo);
		botonCancelar.setVisible(cancelar);
		
		respuesta =-1;
		this.centraEnPantalla();
		this.setVisible(true);
		return respuesta;
	}
	
	public void setInactiva(){
		this.setVisible(false);
	}
	
	private void botonSiActionPerformed(ActionEvent evt) {
		respuesta = 0;
		setInactiva();
	}
	
	private void botonNoActionPerformed(ActionEvent evt) {
		respuesta = 1;
		setInactiva();
	}
	
	private void botonCancelarActionPerformed(ActionEvent evt) {
		respuesta = 2;
		setInactiva();
	}
}