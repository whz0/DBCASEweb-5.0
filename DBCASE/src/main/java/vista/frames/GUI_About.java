package vista.frames;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextPane;

import vista.componentes.GUIPanels.ReportPanel;
import vista.imagenes.ImagePath;
import vista.lenguaje.Lenguaje;
import vista.tema.Theme;

@SuppressWarnings("serial")
public class GUI_About extends JFrame{
	
	private JTextPane texto;
	private Theme theme = Theme.getInstancia();
	
	public GUI_About() {
		this.setTitle(Lenguaje.text(Lenguaje.DBCASE_LABEL));
		this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource(ImagePath.LOGODBDT)).getImage());
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
		setResizable(false);
		//setUndecorated(true);
		this.setSize(600, 900);
		
		texto = new ReportPanel();
		texto.setEditable(false);
		this.add(texto);
	}
	public void setActiva(boolean b) {
		setLocationRelativeTo(null);
		texto.setText("");
		//css
		texto.setText("<style>"
				+ "body{background-color:"+theme.background().hexValue()+";margin:0;padding:0;}"
				+ "h1{text-align:center;font-size:30px;padding-top:15px;color:"+theme.fontColor().hexValue()+"}"
				+ "h2{font-style:italic;text-align:center;font-size:14px;color:"+theme.paragraph().hexValue()+"}"
				+ "h3{text-align:center;font-size:12px;color:"+theme.fontColor().hexValue()+"}"
				+ "li{padding-top:0px;margin-left:8px}"
				+ "p{font-size:11px;padding-left:30px;color:"+theme.paragraph().hexValue()+"}"
				+ "</style><p></p>");
		//texto
		texto.setText(
			"<body><h1>"+Lenguaje.text(Lenguaje.DB_CASE_TOOL)+"</h1>" +
			"<h2>"+Lenguaje.text(Lenguaje.TOOL_FOR_DESING)+"</h2>" +
			"<p><strong>"+Lenguaje.text(Lenguaje.DIRECTOR)+"</strong> "+
				Lenguaje.text(Lenguaje.TEACHER_NAME)+"</p>"+
			"<p><strong>"+Lenguaje.text(Lenguaje.AUTHOR)+"</strong> "+
				Lenguaje.text(Lenguaje.AUTHOR1)+"</p><br>"+
			"<h3>"+Lenguaje.text(Lenguaje.BASED)+"</h3>"+
			"<p><strong>"+Lenguaje.text(Lenguaje.BASED1)+"</strong></p>"+
			"<p>"+Lenguaje.text(Lenguaje.AUTHORS)+"</p>"+
			"<ul><li><p>"+Lenguaje.text(Lenguaje.BASED1A1)+"</p></li>"+
			"<li><p>"+Lenguaje.text(Lenguaje.BASED1A2)+"</p></li>"+
			"<li><p>"+Lenguaje.text(Lenguaje.BASED1A3)+"</p></li></ul><br>"+
			"<p><strong>"+Lenguaje.text(Lenguaje.BASED2)+"<strong></p>"+
			"<p>"+Lenguaje.text(Lenguaje.AUTHORS)+"</p>"+
			"<ul><li><p>"+Lenguaje.text(Lenguaje.BASED2A1)+"</p></li>"+
			"<li><p>"+Lenguaje.text(Lenguaje.BASED2A2)+"</p></li>"+
			"<li><p>"+Lenguaje.text(Lenguaje.BASED2A3)+"</p></li>"+
			"<li><p>"+Lenguaje.text(Lenguaje.BASED2P)+"</p></li></ul><br><br><br><br>"+
			"<p>"+Lenguaje.text(Lenguaje.SS_II)+"</p>"+
			"<p>"+Lenguaje.text(Lenguaje.COLLEGE)+". "+
			Lenguaje.text(Lenguaje.UNIVERSITY)+"</p><br>"+
			"<p>"+Lenguaje.text(Lenguaje.CONTACT) +"</p>"+
			"<p>Source code: https://github.com/miguelarriba/DBCase</p>"+"</body>");
		this.setVisible(true);
	}
}
