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
    public String dump(String prefix) {
        String result = prefix + "{\n";
        result += prefix + "  keyword = " + this.keyword + "\n";
        result += prefix + "  type = CaffaObjectArrayField::";

        if (!this.localValue.isEmpty()) {
            result += "local\n";
        } else {
            result += "grpc\n";
        }
        result += prefix + "  value = [\n";

        for (CaffaObject object : this.get()) {
            assert object != null;
            result += object.dump(prefix + "  ") + "\n";
        }
        result += prefix + "  ]\n";
        result += prefix + "}\n";
        return result;
    }

    @Override
    public CaffaField<CaffaObject[]> newInstance(CaffaObject owner, String keyword) {
        return new CaffaObjectArrayField(owner, keyword);
    }

    public String typeString() {
        return "object[]";
    }

}
