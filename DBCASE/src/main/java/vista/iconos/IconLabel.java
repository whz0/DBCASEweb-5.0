package vista.iconos;

import java.awt.Cursor;
import javax.swing.Icon;
import javax.swing.JLabel;
import vista.tema.Theme;

@SuppressWarnings("serial")
public class IconLabel extends JLabel{
	private Theme theme = Theme.getInstancia();
	public IconLabel(Icon icon){
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
