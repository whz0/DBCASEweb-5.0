package vista.iconos;

import vista.tema.Theme;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class IconLabel extends JLabel {
    private final Theme theme = Theme.getInstancia();

    public IconLabel(Icon icon) {
        super(icon);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public IconLabel(Icon icon, String text) {
        super(icon);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setText(text);
        setFont(theme.font());
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
    }
}
