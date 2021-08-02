package org.caffa.rpc;

import java.util.ArrayList;
import java.util.List;

public class CaffaStringArrayField extends CaffaArrayField<String> {
    public CaffaStringArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, String.class);
    }

    @Override
    public List<String> getChunk(GenericArray reply) {
        StringArray stringArray = reply.getStrings();
        return stringArray.getDataList();
    }

    @Override
    public CaffaField<ArrayList<String>> newInstance(CaffaObject owner, String keyword) {
        return new CaffaStringArrayField(owner, keyword);
    }
}
