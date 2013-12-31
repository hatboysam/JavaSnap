package com.habosa.javasnap;

import org.json.JSONObject;

/**
 * Author: samstern
 * Date: 12/31/13
 */
public interface JSONBinder<T> {

    public T bind(JSONObject obj);

}
