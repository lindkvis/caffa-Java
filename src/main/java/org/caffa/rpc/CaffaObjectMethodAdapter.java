package org.caffa.rpc;

import org.caffa.rpc.CaffaObject;
import org.caffa.rpc.CaffaObjectAdapter;

import io.grpc.ManagedChannel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;

public class CaffaObjectMethodAdapter extends CaffaObjectAdapter {

    private final CaffaObject self;

    public CaffaObjectMethodAdapter(CaffaObject self, ManagedChannel channel) {
        super(channel);
        this.self = self;
    }

    public CaffaObjectMethod deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        CaffaObjectMethod caffaObjectMethod = new CaffaObjectMethod(this.self);

        readFields(caffaObjectMethod, json);

        return caffaObjectMethod;
    }

    public JsonElement serialize(CaffaObjectMethod caffaObjectMethod, Type typeOfSrc, JsonSerializationContext context) {        
        final JsonObject jsonObject = new JsonObject();

        writeFields(caffaObjectMethod, jsonObject);

        return jsonObject;
    }

}
