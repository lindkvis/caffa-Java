package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;
import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaFieldFactory;
import org.caffa.rpc.CaffaObjectAdapter;
import org.caffa.rpc.CaffaObjectField;
import org.caffa.rpc.CaffaObjectArrayField;

import io.grpc.ManagedChannel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class CaffaFieldAdapter implements JsonDeserializer<CaffaAbstractField> {
    private final CaffaObject object;

    public CaffaFieldAdapter(CaffaObject object) {
        super();

        this.object = object;
    }

    public CaffaAbstractField createField(String keyword, String dataType, JsonElement valueElement) {
        if (dataType.equals("object")) {
            CaffaObject caffaObject = new GsonBuilder()
                    .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.object.channel)).create()
                    .fromJson(valueElement, CaffaObject.class);
            return new CaffaObjectField(this.object, keyword, caffaObject);

        } else if (dataType.equals("object[]")) {
            CaffaObjectArrayField caffaField = new CaffaObjectArrayField(this.object, keyword);
            if (valueElement.isJsonArray()) {
                for (JsonElement arrayElement : valueElement.getAsJsonArray()) {
                    CaffaObject caffaObject = new GsonBuilder()
                            .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.object.channel))
                            .create().fromJson(arrayElement, CaffaObject.class);
                    caffaField.add(caffaObject);
                }
            }
            return caffaField;
        }
        return CaffaFieldFactory.create(this.object, keyword, dataType);
    }

    public CaffaAbstractField deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject object = json.getAsJsonObject();

        String keyword = object.get("keyword").getAsString();
        String dataType = object.get("type").getAsString();

        JsonElement valueElement = object.get("value");
        CaffaAbstractField abstractCaffaField = createField(keyword, dataType, valueElement);

        return abstractCaffaField;
    }

}
