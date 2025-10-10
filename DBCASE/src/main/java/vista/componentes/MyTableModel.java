package vista.componentes;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

import vista.lenguaje.Lenguaje;

@SuppressWarnings("serial")
public class MyTableModel extends AbstractTableModel{
	private String[] columnas = 
		{Lenguaje.text(Lenguaje.TABLE), Lenguaje.text(Lenguaje.VOLUME), Lenguaje.text(Lenguaje.FREQ)};
	private ArrayList<ArrayList<String>> data;
	public MyTableModel(){
		super();
		data = new ArrayList<ArrayList<String>>();
	}
	public void refresh(String[][] s) {
		int size = s.length;
		data.clear();
		
		for (int row = 0; row < size; row ++) {
			ArrayList<String> Arr1 = new ArrayList<String>();
		    for (int col = 0; col < s[0].length; col++) Arr1.add(s[row][col]);
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
         if(col==0)return false;
         return true;
    }
	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex).get(columnIndex);
	}
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex){
		if(isInt((String) aValue)) {
			this.data.get(rowIndex).set(columnIndex, (String) aValue);
			fireTableDataChanged();
		}
	}
	
	private boolean isInt(String s) {
		try {Integer.parseInt(s);}
		catch(Exception e) {return false;}	  
		return true;
	}
}