package vista.componentes;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import controlador.Controlador;
import controlador.TC;
import vista.iconos.IconLabel;
import vista.iconos.perspective.allIcon;
import vista.iconos.perspective.codeIcon;
import vista.iconos.perspective.diagramIcon;
import vista.lenguaje.Lenguaje;
import vista.tema.Theme;

@SuppressWarnings("serial")
public class MyMenu extends JMenuBar{

	private JMenu menuSistema;
	private JMenuItem submenuNuevo;
	private JMenuItem submenuAbrir;
	private JMenuItem submenuGuardar;
	private JMenuItem submenuGuardarComo;
	private AbstractButton submenuImprimir;
	private AbstractButton submenuExportarJPEG;
	private AbstractButton submenuSalir;
	private AbstractButton menuOpciones;
	private JMenu menuLenguajes;
	private Vector<JRadioButtonMenuItem> elementosMenuLenguajes;
	private JRadioButtonMenuItem nullAttr;
	private JMenu optionsMenu;
	private JMenu menuAyuda;
	private AbstractButton submenuAcercaDe;
	private Theme theme;
	private MenuListener a;
	private diagramIcon diagramIcon;
	private codeIcon codeIcon;
	private allIcon allIcon;

	public MyMenu(Controlador c) {
		this.theme = Theme.getInstancia();
		setOpaque(true);
		setBorder(BorderFactory.createCompoundBorder(null,null));
		//File
		menuSistema = new JMenu();
		menuSistema.setForeground(theme.fontColor());
		menuSistema.setFont(theme.font());
		menuSistema.addMenuListener(a);
		add(menuSistema);
		menuSistema.setText(Lenguaje.text(Lenguaje.FILE));
		menuSistema.setMnemonic(Lenguaje.text(Lenguaje.FILE).charAt(0));
			//File/new
			submenuNuevo = new JMenuItem();
			submenuNuevo.setFont(theme.font());
			submenuNuevo.setForeground(theme.fontColor());
			menuSistema.add(submenuNuevo);
			submenuNuevo.setText(Lenguaje.text(Lenguaje.NEW));
			submenuNuevo.setMnemonic(Lenguaje.text(Lenguaje.NEW).charAt(0));
			submenuNuevo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_Submenu_Nuevo, null);
				}
			});
			//File/open
			submenuAbrir = new JMenuItem();
			submenuAbrir.setForeground(theme.fontColor());
			submenuAbrir.setFont(theme.font());
			menuSistema.add(submenuAbrir);
			submenuAbrir.setText(Lenguaje.text(Lenguaje.OPEN)+"...");
			submenuAbrir.setMnemonic(Lenguaje.text(Lenguaje.OPEN).charAt(0));
			submenuAbrir.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_Submenu_Abrir, null);
				}
			});
			
			//File/separator
			menuSistema.add(new JSeparator());
			//File/save
			submenuGuardar = new JMenuItem();
			submenuGuardar.setFont(theme.font());
			submenuGuardar.setForeground(theme.fontColor());
			menuSistema.add(submenuGuardar);
			submenuGuardar.setText(Lenguaje.text(Lenguaje.SAVE));
			submenuGuardar.setMnemonic(Lenguaje.text(Lenguaje.SAVE).charAt(0));
			submenuGuardar.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_Submenu_Guardar, null);
				}
			});
			//File/save as...
			submenuGuardarComo = new JMenuItem();
			submenuGuardarComo.setForeground(theme.fontColor());
			submenuGuardarComo.setFont(theme.font());
			menuSistema.add(submenuGuardarComo);
			submenuGuardarComo.setText(Lenguaje.text(Lenguaje.SAVE_AS)+"...");
			submenuGuardarComo.setMnemonic(Lenguaje.text(Lenguaje.SAVE_AS).charAt(1));
			submenuGuardarComo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_Submenu_GuardarComo, null);
				}
			});
			//File/separator2
			menuSistema.add(new JSeparator());
			//File/imprimir
			submenuImprimir = new JMenuItem();
			submenuImprimir.setFont(theme.font());
			submenuImprimir.setForeground(theme.fontColor());
			menuSistema.add(submenuImprimir);
			submenuImprimir.setText(Lenguaje.text(Lenguaje.PRINT_DIAGRAM));
			submenuImprimir.setMnemonic(Lenguaje.text(Lenguaje.PRINT_DIAGRAM).charAt(0));
			submenuImprimir.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_Imprimir, null);
				}
			});
			//File/Export
			submenuExportarJPEG = new JMenuItem();
			submenuExportarJPEG.setFont(theme.font());
			submenuExportarJPEG.setForeground(theme.fontColor());
			menuSistema.add(submenuExportarJPEG);
			submenuExportarJPEG.setText(Lenguaje.text(Lenguaje.EXPORT_DIAGRAM));
			submenuExportarJPEG.setMnemonic(Lenguaje.text(Lenguaje.EXPORT_DIAGRAM).charAt(0));
			submenuExportarJPEG.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					exportarJPG(evt);
				}
			});
			//File/Separator
			menuSistema.add(new JSeparator());
			//File/salir
			submenuSalir = new JMenuItem();
			submenuSalir.setFont(theme.font());
			submenuSalir.setForeground(theme.fontColor());
			menuSistema.add(submenuSalir);
			submenuSalir.setText(Lenguaje.text(Lenguaje.EXIT_MINCASE));
			submenuSalir.setMnemonic(Lenguaje.text(Lenguaje.EXIT_MINCASE).charAt(0));
			submenuSalir.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_Submenu_Salir, null);
				}
			});
		//Vista
		menuOpciones = new JMenu();
		menuOpciones.setForeground(theme.fontColor());
		menuOpciones.setFont(theme.font());
		
	    //menuOpciones.add(iconosPerspectiva);
		add(menuOpciones);
		menuOpciones.setText(Lenguaje.text(Lenguaje.OPTIONS));
		menuOpciones.setMnemonic(Lenguaje.text(Lenguaje.OPTIONS).charAt(0));
		optionsMenu = new JMenu();
		optionsMenu.setForeground(theme.fontColor());
		for(String s : this.theme.getAvaiableThemes()) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem();
			item.setText(s);
			item.setFont(theme.font());
			item.setForeground(theme.fontColor());
			item.setActionCommand(s);
			if(s.equals(theme.getThemeName()))item.setSelected(true);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_CambiarTema, e.getActionCommand());
					int i = 0;
					for(String s : theme.getAvaiableThemes()) {
						if(theme.getThemeName().equals(s))optionsMenu.getItem(i).setSelected(true);
						else optionsMenu.getItem(i).setSelected(false);
						i++;
					}
				}
			});
			optionsMenu.add(item);
		}
		optionsMenu.setFont(theme.font());
		menuOpciones.add(optionsMenu);
		optionsMenu.setText(Lenguaje.text(Lenguaje.THEME));
		//Opciones/Lenguaje
		menuLenguajes = new JMenu();
		menuLenguajes.setForeground(theme.fontColor());
		menuLenguajes.setFont(theme.font());
		menuOpciones.add(menuLenguajes);
		menuLenguajes.setText(Lenguaje.text(Lenguaje.SELECT_LANGUAGE));
		menuLenguajes.setMnemonic(Lenguaje.text(Lenguaje.SELECT_LANGUAGE).charAt(0));
		elementosMenuLenguajes = new Vector<JRadioButtonMenuItem>(0,1);
		Vector<String> lenguajes = Lenguaje.obtenLenguajesDisponibles();
		for (int m=0; m<lenguajes.size(); m++){
			JRadioButtonMenuItem lenguaje = new JRadioButtonMenuItem();
			lenguaje.setText(lenguajes.get(m));
			lenguaje.setFont(theme.font());
			lenguaje.setSelected(lenguajes.get(m).equalsIgnoreCase(Lenguaje.getIdiomaActual()));
			lenguaje.setForeground(theme.fontColor());
			lenguaje.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JRadioButtonMenuItem check = (JRadioButtonMenuItem) e.getSource();
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_CambiarLenguaje, check.getText());
					// Actualizar los checkBox
					for (int k=0; k<elementosMenuLenguajes.size(); k++){
						JRadioButtonMenuItem l = elementosMenuLenguajes.get(k);
						l.setSelected(l.getText().equalsIgnoreCase(Lenguaje.getIdiomaActual()));
					}
				}
			});
			menuLenguajes.add(lenguaje);
			elementosMenuLenguajes.add(lenguaje);
		}
		nullAttr = new JRadioButtonMenuItem();
		menuOpciones.add(nullAttr);
		nullAttr.setText(Lenguaje.text(Lenguaje.NULLATTR));
		nullAttr.setSelected(c.isNullAttrs());
		nullAttr.setForeground(theme.fontColor());
		nullAttr.setFont(theme.font());
		nullAttr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_NULLATTR, null);
			}
		});
		//Ayuda
		menuAyuda = new JMenu();
		menuAyuda.setForeground(theme.fontColor());
		menuAyuda.setFont(theme.font());
		add(menuAyuda);
		menuAyuda.setText(Lenguaje.text(Lenguaje.HELP));
		menuAyuda.setMnemonic(Lenguaje.text(Lenguaje.HELP).charAt(0));
			//Ayuda/acerca de
			submenuAcercaDe = new JMenuItem();
			submenuAcercaDe.setFont(theme.font());
			submenuAcercaDe.setForeground(theme.fontColor());
			menuAyuda.add(submenuAcercaDe);
			submenuAcercaDe.setText(Lenguaje.text(Lenguaje.ABOUT));
			submenuAcercaDe.setMnemonic(Lenguaje.text(Lenguaje.ABOUT).charAt(0));
			submenuAcercaDe.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_ABOUT, null);
				}
			});
			JToolBar iconosPerspectiva = new JToolBar();
			diagramIcon = new diagramIcon(false);
			IconLabel diagramLabel = new IconLabel(diagramIcon);
			diagramLabel.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mousePressed(MouseEvent e) {
	            	c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_ModoDiseno, null);
	            }
	        });
			allIcon = new allIcon(false);
			IconLabel allLabel = new IconLabel(allIcon);
			allLabel.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mousePressed(MouseEvent e) {
	            	c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_ModoVerTodo, null);
	            }
	        });
			codeIcon = new codeIcon(false);
			IconLabel codeLabel = new IconLabel(codeIcon);
			codeLabel.addMouseListener(new MouseAdapter() {
	            @Override
	            public void mousePressed(MouseEvent e) {
	            	c.mensajeDesde_GUIPrincipal(TC.GUI_Principal_Click_ModoProgramador, null);
	            }
	        });
			iconosPerspectiva.add(Box.createRigidArea(new Dimension(4,0)));
			iconosPerspectiva.add(diagramLabel);
			iconosPerspectiva.add(Box.createRigidArea(new Dimension(14,0)));
			iconosPerspectiva.add(allLabel);
			iconosPerspectiva.add(Box.createRigidArea(new Dimension(14,0)));
			iconosPerspectiva.add(codeLabel);
			iconosPerspectiva.add(Box.createRigidArea(new Dimension(4,0)));
			iconosPerspectiva.setBounds(0, 0, 60, 80);
			iconosPerspectiva.setFloatable(false);
			iconosPerspectiva.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); 
			add(iconosPerspectiva);
			add(Box.createRigidArea(new Dimension(50,0)));
	}
	
	/******************
	 * Listeners
	 ******************/
	private void exportarJPG(ActionEvent evt) {
		MyFileChooser fileChooser = new MyFileChooser();
		fileChooser.setDialogTitle(Lenguaje.text(Lenguaje.DBCASE));
		fileChooser.setFileFilter(new FileNameExtensionFilter(Lenguaje.text(Lenguaje.JPEG_FILES), "jpg"));
		int resul = fileChooser.showSaveDialog(null);
		if (resul == 0){
			File ruta = fileChooser.getSelectedFile();
			JOptionPane.showMessageDialog(
				null,
				Lenguaje.text(Lenguaje.INFO)+"\n"+
				Lenguaje.text(Lenguaje.OK_EXPORT)+".\n" +
				Lenguaje.text(Lenguaje.FILE)+": "+ruta,
				Lenguaje.text(Lenguaje.DBCASE),
				JOptionPane.PLAIN_MESSAGE);
		}
	}
	
	/************
	 * Modifiers
	 ************/
	public void setModoVista(int m) {
		diagramIcon.setSelected(m==1);
		allIcon.setSelected(m==0);
		codeIcon.setSelected(m==2);
		this.repaint();
	}
}