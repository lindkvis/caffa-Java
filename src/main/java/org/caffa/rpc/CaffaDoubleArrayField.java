package org.caffa.rpc;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class CaffaDoubleArrayField extends CaffaArrayField<Double> {
    public CaffaDoubleArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Double.class);
    }

    public List<Double> getChunk(GenericArray reply) {
        DoubleArray doubleArray = reply.getDoubles();
        return doubleArray.getDataList();
    }

    @Override
    public GenericArray createChunk(List<Double> values)
    {
        DoubleArray doubleValues = DoubleArray.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setDoubles(doubleValues).build();
    }

    @Override
    public JsonArray getJsonArray() {
        List<Double> values = getChunk(localArray);
        JsonArray array = new JsonArray();
        for (Double value : values) {
            array.add(value);
        }
        return array;
    }

    @Override
    public CaffaField<ArrayList<Double>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaDoubleArrayField(owner, keyword);
    }
}
