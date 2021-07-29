package org.caffa.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import org.caffa.rpc.ObjectAccessGrpc.ObjectAccessBlockingStub;

import io.grpc.ManagedChannel;

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
            if (field.type() == CaffaObject.class) {
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

    @SuppressWarnings("unchecked")
    public <T extends CaffaAbstractField> T field(String keyword) {
        return (T) this.fields.get(keyword);
    }

    public List<CaffaAbstractField> fields()
    {
        ArrayList<CaffaAbstractField> allFields = new ArrayList<>();
        for (Map.Entry<String, CaffaAbstractField> entry : fields.entrySet()) {
            CaffaAbstractField field = entry.getValue();
            allFields.add(field);
        }
        return allFields;
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

        Object self = Object.newBuilder().setJson(getJson()).build();
        ObjectList methodList = this.objectStub.listMethods(self);
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

    public CaffaObject execute(CaffaObjectMethod method)
    {
        Object self = Object.newBuilder().setJson(getJson()).build();
        String name = method.classKeyword;

        String paramJson = method.getJson();
        System.out.println("Parameter json: " + paramJson);
        Object params = Object.newBuilder().setJson(paramJson).build();

        MethodRequest request = MethodRequest.newBuilder().setSelf(self).setMethod(name).setParams(params).build();
        Object returnValue = this.objectStub.executeMethod(request);
        return new GsonBuilder()
                .registerTypeAdapter(CaffaObject.class, new CaffaObjectAdapter(this.channel)).create()
                .fromJson(returnValue.getJson(), CaffaObject.class);
    }
}
