package vista;

import java.awt.Container;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;


/*
 * Clase que maneja las perspectivas
 * */
public class Perspectiva {
	
	private Container mainPanel;
	private JPanel diagrama;
	private JPanel codigos;
	private JSplitPane diagramaSplitCode;
	private JSplitPane splitCodigos;
	private JSplitPane infoSplitMapa;
	private JSplitPane programmerSplit;
	private JTabbedPane infoPanel;
	private byte modo;
		
	public Perspectiva(Container mainPanel, JPanel diagrama, JPanel codigos, JTabbedPane infoPanel){
		this.mainPanel = mainPanel;
		this.diagrama = diagrama;
		this.codigos = codigos;
		this.infoPanel = infoPanel;
		this.splitCodigos = ((JSplitPane) codigos.getComponent(0));
		this.infoSplitMapa = (JSplitPane) (((JSplitPane) diagrama.getComponent(0)).getComponent(2));
		this.diagramaSplitCode = new JSplitPane();
		this.diagramaSplitCode.setBorder(null);
		this.programmerSplit = new JSplitPane();
		this.programmerSplit.setBorder(null);
	}
	
	public void loadDefaultView() {
		if(modo==0) modoVerTodo();
		else if(modo==1) modoDiseno();
		else if(modo==2) modoProgramador();
		else modoVerTodo();
	}
	/*
	 * Muestra todos los paneles
	 * */
	public void modoVerTodo() {
		mainPanel.removeAll();
		infoSplitMapa.add(infoPanel, JSplitPane.RIGHT);
		mainPanel.add(diagramaSplitCode);
		splitCodigos.setOrientation(JSplitPane.VERTICAL_SPLIT);
		diagramaSplitCode.add(codigos, JSplitPane.RIGHT);
		diagramaSplitCode.add(diagrama, JSplitPane.LEFT);
		infoSplitMapa.setResizeWeight(0.2);
		diagramaSplitCode.setResizeWeight(0);
		diagramaSplitCode.setVisible(true);
		programmerSplit.setVisible(false);
		mainPanel.revalidate();		
		mainPanel.repaint();
		modo = 0;
	}
	
	/*
	 * Muestra solo los paneles de diseno del diagrama
	 * */
	public void modoDiseno() {
		mainPanel.removeAll();
		infoSplitMapa.add(infoPanel, JSplitPane.RIGHT);
		mainPanel.add(diagrama);
		infoSplitMapa.setResizeWeight(0.2);
		diagramaSplitCode.setResizeWeight(0.1);
		diagramaSplitCode.setVisible(false);
		programmerSplit.setVisible(false);
		mainPanel.revalidate();
		mainPanel.repaint();
		modo = 1;
	}
	
	/*
	 * Muestra solo los paneles de edicion de codigo
	 * */
	public void modoProgramador() {
		mainPanel.removeAll();
		splitCodigos.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		programmerSplit.add(infoPanel, JSplitPane.LEFT);
		programmerSplit.add(codigos, JSplitPane.RIGHT);
		mainPanel.add(programmerSplit);
		diagramaSplitCode.setVisible(false);
		programmerSplit.setVisible(true);
		mainPanel.revalidate();
		mainPanel.repaint();
		modo = 2;
	}
	
	public byte getPanelsMode() {
		return modo;
	}
}
