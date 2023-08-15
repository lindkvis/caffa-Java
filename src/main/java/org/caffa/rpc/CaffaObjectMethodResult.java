package org.caffa.rpc;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CaffaObjectMethodResult{
    private final RestClient client;
    private final String jsonValue;
    private final String jsonSchema;
    
    public CaffaObjectMethodResult(RestClient client, String jsonValue, String jsonSchema) {
        this.client = client;
        this.jsonValue = jsonValue;
        this.jsonSchema = jsonSchema;
    }

    public <T> T get(Class<T> valueType) {
        JsonElement schema = JsonParser.parseString(jsonSchema);
        JsonObject schemaObject = null;
        if (schema.isJsonObject()) {
            schemaObject = schema.getAsJsonObject();
        }

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(client, schemaObject, true));
        builder.registerTypeAdapter(CaffaAppEnum.class, new CaffaAppEnumAdapter());
        return builder.create().fromJson(this.jsonValue, valueType);
    }

}
