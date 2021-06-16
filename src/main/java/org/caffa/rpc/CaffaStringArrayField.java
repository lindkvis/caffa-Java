package org.caffa.rpc;

import org.caffa.rpc.CaffaArrayField;

import java.util.Iterator;
import java.util.List;

public class CaffaStringArrayField extends CaffaArrayField<String> {
    public CaffaStringArrayField(CaffaObject owner) {
        super(owner, String.class);
    }

    public List<String> getChunk(GetterArrayReply reply) {
        StringArray StringArray = reply.getStrings();
        return StringArray.getDataList();
    }
}
