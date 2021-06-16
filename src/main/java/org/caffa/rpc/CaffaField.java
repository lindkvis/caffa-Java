package org.caffa.rpc;

import org.caffa.rpc.CaffaAbstractField;
import org.caffa.rpc.CaffaObject;

import io.grpc.ManagedChannel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;

public class CaffaField<DataType> extends CaffaAbstractField {
    private final Class<DataType> dataType;

    public CaffaField(CaffaObject owner, Class<DataType> dataType) {
        super(owner);

        this.dataType = dataType;
    }

    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.owner.channel));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this.owner);
        Object self = Object.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(self).build();

        GetterReply reply = this.fieldStub.getValue(fieldRequest);
        return reply.getValue();
    }

    public void setJson(String value) {

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.owner.channel));
        String jsonObject = builder.create().toJson(this.owner);
        Object object = Object.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(object).build();

        String jsonValue = new Gson().toJson(value);
        SetterRequest setterRequest = SetterRequest.newBuilder().setField(fieldRequest).setValue(jsonValue).build();
        this.fieldStub.setValue(setterRequest);
    }

    public DataType get() {
        String json = getJson();
        return new Gson().fromJson(json, this.dataType);

    }

    public void set(DataType value) {
        setJson(value.toString());
    }

    public void dump() {
        System.out.println("CaffaField <" + dataType + "> {");
        System.out.println("keyword = " + keyword);
        System.out.println("type = " + type);
        System.out.println("}");

    }
}
