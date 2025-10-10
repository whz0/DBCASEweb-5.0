package vista.iconos;

import javax.swing.Icon;

public abstract class icon implements Icon{
	
	private int size;
	private boolean pintarMas;
	private boolean selected;
	private final int DEFAULTSIZE = 60;
	private final int MINISIZE = 30;
	private final int PERSPECTIVESIZE = 50;
	protected double offset = .1;
	
	public icon() {
		super();
		pintarMas = true;
		size = DEFAULTSIZE;
	}
	
	public icon(String tipo) {
		super();
		switch(tipo) {
			case "mini": size = MINISIZE;pintarMas = false;break;
			case "perspective": size = PERSPECTIVESIZE;pintarMas = false;break;
			default: size = DEFAULTSIZE;pintarMas = true;
		}
	}
	public icon(String string, boolean selected) {
		this(string);
		this.selected = selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	protected boolean isSelected() {
		return selected;
	}
	
	protected boolean pintarMas() {
		return pintarMas;
	}
	@Override
    public int getIconWidth() {
      return size;
    }
    @Override
    public int getIconHeight() {
      return size == PERSPECTIVESIZE ? size :size/2;
    }
}
