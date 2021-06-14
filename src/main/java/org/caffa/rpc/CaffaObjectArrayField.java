package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;

import io.grpc.ManagedChannel;

import java.util.ArrayList;

public class CaffaObjectArrayField extends CaffaArrayField<CaffaObject> {
    public ArrayList<CaffaObject> value;

    public CaffaObjectArrayField(CaffaObject owner, ManagedChannel channel) {
        super(owner, channel);
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
