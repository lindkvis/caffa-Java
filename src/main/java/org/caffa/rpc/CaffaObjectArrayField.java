package org.caffa.rpc;

import java.util.ArrayList;

public class CaffaObjectArrayField extends CaffaAbstractObjectField {
    private ArrayList<CaffaObject> values;

    public CaffaObjectArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword);
        this.values = new ArrayList<CaffaObject>();
    }

    public final ArrayList<CaffaObject> children() {
        return values();
    }

    void add(CaffaObject object) {
        object.parentField = this;
        this.values.add(object);
    }

    public final ArrayList<CaffaObject> values() {
        return this.values;
    }

    public void dump() {
        System.out.println("CaffaObjectArrayField {");
        super.dump();
        for (CaffaObject object : this.values) {
            object.dump();
        }
        System.out.println("}");
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaObjectArrayField(owner, keyword);
    }
}
