package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;

import java.util.ArrayList;

public class CaffaObjectArrayField extends CaffaArrayField<CaffaObject> {
    public ArrayList<CaffaObject> value;

    public CaffaObjectArrayField() {
        this.value = new ArrayList<CaffaObject>();
    }

    public void dump() {
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type);
        for (CaffaObject object : this.value) {
            object.dump();
        }
    }
}
