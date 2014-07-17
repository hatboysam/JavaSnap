package com.habosa.javasnap;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.body.MultipartBody;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

/**
 * Author: samstern
 * Date: 12/29/13
 */
public class Snapchat {

    /**
     * Last response received.  Used for error reporting.
     */
    public static String lastRequestPath;
    public static HttpResponse lastResponse;
    public static Class lastResponseBodyClass;

    /**
     * POST parameter keys for sending requests to Snapchat.
     */
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String REQ_TOKEN_KEY = "req_token";
    private static final String AUTH_TOKEN_KEY = "auth_token";
    private static final String ID_KEY = "id";
    private static final String SNAP_KEY = "snap";
    private static final String CHAT_MESSAGE_KEY = "chat_message";
    private static final String MESSAGES_KEY = "messages";
    private static final String FRIEND_STORIES_KEY = "friend_stories";
    private static final String STORIES_KEY = "stories";
    private static final String FRIENDS_KEY = "friends";
    private static final String MEDIA_ID_KEY = "media_id";
    private static final String CLIENT_ID_KEY = "client_id";
    private static final String CAPTION_TEXT_DISPLAY_KEY = "caption_text_display";
    private static final String TYPE_KEY = "type";
    private static final String DATA_KEY = "data";
    private static final String ZIPPED_KEY = "zipped";
    private static final String TIME_KEY = "time";
    private static final String RECIPIENT_KEY = "recipient";
    private static final String ADDED_FRIENDS_TIMESTAMP_KEY = "added_friends_timestamp";
    private static final String JSON_KEY = "json";
    private static final String EVENTS_KEY = "events";
    private static final String LOGGED_KEY = "logged";
    private static final String CONVERSATION_MESSAGES_KEY = "conversation_messages";
    private static final String RECIPIENT_USERNAMES = "recipient_usernames";
    private static final String ACTION_KEY = "action";
    private static final String FRIEND_KEY = "friend";
    private static final String DISPLAY_KEY = "display";
    private static final String MY_STORIES_KEY = "my_stories";

    /**
     * Paths for various Snapchat groups in loginObj_full
     */
    private static final String UPDATES_RESPONSE_KEY = "updates_response";
    private static final String MESSAGING_GATEWAY_INFO_RESPONSE_KEY = "messaging_gateway_info";
    private static final String STORIES_RESPONSE_KEY = "stories_response";
    private static final String CONVERSATIONS_RESPONSE_KEY = "conversations_response";

    /**
     * Paths for various Snapchat actions, relative to BASE_URL.
     */
    private static final String LOGIN_PATH = "loq/login";
    private static final String ALL_UPDATES_PATH = "/loq/all_updates";
    private static final String UPLOAD_PATH = "bq/upload";
    private static final String SEND_PATH = "ph/send"; //TODO : UPDATE PATH
    private static final String STORY_PATH = "bq/post_story";
    private static final String DOUBLE_PATH = "bq/double_post"; //TODO : UPDATE PATH
    private static final String BLOB_PATH = "ph/blob";
    private static final String FRIEND_STORIES_PATH = "bq/stories";
    private static final String STORY_BLOB_PATH = "bq/story_blob";
    private static final String UPDATE_SNAPS_PATH = "bq/update_snaps";
    private static final String CHAT_TYPING_PATH = "bq/chat_typing";
    private static final String FRIEND_PATH = "bq/friend";
    
    /**
     * Static members for forming HTTP requests.
     */
    private static final String BASE_URL = "https://feelinsonice-hrd.appspot.com/";
    private static final String JSON_TYPE_KEY = "accept";
    private static final String JSON_TYPE = "application/json";
    private static final String USER_AGENT_KEY = "User-Agent";
    private static final String USER_AGENT = "Snapchat/3.0.2 (Nexus 4; Android 18; gzip)";
    
    /**
     * Local variables
     */
    private JSONObject loginObj_full;
    private JSONObject loginObj_updates;
    private JSONObject loginObj_messaging_gateway_info;
    private JSONObject loginObj_stories;
    private JSONArray loginObj_conversations;

    private String username;
    private String authToken;
    private long friendsTimestamp;
    private Friend[] friends;
    private Story[] stories;
    private Story[] mystories;
    private Snap[] snaps;
    private Message[] messages;
    private long lastRefreshed;
    
    /**
     * Build the Snapchat object
     * @see Snapchat#login(String, String)
     */
    private Snapchat(JSONObject loginObj_full){
        try {
            //Setup all inner json objects
            setupLoginJSONObjects(loginObj_full);

            //Setup all local variables
            this.username = this.loginObj_updates.getString(USERNAME_KEY);
            this.authToken = this.loginObj_updates.getString(AUTH_TOKEN_KEY);
            this.friendsTimestamp = this.loginObj_updates.getLong(Snapchat.ADDED_FRIENDS_TIMESTAMP_KEY);
        } catch (JSONException e) {
            //TODO Something is wrong with the loginObj_full
            e.printStackTrace();
        }
    }

    /**
     * Log in to Snapchat.
     *
     * @param username the Snapchat username.
     * @param password the Snapchat password.
     * @return the entire JSON login response.
     */
    public static Snapchat login(String username, String password) {
        Map<String, Object> params = new HashMap<String, Object>();

        // Add username and password
        params.put(USERNAME_KEY, username);
        params.put(PASSWORD_KEY, password);

        // Add timestamp and requestJson token made using static auth token
        Long timestamp = getTimestamp();
        String reqToken = TokenLib.staticRequestToken(timestamp);

        params.put(TIMESTAMP_KEY, timestamp.toString());
        params.put(REQ_TOKEN_KEY, reqToken);

        try {
            HttpResponse<JsonNode> resp = requestJson(LOGIN_PATH, params, null);
            JSONObject obj = resp.getBody().getObject();
            if(obj.has(UPDATES_RESPONSE_KEY) && obj.getJSONObject(UPDATES_RESPONSE_KEY).getBoolean(LOGGED_KEY)){
                return new Snapchat(obj);
            }
            return null;
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Refresh your snaps, friends, stories.
     *
     * @return true if successful, otherwise false.
     */
    public boolean refresh() {
        if(updateLoginObj()){
            this.snaps = null;
            this.messages = null;
            this.stories = null;
            this.mystories = null;
            this.friends = null;

            getSnaps();
            getMessages();
            getStories();
            getMyStories();
            getFriends();

            lastRefreshed = new Date().getTime();
            return true;
        }
        return false;
    }

    /**
     * Get your friends
     * @return a Friend[]
     */
    public Friend[] getFriends() {
        if(this.friends != null){
            return this.friends;
        }else{
            try {
                JSONArray friendsArr = this.loginObj_updates.getJSONArray(FRIENDS_KEY);
                List<Friend> resultList = bindArray(friendsArr, Friend.class);
                this.friends = resultList.toArray(new Friend[resultList.size()]);
                return this.friends;
            } catch (JSONException e) {
                return new Friend[0];
            }
        }
    }

    /**
     * Get received and sent snaps.
     * @return a Snap[]
     */
    public Snap[] getSnaps() {
        if(this.snaps != null){
            return this.snaps;
        }else{
            parseSnapsAndMessages();
            return this.snaps;
        }
    }

    /**
     * Get all received messages.
     * @return an array of Message.
     */
    public Message[] getMessages(){
        if(this.messages != null){
            return this.messages;
        }else{
            parseSnapsAndMessages();
            return this.messages;
        }
    }

    /**
     * Get Friends Stories from Snapchat.
     * @return a Story[]
     */
    public Story[] getStories() {
        if(this.stories != null){
            return this.stories;
        }else{
            try {
                JSONArray stories_list = new JSONArray();
                JSONArray friend_stories = this.loginObj_stories.getJSONArray(FRIEND_STORIES_KEY);
                //For each friend having posted a story
                for(int i = 0; i < friend_stories.length(); i++){
                    //Get friend story/stories
                    JSONArray stories = friend_stories.getJSONObject(i).getJSONArray(STORIES_KEY);
                    //For each story this friend has posted
                    for(int s = 0; s < stories.length(); s++){
                        stories_list.put(stories.get(s));
                    }
                }
                List<Story> stories = bindArray(stories_list, Story.class);
                this.stories = stories.toArray(new Story[stories.size()]);
                return this.stories;
            } catch (JSONException ex) {
                ex.printStackTrace();
                return new Story[0];
            }
        }
    }
    
    /**
     * Get My Stories from Snapchat.
     * @return a Story[]
     */
    public Story[] getMyStories() {
        if(this.mystories != null){
            return this.mystories;
        }else{
            try {
                JSONArray mystories_list = new JSONArray();
                JSONArray my_stories = this.loginObj_stories.getJSONArray(MY_STORIES_KEY);
                List<Story> mystories = bindArray(my_stories, Story.class);
                this.mystories = mystories.toArray(new Story[mystories.size()]);
                return this.mystories;
            } catch (JSONException ex) {
                ex.printStackTrace();
                return new Story[0];
            }
        }
    }

    /**
     * Download and un-encrypt a Snap from the server.
     *
     * @param snap the Snap to download.
     * @return a byte[] containing decrypted image or video data.
     */
    public byte[] getSnap(Snap snap) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(USERNAME_KEY, username);
            Long timestamp = getTimestamp();
            params.put(TIMESTAMP_KEY, timestamp);
            params.put(REQ_TOKEN_KEY, TokenLib.requestToken(authToken, timestamp));
            params.put(ID_KEY, snap.getId());
            
            HttpResponse<InputStream> resp = requestBinary(BLOB_PATH, params, null);
            InputStream is = resp.getBody();
            byte[] encryptedBytes = IOUtils.toByteArray(is);
            byte[] decryptedBytes = Encryption.decrypt(encryptedBytes);
            return decryptedBytes;
        } catch (UnirestException e) {
            return new byte[0];
        } catch (IOException e) {
            return new byte[0];
        } catch (Encryption.EncryptionException e) {
            return new byte[0];
        }
    }

    /**
     * Download and un-encrypt a Story from the server. Added by Liam Cottle
     *
     * @param story the Story to download.
     * @return a byte[] containing decrypted image or video data.
     */
    public static byte[] getStory(Story story) {
        try {
            HttpResponse<InputStream> resp = requestStoryBinary(STORY_BLOB_PATH + "?story_id=" + story.getId());
            InputStream is = resp.getBody();
            byte[] encryptedBytes = IOUtils.toByteArray(is);
            byte[] decryptedBytes = StoryEncryption.decrypt(encryptedBytes,story.getMediaKey(),story.getMediaIV());
            return decryptedBytes;
        } catch (UnirestException e) {
            return new byte[0];
        } catch (IOException e) {
            return new byte[0];
        }
    }

    /**
     * Send a snap image or video
     *
     * @param image the image/video file to upload.
     * @param recipients a list of Snapchat usernames to send to.
     * @param story true if this should be uploaded to the sender's story as well.
     * @param video true if video, otherwise false.
     * @param time the time (max 10) for which this snap should be visible.
     * @return true if success, otherwise false.
     */
    public boolean sendSnap(File image, List<String> recipients, boolean video, boolean story, int time){
        String upload_media_id = upload(image, video);
        if(upload_media_id != null){
            return send(upload_media_id, recipients, story, time);
        }
        return false;
    }

    /**
     * Add a snap to your story
     *
     * @param image the image/video file to upload.
     * @param video true if video, otherwise false.
     * @param time the time (max 10) for which this story should be visible.
     * @param caption a caption. Nobody knows what it is used for. eg. "My Story"
     * @return true if success, otherwise false.
     */
    public boolean sendStory(File image, boolean video, int time, String caption){
        String upload_media_id = upload(image, video);
        if(upload_media_id != null){
            return sendStory(upload_media_id, time, video, caption);
        }
        return false;
    }

    /**
     * Make a change to a snap/story, eg mark it as viewed or screenshot or seen.
     *
     * @param snap the snap object we are interacting with
     * @param seen boolean stating if we have seen this snap or not.
     * @param screenshot boolean stating if we have screenshot this snap or not.
     * @param replayed integer stating how many times we have replayed this snap.
     * @return true if successful, false otherwise.
     */
    public boolean setSnapFlags(Snap snap, boolean seen, boolean screenshot, boolean replayed){
        return updateSnap(snap, seen, screenshot, replayed);
    }

    /**
     * Tell your recipient that you are typing a chat message.
     *
     * @param recipient username to tell.
     * @return true if successful, otherwise false.
     */
    public boolean tellIAmTyping(String recipient){
        try {
            Map<String, Object> params = new HashMap<String, Object>();

            // Add timestamp and requestJson token made using auth token
            Long timestamp = getTimestamp();
            String reqToken = TokenLib.requestToken(this.authToken, timestamp);

            //Add params
            params.put(USERNAME_KEY, this.username);
            params.put(TIMESTAMP_KEY, timestamp.toString());
            params.put(REQ_TOKEN_KEY, reqToken);
            //Odd : Requires a JSONArray but only works with 1 username.
            params.put(RECIPIENT_USERNAMES, new JSONArray(new String[]{recipient}).toString());

            //Make the request
            HttpResponse<String> resp = requestString(CHAT_TYPING_PATH, params, null);
            System.out.println(resp.getBody().toString());
            if (resp.getCode() == 200 || resp.getCode() == 201) {
                return true;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete a friend
     *
     * @param friend username to add.
     * @return true if successful, otherwise false.
     */
    public boolean deleteFriend(String friend){
        try {
            Map<String, Object> params = new HashMap<String, Object>();

            // Add timestamp and requestJson token made using auth token
            Long timestamp = getTimestamp();
            String reqToken = TokenLib.requestToken(this.authToken, timestamp);

            //Add params
            params.put(USERNAME_KEY, this.username);
            params.put(TIMESTAMP_KEY, timestamp.toString());
            params.put(REQ_TOKEN_KEY, reqToken);
            params.put(ACTION_KEY, "delete");
            params.put(FRIEND_KEY, friend);

            //Make the request
            HttpResponse<String> resp = requestString(FRIEND_PATH, params, null);
            //The request seems to be a success even if you weren't already friends...
            if (resp.getCode() == 200 || resp.getCode() == 201) {
                return true;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Parse a JSONArray into a list of type
     *
     * @param arr the JSON array
     * @param clazz the class of type
     * @return a list of type
     */
    public static <T> List<T> bindArray(JSONArray arr, Class<? extends JSONBinder<T>> clazz) {
        try {
            int length = arr.length();
            List<T> result = new ArrayList<T>();
            for (int i = 0; i < length; i++) {
                JSONObject obj = arr.getJSONObject(i);
                T bound = clazz.newInstance().bind(obj);
                result.add(bound);
            }
            return result;
        } catch (JSONException e) {
            return new ArrayList<T>();
        } catch (InstantiationException e) {
            return new ArrayList<T>();
        } catch (IllegalAccessException e) {
            return new ArrayList<T>();
        }
    }

    /**
     * ==================================================  PRIVATE NON-STATIC METHODS REGION ==================================================
     */

    /**
     * Parses Snaps and Messages from the loginObj_conversations.
     * Saves the result in variables.
     */
    private void parseSnapsAndMessages(){
        try {
            JSONArray snapsArray = new JSONArray();
            JSONArray messagesArray = new JSONArray();
            //For each inner JSONObject  containing conversations per username
            for(int i = 0; i < this.loginObj_conversations.length(); i++){
                //Inner JSONObject containing conversations (snaps & chat messages)
                JSONObject conversation_messages = this.loginObj_conversations.getJSONObject(i).getJSONObject(CONVERSATION_MESSAGES_KEY);
                //Array of messages (snap or chat message)
                JSONArray messages = conversation_messages.getJSONArray(MESSAGES_KEY);
                for(int m = 0; m < messages.length(); m++){
                    //Get the JSONObject representing the message(snap or chat message)
                    JSONObject message = messages.getJSONObject(m);
                    //if it is a snap
                    if(message.has(SNAP_KEY)){
                        snapsArray.put(message.getJSONObject(SNAP_KEY));
                    }else if(message.has(CHAT_MESSAGE_KEY)){
                        messagesArray.put(message.getJSONObject(CHAT_MESSAGE_KEY));
                    }
                }
            }
            List<Snap> snapsList = bindArray(snapsArray, Snap.class);
            List<Message> messagesList = bindArray(messagesArray, Message.class);
            this.snaps = snapsList.toArray(new Snap[snapsList.size()]);
            this.messages = messagesList.toArray(new Message[messagesList.size()]);
        } catch (JSONException e) {
            this.snaps = new Snap[0];
            this.messages = new Message[0];
        }
    }

     /**
     * Send a snap that has already been uploaded.
     *
     * @param mediaId the media_id of the uploaded snap.
     * @param recipients a list of Snapchat usernames to send to.
     * @param story true if this should be uploaded to the sender's story as well.
     * @param time the time (max 10) for which this snap should be visible.
     * @return true if successful, false otherwise.
     */
    private boolean send(String mediaId, List<String> recipients, boolean story, int time) {
        try {
            // Prepare parameters
            Long timestamp = getTimestamp();
            String requestToken = TokenLib.requestToken(authToken, timestamp);
            int snapTime = Math.min(10, time);

            // Create comma-separated recipient string
            StringBuilder sb = new StringBuilder();
            if (recipients.size() == 0 && !story) {
                // Can't send to nobody
                return false;
            }else if(recipients.size() == 0 && story){
                // Send to story only
                //TODO : Send to story only
                return false;
            }
            sb.append(recipients.get(0));
            for (int i = 1; i < recipients.size(); i++) {
                String recip = recipients.get(i);
                if (recip != null) {
                    sb.append(",");
                    sb.append(recip);
                }
            }
            String recipString = sb.toString();

            // Make parameter map
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(USERNAME_KEY, username);
            params.put(TIMESTAMP_KEY, timestamp.toString());
            params.put(REQ_TOKEN_KEY, requestToken);
            params.put(MEDIA_ID_KEY, mediaId);
            params.put(TIME_KEY, Integer.toString(snapTime));
            params.put(RECIPIENT_KEY, recipString);
            params.put(ZIPPED_KEY, "0");

            // Sending path
            String path = SEND_PATH;

            // Add to story, maybe
            if (story) {
                path = DOUBLE_PATH;
                params.put(CAPTION_TEXT_DISPLAY_KEY, "My Story");
                params.put(CLIENT_ID_KEY, mediaId);
                params.put(TYPE_KEY, "0");
            }

            // Execute request
            HttpResponse<String> resp = requestString(path, params, null);
            if (resp.getCode() == 200 || resp.getCode() == 202) {
                return true;
            } else {
                return false;
            }
        } catch (UnirestException e) {
            return false;
        }
    }

    /**
     * Set a story from media already uploaded.
     *
     * @param mediaId the media_id of the uploaded snap.
     * @param time the time (max 10) for which this story should be visible.
     * @param video is video
     * @param caption the caption
     * @return true if successful, false otherwise.
     */
    private boolean sendStory(String mediaId, int time, boolean video, String caption) {
        try {
            // Prepare parameters
            Long timestamp = getTimestamp();
            String requestToken = TokenLib.requestToken(authToken, timestamp);
            int snapTime = Math.min(10, time);

            // Make parameter map
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(USERNAME_KEY, username);
            params.put(TIMESTAMP_KEY, timestamp.toString());
            params.put(REQ_TOKEN_KEY, requestToken);
            params.put(MEDIA_ID_KEY, mediaId);
            params.put(CLIENT_ID_KEY, mediaId);
            params.put(TIME_KEY, Integer.toString(snapTime));
            params.put(CAPTION_TEXT_DISPLAY_KEY, caption);
            params.put(ZIPPED_KEY, "0");
            if(video){
                params.put(TYPE_KEY, "1");
            }
            else{
                params.put(TYPE_KEY, "0");
            }

            // Execute request
            HttpResponse<String> resp = requestString(STORY_PATH, params, null);
            if (resp.getCode() == 200 || resp.getCode() == 202) {
                return true;
            } else {
                return false;
            }
        } catch (UnirestException e) {
            return false;
        }
    }

    /**
     * Setup all loginObj variables from the full loginObj
     *
     * @param newLoginObj_full full loginObj received from Snapchat Server.
     * @return true if successful, otherwise false.
     */
    private boolean setupLoginJSONObjects(JSONObject newLoginObj_full){
        try {
            this.loginObj_full = newLoginObj_full;
            this.loginObj_updates = loginObj_full.getJSONObject(UPDATES_RESPONSE_KEY);
            this.loginObj_messaging_gateway_info = loginObj_full.getJSONObject(MESSAGING_GATEWAY_INFO_RESPONSE_KEY);
            this.loginObj_stories = loginObj_full.getJSONObject(STORIES_RESPONSE_KEY);
            this.loginObj_conversations = loginObj_full.getJSONArray(CONVERSATIONS_RESPONSE_KEY);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetch latest version of full loginObj from Snapchat Server.
     * @return true if the update is successful, otherwise false.
     */
    private boolean updateLoginObj(){
        Map<String, Object> params = new HashMap<String, Object>();

        // Add username and password
        params.put(USERNAME_KEY, username);

        // Add timestamp and requestJson token made using auth token
        Long timestamp = getTimestamp();
        String reqToken = TokenLib.requestToken(this.authToken, timestamp);

        params.put(TIMESTAMP_KEY, timestamp.toString());
        params.put(REQ_TOKEN_KEY, reqToken);

        try {
            HttpResponse<JsonNode> resp = requestJson(ALL_UPDATES_PATH, params, null);
            JSONObject obj = resp.getBody().getObject();
            if(obj.has(UPDATES_RESPONSE_KEY) && obj.getJSONObject(UPDATES_RESPONSE_KEY).getBoolean(LOGGED_KEY)){
                return setupLoginJSONObjects(obj);
            }
            return false;
        } catch (UnirestException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Make a change to a snap/story, eg mark it as viewed or screenshot or seen.
     *
     * @param snap the snap object we are interacting with
     * @param seen boolean stating if we have seen this snap or not.
     * @param screenshot boolean stating if we have screenshot this snap or not.
     * @param replayed integer stating how many times we have replayed this snap.
     * @return true if successful, false otherwise.
     */
    private boolean updateSnap(Snap snap, boolean seen, boolean screenshot, boolean replayed) {
        try {
            // Prepare parameters
            Long timestamp = getTimestamp();
            String requestToken = TokenLib.requestToken(authToken, timestamp);

            int statusInt = 0;
            int replayedInt = 0;

            if(seen){
                statusInt = 0;
            }
            else if(screenshot){
                statusInt = 1;
            }

            if(replayed){
                replayedInt = 1;
            }

            String jsonString = "{\"" + snap.getId() + "\":{\"c\":" + statusInt + ",\"t\":" + timestamp + ",\"replayed\":" + replayedInt + "}}";

            String eventsString = "[]";

            // Make parameter map
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(USERNAME_KEY, username);
            params.put(TIMESTAMP_KEY, timestamp.toString());
            params.put(REQ_TOKEN_KEY, requestToken);
            params.put(ADDED_FRIENDS_TIMESTAMP_KEY, friendsTimestamp);
            params.put(JSON_KEY, jsonString);
            params.put(EVENTS_KEY, eventsString);
            //params.put(TIME_KEY, Integer.toString(snapTime));

            // Sending path
            String path = UPDATE_SNAPS_PATH;

            // Execute request
            HttpResponse<String> resp = requestString(path, params, null);
            if (resp.getCode() == 200 || resp.getCode() == 202) {
                return true;
            } else {
                return false;
            }
        } catch (UnirestException e) {
            return false;
        }
    }

    /**
     * Upload a file and return the media_id for sending.
     *
     * @param image the image file to upload.
     * @param video is a video
     * @return the new upload's media_id.  Returns null if there is an error.
     */
    private String upload(File image, boolean video) {
        try {
            // Open file and ecnrypt it
            byte[] fileBytes = IOUtils.toByteArray(new FileInputStream(image));
            byte[] encryptedBytes = Encryption.encrypt(fileBytes);

            // Write to a temporary file
            File encryptedFile = File.createTempFile("encr", "snap");

            FileOutputStream fos = new FileOutputStream(encryptedFile);
            fos.write(encryptedBytes);
            fos.close();

            // Create other params
            Long timestamp = getTimestamp();
            String requestToken = TokenLib.requestToken(authToken, timestamp);
            String mediaId = Snapchat.getNewMediaId(username);

            // Make parameter map
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(USERNAME_KEY, username);
            params.put(TIMESTAMP_KEY, timestamp);
            params.put(REQ_TOKEN_KEY, requestToken);
            params.put(MEDIA_ID_KEY, mediaId);
            if(video){
                params.put(TYPE_KEY, 1);
            }
            else{
                params.put(TYPE_KEY, 0);
            }

            // Perform request and check for 200
            HttpResponse<String> resp = requestString(UPLOAD_PATH, params, encryptedFile);
            if (resp.getCode() == 200) {
                return mediaId;
            } else {
                System.out.println("Upload failed, Response Code: " + resp.getCode());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Encryption.EncryptionException e) {
            e.printStackTrace();
            return null;
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch(OutOfMemoryError e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Add a friend
     *
     * @param friend username to add.
     * @return true if successful, otherwise false.
     */
    public boolean addFriend(String friend) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();

            // Add timestamp and requestJson token made using auth token
            Long timestamp = getTimestamp();
            String reqToken = TokenLib.requestToken(this.authToken, timestamp);

            //Add params
            params.put(USERNAME_KEY, this.username);
            params.put(TIMESTAMP_KEY, timestamp.toString());
            params.put(REQ_TOKEN_KEY, reqToken);
            params.put(ACTION_KEY, "add");
            params.put(FRIEND_KEY, friend);

            //Make the request
            HttpResponse<String> resp = requestString(FRIEND_PATH, params, null);
            if (resp.getCode() == 200 || resp.getCode() == 201) {
                if (resp.getBody().toString().toLowerCase().contains("Sorry!".toLowerCase())) {
                    return false;
                }
                return true;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Set Display for a Friend
     *
     * @param friend username to edit.
     * @param display display name to set.
     * @return true if successful, otherwise false.
     */
    public boolean setFriendDisplay(String friend, String display){
        try {
            Map<String, Object> params = new HashMap<String, Object>();

            Long timestamp = getTimestamp();
            String reqToken = TokenLib.requestToken(this.authToken, timestamp);

            //Add params
            params.put(USERNAME_KEY, this.username);
            params.put(TIMESTAMP_KEY, timestamp.toString());
            params.put(REQ_TOKEN_KEY, reqToken);
            params.put(ACTION_KEY, "display");
            params.put(FRIEND_KEY, friend);
            params.put(DISPLAY_KEY, display);

            //Make the request
            HttpResponse<String> resp = requestString(FRIEND_PATH, params, null);
            System.out.println(resp.getBody().toString());
            if (resp.getCode() == 200 || resp.getCode() == 201) {
                return true;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ====================================================  PRIVATE STATIC METHODS REGION ====================================================
     */

    /**
     * Get a new, random media_id for uploading media to Snapchat.
     *
     * @param username your Snapchat username.
     * @return a media_id as a String.
     */
    private static String getNewMediaId(String username) {
        String uuid = UUID.randomUUID().toString();
        return username.toUpperCase() + "~" + uuid;
    }
    
    /**
     * Get a new timestamp to use in a request.
     *
     * @return a timestamp.
     */
    private static Long getTimestamp() {
        Long timestamp = (new Date()).getTime() / 1000L;
        return timestamp;
    }

    private static MultipartBody prepareRequest(String path, Map<String, Object> params, File file) {
        // Set up a JSON request
        MultipartBody req = Unirest.post(BASE_URL + path)
                .header(JSON_TYPE_KEY, JSON_TYPE)
                .header(USER_AGENT_KEY, USER_AGENT)
                .fields(params);

        // Add file if there is one
        if (file != null) {
            return req.field(DATA_KEY, file);
        }

        return req;
    }

    private static HttpResponse<InputStream> requestBinary(String path, Map<String, Object> params, File file) throws UnirestException {
        MultipartBody req = prepareRequest(path, params, file);

        // Execute and return as bytes
        HttpResponse<InputStream> resp = req.asBinary();

        // Record
        lastRequestPath = path;
        lastResponse = resp;
        lastResponseBodyClass = InputStream.class;

        return resp;
    }

    private static HttpResponse<JsonNode> requestJson(String path, Map<String, Object> params, File file) throws UnirestException {
        MultipartBody req = prepareRequest(path, params, file);

        // Execute and return response as JSON
        HttpResponse<JsonNode> resp = req.asJson();

        // Record
        lastRequestPath = path;
        lastResponse = resp;
        lastResponseBodyClass = JsonNode.class;

        return resp;
    }

    private static HttpResponse<InputStream> requestStoryBinary(String path) throws UnirestException {
        HttpRequest req = Unirest.get(BASE_URL + path)
                .header(JSON_TYPE_KEY, JSON_TYPE)
                .header(USER_AGENT_KEY, USER_AGENT);

        // Execute and return as bytes
        HttpResponse<InputStream> resp = req.asBinary();

        // Record
        lastRequestPath = path;
        lastResponse = resp;
        lastResponseBodyClass = InputStream.class;

        return resp;
    }

    private static HttpResponse<String> requestString(String path, Map<String, Object> params, File file) throws UnirestException {
        MultipartBody req = prepareRequest(path, params, file);

        // Execute and return response as String
        HttpResponse<String> resp = req.asString();

        // Record
        lastRequestPath = path;
        lastResponse = resp;
        lastResponseBodyClass = String.class;

        return resp;
    }

}
