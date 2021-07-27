package org.caffa.rpc;

import java.util.List;

public class CaffaLongArrayField extends CaffaArrayField<Long> {
    public CaffaLongArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Long.class);
    }

    public List<Long> getChunk(GetterArrayReply reply) {
        UInt64Array LongArray = reply.getUint64S();
        return LongArray.getDataList();
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaLongArrayField(owner, keyword);
    }
}
