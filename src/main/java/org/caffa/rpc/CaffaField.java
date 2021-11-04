package org.caffa.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Type;

public class CaffaField<T extends Object> {
    private final Type dataType;
    protected final Type scalarType;

    protected CaffaObject owner;
    protected static final Logger logger = Logger.getLogger(CaffaField.class.getName());

    public String keyword;
    public boolean unsigned;

    private FieldAccessGrpc.FieldAccessBlockingStub fieldStub = null;
    private String localValue = null;

    public CaffaField(CaffaObject owner, String keyword, Type dataType) {
        this.owner = owner;
        this.keyword = keyword;
        this.dataType = dataType;
        this.scalarType = dataType;
    }

    public CaffaField(CaffaObject owner, String keyword, Type dataType, Type scalarType) {
        this.owner = owner;
        this.keyword = keyword;
        this.dataType = dataType;
        this.scalarType = scalarType;

    }

    public void createAccessor(boolean grpc) {
        if (grpc) {
            if (this.owner != null) {
                this.fieldStub = FieldAccessGrpc.newBlockingStub(this.owner.channel);
            }
        } else {
            this.localValue = "";
        }
    }

    public String getJson() {
        if (localValue != null) {
            logger.log(Level.FINEST, "Local value: " + this.localValue);
            return this.localValue;
        }

        String jsonObject = this.owner.getAddressJson();
        logger.log(Level.FINEST, "Got owner json: " + jsonObject);
        RpcObject self = RpcObject.newBuilder().setJson(jsonObject).build();
        FieldRequest fieldRequest = FieldRequest.newBuilder().setKeyword(this.keyword).setSelfObject(self).build();

        logger.log(Level.FINEST, "Trying to get field value for " + this.keyword);
        GenericScalar reply = this.fieldStub.getValue(fieldRequest);
        logger.log(Level.FINEST, "Got field reply: " + reply.getValue());
        return reply.getValue();
    }

    public void setJson(String value) {
        if (this.localValue != null) {
            logger.log(Level.FINEST, "Setting local value: " + value);

            this.localValue = value;
        } else {
            String jsonObject = this.owner.getAddressJson();
            RpcObject object = RpcObject.newBuilder().setJson(jsonObject).build();
            FieldRequest fieldRequest = FieldRequest.newBuilder().setKeyword(this.keyword).setSelfObject(object).build();

            String jsonValue = value;
            SetterRequest setterRequest = SetterRequest.newBuilder().setField(fieldRequest).setValue(jsonValue).build();
            this.fieldStub.setValue(setterRequest);
        }
    }

    public T get() {
        logger.log(Level.FINEST, "Getting JSON for field " + this.keyword);
        String json = getJson();
        logger.log(Level.FINEST, "Got JSON: " + json);
        return new GsonBuilder().registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.owner.channel))
                .create().fromJson(json, this.dataType);
    }

    public void set(T value) {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.owner.channel));
        setJson(builder.create().toJson(value));
    }

    public List<CaffaObject> children() {
        return new ArrayList<>();
    }

    public void dump() {
        System.out.print("CaffaField<" + dataType + ">::");
        if (this.localValue != null) {
            System.out.print("local");
        } else {
            System.out.print("grpc");
        }

        System.out.println(" {");
        System.out.println("keyword = " + this.keyword);
        if (this.localValue != null) {
            System.out.println("value = " + this.localValue);
        }
        System.out.println("}");
    }

    public <U, V> U cast(Class<U> fieldType, Class<V> primitiveType) {
        if (primitiveType == this.scalarType) {
            return fieldType.cast(this);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <V> CaffaField<V> cast(Class<V> primitiveType) {
        if (primitiveType == this.scalarType) {
            return (CaffaField<V>) this;
        }
        return null;
    }

    public <U> void set(U value, Class<U> primitiveType) {
        CaffaField<U> typedField = this.cast(primitiveType);
        if (typedField != null) {
            typedField.set(value);
        } else {
            throw new ClassCastException();
        }
    }

    public <U> U get(Class<U> primitiveType) {
        CaffaField<U> typedField = this.cast(primitiveType);
        if (typedField == null) {
            throw new ClassCastException();
        }
        return typedField.get();
    }

    public CaffaField<T> newInstance(CaffaObject owner, String keyword) {
        return new CaffaField<>(owner, keyword, this.dataType);
    }

    public Type type() {
        return this.dataType;
    }

    public void setUnsigned(boolean unsigned) {
        this.unsigned = unsigned;
    }

    public boolean getUnsigned() {
        return this.unsigned;
    }
}
