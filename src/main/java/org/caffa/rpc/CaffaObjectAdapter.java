package org.caffa.rpc;

import java.lang.reflect.Type;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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
    protected final String sessionUuid;
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectAdapter.class);

    public CaffaObjectAdapter(ManagedChannel channel, boolean grpc, String sessionUuid) {
        super();

        this.channel = channel;
        this.grpc = grpc;
        this.sessionUuid = sessionUuid;
    }

    public CaffaObject deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        assert (json.isJsonObject());
        final JsonObject object = json.getAsJsonObject();

        assert object.has("Class") && object.has("UUID");

        String classKeyword = object.get("Class").getAsString();
        String objectUuid = object.get("UUID").getAsString();

        CaffaObject caffaObject = new CaffaObject(classKeyword, objectUuid, this.channel, this.grpc, this.sessionUuid);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CaffaField.class, new CaffaFieldAdapter(caffaObject, this.grpc, this.sessionUuid))
                .registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.channel, this.grpc, this.sessionUuid))
                .create();

        readFields(caffaObject, object);

        return caffaObject;
    }

    public void readFields(CaffaObject caffaObject, JsonObject object) {
        assert caffaObject != null && object != null && object.isJsonObject();

        logger.debug("JSON: " + object.toString());

        for (String key : object.keySet()) {
            if (!key.equals("Class") && !key.equals("UUID")) {
                JsonObject jsonElement = object.get(key).getAsJsonObject();
                jsonElement.addProperty("keyword", key);
                CaffaField<?> field = new GsonBuilder()
                        .registerTypeAdapter(CaffaField.class,
                                new CaffaFieldAdapter(caffaObject, this.grpc, this.sessionUuid))
                        .create()
                        .fromJson(jsonElement, CaffaField.class);

                caffaObject.addField(field);
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
        logger.debug("Writing fields for object: " + caffaObject.classKeyword + " " + this.grpc);
        jsonObject.addProperty("Class", caffaObject.classKeyword);
        jsonObject.addProperty("UUID", caffaObject.uuid);

        for (CaffaField<?> field : caffaObject.fields()) {
            jsonObject.add(field.keyword,
                    new CaffaFieldAdapter(caffaObject, this.grpc, this.sessionUuid).serialize(field, typeOfSrc,
                            context));
        }
    }
}
