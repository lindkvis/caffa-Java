package org.caffa.rpc;

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
    public GenericArray createChunk(List<Long> values) {
        UInt64Array valueArray = UInt64Array.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setUint64S(valueArray).build();
    }

    @Override
    public CaffaField<ArrayList<Long>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaLongArrayField(owner, keyword);
    }
}
