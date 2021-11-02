package org.caffa.rpc;

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
    public GenericArray createChunk(List<Integer> values) {
        IntArray valueArray = IntArray.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setInts(valueArray).build();
    }

    @Override
    public CaffaField<ArrayList<Integer>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaIntArrayField(owner, keyword);
    }
}
