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

@SuppressWarnings({"rawtypes" ,"unchecked", "serial"})
public class GUI_AnadirEntidadHija extends Parent_GUI {

	private Controlador controlador;
	private Vector<TransferEntidad> listaEntidades;
	private JComboBox comboEntidades;
	private JButton botonInsertar;
	private JLabel explicacion;
	private TransferRelacion relacion;

	public GUI_AnadirEntidadHija() {
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(null);
		setTitle(Lenguaje.text(Lenguaje.INSERT_NEW_DAUGTHER));
		this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setModal(true);
		setResizable(false);
		getContentPane().add(getExplicacion());
		getContentPane().add(getComboEntidades());
		getContentPane().add(getBotonInsertar());
		this.setSize(300, 180);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}

	/*
	 * Oyentes de los botones
	 */

	private void botonSalirActionPerformed(java.awt.event.ActionEvent evt) {                                           
		this.setVisible(false);
	}                                          

	private void botonAnadirActionPerformed(java.awt.event.ActionEvent evt) {
		// Obtenemos la entidad seleccionada
		TransferEntidad te = this.listaEntidades.get(this.comboEntidades.getSelectedIndex());
		// Generamos los datos que enviaremos al controlador
		Vector<Transfer> datos = new Vector<Transfer>();
		datos.add(this.getRelacion());
		datos.add(te);
		// Mandamos el mensaje al controlador con los datos
		this.controlador.mensajeDesde_GUI(TC.GUIAnadirEntidadHija_ClickBotonAnadir, datos);
	}
	public void keyPressed( KeyEvent e ) {
		switch (e.getKeyCode()){
			case 27: {
				this.setInactiva();
				break;
			}
			case 10:{
				this.botonAnadirActionPerformed(null);
				break;
			}
		}
	} 
	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent arg0) {}
	
	//Oyente para todos los elementos
	private KeyListener general = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==10){botonAnadirActionPerformed(null);}
			if(e.getKeyCode()==27){botonSalirActionPerformed(null);}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		// Si no tiene establecida la entidad padre, lanzamos un error
		if(this.getRelacion().getListaEntidadesYAridades().isEmpty())
			JOptionPane.showMessageDialog(
					null,
					(Lenguaje.text(Lenguaje.ERROR))+"\n" +
					(Lenguaje.text(Lenguaje.IMPOSIBLE_TO_INSERT_DAUGHTER))+"\n" +
					(Lenguaje.text(Lenguaje.NO_FATHER))+"\n",
					Lenguaje.text(Lenguaje.INSERT_NEW_DAUGTHER),
					JOptionPane.PLAIN_MESSAGE);
		else{
			this.controlador.mensajeDesde_GUI(TC.GUIAnadirEntidadHija_ActualizameListaEntidades, null);
			// Generamos los items (ya filtrados)
			String[] items = this.generaItems();
			if(items.length == 0)
				JOptionPane.showMessageDialog(
						null,
						(Lenguaje.text(Lenguaje.ERROR))+"\n" +
						(Lenguaje.text(Lenguaje.IMPOSIBLE_TO_INSERT_DAUGHTER))+"\n" +
						(Lenguaje.text(Lenguaje.NO_ENTITIES_AVAILABLES))+"\n",
						Lenguaje.text(Lenguaje.INSERT_NEW_DAUGTHER),
						JOptionPane.PLAIN_MESSAGE);

			else{
				comboEntidades.setModel(new javax.swing.DefaultComboBoxModel(items));
				comboEntidades.setSelectedIndex(0);
				SwingUtilities.invokeLater(doFocus);
				this.centraEnPantalla();
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

	private String[] generaItems(){
		// Filtramos la lista de entidades quitando la entidad padre y las entidades hermanas
		Vector<EntidadYAridad> vectorTupla = this.getRelacion().getListaEntidadesYAridades();
		Vector vectorIdsEntidades = new Vector();
		int cont = 0;
		while(cont<vectorTupla.size()){
			vectorIdsEntidades.add((vectorTupla.get(cont)).getEntidad());
			cont++;
		}
		cont = 0;
		Vector<TransferEntidad> listaEntidadesFiltrada = new Vector<TransferEntidad>();
		while(cont<this.getListaEntidades().size()){
			TransferEntidad te = this.getListaEntidades().get(cont);
			if(!vectorIdsEntidades.contains(te.getIdEntidad()))
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

	/*
	 * Interfaz
	 */
	private JLabel getExplicacion() {
		if(explicacion == null) {
			explicacion = new JLabel();
			explicacion.setFont(theme.font());
			explicacion.setText(Lenguaje.text(Lenguaje.SELECT_ENTITY_DAUTHTER));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 10, 115, 24);
			explicacion.setFocusable(false);
		}
		return explicacion;
	}

	private JComboBox getComboEntidades() {
		if(comboEntidades == null) {
			comboEntidades = new JComboBox();
			comboEntidades.setRenderer(new MyComboBoxRenderer());
			comboEntidades.setFont(theme.font());
			comboEntidades.setBounds(25, 40, 231, 27);
		}
		comboEntidades.addKeyListener(general);
		return comboEntidades;
	}

	private JButton getBotonInsertar() {
		if(botonInsertar == null) {
			botonInsertar = botonInsertar(165,100);
			botonInsertar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonAnadirActionPerformed(evt);
				}
			});
			botonInsertar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonAnadirActionPerformed(null);}
					else if(e.getKeyCode()==27){botonSalirActionPerformed(null);}
					else if(e.getKeyCode()==37){botonInsertar.grabFocus();}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return botonInsertar;
	}

}
