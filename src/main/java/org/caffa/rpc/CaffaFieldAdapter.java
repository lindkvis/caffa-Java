package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;
import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObjectField;
import org.caffa.rpc.CaffaObjectArrayField;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CaffaFieldAdapter implements JsonDeserializer<CaffaAbstractField> {
    public CaffaAbstractField deserializeScalarValue(String dataType, JsonElement valueElement,
            JsonDeserializationContext context) {
        if (dataType.startsWith("object:")) {
            return new CaffaObjectField(context.deserialize(valueElement, CaffaObject.class));
        }
        switch (dataType) {
            case "int": {
                return new CaffaField<Integer>();
            }
            case "string": {
                return new CaffaField<String>();
            }
            case "double": {
                return new CaffaField<Double>();
            }
            case "float": {
                return new CaffaField<Float>();
            }
            case "bool": {
                return new CaffaField<Boolean>();
            }

        }
        System.err.println("Could not create field for data type: " + dataType);
        return null;
    }

    public CaffaAbstractField deserializeArrayValue(String dataType, JsonElement valueElement,
            JsonDeserializationContext context) {

        if (dataType.startsWith("object:")) {
            CaffaObjectArrayField caffaField = new CaffaObjectArrayField();
            if (valueElement.isJsonArray()) {
                for (JsonElement arrayElement : valueElement.getAsJsonArray()) {
                    caffaField.value.add(context.deserialize(arrayElement, CaffaObject.class));
                }
            }
            return caffaField;
        }

        switch (dataType) {
            case "int": {
                return new CaffaArrayField<Integer>();
            }
            case "string": {
                return new CaffaArrayField<String>();

            }
            case "double": {
                return new CaffaArrayField<Double>();
            }
            case "float": {
                return new CaffaArrayField<Float>();

            }
            case "bool": {
                return new CaffaArrayField<Boolean>();
            }
        }
        System.err.println("Could not create array field for data type: " + dataType);
        return null;
    }

    public CaffaAbstractField deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        final JsonObject object = json.getAsJsonObject();

        CaffaAbstractField abstractCaffaField = null;
        String keyword = object.get("keyword").getAsString();
        String dataType = object.get("type").getAsString();
        JsonElement valueElement = object.get("value");

        System.out.println("Trying to deserialize " + keyword);

        if (dataType.endsWith("[]")) {
            dataType = dataType.substring(0, dataType.length() - 2);
            abstractCaffaField = deserializeArrayValue(dataType, valueElement, context);
        } else {
            abstractCaffaField = deserializeScalarValue(dataType, valueElement, context);
        }
        if (abstractCaffaField != null) {
            abstractCaffaField.keyword = keyword;
            abstractCaffaField.type = dataType;
        }
        return abstractCaffaField;
    }

}
