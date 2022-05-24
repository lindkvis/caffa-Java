package org.caffa.rpc;

import java.lang.NoSuchFieldError;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger logger = LoggerFactory.getLogger(CaffaObject.class);

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
        logger.debug("Fields size: " + fields.size());
        for (Map.Entry<String, CaffaField<?>> entry : fields.entrySet()) {
            CaffaField<?> field = entry.getValue();
            logger.debug("Getting children from field: " + entry.getKey());
            allChildren.addAll(field.children());
            logger.debug("Got children from field: " + entry.getKey());
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

    public CaffaField<?> field(String keyword) throws RuntimeException {
        if (!this.fields.containsKey(keyword)) {
            String errMsg = "Field does not exist: " + keyword;
            logger.error( errMsg);
            throw new RuntimeException(errMsg);
        }
        return this.fields.get(keyword);
    }

    public <T> CaffaField<T> typedField(String keyword, Class<T> type) throws RuntimeException {
        CaffaField<?> untypedField = this.fields.get(keyword);
        if (untypedField == null) {
            String errMsg = "Field '" + keyword + "' of type " + type.getName() + " does not exist";
            logger.error( errMsg);
            throw new RuntimeException(errMsg);

        }
        return untypedField.cast(type);
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
                new CaffaObjectAdapter(this.channel, true));
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
        logger.debug("Parameter json: " + paramJson);
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
            logger.error( "Failed to execute method with error: " + status.getDescription());
            return null;
        }
    }
}
