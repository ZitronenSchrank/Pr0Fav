import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class CSVManager {

    private static HashMap<Integer, String> stringMap = new HashMap<>();
    private static int beforeWrite, afterWrite;

    public static void readFile(File file) throws IOException {
        if(file.isFile()){
            //clearMap();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                //stringSet.add(line.replace("\n", "").replace("\r", ""));
                String firstField = line.split("([,])")[0];
                if(firstField.equals("ID")){
                   continue;
                }
                int key = Integer.parseInt(line.split("([,])")[0]);
                stringMap.put(key, line);
            }
        }

        System.out.println(stringMap.size());
        beforeWrite = stringMap.size();
    }

    public static int writeCSV(File file, Vector<ProApi.ProItem> items) throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter writer = new PrintWriter(file, "UTF-8");
        writer.println("ID,Promoted,Up,Down,Benis,Created,Image,Thumb,Fullsize,Width,Height,Audio,Source,Flag,Deleted,User,Mark,isVideo,Pr0-URL,File-URL");

        for (ProApi.ProItem item : items) {
            if(stringMap.get(item.getId()) == null) {
                StringBuilder itemBuilder = new StringBuilder();
                itemBuilder.append(item.getId() + ",");
                itemBuilder.append(item.getPromoted() + ",");
                itemBuilder.append(item.getUp() + ",");
                itemBuilder.append(item.getDown() + ",");
                itemBuilder.append(item.getUp() - item.getDown() + ",");
                itemBuilder.append(item.getCreated() + ",");
                itemBuilder.append(item.getImage() + ",");
                itemBuilder.append(item.getThumb() + ",");
                itemBuilder.append(item.getFullsize() + ",");
                itemBuilder.append(item.getWidth() + ",");
                itemBuilder.append(item.getHeight() + ",");
                itemBuilder.append(item.isAudio() + ",");
                itemBuilder.append(item.getSource() + ",");
                itemBuilder.append(item.getFlags() + ",");
                itemBuilder.append(item.getDeleted() + ",");
                itemBuilder.append(item.getUser() + ",");
                itemBuilder.append(item.getMark() + ",");
                itemBuilder.append(item.isVideo() + ",");
                itemBuilder.append("https://pr0gramm.com/new/" + item.getId() + ",");
                itemBuilder.append(item.getUrlToFile() + "");

                //System.out.println(itemBuilder);
                stringMap.put(item.getId(), itemBuilder.toString());
            }
        }

        for (String value : stringMap.values()) {
            writer.print(value);
            writer.println("");
        }

        System.out.println(stringMap.size());
        afterWrite = stringMap.size();
        writer.close();

        return afterWrite - beforeWrite;
    }

    public static void clearMap(){
        stringMap.clear();
    }
}

