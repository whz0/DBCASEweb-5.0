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

@SuppressWarnings({"rawtypes" ,"unchecked", "serial"})
public class GUI_InsertarDominio extends Parent_GUI {
	private Controlador controlador;
	private JTextField cajaNombre = this.getCajaNombre(25, 40);
	private JTextField cajaValores;
	private JComboBox comboTipo;
	private JLabel explicacion;
	private JLabel textType;
	private JLabel textValues;
	private JButton botonInsertar;

	public GUI_InsertarDominio() {
		initComponents();
	}

	private void initComponents() {
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(Lenguaje.text(Lenguaje.ADD_DOMAIN));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setResizable(false);
		setModal(true);
		getContentPane().setLayout(null);
		setSize(300, 250);
		cajaNombre.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==10){botonInsertarActionPerformed(null);}
				else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		{
			botonInsertar = boton(120,180,Lenguaje.text(Lenguaje.ADD_DOMAIN));
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
		}
		{
			explicacion = new JLabel();
			getContentPane().add(explicacion);
			explicacion.setFont(theme.font());
			explicacion.setText(Lenguaje.text(Lenguaje.NAME));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 10, 348, 25);
			explicacion.setFocusable(false);
		}
		{
			textType = new JLabel();
			getContentPane().add(textType);
			textType.setFont(theme.font());
			textType.setText(Lenguaje.text(Lenguaje.TYPE));
			textType.setOpaque(false);
			textType.setBounds(25, 65, 348, 25);
			textType.setFocusable(false);
		}
		{
			textValues = new JLabel();
			getContentPane().add(textValues);
			textValues.setFont(theme.font());
			textValues.setText(Lenguaje.text(Lenguaje.VALUES));
			textValues.setOpaque(false);
			textValues.setBounds(25, 115, 348, 25);
			textValues.setFocusable(false);
		}
		{
			getContentPane().add(cajaNombre);
		}
		{
			comboTipo = new JComboBox();
			comboTipo.setRenderer(new MyComboBoxRenderer());
			getContentPane().add(comboTipo);
			comboTipo.setFont(theme.font());
			comboTipo.setBounds(25, 90, 236, 25);
			//Creamos lista de tipos básicos
			Object[] items = modelo.transfers.TipoDominio.values();
			Object[] items2 = new Object[items.length-1];
			//quitamos BLOB
			int i=0;
			while (i<items.length && !items[i].toString().equals("BLOB")){ 
				items2[i]=items[i];
				i++;
			}
			for (int j=i+1; j<items.length;j++) items2[j-1]=items[j];
			comboTipo.setModel(new javax.swing.DefaultComboBoxModel(items2));
			comboTipo.addKeyListener(general);
		}
		{
			cajaValores = new JTextField();
			getContentPane().add(cajaValores);
			cajaValores.setFont(theme.font());
			cajaValores.setForeground(theme.labelFontColorDark());
			cajaValores.setBounds(25, 140, 236, 25);
			cajaValores.addKeyListener(general);
		}
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	/*
	 * Oyentes de los botones 
	 */
	private void botonInsertarActionPerformed(java.awt.event.ActionEvent evt) {                                            
		// Generamos el transfer que mandaremos al controlador
		TransferDominio td = new TransferDominio();
		String name = this.cajaNombre.getText().replace("(", "");
		td.setNombre(name.replace(")", ""));
		td.setTipoBase((TipoDominio)this.comboTipo.getSelectedItem());
		td.setListaValores(listaValores());
		// Mandamos mensaje + datos al controlador
		this.getControlador().mensajeDesde_GUI(TC.GUIInsertarDominio_Click_BotonInsertar, td);
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
	
	//Oyente para todos los elementos
	private KeyListener general = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==10){botonInsertarActionPerformed(null);}
			if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
		
	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		this.centraEnPantalla();
		this.cajaNombre.setText("");
		this.cajaValores.setText("");
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
	 * Utilidades
	 */
	//TODO Nota: deberiamos controlar repeticiones y más casos raros
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
				if (segundaComilla!= -1){
				subS = subS.substring(primeraComilla, segundaComilla+1);
				}
			}else{
				subS = subS.replaceAll(" ","");
				subS = subS.replaceAll(",","");
			}
			v.add(subS);
		}
		return v;
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
}
