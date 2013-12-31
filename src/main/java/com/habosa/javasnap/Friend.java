package com.habosa.javasnap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author: samstern
 * Date: 12/31/13
 */
public class Friend implements JSONBinder<Friend> {

    private static final String USERNAME_KEY = "name";
    private static final String DISPLAY_NAME_KEY = "display";

    private String username;
    private String displayName;

    public Friend() { }

    public Friend bind(JSONObject obj) {
        try {
            this.username = obj.getString(USERNAME_KEY);
            this.displayName = obj.getString(DISPLAY_NAME_KEY);
        } catch (JSONException e) {
            return this;
        }

        return this;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return username + " ~>  " + displayName;
    }
}
