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
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import persistencia.EntidadYAridad;
import vista.componentes.MyComboBoxRenderer;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings({ "rawtypes", "serial" , "unchecked"})
public class GUI_QuitarEntidadARelacion extends Parent_GUI {

	private Controlador controlador;
	private Vector<TransferEntidad> listaEntidades;
	private TransferRelacion relacion;
	private JComboBox comboEntidades;
	private JComboBox comboRoles;
	private JLabel explicacionRol;
	private JButton botonQuitar;
	private JLabel explicacion;
	private Vector<String> items;
	private Vector<String> itemsRoles;

	public GUI_QuitarEntidadARelacion() {
		initComponents();
	}

	private void initComponents() {
		setTitle(Lenguaje.text(Lenguaje.QUIT_ENTITY));
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setResizable(false);
		setModal(true);
		getContentPane().setLayout(null);
		getContentPane().add(getExplicacion());
		getContentPane().add(getComboEntidades());
		getContentPane().add(getBotonQuitar());
		getContentPane().add(getExplicacionRol());
		getContentPane().add(getComboRoles());
		this.setSize(300, 220);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}


	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		// Si no tiene entidades
		if(this.getRelacion().getListaEntidadesYAridades().isEmpty())
			JOptionPane.showMessageDialog(
				null,
				(Lenguaje.text(Lenguaje.ERROR))+"\n" +
				(Lenguaje.text(Lenguaje.IMPOSIBLE_QUIT_ENTITY))+"\n" +
				(Lenguaje.text(Lenguaje.NO_ENTITIES_IN_RELATION))+"\n",
				Lenguaje.text(Lenguaje.QUIT_ENTITY),
				JOptionPane.PLAIN_MESSAGE);
		else{
			this.controlador.mensajeDesde_GUI(TC.GUIQuitarEntidadARelacion_ActualizameListaEntidades, null);
			// Generamos los items (ya filtrados)
			this.items = this.generaItems();
			comboEntidades.setModel(new javax.swing.DefaultComboBoxModel(this.items));
			comboEntidades.setSelectedIndex(0);
			comboEntidades.grabFocus();
			this.itemsRoles = this.generaItemsRoles();
			comboRoles.setModel(new javax.swing.DefaultComboBoxModel(this.itemsRoles));
			SwingUtilities.invokeLater(doFocus);
			this.centraEnPantalla();
			this.setVisible(true);	
		}
	}


	private JLabel getExplicacion() {
		if(explicacion == null) {
			explicacion = new JLabel();
			explicacion.setFont(theme.font());
			explicacion.setText((Lenguaje.text(Lenguaje.SELECT_ENTITY_TO_QUIT)));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 12, 238, 25);
			explicacion.setFocusable(false);
		}
		return explicacion;
	}
		
	private JComboBox getComboEntidades() {
		if(comboEntidades == null) {
			comboEntidades = new JComboBox();
			comboEntidades.setRenderer(new MyComboBoxRenderer());
			comboEntidades.setFont(theme.font());
			comboEntidades.setBounds(25, 40, 238, 25);
			comboEntidades.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					comboEntidadesItemStateChanged(evt);
				}
			});
		}
		return comboEntidades;
	}
	
	private JButton getBotonQuitar() {
		if(botonQuitar == null) {
			botonQuitar = boton(160, 150,Lenguaje.text(Lenguaje.REMOVE));
			botonQuitar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonQuitarActionPerformed(evt);
				}
			});
			botonQuitar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10) botonQuitarActionPerformed(null);
					else if(e.getKeyCode()==27) botonCancelarActionPerformed(null);
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return botonQuitar;
	}

	private JLabel getExplicacionRol() {
		if(explicacionRol == null) {
			explicacionRol = new JLabel();
			explicacionRol.setFont(theme.font());
			explicacionRol.setText(Lenguaje.text(Lenguaje.IF_ENTITY_HAS_ROLLE));
			explicacionRol.setBounds(25, 67, 238, 25);
			explicacionRol.setOpaque(false);
			explicacionRol.setFocusable(false);
		}
		return explicacionRol;
	}
	
	private JComboBox getComboRoles() {
		if(comboRoles == null) {
			comboRoles = new JComboBox();
			comboRoles.setRenderer(new MyComboBoxRenderer());
			comboRoles.setFont(theme.font());
			comboRoles.setBounds(25, 95, 238, 25);
		}
		return comboRoles;
	}
	
	private Runnable doFocus = new Runnable() {
	     public void run() {
	    	 comboEntidades.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}

	//Genera los items de las entidades que están asociadas a la relación
	private Vector<String> generaItems(){
		// Filtramos la lista de entidades quitando las entidades que no intervienen
		Vector<EntidadYAridad> vectorTupla = this.getRelacion().getListaEntidadesYAridades();
		Vector vectorIdsEntidades = new Vector();
		int cont = 0; // Para saltar la entidad padre
		while(cont<vectorTupla.size()){
			vectorIdsEntidades.add((vectorTupla.get(cont)).getEntidad());
			cont++;
		}
		cont = 0;
		Vector<TransferEntidad> listaEntidadesFiltrada = new Vector<TransferEntidad>();
		while(cont<this.getListaEntidades().size()){
			TransferEntidad te = this.getListaEntidades().get(cont);
			if(vectorIdsEntidades.contains(te.getIdEntidad())){
				listaEntidadesFiltrada.add(te);
			}
			cont++;
		}		
		this.setListaEntidades(listaEntidadesFiltrada);
		// Generamos los items
		cont = 0;
		Vector<String> items = new Vector<String>();;
		while (cont<this.listaEntidades.size()){
			TransferEntidad te = this.listaEntidades.get(cont);
			items.add(cont,te.getNombre());
			cont++;
		}
		return items;
	}
	
	//Genera los roles asociados a la entidad que esté seleccionada
	private Vector<String> generaItemsRoles(){
		Vector<String> v = new Vector<String>();
		int itemSeleccionado = this.comboEntidades.getSelectedIndex();
		TransferEntidad te = this.listaEntidades.get(indiceAsociado(itemSeleccionado));
		int idEntidad = te.getIdEntidad();
		Vector veya = this.relacion.getListaEntidadesYAridades();
		int cont = 0;
		while(cont<veya.size()){
			EntidadYAridad eya = (EntidadYAridad) veya.get(cont);
			if(eya.getEntidad()==idEntidad){
				v.add(eya.getRol());
			}
			cont++;
		}
		return v;
	}
	
	

	/*
	 * Oyentes de los botones
	 */

	private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
	}                                          

	private void botonQuitarActionPerformed(java.awt.event.ActionEvent evt) { 
		int numRol = comboRoles.getSelectedIndex();
		this.itemsRoles = this.generaItemsRoles();
		String rol = this.itemsRoles.get(numRol);
		Vector<Object> v = new Vector<Object>();
		//Añadimos la relación
		v.add(this.getRelacion());
		//Añadimos la entidad-> Como están ordenadas alfabéticamente hay que calcular el indice asociado
		v.add(this.listaEntidades.get(indiceAsociado(this.comboEntidades.getSelectedIndex())));
		v.add(rol);
		//Mandamos el mensaje al controlador con los datos
		this.controlador.mensajeDesde_GUI(TC.GUIQuitarEntidadARelacion_ClickBotonQuitar, v);
		
		
	}
	
	private void comboEntidadesItemStateChanged(java.awt.event.ItemEvent evt) {
		Vector<String> itemsRoles = generaItemsRoles();
		comboRoles.setModel(new javax.swing.DefaultComboBoxModel(itemsRoles));
		comboRoles.grabFocus();		
	}
	
	public void keyPressed( KeyEvent e ) {
		switch (e.getKeyCode()){
			case 27: {
				this.setInactiva();
				break;
			}
			case 10:{
				this.botonQuitarActionPerformed(null);
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
	
	/*Dada la posición seleccionada en el comboBox devuelve el índice correspondiente a dicho 
	 * elementeo en la lista de Entidades. Es necesario porque al ordenar alfabeticamente se perdió 
	 * la correspondencia.*/
	private int indiceAsociado (int selec){
		boolean encontrado= false;
		int i=0;
		while ((i<this.listaEntidades.size())&& (!encontrado)){
			if((this.items.get(selec)).equals(this.listaEntidades.get(i).getNombre())){
				encontrado =true;
				return i;
			}
			else i++;
		}
		return i;
	}
}

