package Pr0Fav;

import org.json.*;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

/**
 * Bietet Methoden zum arbeiten mit der Pr0-API mit Java.
 */
public class ProApi {

    public static final String PRO_URL = "https://pr0gramm.com";
    public static final int SFW = 9;
    public static final int NSFL = 4;
    public static final int NSFW = 2;
    public static final int ALL = SFW + NSFL + NSFW;

    private final String COOKIES_HEADER = "Set-Cookie";
    private final String PRO_LOGIN = PRO_URL + "/api/user/login";
    private final String PRO_USER_INFO = PRO_URL + "/api/user/info";
    // private final String PRO_LOGOUT = PRO_URL + "/api/user/logout";
    private final String PRO_ITEM_GET = PRO_URL + "/api/items/get";
    // private final String PRO_ITEM_INFO = PRO_URL + "/api/items/info?itemId=";

    private final String PRO_COOKIE_PATH = System.getProperty("user.home")+File.separator+ ".ProFav"+File.separator;
    private final String PRO_COOKIE_FILE_NAME = PRO_COOKIE_PATH+"ProFav.cookie";
    private final String PRO_DEFAULTPATH_FILE_NAME = PRO_COOKIE_PATH+"ProFav.txt";

    private CookieManager cookieManager;
    //private String userName = "";
    private boolean isCookieValid = false;

    private JLabel statusLabel;

    public ProApi() {
        cookieManager = new CookieManager();
    }
    public ProApi(JLabel statusLabel) { cookieManager = new CookieManager(); this.statusLabel = statusLabel; }

    /**
     * Sammelt alle Favoriten oder Uploads eines Users.
     * @param collection Welche Colletion runtergeladen werden soll. Leerer String Hochlads der Person.
     * @param name Der User wessen Favoriten oder Uploads runtergeladen werden sollen.
     * @param flag Aus welchen Kategorien die Favoriten kommen sollen.
     * @return Gibt eine ArrayList mit allen Favoriten zurück.
     */
    public Vector<ProItem> getItemList(String collection, String name, int flag) {
        Vector<ProItem> favList = new Vector<ProItem>();
        String favString = !collection.equals("") ? "&collection=" + collection : "";
        try {
            // Holt die ersten 120 Favoriten
            String s = sendGet(new URL(PRO_ITEM_GET + "?flags=" + flag + "&" + "user" + "=" + name + favString));

            // Erstellt die Liste in der die Favoriten gespeichert werden sollen.
            favList = new Vector<ProItem>();

            // Parse das JSON Ergebnis
            JSONObject obj = new JSONObject(s);

            // Schaue ob das Ende erreicht wurde
            try {
                boolean atEnd = obj.getBoolean("atEnd");

                // Wandle die Items im JSON Array in ein Array um mit welchem Java umgehen kann
                JSONArray array = obj.getJSONArray("items");

                // erstelle ein JSONObeject in der die JSON Informationen zum aktuellen Item gespeichert werden sollen.
                JSONObject favItem;

                // Loope solange bis "atEnd" erreicht.
                while (!atEnd) {
                    // Loope durch das JSONArray und füge allle Elemente als ProItem in die ArrayList favList ein.
                    for (int i = 0; i < array.length(); i++) {
                        favItem = array.getJSONObject(i);
                        favList.add(createProItemFromJSON(favItem));
                    }

                    // Hole die nächsten 120 Favoriten.
                    s = sendGet(new URL(PRO_ITEM_GET + "?older=" + favList.get(favList.size() - 1).id + "&flags=" + flag + "&" + "user" + "=" + name + favString));
                    obj = new JSONObject(s);

                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {

                    }
                    this.writeStatus("Es wurden " + favList.size() + " Uploads gefunden...");
                    // Schaue ob diesmal ein Ende erreicht wurde.
                    atEnd = obj.getBoolean("atEnd");
                    array = obj.getJSONArray("items");
                }
                // Falls "atEnd" erreicht wurde wird aus der while-Schleife gesprungen
                // Füge hier die letzten paar Favoriten zum Array hinzu.
                for (int i = 0; i < array.length(); i++) {
                    favItem = array.getJSONObject(i);
                    favList.add(createProItemFromJSON(favItem));
                    this.writeStatus("Es wurden " + favList.size() + " Uploads gefunden...");
                }
            } catch (JSONException je) {
                System.out.println(je);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.writeCookieFile();
        return favList;
    }


    public String checkIfValidCookie() {
        String cookieContent = this.readCookieFile();
        if(cookieContent != null) {
            String splitContent[] = cookieContent.split(";");

            int index = 0;
            boolean found = false;
            for(index = 0; index < splitContent.length; index++){
                if(splitContent[index].contains("me=%")) {
                    found = true;
                    break;
                }
            }

            if(found){
                try {
                    JSONObject userInfo = new JSONObject(URLDecoder.decode(splitContent[index].replace(';',' ').substring(3), StandardCharsets.UTF_8.name()));
                    String name = userInfo.getString("n");
                    isCookieValid = true;

                    String s = sendGet(new URL(PRO_USER_INFO));

                    JSONObject res = new JSONObject(s);

                    res.getJSONObject("account");

                    return name;
                } catch (Exception ex) {
                    isCookieValid = false;
                    cookieManager.getCookieStore().removeAll();
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Logt sich in das Pr0 ein.
     *
     * @param name     Name des Nutzers
     * @param password Passwort des Nutzers
     * @param captcha Das gelöste Captcha
     * @param token Das Token welches zum Captcha gehört.
     * @return Gibt ein LoginResult zurück, in welchem steht ob alles erfolgreich verlaufen ist.
     */
    public LoginResult login(String name, String password, String captcha, String token) {
        //userName = name;
        String s = "";
        try {
            // Sende ein PostRequest mit Parametern in der richtigen Form
            s = sendPost(new URL(PRO_LOGIN), "name=" + name + "&password=" + password + "&captcha=" + captcha + "&token="+ token +"");

            // Wandle das Ergebnis vom Server in ein LoginResult um.
            JSONObject obj = new JSONObject(s);
            boolean success = obj.getBoolean("success");
            Object ban = obj.get("ban");
            return new LoginResult(success, ban);

        } catch (MalformedURLException e) {

        }
        return new LoginResult(false, "");
    }

    /**
     * Holt sich ein Captcha um sich einloggen zu können.
     * @return Ein Captcha Objekt mit dem Bild und Token.
     */
    public Captcha getCaptcha() throws MalformedURLException, IOException{

        String s = sendGet(new URL("https://pr0gramm.com/api/user/captcha"));
        JSONObject obj = new JSONObject(s);
        String base64String = obj.getString("captcha");
        String token = obj.getString("token");
        String imgString = base64String.split(",")[1];

        BufferedImage image;

        byte[] imageByte;

        Base64.Decoder decoder = Base64.getDecoder();
        imageByte = decoder.decode(imgString);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
        image = ImageIO.read(bis);
        bis.close();

        return new Captcha(token, image);
    }

    /**
     * Lädt ein ProItem runter und speichert es.
     *
     * @param item         Das Item welches runtergeladen werden soll
     * @param saveLocation Der Ort wo es gespeichert werden soll
     * @return True falls Download erfolgreich, false bei Fehler/Exceptions
     */
    public boolean downloadItem(ProItem item, String saveLocation) {
        try {
            System.out.println("Downloading: " + item.getUrlToFile());
            // Trenne item.getImage() beim ersten Punkt (Zum Erhalten des Typs)
            String ext = item.getImage().split("([.])")[1];
            File newFile = new File(saveLocation + "/" + item.getId() + "." + ext);

            // Falls Datei schon vorhanden lade sie nicht nochmal runter
            if (!newFile.isFile()) {

                // Lade die Datei runter
                URL url = new URL(item.getUrlToFile());
                InputStream in = new BufferedInputStream(url.openStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();
                byte[] response = out.toByteArray();

                // Erstelle eine neue Datei auf der Festplatte und schrieb den Inhalt rein.
                FileOutputStream fos = new FileOutputStream(newFile);
                fos.write(response);
                fos.close();
                return true;
            }
            // False falls Datei bereits vorhanden
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            // False bei Exception
            return false;
        }
    }

    /**
     * Sendet ein get-Request an eine URL
     *
     * @param getUrl Die Ziel-URL
     * @return Die Antwort vom Server
     */
    private String sendGet(URL getUrl) {
        try {
            // Baut eine Verbindung zur URL auf
            HttpsURLConnection con = (HttpsURLConnection) getUrl.openConnection();
            con.setRequestMethod("GET");
            getCookies(con);

            // Antwort erhalten.
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + getUrl);
            System.out.println("Response Code : " + responseCode);
            setCookies(con);

            if(responseCode >= 500){
                System.out.println("Error! Warte 15 Sekunden.");
                Thread.sleep(15000);

                con = (HttpsURLConnection) getUrl.openConnection();
                con.setRequestMethod("GET");
                getCookies(con);

                // Antwort erhalten.
                responseCode = con.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + getUrl);
                System.out.println("Response Code : " + responseCode);
                setCookies(con);
            }

            // Speicher Antwort in der Variable Response.
            return getResponse(con);

        } catch (Exception e) {

        }
        return "";
    }

    /**
     * Sendet ein post-Request an eine URL
     *
     * @param getUrl     Die Ziel-URL
     * @param parameters Die Parameter für das post-Request.
     * @return Die Antwort vom Server
     */
    private String sendPost(URL getUrl, String parameters) {
        try {
            // Baut eine Verbindung zur URL auf
            HttpsURLConnection con = (HttpsURLConnection) getUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:66.0) Gecko/20100101 Firefox/66.0");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            getCookies(con);

            // Sendet das Post Request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(parameters);
            wr.flush();
            wr.close();

            // Response
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + getUrl);
            System.out.println("Response Code : " + responseCode);
            //System.out.println("Parameter: " + parameters);

            // Cookie
            setCookies(con);

            // Speicher Antwort in der Variable Response.
            return getResponse(con);

        } catch (Exception e) {

        }
        return "";
    }

    private String getResponse(HttpsURLConnection con) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    /**
     * Wandelt die Cookies die im CookieManager liegen in eine brauchbare Form.
     *
     * @return Die Cookies in der Form "name=wert;(...);"
     */
    private String cookiesToString() {
        StringBuilder cookies = new StringBuilder();
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            for (HttpCookie c : cookieManager.getCookieStore().getCookies()) {
                cookies.append(c + ";");
            }
        }
        return cookies.toString();
    }

    /**
     * Erstellt aus einem JSONObject ein ProItem.
     *
     * @param object das JSONObject
     * @return Ein Pr0Item mit den Werten aus dem JSONObject.
     */
    private ProItem createProItemFromJSON(JSONObject object) {
        int id = object.getInt("id");
        int promoted = object.getInt("promoted");
        int up = object.getInt("up");
        int down = object.getInt("down");
        int created = object.getInt("created");
        String image = object.getString("image");
        String thumb = object.getString("thumb");
        String fullsize = object.getString("fullsize");
        int width = object.getInt("width");
        int height = object.getInt("height");
        boolean audio = object.getBoolean("audio");
        String source = object.getString("source");
        int flags = object.getInt("flags");
        int gift = object.getInt("gift");
        String user = object.getString("user");
        int mark = object.getInt("mark");

        return new ProItem(id, promoted, up, down, created, image, thumb, fullsize, width, height, audio, source, flags, user, mark, gift);
    }

    /**
     * Setzt Cookies in den CookieManager, falls dies vom Server verlangt
     *
     * @param con Die Connection mit der Antwort vom Server
     */
    private void setCookies(HttpsURLConnection con) {
        Map<String, List<String>> headerFields = con.getHeaderFields();
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        }
    }

    private String readCookieFile() {
        File f = new File(PRO_COOKIE_FILE_NAME);
        StringBuilder cookies = new StringBuilder();
        if(f.isFile()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    cookies.append(line);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        else {
            return null;
        }
        return cookies.toString();
    }

    public void writeCookieFile() {
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            String cookies = cookiesToString();
            try {

                File f = new File(PRO_COOKIE_PATH);
                if(!f.isDirectory()) f.mkdirs();

                PrintWriter writer = new PrintWriter(new File(PRO_COOKIE_FILE_NAME), "UTF-8");
                writer.println(cookies);
                writer.flush();
                writer.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sendet die Cookies vom CookieManager mit dem HTTP Request mit.
     *
     * @param con Die Connection mit dem Request.
     */
    private void getCookies(HttpsURLConnection con) {
        if (!isCookieValid) {
            String cookies = cookiesToString();
            con.setRequestProperty("Cookie", cookies);
        } else {
            String cookies = readCookieFile();
            con.setRequestProperty("Cookie", cookies);
        }
    }

    /**
     * Eine Nachricht wird in den StatusLabel geschrieben
     * @param message Die Nachricht die geschrieben werden soll.
     */
    private void writeStatus(String message){
        if(statusLabel != null){
            statusLabel.setText(message);
        }
    }

    private File readDefaultLocationFile(){
        File f = new File(PRO_DEFAULTPATH_FILE_NAME);

        if(f.isFile()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line = br.readLine();
                br.close();

                if(line != null) {
                    File path = new File(line);
                    if(path.isDirectory()) {
                        return path;
                    }
                }

                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        else {
            return null;
        }
    }

    private void writeToDefaultLocation(String location) {
        try {

            File f = new File(PRO_COOKIE_PATH);
            if(!f.isDirectory()) f.mkdirs();

            PrintWriter writer = new PrintWriter(new File(PRO_DEFAULTPATH_FILE_NAME), "UTF-8");
            writer.println(location);
            writer.flush();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public File getDefaultLocation() {
        File path = this.readDefaultLocationFile();

        if(path == null){
            return FileSystemView.getFileSystemView().getHomeDirectory();
        } else {
            return path;
        }
    }

    public void setDefaultLocation(String path) {
        this.writeToDefaultLocation(path);
    }

    /**
     * Das Ergebis eines Login Versuches
     */
    class LoginResult {
        private boolean success;
        private Object ban;

        LoginResult(boolean success, Object ban) {
            this.success = success;
            this.ban = ban;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getBan() {
            if (ban == null) {
                return "";
            } else {
                return ban.toString();
            }
        }
    }

    /**
     * Ein Pr0-Item mit allen möglichen Informationen.
     */
    class ProItem {
        private int id;
        private int promoted;
        private int up;
        private int down;
        private int created;
        private String image;
        private String thumb;
        private String fullsize;
        private int width;
        private int height;
        private boolean audio;
        private String source;
        private int flags;
        private int gift;
        private String user;
        private int mark;

        private boolean isVideo = false;
        private String urlToFile = "";

        public ProItem(int id, int promoted, int up, int down, int created, String image, String thumb, String fullsize, int width, int height, boolean audio, String source, int flags, String user, int mark, int gift) {
            this.id = id;
            this.promoted = promoted;
            this.up = up;
            this.down = down;
            this.created = created;
            this.image = image;
            this.thumb = thumb;
            this.fullsize = fullsize;
            this.width = width;
            this.height = height;
            this.audio = audio;
            this.source = source.replaceAll("([,])", "");
            this.flags = flags;
            this.gift = gift;
            this.user = user.replaceAll("([,])", "");
            this.mark = mark;

            if (this.image.toLowerCase().contains(".mp4") || this.image.toLowerCase().contains(".webm")) {
                isVideo = true;
                urlToFile = "https://vid.pr0gramm.com/" + this.image;
            } else {
                urlToFile = "https://img.pr0gramm.com/" + this.image;
            }
        }

        public boolean isVideo() {
            return isVideo;
        }

        public String getUrlToFile() {
            return urlToFile;
        }

        public int getId() {
            return id;
        }

        public int getPromoted() {
            return promoted;
        }

        public int getUp() {
            return up;
        }

        public int getDown() {
            return down;
        }

        public int getCreated() {
            return created;
        }

        public String getImage() {
            return image;
        }

        public String getThumb() {
            return thumb;
        }

        public String getFullsize() {
            return fullsize;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isAudio() {
            return audio;
        }

        public String getSource() {
            return source;
        }

        public int getFlags() {
            return flags;
        }

        public int getGift() {
            return gift;
        }

        public String getUser() {
            return user;
        }

        public int getMark() {
            return mark;
        }
    }

    /**
     * Das Captcha welches für den Login benötigt wird.
     */
    class Captcha{
        private  String token = "";
        private BufferedImage image;

        Captcha(String token, BufferedImage image){
            this.token = token;
            this.image = image;
        }

        public String getToken() {
            return token;
        }

        public BufferedImage getImage() {
            return image;
        }
    }

}