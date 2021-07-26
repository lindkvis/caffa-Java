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
        if (object.has("uuid") && object.get("uuid").isJsonPrimitive()) {
            caffaObject.uuid = object.get("uuid").getAsString();
        } else {
            System.err.println("Could not find uuid in object!");
        }

        return caffaObject;
    }

    public void readFields(CaffaObject caffaObject, JsonElement json)
    {
        final JsonObject object = json.getAsJsonObject();

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
        if (object.has("uuid") && object.get("uuid").isJsonPrimitive()) {
            caffaObject.uuid = object.get("uuid").getAsString();
        } else {
            System.err.println("Could not find uuid in object!");
        }
    }

    public JsonElement serialize(CaffaObject caffaObject, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        writeFields(caffaObject, jsonObject);
        return jsonObject;
    }

    public void writeFields(CaffaObject caffaObject,  JsonObject jsonObject)
    {
        jsonObject.addProperty("classKeyword", caffaObject.classKeyword);
        jsonObject.addProperty("uuid", caffaObject.uuid);
    }

}
