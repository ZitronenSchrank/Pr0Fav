package Pr0Fav;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

public class DownloadThread extends Thread {

    ProApi api;
    Vector<ProApi.ProItem> items;
    Path saveLocation;
    JLabel laStatus;
    JButton btDownload;
    boolean seperateFolders;
    final Path[] subfolders;

    public DownloadThread(ProApi api, Vector<ProApi.ProItem> items, Path saveLocation, JLabel laStatus, JButton btDownload, boolean seperateFolders){
        this.api = api;
        this.items = items;
        this.laStatus = laStatus;
        this.saveLocation = saveLocation;
        this.btDownload = btDownload;
        this.seperateFolders = seperateFolders;

        this.subfolders = new Path[]{
                this.saveLocation.resolve("sfw"),
                this.saveLocation.resolve("nsfw"),
                this.saveLocation.resolve("nsfl")
        };
    }

    @Override
    public void run() {
        btDownload.setEnabled(false);

        // Create subfolders if option checked
        if(this.seperateFolders){
            this.createSeperateFolders();
        }

        for(int i = 0; i < items.size(); i++){
            int randomInt = ThreadLocalRandom.current().nextInt(1, 1000);
            System.out.print(i + " (" + (2200 + randomInt) + ") : ");

            Path saveFileLocation = saveLocation;

            if(seperateFolders){
                switch (items.get(i).getFlags()){
                    case ProApi.SFW:
                        saveFileLocation = saveFileLocation.resolve(this.subfolders[0]);
                        break;
                    case ProApi.NSFW:
                        saveFileLocation = saveFileLocation.resolve(this.subfolders[1]);
                        break;
                    case  ProApi.NSFL:
                        saveFileLocation = saveFileLocation.resolve(this.subfolders[2]);
                        break;
                }
            }


            if(api.downloadItem(items.get(i), saveFileLocation)){
                try {

                    laStatus.setText(i+1 + " von " + items.size() + " (" + Math.floor((((i+1) / (double) items.size()) * 100.00)) + "%) runtergeladen.");
                    Thread.sleep(2200 + randomInt);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    btDownload.setEnabled(true);
                }
            }
        }
        btDownload.setEnabled(true);
        laStatus.setText("Download abgeschlossen.");
    }

    private void createSeperateFolders(){


        for(Path path: subfolders){
            if(!Files.exists(path)) {
                try {
                    Files.createDirectory(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}