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
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectMethodAdapter.class);

    public CaffaObjectMethodAdapter(CaffaObject self) {
        super();
        this.self = self;
    }

    @Override
    public CaffaObjectMethod deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        assert json.isJsonObject();
        final JsonObject object = json.getAsJsonObject();

        assert object.has("keyword");
        assert object.get("keyword").isJsonPrimitive();

        String classKeyword = object.get("keyword").getAsString();

        CaffaObjectMethod caffaObjectMethod = new CaffaObjectMethod(classKeyword, this.self);

        if  (object.has("arguments")) {
            JsonElement arguments = object.get("arguments");
            if (arguments.isJsonArray()) {
                JsonArray argumentArray = arguments.getAsJsonArray();
                readFields(caffaObjectMethod, argumentArray);
            }
        }

        return caffaObjectMethod;
    }

    @Override
    public JsonElement serialize(CaffaObject caffaObjectMethod, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("keyword", caffaObjectMethod.keyword);
        JsonArray jsonArray = new JsonArray();

        writeFields(caffaObjectMethod, jsonArray, typeOfSrc, context);
        
        jsonObject.add("arguments", jsonArray);

        return jsonObject;
    }
}
