package org.caffa.rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class CaffaStringArrayField extends CaffaArrayField<String> {
    public CaffaStringArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, String.class);
    }

    @Override
    public List<String> getChunk(GenericArray reply) {
        StringArray stringArray = reply.getStrings();
        return stringArray.getDataList();
    }

    @Override
    public GenericArray createChunk(List<String> values)
    {
        StringArray stringValues = StringArray.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setStrings(stringValues).build();
    }

    @Override
    public JsonArray getJsonArray() {
        List<String> values = getChunk(localArray);
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }


    @Override
    protected List<String> getListFromJsonArray(JsonArray jsonArray)
    {
        ArrayList<String> values = new ArrayList<>();
        for (JsonElement element : jsonArray)
        {
            values.add(element.getAsString());
        }
        return values;
    }
    
    @Override
    public CaffaField<ArrayList<String>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaStringArrayField(owner, keyword);
    }
}
