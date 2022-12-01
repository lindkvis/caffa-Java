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
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectAdapter.class);

    protected final ManagedChannel channel;
    protected final String sessionUuid;

    /**
     * Constructor
     *
     * @param channel     if null the adapter will create local objects without a
     *                    gRPC connection
     * @param sessionUuid
     */
    public CaffaObjectAdapter(ManagedChannel channel, String sessionUuid) {
        super();

        this.channel = channel;
        this.sessionUuid = sessionUuid;
    }

    /**
     * Constructor for creating local objects without a gRPC connection
     *
     * @param sessionUuid
     */
    public CaffaObjectAdapter(String sessionUuid) {
        super();

        this.channel = null;
        this.sessionUuid = sessionUuid;
    }

    public CaffaObject deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        assert (json.isJsonObject());
        final JsonObject object = json.getAsJsonObject();

        assert object.has("class");

        String classKeyword = object.get("class").getAsString();

        CaffaObject caffaObject = null;

        if (object.has("uuid")) {
            String objectUuid = object.get("uuid").getAsString();

            caffaObject = new CaffaObject(classKeyword, objectUuid, this.sessionUuid);
            if (this.channel != null) {
                caffaObject.createGrpcAccessor(this.channel);
            }
        } else {
            caffaObject = new CaffaObject(classKeyword, this.sessionUuid);
        }

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CaffaField.class,
                        new CaffaFieldAdapter(caffaObject, this.channel))
                .registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.channel, this.sessionUuid))
                .create();

        readFields(caffaObject, object);

        return caffaObject;
    }

    public void readFields(CaffaObject caffaObject, JsonObject object) {
        assert caffaObject != null && object != null && object.isJsonObject();

        logger.debug("JSON: " + object.toString());

        for (String key : object.keySet()) {
            if (!key.equals("class") && !key.equals("uuid") && !object.get(key).isJsonNull()) {
                JsonObject jsonElement = object.get(key).getAsJsonObject();
                jsonElement.addProperty("keyword", key);
                CaffaField<?> field = new GsonBuilder()
                        .registerTypeAdapter(CaffaField.class,
                                new CaffaFieldAdapter(caffaObject, this.channel))
                        .create()
                        .fromJson(jsonElement, CaffaField.class);
                if (field != null) {
                    caffaObject.addField(field);
                }
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
        logger.debug("Writing fields for object: " + caffaObject.classKeyword);
        if (this.channel != null) {
            logger.debug("with a gRPC-connection");
        } else {
            logger.debug("without a gRPC-connection");
        }
        jsonObject.addProperty("class", caffaObject.classKeyword);
        jsonObject.addProperty("uuid", caffaObject.uuid);

        for (CaffaField<?> field : caffaObject.fields()) {
            jsonObject.add(field.keyword,
                    new CaffaFieldAdapter(caffaObject, this.channel).serialize(field, typeOfSrc,
                            context));
        }
    }
}
