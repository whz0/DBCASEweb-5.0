package vista.frames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import controlador.Controlador;
import controlador.TC;
import vista.componentes.MyFileChooser;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;


@SuppressWarnings("serial")
public class GUI_SaveAs extends Parent_GUI{

	private Controlador controlador;
	private JPanel panelPrincipal;
	private MyFileChooser jfc;
	private JLabel jLabel1;
	private int abrir;
	private boolean actuado; //vale true tras guardar o abrir, false si pulsa en cancelar o cierra la ventana
	
	public GUI_SaveAs() {
		this.initComponents();
	}

	private void initComponents(){
		this.setTitle(Lenguaje.text(Lenguaje.DBCASE));
		this.setModal(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		panelPrincipal = new JPanel();
		panelPrincipal.setLayout(null);
		getContentPane().add(panelPrincipal, BorderLayout.CENTER);
		panelPrincipal.setPreferredSize(new java.awt.Dimension(545, 318));
		jLabel1 = new JLabel();
		jLabel1.setFont(theme.font());
		panelPrincipal.add(jLabel1);
		jLabel1.setBounds(12, 12, 521, 14);
		jfc = new MyFileChooser();
		jfc.setFont(theme.font());
		jfc.setCurrentDirectory(new File(System.getProperty("user.dir")+"/projects"));
		panelPrincipal.add(jfc);
		jfc.setBounds(0, 32, 547, 286);
		jfc.setDialogType(2);
		jfc.setFileSelectionMode(MyFileChooser.FILES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		XMLFileFilter filter = new XMLFileFilter();
		jfc.addChoosableFileFilter(filter);
		jfc.setApproveButtonText(Lenguaje.text(Lenguaje.SELECT));
		jfc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jfcActionPerformed(evt);
			}
		});
		this.setSize(553, 354);
		this.setContentPane(panelPrincipal);
	}
	
	/*
	 * Activar y desactivar el dialogo
	 */
	
	//devuelve siempre true, salvo si se ha pulsado en cancelar
	public boolean setActiva(int b){
		actuado=false;
		switch (b){			
			case 1:{//abrir
				jLabel1.setText(Lenguaje.text(Lenguaje.OPEN)+":");
				abrir=b;
				this.centraEnPantalla();
				this.setVisible(true);	
				break;
			}
			case 2:{//guardar
				jLabel1.setText(Lenguaje.text(Lenguaje.SAVE)+":");
				if(controlador.getFileguardar()==null || !controlador.getFileguardar().exists()){
					abrir=b;
					this.centraEnPantalla();
					this.setVisible(true);	
				}
				else{
					actuado=true;
					guardarProyecto();	
				}
				break;
			}
			case 3:{//guardarComo
				jLabel1.setText(Lenguaje.text(Lenguaje.SAVE_AS)+":");
				abrir=b;
				this.centraEnPantalla();
				this.setVisible(true);	
				break;
			}
		}
		return actuado;
	}
	
	public void setInactiva(){
		this.setVisible(false);
	}

	/*
	 * Oyente del MyFileChooser
	 */
	private void jfcActionPerformed(ActionEvent evt) {
		String command = evt.getActionCommand();
		if (command.equals(MyFileChooser.APPROVE_SELECTION)){
			actuado=true;
			switch (abrir){
				case 1: abrirProyecto(); break;
				case 2: guardarComo(); break;
				case 3: guardarComo(); break;
			}
			this.dispose();
		}
		// Si se ha pulsado el boton cancelar
		else if (command.equals(MyFileChooser.CANCEL_SELECTION)) {
			actuado=false;
			this.dispose();
		}
	}
	
	private void abrirProyecto(){
		File f = this.jfc.getSelectedFile();
		//construimos la ruta
		String ruta = f.getPath();
		if (!ruta.endsWith(".xml")) ruta = ruta+".xml";
		//si ya existe, 
		if (f.exists()){
			//compruebo que sea un xml de esta aplicación
			if (esValido(f)){
				controlador.setFileguardar(f);
				this.controlador.mensajeDesde_GUIWorkSpace(TC.GUI_WorkSpace_Click_Abrir, ruta);
			}else{
				JOptionPane.showMessageDialog(null,
						Lenguaje.text(Lenguaje.WRONG_FILE),
						Lenguaje.text(Lenguaje.DBCASE), JOptionPane.ERROR_MESSAGE);
			}				
		}else{
			JOptionPane.showMessageDialog(null,
					Lenguaje.text(Lenguaje.NOT_EXIST_FILE),
					Lenguaje.text(Lenguaje.DBCASE), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void guardarProyecto(){
		String ruta=controlador.getFileguardar().getAbsolutePath();
		if (!ruta.endsWith(".xml")) ruta = ruta+".xml";
		if (controlador.getFileguardar().exists())
			this.controlador.mensajeDesde_GUIWorkSpace(TC.GUI_WorkSpace_Click_Guardar, ruta);
	}
	
	private void guardarComo(){
		File f = this.jfc.getSelectedFile();
		String ruta = f.getPath();
		if (!ruta.endsWith(".xml")) ruta = ruta+".xml";
		
		//si ya existe, 
		if (f.exists()) this.controlador.mensajeDesde_GUIWorkSpace(TC.GUI_WorkSpace_Click_Guardar, ruta);
		else this.controlador.mensajeDesde_GUIWorkSpace(TC.GUI_WorkSpace_Click_Guardar, ruta);
		f = new File(ruta);
		controlador.setFileguardar(f);
	}
			
	/*GETTERS & SETTERS*/
	public Controlador getControlador() {
		return controlador;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}

	@SuppressWarnings("static-access")
	public void nuevoTemp(){
		try {
			controlador.setFiletemp(File.createTempFile("dbcase", "xml"));
			controlador.creaFicheroXML(controlador.getFiletemp());
			String ruta= controlador.getFiletemp().getAbsolutePath();
			this.controlador.mensajeDesde_GUIWorkSpace(TC.GUI_WorkSpace_Nuevo, ruta);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
				Lenguaje.text(Lenguaje.ERROR_TEMP_FILE),
				Lenguaje.text(Lenguaje.DBCASE), JOptionPane.ERROR_MESSAGE);
		}
		controlador.setFileguardar(null);
	}
	

	/*
	 * METODOS PARA MANEJAR LOS FICHEROS XML
	 */
	class XMLFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
		}
		@Override
		public String getDescription() {
			return "xml files";
		}
	}
	

	private boolean esValido(File f) {
		try{
			Document doc= dameDoc(f);
			//comprobamos que sea de la forma de nuestros ficheros
			NodeList LC = doc.getElementsByTagName("Inf_dbcase");
			if (LC.getLength()!=1)
				return false;
			else{
				if( LC.item(0).getChildNodes().item(1).getNodeName().equals("EntityList") &&
					LC.item(0).getChildNodes().item(3).getNodeName().equals("RelationList") &&
					LC.item(0).getChildNodes().item(5).getNodeName().equals("AttributeList") &&
					LC.item(0).getChildNodes().item(7).getNodeName().equals("DomainList"))
						return true;
				else return false;
			}	
		}catch(Exception e){
			return false;
		}
	}
	private Document dameDoc(File f) {
		Document doc = null;
		DocumentBuilder parser = null;
		try {
			DocumentBuilderFactory factoria = DocumentBuilderFactory.newInstance();
			parser = factoria.newDocumentBuilder();
			doc = parser.parse(f);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(
					null,
					Lenguaje.text(Lenguaje.ERROR)+":\n" +
					Lenguaje.text(Lenguaje.UNESPECTED_XML_ERROR)+" \""+f.getName() ,
					Lenguaje.text(Lenguaje.DBCASE),
					JOptionPane.ERROR_MESSAGE);
		}
		return doc;
	}
}
