package vista.tema;

import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("unchecked")
public class Theme {

    private static Theme INSTANCE;
    private static ArrayList<String> themes;
    private static String current;
    private static HashMap<String, MyColor> colors;
    private static final String DEFAULT = "light";
    private static final Font font = new Font("Verdana", Font.PLAIN, 16);

    public static Theme getInstancia() {
        if (INSTANCE == null) INSTANCE = new Theme();
        return INSTANCE;
    }

    public static void loadThemes() {
        colors = new HashMap<>();
        themes = new ArrayList<>();
        listFilesForFolder(new File("./themes/"));
    }

    private static void listFilesForFolder(final File folder) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles()))
            if (fileEntry.isDirectory()) listFilesForFolder(fileEntry);
            else themes.add(fileEntry.getName().split(".json")[0]);
    }

    public static void loadDefaultTheme() {
        current = Theme.DEFAULT;
        //loadTheme();
    }

    private static void loadTheme() {
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(new FileReader("./themes/" + current + ".json"));
            JSONObject jsonColors = (JSONObject) obj.get("colors");
            Set<String> key = jsonColors.keySet();
            for (String s : key) {
                JSONArray color = (JSONArray) jsonColors.get(s);
                colors.put(s, new MyColor(Integer.parseInt(color.get(0).toString()), Integer.parseInt(color.get(1).toString()), Integer.parseInt(color.get(2).toString())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeTheme(String name) {
        if (themes.contains(name)) {
            current = name;
            loadTheme();
        }
    }

    public Font font() {
        return font;
    }

    public String getThemeName() {
        return current;
    }

    public ArrayList<String> getAvailableThemes() {
        return themes;
    }

    /*
     * Getters de los colores
     * */
    public MyColor main() {
        return colors.get("main");
    }

    public MyColor background() {
        return colors.get("background");
    }

    public MyColor entity() {
        return colors.get("entity");
    }

    public MyColor attribute() {
        return colors.get("atribute");
    }

    public MyColor relation() {
        return colors.get("relation");
    }

    public MyColor fontColor() {
        return colors.get("fontColor");
    }

    public MyColor lines() {
        return colors.get("lines");
    }

    public MyColor control() {
        return colors.get("control");
    }

    public MyColor codeBackground() {
        return colors.get("codeBackground");
    }

    public MyColor selectionBackground() {
        return colors.get("SelectionBackground");
    }

    public MyColor labelFontColorDark() {
        return colors.get("labelFontColorDark");
    }

    public MyColor labelFontColorLight() {
        return colors.get("labelFontColorLight");
    }

    public MyColor paragraph() {
        return colors.get("paragraph");
    }

    public MyColor blueFont() {
        return colors.get("blueFont");
    }

    public MyColor toolBar() {
        return colors.get("toolBar");
    }
}
