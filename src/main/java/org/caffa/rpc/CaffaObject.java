package org.caffa.rpc;

import java.util.ArrayList;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.caffa.rpc.CaffaField;

public class CaffaObject {
    public String classKeyword;
    public long address;
    public ArrayList<CaffaAbstractField> fields;

    public CaffaObject() {
        fields = new ArrayList<CaffaAbstractField>();
    }

    public void dump() {
        System.out.println("{");
        System.out.println("classKeyword = " + classKeyword);
        System.out.println("address = " + address);
        System.out.println("fields = [");
        for (CaffaAbstractField field : fields) {
            field.dump();
        }
        System.out.println("]");
        System.out.println("}");
    }
}
