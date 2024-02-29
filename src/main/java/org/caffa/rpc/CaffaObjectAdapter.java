package org.caffa.rpc;

import java.lang.reflect.Type;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CaffaObjectAdapter implements JsonDeserializer<CaffaObject>, JsonSerializer<CaffaObject> {
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectAdapter.class);

    protected final RestClient client;
    protected final JsonObject schemaObject;
    private final boolean createLocalFields;

    /**
     * Constructor
     *
     */
    public CaffaObjectAdapter(RestClient client, JsonObject schemaObject, boolean createLocalFields) {
        super();

        this.client = client;
        this.schemaObject = resolveObjectSchema(schemaObject);
        this.createLocalFields = createLocalFields;
    }

    private JsonObject resolveObjectSchema(JsonObject schemaObject)
    {
        if (schemaObject == null || schemaObject.isJsonNull()) return schemaObject;

        if (schemaObject.has("items")) {
            return resolveObjectSchema(schemaObject.get("items").getAsJsonObject());
        }

        if (schemaObject.has("$ref")) {
            return client.getObjectSchema(schemaObject.get("$ref").getAsString());
        }
        return schemaObject;
    }

    public CaffaObject deserialize(JsonElement jsonValue, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        assert (jsonValue.isJsonObject());

        JsonObject valueObject = jsonValue.getAsJsonObject();

        String id = schemaObject.get("$id").getAsString();
        String[] path = id.split("/");

        String classKeyword = path[path.length - 1];
        String uuid = "";
        if (valueObject.has("uuid")) {
            uuid = valueObject.get("uuid").getAsString();
        }

        CaffaObject caffaObject = new CaffaObject(classKeyword, createLocalFields, client, uuid);
        readProperties(caffaObject, schemaObject, valueObject);

        return caffaObject;
    }

    private void readProperties(CaffaObject caffaObject, JsonObject schemaObject, JsonObject valueObject) {
        if (schemaObject.has("allOf")) {
            JsonArray allOfArray = schemaObject.get("allOf").getAsJsonArray();
            for (JsonElement entry : allOfArray) {
                JsonObject entryObject = entry.getAsJsonObject();
                if (entryObject.has("properties")) {
                    JsonObject properties = entryObject.get("properties").getAsJsonObject();
                    readFields(caffaObject, properties, valueObject);
                    if (properties.has("methods")) {
                        JsonObject methods = properties.get("methods").getAsJsonObject();
                        if (methods.has("properties")) {
                            JsonObject methodProperties = methods.get("properties").getAsJsonObject();
                            readMethods(caffaObject, methodProperties);
                        }
                    }
                } else if (entryObject.has("$ref")) {
                    String schemaLocation = entryObject.get("$ref").getAsString();
                    JsonObject subSchemaObject = client.getObjectSchema(schemaLocation);
                    readProperties(caffaObject, subSchemaObject, valueObject);
                }
            }
        }
    }

    public void readField(CaffaObject caffaObject, String keyword, JsonObject schema, JsonElement value) {

        CaffaField<?> field = new GsonBuilder()
                .registerTypeAdapter(CaffaField.class,
                        new CaffaFieldAdapter(caffaObject, keyword, schema, createLocalFields))
                .registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.client, schema, this.createLocalFields))
                .create()
                .fromJson(value, CaffaField.class);
        if (field != null) {
            caffaObject.addField(field);
        }
    }

    public void readMethod(CaffaObject caffaObject, String keyword, JsonObject schema) {
        CaffaObjectMethod method = new GsonBuilder()
                .registerTypeAdapter(CaffaObjectMethod.class, new CaffaObjectMethodAdapter(caffaObject, keyword))
                .create()
                .fromJson(schema, CaffaObjectMethod.class);
        if (method != null) {
            caffaObject.addMethod(method);
        }
    }

    public void readFields(CaffaObject caffaObject, JsonObject propertiesSchema, JsonObject objectValue) {
        assert caffaObject != null && propertiesSchema != null;

        for (String key : propertiesSchema.keySet()) {
            if (!key.equals("methods") && !key.equals("keyword") && !key.equals("uuid") && !key.startsWith("$")) {
                JsonElement fieldValue = CaffaFieldAdapter.NULL_PLACEHOLDER;
                if (objectValue.has(key)) {
                    fieldValue = objectValue.get(key);
                }
                readField(caffaObject, key, propertiesSchema.get(key).getAsJsonObject(), fieldValue);
            }
        }
    }

    public void readMethods(CaffaObject caffaObject, JsonElement containerElement) {
        assert caffaObject != null && containerElement != null;
        assert containerElement.isJsonObject();
        JsonObject object = containerElement.getAsJsonObject();
        for (String key : object.keySet()) {
            readMethod(caffaObject, key, object.get(key).getAsJsonObject());
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

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (CaffaField<?> field : caffaObject.fields()) {
                jsonObject.add(field.keyword,
                        new CaffaFieldAdapter(caffaObject, field.keyword, null, this.createLocalFields).serialize(field,
                                typeOfSrc,
                                context));
            }
        }
    }
}
