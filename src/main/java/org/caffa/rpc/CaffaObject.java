package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObjectMethod;
import org.caffa.rpc.MethodRequest;
import org.caffa.rpc.Object;
import org.caffa.rpc.ObjectList;
import org.caffa.rpc.ObjectAccessGrpc.ObjectAccessBlockingStub;

import io.grpc.ManagedChannel;

import com.google.gson.annotations.Expose;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map;

public class CaffaObject {
    @Expose
    public String classKeyword;
    @Expose
    public String uuid;


    public Map<String, CaffaAbstractField> fields;
    public CaffaAbstractObjectField parentField = null;

    public ManagedChannel channel;
    private final ObjectAccessBlockingStub objectStub;

    public CaffaObject(ManagedChannel channel) {
        this.channel = channel;
        this.objectStub = ObjectAccessGrpc.newBlockingStub(this.channel);
        this.fields = new TreeMap<String, CaffaAbstractField>();
    }

    public void dump() {
        System.out.println(this.classKeyword + " {");
        System.out.println("uuid = " + uuid);
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

    public String getJson()
    {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
        new CaffaObjectAdapter(this.channel));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this);
        return jsonObject;
    }

    public ArrayList<CaffaObjectMethod> methods()
    {
        ArrayList<CaffaObjectMethod> methods = new ArrayList<CaffaObjectMethod>();

        Object object = Object.newBuilder().setJson(getJson()).build();
        ObjectList methodList = this.objectStub.listMethods(object);
        for (Object method : methodList.getObjectsList())
        {
            CaffaObjectMethod caffaMethod = new GsonBuilder()
            .registerTypeAdapter(CaffaObjectMethod.class, new CaffaObjectMethodAdapter(this, this.channel)).create()
            .fromJson(method.getJson(), CaffaObjectMethod.class);
            methods.add(caffaMethod);
        }
        return methods;
    }

    public CaffaObjectMethod method(String name)
    {
        for (CaffaObjectMethod myMethod : methods())
        {
            if (myMethod.classKeyword == name) return myMethod;
        }
        return null;
    }

}
