package org.caffa.rpc;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CaffaAppEnumAdapter
        implements JsonDeserializer<CaffaAppEnum>, JsonSerializer<CaffaAppEnum> {

    public CaffaAppEnumAdapter() {
        super();

    }

    public CaffaAppEnum deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonElement valueElement = json;
        if (json.isJsonObject())
        {
            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has("value")) {
                valueElement = jsonObject.get("value");
            }
        }
        return new CaffaAppEnum(valueElement.getAsString());
    }

    @Override
    public JsonElement serialize(CaffaAppEnum src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.value());
    }
}
