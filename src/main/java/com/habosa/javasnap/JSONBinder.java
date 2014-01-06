package com.habosa.javasnap;

import org.json.JSONObject;

/**
 * Author: samstern
 * Date: 12/31/13
 */
public interface JSONBinder<T> {

    /**
     * Populate the fields of this object from a JSONObject.
     *
     * @param obj the JSONObject to use as a data source
     * @return this object.
     */
    public T bind(JSONObject obj);

}
