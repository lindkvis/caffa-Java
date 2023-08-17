package org.caffa.rpc;

import java.lang.reflect.Type;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;


public class CaffaObjectMethodAdapter extends CaffaObjectAdapter {

    private final CaffaObject self;
    private final String methodName;
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectMethodAdapter.class);

    public CaffaObjectMethodAdapter(CaffaObject self, String methodName) {
        super(self.getClient(), null, true);
        this.self = self;
        this.methodName = methodName;
    }

    @Override
    public CaffaObjectMethod deserialize(JsonElement schema, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        CaffaObjectMethod caffaObjectMethod = new CaffaObjectMethod(this.methodName, this.self, this.client);

        final JsonObject schemaObject = schema.getAsJsonObject();
        if (schemaObject.has("properties")) {
            final JsonObject properties = schemaObject.get("properties").getAsJsonObject();
            if (properties.has("labelledArguments")) {
                final JsonObject labelledArguments = properties.get("labelledArguments").getAsJsonObject();
                assert labelledArguments.has("properties");
                final JsonObject labelledArgProperties = labelledArguments.get("properties").getAsJsonObject();
                for (String key : labelledArgProperties.keySet()) {
                    readField(caffaObjectMethod, key, labelledArgProperties.get(key).getAsJsonObject(),
                            CaffaFieldAdapter.NULL_PLACEHOLDER);
                }
            }
            if (properties.has("returns")) {
                caffaObjectMethod.setResultSchema(properties.get("returns").toString());
            }

        }
        return caffaObjectMethod;
    }

    @Override
    public JsonElement serialize(CaffaObject caffaObjectMethod, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        JsonObject labelledArguments = new JsonObject();

        writeFields(caffaObjectMethod, labelledArguments, typeOfSrc, context);
        
        jsonObject.add("labelledArguments", labelledArguments);;

        return jsonObject;
    }
}
