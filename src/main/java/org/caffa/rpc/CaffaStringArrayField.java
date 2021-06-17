package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;

import java.util.Iterator;
import java.util.List;

public class CaffaStringArrayField extends CaffaArrayField<String> {
    public CaffaStringArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, String.class);
    }

    public List<String> getChunk(GetterArrayReply reply) {
        StringArray StringArray = reply.getStrings();
        return StringArray.getDataList();
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaStringArrayField(owner, keyword);
    }
}
