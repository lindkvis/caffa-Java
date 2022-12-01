package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import io.grpc.ManagedChannel;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

public abstract class CaffaAbstractField {
    protected CaffaObject owner;
    public final String keyword;

    protected ManagedChannel channel = null;
    protected FieldAccessGrpc.FieldAccessBlockingStub fieldBlockingStub = null;
    protected FieldAccessGrpc.FieldAccessStub fieldStub = null;

    private static Logger logger = LoggerFactory.getLogger(CaffaAbstractField.class);

    public CaffaAbstractField(CaffaObject owner, String keyword) {
        this.owner = owner;
        this.keyword = keyword;
    }

    public boolean isLocalField() {
        return !isRemoteField();
    }

    public boolean isRemoteField() {
        return this.channel != null && this.fieldStub != null && this.fieldBlockingStub != null;
    }

    public void createGrpcAccessor(ManagedChannel channel) {
        assert this.owner != null;
        this.channel = channel;
        this.fieldBlockingStub = FieldAccessGrpc.newBlockingStub(channel);
        this.fieldStub = FieldAccessGrpc.newStub(channel);
    }

    public abstract String getJson();

    public abstract void setJson(String value);

    public abstract String typeString();

    public abstract void dump();

}
