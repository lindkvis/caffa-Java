package org.caffa.rpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class CaffaBooleanArrayField extends CaffaArrayField<Boolean> {
    public CaffaBooleanArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Boolean.class);
    }

    @Override
    public List<Boolean> getChunk(GenericArray reply) {
        BoolArray boolArray = reply.getBools();
        return boolArray.getDataList();
    }

    @Override
    public GenericArray createChunk(List<Boolean> values)
    {
        BoolArray boolValues = BoolArray.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setBools(boolValues).build();
    }

    @Override
    public JsonArray getJsonArray() {
        List<Boolean> values = getChunk(localArray);
        JsonArray array = new JsonArray();
        for (Boolean value : values) {
            array.add(value);
        }
        return array;
    }

    @Override
    protected List<Boolean> getListFromJsonArray(JsonArray jsonArray)
    {
        ArrayList<Boolean> values = new ArrayList<>();
        for (JsonElement element : jsonArray)
        {
            values.add(element.getAsBoolean());
        }
        return values;
    }

    @Override
    public CaffaField<ArrayList<Boolean>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaBooleanArrayField(owner, keyword);
    }
}
