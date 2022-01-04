package org.caffa.rpc;

import com.google.gson.JsonArray;

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
    public CaffaField<ArrayList<String>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaStringArrayField(owner, keyword);
    }
}
