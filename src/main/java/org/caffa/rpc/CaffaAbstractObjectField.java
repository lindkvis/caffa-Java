package org.caffa.rpc;

import java.util.List;

public abstract class CaffaAbstractObjectField extends CaffaField<CaffaObject> {
    public CaffaAbstractObjectField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaObject.class);
    }

    public abstract List<CaffaObject> children();
}
