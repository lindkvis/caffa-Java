package org.caffa.rpc;

import com.google.gson.Gson;

public class CaffaField<T> extends CaffaAbstractField {
    private final Class<T> dataType;

    private FieldAccessGrpc.FieldAccessBlockingStub fieldStub = null;
    private String localValue = null;

    public CaffaField(CaffaObject owner, String keyword, Class<T> dataType) {
        super(owner, keyword);
        this.dataType = dataType;
    }

    @Override
    public void createAccessor(boolean grpc)
    {
        if (grpc)
        {
            if (this.owner != null) {
                this.fieldStub = FieldAccessGrpc.newBlockingStub(this.owner.channel);
            }    
        }
        else
        {
            this.localValue = "";
        }
    }

    public String getJson() {
        if (localValue != null) return this.localValue;

        String jsonObject = this.owner.getJson();
        Object self = Object.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(self).build();

        GetterReply reply = this.fieldStub.getValue(fieldRequest);
        return reply.getValue();
    }

    public void setJson(String value) {
        if (this.localValue != null) 
        {
            this.localValue = value;
        }
        else
        {
            String jsonObject = this.owner.getJson();
            Object object = Object.newBuilder().setJson(jsonObject).build();
            FieldRequest fieldRequest = FieldRequest.newBuilder().setMethod(this.keyword).setSelf(object).build();

            String jsonValue = value;
            SetterRequest setterRequest = SetterRequest.newBuilder().setField(fieldRequest).setValue(jsonValue).build();
            this.fieldStub.setValue(setterRequest);
        }
    }

    public T get() {
        String json = getJson();
        return new Gson().fromJson(json, this.dataType);
    }

    public void set(T value) {
        setJson(new Gson().toJson(value));
    }

    public void dump() {
        System.out.print("CaffaField<" + dataType + ">::");
        if (this.localValue != null)
        {
            System.out.print("local");
        }
        else{
            System.out.print("grpc");
        }
        
        System.out.println(" {");
        super.dump();
        if (this.localValue != null)
        {
            System.out.println("value = " + this.localValue);
        }
        System.out.println("}");
    }

    public CaffaAbstractField newInstance(CaffaObject owner, String keyword) {
        return new CaffaField<T>(owner, keyword, this.dataType);
    }

    public Class<?> type() {
        return this.dataType;
    }
}