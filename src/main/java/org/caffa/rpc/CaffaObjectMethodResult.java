package org.caffa.rpc;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.grpc.ManagedChannel;

public class CaffaObjectMethodResult{
    private final ManagedChannel channel;
    private final String sessionUuid;
    private final String jsonString;
    public CaffaObjectMethodResult(ManagedChannel channel, String sessionUuid, String jsonString) {
        this.channel = channel;
        this.sessionUuid = sessionUuid;
        this.jsonString = jsonString;
    }

    public <T> T get(Class<T> valueType) {
        JsonElement element = JsonParser.parseString(jsonString);
        assert element.isJsonObject();
        JsonObject resultObject = element.getAsJsonObject();
        JsonElement value = resultObject.get("value");

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.channel, this.sessionUuid, true));
        builder.registerTypeAdapter(CaffaAppEnum.class, new CaffaAppEnumAdapter());
        return builder.create().fromJson(value.toString(), valueType);
    }

}
