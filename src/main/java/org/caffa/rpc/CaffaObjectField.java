package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;
import io.grpc.ManagedChannel;

public class CaffaObjectField extends CaffaField<CaffaObject> {
    public CaffaObject value;

    public CaffaObjectField(CaffaObject owner, CaffaObject value) {
        super(owner, CaffaObject.class);
        this.value = value;
    }

    public CaffaObjectField(CaffaObject owner) {
        super(owner, CaffaObject.class);
    }

    public void dump() {
        System.out.println("CaffaObjectField {");
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type);
        this.value.dump();
        System.out.println("}");
    }
}
