package org.caffa.rpc;

import io.grpc.ManagedChannel;

public abstract class CaffaAbstractField {
    protected CaffaObject owner;
    public final String keyword;

    protected ManagedChannel channel = null;
    protected FieldAccessGrpc.FieldAccessBlockingStub fieldBlockingStub = null;
    protected FieldAccessGrpc.FieldAccessStub fieldStub = null;

    protected boolean isLocalField = false;


    public CaffaAbstractField(CaffaObject owner, String keyword) {
        this.owner = owner;
        this.keyword = keyword;
    }

    public boolean isLocalField() {
        return this.isLocalField;
    }

    public void createGrpcAccessor(ManagedChannel channel) {
        assert this.owner != null;
        this.channel = channel;
        this.fieldBlockingStub = FieldAccessGrpc.newBlockingStub(channel);
        this.fieldStub = FieldAccessGrpc.newStub(channel);
    }

    public void setIsLocalField(boolean isLocalField) {
        this.isLocalField = isLocalField;
    }

    public abstract String getJson();

    public abstract void setJson(String value);

    public abstract String typeString();

    public String dump() {
        return dump("");
    }

    public abstract String dump(String prefix);

}
