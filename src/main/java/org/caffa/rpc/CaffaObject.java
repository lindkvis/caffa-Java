package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.Object;

import io.grpc.ManagedChannel;

import com.google.gson.annotations.Expose;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map;

public class CaffaObject {
    @Expose
    public String classKeyword;
    @Expose
    public long serverAddress;

    public Map<String, CaffaAbstractField> fields;
    public CaffaAbstractObjectField parentField = null;

    public ManagedChannel channel;

    public CaffaObject(ManagedChannel channel) {
        this.channel = channel;
        this.fields = new TreeMap<String, CaffaAbstractField>();
    }

    public void dump() {
        System.out.println("CaffaObject {");
        System.out.println("fields = [");
        for (Map.Entry<String, CaffaAbstractField> entry : this.fields.entrySet()) {
            System.out.print(entry.getKey() + " = ");
            entry.getValue().dump();
        }
        System.out.println("]");
        System.out.println("}");
    }

    public ArrayList<CaffaObject> children() {
        ArrayList<CaffaObject> allChildren = new ArrayList<CaffaObject>();
        for (Map.Entry<String, CaffaAbstractField> entry : fields.entrySet()) {
            CaffaAbstractField field = entry.getValue();
            if (field.getType() == CaffaObject.class) {
                CaffaAbstractObjectField objectField = (CaffaAbstractObjectField) entry.getValue();
                allChildren.addAll(objectField.children());
            }
        }
        return allChildren;
    }

    public ArrayList<CaffaObject> descendantsMatchingKeyword(String keyword) {
        ArrayList<CaffaObject> matchingObjects = new ArrayList<CaffaObject>();
        for (CaffaObject child : children()) {
            if (child.classKeyword.equals(keyword)) {
                matchingObjects.add(child);
            }
            matchingObjects.addAll(child.descendantsMatchingKeyword(keyword));
        }
        return matchingObjects;
    }

    public ArrayList<CaffaObject> ancestorsMatchingKeyword(String keyword) {
        ArrayList<CaffaObject> matchingObjects = new ArrayList<CaffaObject>();
        matchingObjects.addAll(parent().ancestorsMatchingKeyword(keyword));
        if (parent().classKeyword.equals(keyword)) {
            matchingObjects.add(parent());
        }
        return matchingObjects;
    }

    public CaffaAbstractField field(String keyword) {
        return this.fields.get(keyword);
    }

    public CaffaObject parent() {
        return parentField.owner;
    }

}
