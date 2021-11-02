package org.caffa.rpc;

import java.util.ArrayList;
import java.util.List;

public class CaffaBooleanArrayField extends CaffaArrayField<Boolean> {
    public CaffaBooleanArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, Boolean.class);
    }

    @Override
    public List<Boolean> getChunk(GenericArray reply) {
        BoolArray boolArray = reply.getBools();
        return boolArray.getDataList();
    }

    @Override
    public GenericArray createChunk(List<Boolean> values) {
        BoolArray valueArray = BoolArray.newBuilder().addAllData(values).build();
        return GenericArray.newBuilder().setBools(valueArray).build();
    }

    @Override
    public CaffaField<ArrayList<Boolean>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaBooleanArrayField(owner, keyword);
    }
}
