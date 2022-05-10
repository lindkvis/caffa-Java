package org.caffa.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import org.caffa.rpc.ObjectAccessGrpc.ObjectAccessBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.Status;

public class CaffaObject {
    @Expose
    public String classKeyword;
    @Expose
    public String uuid;

    public Map<String, CaffaField<?>> fields;
    public CaffaField<?> parentField = null;

    public ManagedChannel channel;
    private final ObjectAccessBlockingStub objectStub;

    protected static final Logger logger = Logger.getLogger(CaffaObject.class.getName());

    public CaffaObject(ManagedChannel channel) {
        this.channel = channel;
        this.objectStub = ObjectAccessGrpc.newBlockingStub(this.channel);
        this.fields = new TreeMap<String, CaffaField<?>>();
    }

    public void dump() {
        System.out.println(this.classKeyword + " {");
        System.out.println("uuid = " + uuid);
        System.out.println("fields = [");
        for (Map.Entry<String, CaffaField<?>> entry : this.fields.entrySet()) {
            System.out.print(entry.getKey() + " = ");
            entry.getValue().dump();
        }
        System.out.println("]");
        System.out.println("}");
    }

    public ArrayList<CaffaObject> children() {
        ArrayList<CaffaObject> allChildren = new ArrayList<CaffaObject>();
        logger.log(Level.FINER, "Fields size: " + fields.size());
        for (Map.Entry<String, CaffaField<?>> entry : fields.entrySet()) {
            CaffaField<?> field = entry.getValue();
            logger.log(Level.FINER, "Getting children from field: " + entry.getKey());
            allChildren.addAll(field.children());
            logger.log(Level.FINER, "Got children from field: " + entry.getKey());
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

    public CaffaField<?> field(String keyword) {
        if (!this.fields.containsKey(keyword)) {
            logger.log(Level.WARNING, "Field does not exist: " + keyword);
            return null;
        }
        return this.fields.get(keyword);
    }

    public <T> CaffaField<T> typedField(String keyword, Class<T> type) {
        CaffaField<?> untypedField = this.fields.get(keyword);
        if (untypedField != null) {
            return untypedField.cast(type);
        } else {
            logger.log(Level.WARNING, "Field '" + keyword + "' of type " + type.getName() + " does not exist");
            return null;
        }
    }

    public List<CaffaField<?>> fields() {
        ArrayList<CaffaField<?>> allFields = new ArrayList<>();
        for (Map.Entry<String, CaffaField<?>> entry : fields.entrySet()) {
            allFields.add(entry.getValue());
        }
        return allFields;
    }

    public CaffaObject parent() {
        return parentField.owner;
    }

    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                new CaffaObjectAdapter(this.channel));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this);
        return jsonObject;
    }

    public String getAddressJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("Class", this.classKeyword);
        jsonObject.addProperty("UUID", this.uuid);
        return jsonObject.toString();
    }

    public ArrayList<CaffaObjectMethod> methods() {
        ArrayList<CaffaObjectMethod> methods = new ArrayList<CaffaObjectMethod>();

        RpcObject self = RpcObject.newBuilder().setJson(getJson()).build();
        RpcObjectList methodList = this.objectStub.listMethods(self);
        for (RpcObject method : methodList.getObjectsList()) {
            CaffaObjectMethod caffaMethod = new GsonBuilder()
                    .registerTypeAdapter(CaffaObjectMethod.class, new CaffaObjectMethodAdapter(this, this.channel))
                    .create()
                    .fromJson(method.getJson(), CaffaObjectMethod.class);
            methods.add(caffaMethod);
        }
        return methods;
    }

    public CaffaObjectMethod method(String name) {
        String nameWithClass = this.classKeyword + "_" + name;
        for (CaffaObjectMethod myMethod : methods()) {
            if (myMethod.classKeyword.equals(name) || myMethod.classKeyword.equals(nameWithClass))
                return myMethod;
        }
        return null;
    }

    public CaffaObjectMethodResult execute(CaffaObjectMethod method) {
        RpcObject self = RpcObject.newBuilder().setJson(getJson()).build();
        String name = method.classKeyword;

        String paramJson = method.getJson();
        logger.log(Level.FINER, "Parameter json: " + paramJson);
        RpcObject params = RpcObject.newBuilder().setJson(paramJson).build();

        MethodRequest request = MethodRequest.newBuilder().setSelfObject(self).setMethod(name).setParams(params)
                .build();

        try {
            RpcObject returnValue = this.objectStub.executeMethod(request);

            return new GsonBuilder()
                    .registerTypeAdapter(CaffaObjectMethodResult.class, new CaffaObjectMethodResultAdapter(
                            this, this.channel))
                    .create()
                    .fromJson(returnValue.getJson(), CaffaObjectMethodResult.class);
        } catch (Exception e) {
            Status status = Status.fromThrowable(e);
            logger.log(Level.SEVERE, "Failed to execute method with error: " + status.getDescription());
            return null;
        }
    }
}
