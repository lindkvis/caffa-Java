package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;

public class CaffaField<T extends Object> extends CaffaAbstractField {
    private final Type dataType;

    private static Logger logger = LoggerFactory.getLogger(CaffaField.class);

    public boolean unsigned;
    protected String localValue = "";

    public CaffaField(CaffaObject owner, String keyword, Type dataType) {
        super(owner, keyword);
        this.dataType = dataType;
    }

    public String getJson() {
        if (isLocalField()) {
            logger.debug("Local value: " + this.localValue);
            return this.localValue;
        }

        SessionMessage session = SessionMessage.newBuilder().setUuid(this.owner.sessionUuid()).build();

        FieldRequest fieldRequest = FieldRequest.newBuilder().setKeyword(this.keyword)
                .setClassKeyword(this.owner.classKeyword).setUuid(this.owner.uuid).setSession(session).build();

        logger.debug("Trying to get field value for " + this.keyword + " class " + this.owner.classKeyword);
        GenericValue reply = this.fieldBlockingStub.getValue(fieldRequest);
        logger.debug("Got field reply: " + reply.getValue());
        return reply.getValue();
    }

    public void setJson(String value) {
        if (isLocalField()) {
            logger.debug("Setting local value: " + value);
            this.localValue = value;
        } else {
            SessionMessage session = SessionMessage.newBuilder().setUuid(this.owner.sessionUuid()).build();
            FieldRequest fieldRequest = FieldRequest.newBuilder().setKeyword(this.keyword)
                    .setClassKeyword(this.owner.classKeyword).setUuid(this.owner.uuid).setSession(session).build();

            String jsonValue = value;
            SetterRequest setterRequest = SetterRequest.newBuilder().setField(fieldRequest).setValue(jsonValue).build();
            this.fieldBlockingStub.setValue(setterRequest);
        }
    }

    public T get() {
        logger.debug("Getting JSON for field " + this.keyword);
        String json = getJson();
        logger.debug("Got JSON: " + json);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.channel, this.owner.sessionUuid()));
        builder.registerTypeAdapter(CaffaAppEnum.class, new CaffaAppEnumAdapter());
        return builder.create().fromJson(json, this.dataType);
    }

    public void set(T value) throws Exception {
        logger.debug("Setting JSON for field " + this.keyword + " with value " + value);

        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.channel, this.owner.sessionUuid()));
        setJson(builder.create().toJson(value));
    }

    public CaffaObject[] children() {
        CaffaObject[] emptyArray = {};
        return emptyArray;
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
        System.out.println("Casting field of " + this.dataType.getTypeName() + " to "
                + fieldType.getTypeName() + "::" + primitiveType.getTypeName());
        if (primitiveType == this.dataType) {
            return fieldType.cast(this);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <V> CaffaField<V> cast(Class<V> primitiveType) {
        if (primitiveType == this.dataType) {
            return (CaffaField<V>) this;
        }
        return null;
    }

    public <U> void set(U value, Class<U> primitiveType) throws Exception {
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

    public boolean isArray() {
        return this.dataType.getClass().isArray();
    }

    public void setUnsigned(boolean unsigned) {
        this.unsigned = unsigned;
    }

    public boolean getUnsigned() {
        return this.unsigned;
    }

    public String typeString() {
        if (this.getUnsigned()) {
            return "u" + CaffaFieldFactory.dataTypes.inverse().get(this.dataType);
        }
        return CaffaFieldFactory.dataTypes.inverse().get(this.dataType);
    }

}
