package vista.componentes;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.table.AbstractTableModel;

import vista.lenguaje.Lenguaje;

import static vista.lenguaje.Lenguaje.text;

@SuppressWarnings("serial")
public class MyTableModel extends AbstractTableModel {
    private final String[] columnas =
            {text(Lenguaje.TABLE), text(Lenguaje.VOLUME), text(Lenguaje.FREQ)};
    private final ArrayList<ArrayList<String>> data;

    public MyTableModel() {
        super();
        data = new ArrayList<>();
    }

    public void refresh(String[][] s) {
        data.clear();
        for (String[] strings : s) {
            data.add(new ArrayList<>(Arrays.asList(strings).subList(0, s[0].length)));
        }
    }

    @Override
    public String getColumnName(int index) {
        return columnas[index];
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col != 0;
    }

    @Override
    public String getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).get(columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (isInt((String) aValue)) {
            this.data.get(rowIndex).set(columnIndex, (String) aValue);
            fireTableDataChanged();
        }
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}