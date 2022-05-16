package org.caffa.rpc;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.List;

public class CaffaObjectField extends CaffaField<CaffaObject> {
    protected static final Logger logger = Logger.getLogger(CaffaObjectField.class.getName());

    public CaffaObjectField(CaffaObject owner, String keyword, CaffaObject value) {
        super(owner, keyword, CaffaObject.class);
        createAccessor(false);
        try {
            this.set(value);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

    }

    public CaffaObjectField(CaffaObject owner, String keyword) {
        super(owner, keyword, CaffaObject.class);
        createAccessor(true);
    }

    @Override
    public void dump() {
        System.out.println("CaffaObjectField {");
        super.dump();
        this.get().dump();
        System.out.println("}");
    }

    @Override
    public final List<CaffaObject> children() {
        return Arrays.asList(this.get());
    }

    @Override
    public CaffaField<CaffaObject> newInstance(CaffaObject owner, String keyword) {
        return new CaffaObjectField(owner, keyword);
    }
}
