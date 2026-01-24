package vista.componentes;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import vista.tema.Theme;

@SuppressWarnings("serial")
public class MyComboBoxRenderer extends BasicComboBoxRenderer {

    private final Theme theme = Theme.getInstancia();

    @SuppressWarnings("rawtypes")
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (isSelected) setBackground(theme.selectionBackground());
        else setBackground(theme.toolBar());
        return this;
    }
}