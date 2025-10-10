package vista.componentes.GUIPanels;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import vista.tema.Theme;
/*
 * 
 * Es un JTextPanel con estilo personalizado
 * 
 * */
@SuppressWarnings("serial")
public class ReportPanel extends JTextPane{

	private JScrollPane scroll;
	private Theme theme = Theme.getInstancia();
	public ReportPanel() {
		setContentType("text/html");
		setBorder(null);
		//css
		setText("<style>"
				+ "body{background-color:"+theme.background().hexValue()+";margin:0}"
				+ "h2{padding-left:15px;font-size:18px}"
				+ "h3{padding-left:15px;font-size:14px}"
				+ "li{padding-top:10px}"
				+ ".card{border:1px solid "+theme.toolBar().hexValue()+";padding:5px;background:"+theme.control().hexValue()+";margin:15px;}"
				+ "p{padding-left:30px;color:"+theme.paragraph().hexValue()+"}"
				+ ".warning{background:"+theme.entity().hexValue()+";padding:5px;color:black;margin:15px;}"
				+ ".error{background:"+theme.relation().hexValue()+";padding:5px;color:white;margin:15px;}"
				+ "</style><p></p>");
		scroll = new JScrollPane(this);
	}
	
	public JScrollPane getPanel() {
		return scroll;
	}
	
	public void goToTop() {
		this.setCaretPosition(0);
	}
	
	@Override
	public String getText() {
		String text = super.getText();
		text = text.replaceAll("(?s)<div class=\"warning\">.*?</div>", "");
		text = text.replaceAll("\n","");
		text = text.replaceAll("<h2>","\n#");
		text = text.replaceAll("</h2>", "\n");
		text = text.replaceAll("</p>", "\n");
		text = text.replaceAll("&gt;",">");
		text = text.replaceAll("&lt;","<");
		text = text.replaceAll("(?s)<!--.*?-->", "");
		text = text.replaceAll("&#186;", "º");
		text = text.replaceAll("&#199;", "Ç");
		text = text.replaceAll("&#191;", "¿");
		text = text.replaceAll("&#231;", "ç");
		text = text.replaceAll("&#193;", "Á");
		text = text.replaceAll("&#194;", "Â");
		text = text.replaceAll("&#195;", "Ã");
		text = text.replaceAll("&#201;", "É");
		text = text.replaceAll("&#202;", "Ê");
		text = text.replaceAll("&#205;", "Í");
		text = text.replaceAll("&#212;", "Ô");
		text = text.replaceAll("&#213;", "Õ");
		text = text.replaceAll("&#211;", "Ó");
		text = text.replaceAll("&#218;", "Ú");
		text = text.replaceAll("&#225;", "á");
		text = text.replaceAll("&#226;", "â");
		text = text.replaceAll("&#227;", "ã");
		text = text.replaceAll("&#233;", "é");
		text = text.replaceAll("&#234;", "ê");
		text = text.replaceAll("&#237;", "í");
		text = text.replaceAll("&#244;", "ô");
		text = text.replaceAll("&#245;", "õ");
		text = text.replaceAll("&#243;", "ó");
		text = text.replaceAll("&#250;", "ú");
		text = text.replaceAll("<strong>", " ");
		text = text.replaceAll("</strong>", " ");
		text = text.replaceAll("\\<.*?>","");
		text = text.trim();
		text = text.replaceAll("\\v\\v"," ");
		text = text.replaceAll("[ ]{2,}"," ");
		text = text.replaceAll(" ;",";");
		text = text.replaceAll(" \\)","\\)");
		text = text.replaceAll(" ,",",");
		return text;
	}
	
	public String getInstrucciones() {
		String text = getText();
		text = text.replaceAll("#[^\n]*","");
		text = text.replaceAll("\n","");
		text = text.replaceAll(";",";\n");
		return text;
	}
}
