package org.caffa.rpc;

import java.util.List;

public class CaffaBooleanArrayField extends CaffaArrayField<Boolean> {
    public CaffaBooleanArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Boolean.class);
    }

    public List<Boolean> getChunk(GenericArray reply) {
        BoolArray boolArray = reply.getBools();
        return boolArray.getDataList();
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaIntArrayField(owner, keyword);
    }
}
