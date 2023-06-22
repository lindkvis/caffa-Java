package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class CaffaFieldAdapter implements JsonDeserializer<CaffaField<?>>, JsonSerializer<CaffaField<?>> {
    private final CaffaObject object;
    private final ManagedChannel channel;
    private static final Logger logger = LoggerFactory.getLogger(CaffaFieldAdapter.class);

    public CaffaFieldAdapter(CaffaObject object, ManagedChannel channel) {
        super();

        this.object = object;
        this.channel = channel;
    }

    public CaffaField<?> createField(String keyword, String dataType, JsonElement valueElement) {
        assert this.object != null;

        if (dataType.endsWith("[][]")) {
            // TODO: Array of array fields are not supported in Caffa-Java yet, but we
            // ignore them silently.
            return null;
        }
        logger.debug("Creating field " + keyword + " of type " + dataType);
        CaffaField<?> field = CaffaFieldFactory.createField(this.object, keyword, dataType);
        assert field != null;
        if (this.channel != null) {
            field.createGrpcAccessor(this.channel);
        } else if (valueElement != null) {
            logger.debug("Setting local value for object " + object.keyword + " and field " + keyword
                    + " to: " + valueElement);
            field.setJson(valueElement.toString());
        } else {
            field.setJson("");
        }
        return field;
    }

    public CaffaField<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        assert json != null;
        logger.debug("JSON: " + json);

        final JsonObject jsonField = json.getAsJsonObject();

        String keyword = jsonField.get("keyword").getAsString();
        String dataType = jsonField.get("type").getAsString();

        JsonElement valueElement = null;

        if (jsonField.has("value")) {
            valueElement = jsonField.get("value");
        }
        return createField(keyword, dataType, valueElement);
    }

    @Override
    public JsonElement serialize(CaffaField<?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonField = new JsonObject();

        String type = src.typeString();

        jsonObject.addProperty("keyword", src.keyword);
        jsonObject.addProperty("type", type);
        if (src.isLocalField()) {
            String jsonString = src.getJson();
            JsonElement element = JsonParser.parseString(jsonString);
            jsonField.add("value", element);
        }

        logger.debug("Done writing field: " + src.keyword);
        return jsonField;
    }
}
