package org.caffa.rpc;

import org.caffa.rpc.CaffaObject;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CaffaObjectAdapter implements JsonDeserializer<CaffaObject> {
    public CaffaObject deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject object = json.getAsJsonObject();

        CaffaObject caffaObject = new CaffaObject();
        caffaObject.classKeyword = object.get("classKeyword").getAsString();
        if (object.has("fields") && object.get("fields").isJsonArray()) {
            JsonArray fields = object.get("fields").getAsJsonArray();
            for (JsonElement jsonElement : fields) {
                CaffaAbstractField field = context.deserialize(jsonElement, CaffaAbstractField.class);
                caffaObject.fields.add(field);
            }
        }
        return caffaObject;
    }

}
