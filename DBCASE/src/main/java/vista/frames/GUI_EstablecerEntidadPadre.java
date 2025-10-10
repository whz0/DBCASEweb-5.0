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
import vista.componentes.MyComboBoxRenderer;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;


@SuppressWarnings({"rawtypes" ,"unchecked", "serial"})
public class GUI_EstablecerEntidadPadre extends Parent_GUI{

	private Controlador controlador;
	private Vector<TransferEntidad> listaEntidades;
	private JComboBox comboEntidades;
	private JLabel textoExplicacion;
	private JButton botonInsertar;
	private TransferRelacion relacion;

	public GUI_EstablecerEntidadPadre() {
		initComponents();
	}

	private void initComponents() {
		this.setTitle((Lenguaje.text(Lenguaje.SET_PARENT_ENTITY)));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		getContentPane().setLayout(null);
		setModal(true);
		this.setResizable(false);
		getContentPane().add(getBotonInsertar());
		getContentPane().add(getComboEntidades());
		getContentPane().add(getTextoExplicacion());
		this.setSize(300,180);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		// Si ya tiene establecida la entidad padre, lanzamos un error
		if(!this.getRelacion().getListaEntidadesYAridades().isEmpty())
			JOptionPane.showMessageDialog(
					null, (Lenguaje.text(Lenguaje.ERROR))+"\n" +
					(Lenguaje.text(Lenguaje.IMPOSIBLE_SET_PARENT))+"\n" +
					(Lenguaje.text(Lenguaje.OTHER_PARENT))+"\n",
					Lenguaje.text(Lenguaje.SET_PARENT_ENTITY),
					JOptionPane.PLAIN_MESSAGE);
		else{
			this.controlador.mensajeDesde_GUI(TC.GUIEstablecerEntidadPadre_ActualizameListaEntidades, null);
			// Generamos los items
			String[] items = this.generaItems();
			if(items.length == 0)
				JOptionPane.showMessageDialog(
						null,
						(Lenguaje.text(Lenguaje.ERROR))+"\n" +
						(Lenguaje.text(Lenguaje.IMPOSIBLE_SET_PARENT_IN_ISA))+"\n" +
						(Lenguaje.text(Lenguaje.NO_ENTITIES_AVAILABLES))+"\n",
						Lenguaje.text(Lenguaje.SET_PARENT_ENTITY),
						JOptionPane.PLAIN_MESSAGE);
			else{
				comboEntidades.setModel(new javax.swing.DefaultComboBoxModel(items));
				comboEntidades.setSelectedIndex(0);
				this.centraEnPantalla();
				SwingUtilities.invokeLater(doFocus);
				this.setVisible(true);
			}
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
	 * Oyentes de los botones
	 */
	private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);

	}

	private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {
		// Obtenemos la entidad seleccionada
		TransferEntidad te = this.listaEntidades.get(this.comboEntidades.getSelectedIndex());
		// Generamos los datos que enviaremos al controlador
		Vector<Transfer> datos = new Vector<Transfer>();
		datos.add(this.getRelacion());
		datos.add(te);
		// Mandamos el mensaje al controlador con los datos
		this.controlador.mensajeDesde_GUI(TC.GUIEstablecerEntidadPadre_ClickBotonAceptar, datos);
	}
	public void keyPressed( KeyEvent e ) {
		switch (e.getKeyCode()){
			case 27: {
				this.setInactiva();
				break;
			}
			case 10:{
				this.botonAceptarActionPerformed(null);
				break;
			}
		}
	} 
	
	//Oyente para todos los elementos
	private KeyListener general = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==10){botonAceptarActionPerformed(null);}
			if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	/*
	 * Interfaz
	 */
	private JButton getBotonInsertar() {
		if(botonInsertar == null) {
			botonInsertar = boton(180,100,Lenguaje.text(Lenguaje.ACCEPT));
			botonInsertar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonAceptarActionPerformed(evt);
				}
			});
			botonInsertar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonAceptarActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return botonInsertar;
	}
	
	
	
	private JComboBox getComboEntidades() {
		if(comboEntidades == null) {
			ComboBoxModel comboEntidadesModel = new DefaultComboBoxModel(new String[] { "Item One", "Item Two" });
			comboEntidades = new JComboBox();
			comboEntidades.setFont(theme.font());
			comboEntidades.setRenderer(new MyComboBoxRenderer());
			comboEntidades.setModel(comboEntidadesModel);
			comboEntidades.setBounds(25, 40, 231, 27);
			comboEntidades.addKeyListener(general);
		}
		return comboEntidades;
	}

	
	private JLabel getTextoExplicacion() {
		if(textoExplicacion == null) {
			textoExplicacion = new JLabel();
			textoExplicacion.setFont(theme.font());
			textoExplicacion.setText(Lenguaje.text(Lenguaje.SELECT_PARENT_ENTITY));
			textoExplicacion.setBounds(25, 10, 233, 25);
			textoExplicacion.setOpaque(false);
			textoExplicacion.setFocusable(false);
		}
		return textoExplicacion;
	}
	
	/*
	 * Metodos de la clase
	 */
	private String[] generaItems(){
		int cont = 0;
		String[] items = new String[this.listaEntidades.size()];
		while (cont<this.listaEntidades.size()){
			TransferEntidad te = this.listaEntidades.get(cont);
			items[cont] = te.getNombre();			
			cont++;
		}
		return items;
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

