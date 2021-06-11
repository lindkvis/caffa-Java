package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;

public class CaffaObjectField extends CaffaField<CaffaObject> {
    public CaffaObject value;

    public CaffaObjectField(CaffaObject value) {
        this.value = value;
    }

    public CaffaObjectField() {

    }

    public void dump() {
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type);
        this.value.dump();
    }
}
