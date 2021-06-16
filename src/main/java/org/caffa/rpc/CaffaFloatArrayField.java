package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;

import java.util.Iterator;
import java.util.List;

public class CaffaFloatArrayField extends CaffaArrayField<Float> {
    public CaffaFloatArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Float.class);
    }

    public List<Float> getChunk(GetterArrayReply reply) {
        FloatArray FloatArray = reply.getFloats();
        return FloatArray.getDataList();
    }
}
