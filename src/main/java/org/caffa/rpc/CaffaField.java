package org.caffa.rpc;

import com.google.gson.Gson;

public class CaffaField<DataType> extends CaffaAbstractField {
    private final Class<DataType> dataType;

    public CaffaField(CaffaObject owner, String keyword, Class<DataType> dataType) {
        super(owner, keyword);
        this.dataType = dataType;
    }

    public String getJson() {
        String jsonObject = this.owner.getJson();
        Object self = Object.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(self).build();

        GetterReply reply = this.fieldStub.getValue(fieldRequest);
        return reply.getValue();
    }

    public void setJson(String value) {

        String jsonObject = this.owner.getJson();
        Object object = Object.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(object).build();

        String jsonValue = value;
        SetterRequest setterRequest = SetterRequest.newBuilder().setField(fieldRequest).setValue(jsonValue).build();
        this.fieldStub.setValue(setterRequest);
    }

    public DataType get() {
        String json = getJson();
        return new Gson().fromJson(json, this.dataType);

    }

    public void set(DataType value) {
        setJson(new Gson().toJson(value));
    }

    public void dump() {
        System.out.println("CaffaField <" + dataType + "> {");
        super.dump();
        System.out.println("}");
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaField<DataType>(owner, keyword, this.dataType);
    }

    public Class<?> type() {
        return this.dataType;
    }
}