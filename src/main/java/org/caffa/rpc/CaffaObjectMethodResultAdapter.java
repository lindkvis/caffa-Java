package org.caffa.rpc;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import io.grpc.ManagedChannel;

import java.lang.reflect.Type;

public class CaffaObjectMethodResultAdapter extends CaffaObjectAdapter {

    private final CaffaObject self;

    public CaffaObjectMethodResultAdapter(CaffaObject self, ManagedChannel channel) {
        super(channel, false, self.sessionUuid);
        this.self = self;
    }

    @Override
    public CaffaObjectMethodResult deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        CaffaObjectMethodResult methodResult = new CaffaObjectMethodResult(this.self);
        readFields(methodResult, json);

        return methodResult;
    }

    @Override
    public JsonElement serialize(CaffaObject methodResult, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        writeFields(methodResult, jsonObject, typeOfSrc, context);

        return jsonObject;
    }

}
