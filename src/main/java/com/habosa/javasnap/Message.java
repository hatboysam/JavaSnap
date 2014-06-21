package com.habosa.javasnap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2014-06-17.
 */
public class Message implements JSONBinder<Message> {

    /**
     * Types of Message.
     */
    private final static String TYPE_MEDIA = "media";
    private final static String TYPE_TEXT = "text";

    /**
     * Paths for various inner json objects.
     */
    private final static String HEADER_KEY = "header";
    private final static String BODY_KEY = "body";
    private final static String MEDIA_KEY = "media"; //Only exist for media

    /**
     * Various key to get specific informations
     */
    private final static String CHAT_MESSAGE_ID = "chat_message_id"; //Same as media_id
    private final static String ID_KEY = "id";
    private final static String FROM_KEY = "from";
    private final static String TO_KEY = "to";
    private final static String WIDTH_KEY = "width"; //Only exist for media
    private final static String HEIGHT_KEY = "height"; //Only exist for media
    private final static String IV_KEY = "iv"; //Only exist for media
    private final static String KEY_KEY = "key"; //Only exist for media
    private final static String MEDIA_ID_KEY = "media_id"; //Only exist for media. Same as chat_message_id
    private final static String TEXT_KEY = "text";
    private final static String TYPE_KEY = "type";
    private final static String TIMESTAMP_KEY = "timestamp";

    /**
     * Local variables
     */
    private String sender;
    private List<String> recipients;
    private String chat_message_id;
    private String id;
    private String media_id;
    private int width;
    private int height;
    private String key;
    private String iv;
    private String type;
    private long sent_time;
    private String text;




    @Override
    public Message bind(JSONObject obj) {
        try{
            //Inner json objects
            JSONObject header = obj.getJSONObject(HEADER_KEY);
            JSONObject body = obj.getJSONObject(BODY_KEY);

            //Root
            this.chat_message_id = obj.getString(CHAT_MESSAGE_ID);
            this.id = obj.getString(ID_KEY);
            this.sent_time = obj.getLong(TIMESTAMP_KEY);

            //Header
            this.sender = header.getString(FROM_KEY);
            this.recipients = new ArrayList<String>();
            JSONArray jsonRecipients = header.getJSONArray(TO_KEY);
            for(int i = 0; i < jsonRecipients.length(); i++){
                this.recipients.add(jsonRecipients.getString(i));
            }

            //Body
            this.type = body.getString(TYPE_KEY);
            if(this.type.equalsIgnoreCase(TYPE_MEDIA)){
                JSONObject media = body.getJSONObject(MEDIA_KEY);
                this.width = media.getInt(WIDTH_KEY);
                this.height = media.getInt(HEIGHT_KEY);
                this.media_id = media.getString(MEDIA_ID_KEY);
                this.key = media.getString(KEY_KEY);
                this.iv = media.getString(IV_KEY);
            }else if(this.type.equalsIgnoreCase(TYPE_TEXT)){
                this.text = body.getString(TEXT_KEY);
            }
        }catch(JSONException e){
            e.printStackTrace();
            return this;
        }
        return this;
    }

    /**
     * Get sender username.
     *
     * @return sender username.
     */
    public String getSender(){
        return this.sender;
    }

    /**
     * Get all recipients.
     *
     * @return recipients.
     */
    public List<String> getRecipients(){
        return this.recipients;
    }

    /**
     * Get the text of this message.
     *
     * @return the text.
     */
    public String getText(){
        return this.text;
    }

    /**
     * Get the date of when this message was sent.
     *
     * @return unix timestamp of when this message was sent.
     */
    public long getSentTime(){
        return this.sent_time;
    }

    /**
     * Check if this message was an image.
     *
     * @return true if it is an image, otherwise false.
     */
    public boolean isMedia(){
        return this.type.equalsIgnoreCase(TYPE_MEDIA);
    }

    /**
     * Check if this message is a text message.
     *
     * @return true if it is a text message, otherwise false.
     */
    public boolean isTextMessage(){
        return this.type.equalsIgnoreCase(TYPE_TEXT);
    }

    /**
     * Get the width of this media.
     *
     * @return the width of this media. If this is not a media, returns -1.
     */
    public int getWidth(){
        if(!this.isMedia()){
            return -1;
        }
        return this.width;
    }

    /**
     * Get the height of this media.
     *
     * @return the height of this media. -1 if this is not a media.
     */
    public int getHeight(){
        if(!this.isMedia()){
            return -1;
        }
        return this.height;
    }

    /**
     * Get the key of this media. Used for decryption.
     *
     * @return the key of this media. Null if this is not a media.
     */
    public String getKey(){
        if(!this.isMedia()){
            return null;
        }
        return this.key;
    }

    /**
     * Get the iv key of this media. Used for decryption.
     *
     * @return the iv key of this media. Null if this is not a media.
     */
    public String getIVKey(){
        if(!this.isMedia()){
            return null;
        }
        return this.iv;
    }

    /**
     * Get the ID.
     *
     * @return the id.
     */
    public String getID(){
        return this.id;
    }

    /**
     * Get the Chat Message ID.
     *
     * @return the chat message id.
     */
    public String getChatMessageID(){
        return this.chat_message_id;
    }

    /**
     * Get the media id. Basically same has Message#getChatMessageID
     *
     * @return the media id. Null if not a media.
     */
    public String getMediaID(){
        if(!this.isMedia()){
            return null;
        }
        return this.media_id;
    }
}
