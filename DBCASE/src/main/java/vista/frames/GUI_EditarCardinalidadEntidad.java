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
public class GUI_EditarCardinalidadEntidad extends Parent_GUI{

	private Controlador controlador;
	private Vector<TransferEntidad> listaEntidades;
	private JComboBox comboEntidades;
	private JTextField cajaInicio;
	private JLabel jLabel1;
	private JTextField cajaFinal;
	private JRadioButton buttonMinMax;
	private JComboBox comboRoles;
	private JLabel jTextPane1;
	private JTextField cajaRol;
	private JLabel jTextRol;
	private JLabel explicacion2;
	private JLabel explicacion;
	private JButton botonEditar;
	private TransferRelacion relacion;
	private String rolViejo;
	private JRadioButton buttonMaxN;
	private JRadioButton buttonMax1;
	
	public GUI_EditarCardinalidadEntidad() {
		initComponents();
	}

	private void initComponents() {
        setTitle(Lenguaje.text(Lenguaje.EDIT_ARITY_AND_ROLLE));
        setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setResizable(false);
        setModal(true);
        getContentPane().setLayout(null);
        getContentPane().add(getBotonEditar());
        getContentPane().add(getComboEntidades());
        getContentPane().add(getExplicacion());
        getContentPane().add(getExplicacion2());
        getContentPane().add(getCajaFinal());
        getContentPane().add(getJLabel1());
        getContentPane().add(getCajaInicio());
        getContentPane().add(getJTextRol());
        getContentPane().add(getcajaRol());
        getContentPane().add(getJTextPane1());
        getContentPane().add(getComboRoles());
        getContentPane().add(getButton1a1());
        getContentPane().add(getButtonMinMax());
        getContentPane().add(getButtonNaN());
        this.setSize(300, 385);
        this.addMouseListener(this);
		this.addKeyListener(this);
    }

	/*
	 * Activar y desactivar el dialogo
	 */

	public void setInactiva(){
		this.setVisible(false);
	}

	public void setActiva(){
		if(this.getRelacion().getListaEntidadesYAridades().isEmpty())
			JOptionPane.showMessageDialog(
				null,(Lenguaje.text(Lenguaje.ERROR))+"\n" +
				(Lenguaje.text(Lenguaje.IMPOSIBLE_EDIT_ROLLE))+"\n" +
				(Lenguaje.text(Lenguaje.NO_ENTITIES_IN_RELATION))+"\n",
				(Lenguaje.text(Lenguaje.EDIT_ARITY)),
				JOptionPane.PLAIN_MESSAGE);
		else{
			this.controlador.mensajeDesde_GUI(TC.GUIEditarCardinalidadEntidad_ActualizameListaEntidades, null);
			this.cajaInicio.setText("");
			this.cajaFinal.setText("");
			this.cajaFinal.setEnabled(false);
			this.cajaInicio.setEnabled(false);
			this.cajaFinal.setEditable(false);
			this.cajaFinal.setEditable(false);
			this.buttonMax1.setEnabled(true);
			this.buttonMaxN.setEnabled(true);
			this.buttonMinMax.setEnabled(true);
			// Generamos los items (ya filtrados)
			String[] itemsEntidades = this.generaItemsEntidades();
			comboEntidades.setModel(new javax.swing.DefaultComboBoxModel(itemsEntidades));
			comboEntidades.setSelectedIndex(0);
			comboEntidades.grabFocus();
			// Inicio y fin de la entidad seleccionada
			int vez = 1;//Flag para distingir la inicialización de la ventana
			Vector<String> v = this.generaInicioFin(vez);
			vez ++;
			this.cajaRol.setText(v.get(2));
			rolViejo=v.get(2);
			Vector<String> itemsRoles = this.generaItemsRoles();
			comboRoles.setModel(new javax.swing.DefaultComboBoxModel(itemsRoles));
			if(this.buttonMinMax.isSelected()){
				cajaInicio.setText(v.get(0));
				cajaFinal.setText(v.get(1));
			}
			else{
				cajaInicio.setText("");
				cajaFinal.setText("");
				this.cajaInicio.setEditable(false);
				this.cajaFinal.setEditable(false);
				this.cajaFinal.setEnabled(false);
				this.cajaInicio.setEnabled(false);
			}			
			SwingUtilities.invokeLater(doFocus);
			this.centraEnPantalla();
			if(v.get(0).equals("0"))
				if(v.get(1).equals("1")) this.buttonMax1.setSelected(true);
				else if(v.get(1).equals("N")) this.buttonMaxN.setSelected(true);
				else this.buttonMinMax.setSelected(true);
			else this.buttonMinMax.setSelected(true);
			this.setVisible(true);	
		}
	}

	private Runnable doFocus = new Runnable() {
	     public void run() {
	         comboEntidades.grabFocus();
	     }
	 };
	
	private Vector<String> generaInicioFin(int vez){
		int inicio = 0;
		int fin = 0;
		String rol="";
		int itemSeleccionado = this.comboEntidades.getSelectedIndex();
		int rolSeleccionado = this.comboRoles.getSelectedIndex();
		TransferEntidad te = this.listaEntidades.get(itemSeleccionado);
		int idEntidad = te.getIdEntidad();
		Vector veya = this.relacion.getListaEntidadesYAridades();
		int cont = 0;
		int numRolVisitados = 0;//Si en una relación participa varias veces una entidad con que aparición me quedo
		boolean salir = false;
		while(cont<veya.size() && !salir){
			EntidadYAridad eya = (EntidadYAridad) veya.get(cont);
			if(vez ==1){//Inicialización de la ventana
				if(eya.getEntidad() == idEntidad){
						inicio = eya.getPrincipioRango();
						fin = eya.getFinalRango();
						rol=eya.getRol();
						salir = true;	
				}
			}
			else{
				if(eya.getEntidad() == idEntidad){
					if(numRolVisitados == rolSeleccionado){
						inicio = eya.getPrincipioRango();
						fin = eya.getFinalRango();
						rol=eya.getRol();
						salir = true;	
					}
					else numRolVisitados++;
				}
			}
			cont++;
		}
		Vector<String> v = new Vector<String>();
		if(inicio == Integer.MAX_VALUE) v.add("N"); else v.add(String.valueOf(inicio));
		if(fin == Integer.MAX_VALUE) v.add("N"); else v.add(String.valueOf(fin));
		v.add(String.valueOf(rol));
		return v;
	}

	private String[] generaItemsEntidades(){
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
	
	private Vector<String> generaItemsRoles(){
		Vector<String> v = new Vector<String>();
		int itemSeleccionado = this.comboEntidades.getSelectedIndex();
		TransferEntidad te = this.listaEntidades.get(itemSeleccionado);
		int idEntidad = te.getIdEntidad();
		te.isDebil();
		Vector veya = this.relacion.getListaEntidadesYAridades();
		int cont = 0;
		while(cont<veya.size()){
			EntidadYAridad eya = (EntidadYAridad) veya.get(cont);
			if(eya.getEntidad()==idEntidad) v.add(eya.getRol());
			cont++;
		}
		return v;
	}

	/*
	 * Oyentes
	 */
	private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {                                           
		this.setVisible(false);
	}                                          

	private void botonEditarActionPerformed(java.awt.event.ActionEvent evt) {
		Vector<Object> v = new Vector<Object>();
		v.add(this.getRelacion());
		v.add(this.listaEntidades.get(this.comboEntidades.getSelectedIndex()));
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
		v.add(this.cajaRol.getText());
		v.add(rolViejo);
		this.controlador.mensajeDesde_GUI(TC.GUIEditarCardinalidadEntidad_ClickBotonEditar, v);
	}                                           

	private void comboEntidadesItemStateChanged(java.awt.event.ItemEvent evt) {
		//Se llama a generaInicioFin con un valor cualquiera >1 
		Vector<String> v = this.generaInicioFin(13);
		if(this.buttonMinMax.isSelected()){
			cajaInicio.setText(v.get(0));
			cajaFinal.setText(v.get(1));
		}
		else{
			cajaInicio.setText("");
			cajaFinal.setText("");
			this.cajaInicio.setEditable(false);
			this.cajaFinal.setEditable(false);
			this.cajaFinal.setEnabled(false);
			this.cajaInicio.setEnabled(false);
		}
		cajaRol.setText(v.get(2));
		rolViejo=v.get(2);
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
			
	private JButton getBotonEditar() {
		if(botonEditar == null) {
			botonEditar = boton(190, 325,Lenguaje.text(Lenguaje.ACCEPT));
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
		}
		return botonEditar;
	}
	
	private JComboBox getComboEntidades() {
		if(comboEntidades == null) {
			comboEntidades = new JComboBox();
			comboEntidades.setRenderer(new MyComboBoxRenderer());
			comboEntidades.setBounds(25, 52, 238, 25);
			comboEntidades.setFont(theme.font());
			comboEntidades.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					comboEntidadesItemStateChanged(evt);
				}
			});
			comboEntidades.addKeyListener(general);
		}
		return comboEntidades;
	}
	
	private JLabel getExplicacion() {
		if(explicacion == null) {
			explicacion = new JLabel();
			explicacion.setFont(theme.font());
			explicacion.setText(Lenguaje.text(Lenguaje.SELECT_ENTITY_TO_CHANGE));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 21, 238, 25);
			explicacion.setFocusable(false);
		}
		return explicacion;
	}
	
	private JLabel getExplicacion2() {
		if(explicacion2 == null) {
			explicacion2 = new JLabel();
			explicacion2.setFont(theme.font());
			explicacion2.setText(Lenguaje.text(Lenguaje.WRITE_NEW_ARITY));
			explicacion2.setOpaque(false);
			explicacion2.setBounds(25, 140, 238, 25);
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
			cajaInicio.setBounds(86, 230, 40, 25);
			cajaInicio.addKeyListener(general);
		}
		return cajaInicio;
	}
	
	private JTextField getCajaFinal() {
		if(cajaFinal == null) {
			cajaFinal = new JTextField();
			cajaFinal.setBounds(176, 230, 40, 25);
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
			jLabel1.setBounds(138, 230, 60, 25);
		}
		return jLabel1;
	}
	
	
	private JLabel getJTextRol() {
		if(jTextRol == null) {
			jTextRol = new JLabel();
			jTextRol.setFont(theme.font());
			jTextRol.setText(Lenguaje.text(Lenguaje.WRITE_NEW_ROLLE));
			jTextRol.setBounds(25, 261, 239, 25);
			jTextRol.setOpaque(false);
			jTextRol.setFocusable(false);
		}
		return jTextRol;
	}
	
	private JTextField getcajaRol() {
		if(cajaRol == null) {
			cajaRol = new JTextField();
			cajaRol.setFont(theme.font());
			cajaRol.setBounds(25, 290, 239, 25);
		}
		cajaRol.addKeyListener(general);
		return cajaRol;
	}
	
	private JLabel getJTextPane1() {
		if(jTextPane1 == null) {
			jTextPane1 = new JLabel();
			jTextPane1.setFont(theme.font());
			jTextPane1.setText(Lenguaje.text(Lenguaje.IF_ENTITY_HAS_ROLLE));
			jTextPane1.setOpaque(false);
			jTextPane1.setBounds(25, 77, 238, 25);
			jTextPane1.setFocusable(false);
		}
		return jTextPane1;
	}
		
	private JComboBox getComboRoles() {
		if(comboRoles == null) {
			comboRoles = new JComboBox();
			comboRoles.setRenderer(new MyComboBoxRenderer());
			comboRoles.setFont(theme.font());
			comboRoles.setBounds(25, 110, 238, 25);
			comboRoles.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent evt) {
					comboRolesItemStateChanged(evt);
				}
			});
			comboRoles.addKeyListener(general);
		}
		return comboRoles;
	}
	
	private void comboRolesItemStateChanged(java.awt.event.ItemEvent evt) {
		//Se llama a generaInicioFin con un valor cualquiera >1 
		Vector<String> v = this.generaInicioFin(13);
		if(this.buttonMinMax.isSelected()){
			cajaInicio.setText(v.get(0));
			cajaFinal.setText(v.get(1));
		}
		else{
			cajaInicio.setText("");
			cajaFinal.setText("");
			this.cajaInicio.setEditable(false);
			this.cajaFinal.setEditable(false);
			this.cajaFinal.setEnabled(false);
			this.cajaInicio.setEnabled(false);
		}
		cajaRol.setText(v.get(2));
		rolViejo=v.get(2);
	}

	
	/*Al seleccionar la cardinalidad 1 a 1 deshabilito el resto de botones  y los habilito 
	 * al desseleccionar*/
	private void button1a1ItemStateChanged(java.awt.event.ItemEvent evt) {
		if(this.buttonMax1.isSelected()){
			this.buttonMaxN.setSelected(false);
			this.buttonMinMax.setSelected(false);
			this.cajaFinal.setEditable(false);
			this.cajaFinal.setText("");
			this.cajaInicio.setText("");
			this.cajaInicio.setEditable(false);
			this.jLabel1.setEnabled(false);
		}
		else{
			if(!this.buttonMax1.isSelected()){
				this.buttonMaxN.setEnabled(true);
				this.buttonMinMax.setEnabled(true);
				this.jLabel1.setEnabled(true);
			}
		}
	}
		
	/*Al seleccionar la cardinalidad N a N deshabilito el resto de botones  y los habilito 
	 * al desseleccionar*/
	private void buttonNaNItemStateChanged(java.awt.event.ItemEvent evt) {
		if(this.buttonMaxN.isSelected()){
			this.buttonMax1.setSelected(false);
			this.buttonMinMax.setSelected(false);
			this.cajaFinal.setEditable(false);
			this.cajaFinal.setText("");
			this.cajaInicio.setText("");
			this.cajaInicio.setEditable(false);
			this.jLabel1.setEnabled(false);
		}
		else{
			if(!this.buttonMaxN.isSelected()){
				this.buttonMax1.setEnabled(true);
				this.buttonMinMax.setEnabled(true);
				this.jLabel1.setEnabled(true);
			}
		}
	}
	
	/*Al seleccionar la cardinalidad Min Max deshabilito el resto de botones  y los habilito 
	 * al desseleccionar*/
	private void buttonMinMaxItemStateChanged(java.awt.event.ItemEvent evt) {
		if(this.buttonMinMax.isSelected()){
			//Inicio y fin de la entidad seleccionada
			Vector<String> v = this.generaInicioFin(13);
			this.cajaInicio.setText(v.get(0));
			this.cajaFinal.setText(v.get(1));
			this.buttonMax1.setSelected(false);
			this.buttonMaxN.setSelected(false);
			this.cajaFinal.setEnabled(true);
			this.cajaInicio.setEnabled(true);
			this.cajaFinal.setEditable(true);
			this.cajaInicio.setEditable(true);
		}
		else{
			if(!this.buttonMinMax.isSelected()){
				this.cajaFinal.setEditable(false);
				this.cajaFinal.setText("");
				this.cajaInicio.setText("");
				this.cajaInicio.setEditable(false);
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
			buttonMax1.setBounds(25, 170, 127, 25);
			buttonMax1.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent evt) {
					button1a1ItemStateChanged(evt);
				}
			});
			buttonMax1.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10)buttonMax1.setSelected(!buttonMax1.isSelected());
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
			buttonMaxN.setBounds(25, 200, 127, 25);
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
			buttonMinMax.setBounds(25, 230, 127, 25);
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
}
