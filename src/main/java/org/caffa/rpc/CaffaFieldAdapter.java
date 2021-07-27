package org.caffa.rpc;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CaffaFieldAdapter implements JsonDeserializer<CaffaAbstractField> {
    private final CaffaObject object;
    private final Boolean remoteValues;

    public CaffaFieldAdapter(CaffaObject object) {
        super();

        this.object = object;
        this.remoteValues = true;
    }

    public CaffaFieldAdapter(CaffaObject object, Boolean remoteValues) {
        super();

        this.object = object;
        this.remoteValues = remoteValues;
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
        if (dataType.endsWith("[]")) {
            System.out.println("Creating array field " + keyword);
            dataType = dataType.substring(0, dataType.length() - 2);
            return CaffaFieldFactory.createArrayField(this.object, keyword, dataType);
        }
        System.out.println("Creating scalar field " + keyword);
        return CaffaFieldFactory.createField(this.object, keyword, dataType);
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
