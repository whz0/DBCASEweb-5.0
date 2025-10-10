package vista.iconos;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import vista.tema.Theme;

public class isaIcon extends icon{
	public isaIcon() {
		super();
	}
    public isaIcon(String tipo) {
		super(tipo);
	}

	@Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
    	Graphics2D g2d = (Graphics2D) g;
    	Theme theme = Theme.getInstancia();
    	g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    	g2d.setColor(theme.relation());
    	Polygon s = new Polygon();
        s.addPoint((int) (getIconWidth()*.2), (int) (getIconHeight()*.2));
        s.addPoint((int) (getIconWidth()*.5), (int) (getIconHeight()));
        s.addPoint((int) (getIconWidth()*.8), (int) (getIconHeight()*.2));
        g.fillPolygon(s);
        g2d.setColor(theme.labelFontColorDark());
        g.drawPolygon(s);
        if(pintarMas()) {
        	g2d.setColor(theme.labelFontColorLight());
	        g2d.draw(new Line2D.Double(getIconWidth()*.45,getIconHeight()*.5,getIconWidth()*.55,getIconHeight()*.5));
	        g2d.draw(new Line2D.Double(getIconWidth()*.5,getIconHeight()*.4,getIconWidth()*.5,getIconHeight()*.6));
        }
    }
}
