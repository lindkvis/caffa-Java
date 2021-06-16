package org.caffa.rpc;

import org.caffa.rpc.FieldAccessGrpc;
import org.caffa.rpc.CaffaObject;

import io.grpc.ManagedChannel;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CaffaAbstractField {
    public String keyword;
    public String type;

    protected CaffaObject owner;
    protected final FieldAccessGrpc.FieldAccessBlockingStub fieldStub;
    protected static final Logger logger = Logger.getLogger(CaffaAbstractField.class.getName());

    CaffaAbstractField(CaffaObject owner) {
        this.owner = owner;
        this.fieldStub = FieldAccessGrpc.newBlockingStub(this.owner.channel);
    }

    public void dump() {
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type);
    }
}
