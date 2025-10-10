package vista.frames;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JTextField;

import vista.lenguaje.Lenguaje;
import vista.tema.Theme;

@SuppressWarnings("serial")
public class Parent_GUI extends javax.swing.JDialog implements KeyListener, MouseListener{

	protected Theme theme = Theme.getInstancia();
	
	public void centraEnPantalla(){
		setAlwaysOnTop(false);
		setLocationRelativeTo(null);
	}
	
	protected JButton botonInsertar(int x, int y) {
		return boton(x,y,Lenguaje.text(Lenguaje.INSERT));
	}
	
	protected JButton botonCancelar(int x, int y) {
		return boton(x,y,Lenguaje.text(Lenguaje.CANCEL));
	}
	
	protected JTextField getCajaNombre(int x, int y) {
		JTextField cajaNombre = new JTextField();
		cajaNombre.setFont(theme.font());
		cajaNombre.setBounds(x,y, 232, 30);
		cajaNombre.setForeground(theme.labelFontColorDark());
		return cajaNombre;
	}
	
	protected JButton boton(int x, int y, String name) {
		JButton boton = new JButton();
		boton.setFont(theme.font());
		boton.setText(name);
		boton.setBounds(x,y, name.length()>8 ? 110+name.length()*4: name.length()<4 ?60:110, 30);
		return boton;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		this.requestFocus();
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
	
}
