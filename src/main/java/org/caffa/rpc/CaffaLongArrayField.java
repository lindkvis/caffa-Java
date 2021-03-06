package org.caffa.rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class CaffaLongArrayField extends CaffaArrayField<Long> {
    public CaffaLongArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Long.class);
    }

    @Override
    public List<Long> getChunk(GenericArray reply) {
        UInt64Array longArray = reply.getUint64S();
        return longArray.getDataList();
    }


    @Override
    public GenericArray createChunk(List<Long> values)
    {
        UInt64Array intValues = UInt64Array.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setUint64S(intValues).build();
    }

    @Override
    public JsonArray getJsonArray() {
        List<Long> values = getChunk(localArray);
        JsonArray array = new JsonArray();
        for (Long value : values) {
            array.add(value);
        }
        return array;
    }


    @Override
    protected List<Long> getListFromJsonArray(JsonArray jsonArray)
    {
        ArrayList<Long> values = new ArrayList<>();
        for (JsonElement element : jsonArray)
        {
            values.add(element.getAsLong());
        }
        return values;
    }

    @Override
    public CaffaField<ArrayList<Long>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaLongArrayField(owner, keyword);
    }
}
