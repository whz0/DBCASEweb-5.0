package vista.frames;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import vista.componentes.MyComboBoxRenderer;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;


@SuppressWarnings({"rawtypes" ,"unchecked", "serial"})
public class GUI_InsertarEntidad extends Parent_GUI{
	private Controlador controlador;
	private Point2D posicionEntidad;
	private JTextField cajaNombre = this.getCajaNombre(100, 10);
	private JCheckBox CasillaEsDebil;
	private JLabel explicacion;
	private JButton botonInsertar;
	private JTextField jTextRelacion;
	private JLabel nombreRelacion;
	private JComboBox comboEntidadesFuertes;
	private JLabel selecFuerte;
	private Vector<TransferEntidad> listaEntidades;
	private Vector<String> items;
	private TransferRelacion relacion;
	private boolean factibleEntidad;//Sirve para la comprobación de si se puede añadir una entidad debil

	public GUI_InsertarEntidad() {
		initComponents();
	}

	private void initComponents() {
		System.out.println("Probamos");
		/*
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(Lenguaje.text(Lenguaje.INSERT_ENTITY));
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setResizable(false);
		setModal(true);
		getContentPane().setLayout(null);
		cajaNombre.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==10){botonInsertarActionPerformed(null);}
				else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		{
			botonInsertar = botonInsertar(200,100);
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
			botonInsertar.setMnemonic(Lenguaje.text(Lenguaje.INSERT).charAt(0));
		}
		{
			explicacion = new JLabel();
			getContentPane().add(explicacion);
			explicacion.setText(Lenguaje.text(Lenguaje.NAME));
			explicacion.setOpaque(false);
			explicacion.setBounds(25, 10, 67, 25);
			explicacion.setFont(theme.font());
			explicacion.setFocusable(false);
			explicacion.setAlignmentX(0.0f);
		}
		{
			getContentPane().add(cajaNombre);
		}
		{
			CasillaEsDebil = new JCheckBox();
			getContentPane().add(CasillaEsDebil);
			CasillaEsDebil.setText(Lenguaje.text(Lenguaje.WEAK_ENTITY));
			CasillaEsDebil.setFont(theme.font());
			CasillaEsDebil.setBounds(100, 40, 170, 25);
			CasillaEsDebil.setOpaque(false);
			CasillaEsDebil.setBorderPaintedFlat(true);
			CasillaEsDebil.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10)
						CasillaEsDebil.setSelected(!CasillaEsDebil.isSelected());
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
			*/
			/*Si la entidad es debil, entonces hay que activar la parte de la ventana que permite seleccionar la entidad
			 * fuerte, y dar un nombre a la relación que unira la entidad fuerte y la débil.*/
		/*
			CasillaEsDebil.addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent arg0) {
					//Entidad débil
					if(CasillaEsDebil.isSelected()){
						//Le pedimos al controlador que nos actualice la lista de entidades
						controlador.mensajeDesde_GUI(TC.GUIAnadirEntidadARelacion_ActualizameListaEntidades, null);
						//Generamos los items del comboEntidades
						items = generaItems();
						//Los ordenamos alfabeticamente
						if(items.size() == 0)
							JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.CREATE_STRONG_ENTITY), Lenguaje.text(Lenguaje.ERROR), 0);
						else{
							ampliarVentana();
							jTextRelacion.setFont(theme.font());
							selecFuerte.setVisible(true);
							selecFuerte.setEnabled(true);
							comboEntidadesFuertes.setEnabled(true);
							comboEntidadesFuertes.setVisible(true);
							jTextRelacion.setEnabled(true);
							jTextRelacion.setVisible(true);
							nombreRelacion.setEnabled(true);
							nombreRelacion.setVisible(true);
							if(items.size() != 0)
								comboEntidadesFuertes.setModel(new javax.swing.DefaultComboBoxModel(items));
						}
					}
					//Entidad normal
					else{
						reducirVentana();
						selecFuerte.setEnabled(false);
						selecFuerte.setVisible(false);
						comboEntidadesFuertes.setEnabled(false);
						comboEntidadesFuertes.setVisible(false);
						jTextRelacion.setEnabled(false);
						jTextRelacion.setVisible(false);
						nombreRelacion.setEnabled(false);
						nombreRelacion.setVisible(false);
					}
				}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mousePressed(MouseEvent arg0) {}
				public void mouseReleased(MouseEvent arg0) {}
			});
		}
		{
			selecFuerte = new JLabel();
			getContentPane().add(selecFuerte);
			selecFuerte.setText(Lenguaje.text(Lenguaje.SELECT_STRONG_ENTITY));
			selecFuerte.setFont(theme.font());
			selecFuerte.setBounds(100, 78, 200, 25);
			selecFuerte.setOpaque(false);
			selecFuerte.setEnabled(false);
			selecFuerte.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			selecFuerte.setVisible(false);
		}
		{
			if(comboEntidadesFuertes == null)
				comboEntidadesFuertes = new JComboBox();
			comboEntidadesFuertes.setRenderer(new MyComboBoxRenderer());
			getContentPane().add(comboEntidadesFuertes);
			comboEntidadesFuertes.setEnabled(false);
			comboEntidadesFuertes.setBounds(100, 105, 200, 25);
			comboEntidadesFuertes.setFont(theme.font());
			comboEntidadesFuertes.setVisible(false);
		}
		{
			nombreRelacion = new JLabel();
			getContentPane().add(nombreRelacion);
			nombreRelacion.setText(Lenguaje.text(Lenguaje.WRITE_RELATION_WEAK));
			nombreRelacion.setBounds(100, 130, 200, 25);
			nombreRelacion.setFont(theme.font());
			nombreRelacion.setEnabled(false);
			nombreRelacion.setOpaque(false);
			nombreRelacion.setVisible(false);
		}
		{
			jTextRelacion = new JTextField();
			getContentPane().add(jTextRelacion);
			jTextRelacion.setEnabled(false);
			jTextRelacion.setBounds(100, 163, 200, 25);
			jTextRelacion.setBorder(BorderFactory.createLineBorder(theme.lines(), 1));
			jTextRelacion.setVisible(false);
			jTextRelacion.setForeground(theme.labelFontColorDark());
		}
		this.addMouseListener(this);
		this.addKeyListener(this);
*/
	}
	
	/*Redimensiona la ventana para que se puedan mostrar y solicitar los datos necesarios 
	 * para hacer las entidades débiles*/
	public void ampliarVentana(){
		this.setSize(350, 280); 
		botonInsertar.setBounds(200, 200, 100, 30);
	}
	
	/*Redimensiona la ventana para que se oculten la parte de las entidades débiles*/
	public void reducirVentana(){
		this.setSize(350, 178); 
		botonInsertar.setBounds(200, 100, 100, 30);
	}
	
	/*
	 * Oyentes de los botones
	 */
	private void botonInsertarActionPerformed(java.awt.event.ActionEvent evt) {  
		//Si es una entidad normal, sólo hay que crear dicha entidad
		if (!this.CasillaEsDebil.isSelected()){
			// Generamos el transfer que mandaremos al controlador
			TransferEntidad te = new TransferEntidad();
			te.setPosicion(this.getPosicionEntidad());
			te.setNombre(this.cajaNombre.getText());
			te.setDebil(this.CasillaEsDebil.isSelected());
			te.setListaAtributos(new Vector());
			te.setListaClavesPrimarias(new Vector());
			te.setListaRestricciones(new Vector());
			te.setListaUniques(new Vector());
			// Mandamos mensaje + datos al controlador
			this.getControlador().mensajeDesde_GUI(TC.GUIInsertarEntidad_Click_BotonInsertar, te);
		}
		/*Si es una entidad débil hay que crear dicha entidad, y además crear la relación débil que la unirá con
		 * una entidad fuerte (que existía antes)*/
		else if (!this.cajaNombre.getText().isEmpty()){
			TransferEntidad te = new TransferEntidad();
			//Si la entidad débil y la relación se llaman de diferente manera
			if (!this.cajaNombre.getText().equals(this.jTextRelacion.getText())){
				//Generamos el transfer que mandaremos al controlador para generar la entidad débil				
				te.setPosicion(this.getPosicionEntidad());
				te.setNombre(this.cajaNombre.getText());
				te.setDebil(this.CasillaEsDebil.isSelected());
				te.setListaAtributos(new Vector());
				te.setListaClavesPrimarias(new Vector());
				te.setListaRestricciones(new Vector());
				te.setListaUniques(new Vector());
				// Mandamos mensaje + datos al controlador
				this.getControlador().mensajeDesde_GUI(TC.GUIInsertarEntidadDebil_Click_BotonInsertar, te);
			}
			else{
				te.setNombre(this.cajaNombre.getText());
				this.getControlador().mensajeDesde_GUI(TC.GUIInsertarEntidadDebil_Entidad_Relacion_Repetidos,te);
				this.factibleEntidad=false;
				return;
			}
		}
	} 
	
	public void comprobadaEntidad(boolean factibleEntidad){
		this.factibleEntidad=factibleEntidad;
		//Si la entidad va a poder añadirse compruebo si la relación también va a poder añadirse
		if(factibleEntidad){
			//Generamos el transfer que mandaremos al controlador para generar la relación
			TransferRelacion tr = new TransferRelacion();
			tr.setPosicion((this.getPosicionEntidad()));
			tr.setNombre(this.jTextRelacion.getText());
			tr.setListaAtributos(new Vector());
			tr.setListaEntidadesYAridades(new Vector());			
			tr.setListaRestricciones(new Vector());
			tr.setListaUniques(new Vector());
			tr.setTipo("Debil");
			// Mandamos mensaje + datos al controlador
			this.getControlador().mensajeDesde_GUI(TC.GUIInsertarRelacionDebil_Click_BotonInsertar, tr);
		}					
	}
	
	public void comprobadaRelacion(boolean factibleRelacion){
		/*Si la entidad va a poder añadirse compruebo si la relación también va a poder añadirse
		 *  y la relacion también entonces es cuando creamos la entidad, y la relación*/
		if((items.size() != 0)&&(factibleRelacion)&&(this.factibleEntidad)){
			//Generamos el transfer que mandaremos al controlador para crear la entidad
			TransferEntidad te = new TransferEntidad();
			te.setPosicion(this.getPosicionEntidad());
			te.setNombre(this.cajaNombre.getText());
			te.setDebil(this.CasillaEsDebil.isSelected());
			te.setListaAtributos(new Vector());
			te.setListaClavesPrimarias(new Vector());
			te.setListaRestricciones(new Vector());
			te.setListaUniques(new Vector());
			// Mandamos mensaje + datos al controlador
			this.getControlador().mensajeDesde_GUI(TC.GUIInsertarEntidad_Click_BotonInsertar, te);
			
			//Generamos el transfer que mandaremos al controlador para crear la relación
			TransferRelacion tr = new TransferRelacion();
			Point2D p = new Point();
			p.setLocation(this.getPosicionEntidad().getX(), this.getPosicionEntidad().getY()+150);
			tr.setPosicion(p);
			tr.setNombre(this.jTextRelacion.getText());
			tr.setListaAtributos(new Vector());
			tr.setListaEntidadesYAridades(new Vector());			
			tr.setListaRestricciones(new Vector());
			tr.setListaUniques(new Vector());
			tr.setTipo("Debil");
			// Mandamos mensaje + datos al controlador
			this.getControlador().mensajeDesde_GUI(TC.GUIInsertarRelacion_Click_BotonInsertar, tr);
			//Unir la entidad fuerte con la relación
			//Mandaremos el siguiente vector al controlador
			Vector<Object> v = new Vector<Object>();
			v.add(tr);			
			v.add(this.getListaEntidades().get(indiceAsociado(this.comboEntidadesFuertes.getSelectedIndex())));
			v.add(Integer.toString(1));//Inicio
			v.add("n");//Fin
			v.add("");//Rol
			// Mandamos el mensaje y el vector con los datos
			this.controlador.mensajeDesde_GUI(TC.GUIAnadirEntidadARelacion_ClickBotonAnadir,v);
			
			//Unir la entidad debil con la relación
			//Mandaremos el siguiente vector al controlador
			Vector<Object> w = new Vector<Object>();
			w.add(tr);
			w.add(te);
			w.add(Integer.toString(1));//Inicio
			w.add("1");//Fin
			w.add("");//Rol
			// Mandamos el mensaje y el vector con los datos
			this.controlador.mensajeDesde_GUI(TC.GUIAnadirEntidadARelacion_ClickBotonAnadir,w);
		}
		if(items.size() == 0)
			JOptionPane.showMessageDialog(null, Lenguaje.text(Lenguaje.CREATE_STRONG_ENTITY), Lenguaje.text(Lenguaje.ERROR), 0);
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
	
	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		reducirVentana();
		this.jTextRelacion.setEnabled(false);
		this.selecFuerte.setEnabled(false);
		this.comboEntidadesFuertes.setEnabled(false);
		this.jTextRelacion.setEnabled(false);
		this.jTextRelacion.setText("");
		this.nombreRelacion.setEnabled(false);
		this.CasillaEsDebil.setSelected(false);
		this.centraEnPantalla();
		this.cajaNombre.setText("");
		this.cajaNombre.grabFocus();
		SwingUtilities.invokeLater(doFocus);
		this.setVisible(true);	
		this.factibleEntidad = false;
		selecFuerte.setVisible(false);
		comboEntidadesFuertes.setVisible(false);
		jTextRelacion.setVisible(false);
		nombreRelacion.setVisible(false);
	}
	
	private Runnable doFocus = new Runnable() {
	     public void run() {
	         cajaNombre.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}	
	
	//	@SuppressWarnings("unchecked")
	private Vector<String> generaItems(){
		// Generamos los items
		int cont = 0;
		int indice =0;
		Vector<String> items = new Vector<String>(this.listaEntidades.size());
		while (cont<this.listaEntidades.size()){
			TransferEntidad te = this.listaEntidades.get(cont);
			if (!te.isDebil()){
				items.add(indice,te.getNombre());
				indice++;
			}
			cont++;
		}
		return items;
	}
	
	/*Dada la posición seleccionada en el comboBox devuelve el índice correspondiente a dicho 
	 * elementeo en la lista de Entidades. Es necesario porque al ordenar alfabeticamente se perdió 
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

	/*
	 * Getters y Setters
	 */
	public Point2D getPosicionEntidad() {
		return posicionEntidad;
	}

	public void setPosicionEntidad(Point2D posicionEntidad) {
		this.posicionEntidad = posicionEntidad;
	}

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
