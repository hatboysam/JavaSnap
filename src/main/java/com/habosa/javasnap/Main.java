package com.habosa.javasnap;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

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
        if (loginObj != null) {
            System.out.println("Logged in.");
        } else {
            System.out.println("Failed to log in.");
            return;
        }

        // Get the auth_token
        String token = loginObj.getString(Snapchat.AUTH_TOKEN_KEY);

        // Ask the user what they want to do
        System.out.println();
        System.out.println("Choose an option:");
        System.out.println("\t1) Download un-viewed snaps");
        System.out.println("\t2) Send a snap");
        System.out.println("\t3) Set a Story");
        System.out.println("\t4) Download Stories");
        System.out.println();

        int option = scanner.nextInt();
        scanner.nextLine();
        switch (option) {
            case 1:
                fetchSnaps(loginObj, username, token);
            break;
            case 2:
                System.out.println("Enter path to image file:");
                String snapFileName = scanner.nextLine();
                System.out.println("Enter recipient Snapchat username:");
                String recipient = scanner.nextLine();
                sendSnap(username, recipient, snapFileName, token);
            break;
            case 3:
                System.out.println("Enter path to image file:");
                String storyFileName = scanner.nextLine();
                setStory(username, storyFileName, token);
            break;
            case 4:
                Story[] storyObjs = Snapchat.getStories(username, token);
                Story[] downloadable = Story.filterDownloadable(storyObjs);
                for (Story s : downloadable) {
                  String extension = ".jpg";
                  if(!s.isImage()){
                    extension = ".mp4";
                  }
                  System.out.println("Downloading story from " + s.getSender());
                  byte[] storyBytes = Snapchat.getStory(s, username, token);
                  File storyFile = new File(s.getSender() + "-" + s.getId() + extension);
                  FileOutputStream storyOs = new FileOutputStream(storyFile);
                  storyOs.write(storyBytes);
                  storyOs.close();
                }
                System.out.println("Done.");
            break;
            default:
                System.out.println("Invalid option.");
            break;
        }

    }

    public static void fetchSnaps(JSONObject loginObj, String username, String token) throws IOException {
        // Try fetching all snaps
        System.out.println("Fetching snaps...");
        Snap[] snapObjs = Snapchat.getSnaps(loginObj);
        Snap[] downloadable = Snap.filterDownloadable(snapObjs);
        for (Snap s : downloadable) {
            // TODO(samstern): Support video
            if (s.isImage()) {
                System.out.println("Downloading snap from " + s.getSender());
                byte[] snapBytes = Snapchat.getSnap(s, username, token);
                File snapFile = new File(s.getSender() + "-" + s.getId() + ".jpg");
                FileOutputStream snapOs = new FileOutputStream(snapFile);
                snapOs.write(snapBytes);
                snapOs.close();
            }
        }
        System.out.println("Done.");
    }

    public static void sendSnap(String username, String recipient, String filename, String token)
            throws FileNotFoundException {

        // Try uploading a file
        File file = new File(filename);
        String medId = Snapchat.upload(file, username, token, false);

        // Try sending it
        List<String> recipients = new ArrayList<String>();
        recipients.add(recipient);

        // Send and print
        System.out.println("Sending...");
        boolean postStory = false; //set as true to make this your story as well...

        // TODO(samstern): User-specified time, not automatically 10 seconds
        boolean result = Snapchat.send(medId, recipients, postStory, 10, username, token);
        if (result) {
            System.out.println("Sent.");
        } else {
            System.out.println("Could not send.");
        }
    }
    
    public static void setStory(String username, String filename, String token)
            throws FileNotFoundException {

        boolean video = false; //TODO(liamcottle) upload video snaps from command line.
        // Try uploading a file
        File file = new File(filename);
        String medId = Snapchat.upload(file, username, token, video);

        // Send and print
        System.out.println("Setting...");
        boolean postStory = false; //set as true to make this your story as well...

        // TODO(samstern): User-specified time, not automatically 10 seconds
        boolean result = Snapchat.sendStory(medId, 10, video, "My Story", username, token);
        if (result) {
            System.out.println("Set.");
        } else {
            System.out.println("Could not set.");
        }
    }
}
