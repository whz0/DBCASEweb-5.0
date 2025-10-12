package vista.componentes;

import vista.lenguaje.Lenguaje;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("serial")
public class MyTableModel extends AbstractTableModel {
    private final String[] columnas =
            {Lenguaje.text(Lenguaje.TABLE), Lenguaje.text(Lenguaje.VOLUME), Lenguaje.text(Lenguaje.FREQ)};
    private final ArrayList<ArrayList<String>> data;

    public MyTableModel() {
        super();
        data = new ArrayList<ArrayList<String>>();
    }

    public void refresh(String[][] s) {
        int size = s.length;
        data.clear();

        for (int row = 0; row < size; row++) {
            ArrayList<String> Arr1 = new ArrayList<String>();
            Arr1.addAll(Arrays.asList(s[row]).subList(0, s[0].length));
            data.add(Arr1);
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