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

public class CaffaObjectAdapter implements JsonDeserializer<CaffaObject>, JsonSerializer<CaffaObject> {
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectAdapter.class);

    protected final RestClient client;
    protected final boolean createLocalObjects;

    /**
     * Constructor
     *
     * @param client if null the adapter will create local objects without a
     *               RPC connection
     */
    public CaffaObjectAdapter(RestClient client, boolean createLocalObjects) {
        super();

        this.client = client;
        this.createLocalObjects = createLocalObjects;
    }

    /**
     * Constructor for creating local objects without a RPC connection
     *
     */
    public CaffaObjectAdapter() {
        super();

        this.client = null;
        this.createLocalObjects = true;
    }

    public CaffaObject deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        assert (json.isJsonObject());
        JsonObject object = json.getAsJsonObject();

        if (object.has("value")) {
            object = object.get("value").getAsJsonObject();
        }

        String classKeyword = object.get("keyword").getAsString();

        CaffaObject caffaObject = null;

        if (object.has("uuid")) {
            String objectUuid = object.get("uuid").getAsString();

            caffaObject = new CaffaObject(classKeyword, objectUuid, this.createLocalObjects);

        } else {
            caffaObject = new CaffaObject(classKeyword);
        }

        if (this.client != null) {
            caffaObject.createRestAccessor(this.client);
        }

        new GsonBuilder()
                .registerTypeAdapter(CaffaField.class,
                        new CaffaFieldAdapter(caffaObject, this.client, this.createLocalObjects))
                .registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.client, this.createLocalObjects))
                .create();

        readFields(caffaObject, object);
        readMethods(caffaObject, object);

        return caffaObject;
    }

    public void readField(CaffaObject caffaObject, JsonElement jsonElement) {
        CaffaField<?> field = new GsonBuilder()
                .registerTypeAdapter(CaffaField.class,
                        new CaffaFieldAdapter(caffaObject, this.client, this.createLocalObjects))
                .registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.client, this.createLocalObjects))
                .create()
                .fromJson(jsonElement, CaffaField.class);
        if (field != null) {
            caffaObject.addField(field);
        }
    }

    public void readMethod(CaffaObject caffaObject, JsonObject jsonElement) {
        System.out.println("SELF: " + caffaObject.uuid);
        System.out.println(jsonElement.toString());
        CaffaObjectMethod method = new GsonBuilder().registerTypeAdapter(CaffaField.class,
                new CaffaFieldAdapter(caffaObject, this.client, true))
                .registerTypeAdapter(CaffaObjectMethod.class,
                        new CaffaObjectMethodAdapter(caffaObject))
                .create()
                .fromJson(jsonElement, CaffaObjectMethod.class);
        if (method != null) {
            System.out.println("Method created: " + method.dump());
            caffaObject.addMethod(method);
        }
    }

    public void readFields(CaffaObject caffaObject, JsonElement containerElement) {
        assert caffaObject != null && containerElement != null;

        if (containerElement.isJsonObject()) {
            JsonObject object = containerElement.getAsJsonObject();

            for (String key : object.keySet()) {
                if (!key.equals("methods") && !key.equals("doc") && !key.equals("keyword") && !key.equals("keyword")
                        && !key.equals("uuid") && !object.get(key).isJsonNull()) {
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

    public void readMethods(CaffaObject caffaObject, JsonElement containerElement) {
        assert caffaObject != null && containerElement != null;

        if (containerElement.isJsonObject()) {
            System.out.println("Reading methods from object: " + containerElement.toString());

            JsonObject object = containerElement.getAsJsonObject();
            if (!object.has("methods"))
                return;

            JsonElement methodElement = object.get("methods");
            readMethods(caffaObject, methodElement);
        } else if (containerElement.isJsonArray()) {
            System.out.println("Reading methods from array: " + containerElement.toString());

            JsonArray array = containerElement.getAsJsonArray();

            for (JsonElement methodElement : array) {
                System.out.println("Got method: " + methodElement.toString());
                readMethod(caffaObject, methodElement.getAsJsonObject());
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
        if (this.client != null) {
            logger.debug("with a gRPC-connection");
        } else {
            logger.debug("without a gRPC-connection");
        }

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (CaffaField<?> field : caffaObject.fields()) {
                jsonObject.add(field.keyword,
                        new CaffaFieldAdapter(caffaObject, this.client, this.createLocalObjects).serialize(field,
                                typeOfSrc,
                                context));
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (CaffaField<?> field : caffaObject.fields()) {
                JsonElement fieldElement = new CaffaFieldAdapter(caffaObject, this.client, this.createLocalObjects)
                        .serialize(field, typeOfSrc, context);
                fieldElement.getAsJsonObject().addProperty("keyword", field.keyword);
                jsonArray.add(fieldElement);
            }
        }
    }
}
