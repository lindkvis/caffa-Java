package org.caffa.rpc;

import org.caffa.rpc.FieldAccessGrpc;
import org.caffa.rpc.CaffaObject;

import io.grpc.ManagedChannel;

public class CaffaAbstractField {
    public String keyword;
    public String type;

    protected CaffaObject owner;
    protected final ManagedChannel channel;
    protected final FieldAccessGrpc.FieldAccessBlockingStub fieldStub;

    CaffaAbstractField(CaffaObject owner, ManagedChannel channel) {
        this.owner = owner;
        this.channel = channel;
        this.fieldStub = FieldAccessGrpc.newBlockingStub(channel);
    }

    public void dump() {
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type);
    }
}
