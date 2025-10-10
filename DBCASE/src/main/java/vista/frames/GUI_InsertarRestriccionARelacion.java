package vista.frames;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import controlador.Controlador;
import controlador.TC;
import modelo.transfers.TransferRelacion;
import vista.componentes.CustomCellEditor;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;

@SuppressWarnings("serial")
public class GUI_InsertarRestriccionARelacion extends Parent_GUI {

	private Controlador controlador;
	private TransferRelacion relacion;
	private JScrollPane jScrollPane1;
	private JButton botonNueva;
	private JButton botonEliminar;
	private JButton botonAceptar;
	private JTable tabla;

	public GUI_InsertarRestriccionARelacion() {
		initComponents();
	}

	private void initComponents() {

	setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setResizable(false);
		setModal(true);
		getContentPane().setLayout(null);
		this.setSize(400, 350);
		{
			jScrollPane1 = new JScrollPane();
			getContentPane().add(jScrollPane1);
			jScrollPane1.setBounds(0, 0, 340, 250);
		}
		{
			botonAceptar = boton(270, 275,Lenguaje.text(Lenguaje.ACCEPT));
			getContentPane().add(botonAceptar);
			botonAceptar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonAceptarActionPerformed(evt);
				}
			});
			botonAceptar.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==10){botonCancelarActionPerformed(null);}
					else if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
				}
				public void keyReleased(KeyEvent e) {}
				public void keyTyped(KeyEvent e) {}
			});
		}
		{
			botonNueva = new JButton();
			botonNueva.setFont(theme.font());
			getContentPane().add(botonNueva);
			botonNueva.setText("+");
			botonNueva.setBounds(350, 10, 45, 45);
			botonNueva.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {}
				@Override
				public void keyReleased(KeyEvent e) {}
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==27){botonCancelarActionPerformed(null);}
					else if(e.getKeyCode()==39){botonEliminar.grabFocus();}
				}
			});
			botonNueva.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonNuevaActionPerformed(evt);
				}
			});
		}
		{
			botonEliminar = new JButton();
			getContentPane().add(botonEliminar);
			botonEliminar.setFont(theme.font());
			botonEliminar.setText("-");
			botonEliminar.setBounds(350, 55, 45, 45);
			botonEliminar.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {}
				@Override
				public void keyReleased(KeyEvent e) {}
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==27) botonCancelarActionPerformed(null);
					else if(e.getKeyCode()==37) botonNueva.grabFocus();
				}
			});
			botonEliminar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					botonEliminarActionPerformed(evt);
				}
			});
		}

		this.addMouseListener(this);
		TableModel tablaModel = new DefaultTableModel(new String[][] {{""}},new String[]{ Lenguaje.text(Lenguaje.RESTRICTIONS)+":"});
		tabla = new JTable();
		jScrollPane1.setViewportView(tabla);
		tabla.setModel(tablaModel);
		tabla.setFont(theme.font());
		tabla.setBackground(theme.background());
		DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
		    @Override
		    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row, int column) {
		        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,row, column);
		        setFont(theme.font());
		        return this;
		    }
		};
        setForeground(theme.background());
        tabla.setDefaultEditor(Object.class, CustomCellEditor.make());
		tabla.getColumnModel().getColumn(0).setCellRenderer(r);
		tabla.setRowHeight(25);
		tabla.getTableHeader().setReorderingAllowed(false);//columnas fijadas
		this.addKeyListener(this);
	}

	/*
	 * Oyentes de los botones 
	 */
	private void botonAceptarActionPerformed(java.awt.event.ActionEvent evt) {  
		tabla.editCellAt(0,0);
		Vector<String> predicados = new Vector<String>();
		int i=0;
		while (i<tabla.getRowCount()){
			String s= tabla.getValueAt(i, 0).toString();
			if (!s.equals(""))
				predicados.add(s);
			i++;
		}
		Vector<Object> v = new Vector<Object>();
		v.add(predicados);
		v.add(this.relacion);
		controlador.mensajeDesde_GUI(TC.GUIPonerRestriccionesARelacion_Click_BotonAceptar, v);
		this.setInactiva();
	}                                       
		
	private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {                                              
		this.setInactiva();
	}
	
	private void botonNuevaActionPerformed(ActionEvent evt) {
		tabla.editCellAt(0,0);
		String[][] items = new String[tabla.getRowCount()+1][1];
		int i=0;
		while (i<tabla.getRowCount()){
			items[i][0]=tabla.getValueAt(i,0).toString();
			i++;
		}
		items[tabla.getRowCount()][0]="";
		TableModel tablaModel = 
			new DefaultTableModel(
				items,
				new String[] { Lenguaje.text(Lenguaje.RESTRICTIONS)+":" });
		tabla.setModel(tablaModel);
	}
	
	private void botonEliminarActionPerformed(ActionEvent evt) {
		try{
			int selec =tabla.getSelectedRow();
			String[][] items = new String[tabla.getRowCount()-1][1];
			int i=0;
			int j=0;
			while (i<tabla.getRowCount()){
				if (selec!=i){
					items[j][0]=tabla.getValueAt(i,0).toString();
					j++;
				}
				i++;
			}
			TableModel tablaModel = 
				new DefaultTableModel(
						items,
						new String[] { Lenguaje.text(Lenguaje.RESTRICTIONS)+":" });
			tabla.setModel(tablaModel);
			
		}catch(Exception e){
			System.out.println("no hay ninguna seleccionada");
		}
		
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
	
	/*
	 * Activar y desactivar el dialogo
	 */
	public void setActiva(){
		this.centraEnPantalla();
		this.setTitle(Lenguaje.text(Lenguaje.ADD_RESTRICTIONS) + relacion.getNombre());
		muestraRestricciones();
		SwingUtilities.invokeLater(doFocus);
		this.setVisible(true);
	}
	
	private Runnable doFocus = new Runnable() {
	     public void run() {
	         botonNueva.grabFocus();
	     }
	 };
	
	public void setInactiva(){
		this.setVisible(false);
	}

	/*
	 * Utilidades
	 */
	private void muestraRestricciones(){
		String[][] items = new String[relacion.getListaRestricciones().size()][1];
		int i=0;
		while (i<relacion.getListaRestricciones().size()){
			items[i][0]=relacion.getListaRestricciones().get(i).toString();
			i++;
		}
		TableModel tablaModel = 
			new DefaultTableModel(items,new String[] { Lenguaje.text(Lenguaje.RESTRICTIONS)+":" });
		tabla.setModel(tablaModel);
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
	
	public void setRelacion(TransferRelacion relacion){
		this.relacion = relacion;
	}
}
