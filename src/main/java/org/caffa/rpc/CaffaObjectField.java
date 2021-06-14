package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;
import io.grpc.ManagedChannel;

public class CaffaObjectField extends CaffaField<CaffaObject> {
    public CaffaObject value;

    public CaffaObjectField(CaffaObject owner, CaffaObject value, ManagedChannel channel) {
        super(owner, channel);
        this.value = value;
    }

    public CaffaObjectField(ManagedChannel channel) {
        super(channel);
    }

    public void dump() {
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type);
        this.value.dump();
    }
}
