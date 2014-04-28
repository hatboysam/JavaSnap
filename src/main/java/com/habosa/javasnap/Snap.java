package com.habosa.javasnap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Snap implements JSONBinder<Snap> {

    public static int TYPE_IMAGE = 0;
    public static int TYPE_VIDEO = 1;
    public static int TYPE_VIDEO_NOAUDIO = 2;

    public static int NONE = -1;
    public static int SENT = 0;
    public static int DELIVERED = 1;
    public static int VIEWED = 2;
    public static int SCREENSHOT = 3;

    private static final String ID_KEY = "id";
    private static final String SENDER_KEY = "sn";
    private static final String RECIPENT_KEY = "rp";
    private static final String TYPE_KEY = "m";
    private static final String STATE_KEY = "st";
    private static final String TIME_KEY = "t";
    private static final String SENTTIME_KEY = "sts";
    private static final String CAPTION_KEY = "caption_text_display";

    private String id;
    private String sender;
    private String recipient;
    private int type;
    private int state;
    private int time;
    private int senttime;
    
    private String caption;

    public Snap() { }

    public Snap bind(JSONObject obj) {
        // Check for fields that always exist
        try {
            this.id = obj.getString(ID_KEY);
            this.type = obj.getInt(TYPE_KEY);
            this.state = obj.getInt(STATE_KEY);
            this.senttime = obj.getInt(SENTTIME_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
            return this;
        }

        // Check sender or recipient separately
        try {
            this.sender = obj.getString(SENDER_KEY);
        } catch (JSONException e) {
            // Ignore
        }
        
        try {
            this.sender = obj.getString(SENDER_KEY);
        } catch (JSONException e) {
            // Ignore
        }
        
        try {
            this.caption = obj.getString(CAPTION_KEY);
        } catch (JSONException e) {
            this.caption = "";
        }

        // Check for time separately because it may not exist.
        try {
            this.time = obj.getInt(TIME_KEY);
        } catch (JSONException e) {
            return this;
        }

        return this;
    }

    /**
     * Take an array of Snaps and return only those that are downloadable.
     *
     * @param input the array of Snaps to filter.
     * @return the snaps that are downloadable (media and unviewed).
     */
    public static Snap[] filterDownloadable(Snap[] input) {
        ArrayList<Snap> result = new ArrayList<Snap>();
        for (Snap s : input) {
            if (s.isMedia() && s.isIncoming() && !s.isViewed()) {
                result.add(s);
            }
        }

        return result.toArray(new Snap[result.size()]);
    }

    /**
     * Determine if a Snap has already been viewed.  If not, it can be downloaded.
     *
     * @return true if it has been viewed, false otherwise.
     */
    public boolean isViewed() {
        return (state == VIEWED);
    }

    /**
     * Determine if a Snap is a still image.
     *
     * @return true if it is an image, false if it is a video or other.
     */
    public boolean isImage() {
        return (type == TYPE_IMAGE);
    }

    /**
     * Determine if a Snap is a video.
     *
     * @return true if it is a video, false if it is an image or other.
     */
    public boolean isVideo() {
        return (type == TYPE_VIDEO || type == TYPE_VIDEO_NOAUDIO);
    }

    /**
     * Determine if a Snap is a video or image.  If not, can't be downloaded and viewed.
     *
     * @return true if it is a video or an image, false if other.
     */
    public boolean isMedia() {
        return (type <= TYPE_VIDEO);
    }

    /**
     * Determine if a Snap is incoming or outgoing.
     *
     * @return true if a snap is incoming, false otherwise.
     */
    public boolean isIncoming() {
        return (id.endsWith("r"));
    }

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public int getType() {
        return type;
    }

    public int getState() {
        return state;
    }

    public int getTime() {
        return time;
    }
    
    public int getSentTime() {
        return senttime;
    }

    public String getCaption() {
        return caption;
    }

    @Override
    public String toString() {
        String[] attrs = new String[]{
                id,
                sender,
                recipient,
                Integer.toString(type),
                Integer.toString(state),
                Integer.toString(time),
                Integer.toString(senttime),
                caption
        };
        return Arrays.toString(attrs);
    }
}
