package io.disassemble.knn;

import com.google.gson.JsonObject;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class JsonEntry {

    public final String key;
    public final JsonObject source;

    public JsonEntry(String key, JsonObject source) {
        this.key = key;
        this.source = source;
    }
}
