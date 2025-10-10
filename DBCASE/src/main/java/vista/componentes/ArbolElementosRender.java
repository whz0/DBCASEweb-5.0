package vista.componentes;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import modelo.transfers.TransferAtributo;
import modelo.transfers.TransferEntidad;
import modelo.transfers.TransferRelacion;
import persistencia.NodoEntidad;
import vista.iconos.attributeIcon;
import vista.iconos.entityChildIcon;
import vista.iconos.entityIcon;
import vista.iconos.isaIcon;
import vista.iconos.relationIcon;
import vista.iconos.subAttributeIcon;
import vista.tema.Theme;
/*
 * Clase para representar el arbol de elementos del panel de informacion
 * */
@SuppressWarnings("serial")
public class ArbolElementosRender extends DefaultTreeCellRenderer {

	private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";
	private static final String SUBSPAN_FORMAT = "<span style='color:%s;font-size:9px'>%s</span>";
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
      
      /*
       * if - else que crea el texto a mostrar y su icono
       * */
      if(userObject instanceof TransferAtributo){
    	  if(!((TransferAtributo) userObject).getCompuesto())
    		  text = String.format(SPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject + " : ")+
    			  String.format(SUBSPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), ((TransferAtributo) userObject).getDominio());
    	  else text = String.format(SPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject);
    	  if(((TransferAtributo) userObject).isSubatributo()) setIcon(new subAttributeIcon("mini"));
    	  else setIcon(new attributeIcon("mini"));
      }
      else if(userObject instanceof NodoEntidad) {
    	  /*
    	   * NodoEntidad representa las entidades dependientes de una relacion
    	   * Pueden ser de tres tipos
    	   * - Normal: forman parte de una relacion normal
    	   * - Padre: son la entidad padre de una IsA
    	   * - Hija: son la entidad hija de una IsA
    	   * */
    	  if(((NodoEntidad) userObject).esHija()) {
    		  text = String.format(SPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject);
    		  setIcon(new entityChildIcon("mini"));
    	  }
    	  else if(((NodoEntidad) userObject).esPadre()) {
    		  text = String.format(SPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject);
    		  setIcon(new entityIcon("mini"));
    	  }
    	  else {text = String.format(SPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject+": ")+
    			  String.format(SUBSPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), ((NodoEntidad) userObject).getRango());
    	  		setIcon(new entityIcon("mini"));
    	  }
      }
      else if(userObject instanceof TransferEntidad) {
    	  /*
    	   * Representa a una entidad dentro de la rama de Entidades, 
    	   * independientemente de las relaciones a las que pertenece
    	   * */
    	  text = String.format(SPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject);
    	  setIcon(new entityIcon("mini"));
      }
      else if(userObject instanceof TransferRelacion) {
    	  text = String.format(SPAN_FORMAT, sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue(), userObject);
    	  if((((TransferRelacion) userObject).getTipo().equals("IsA")))setIcon(new isaIcon("mini"));
    	  else setIcon(new relationIcon("mini"));
      }
      else {//Si no es de ningun tipo de los anteriores, es que es un titulo
    	  text =  "<h2 style='padding:10;color:"+ (sel?theme.labelFontColorLight().hexValue():theme.fontColor().hexValue())+";'>"+userObject+"</h2>";
    	  setIcon(null);
    	  this.setFocusable(false);
      }

	  this.setText("<html>" + text + "</html>");
	  return this;
	}
}
