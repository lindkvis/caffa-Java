package org.caffa.rpc;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CaffaAppInfoAdapter
        implements JsonDeserializer<CaffaAppInfo>, JsonSerializer<CaffaAppEnum> {

    public CaffaAppInfoAdapter() {
        super();

    }

    public CaffaAppInfo deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        if (json.isJsonObject()) {
            JsonObject jsonObject = json.getAsJsonObject();
            return new CaffaAppInfo(jsonObject.get("name").getAsString(), jsonObject.get("major_version").getAsInt(),
                    jsonObject.get("minor_version").getAsInt(), jsonObject.get("patch_version").getAsInt(),
                    jsonObject.get("type").getAsInt());
        }
        return new CaffaAppInfo("Unknown", 0, 0, 0, 0);
    }

    @Override
    public JsonElement serialize(CaffaAppEnum src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonObject();
    }
}
