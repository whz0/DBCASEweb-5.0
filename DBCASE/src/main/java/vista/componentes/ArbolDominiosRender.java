package vista.componentes;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import vista.tema.Theme;
/*
 * Clase para representar el arbol de dominios del panel de informacion
 * */
@SuppressWarnings("serial")
public class ArbolDominiosRender extends DefaultTreeCellRenderer {

	private static final String TITLE_FORMAT = "<p style='color:%s;font-size:14px;text-align:center;padding:5px;'>%s</p>";
	private static final String CHILD_FORMAT = "<span style='color:%s;font-size:12px'>%s</span>";
	private static final String BASE_FORMAT = "<span style='color:%s;font-size:9px'>%s</span>";
	private Theme theme = Theme.getInstancia();
	
	/*
	 * Cada nodo del arbol llama a esta función, y segun su tipo lo representamos de una forma u otra
	 * */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      Object userObject = node.getUserObject();
      String text;
      if(esTitulo(node))
	      text = String.format(TITLE_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject);
      else if(esBasetype(node))
    	  text = String.format(BASE_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.paragraph().hexValue(), userObject);
      else text = String.format(CHILD_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject);
      
      setIcon(null);
	  this.setText("<html>" + text + "</html>");
	  return this;
	}
	
	private boolean esTitulo(DefaultMutableTreeNode node) {
		return  node.getLevel()==1;
	}
	
	private boolean esBasetype(DefaultMutableTreeNode node) {
		return  node.getLevel()==2 && node.getChildCount()==0;
	}
}
