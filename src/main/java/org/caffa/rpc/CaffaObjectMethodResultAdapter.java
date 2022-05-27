package org.caffa.rpc;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import io.grpc.ManagedChannel;

import java.lang.reflect.Type;

public class CaffaObjectMethodResultAdapter extends CaffaObjectAdapter {

    public CaffaObjectMethodResultAdapter(ManagedChannel channel, String sessionUuid) {
        super(channel, false, sessionUuid);
    }

    @Override
    public CaffaObjectMethodResult deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        assert (json.isJsonObject());
        final JsonObject object = json.getAsJsonObject();

        assert object.has("Class") && object.has("UUID");

        String classKeyword = object.get("Class").getAsString();
        String objectUuid = object.get("UUID").getAsString();

        CaffaObjectMethodResult methodResult = new CaffaObjectMethodResult(classKeyword, objectUuid, this.channel,
                this.sessionUuid);
        readFields(methodResult, object);

        return methodResult;
    }

    @Override
    public JsonElement serialize(CaffaObject methodResult, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        writeFields(methodResult, jsonObject, typeOfSrc, context);

        return jsonObject;
    }

}
