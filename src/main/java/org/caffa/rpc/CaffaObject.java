package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.Object;

import java.util.ArrayList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class CaffaObject {
    public Object object;
    public ArrayList<CaffaAbstractField> fields;

    public CaffaObject(Object object) {
        this.object = object;
        this.fields = new ArrayList<CaffaAbstractField>();
    }

    public void dump() {
        System.out.println("{");
        System.out.println("classKeyword = " + this.object.getClassKeyword());
        System.out.println("address = " + this.object.getAddress());
        System.out.println("fields = [");
        for (CaffaAbstractField field : fields) {
            field.dump();
        }
        System.out.println("]");
        System.out.println("}");
    }
}
