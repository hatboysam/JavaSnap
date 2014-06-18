package com.habosa.javasnap;

import org.json.JSONObject;

/**
 * Created by James on 2014-06-17.
 */
public class Message implements JSONBinder<Message> {

    @Override
    public Message bind(JSONObject obj) {
        //TODO
        System.out.println("Message :" + obj.toString());
        return null;
    }
}
