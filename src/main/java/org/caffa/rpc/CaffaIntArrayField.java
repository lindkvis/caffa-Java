package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;

import java.util.Iterator;
import java.util.List;

public class CaffaIntArrayField extends CaffaArrayField<Integer> {
    public CaffaIntArrayField(CaffaObject owner) {
        super(owner, Integer.class);
    }

    public List<Integer> getChunk(GetterArrayReply reply) {
        IntArray IntegerArray = reply.getInts();
        return IntegerArray.getDataList();
    }
}
