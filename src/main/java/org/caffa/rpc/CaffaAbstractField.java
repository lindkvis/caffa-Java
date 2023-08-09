package org.caffa.rpc;

public abstract class CaffaAbstractField {
    protected CaffaObject owner;
    public final String keyword;

    protected RestClient client = null;

    protected boolean isLocalField = false;


    public CaffaAbstractField(CaffaObject owner, String keyword) {
        this.owner = owner;
        this.keyword = keyword;
    }

    public boolean isLocalField() {
        return this.isLocalField;
    }

    public void createRestAccessor(RestClient client) {
        assert this.owner != null;
        this.client = client;
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


    public CaffaObject getOwner() {
        return this.owner;
    }
}
