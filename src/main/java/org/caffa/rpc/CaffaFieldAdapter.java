package org.caffa.rpc;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CaffaFieldAdapter implements JsonDeserializer<CaffaField<?>>, JsonSerializer<CaffaField<?>> {
    private final CaffaObject object;
    private final boolean grpc;
    protected static final Logger logger = Logger.getLogger(CaffaFieldAdapter.class.getName());

    public CaffaFieldAdapter(CaffaObject object) {
        super();

        this.object = object;
        this.grpc = true;
    }

    public CaffaFieldAdapter(CaffaObject object, boolean grpc) {
        super();

        this.object = object;
        this.grpc = grpc;
    }

    public CaffaField<?> createField(String keyword, String dataType, JsonElement valueElement) {
        if (dataType.equals("object")) {
            logger.log(Level.FINER, "Creating object field " + keyword);
            if (this.grpc)
                return new CaffaObjectField(this.object, keyword);

            CaffaObject caffaObject = new GsonBuilder()
                    .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.object.channel, this.grpc)).create()
                    .fromJson(valueElement, CaffaObject.class);
            return new CaffaObjectField(this.object, keyword, caffaObject);

        } else if (dataType.equals("object[]")) {
            logger.log(Level.FINER, "Creating object array field " + keyword);

            if (!this.grpc) {
                ArrayList<CaffaObject> objectList = new ArrayList<>();
                JsonArray objectArray = valueElement.getAsJsonArray();
                for (int i = 0; i < objectArray.size(); ++i) {
                    CaffaObject caffaObject = new GsonBuilder()
                            .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.object.channel, this.grpc))
                            .create()
                            .fromJson(objectArray.get(i), CaffaObject.class);
                    if (caffaObject != null) {
                        objectList.add(caffaObject);
                    }
                }
                return new CaffaObjectArrayField(this.object, keyword, objectList);
            }
            return new CaffaObjectArrayField(this.object, keyword);
        }
        if (dataType.endsWith("[]")) {
            logger.log(Level.FINER, "Creating array field " + keyword);
            dataType = dataType.substring(0, dataType.length() - 2);
            CaffaField<?> field = CaffaFieldFactory.createArrayField(this.object, keyword, dataType);
            field.createAccessor(this.grpc);
            if (!this.grpc) {
                logger.log(Level.FINER, "Setting local value for object " + object.classKeyword + " and []field "
                        + keyword + " to: " + valueElement.toString());
                field.setJson(valueElement.toString());
            }

            return field;
        }
        logger.log(Level.FINER, "Creating scalar field " + keyword);
        CaffaField<?> field = CaffaFieldFactory.createField(this.object, keyword, dataType);
        field.createAccessor(this.grpc);
        if (!this.grpc) {
            logger.log(Level.FINER, "Setting local value for object " + object.classKeyword + " and field " + keyword
                    + " to: " + valueElement.toString());
            field.setJson(valueElement.toString());
        }
        return field;
    }

    public CaffaField<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        logger.log(Level.FINER, "JSON: " + json.toString());
        final JsonObject jsonObject = json.getAsJsonObject();

        String keyword = jsonObject.get("keyword").getAsString();
        String dataType = jsonObject.get("type").getAsString();

        JsonElement valueElement = jsonObject.get("value");
        return createField(keyword, dataType, valueElement);
    }

    @Override
    public JsonElement serialize(CaffaField<?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        String type = src.typeString();

        logger.log(Level.FINER, "Writing field: " + src.keyword + " with type: '" + type + "'");
        System.out.println("Writing field " + src.keyword + " with grpc: " + grpc + " and type: '" + type + "'");

        jsonObject.addProperty("type", type);
        if (!this.grpc) {
            JsonElement element = JsonParser.parseString(src.getJson());
            jsonObject.add("value", element);
        }
        logger.log(Level.FINER, "Done writing field: " + src.keyword);
        return jsonObject;
    }
}
