package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public class CaffaField<T> extends CaffaAbstractField {
    private final Type dataType;

    private static final Logger logger = LoggerFactory.getLogger(CaffaField.class);

    public boolean unsigned;
    protected T localValue = null;

    public CaffaField(CaffaObject owner, String keyword, Type dataType) {
        super(owner, keyword);
        this.dataType = dataType;
    }

    public String getRemoteJson() {
        logger.debug("Trying to get field value for " + this.keyword + " class " + this.owner.keyword + ", uuid "
                + this.owner.uuid);
        String reply = this.getClient().getFieldValue(this);
        logger.debug("Got field reply: " + reply);
        return reply;
    }

    public String getLocalJson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.getClient(), this.schema, this.isLocalField()));
        builder.registerTypeAdapter(CaffaAppEnum.class, new CaffaAppEnumAdapter());
        return builder.create().toJson(this.localValue);
    }

    void setLocalJson(String json) {
        logger.debug("Got JSON: " + json);
        assert this.isLocalField();
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.getClient(), this.schema, true));
        builder.registerTypeAdapter(CaffaAppEnum.class, new CaffaAppEnumAdapter());
        try {
            // This mainly throws because we may have uint32-values stored which overflows
            // a regular Java int. Accept this for now.
            this.localValue = builder.create().fromJson(json, this.dataType);
        } catch (Exception e) {
            logger.error("Failed to set value for field " + this.keyword + ": " + e.getMessage());
        }
    }

    public String getJson() {
        if (isLocalField()) {
            return getLocalJson();
        }
        return getRemoteJson();
    }

    public void setJson(String json) throws CaffaConnectionError {
        if (isLocalField()) {
            setLocalJson(json);
            return;
        }
        setRemoteJson(json);

    }

    public void setRemoteJson(String value) throws CaffaConnectionError {
        this.getClient().setFieldValue(this, value);
    }

    public void setDeepCopiedJson(String json) throws CaffaConnectionError {
        if (isLocalField()) {
            setLocalJson(json);
        } else {
            setRemoteJson(json);
        }
    }

    public T get() {
        if (isLocalField()) {
            logger.debug("WAS LOCAL FIELD: " + keyword);
            return this.localValue;
        }

        logger.debug("Getting JSON for field " + this.keyword);
        String json = getRemoteJson();
        logger.debug("Got JSON: " + json);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.getClient(), this.schema, false));
        builder.registerTypeAdapter(CaffaAppEnum.class, new CaffaAppEnumAdapter());
        return builder.create().fromJson(json, this.dataType);
    }

    public void set(T value) throws Exception {
        if (isLocalField()) {
            this.localValue = value;
            return;
        }

        logger.debug("Setting JSON for field " + this.keyword + " with value " + value);
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.getClient(), this.schema, false));
        setRemoteJson(builder.create().toJson(value));
    }

    public T deepClone() {
        String json = getJson();
        logger.debug("Got JSON: " + json);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.getClient(), this.schema, true));
        builder.registerTypeAdapter(CaffaAppEnum.class, new CaffaAppEnumAdapter());
        return builder.create().fromJson(json, this.dataType);
    }

    public void deepCopyFrom(T value) throws Exception {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.getClient(), this.schema, true));
        setDeepCopiedJson(builder.create().toJson(value));
    }

    public String dump(String prefix) {
        String result = prefix + "{\n";
        result += prefix + "  keyword = " + this.keyword + "\n";

        result += prefix + "  type = CaffaField<" + dataType + ">::";
        if (isLocalField()) {
            result += "local\n";
        } else {
            result += "rpc\n";
        }

        if (isLocalField()) {
            result += prefix + "  value = " + getLocalJson() + "\n";
        }
        result += prefix + "}\n";

        return result;
    }

    public <U, V> U cast(Class<U> fieldType, Class<V> primitiveType) {
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
}
