package com.habosa.javasnap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Viewer implements JSONBinder<Viewer> {

    private static final String VIEWER_KEY = "viewer"; //Always : Viewer Username.
    private static final String SCREENSHOTTED_KEY = "screenshotted"; //Always : If your story has been viewed or not.
    private static final String TIMESTAMP_KEY = "timestamp"; //Always : Timestamp of when the story was viewed by this viewer.

    private String viewer;
    private Boolean screenshotted;
    private Long timestamp;

    public Viewer() { }

    public Viewer bind(JSONObject obj) {
        // Check for fields that always exist
        try {
            this.viewer = obj.getString(VIEWER_KEY);
            this.screenshotted = obj.getBoolean(SCREENSHOTTED_KEY);
            this.timestamp = obj.getLong(TIMESTAMP_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Has user screenshoted this story.
     *
     * @return true if user has screenshotted.
     */
    public boolean isScreenshoted(){
        return screenshotted;
    }

    /**
     * Get this viewer username.
     *
     * @return the viewer username.
     */
    public String getViewer() {
        return viewer;
    }

    /**
     * Get when the user has seen this story.
     *
     * @return unix timestamp of when the user has seen this story.
     */
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        String[] attrs = new String[]{
                viewer,
                String.valueOf(screenshotted),
                String.valueOf(timestamp)
        };
        return Arrays.toString(attrs);
    }
}
