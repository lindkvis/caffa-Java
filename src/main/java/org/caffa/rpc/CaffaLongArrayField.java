package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;

import java.util.Iterator;
import java.util.List;

public class CaffaLongArrayField extends CaffaArrayField<Long> {
    public CaffaLongArrayField(CaffaObject owner) {
        super(owner, Long.class);
    }

    public List<Long> getChunk(GetterArrayReply reply) {
        UInt64Array LongArray = reply.getUint64S();
        return LongArray.getDataList();
    }
}
