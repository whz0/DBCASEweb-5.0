package vista.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TipoDominio;
import modelo.transfers.Transfer;
import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferDominio;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import vista.componentes.MyComboBoxRenderer;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings({"rawtypes" ,"unchecked", "serial"})
public class GUI_AnadirAtributo extends Parent_GUI{

	private Controlador controlador;
	private JTextField cajaNombre = this.getCajaNombre(25, 85);
	private JCheckBox opcionClavePrimaria;
	private JCheckBox opcionMultivalorado;
	private JCheckBox opcionCompuesto;
	private JCheckBox opcionNotnull;
	private JCheckBox opcionUnique;
	private JComboBox comboDominios;
	private JComboBox comboTransfers;
	private JButton botonInsertar;
	private JLabel labelTamano;
	private JTextField cajaTamano;
	private JLabel jTextPane2;
	private JLabel explicacion;
	private JLabel eligeTransfer;
	private Vector<TransferDominio> listaDominios;
	private Vector<Transfer> listaTransfers;

	public GUI_AnadirAtributo() {
		this.initComponents();
	}

	private void initComponents() {
		setTitle(Lenguaje.text(Lenguaje.INSERT_ATTRIBUTE));
		this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		cajaNombre.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==10){botonAnadirActionPerformed(null);}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		getContentPane().setLayout(null);
		getContentPane().add(getEligeTransfer());
		getContentPane().add(getComboTransfers());
		getContentPane().add(getExplicacion());
		getContentPane().add(cajaNombre);
		getContentPane().add(getOpcionClavePrimaria());
		getContentPane().add(getOpcionCompuesto());
		getContentPane().add(getOpcionNotnull());
		getContentPane().add(getOpcionUnique());
		getContentPane().add(getOpcionMultivalorado());
		getContentPane().add(getJTextPane2());
		getContentPane().add(getComboDominios());
		getContentPane().add(getCajaTamano());
		getContentPane().add(getLabelTamano());
		getContentPane().add(getBotonInsertar());
		this.setSize(300, 400);
		this.addMouseListener(this);
		this.addKeyListener(this);
	}


	/*
	 * Oyentes de los checkBox y del combo
	 */
	private void opcionCompuestoItemStateChanged(java.awt.event.ItemEvent evt) {
		if(this.opcionCompuesto.isSelected()){
			this.comboDominios.setEnabled(false);
			this.comboTransfers.setEnabled(false);
			this.opcionNotnull.setSelected(false);
			this.opcionNotnull.setEnabled(false);
			this.opcionUnique.setSelected(false);
			this.opcionUnique.setEnabled(false);
			this.cajaTamano.setEditable(false);
			this.cajaTamano.setEnabled(false);
		}
		else{
			if(!this.opcionClavePrimaria.isSelected()){
				this.comboDominios.setEnabled(true);
				this.comboTransfers.setEnabled(true);
				this.opcionNotnull.setEnabled(true);
				this.opcionUnique.setEnabled(true);
			}
			this.cajaTamano.setEditable(this.activarTamano());
			this.cajaTamano.setEnabled(true);
		}
	}
	
	private void opcionClavePrimariaItemStateChanged(java.awt.event.ItemEvent evt) {
		if(this.opcionClavePrimaria.isSelected()){
			this.opcionNotnull.setSelected(false);
			this.opcionNotnull.setEnabled(false);
			this.opcionUnique.setSelected(false);
			this.opcionUnique.setEnabled(false);
			this.opcionMultivalorado.setSelected(false);
			this.opcionMultivalorado.setEnabled(false);
		}
		else{
			if(!this.opcionCompuesto.isSelected()){
				this.opcionNotnull.setEnabled(true);
				this.opcionUnique.setEnabled(true);
			}
			this.opcionMultivalorado.setEnabled(true);
		}
	}

	private void comboDominiosItemStateChanged(java.awt.event.ItemEvent evt) {
		if (this.activarTamano()){
			this.cajaTamano.setText("10");
			this.cajaTamano.setEditable(true);
			this.cajaTamano.setEnabled(true);
		}
		else{
			this.cajaTamano.setText("");
			this.cajaTamano.setEditable(false);
			this.cajaTamano.setEnabled(false);
		}
	}

	/*
	 * Oyentes de los botones
	 */
	private void botonAnadirActionPerformed(ActionEvent evt) {
		TransferAtributo ta = new TransferAtributo(controlador);
		ta.setNombre(this.cajaNombre.getText());
		String tamano = "";
		
		ta.setListaComponentes(new Vector());
		if(this.opcionClavePrimaria.isSelected()) ta.setClavePrimaria(true);
		else ta.setClavePrimaria(false);
		
		// Si esta seleccionada la opcion compuesto el dominio del atributo lo
		// ponemos a null
		if(this.opcionCompuesto.isSelected()){
			ta.setCompuesto(true);
			ta.setNotnull(false);
			ta.setUnique(false);
			ta.setDominio("null");
			ta.setMultivalorado(opcionMultivalorado.isSelected());
		}
		else{
			ta.setCompuesto(false);
			// Si esta seleccionado la opcion multivalorado
			if (this.opcionMultivalorado.isSelected()) ta.setMultivalorado(true);
			else ta.setMultivalorado(false);
			
			//Unique y Notnull
			ta.setNotnull(this.opcionNotnull.isSelected());
			//ponemos unique a false, ya que en caso de ser Unique se hace la llamada abajo.
			ta.setUnique(false);
			
			TipoDominio dominio;
			String dom;
			try{
				// Obtenemos el dominio seleccionado
				dominio = TipoDominio.valueOf(this.comboDominios.getSelectedItem().toString());
				// Si es un dominio para el que no hay que especificar tamano
				if (!(((TipoDominio)dominio).equals(TipoDominio.CHAR) ||
					((TipoDominio)dominio).equals(TipoDominio.VARCHAR) ||
					((TipoDominio)dominio).equals(TipoDominio.TEXT) ||
					((TipoDominio)dominio).equals(TipoDominio.INTEGER) ||
					((TipoDominio)dominio).equals(TipoDominio.DECIMAL) ||
					((TipoDominio)dominio).equals(TipoDominio.FLOAT))){
						dom=dominio.toString();
						ta.setDominio(dom);
				}
				// Si hay que especificar el tamano
				else{
					tamano = this.cajaTamano.getText();
					// Si no se ha especificado tamano ponemos un 1 por defecto
					if (tamano.isEmpty()) tamano = "10";
					// Formamos la cadena del dominio
					ta.setDominio(dominio.toString()+"("+tamano+")");
				}
			}catch(Exception e){//Dominio definido por el usuario
				dom = this.comboDominios.getSelectedItem().toString();
				ta.setDominio(dom);
				ta.setNotnull(this.opcionNotnull.isSelected());
				ta.setCompuesto(this.opcionCompuesto.isSelected());
				ta.setMultivalorado(this.opcionMultivalorado.isSelected());
			}
		}
		ta.setListaRestricciones(new Vector());
		double x = this.listaTransfers.get(comboTransfers.getSelectedIndex()).getPosicion().getX() + 100;
		double y = this.listaTransfers.get(comboTransfers.getSelectedIndex()).getPosicion().getY();
		ta.setPosicion(new Point2D.Double(x,y));
		// Mandamos la entidad, el nuevo atributo y si hay tamano tambien
		Vector<Object> v = new Vector<Object>();
		v.add(listaTransfers.get(comboTransfers.getSelectedIndex()));
		v.add(ta);
		if (!tamano.isEmpty()) v.add(tamano);
		if(comboTransfers.getSelectedItem() instanceof TransferRelacion) 
			controlador.mensajeDesde_GUI(TC.GUIAnadirAtributoRelacion_Click_BotonAnadir, v);
		else if(comboTransfers.getSelectedItem() instanceof TransferEntidad) {
			controlador.mensajeDesde_GUI(TC.GUIAnadirAtributoEntidad_Click_BotonAnadir, v);
			if (this.opcionClavePrimaria.isSelected()){
				Vector<Object> v1= new Vector<Object>();
				TransferAtributo clon_atributo2 = ta.clonar();
				clon_atributo2.setClavePrimaria(false);
				v1.add(clon_atributo2);
			    v1.add(listaTransfers.get(comboTransfers.getSelectedIndex()));
				controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarClavePrimariaAtributo,v1);
			}
		}else if(comboTransfers.getSelectedItem() instanceof TransferAtributo) 
			controlador.mensajeDesde_GUI(TC.GUIAnadirSubAtributoAtributo_Click_BotonAnadir, v);
		if (this.opcionUnique.isSelected()){
			TransferAtributo clon_atributo = ta.clonar();
			controlador.mensajeDesde_PanelDiseno(TC.PanelDiseno_Click_EditarUniqueAtributo,clon_atributo);
		}
		this.setVisible(false);
	}

	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		Object[] nuevos = new Object[this.listaDominios.size()];
		this.centraEnPantalla();
		this.opcionClavePrimaria.setSelected(false);
		this.opcionCompuesto.setSelected(false);
		this.opcionNotnull.setSelected(false);
		this.opcionUnique.setSelected(false);
		this.opcionMultivalorado.setSelected(false);
		this.comboDominios.setEnabled(true);
		this.comboTransfers.setEnabled(true);
		this.cajaTamano.setText("");
		this.cajaNombre.setText("");
		controlador.mensajeDesde_GUI(TC.GUIAnadirAtributoEntidad_ActualizameLaListaDeDominios, null);
		
		//Genera Transfers
		this.comboTransfers.setModel(new javax.swing.DefaultComboBoxModel(listaTransfers));
		
		//Genera Dominios
		this.generaItems(nuevos);
		this.comboDominios.setModel(new javax.swing.DefaultComboBoxModel(nuevos));
		this.comboDominios.setSelectedItem("VARCHAR");
		if (this.activarTamano()){
			this.cajaTamano.setEditable(true);
			this.cajaTamano.setText("10");
		}
		else this.cajaTamano.setEditable(false);
		
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
	 * Getters y Setters
	 */
	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}

	/*
	 * Interfaz
	 */
	private JLabel getEligeTransfer() {
		if(eligeTransfer == null) {
			eligeTransfer = new JLabel();
			eligeTransfer.setFont(theme.font());
			eligeTransfer.setText(Lenguaje.text(Lenguaje.ELEMENTS));
			eligeTransfer.setBounds(25, 20, 232, 20);
			eligeTransfer.setOpaque(false);
			eligeTransfer.setFocusable(false);
		}
		return eligeTransfer;
	}
	private JComboBox getComboTransfers() {
		if(comboTransfers == null) {
			comboTransfers = new JComboBox();
			comboTransfers.setRenderer(new MyComboBoxRenderer());
			comboTransfers.setFont(theme.font());
			comboTransfers.setBackground(theme.background());
			comboTransfers.setBounds(25, 40, 231, 21);
			comboTransfers.addKeyListener(general);
		}
		return comboTransfers;
	}
	private JLabel getExplicacion() {
		if(explicacion == null) {
			explicacion = new JLabel();
			explicacion.setFont(theme.font());
			explicacion.setText(Lenguaje.text(Lenguaje.NAME));
			explicacion.setBounds(25, 65, 232, 20);
			explicacion.setOpaque(false);
			explicacion.setFocusable(false);
		}
		return explicacion;
	}
	
	private JCheckBox getOpcionClavePrimaria() {
		if(opcionClavePrimaria == null) {
			opcionClavePrimaria = new JCheckBox();
			opcionClavePrimaria.setFont(theme.font());
			opcionClavePrimaria.setText(Lenguaje.text(Lenguaje.PRIMARY_KEY_ATTRIBUTE));
			opcionClavePrimaria.setBounds(25, 115, 220, 18);
			opcionClavePrimaria.setOpaque(false);
			opcionClavePrimaria.setBorderPaintedFlat(true);
			opcionClavePrimaria.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent evt) {
					opcionClavePrimariaItemStateChanged(evt);
				}
			});
			opcionClavePrimaria.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){opcionClavePrimaria.setSelected(!opcionClavePrimaria.isSelected());}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return opcionClavePrimaria;
	}
	
	private JCheckBox getOpcionCompuesto() {
		if(opcionCompuesto == null) {
			opcionCompuesto = new JCheckBox();
			opcionCompuesto.setFont(theme.font());
			opcionCompuesto.setText(Lenguaje.text(Lenguaje.COMPOSITE_ATTRIBUTE));
			opcionCompuesto.setBounds(25, 134, 220, 18);
			opcionCompuesto.setOpaque(false);
			opcionCompuesto.setBorderPaintedFlat(true);
			opcionCompuesto.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent evt) {
					opcionCompuestoItemStateChanged(evt);
				}
			});
			opcionCompuesto.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10)opcionCompuesto.setSelected(!opcionCompuesto.isSelected());
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return opcionCompuesto;
	}
	
	private JCheckBox getOpcionNotnull() {
		if(opcionNotnull == null) {
			opcionNotnull = new JCheckBox();
			opcionNotnull.setFont(theme.font());
			opcionNotnull.setText(Lenguaje.text(Lenguaje.NOT_NULL_ATTRIBUTE));
			opcionNotnull.setBounds(25, 153, 220, 18);
			opcionNotnull.setOpaque(false);
			opcionNotnull.setBorderPaintedFlat(true);
			opcionNotnull.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10) opcionNotnull.setSelected(!opcionNotnull.isSelected());
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return opcionNotnull;
	}
	
	private JCheckBox getOpcionUnique() {
		if(opcionUnique == null) {
			opcionUnique = new JCheckBox();
			opcionUnique.setText(Lenguaje.text(Lenguaje.UNIQUE_ATTRIBUTE));
			opcionUnique.setBounds(25, 172, 220, 18);
			opcionUnique.setOpaque(false);
			opcionUnique.setFont(theme.font());
			opcionUnique.setBorderPaintedFlat(true);
			opcionUnique.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10)opcionUnique.setSelected(!opcionUnique.isSelected());
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		return opcionUnique;
	}
	
	private JCheckBox getOpcionMultivalorado() {
		if(opcionMultivalorado == null) {
			opcionMultivalorado = new JCheckBox();
			opcionMultivalorado.setFont(theme.font());
			opcionMultivalorado.setText(Lenguaje.text(Lenguaje.VALUE_ATTRIBUTE));
			opcionMultivalorado.setBounds(25, 191, 221, 18);
			opcionMultivalorado.setOpaque(false);
			opcionMultivalorado.setBorderPaintedFlat(true);
		}
		opcionMultivalorado.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==10) opcionMultivalorado.setSelected(!opcionMultivalorado.isSelected());
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		return opcionMultivalorado;
	}
	
	private JLabel getJTextPane2() {
		if(jTextPane2 == null) {
			jTextPane2 = new JLabel();
			jTextPane2.setFont(theme.font());
			jTextPane2.setText(Lenguaje.text(Lenguaje.DOMAIN_ATTRIBUTE));
			jTextPane2.setOpaque(false);
			jTextPane2.setBounds(25, 208, 231, 20);
			jTextPane2.setFocusable(false);
		}
		return jTextPane2;
	}
	
	private JComboBox getComboDominios() {
		if(comboDominios == null) {
			comboDominios = new JComboBox();
			comboDominios.setRenderer(new MyComboBoxRenderer());
			comboDominios.setFont(theme.font());
			comboDominios.setBounds(25, 230, 231, 27);
			comboDominios.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent evt) {
					comboDominiosItemStateChanged(evt);
				}
			});
			comboDominios.addKeyListener(general);
		}
		return comboDominios;
	}	
	private JLabel getLabelTamano() {
		if(labelTamano == null) {
			labelTamano = new JLabel();
			labelTamano.setFont(theme.font());
			labelTamano.setText(Lenguaje.text(Lenguaje.SIZE_ATTRIBUTE));
			labelTamano.setBounds(25, 263, 110, 14);
		}
		return labelTamano;
	}
	
	private JTextField getCajaTamano() {
		if(cajaTamano == null) {
			cajaTamano = new JTextField();
			cajaTamano.setFont(theme.font());
			cajaTamano.setForeground(theme.labelFontColorDark());
			cajaTamano.setBounds(25, 283, 166, 27);
		}
		cajaTamano.addKeyListener(general);
		return cajaTamano;
	}
	
	private JButton getBotonInsertar() {
		if(botonInsertar == null) {
			botonInsertar = this.botonInsertar(160, 330);
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
	
	private Object[] generaItems(Object[] items){
		int cont = 0;
		while (cont<this.listaDominios.size()){
			TransferDominio td = this.listaDominios.get(cont);
			items[cont] = td.getNombre();
			cont++;
		}
		return items;
	}
	
	/**
	 * Metodos privados
	 */
	
	private boolean activarTamano(){
		boolean activo = false;
		try{
			TipoDominio dominio = TipoDominio.valueOf(this.comboDominios.getSelectedItem().toString());
			if (((TipoDominio)dominio).equals(TipoDominio.CHAR) ||
					((TipoDominio)dominio).equals(TipoDominio.VARCHAR) ||
					((TipoDominio)dominio).equals(TipoDominio.TEXT) ||
					((TipoDominio)dominio).equals(TipoDominio.INTEGER) ||
					((TipoDominio)dominio).equals(TipoDominio.DECIMAL) ||
					((TipoDominio)dominio).equals(TipoDominio.FLOAT))
					activo = true;
			return activo;
		}catch(Exception e){
			return false;
			
		}
	}
	
    public Vector<TransferDominio> getListaDominios() {
		return listaDominios;
	}

	public void setListaDominios(Vector<TransferDominio> listaDominios) {
		this.listaDominios = listaDominios;
	}
	public Vector<Transfer> getListaTransfers() {
		return listaTransfers;
	}

	public void setListaTransfers(Vector<Transfer> lista) {
		this.listaTransfers = lista;
	}
	public void keyPressed( KeyEvent e ) {
		switch (e.getKeyCode()){
		case 27: 
			this.setInactiva();
			break;
		case 10:
			this.botonAnadirActionPerformed(null);
			break;
		}
	} 

	//Oyente para todos los elementos
	private KeyListener general = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode()==10)botonAnadirActionPerformed(null);	
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	};
}
