package org.caffa.rpc;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CaffaFieldAdapter implements JsonDeserializer<CaffaAbstractField>, JsonSerializer<CaffaAbstractField> {
    private final CaffaObject object;
    private final boolean grpc;

    public CaffaFieldAdapter(CaffaObject object) {
        super();

        this.object = object;
        this.grpc = true;
    }

    public CaffaFieldAdapter(CaffaObject object, boolean grpc) {
        super();

        this.object = object;
        this.grpc = grpc;
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
            CaffaAbstractField field = CaffaFieldFactory.createArrayField(this.object, keyword, dataType);
            field.createAccessor(this.grpc);
            if (!this.grpc)
            {
                field.setJson(valueElement.toString());
            }
    
            return field;
        }
        System.out.println("Creating scalar field " + keyword);
        CaffaAbstractField field = CaffaFieldFactory.createField(this.object, keyword, dataType);
        field.createAccessor(this.grpc);
        if (!this.grpc)
        {
            field.setJson(valueElement.toString());
        }
        return field;
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
    
    @Override
    public JsonElement serialize(CaffaAbstractField src, Type typeOfSrc, JsonSerializationContext context) {
        System.out.println("Writing field: " + src.keyword);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("keyword", src.keyword);
        jsonObject.addProperty("type", CaffaFieldFactory.dataTypes.inverse().get(src.type()));
        if (!this.grpc)
        {            
            JsonElement element = JsonParser.parseString(src.getJson());
            jsonObject.add("value", element);
        }
        return jsonObject;
    }
}
