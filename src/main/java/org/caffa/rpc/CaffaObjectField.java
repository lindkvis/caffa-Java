package org.caffa.rpc;

import org.caffa.rpc.CaffaAbstractObjectField;
import org.caffa.rpc.CaffaObject;
import io.grpc.ManagedChannel;

import java.util.ArrayList;

public class CaffaObjectField extends CaffaAbstractObjectField {
    private CaffaObject value;

    public CaffaObjectField(CaffaObject owner, String keyword, CaffaObject value) {
        super(owner, keyword);
        this.value = value;
    }

    public CaffaObjectField(CaffaObject owner, String keyword) {
        super(owner, keyword);
    }

    public final ArrayList<CaffaObject> children() {
        ArrayList<CaffaObject> onlyChild = new ArrayList<CaffaObject>();
        onlyChild.add(this.value);
        return onlyChild;
    }

    public void set(CaffaObject object) {
        object.parentField = this;
        this.value = object;
    }

    public CaffaObject get() {
        return this.value;
    }

    public void dump() {
        System.out.println("CaffaObjectField {");
        super.dump();
        this.value.dump();
        System.out.println("}");
    }
}
