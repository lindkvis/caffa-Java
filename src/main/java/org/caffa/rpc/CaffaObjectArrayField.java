package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import io.grpc.ManagedChannel;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class CaffaObjectArrayField extends CaffaField<CaffaObject[]> {
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectArrayField.class);

    public CaffaObjectArrayField(CaffaObject owner, String keyword, CaffaObject[] value) {
        super(owner, keyword, CaffaObject[].class);
        assert value != null;
        try {
            this.set(value);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public CaffaObjectArrayField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaObject[].class);
    }

    @Override
    public final CaffaObject[] children() {
        return get();
    }

    @Override
    public void dump() {
        System.out.println("CaffaObjectArrayField {");
        super.dump();
        for (CaffaObject object : this.get()) {
            object.dump();
        }
        System.out.println("}");
    }

    @Override
    public CaffaField<CaffaObject[]> newInstance(CaffaObject owner, String keyword) {
        return new CaffaObjectArrayField(owner, keyword);
    }

    public String typeString() {
        return "object[]";
    }

    @Override
    public CaffaObject[] get() {
        logger.debug("Getting JSON for field " + this.keyword);
        String json = getJson();
        return new Gson().fromJson(json, CaffaObject[].class);
    }
}
