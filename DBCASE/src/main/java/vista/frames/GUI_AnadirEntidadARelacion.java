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
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import persistencia.EntidadYAridad;
import vista.componentes.MyComboBoxRenderer;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings({"rawtypes" ,"unchecked", "serial"})
public class GUI_AnadirEntidadARelacion extends Parent_GUI{

	private Controlador controlador;
	private Vector<TransferEntidad> listaEntidades;
	private JComboBox comboEntidades;
	private JLabel jLabel1;
	private JTextField cajaFinal;
	private JRadioButton buttonMaxN;
	private JRadioButton buttonMax1;
	private JRadioButton buttonMinMax;
	private JButton botonInsertar;
	private JTextField cajaInicio;
	private JLabel explicacion2;
	private JLabel explicacion;
	private TransferRelacion relacion;
	private JLabel explicacion3;
	private JTextField cajaRol;
	private Vector<String> items;
	
	public GUI_AnadirEntidadARelacion() {
		initComponents();
	}

	private void initComponents() {
		setTitle(Lenguaje.text(Lenguaje.INSERT_NEW_ENTITY_TO_RELATION));
        this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setModal(true);
        getContentPane().setLayout(null);
        getContentPane().add(getExplicacion());
        getContentPane().add(getComboEntidades());
        getContentPane().add(getExplicacion2());
        getContentPane().add(getCajaInicio());
        getContentPane().add(getCajaFinal());
        getContentPane().add(getJLabel1());
        getContentPane().add(getExplicacion3());
        getContentPane().add(getCajaRol());
        getContentPane().add(getBotonInsertar());
        getContentPane().add(getButton1a1());
        getContentPane().add(getButtonNaN());
        getContentPane().add(getButtonMinMax());
        this.setSize(300,350);
        this.addMouseListener(this);
		this.addKeyListener(this);
    }

	/*
	 * Activar y desactivar el dialogo
	 */
	
	public void setActiva(){
		// Le pedimos al controlador que nos actualice la lista de entidades
		this.controlador.mensajeDesde_GUI(TC.GUIAnadirEntidadARelacion_ActualizameListaEntidades, null);
		// Generamos los items del comboEntidades
		items = this.generaItems();
		//Los ordenamos alfabéticamente
		if(items.size() == 0)
			JOptionPane.showMessageDialog(
				null,
				(Lenguaje.text(Lenguaje.ERROR))+"\n" +
				(Lenguaje.text(Lenguaje.INSERT_NEW_ENTITY_TO_RELATION))+"\n" +
				(Lenguaje.text(Lenguaje.IMPOSIBLE_TO_INSERT_ENTITY))+"\n" +
				(Lenguaje.text(Lenguaje.NO_ENTITY))+"\n",
				(Lenguaje.text(Lenguaje.INSERT_ATTRIBUTE)),
				JOptionPane.PLAIN_MESSAGE);	
		else{
			this.cajaInicio.setText("");
			this.cajaFinal.setText("");
			this.buttonMax1.setEnabled(true);
			this.buttonMax1.setSelected(false);
			this.buttonMaxN.setEnabled(true);
			this.buttonMaxN.setSelected(true);
			this.buttonMinMax.setEnabled(true);
			this.buttonMinMax.setSelected(false);
			this.cajaInicio.setEnabled(false);
			this.cajaFinal.setEnabled(false);
			this.comboEntidades.setModel(new javax.swing.DefaultComboBoxModel(items));
			this.comboEntidades.setSelectedItem(primerItem());
			this.cajaRol.setText("");
			this.centraEnPantalla();
			SwingUtilities.invokeLater(doFocus);
			this.setVisible(true);
		}	
	}
	
	private Runnable doFocus = new Runnable() {
	     public void run() {
	         comboEntidades.grabFocus();
	     }
	 };

	private Vector<String> generaItems(){
		// Generamos los items
		int cont = 0;
		Vector<String> items = new Vector<String>(this.listaEntidades.size());
		while (cont<this.listaEntidades.size()){
			TransferEntidad te = this.listaEntidades.get(cont);
			items.add(cont,te.getNombre());
			cont++;
		}
		return items;
	}
	
	
	/*Devuelve el nombre de la primera entidad que haya en el sistema y no esté participando
	 * en la relación.*/
	private String primerItem(){
		// Filtramos la lista de entidades quitando las entidades que no intervienen
		Vector<EntidadYAridad> vectorTupla = this.getRelacion().getListaEntidadesYAridades();
		Vector vectorIdsEntidades = new Vector();
		int cont = 0; // Para saltar la entidad padre
		//Guardo en vectorIdsEntidades los ids de las entidades que ya participan en esa relacion
		while(cont<vectorTupla.size()){
			vectorIdsEntidades.add((vectorTupla.get(cont)).getEntidad());
			cont++;
		}
		cont = 0;
		boolean encontrado=false;
		Vector<TransferEntidad> listaEntidadesFiltrada = new Vector<TransferEntidad>();
		while((cont<this.getListaEntidades().size())&&(!encontrado)){
			TransferEntidad te = this.getListaEntidades().get(cont);
			if(vectorIdsEntidades.contains(te.getIdEntidad())){
				listaEntidadesFiltrada.add(te);
				cont++;
			}
			else
				encontrado= true;
			
		}
		TransferEntidad te;
		if((this.listaEntidades.size()==1)||(!encontrado))
			te = this.listaEntidades.get(0);
		else te = this.listaEntidades.get(cont);
		return te.getNombre();
	}
	
	/*Dada la posición seleccionada en el comboBox devuelve el índice correspondiente a dicho 
	 * elementeo en la lista de Entidades.  Es necesario porque al ordenar alfabeticamente se perdió 
	 * la correspondencia.*/
	private int indiceAsociado (int selec){
		boolean encontrado= false;
		int i=0;
		while ((i<this.getListaEntidades().size())&& (!encontrado)){
			if((this.items.get(selec)).equals(this.getListaEntidades().get(i).getNombre())){
				encontrado =true;
				return i;
			}
			else i++;
		}
		return i;
	}


	public void setInactiva(){
		this.setVisible(false);
	}

	/*
	 * Oyentes de los botones
	 */
	private void botonAnadirActionPerformed(java.awt.event.ActionEvent evt) {
		// Mandaremos el siguiente vector al controlador
		Vector v = new Vector();
		v.add(this.getRelacion());
		v.add(this.getListaEntidades().get(indiceAsociado(this.comboEntidades.getSelectedIndex())));
		//En función de que boton de la cardinalidad haya seleccionado se guardará una u otra:
		if (this.buttonMax1.isSelected()){
			v.add(String.valueOf(0));
			v.add(String.valueOf(1));
		}
		else if (this.buttonMaxN.isSelected()){
			v.add(String.valueOf(0));
			v.add("n");
		}		
		else{
			v.add(String.valueOf(this.cajaInicio.getText().toLowerCase()));
			v.add(String.valueOf(this.cajaFinal.getText().toLowerCase()));
		}
		v.add(String.valueOf(this.cajaRol.getText()));
		// Mandamos el mensaje y el vector con los datos
		this.controlador.mensajeDesde_GUI(TC.GUIAnadirEntidadARelacion_ClickBotonAnadir,v);

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
				this.botonAnadirActionPerformed(null);
				break;
			}
		}
	} 
	
	//Oyente para todos los elementos
	private KeyListener general = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==10){botonAnadirActionPerformed(null);}
			if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
	
	/*
	 * Interfaz
	 */
	private JLabel getExplicacion() {
		if(explicacion == null) {
			explicacion = new JLabel();
			explicacion.setFont(theme.font());
			explicacion.setText(Lenguaje.text(Lenguaje.SELECT_ENTITY));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 8, 115, 24);
			explicacion.setFocusable(false);
		}
		return explicacion;
	}
	
	private JComboBox getComboEntidades() {
		if(comboEntidades == null) {
			comboEntidades = new JComboBox();
			comboEntidades.setRenderer(new MyComboBoxRenderer());
			comboEntidades.setFont(theme.font());
			comboEntidades.setBounds(25, 35, 231, 27);
		}
		comboEntidades.addKeyListener(general);
		return comboEntidades;
	}
	
	private JLabel getExplicacion2() {
		if(explicacion2 == null) {
			explicacion2 = new JLabel();
			explicacion2.setFont(theme.font());
			explicacion2.setText(Lenguaje.text(Lenguaje.WRITE_NUMBERS_RELATION));
			explicacion2.setOpaque(false);
			explicacion2.setBounds(25, 65, 107, 24);
			explicacion2.setFocusable(false);
		}
		return explicacion2;
	}
	
	private JTextField getCajaInicio() {
		if(cajaInicio == null) {
			cajaInicio = new JTextField();
			cajaInicio.setEditable(true);
			cajaInicio.setEnabled(true);
			cajaInicio.setFont(theme.font());
			cajaInicio.setForeground(theme.labelFontColorDark());
			cajaInicio.setBounds(86, 160, 40, 25);
			cajaInicio.addKeyListener(general);
		}
		return cajaInicio;
	}
	
	private JTextField getCajaFinal() {
		if(cajaFinal == null) {
			cajaFinal = new JTextField();
			cajaFinal.setBounds(176, 160, 40, 25);
			cajaFinal.setFont(theme.font());
			cajaFinal.setForeground(theme.labelFontColorDark());
			cajaFinal.setEnabled(true);
			cajaFinal.setEditable(true);
			cajaFinal.addKeyListener(general);
		}
		return cajaFinal;
	}
	
	private JLabel getJLabel1() {
		if(jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setEnabled(true);
			jLabel1.setFont(theme.font());
			jLabel1.setText(Lenguaje.text(Lenguaje.TO));
			jLabel1.setBounds(138, 160, 60, 25);
		}
		return jLabel1;
	}
	
	private JLabel getExplicacion3() {
		if(explicacion3 == null) {
			explicacion3 = new JLabel();
			explicacion3.setFont(theme.font());
			explicacion3.setText(Lenguaje.text(Lenguaje.WRITE_ROLL));
			explicacion3.setOpaque(false);
			explicacion3.setBounds(25, 194, 147, 24);
			explicacion3.setFocusable(false);
		}
		return explicacion3;
	}
	
	private JTextField getCajaRol() {
		if(cajaRol == null) {
			cajaRol = new JTextField();
			cajaRol.setFont(theme.font());
			cajaRol.setForeground(theme.labelFontColorDark());
			cajaRol.setBounds(25, 227, 232, 27);
		}
		cajaRol.addKeyListener(general);
		return cajaRol;
	}
	
	private JButton getBotonInsertar() {
		if(botonInsertar == null) {
			botonInsertar = this.botonInsertar(160,280);
			botonInsertar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonAnadirActionPerformed(evt);
				}
			});
			botonInsertar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10) botonAnadirActionPerformed(null);
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return botonInsertar;
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
	
	/*Al seleccionar la cardinalidad 1 a 1 deshabilito el resto de botones  y los habilito 
	 * al desseleccionar*/
	private void button1a1ItemStateChanged(java.awt.event.ItemEvent evt) {
		if(this.buttonMax1.isSelected()){
			this.buttonMaxN.setSelected(false);
			this.buttonMinMax.setSelected(false);
			this.cajaInicio.setEnabled(false);
			this.cajaFinal.setEnabled(false);
			this.cajaFinal.setText("");
			this.cajaInicio.setText("");
		}
		else{
			if(!this.buttonMax1.isSelected()){
				this.buttonMaxN.setEnabled(true);
				this.buttonMinMax.setEnabled(true);
			}
		}
	}
	
	/*Al seleccionar la cardinalidad N a N deshabilito el resto de botones  y los habilito 
	 * al desseleccionar*/
	private void buttonNaNItemStateChanged(java.awt.event.ItemEvent evt) {
		if(this.buttonMaxN.isSelected()){
			this.buttonMax1.setSelected(false);
			this.buttonMinMax.setSelected(false);
			this.cajaInicio.setEnabled(false);
			this.cajaFinal.setEnabled(false);
			this.cajaFinal.setText("");
			this.cajaInicio.setText("");
		}
		else{
			if(!this.buttonMaxN.isSelected()){
				this.buttonMax1.setEnabled(true);
				this.buttonMinMax.setEnabled(true);
			}
		}
	}
	
	/*Al seleccionar la cardinalidad Min Max deshabilito el resto de botones  y los habilito 
	 * al desseleccionar*/
	private void buttonMinMaxItemStateChanged(java.awt.event.ItemEvent evt) {
		if(this.buttonMinMax.isSelected()){
			this.buttonMax1.setSelected(false);
			this.buttonMaxN.setSelected(false);
			this.cajaInicio.setEnabled(true);
			this.cajaFinal.setEnabled(true);
		}
		else{
			if(!this.buttonMinMax.isSelected()){
				this.cajaFinal.setText("");
				this.cajaInicio.setText("");
			}
		}
	}
	
	private JRadioButton getButton1a1() {
		if(buttonMax1 == null) {
			buttonMax1 = new JRadioButton();
			buttonMax1.setOpaque(false);
			buttonMax1.setEnabled(false);
			buttonMax1.setFont(theme.font());
			buttonMax1.setText(Lenguaje.text(Lenguaje.LABEL1A1));
			buttonMax1.setBounds(25, 100, 127, 24);
			buttonMax1.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent evt) {
					button1a1ItemStateChanged(evt);
				}
			});
			buttonMax1.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){buttonMax1.setSelected(
												!buttonMax1.isSelected());}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return buttonMax1;
	}
	
	private JRadioButton getButtonNaN() {
		if(buttonMaxN == null) {
			buttonMaxN = new JRadioButton();
			buttonMaxN.setFont(theme.font());
			buttonMaxN.setOpaque(false);
			buttonMaxN.setSelected(true);
			buttonMaxN.setText(Lenguaje.text(Lenguaje.LABELNAN));
			buttonMaxN.setBounds(25, 130, 127, 24);
			buttonMaxN.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent evt) {
					buttonNaNItemStateChanged(evt);
				}
			});
			buttonMaxN.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){buttonMaxN.setSelected(!buttonMaxN.isSelected());}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return buttonMaxN;
	}
	
	private JRadioButton getButtonMinMax() {
		if(buttonMinMax == null) {
			buttonMinMax = new JRadioButton();
			buttonMinMax.setFont(theme.font());
			buttonMinMax.setOpaque(false);
			buttonMinMax.setEnabled(false);
			buttonMinMax.setText(Lenguaje.text(Lenguaje.THE));
			buttonMinMax.setBounds(25, 160, 127, 24);
			buttonMinMax.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent evt) {
					buttonMinMaxItemStateChanged(evt);
				}
			});
			buttonMinMax.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){buttonMinMax.setSelected(!buttonMinMax.isSelected());}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return buttonMinMax;
	}

}
