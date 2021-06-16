package org.caffa.rpc;

import org.caffa.rpc.CaffaAbstractObjectField;
import org.caffa.rpc.CaffaObject;

import io.grpc.ManagedChannel;

import java.util.ArrayList;

public class CaffaObjectArrayField extends CaffaAbstractObjectField {
    private ArrayList<CaffaObject> values;

    public CaffaObjectArrayField(CaffaObject owner) {
        super(owner);
        this.values = new ArrayList<CaffaObject>();
    }

    public final ArrayList<CaffaObject> children()
    {
        return values();
    }

    void add(CaffaObject object)
    {
        object.parentField = this;
        this.values.add(object);
    }

    public final ArrayList<CaffaObject> values()
    {
        return this.values;
    }

    public void dump() {
        System.out.println("CaffaObjectArrayField {");
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type);
        for (CaffaObject object : this.values) {
            object.dump();
        }
        System.out.println("}");
    }
}
