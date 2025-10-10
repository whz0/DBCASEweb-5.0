package vista.tema;

import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("unchecked")
public class Theme {

	private static Theme INSTANCE;
	private static ArrayList<String> themes;
	private static String current;
	private static HashMap<String,myColor> colors;
	private static final String DEFAULT = "light";
	private static Font font = new Font("Verdana", Font.PLAIN, 16);
		
	public static Theme getInstancia() {
		if(INSTANCE==null) INSTANCE = new Theme();
		return INSTANCE;
	}
	
	public static void loadThemes(){
		colors = new HashMap<String,myColor>();
		themes = new ArrayList<String>();
		listFilesForFolder(new File("./themes/"));
	}
	
	private static void listFilesForFolder(final File folder) {
	    for (final File fileEntry : folder.listFiles())
	    	if (fileEntry.isDirectory()) listFilesForFolder(fileEntry);
	        else  themes.add(fileEntry.getName().split(".json")[0]); 
	}

	public static void loadDefaultTheme(){
		current = Theme.DEFAULT;
		//loadTheme();
	}
	
	
	private static void loadTheme() {
		JSONParser parser = new JSONParser();
        try {
        	JSONObject obj = (JSONObject) parser.parse(new FileReader("./themes/"+current+".json"));
        	JSONObject jsoncolors = (JSONObject) obj.get("colors");
			Set<String> key = jsoncolors.keySet();
        	for(String s : key) {
        		JSONArray color = (JSONArray) jsoncolors.get(s);
        		colors.put(s, new myColor(Integer.parseInt(color.get(0).toString()), Integer.parseInt(color.get(1).toString()), Integer.parseInt(color.get(2).toString())));
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void changeTheme(String name) {
		if(themes.contains(name)) {
			current = name;
			loadTheme();
		}
	}
	
	public Font font(){
		return font;
	}
	
	public String getThemeName(){
		return current;
	}
	
	public ArrayList<String> getAvaiableThemes(){
		return themes;
	}
	/*
	 * Getters de los colores
	 * */
	public myColor main() {
		return colors.get("main");
	}
	public myColor background() {
		return colors.get("background");
	}
	public myColor entity() {
		return colors.get("entity");
	}
	public myColor attribute() {
		return colors.get("atribute");
	}
	public myColor relation() {
		return colors.get("relation");
	}
	public myColor fontColor() {
		return colors.get("fontColor");
	}
	public myColor lines() {
		return colors.get("lines");
	}
	public myColor control() {
		return colors.get("control");
	}
	public myColor codeBackground() {
		return colors.get("codeBackground");
	}
	public myColor SelectionBackground() {
		return colors.get("SelectionBackground");
	}
	public myColor labelFontColorDark() {
		return colors.get("labelFontColorDark");
	}
	public myColor labelFontColorLight() {
		return colors.get("labelFontColorLight");
	}
	public myColor paragraph() {
		return colors.get("paragraph");
	}
	public myColor blueFont() {
		return colors.get("blueFont");
	}
	public myColor toolBar() {
		return colors.get("toolBar");
	}
}
