package org.caffa.rpc;

import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.grpc.ManagedChannel;

public class CaffaObjectAdapter implements JsonDeserializer<CaffaObject>, JsonSerializer<CaffaObject> {
    protected final ManagedChannel channel;
    protected final boolean grpc;
    protected static final Logger logger = Logger.getLogger(CaffaObjectAdapter.class.getName());

    public CaffaObjectAdapter(ManagedChannel channel, boolean grpc) {
        super();

        this.channel = channel;
        this.grpc = grpc;
    }

    public CaffaObject deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        final JsonObject object = json.getAsJsonObject();

        CaffaObject caffaObject = new CaffaObject(this.channel);

        Gson gson = new GsonBuilder().registerTypeAdapter(CaffaField.class, new CaffaFieldAdapter(caffaObject, this.grpc))
                .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.channel, this.grpc)).create();

        logger.log(Level.FINER, "Deserializing object: " + object.toString());

        if (object.has("Class") && object.get("Class").isJsonPrimitive()) {
            caffaObject.classKeyword = object.get("Class").getAsString();
        } else {
            logger.log(Level.SEVERE, "Could not find classKeyword in object!");
        }
        if (object.has("UUID") && object.get("UUID").isJsonPrimitive()) {
            caffaObject.uuid = object.get("UUID").getAsString();
        } else {
            logger.log(Level.SEVERE, "Could not find uuid in object!");
        }

        for (String key : object.keySet()) {
            if (!key.equals("Class") && !key.equals("UUID")) {
                JsonObject jsonElement = object.get(key).getAsJsonObject();
                jsonElement.addProperty("keyword", key);
                CaffaField<?> field = gson.fromJson(jsonElement, CaffaField.class);
                caffaObject.fields.put(field.keyword, field);
            }
        }

        return caffaObject;
    }

    public void readFields(CaffaObject caffaObject, JsonElement json) {
        logger.log(Level.FINER, "JSON: " + json.toString());
        final JsonObject object = json.getAsJsonObject();

        if (object.has("Class") && object.get("Class").isJsonPrimitive()) {
            caffaObject.classKeyword = object.get("Class").getAsString();
        } else {
            logger.log(Level.SEVERE, "Could not find classKeyword in object!");
        }
        if (object.has("UUID") && object.get("UUID").isJsonPrimitive()) {
            caffaObject.uuid = object.get("UUID").getAsString();
        } else {
            logger.log(Level.SEVERE, "Could not find uuid in object!");
        }

        for (String key : object.keySet()) {
            if (!key.equals("Class") && !key.equals("UUID")) {
                JsonObject jsonElement = object.get(key).getAsJsonObject();
                jsonElement.addProperty("keyword", key);
                CaffaField<?> field = new GsonBuilder()
                        .registerTypeAdapter(CaffaField.class, new CaffaFieldAdapter(caffaObject, this.grpc)).create()
                        .fromJson(jsonElement, CaffaField.class);

                caffaObject.fields.put(field.keyword, field);
            }
        }

    }

    public JsonElement serialize(CaffaObject caffaObject, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        writeFields(caffaObject, jsonObject, typeOfSrc, context);
        return jsonObject;
    }

    public void writeFields(CaffaObject caffaObject, JsonObject jsonObject, Type typeOfSrc,
            JsonSerializationContext context) {
        logger.log(Level.FINER, "Writing fields for object: " + caffaObject.classKeyword + " " + this.grpc);
        jsonObject.addProperty("Class", caffaObject.classKeyword);
        jsonObject.addProperty("UUID", caffaObject.uuid);

        for (CaffaField<?> field : caffaObject.fields()) {
            jsonObject.add(field.keyword,
                    new CaffaFieldAdapter(caffaObject, this.grpc).serialize(field, typeOfSrc, context));
        }
    }
}
