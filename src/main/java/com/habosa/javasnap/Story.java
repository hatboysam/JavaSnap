package com.habosa.javasnap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Story implements JSONBinder<Story> {

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_VIDEO = 1;
    private static final int TYPE_VIDEO_NOAUDIO = 2;

    private static final String STORY_KEY = "story";
    private static final String STORY_NOTES_KEY = "story_notes";
    private static final String STORY_EXTRAS_KEY = "story_extras";
    private static final String TIMESTAMP_KEY = "timestamp";

    private static final String MEDIA_ID_KEY = "media_id";
    private static final String MEDIA_KEY_KEY = "media_key";
    private static final String MEDIA_IV_KEY = "media_iv";
    private static final String MEDIA_TYPE_KEY = "media_type";
    private static final String SENDER_KEY = "username";
    private static final String TIME_KEY = "time";
    private static final String TIME_LEFT_KEY = "time_left";
    private static final String CAPTION_KEY = "caption_text_display";
    private static final String VIEWED_KEY = "viewed";
    private static final String SCREENSHOT_COUNT_KEY = "screenshot_count";
    private static final String VIEW_COUNT_KEY = "view_count";

    private String id;
    private String media_key;
    private String media_iv;
    private String sender;
    private int type;
    private int time;
    private int time_left;
    private Long timestamp;
    private boolean viewed;
    private boolean isMine;
    private Viewer[] viewers;
    private int screenshot_count;
    private int views;

    private String caption;

    public Story() {
    }


    public Story bind(JSONObject obj) {
        try {
            JSONObject storyObj = obj.getJSONObject(STORY_KEY);
            try {
                this.viewed = obj.getBoolean(VIEWED_KEY); //For some reason, this is not in the inner story json obj.
                this.isMine = false; //Viewed key only exist for friend's stories.
            } catch (JSONException e) {
                this.isMine = true;
            }


            this.id = storyObj.getString(MEDIA_ID_KEY);
            this.media_key = storyObj.getString(MEDIA_KEY_KEY);
            this.media_iv = storyObj.getString(MEDIA_IV_KEY);
            this.type = storyObj.getInt(MEDIA_TYPE_KEY);
            this.sender = storyObj.getString(SENDER_KEY);
            this.timestamp = storyObj.getLong(TIMESTAMP_KEY);
            this.time_left = storyObj.getInt(TIME_LEFT_KEY);
            this.caption = storyObj.has(CAPTION_KEY) ? storyObj.getString(CAPTION_KEY) : null;
            this.time = storyObj.getInt(TIME_KEY);

            if (this.isMine) {
                //Who have seen my story
                JSONArray notesObj = obj.getJSONArray(STORY_NOTES_KEY);
                List<Viewer> viewers = Snapchat.bindArray(notesObj, Viewer.class);
                this.viewers = viewers.toArray(new Viewer[viewers.size()]);
                //Statistics of my story
                JSONObject extrasObj = obj.getJSONObject(STORY_EXTRAS_KEY);
                screenshot_count = extrasObj.getInt(SCREENSHOT_COUNT_KEY);
                views = extrasObj.getInt(VIEW_COUNT_KEY);
            }
        } catch (JSONException e) {
            System.out.println("Error parsing story : " + obj.toString());
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Take an array of Stories and returns what is downloadable.
     * USELESS: Stories are always downloadable.
     *
     * @param input the array of Snaps to filter.
     * @return the snaps that are downloadable.
     */
    public static Story[] filterDownloadable(Story[] input) {
        List<Story> downloadable = new ArrayList<Story>();
        for (Story s : input) {
            if (s.isDownloadable()) {
                downloadable.add(s);
            }
        }
        return downloadable.toArray(new Story[downloadable.size()]);
    }

    /**
     * Check if the story is downloadable.
     * USELESS: Stories are always downloadable.
     *
     * @return true if the story is downloadable.
     */
    public boolean isDownloadable() {
        return true;
    }

    /**
     * Determine if a Story is a still image.
     *
     * @return true if it is an image, false if it is a video or other.
     */
    public boolean isImage() {
        return (type == TYPE_IMAGE);
    }

    /**
     * Determine if a Story is a video.
     *
     * @return true if it is a video, false if it is an image or other.
     */
    public boolean isVideo() {
        return (type == TYPE_VIDEO || type == TYPE_VIDEO_NOAUDIO);
    }

    /**
     * Determine if a Story is a video or image.
     *
     * @return true if it is a video or an image, false if other.
     */
    public boolean isMedia() {
        return (type <= TYPE_VIDEO_NOAUDIO);
    }

    /**
     * Get this Story ID.
     *
     * @return the ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the media key of this story. Used for decryption.
     *
     * @return the media key.
     */
    public String getMediaKey() {
        return media_key;
    }

    /**
     * Get the media iv. Used for decryption.
     *
     * @return the media iv.
     */
    public String getMediaIV() {
        return media_iv;
    }

    /**
     * If you have seen this story.
     *
     * @return true if seen, otherwise false.
     */
    public boolean isViewed() {
        return this.viewed;
    }

    public String getSender() {
        return sender;
    }

    /**
     * Get the maximum allowed time to view this story.
     *
     * @return maximum time allowed.
     */
    public int getTime() {
        return time;
    }

    /**
     * Get the expiration date of this story.
     *
     * @return unix timespamp of the expiration date.
     */
    public int getTimeLeft() {
        return time_left;
    }

    /**
     * Get the timestamp of creation date for this story.
     *
     * @return unix timestamp of the creation date.
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the text of this story.
     *
     * @return the text.
     */
    public String getCaption() {
        return caption;
    }

    /**
     * If the story belongs to us, thus will have viewers...
     *
     * @return boolean
     */
    public boolean isMine() {
        return this.isMine;
    }

    /**
     * Get the viewers of this story.
     *
     * @return Viewer[] the viewers or null.
     */
    public Viewer[] getViewers() {
        return viewers;
    }

    /**
     * Get the views count of this story.
     *
     * @return int the viewer count or 0 if not my story.
     */
    public int getViewCount() {
        return views;
    }

    /**
     * Get the screenshots count of this story.
     *
     * @return int the screenshot count or 0 if not my story.
     */
    public int getScreenshotCount() {
        return screenshot_count;
    }

    @Override
    public String toString() {
        String[] attrs = new String[]{
                id,
                sender,
                Integer.toString(type),
                Integer.toString(time),
                Integer.toString(time_left),
                Integer.toString(views),
                Integer.toString(screenshot_count),
                caption
        };
        return Arrays.toString(attrs);
    }
}
