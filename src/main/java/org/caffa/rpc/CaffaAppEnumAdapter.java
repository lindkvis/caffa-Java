package org.caffa.rpc;

import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CaffaAppEnumAdapter
        implements JsonDeserializer<CaffaAppEnum>, JsonSerializer<CaffaAppEnum> {
    protected static final Logger logger = Logger.getLogger(CaffaAppEnumAdapter.class.getName());

    public CaffaAppEnumAdapter() {
        super();

    }

    public CaffaAppEnum deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        logger.log(Level.FINEST, "JSON value: " + json.toString());
        return new CaffaAppEnum(json.getAsString());
    }

    @Override
    public JsonElement serialize(CaffaAppEnum src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.value());
    }
}
