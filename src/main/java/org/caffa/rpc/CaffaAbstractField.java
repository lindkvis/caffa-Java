package org.caffa.rpc;

import java.util.logging.Logger;

public abstract class CaffaAbstractField {
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

    public abstract Class<?> type();

    @SuppressWarnings("unchecked")
    public <T> CaffaField<T> cast()
    {
        return (CaffaField<T>) this;
    }

    public abstract CaffaAbstractField newInstance(CaffaObject owner, String keyword);
}
