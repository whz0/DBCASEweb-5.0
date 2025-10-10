package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.Transfer;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import persistencia.EntidadYAridad;
import vista.componentes.MyComboBoxRenderer;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings({ "rawtypes", "serial" , "unchecked"})
public class GUI_QuitarEntidadHija extends Parent_GUI {

	private Controlador controlador;
	private Vector<TransferEntidad> listaEntidades;
	private TransferRelacion relacion;
	private JComboBox comboEntidades;
	private JButton botonQuitar;
	private JLabel explicacion;

	public GUI_QuitarEntidadHija() {
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(null);
		setTitle(Lenguaje.text(Lenguaje.QUIT_DAUGHTER_ENTITY));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setModal(true);
		this.setResizable(false);
		getContentPane().add(getExplicacion());
		getContentPane().add(getBotonQuitar());
		getContentPane().add(getComboEntidades());
		this.setSize(300, 170);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	/*
	 * Oyentes de los botones
	 */
	private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {                                           
		this.setVisible(false);
	}                                          

	private void botonQuitarActionPerformed(java.awt.event.ActionEvent evt) {
		// Obtenemos la entidad seleccionada
		TransferEntidad te = this.listaEntidades.get(this.comboEntidades.getSelectedIndex());
		// Generamos los datos que enviaremos al controlador
		Vector<Transfer> datos = new Vector<Transfer>();
		datos.add(this.getRelacion());
		datos.add(te);
		// Mandamos el mensaje al controlador con los datos
		this.controlador.mensajeDesde_GUI(TC.GUIQuitarEntidadHija_ClickBotonQuitar, datos);
	}
	
	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		// Si no tiene entidades hijas lanzamos un error
		if(this.getRelacion().getListaEntidadesYAridades().size()<=1)
			JOptionPane.showMessageDialog(
					null,
					(Lenguaje.text(Lenguaje.ERROR))+"\n" +
					(Lenguaje.text(Lenguaje.IMPOSIBLE_QUIT_DAUGHTER_ENTITY))+"\n" +
					(Lenguaje.text(Lenguaje.NO_DAUGHTER_ENTITY))+"\n",
					Lenguaje.text(Lenguaje.QUIT_DAUGHTER_ENTITY),
					JOptionPane.PLAIN_MESSAGE);
		else{
			this.controlador.mensajeDesde_GUI(TC.GUIQuitarEntidadHija_ActualizameListaEntidades, null);
			// Generamos los items (ya filtrados)
			String[] items = this.generaItems();
			comboEntidades.setModel(new javax.swing.DefaultComboBoxModel(items));
			comboEntidades.setSelectedIndex(0);
			SwingUtilities.invokeLater(doFocus);
			this.centraEnPantalla();
			this.setVisible(true);	
		}
	}

	private Runnable doFocus = new Runnable() {
	     public void run() {
	         comboEntidades.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}

	/*
	 * Metodos privados del dialogo
	 */
	private String[] generaItems(){
		// Filtramos la lista de entidades quitando la entidad padre y las entidades que no intervienen
		Vector<EntidadYAridad> vectorTupla = this.getRelacion().getListaEntidadesYAridades();
		Vector vectorIdsEntidades = new Vector();
		int cont = 1; // Para saltar la entidad padre
		while(cont<vectorTupla.size()){
			vectorIdsEntidades.add((vectorTupla.get(cont)).getEntidad());
			cont++;
		}
		cont = 0;
		Vector<TransferEntidad> listaEntidadesFiltrada = new Vector<TransferEntidad>();
		while(cont<this.getListaEntidades().size()){
			TransferEntidad te = this.getListaEntidades().get(cont);
			if(vectorIdsEntidades.contains(te.getIdEntidad()))
				listaEntidadesFiltrada.add(te);
			cont++;
		}
		this.setListaEntidades(listaEntidadesFiltrada);
		// Generamos los items
		cont = 0;
		String[] items = new String[this.listaEntidades.size()];
		while (cont<this.listaEntidades.size()){
			TransferEntidad te = this.listaEntidades.get(cont);
			items[cont] = te.getNombre();
			cont++;
		}
		return items;
	}

	/*
	 * Intefaz
	 */
	private JLabel getExplicacion() {
		if(explicacion == null) {
			explicacion = new JLabel();
			explicacion.setFont(theme.font());
			explicacion.setText(Lenguaje.text(Lenguaje.SELECT_DAUGHTER_TO_QUIT));
			explicacion.setBounds(25, 12, 235, 25);
			explicacion.setOpaque(false);
			explicacion.setFocusable(false);
		}
		return explicacion;
	}

	private JButton getBotonQuitar() {
		if(botonQuitar == null) {
			botonQuitar = boton(150, 90,Lenguaje.text(Lenguaje.REMOVE));
			botonQuitar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonQuitarActionPerformed(evt);
				}
			});
			botonQuitar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonQuitarActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			botonQuitar.setMnemonic(Lenguaje.text(Lenguaje.REMOVE).charAt(0));
		}
		return botonQuitar;
	}

	private JComboBox getComboEntidades() {
		if(comboEntidades == null) {
			ComboBoxModel comboEntidadesModel = new DefaultComboBoxModel(new String[] { "Item One", "Item Two" });
			comboEntidades = new JComboBox();
			comboEntidades.setRenderer(new MyComboBoxRenderer());
			comboEntidades.setFont(theme.font());
			comboEntidades.setModel(comboEntidadesModel);
			comboEntidades.setBounds(25, 38, 230, 25);
		}
		return comboEntidades;
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
}
