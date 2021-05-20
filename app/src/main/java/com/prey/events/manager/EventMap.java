/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.events.manager;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("serial")
public class EventMap<K, V> extends HashMap<K, V> {

    public JSONArray toJSONArray() {
        JSONArray jsonjArray = new JSONArray();
        Iterator<K> it = this.keySet().iterator();
        while (it.hasNext()) {
            K key = (K) it.next();
            JSONObject data = (JSONObject) this.get(key);
            jsonjArray.put(data);
        }
        return jsonjArray;
    }

    @SuppressWarnings("rawtypes")
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        Iterator<K> it = this.keySet().iterator();
        while (it.hasNext()) {
            K key = (K) it.next();
            JSONObject data = (JSONObject) this.get(key);

            Iterator ite2 = data.keys();
            while (ite2.hasNext()) {
                String name = (String) ite2.next();
                try {
                    jsonObject.put(name, data.get(name));
                } catch (JSONException e) {
                }
            }
        }
        return jsonObject;
    }

    public boolean isCompleteData() {
        boolean isCompleteData = true;
        Iterator<K> it = this.keySet().iterator();
        while (it.hasNext()) {
            K key = (K) it.next();
            JSONObject data = (JSONObject) this.get(key);
            if (data == null) {
                isCompleteData = false;
                break;
            }
        }
        return isCompleteData;
    }

}