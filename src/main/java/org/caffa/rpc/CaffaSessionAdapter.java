package org.caffa.rpc;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CaffaSessionAdapter
        implements JsonDeserializer<CaffaSession>, JsonSerializer<CaffaSession> {

    public CaffaSessionAdapter() {
        super();

    }

    public CaffaSession deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            String uuid = jsonObject.get("uuid").getAsString();
            String typeString = jsonObject.get("type").getAsString();
            boolean valid = jsonObject.get("valid").getAsBoolean();
            if (valid) {
                CaffaSession.Type typeFound = CaffaSession.Type.fromString(typeString);
                return new CaffaSession(uuid, typeFound);
            }
        }
        return null;
    }

    @Override
    public JsonElement serialize(CaffaSession src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonObject();
    }
}
