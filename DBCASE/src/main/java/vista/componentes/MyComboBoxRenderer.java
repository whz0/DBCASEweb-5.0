package vista.componentes;

import vista.tema.Theme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class MyComboBoxRenderer extends BasicComboBoxRenderer {

    private final Theme theme = Theme.getInstancia();

    @SuppressWarnings("rawtypes")
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (isSelected) setBackground(theme.SelectionBackground());
        else setBackground(theme.toolBar());
        return this;
    }
}