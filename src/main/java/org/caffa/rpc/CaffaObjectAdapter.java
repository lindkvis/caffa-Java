package org.caffa.rpc;

import java.lang.reflect.Type;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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
    protected final boolean createLocalObjects;

    /**
     * Constructor
     *
     * @param channel     if null the adapter will create local objects without a
     *                    gRPC connection
     * @param sessionUuid
     */
    public CaffaObjectAdapter(ManagedChannel channel, String sessionUuid, boolean createLocalObjects) {
        super();

        this.channel = channel;
        this.sessionUuid = sessionUuid;
        this.createLocalObjects = createLocalObjects;
    }

    /**
     * Constructor for creating local objects without a gRPC connection
     *
     * @param sessionUuid
     */
    public CaffaObjectAdapter() {
        super();

        this.channel = null;
        this.sessionUuid = "";
        this.createLocalObjects = true;
    }

    public CaffaObject deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        assert (json.isJsonObject());
        final JsonObject object = json.getAsJsonObject();
        String classKeyword = object.get("keyword").getAsString();

        CaffaObject caffaObject = null;

        if (object.has("uuid")) {
            String objectUuid = object.get("uuid").getAsString();

            caffaObject = new CaffaObject(classKeyword, objectUuid, this.sessionUuid, this.createLocalObjects);
           
        } else {
            caffaObject = new CaffaObject(classKeyword);
        }

        if (this.channel != null) {
            caffaObject.createGrpcAccessor(this.channel);
        }

        new GsonBuilder()
                .registerTypeAdapter(CaffaField.class,
                        new CaffaFieldAdapter(caffaObject, this.channel, this.createLocalObjects))
                .registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.channel, this.sessionUuid, this.createLocalObjects))
                .create();

        readFields(caffaObject, object);

        return caffaObject;
    }

    public void readField(CaffaObject caffaObject, JsonElement jsonElement) {
        CaffaField<?> field = new GsonBuilder()
                .registerTypeAdapter(CaffaField.class,
                        new CaffaFieldAdapter(caffaObject, this.channel, this.createLocalObjects))
                .registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.channel, this.sessionUuid, this.createLocalObjects))
                .create()
                .fromJson(jsonElement, CaffaField.class);
        if (field != null) {
            caffaObject.addField(field);
        }
    }

    public void readFields(CaffaObject caffaObject, JsonElement containerElement) {
        assert caffaObject != null && containerElement != null;

        if (containerElement.isJsonObject()) {
            JsonObject object = containerElement.getAsJsonObject();
         
            for (String key : object.keySet()) {
                if (!key.equals("keyword") && !key.equals("keyword") && !key.equals("uuid") && !object.get(key).isJsonNull()) {
                    JsonObject fieldElement = object.get(key).getAsJsonObject();
                    fieldElement.addProperty("keyword", key);
                    readField(caffaObject, fieldElement);
            
                }
            }
        } else if (containerElement.isJsonArray()) {
            JsonArray array = containerElement.getAsJsonArray();

            for (int i = 0; i < array.size(); ++i) {
                JsonObject fieldElement = array.get(i).getAsJsonObject();
                readField(caffaObject, fieldElement);
            }
        }
    }

    public JsonElement serialize(CaffaObject caffaObject, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("keyword", caffaObject.keyword);
        if (!caffaObject.uuid.isEmpty()) {
            jsonObject.addProperty("uuid", caffaObject.uuid);
        }

        writeFields(caffaObject, jsonObject, typeOfSrc, context);
        return jsonObject;
    }

    public void writeFields(CaffaObject caffaObject, JsonElement jsonElement, Type typeOfSrc,
            JsonSerializationContext context) {
        logger.debug("Writing fields for object: " + caffaObject.keyword);
        if (this.channel != null) {
            logger.debug("with a gRPC-connection");
        } else {
            logger.debug("without a gRPC-connection");
        }

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (CaffaField<?> field : caffaObject.fields()) {
                jsonObject.add(field.keyword,
                        new CaffaFieldAdapter(caffaObject, this.channel, this.createLocalObjects).serialize(field, typeOfSrc,
                                context));
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (CaffaField<?> field : caffaObject.fields()) {
                JsonElement fieldElement = new CaffaFieldAdapter(caffaObject, this.channel, this.createLocalObjects).serialize(field, typeOfSrc, context);
                fieldElement.getAsJsonObject().addProperty("keyword", field.keyword);
                jsonArray.add(fieldElement);
            }
        }
    }
}
