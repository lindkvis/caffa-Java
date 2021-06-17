package org.caffa.rpc;

import org.caffa.rpc.FieldAccessGrpc;
import org.caffa.rpc.CaffaFieldFactory;
import org.caffa.rpc.CaffaObject;

import io.grpc.ManagedChannel;

import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class CaffaAbstractField {
    public String keyword;
    protected CaffaObject owner;
    protected FieldAccessGrpc.FieldAccessBlockingStub fieldStub = null;
    protected static final Logger logger = Logger.getLogger(CaffaAbstractField.class.getName());

    CaffaAbstractField(CaffaObject owner, String keyword) {
        this.owner = owner;
        this.keyword = keyword;
        if (this.owner != null) {
            this.fieldStub = FieldAccessGrpc.newBlockingStub(this.owner.channel);
        }
    }

    public void dump() {
        System.out.println("keyword = " + keyword);
    }

    abstract public Class<?> getType();

    abstract public CaffaAbstractField newInstance(CaffaObject owner, String keyword);
}
