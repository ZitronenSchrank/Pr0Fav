import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Base64;
import java.util.Vector;

public class Window extends JFrame implements ActionListener{

    private JButton btLogin;
    private JButton btDirChoose;
    private JButton btDownload;
    private JButton btMetaData;
    private JButton btOtherFav;
    private JButton btOwnFav;
    private JButton btOtherPost;
    private JButton btOwnPost;

    private JLabel laStatus;
    private JLabel captchaImage;

    private JScrollPane spResponse;
    private JTextArea taResponse;

    private JPlaceHolderTextField tfName;
    private JPlaceHolderTextField tfLocation;
    private JPlaceHolderTextField tfOtherName;
    private JPlaceHolderTextField tfCaptcha;
    private JPlaceHolderPassword tfPassword;

    private JCheckBox cbSFW;
    private JCheckBox cbNSFW;
    private JCheckBox cbNSFL;

    private JFileChooser fcSaveLoc;

    private JPanel mainPanel;
    private JPanel northPanel;
    private JPanel northGridPanel;
    private JPanel southPanel;
    private JPanel southGridPanel;
    private JPanel southBorderLayout;
    private JPanel southBorderGridLayout;

    private ProApi api;

    private String captchaToken = "";

    private boolean loggedIn = false;

    private Vector<ProApi.ProItem> items;

    Window() {
        super();

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("Fav Downloader");

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());

        northGridPanel = new JPanel();
        northGridPanel.setLayout(new GridLayout(0,4));

        southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());

        southGridPanel = new JPanel();
        southGridPanel.setLayout(new GridLayout(0,2));

        southBorderLayout = new JPanel();
        southBorderLayout.setLayout(new BorderLayout());

        southBorderGridLayout = new JPanel();
        southBorderGridLayout.setLayout(new GridLayout(0, 4));

        btLogin = new JButton("Login");
        btDirChoose = new JButton("Ordner suchen...");
        btDownload = new JButton("Download");
        btMetaData = new JButton("Als CSV");
        btOtherFav = new JButton("Andere Favoriten");
        btOwnFav = new JButton("Eigene Favoriten");
        btOtherPost = new JButton("Andere Hochlads");
        btOwnPost = new JButton("Eigene Hochlads");
        btDownload.setEnabled(false);
        btMetaData.setEnabled(false);
        btOwnFav.setEnabled(false);
        btOtherFav.setEnabled(false);
        btOtherPost.setEnabled(false);
        btOwnPost.setEnabled(false);

        laStatus = new JLabel("Nicht eingeloggt.");
        captchaImage = new JLabel();

        tfName = new JPlaceHolderTextField();
        tfName.setPlaceholder("Name...");

        tfOtherName = new JPlaceHolderTextField();
        tfOtherName.setPlaceholder("Anderer Nutzername...");

        tfLocation = new JPlaceHolderTextField(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath());
        tfLocation.setPlaceholder("Pfad...");

        tfPassword = new JPlaceHolderPassword();
        tfPassword.setPlaceholder("Password...");

        tfCaptcha = new JPlaceHolderTextField();
        tfCaptcha.setPlaceholder("Captcha...");

        cbSFW = new JCheckBox("SFW");
        cbNSFW= new JCheckBox("NSFW");
        cbNSFL= new JCheckBox("NSFL");
        cbNSFW.setSelected(true);
        cbSFW.setSelected(true);
        cbNSFL.setSelected(true);

        taResponse = new JTextArea();
        spResponse = new JScrollPane(taResponse);
        taResponse.setEditable(false);

        fcSaveLoc = new JFileChooser();
        fcSaveLoc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fcSaveLoc.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());

        northGridPanel.add(tfName);
        northGridPanel.add(tfPassword);
        northGridPanel.add(tfCaptcha);
        northGridPanel.add(btLogin);

        northGridPanel.add(cbSFW);
        northGridPanel.add(cbNSFW);
        northGridPanel.add(cbNSFL);

        northPanel.add(northGridPanel, BorderLayout.NORTH);
        northPanel.add(captchaImage, BorderLayout.CENTER);

        southGridPanel.add(btDirChoose);
        southGridPanel.add(tfLocation);
        southGridPanel.add(btDownload);
        southGridPanel.add(btMetaData);

        southBorderGridLayout.add(btOwnFav);
        southBorderGridLayout.add(btOtherFav);
        southBorderGridLayout.add(btOwnPost);
        southBorderGridLayout.add(btOtherPost);

        southBorderLayout.add(laStatus, BorderLayout.NORTH);
        southBorderLayout.add(southBorderGridLayout, BorderLayout.SOUTH);
        southBorderLayout.add(tfOtherName, BorderLayout.CENTER);

        southPanel.add(southGridPanel, BorderLayout.SOUTH);
        //southPanel.add(btDownload, BorderLayout.CENTER);
        southPanel.add(southBorderLayout, BorderLayout.NORTH);

        mainPanel.add(northPanel, BorderLayout.NORTH);
        //mainPanel.add(spResponse, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        api = new ProApi(laStatus);

        getCaptcha();

        this.setPreferredSize(new Dimension(650, 400));
        this.add(mainPanel);

        this.pack();
        this.setVisible(true);

        btLogin.addActionListener(this);
        btDownload.addActionListener(this);
        btDirChoose.addActionListener(this);
        btMetaData.addActionListener(this);
        btOwnFav.addActionListener(this);
        btOtherFav.addActionListener(this);
        cbSFW.addActionListener(this);
        cbNSFL.addActionListener(this);
        cbNSFW.addActionListener(this);
        btOwnPost.addActionListener(this);
        btOtherPost.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btLogin){
            //taResponse.append(api.getItemInfo(3198555));
            String pass = new String(tfPassword.getPassword());
            ProApi.LoginResult result = api.login(tfName.getText(), pass, tfCaptcha.getText(), captchaToken);
            if(result.isSuccess()){
                loggedIn = true;

                tfName.setEnabled(false);
                tfPassword.setEnabled(false);
                tfCaptcha.setEnabled(false);
                btLogin.setEnabled(false);

                btOwnFav.setEnabled(true);
                btOtherFav.setEnabled(true);
                btOwnPost.setEnabled(true);
                btOtherPost.setEnabled(true);

                northPanel.remove(captchaImage);
                mainPanel.add(spResponse, BorderLayout.CENTER);
                this.pack();

                laStatus.setText("Hallo " + tfName.getText() +".");
                api.writeCookieFile();
            } else {
                if(result.getBan().equals("null")){
                    laStatus.setText("Es ist ein Fehler aufgetreten. Falsches Passwort?");
                } else {
                    laStatus.setText("Du bist gebannt. Grund: " + result.getBan());
                }
                getCaptcha();
            }
        }

        if(e.getSource() == btDirChoose){
            fcSaveLoc.showSaveDialog(this);
            if(fcSaveLoc.getSelectedFile() != null){
                tfLocation.setText(fcSaveLoc.getSelectedFile().getAbsolutePath());
            }

        }

        if(e.getSource() == btDownload){
            if(loggedIn) {
                startItemsDownload();
            }
        }

        if(e.getSource() == btOtherFav){
            if(loggedIn) {
                createFavList(true, tfOtherName.getText());
            }
        }

        if(e.getSource() == btOwnFav){
            if(loggedIn){
                createFavList(true, tfName.getText());
            }
        }

        if(e.getSource() == btOtherPost){
            if(loggedIn){
                createFavList(false, tfOtherName.getText());
            }
        }

        if(e.getSource() == btOwnPost){
            if(loggedIn){
                createFavList(false, tfName.getText());
            }
        }

        if(e.getSource() == btMetaData){
            try {
                File f = new File(tfLocation.getText() + "/favData.csv");

                CSVManager.readFile(f);
                int newLines = CSVManager.writeCSV(f, items);
                laStatus.setText("Es wurden " + newLines + " neue Zeilen hinzugefügt.");

            } catch (Exception exception){
                exception.printStackTrace();
            }

        }
    }

    private void getCaptcha(){
        try{
            ProApi.Captcha captcha = api.getCaptcha();
            captchaToken = captcha.getToken();

            ImageIcon captchaIcon = new ImageIcon(new ImageIcon(captcha.getImage()).getImage().getScaledInstance(650, 190, Image.SCALE_SMOOTH));
            // ImageIcon captchaIcon = new ImageIcon(image)

            captchaImage.setIcon(captchaIcon);

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void startItemsDownload(){
        if(new File(tfLocation.getText()).isDirectory()){
            btDownload.setEnabled(false);
            tfLocation.setEnabled(false);

            DownloadThread downloadThread = new DownloadThread(api, items, tfLocation.getText(), laStatus, btDownload);
            downloadThread.start();


        } else {
            laStatus.setText("Ordner existiert nicht.");
        }
    }

    private void createFavList(boolean favList, String name){
        int flag = 0;
        if(!name.equals("")){
            taResponse.setText("");
            flag += cbSFW.isSelected() ? ProApi.SFW : 0;
            flag += cbNSFW.isSelected() ? ProApi.NSFW : 0;
            flag += cbNSFL.isSelected() ? ProApi.NSFL : 0;

            items = api.getItemList(favList, name, flag);
            for(ProApi.ProItem item : items){
                taResponse.append("" + item.getUrlToFile() + "\n");
            }

            if(items.size() > 0){
                btDownload.setEnabled(true);
                btMetaData.setEnabled(true);
            }

            if(items.size() == 0){
                laStatus.setText(name +" hat keine Hochlads/Favoriten oder sie nicht öffentlich.");
            } else {
                laStatus.setText(name +" hat " + items.size() + " Hochlads/Favoriten in dieser Kategorie.");
            }
        } else {
            laStatus.setText("Es wurde kein Name eingegeben.");
        }

    }
}

