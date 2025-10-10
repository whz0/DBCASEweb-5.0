package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TipoDominio;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferDominio;
import vista.componentes.MyComboBoxRenderer;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings({"rawtypes" ,"unchecked", "serial"})
public class GUI_EditarDominioAtributo extends Parent_GUI {

	private Controlador controlador;
	private TransferAtributo atributo;
	private JComboBox comboDominios;
	private JLabel labelTamano;
	private JTextField cajaTamano;
	private JButton botonEditar;
	private Vector<TransferDominio> listaDominios;

	public GUI_EditarDominioAtributo() {
		initComponents();
	}

	private void initComponents() {
		setTitle(Lenguaje.text(Lenguaje.EDIT_DOMAIN_ATTRIBUTE));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		getContentPane().setLayout(null);
		getContentPane().add(getBotonEditar());
		getContentPane().add(getComboDominios());
		getContentPane().add(getCajaTamano());
		getContentPane().add(getLabelTamano());
		this.setSize(300, 200);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		controlador.mensajeDesde_GUI(TC.GUIEditarDominioAtributo_ActualizameLaListaDeDominios, null);
		Object[] nuevos = new Object[this.listaDominios.size()];
		this.generaItems(nuevos);
		String[] s = atributo.getDominio().split("\\(");
		this.comboDominios.setModel(new javax.swing.DefaultComboBoxModel(nuevos));
		if(s[0]!=null) {
			this.comboDominios.setSelectedItem(s[0]);
			if (this.activarTamano()){
				this.cajaTamano.setEditable(true);
				if(s[1]!=null) this.cajaTamano.setText(s[1].split("\\)")[0]);
			}
			else {
				this.labelTamano.setText("");
				this.cajaTamano.setEditable(false);
			}
		}
		this.centraEnPantalla();
		
		// Si el atributo es compuesto, su dominio es null y no se puede editar
		if (this.getAtributo().getCompuesto()){
			JOptionPane.showMessageDialog(
					null,(Lenguaje.text(Lenguaje.ERROR))+"\n" +
					(Lenguaje.text(Lenguaje.IMPOSIBLE_EDIT_DOMAIN))+"\""+this.getAtributo().getNombre()+"\"\n" +
					(Lenguaje.text(Lenguaje.COMPLEX_ATTRIBUTE))+"\n",
					(Lenguaje.text(Lenguaje.EDIT_DOMAIN_ATTRIBUTE)),
					JOptionPane.PLAIN_MESSAGE);
			return;
		}
		// Si se puede, ponemos el dominio que tiene en el combo y tamano en caso de tenerlo
		String dominio = this.getAtributo().getDominio();
		
		// Si el dominio es null es que antes era un atributo compuesta y ahora ya no lo es.
		// Por defecto seleccionamos el tipo INTEGER
		if (dominio.equals("null")){
			this.comboDominios.setSelectedItem(TipoDominio.INTEGER);
			this.cajaTamano.setText("");
			this.labelTamano.setText("");
			this.cajaTamano.setEnabled(false);
			this.cajaTamano.setEditable(false);
		}
		else{
			try{// Si el dominio tiene un parentesis al final es que tiene tamano
				if(dominio.charAt(dominio.length()-1)==')'){
					String tipo = dominio.substring(0,dominio.indexOf("("));
					String tam = dominio.substring(dominio.indexOf("(")+1,dominio.indexOf(")"));
					this.comboDominios.setSelectedItem(TipoDominio.valueOf(tipo));
					this.cajaTamano.setText(tam);
					this.cajaTamano.setEnabled(true);
					this.cajaTamano.setEditable(true);
				}
				// Si es un dominio simple
				else{
					this.comboDominios.setSelectedItem(TipoDominio.valueOf(dominio));
					this.labelTamano.setText("");
					this.cajaTamano.setText("");
					this.cajaTamano.setEnabled(false);
					this.cajaTamano.setEditable(false);
				}
			}catch(Exception e){
				this.comboDominios.setSelectedItem(dominio);
			}
		}
		this.centraEnPantalla();
		boolean b= this.activarTamano();
		this.cajaTamano.setEditable(b);
		this.cajaTamano.setEnabled(b);
		SwingUtilities.invokeLater(doFocus);
		this.setVisible(true);
	}

	private Runnable doFocus = new Runnable() {
	     public void run() {
	        comboDominios.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}

	/*
	 * Oyentes de los botones
	 */
	private void botonEditarActionPerformed(java.awt.event.ActionEvent evt) {
		String dominioCadena;
		String nuevoTamano = "";
		try{
			// Obtenemos el nuevo dominio
			TipoDominio nuevoDominio = TipoDominio.valueOf(this.comboDominios.getSelectedItem().toString());
			dominioCadena = nuevoDominio.toString();
			
			// Si es dominio con tamano
			if (((TipoDominio)nuevoDominio).equals(TipoDominio.CHAR) ||
				((TipoDominio)nuevoDominio).equals(TipoDominio.VARCHAR) ||
				((TipoDominio)nuevoDominio).equals(TipoDominio.TEXT)){
					// Obtenemos el tamano de la caja
					nuevoTamano = this.cajaTamano.getText();
					// Si no se ha especificado tamano ponemos un 1 por defecto
					if (nuevoTamano.isEmpty()) nuevoTamano = "10";
					// Formamos la cadena
					dominioCadena += "("+nuevoTamano+")";
			}
		}catch(Exception e){ //Dominio definido por el usuario
			dominioCadena = this.comboDominios.getSelectedItem().toString();
		}
		// Mandamos la entidad, el nuevo atributo y si hay tamano tambien
		Vector<Object> v = new Vector<Object>();
		v.add(this.getAtributo());
		v.add(dominioCadena);
		if(!nuevoTamano.isEmpty()) v.add(nuevoTamano);
		controlador.mensajeDesde_GUI(TC.GUIEditarDominioAtributo_Click_BotonEditar, v);	
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
	
	//Oyente para todos los elementos
	private KeyListener general = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==10) botonEditarActionPerformed(null);
			if(e.getKeyCode()==27) botonCancelarActionPerformed(null);
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	/*
	 * Oyente del cambio en el comboDominios
	 */
	private void comboDominiosItemStateChanged(java.awt.event.ItemEvent evt) {                                               
		if (this.activarTamano()){
			this.cajaTamano.setText("");
			this.labelTamano.setText(Lenguaje.text(Lenguaje.SIZE_ATTRIBUTE));
			this.cajaTamano.setEnabled(true);
			this.cajaTamano.setEditable(true);
		}
		else{
			this.labelTamano.setText("");
			this.cajaTamano.setText("");
			this.cajaTamano.setEnabled(false);
			this.cajaTamano.setEditable(false);
		}
	}
	
	/*
	 * Metodos privados
	 */
	private boolean activarTamano(){
		boolean activo = false;
		try{
			TipoDominio dominio = TipoDominio.valueOf(this.comboDominios.getSelectedItem().toString());
			if (((TipoDominio)dominio).equals(TipoDominio.CHAR) ||
					((TipoDominio)dominio).equals(TipoDominio.VARCHAR) ||
					((TipoDominio)dominio).equals(TipoDominio.TEXT))
					activo = true;
			return activo;
		}catch(Exception e){
			return false;
		}
	}

	private JButton getBotonEditar() {
		if(botonEditar == null) {
			botonEditar = boton(200, 120,Lenguaje.text(Lenguaje.ACCEPT));
			botonEditar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonEditarActionPerformed(evt);
				}
			});
			botonEditar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10) botonEditarActionPerformed(null);
					else if(e.getKeyCode()==27) botonCancelarActionPerformed(null);
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			botonEditar.setMnemonic(Lenguaje.text(Lenguaje.ACCEPT).charAt(0));
		}
		return botonEditar;
	}

	private JComboBox getComboDominios() {
		if(comboDominios == null) {
			comboDominios = new JComboBox();
			comboDominios.setRenderer(new MyComboBoxRenderer());
			comboDominios.setBounds(25, 25, 228, 25);
			comboDominios.setFont(theme.font());
			comboDominios.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					comboDominiosItemStateChanged(evt);
				}
			});
			comboDominios.addKeyListener(general);
		}
		return comboDominios;
	}

	private JTextField getCajaTamano() {
		if(cajaTamano == null) {
			cajaTamano = new JTextField();
			cajaTamano.setFont(theme.font());
			cajaTamano.setForeground(theme.labelFontColorDark());
			cajaTamano.setBounds(120, 75, 130, 25);
		}
		cajaTamano.addKeyListener(general);
		return cajaTamano;
	}

	private JLabel getLabelTamano() {
		if(labelTamano == null) {
			labelTamano = new JLabel();
			labelTamano.setFont(theme.font());
			labelTamano.setText(Lenguaje.text(Lenguaje.SIZE_ATTRIBUTE));
			labelTamano.setBounds(25, 75, 140, 25);
		}
		return labelTamano;
	}

	private Object[] generaItems(Object[] items){
			// Generamos los items
			int cont = 0;
			while (cont<this.listaDominios.size()){
				TransferDominio td = this.listaDominios.get(cont);
				items[cont] = td.getNombre();
				cont++;
			}
			return items;
	}
	
	public Vector<TransferDominio> getListaDominios() {
		return listaDominios;
	}
	
	public void setListaDominios(Vector<TransferDominio> listaDominios) {
		this.listaDominios = listaDominios;
	}

	/*
	 * Getters y Setters
	 */
	public TransferAtributo getAtributo() {
		return atributo;
	}

	public void setAtributo(TransferAtributo atributo) {
		this.atributo = atributo;
	}

	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}      
}
