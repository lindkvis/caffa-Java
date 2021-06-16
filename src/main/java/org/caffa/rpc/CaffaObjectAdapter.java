package org.caffa.rpc;

import org.caffa.rpc.CaffaObject;

import io.grpc.ManagedChannel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;

public class CaffaObjectAdapter implements JsonDeserializer<CaffaObject>, JsonSerializer<CaffaObject> {
    private final ManagedChannel channel;

    public CaffaObjectAdapter(ManagedChannel channel) {
        super();

        this.channel = channel;
    }

    public CaffaObject deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        final JsonObject object = json.getAsJsonObject();

        CaffaObject caffaObject = new CaffaObject(this.channel);

        if (object.has("fields") && object.get("fields").isJsonArray()) {
            JsonArray fields = object.get("fields").getAsJsonArray();
            for (JsonElement jsonElement : fields) {
                CaffaAbstractField field = new GsonBuilder()
                        .registerTypeAdapter(CaffaAbstractField.class, new CaffaFieldAdapter(caffaObject)).create()
                        .fromJson(jsonElement, CaffaAbstractField.class);

                caffaObject.fields.put(field.keyword, field);
            }
        }
        if (object.has("classKeyword") && object.get("classKeyword").isJsonPrimitive()) {
            caffaObject.classKeyword = object.get("classKeyword").getAsString();
        } else {
            System.err.println("Could not find classKeyword in object!");
        }
        if (object.has("serverAddress") && object.get("serverAddress").isJsonPrimitive()) {
            caffaObject.serverAddress = object.get("serverAddress").getAsLong();
        } else {
            System.err.println("Could not find serverAddress in object!");
        }

        return caffaObject;
    }

    public JsonElement serialize(CaffaObject caffaObject, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("classKeyword", caffaObject.classKeyword);
        jsonObject.addProperty("serverAddress", caffaObject.serverAddress);

        return jsonObject;
    }

}
