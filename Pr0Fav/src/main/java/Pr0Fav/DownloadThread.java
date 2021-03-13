package Pr0Fav;

import javax.swing.*;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

public class DownloadThread extends Thread {

    ProApi api;
    Vector<ProApi.ProItem> items;
    String saveLocation;
    JLabel laStatus;
    JButton btDownload;

    public DownloadThread(ProApi api, Vector<ProApi.ProItem> items, String saveLocation, JLabel laStatus, JButton btDownload){
        this.api = api;
        this.items = items;
        this.laStatus = laStatus;
        this.saveLocation = saveLocation;
        this.btDownload = btDownload;
    }

    @Override
    public void run() {
        super.run();
        btDownload.setEnabled(false);
        for(int i = 0; i < items.size(); i++){
            int randomInt = ThreadLocalRandom.current().nextInt(1, 1000);
            System.out.print(i + " (" + (2200 + randomInt) + ") : ");
            if(api.downloadItem(items.get(i), saveLocation)){
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
}