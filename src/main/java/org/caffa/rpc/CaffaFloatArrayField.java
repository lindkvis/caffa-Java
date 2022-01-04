package org.caffa.rpc;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public class CaffaFloatArrayField extends CaffaArrayField<Float> {
    public CaffaFloatArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Float.class);
    }

    @Override
    public List<Float> getChunk(GenericArray reply) {
        FloatArray floatArray = reply.getFloats();
        return floatArray.getDataList();
    }

    @Override
    public GenericArray createChunk(List<Float> values)
    {
        FloatArray floatValues = FloatArray.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setFloats(floatValues).build();
    }

    @Override
    public JsonArray getJsonArray() {
        List<Float> values = getChunk(localArray);
        JsonArray array = new JsonArray();
        for (Float value : values) {
            array.add(value);
        }
        return array;
    }

    @Override
    public CaffaField<ArrayList<Float>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaFloatArrayField(owner, keyword);
    }

    @Override
    public void dump() {
        System.out.print("CaffaFloatArrayField::");
        if (this.localArray != null)
        {
            System.out.print("local");
        }
        else{
            System.out.print("grpc");
        }      
        System.out.println(" {");
        System.out.println("keyword = " + this.keyword);
        if (this.localArray != null)
        {
            System.out.println("value = " + this.localArray);
        }
        System.out.println("}");  
    }
}
