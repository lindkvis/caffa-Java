package org.caffa.rpc;

import java.util.List;

public class CaffaStringArrayField extends CaffaArrayField<String> {
    public CaffaStringArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, String.class);
    }

    public List<String> getChunk(GenericArray reply) {
        StringArray StringArray = reply.getStrings();
        return StringArray.getDataList();
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaStringArrayField(owner, keyword);
    }
}
