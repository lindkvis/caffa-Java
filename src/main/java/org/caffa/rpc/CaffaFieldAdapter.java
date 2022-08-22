package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.grpc.ManagedChannel;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CaffaFieldAdapter implements JsonDeserializer<CaffaField<?>>, JsonSerializer<CaffaField<?>> {
    private final CaffaObject object;
    private final ManagedChannel channel;
    private final String sessionUuid;
    private static Logger logger = LoggerFactory.getLogger(CaffaFieldAdapter.class);

    public CaffaFieldAdapter(CaffaObject object, ManagedChannel channel, String sessionUuid) {
        super();

        this.object = object;
        this.channel = channel;
        this.sessionUuid = sessionUuid;
    }

    public CaffaField<?> createField(String keyword, String dataType, JsonElement valueElement) {
        assert this.object != null;

        if (this.channel == null) {
            assert valueElement != null;
        }

        if (dataType.equals("object")) {
            logger.debug("Creating object field " + keyword + " with grpc: " + (this.channel != null));

            if (this.channel != null) {
                CaffaObjectField field = new CaffaObjectField(this.object, keyword);
                field.createGrpcAccessor(this.channel);
                return field;
            }

            CaffaObject caffaObject = new GsonBuilder()
                    .registerTypeAdapter(CaffaObject.class,
                            new CaffaObjectAdapter(this.channel, this.sessionUuid))
                    .create()
                    .fromJson(valueElement, CaffaObject.class);
            return new CaffaObjectField(this.object, keyword, caffaObject);

        }
        if (dataType.equals("object[]")) {
            logger.debug("Creating object array field " + keyword);

            if (this.channel != null) {
                CaffaObjectArrayField field = new CaffaObjectArrayField(this.object, keyword);
                field.createGrpcAccessor(this.channel);
                return field;
            }
            ArrayList<CaffaObject> objectList = new ArrayList<>();
            JsonArray objectArray = valueElement.getAsJsonArray();
            for (int i = 0; i < objectArray.size(); ++i) {
                CaffaObject caffaObject = new GsonBuilder()
                        .registerTypeAdapter(CaffaObject.class,
                                new CaffaObjectAdapter(this.channel, this.sessionUuid))
                        .create()
                        .fromJson(objectArray.get(i), CaffaObject.class);
                if (caffaObject != null) {
                    objectList.add(caffaObject);
                }
            }
            return new CaffaObjectArrayField(this.object, keyword, objectList);
        }
        if (dataType.endsWith("[][]"))
        {
            // TODO: Array of array fields are not supported in Caffa-Java yet, but we ignore them silently.
            return null;
        }
        logger.debug("Creating field " + keyword + " of type " + dataType);
        CaffaField<?> field = CaffaFieldFactory.createField(this.object, keyword, dataType);
        assert field != null;
        if (this.channel != null) {
            field.createGrpcAccessor(this.channel);
        } else {
            logger.debug("Setting local value for object " + object.classKeyword + " and field " + keyword
                    + " to: " + valueElement.toString());
            field.setJson(valueElement.toString());
        }
        return field;
    }

    public CaffaField<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        assert json != null;
        logger.debug("JSON: " + json.toString());

        final JsonObject jsonObject = json.getAsJsonObject();

        String keyword = jsonObject.get("keyword").getAsString();
        String dataType = jsonObject.get("type").getAsString();

        JsonElement valueElement = null;
        if (this.channel == null) {
            assert jsonObject.has("value");
            valueElement = jsonObject.get("value");
        }
        return createField(keyword, dataType, valueElement);
    }

    @Override
    public JsonElement serialize(CaffaField<?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        String type = src.typeString();

        logger.debug("Writing field: " + src.keyword + " with type: '" + type + "'");

        jsonObject.addProperty("type", type);
        if (this.channel == null) {
            JsonElement element = JsonParser.parseString(src.getJson());
            jsonObject.add("value", element);
        }
        logger.debug("Done writing field: " + src.keyword);
        return jsonObject;
    }
}
