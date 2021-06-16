package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;
import io.grpc.ManagedChannel;

import java.util.ArrayList;

abstract public class CaffaAbstractObjectField extends CaffaField<CaffaObject> {
    public CaffaAbstractObjectField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaObject.class);
    }

    abstract public ArrayList<CaffaObject> children();
}
