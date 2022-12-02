package org.caffa.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;

import java.util.Arrays;
import java.util.List;

public class CaffaObjectField extends CaffaField<CaffaObject> {
    protected static final Logger logger = LoggerFactory.getLogger(CaffaObjectField.class);

    public CaffaObjectField(CaffaObject owner, String keyword, CaffaObject value) {
        super(owner, keyword, CaffaObject.class);
        assert value != null;
        try {
            this.set(value);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public CaffaObjectField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaObject.class);
    }

    @Override
    public String dump(String prefix) {
        String result = prefix + "{\n";
        result += prefix + "  keyword = " + this.keyword + "\n";
        result += prefix + "  type = CaffaObjectField::";
        if (!this.localValue.isEmpty()) {
            result += "local\n";
        } else {
            result += "grpc\n";
        }

        result += prefix + "  value = " + this.get().dump(prefix + "  ");
        result += prefix + "}\n";
        return result;
    }

    @Override
    public final CaffaObject[] children() {
        CaffaObject[] singleEntry = { this.get() };
        return singleEntry;
    }

    @Override
    public CaffaField<CaffaObject> newInstance(CaffaObject owner, String keyword) {
        return new CaffaObjectField(owner, keyword);
    }
}
