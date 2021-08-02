package org.caffa.rpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CaffaObjectField extends CaffaField<CaffaObject> {
    public CaffaObjectField(CaffaObject owner, String keyword, CaffaObject value) {
        super(owner, keyword, CaffaObject.class);
        createAccessor(false);
        this.set(value);
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
    public final List<CaffaObject> children()
    {
        return Arrays.asList(this.get());
    }

    @Override
    public CaffaField<CaffaObject> newInstance(CaffaObject owner, String keyword) {
        return new CaffaObjectField(owner, keyword);
    }
}
