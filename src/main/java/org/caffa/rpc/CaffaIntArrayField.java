package org.caffa.rpc;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class CaffaIntArrayField extends CaffaArrayField<Integer> {
    public CaffaIntArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Integer.class);
    }

    @Override
    public List<Integer> getChunk(GenericArray reply) {
        IntArray integerArray = reply.getInts();
        return integerArray.getDataList();
    }

    @Override
    public GenericArray createChunk(List<Integer> values)
    {
        IntArray intValues = IntArray.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setInts(intValues).build();
    }

    @Override
    public JsonArray getJsonArray() {
        List<Integer> values = getChunk(localArray);
        JsonArray array = new JsonArray();
        for (Integer value : values) {
            array.add(value);
        }
        return array;
    }

    @Override
    public CaffaField<ArrayList<Integer>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaIntArrayField(owner, keyword);
    }
}
