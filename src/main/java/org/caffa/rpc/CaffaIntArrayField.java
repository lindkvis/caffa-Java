package org.caffa.rpc;

import java.util.List;

public class CaffaIntArrayField extends CaffaArrayField<Integer> {
    public CaffaIntArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Integer.class);
    }

    public List<Integer> getChunk(GenericArray reply) {
        IntArray IntegerArray = reply.getInts();
        return IntegerArray.getDataList();
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaIntArrayField(owner, keyword);
    }
}
