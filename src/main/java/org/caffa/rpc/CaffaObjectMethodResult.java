package org.caffa.rpc;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CaffaObjectMethodResult{
    private final String jsonString;
    public CaffaObjectMethodResult(String jsonString) {
        this.jsonString = jsonString;
    }

    public <T> T get(Class<T> valueType) {
        JsonElement element = JsonParser.parseString(jsonString);
        assert element.isJsonObject();
        JsonObject resultObject = element.getAsJsonObject();
        JsonElement value = resultObject.get("value");

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(null, ""));
        builder.registerTypeAdapter(CaffaAppEnum.class, new CaffaAppEnumAdapter());
        return builder.create().fromJson(value.toString(), valueType);
    }

}
