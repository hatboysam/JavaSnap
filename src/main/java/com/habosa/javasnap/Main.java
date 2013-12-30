package com.habosa.javasnap;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // Get username and password
        Scanner scanner = new Scanner(System.in);
        System.out.println("Snapchat username: ");
        String username = scanner.nextLine();
        System.out.println("Snapchat password: ");
        String password = scanner.nextLine();

        // Test logging in
        System.out.println("Logging in...");
        JSONObject loginObj = Snapchat.login(username, password);
        String token = loginObj.getString(Snapchat.AUTH_TOKEN_KEY);

        // Try fetching all snaps
        System.out.println("Fetching snaps...");
        Snap[] snapObjs = Snapchat.getSnaps(loginObj);
        Snap[] downloadable = Snap.filterDownloadable(snapObjs);
        for (Snap s : downloadable) {
            byte[] snapBytes = Snapchat.getSnap(s, username, token);
            // TODO(samstern): Support video
            if (s.isImage()) {
                System.out.println("Downloading snap from " + s.getSender());
                File snapFile = new File(s.getSender() + "-" + s.getId() + ".jpg");
                FileOutputStream snapOs = new FileOutputStream(snapFile);
                snapOs.write(snapBytes);
                snapOs.close();
            }
        }
    }
}
