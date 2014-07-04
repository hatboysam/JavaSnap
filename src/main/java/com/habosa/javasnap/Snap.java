package com.habosa.javasnap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class Snap implements JSONBinder<Snap> {

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_VIDEO = 1;
    private static final int TYPE_VIDEO_NOAUDIO = 2;
    private static final int TYPE_FRIEND_REQUEST = 3;
    private static final int TYPE_FRIEND_REQUEST_IMAGE = 4;
    private static final int TYPE_FRIEND_REQUEST_VIDEO = 5;
    private static final int TYPE_FRIEND_REQUEST_VIDEO_NOAUDIO = 6;

    private static final int NONE = -1;
    private static final int SENT = 0;
    private static final int DELIVERED = 1;
    private static final int VIEWED = 2;
    private static final int SCREENSHOT = 3;

    private static final String ID_KEY = "id"; //Always : Snap ID.
    private static final String SENTTIME_KEY = "sts"; //Always : Snaps sent time.
    private static final String LAST_INTERACTION_TIME_KEY = "ts"; //Always : Recipient : ts == sts. Sender : Last interaction time.
    private static final String TYPE_KEY = "m"; //Always : Image or Video
    private static final String STATE_KEY = "st"; //Always : Sent, Delivered, Viewed, Screnshot.
    private static final String SENDER_KEY = "sn"; //Only there for recipient : Sender username.
    private static final String RECIPENT_KEY = "rp"; //Only there for sender : Recipient username.
    private static final String TIME_KEY = "t"; //Unseen snaps only : How long can it be viewed for.

    private String id;
    private String sender;
    private String recipient;
    private int type;
    private int state;
    private int time;
    private long senttime;
    private long lastInteractionTime;

    private String caption;

    public Snap() { }

    public Snap bind(JSONObject obj) {
        // Check for fields that always exist
        try {
            this.id = obj.getString(ID_KEY);
            this.type = obj.getInt(TYPE_KEY);
            this.state = obj.getInt(STATE_KEY);
            this.lastInteractionTime = obj.getLong(LAST_INTERACTION_TIME_KEY);
            this.senttime = obj.getLong(SENTTIME_KEY);

            if(obj.has(SENDER_KEY)){
                this.sender = obj.getString(SENDER_KEY);
            }

            if(obj.has(RECIPENT_KEY)){
                this.recipient = obj.getString(RECIPENT_KEY);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return this;
        }

        // Check for time separately because it may not exist.
        // Only exist when the snap hasn't been viewed.
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
            if(s.isDownloadable()){
                result.add(s);
            }
        }

        return result.toArray(new Snap[result.size()]);
    }

    /**
     * Check if this snap can be downloaded
     *
     * @return is downloadable
     */
    public boolean isDownloadable(){
        if (this.isMedia() && this.isIncoming() && !this.isViewed()) {
            return true;
        }
        return false;
    }

    /**
     * Determine if a Snap has already been viewed.  If not, it can be downloaded.
     *
     * @return true if it has been viewed, false otherwise.
     */
    public boolean isViewed() {
        return (state == VIEWED || state == SCREENSHOT);
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
        return (isImage() || isVideo());
    }

    /**
     * Determine if a snap has been screenshoted.
     *
     * @return true if it is screenshoted.
     */
    public boolean isScreenshoted(){
        return state == SCREENSHOT;
    }

    /**
     * Determine if a Snap is incoming or outgoing.
     *
     * @return true if a snap is incoming, false otherwise.
     */
    public boolean isIncoming() {
        return (id.endsWith("r"));
    }

    public boolean isFriendRequest(){
        return (type == TYPE_FRIEND_REQUEST || type == TYPE_FRIEND_REQUEST_IMAGE || type == TYPE_FRIEND_REQUEST_VIDEO || type == TYPE_FRIEND_REQUEST_VIDEO_NOAUDIO);
    }

    /**
     * Get this snap ID.
     *
     * @return the snap ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get this snap sender username.
     *
     * @return the sender username.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Get this snap recipient username.
     *
     * @return the recipient username.
     */
    public String getRecipient() {
        return recipient;
    }

    public int getTime() {
        return time;
    }

    /**
     * Last interaction time. For recipients, this is the same as sent time.
     *
     * @return last interaction time.
     */
    public long getLastInteractionTime(){
        return this.lastInteractionTime;
    }

    public long getSentTime() {
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
                Long.toString(senttime),
                caption
        };
        return Arrays.toString(attrs);
    }
}
