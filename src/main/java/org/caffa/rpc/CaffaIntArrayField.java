package org.caffa.rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class CaffaIntArrayField extends CaffaArrayField<Integer> {
    public CaffaIntArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Integer.class);
    }

    @Override
    public List<Integer> getChunk(GenericArray reply) {
        if (this.getUnsigned())
        {
            UIntArray integerArray = reply.getUints();
            return integerArray.getDataList();
    
        }
        else
        {
            IntArray integerArray = reply.getInts();
            return integerArray.getDataList();
        }
    }

    @Override
    public GenericArray createChunk(List<Integer> values)
    {
        
        if (this.getUnsigned())
        {
            UIntArray uintValues = UIntArray.newBuilder().addAllData(values).build();
            return GenericArray.newBuilder().setUints(uintValues).build();
        }
        else
        {
            IntArray intValues = IntArray.newBuilder().addAllData(values).build();
            return GenericArray.newBuilder().setInts(intValues).build();
        }
        
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
    protected List<Integer> getListFromJsonArray(JsonArray jsonArray)
    {
        ArrayList<Integer> values = new ArrayList<>();
        for (JsonElement element : jsonArray)
        {
            values.add(element.getAsInt());
        }
        return values;
    }

    @Override
    public CaffaField<ArrayList<Integer>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaIntArrayField(owner, keyword);
    }
}
