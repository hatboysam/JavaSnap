package com.habosa.javasnap;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: samstern
 * Date: 12/29/13
 */
public class Snapchat {

    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String TIMESTAMP_KEY = "timestamp";
    public static final String REQ_TOKEN_KEY = "req_token";
    public static final String AUTH_TOKEN_KEY = "auth_token";
    public static final String ID_KEY = "id";
    public static final String SNAPS_KEY = "snaps";

    public static final String LOGIN_PATH = "bq/login";
    public static final String UPLOAD_PATH = "ph/upload";
    public static final String BLOB_PATH = "ph/blob";

    private static final String BASE_URL = "https://feelinsonice-hrd.appspot.com/";
    private static final String TYPE_KEY = "accept";
    private static final String JSON_TYPE = "application/json";

    /**
     * Log in to Snapchat.
     *
     * @param username the Snapchat username.
     * @param password the Snapchat password.
     * @return the entire JSON login response.
     */
    public static JSONObject login(String username, String password) {
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
            return obj;
        } catch (UnirestException e) {
            return null;
        }
    }

    public static Snap[] getSnaps(JSONObject loginObject) {
        try {
            JSONArray snapArr = loginObject.getJSONArray(SNAPS_KEY);
            int length = snapArr.length();
            Snap[] result = new Snap[length];
            for (int i = 0; i < length; i++) {
                result[i] = new Snap(snapArr.getJSONObject(i));
            }
            return result;
        } catch (JSONException e) {
            return new Snap[0];
        }
    }

    public static byte[] getSnap(Snap snap, String username, String authToken) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(USERNAME_KEY, username);
        Long timestamp = getTimestamp();
        params.put(TIMESTAMP_KEY, timestamp);
        params.put(REQ_TOKEN_KEY, TokenLib.requestToken(authToken, timestamp));
        params.put(ID_KEY, snap.getId());

        try {
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

    public static Long getTimestamp() {
        Long timestamp = (new Date()).getTime() / 1000L;
        return timestamp;
    }

    public static HttpResponse<JsonNode> requestJson(String path, Map<String, Object> params, File file) throws UnirestException {
        HttpRequestWithBody req = prepareRequest(path, params, file);

        // Execute and return response as JSON
        HttpResponse<JsonNode> resp = req.asJson();
        return resp;
    }

    public static HttpResponse<String> requestString(String path, Map<String, Object> params, File file) throws UnirestException {
        HttpRequestWithBody req = prepareRequest(path, params, file);

        // Execute and return response as String
        HttpResponse<String> resp = req.asString();
        return resp;
    }

    public static HttpResponse<InputStream> requestBinary(String path, Map<String, Object> params, File file) throws UnirestException {
        HttpRequestWithBody req = prepareRequest(path, params, file);

        // Execute and return as bytes
        HttpResponse<InputStream> resp = req.asBinary();
        return resp;
    }


    private static HttpRequestWithBody prepareRequest(String path, Map<String, Object> params, File file) {
        // Set up a JSON requestJson
        HttpRequestWithBody req = Unirest.post(BASE_URL + path);
        req.header(TYPE_KEY, JSON_TYPE);

        // Add fields
        req.fields(params);

        // Add file
        if (file != null) {
            req.field("data", file);
        }

        return req;
    }

}
