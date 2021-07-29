package org.caffa.rpc;

import com.google.gson.Gson;

public abstract class CaffaFieldAccessor {
    protected final Class<?> dataType;

    protected CaffaFieldAccessor(Class<?> dataType)
    {
        this.dataType = dataType;
    }

    public abstract String getJson(CaffaAbstractField field);
    public abstract void setJson(CaffaAbstractField field, String value);
    public abstract CaffaFieldAccessor clone();

    public void dump()
    {
        System.out.print("<" + dataType.toString() + ">");
    }

    public <T> T get(CaffaAbstractField field, Class<T> dataType) {
        if (dataType == this.dataType)
        {
            String json = getJson(field);
            return new Gson().fromJson(json, dataType);
        }
        return null;
    }

    public <T> void set(CaffaAbstractField field, T value) {
        setJson(field, new Gson().toJson(value));
    }
    
}
