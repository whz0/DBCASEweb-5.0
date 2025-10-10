package vista.iconos;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import vista.tema.Theme;

public class subAttributeIcon extends icon{
  
	public subAttributeIcon() {
		super();
	}
    public subAttributeIcon(String tipo) {
		super(tipo);
	}

	@Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
    	Graphics2D g2d = (Graphics2D) g;
    	Theme theme = Theme.getInstancia();
    	g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    	g2d.setColor(theme.attribute());
    	g2d.fill(new Ellipse2D.Double(x+getIconWidth()*.25, y-getIconHeight()*.07,getIconWidth()*.7, getIconHeight()*.85));
    	g2d.setColor(theme.labelFontColorDark());
    	g2d.draw(new Ellipse2D.Double(x+getIconWidth()*.25, y-getIconHeight()*.07,getIconWidth()*.7, getIconHeight()*.85));
    	g2d.setColor(theme.lines());
    	g2d.draw(new Line2D.Double(0,getIconHeight()*.75,getIconWidth()*.25,getIconHeight()*.75));
	    g2d.draw(new Line2D.Double(0,0,0,getIconHeight()*.75));
    }
}
