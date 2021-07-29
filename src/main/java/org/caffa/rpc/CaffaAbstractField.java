package org.caffa.rpc;

import java.util.logging.Logger;
import com.google.gson.JsonPrimitive;

public abstract class CaffaAbstractField {
    public String keyword;
    protected CaffaObject owner;
    protected static final Logger logger = Logger.getLogger(CaffaAbstractField.class.getName());

    CaffaAbstractField(CaffaObject owner, String keyword) {
        this.owner = owner;
        this.keyword = keyword;
    }

    public abstract void createAccessor(boolean grpc);

    public void dump() {
        System.out.println("keyword = " + keyword);
    }

    public abstract Class<?> type();
    
    public abstract String getJson();
    public abstract void setJson(String value);

    @SuppressWarnings("unchecked")
    public <T> CaffaField<T> cast()
    {
        return (CaffaField<T>) this;
    }

    public abstract CaffaAbstractField newInstance(CaffaObject owner, String keyword);
}
