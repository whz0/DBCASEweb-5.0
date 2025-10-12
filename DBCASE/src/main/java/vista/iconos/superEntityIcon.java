package vista.iconos;

import vista.tema.Theme;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class superEntityIcon extends icon {

    public superEntityIcon() {
        super();
    }

    public superEntityIcon(String tipo) {
        super(tipo);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        Rectangle2D rect = new Rectangle2D.Double();
        Theme theme = Theme.getInstancia();
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        //g2d.setColor(theme.superEntity());
        g2d.fill(rect);
        g2d.setColor(theme.labelFontColorDark());
        g2d.draw(rect);
        if (pintarMas()) {
            g2d.draw(new Line2D.Double(getIconWidth() * .4, getIconHeight() * .4, getIconWidth() * .5, getIconHeight() * .4));
            g2d.draw(new Line2D.Double(getIconWidth() * .45, getIconHeight() * .3, getIconWidth() * .45, getIconHeight() * .5));

        }
    }

}
