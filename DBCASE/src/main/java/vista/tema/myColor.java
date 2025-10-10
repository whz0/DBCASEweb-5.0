package vista.tema;

import java.awt.Color;

@SuppressWarnings("serial")
public class myColor extends Color{
	int r, g, b;
	public myColor(int r, int g, int b) {
		super(r, g, b);
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public String hexValue() {
		return String.format("#%02x%02x%02x", r, g, b);
	}
}
