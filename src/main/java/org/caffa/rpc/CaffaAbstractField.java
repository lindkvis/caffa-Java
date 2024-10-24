package org.caffa.rpc;

import com.google.gson.JsonObject;

public abstract class CaffaAbstractField {
    protected final CaffaObject owner;
    protected final String keyword;
    protected JsonObject schema = null;

    protected boolean isLocalField = false;


    public CaffaAbstractField(CaffaObject owner, String keyword) {
        this.owner = owner;
        this.keyword = keyword;
    }

    public boolean isLocalField() {
        return this.isLocalField;
    }

    public void setIsLocalField(boolean isLocalField) {
        this.isLocalField = isLocalField;
    }

    public abstract String getJson();

    public abstract void setJson(String value) throws CaffaConnectionError;

    public void setSchema(JsonObject schema) {
        this.schema = schema;
    }

    public String dump() {
        return dump("");
    }

    public abstract String dump(String prefix);


    public CaffaObject getOwner() {
        return this.owner;
    }

    public RestClient getClient() {
        return this.owner.getClient();
    }

    public String keyword() { return this.keyword; }
}
