package org.caffa.rpc;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import io.grpc.ManagedChannel;

public class CaffaObjectMethodAdapter extends CaffaObjectAdapter {

    private final CaffaObject self;

    public CaffaObjectMethodAdapter(CaffaObject self, ManagedChannel channel) {
        super(channel, false);
        this.self = self;
    }

    @Override
    public CaffaObjectMethod deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        CaffaObjectMethod caffaObjectMethod = new CaffaObjectMethod(this.self);
        readFields(caffaObjectMethod, json);

        return caffaObjectMethod;
    }

    @Override
    public JsonElement serialize(CaffaObject caffaObjectMethod, Type typeOfSrc, JsonSerializationContext context) {        
        final JsonObject jsonObject = new JsonObject();

        writeFields(caffaObjectMethod, jsonObject, typeOfSrc, context);

        return jsonObject;
    }


}
