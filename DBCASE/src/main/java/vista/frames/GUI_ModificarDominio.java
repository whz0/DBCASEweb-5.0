package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TipoDominio;
import modelo.transfers.TransferDominio;
import vista.componentes.MyComboBoxRenderer;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings({ "rawtypes", "serial" , "unchecked"})
public class GUI_ModificarDominio extends Parent_GUI {

	private Controlador controlador;
	private TransferDominio dominio;
	private JTextField cajaValores;
	private JLabel explicacion;
	private JLabel textoTipo;
	private JComboBox comboDominios;
	private JButton botonEditar;
	
	public GUI_ModificarDominio() {
		initComponents();
	}

	private void initComponents() {
		setTitle(Lenguaje.text(Lenguaje.EDIT_DOMAIN));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		getContentPane().setLayout(null);
		getContentPane().add(getBotonEditar());
		getContentPane().add(getCajaValores());
		getContentPane().add(getComboDominios());
		getContentPane().add(getExplicacion());
		getContentPane().add(getTextoTipo());
		this.setSize(300, 250);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		this.cajaValores.setText(this.getValores());
		this.actualizaComboDominios();		
		this.centraEnPantalla();
		SwingUtilities.invokeLater(doFocus);
		this.setVisible(true);
	}

	/*
	 * Metodos privados
	 */
	private JButton getBotonEditar() {
		if(botonEditar == null) {
			botonEditar = boton(180, 170,Lenguaje.text(Lenguaje.ACCEPT));
			botonEditar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonEditarActionPerformed(evt);
				}
			});
			botonEditar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonEditarActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			botonEditar.setMnemonic(Lenguaje.text(Lenguaje.ACCEPT).charAt(0));
		}
		return botonEditar;
	}

	private String getValores(){
		String valores = "";
		for (int i=0;i< dominio.getListaValores().size();i++){
			valores =valores.concat((String)dominio.getListaValores().get(i));
			if(i<dominio.getListaValores().size()-1) valores = valores.concat(", ");
		} 
		return valores;
	}
	
	private JTextField getCajaValores() {
		if(cajaValores == null) {
			cajaValores = new JTextField();
			cajaValores.setFont(theme.font());
			cajaValores.setForeground(theme.labelFontColorDark());
			getContentPane().add(cajaValores);
			cajaValores.setBounds(25, 110, 236, 30);
			cajaValores.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonEditarActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return cajaValores;
	}

	private JLabel getExplicacion() {
		if(explicacion == null) {
			explicacion = new JLabel();
			explicacion.setFont(theme.font());
			explicacion.setText(Lenguaje.text(Lenguaje.VALUES));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 80, 353, 25);
			explicacion.setFocusable(false);
		}
		return explicacion;
	}
	
	private JLabel getTextoTipo() {
		if(textoTipo == null) {
			textoTipo = new JLabel();
			textoTipo.setFont(theme.font());
			textoTipo.setText(Lenguaje.text(Lenguaje.TYPE));
			textoTipo.setOpaque(false);
			textoTipo.setBounds(25, 10, 353, 25);
			textoTipo.setFocusable(false);
		}
		return textoTipo;
	}

	
	private JComboBox getComboDominios() {
		if(comboDominios == null) {
			comboDominios = new JComboBox();
			comboDominios.setRenderer(new MyComboBoxRenderer());
			comboDominios.setFont(theme.font());
			comboDominios.setBounds(25, 40, 236, 25);
		}
		return comboDominios;
	}

	/*
	 * Utilidades
	 */
	private void actualizaComboDominios() {
		Object[] items = modelo.transfers.TipoDominio.values();
		Object[] items2 = new Object[items.length-1];
		//quitamos BLOB
		int i=0;
		while (i<items.length && !items[i].toString().equals("BLOB")){ 
			items2[i]=items[i];
			i++;
		}
		for (int j=i+1; j<items.length;j++)
			items2[j-1]=items[j];
		this.comboDominios.setModel(new javax.swing.DefaultComboBoxModel(items2));
		// Si se puede, ponemos el dominio que tiene en el combo y tamano en caso de tenerlo
		String dominio = this.getDominio().getTipoBase().toString();
		// Si el dominio es null por defecto seleccionamos el tipo INTEGER
		if (dominio.equals("null")) this.comboDominios.setSelectedItem(TipoDominio.INTEGER);
		else this.comboDominios.setSelectedItem(TipoDominio.valueOf(dominio));
	}
	
	private Vector listaValores(){
		Vector v = new Vector();
		String s = this.cajaValores.getText();
		int pos0 = 0;
		int comilla1 = s.indexOf("'");
		int comilla2 = s.indexOf("'", comilla1+1);
		int pos1;
		
		if (comilla2!= -1) pos1 = s.indexOf(",", comilla2);
		else pos1 = s.indexOf(",");
		
		while(pos0 != -1 ){
			String subS;
			if (pos1 !=-1) subS = s.substring(pos0, pos1);
			else subS = s.substring(pos0, s.length());
			
			pos0 = pos1;
			pos1 = s.indexOf (",",pos1+1);
			if (subS.contains("'")){
				//eliminamos todos lo que este fuera de las comillas
				int primeraComilla = subS.indexOf("'");
				int segundaComilla= subS.indexOf("'", primeraComilla+1);
				if (segundaComilla!= -1)
					subS = subS.substring(primeraComilla, segundaComilla+1);
			}else{
				subS = subS.replaceAll(" ","");
				subS = subS.replaceAll(",","");
			}
			v.add(subS);
		}
		return v;
	}

	private Runnable doFocus = new Runnable() {
	     public void run() {
	         cajaValores.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}

	/*
	 * Oyentes de los botones
	 */
	private void botonEditarActionPerformed(java.awt.event.ActionEvent evt) {
		// Obtenemos los nuevos valores
		Vector nuevosValores = this.listaValores();
		TipoDominio nuevoDominio = (TipoDominio) this.comboDominios.getSelectedItem();
		
		// Mandamos el dominio, con sus nuevos valores
		Vector<Object> v = new Vector<Object>();
		v.add(this.getDominio());
		v.add(nuevosValores);
		v.add(nuevoDominio);
		
		controlador.mensajeDesde_GUI(TC.GUIModificarDominio_Click_BotonEditar, v);
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
				this.botonEditarActionPerformed(null);
				break;
			}
		}
	} 
	
	/*
	 * Getters y Setters
	 */
	public TransferDominio getDominio() {
		return dominio;
	}

	public void setDominio(TransferDominio dominio) {
		this.dominio = dominio;
		setTitle(Lenguaje.text(Lenguaje.EDIT)+" "+dominio);
	}

	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}      
}
