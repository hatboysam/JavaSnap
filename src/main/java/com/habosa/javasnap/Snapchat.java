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
    private static String lastRequestPath;
    private static HttpResponse lastResponse;
    private static Class lastResponseBodyClass;

    /**
     * POST parameter keys for sending requests to Snapchat.
     */
    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String REQ_TOKEN_KEY = "req_token";
    private static final String AUTH_TOKEN_KEY = "auth_token";
    private static final String ID_KEY = "id";
    private static final String SNAPS_KEY = "snaps";
    private static final String MY_STORIES_KEY = "my_stories";
    private static final String FRIENDS_STORIES_KEY = "friend_stories";
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

    /**
     * Paths for various Snapchat actions, relative to BASE_URL.
     */
    private static final String LOGIN_PATH = "bq/login";
    private static final String UPLOAD_PATH = "bq/upload";
    private static final String SEND_PATH = "ph/send";
    private static final String STORY_PATH = "bq/post_story";
    private static final String DOUBLE_PATH = "bq/double_post";
    private static final String BLOB_PATH = "ph/blob";

    private static final String FRIEND_STORIES_PATH = "bq/stories";
    private static final String STORY_BLOB_PATH = "bq/story_blob";
    private static final String UPDATE_SNAPS_PATH = "bq/update_snaps";
    
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
    private JSONObject loginObj;
    private String username;
    private String authToken;
    private String friendsTimestamp;
    private Friend[] friends;
    private Story[] stories;
    private Snap[] snaps;
    private long lastRefreshed;
    
    /**
     * Build the Snapchat object
     * @see Snapchat#login(String, String)
     */
    private Snapchat(JSONObject loginObj){
        this.loginObj = loginObj;
        try {
            this.username = loginObj.getString(USERNAME_KEY);
            this.authToken = loginObj.getString(AUTH_TOKEN_KEY);
            this.friendsTimestamp = loginObj.getString(Snapchat.ADDED_FRIENDS_TIMESTAMP_KEY);
            refresh();
        } catch (JSONException e) {
            //TODO Something is wrong with the loginObj
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
            if(obj.has(LOGGED_KEY) && obj.getBoolean(LOGGED_KEY)){
                return new Snapchat(obj);
            }else{
                return null;
            }
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
     */
    public void refresh() {
        //TODO Get update. There is a better way than refreshing loginObj by relogging in.
        //TODO : Nothing is refreshed here except stories.
        getStories();
        getSnaps();
        getFriends();
        lastRefreshed = new Date().getTime();
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
                JSONArray friendsArr = this.loginObj.getJSONArray(FRIENDS_KEY);
                List<Friend> resultList = bindArray(friendsArr, Friend.class);
                this.friends = resultList.toArray(new Friend[resultList.size()]);
                return this.friends;
            } catch (JSONException e) {
                return new Friend[0];
            }
        }
    }

    /**
     * Get an array of Snap objects.
     * @return a Snap[]
     */
    public Snap[] getSnaps() {
        if(this.snaps != null){
            return this.snaps;
        }else{
            try {
                JSONArray snapArr = this.loginObj.getJSONArray(SNAPS_KEY);
                List<Snap> resultList = bindArray(snapArr, Snap.class);
                this.snaps = resultList.toArray(new Snap[resultList.size()]);
                return this.snaps;
            } catch (JSONException e) {
                return new Snap[0];
            }
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
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(USERNAME_KEY, username);
                Long timestamp = getTimestamp();
                params.put(TIMESTAMP_KEY, timestamp);
                params.put(REQ_TOKEN_KEY, TokenLib.requestToken(authToken, timestamp));

                HttpResponse<JsonNode> resp = requestJson(FRIEND_STORIES_PATH, params, null);
                JSONObject obj = resp.getBody().getObject();
                JSONArray storyArr = obj.getJSONArray(FRIENDS_STORIES_KEY);

                JSONArray storiesArray = new JSONArray();

                for (int i=0; i<storyArr.length(); i++) {
                    JSONArray items = storyArr.getJSONObject(i).getJSONArray("stories");
                    for (int j=0; j<items.length(); j++) {
                        JSONObject _story = items.getJSONObject(j).getJSONObject("story");
                        storiesArray.put(_story);
                    }
                }

                List<Story> resultList = bindArray(storiesArray, Story.class);
                this.stories = resultList.toArray(new Story[resultList.size()]);
                return this.stories;
            } catch (UnirestException e) {
                return new Story[0];
            } catch (JSONException ex) {
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
     * ==================================================  PRIVATE NON-STATIC METHODS REGION ==================================================
     */

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
            System.out.println(e);
            return null;
        } catch (Encryption.EncryptionException e) {
            System.out.println(e);
            return null;
        } catch (UnirestException e) {
            System.out.println(e);
            return null;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        } catch(OutOfMemoryError e){
            System.out.println(e);
            return null;
        }
    }

    /**
     * ====================================================  PRIVATE STATIC METHODS REGION ====================================================
     */

    /**
     * Parse a JSONArray into a list of type
     *
     * @param arr the JSON array
     * @param clazz the class of type
     * @return a list of type
     */
    private static <T> List<T> bindArray(JSONArray arr, Class<? extends JSONBinder<T>> clazz) {
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
