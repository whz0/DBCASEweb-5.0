package vista.componentes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.accessibility.AccessibleContext;
import javax.swing.JFileChooser;
import vista.tema.Theme;

/*
 * Clase que modifica los colores de fuente del JFileChooser
 * */
@SuppressWarnings("serial")
public class MyFileChooser extends JFileChooser{
	Theme theme = Theme.getInstancia();
	public MyFileChooser(){
		setColors(this.getComponent(2), theme.labelFontColorDark());
		setColors(((Container) ((Container) this.getComponent(3)).getComponent(0)).getComponent(1), theme.labelFontColorDark());
	}
	
	private static void setColors(Component c, Color fg) {
	    setColors0(c.getAccessibleContext(), fg);
	}

	/*
	 * Metodo recursivo
	 * */
	private static void setColors0(AccessibleContext ac, Color fg) {
		ac.getAccessibleComponent().setForeground(fg);
	    int n = ac.getAccessibleChildrenCount();
	    for (int i=0; i<n; i++)
	        setColors0(ac.getAccessibleChild(i).getAccessibleContext(), fg);
	}
}
