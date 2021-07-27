package org.caffa.rpc;

import java.util.List;

public class CaffaFloatArrayField extends CaffaArrayField<Float> {
    public CaffaFloatArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Float.class);
    }

    public List<Float> getChunk(GetterArrayReply reply) {
        FloatArray floatArray = reply.getFloats();
        return floatArray.getDataList();
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaFloatArrayField(owner, keyword);
    }
}
