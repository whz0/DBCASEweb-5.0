package vista.iconos;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import vista.tema.Theme;

public class entityIcon extends icon{
	public entityIcon() {
		super();
	}
    public entityIcon(String tipo) {
		super(tipo);
	}

	@Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
    	Graphics2D g2d = (Graphics2D) g;
    	RoundRectangle2D rect = new RoundRectangle2D.Double(x, y,getIconWidth()*.9, getIconHeight()*.8, 10, 10);
    	Theme theme = Theme.getInstancia();
    	g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    	g2d.setColor(theme.entity());
    	g2d.fill(rect);
    	g2d.setColor(theme.labelFontColorDark());
    	g2d.draw(rect);
    	if(pintarMas()) {
	    	g2d.draw(new Line2D.Double(getIconWidth()*.4,getIconHeight()*.4,getIconWidth()*.5,getIconHeight()*.4));
	        g2d.draw(new Line2D.Double(getIconWidth()*.45,getIconHeight()*.3,getIconWidth()*.45,getIconHeight()*.5));
    	}
    }	
}
