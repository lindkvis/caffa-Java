package org.caffa.rpc;

import java.util.ArrayList;

public abstract class CaffaAbstractObjectField extends CaffaField<CaffaObject> {
    public CaffaAbstractObjectField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaObject.class);
    }

    public abstract ArrayList<CaffaObject> children();
}
