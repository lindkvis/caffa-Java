package org.caffa.rpc;

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
