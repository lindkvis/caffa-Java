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

import java.lang.reflect.Type;

public class CaffaFieldAdapter implements JsonDeserializer<CaffaField<?>>, JsonSerializer<CaffaField<?>> {
    private static final Logger logger = LoggerFactory.getLogger(CaffaFieldAdapter.class);

    private final CaffaObject object;
    private final String keyword;
    private final JsonObject schema;

    public static final JsonObject NULL_PLACEHOLDER = new JsonObject();

    public CaffaFieldAdapter(CaffaObject object, String keyword, JsonObject schema) {
        super();

        this.object = object;
        this.keyword = keyword;
        this.schema = schema;
    }

    public CaffaField<?> createField(String keyword, String dataType) {
        assert this.object != null;

        if (dataType.endsWith("[][]")) {
            // TODO: Array of array fields are not supported in Caffa-Java yet, but we
            // ignore them silently.
            return null;
        }
        logger.debug("Creating field " + keyword + " of type " + dataType);
        CaffaField<?> field = CaffaFieldFactory.createField(this.object, keyword, dataType);
        assert field != null;
        field.setSchema(this.schema);

        return field;
    }

    private String getDataType(JsonObject jsonField) {
        if (jsonField.has("enum")) {            
            return "enum" + jsonField.get("enum").toString();
        } else if (jsonField.has("$ref")) {
            return "object";
        } else {
            assert jsonField.has("type");
            String type = jsonField.get("type").getAsString();
            if (type.equals("array")) {
                assert jsonField.has("items");
                JsonObject itemObject = jsonField.get("items").getAsJsonObject();
                return getDataType(itemObject) + "[]";
            }
            return type;
        }
    }

    public CaffaField<?> deserialize(JsonElement value, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        String dataType = getDataType(this.schema);
        logger.debug("Creating field with type '" + dataType + "'");
        
        CaffaField<?> field = createField(this.keyword, dataType);
        if (field.isLocalField() && !isNullPlaceHolder(value)) {
            field.setJson(value.toString());
        }
        
        return field;
    }

    @Override
    public JsonElement serialize(CaffaField<?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonField = new JsonObject();

        if (src.isLocalField()) {
            return JsonParser.parseString(src.getJson());
        }
        return jsonField;
    }

    private boolean isNullPlaceHolder(JsonElement element) {
        return element.toString().equals(NULL_PLACEHOLDER.toString());
    }
}
