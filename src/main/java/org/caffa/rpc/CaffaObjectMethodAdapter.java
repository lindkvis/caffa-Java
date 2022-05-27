package org.caffa.rpc;

import java.lang.reflect.Type;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import io.grpc.ManagedChannel;

public class CaffaObjectMethodAdapter extends CaffaObjectAdapter {

    private final CaffaObject self;
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectMethodAdapter.class);

    public CaffaObjectMethodAdapter(CaffaObject self) {
        super(self.channel(), false, self.sessionUuid());
        this.self = self;
    }

    @Override
    public CaffaObjectMethod deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        assert json.isJsonObject();
        final JsonObject object = json.getAsJsonObject();

        assert object.has("Class") && object.has("UUID");

        String classKeyword = object.get("Class").getAsString();
        String objectUuid = object.get("UUID").getAsString();

        CaffaObjectMethod caffaObjectMethod = new CaffaObjectMethod(classKeyword, objectUuid, this.self);
        readFields(caffaObjectMethod, object);

        return caffaObjectMethod;
    }

    @Override
    public JsonElement serialize(CaffaObject caffaObjectMethod, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        writeFields(caffaObjectMethod, jsonObject, typeOfSrc, context);

        return jsonObject;
    }

}
