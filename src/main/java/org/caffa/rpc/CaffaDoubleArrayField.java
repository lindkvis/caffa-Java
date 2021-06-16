package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;

import java.util.Iterator;
import java.util.List;

public class CaffaDoubleArrayField extends CaffaArrayField<Double> {
    public CaffaDoubleArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Double.class);
    }

    public List<Double> getChunk(GetterArrayReply reply) {
        DoubleArray doubleArray = reply.getDoubles();
        return doubleArray.getDataList();
    }
}
