package org.caffa.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.caffa.rpc.ObjectAccessGrpc.ObjectAccessBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.Status;

public class CaffaObject {
    public final String classKeyword;
    public final String uuid;

    private Map<String, CaffaField<?>> fields;
    private CaffaField<?> parentField = null;

    protected final String sessionUuid;
    protected ManagedChannel channel;
    private ObjectAccessBlockingStub objectStub;

    private static Logger logger = LoggerFactory.getLogger(CaffaObject.class);

    public CaffaObject(String classKeyword, String uuid, String sessionUuid) {
        assert !classKeyword.isEmpty();
        assert !uuid.isEmpty();
        assert !sessionUuid.isEmpty();

        this.classKeyword = classKeyword;
        this.uuid = uuid;
        this.sessionUuid = sessionUuid;
        this.fields = new TreeMap<String, CaffaField<?>>();
    }

    public CaffaObject(String classKeyword, String sessionUuid) {
        assert !classKeyword.isEmpty();
        assert !sessionUuid.isEmpty();

        this.classKeyword = classKeyword;
        this.uuid = "";
        this.sessionUuid = sessionUuid;
        this.fields = new TreeMap<String, CaffaField<?>>();
    }

    void createGrpcAccessor(ManagedChannel channel) {
        assert !uuid.isEmpty();
        this.channel = channel;
        this.objectStub = ObjectAccessGrpc.newBlockingStub(this.channel);
    }

    public boolean isRemoteObject() {
        return this.channel != null && this.objectStub != null;
    }

    public boolean isLocalObject() {
        return !isRemoteObject();
    }

    public ManagedChannel channel() {
        return this.channel;
    }

    public String sessionUuid() {
        return this.sessionUuid;
    }

    public String dump() {
        return dump("");
    }

    public String dump(String prefix) {
        String result = prefix + "{\n";
        result += prefix + "  classKeyword = " + this.classKeyword + "\n";
        result += prefix + "  uuid = " + uuid + "\n";
        result += prefix + "  fields = [\n";
        for (Map.Entry<String, CaffaField<?>> entry : this.fields.entrySet()) {
            CaffaField<?> field = entry.getValue();
            if (field == null) {
                logger.error("Field " + entry.getKey() + " is null!");
            } else {
                assert field != null;
                result += field.dump(prefix + "    ");
            }
        }
        result += prefix + "  ]\n";
        result += prefix + "}\n";
        return result;
    }

    public CaffaField<?> field(String keyword) throws RuntimeException {
        if (!this.fields.containsKey(keyword)) {
            String errMsg = "Field does not exist: " + keyword;
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        return this.fields.get(keyword);
    }

    public <T> CaffaField<T> field(String keyword, Class<T> type) throws RuntimeException {
        return field(keyword).cast(type);
    }

    public List<CaffaField<?>> fields() {
        ArrayList<CaffaField<?>> allFields = new ArrayList<>();
        for (Map.Entry<String, CaffaField<?>> entry : fields.entrySet()) {
            allFields.add(entry.getValue());
        }
        return allFields;
    }

    public void addField(CaffaField<?> field) {
        this.fields.put(field.keyword, field);
    }

    public CaffaObject parent() {
        return parentField.owner;
    }

    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaField.class,
                new CaffaFieldAdapter(this, this.channel)).registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.channel, this.sessionUuid));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this);
        return jsonObject;
    }

    public String getAddressJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("class", this.classKeyword);
        jsonObject.addProperty("uuid", this.uuid);
        return jsonObject.toString();
    }

    public ArrayList<CaffaObjectMethod> methods() {
        ArrayList<CaffaObjectMethod> methods = new ArrayList<CaffaObjectMethod>();

        RpcObject self = RpcObject.newBuilder().setJson(getJson()).build();
        SessionMessage session = SessionMessage.newBuilder().setUuid(this.sessionUuid).build();
        ListMethodsRequest request = ListMethodsRequest.newBuilder().setSelfObject(self).setSession(session).build();

        RpcObjectList methodList = this.objectStub.listMethods(request);
        for (RpcObject method : methodList.getObjectsList()) {
            CaffaObjectMethod caffaMethod = new GsonBuilder().registerTypeAdapter(CaffaField.class,
                    new CaffaFieldAdapter(this, this.channel))
                    .registerTypeAdapter(CaffaObjectMethod.class,
                            new CaffaObjectMethodAdapter(this))
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
        throw new RuntimeException("Failed to find method " + nameWithClass);
    }

    public CaffaObjectMethodResult execute(CaffaObjectMethod method) throws Exception {
        SessionMessage session = SessionMessage.newBuilder().setUuid(this.sessionUuid).build();
        RpcObject self = RpcObject.newBuilder().setJson(getJson()).build();
        String name = method.classKeyword;

        String paramJson = method.getJson();
        logger.debug("Parameter json: " + paramJson);
        RpcObject params = RpcObject.newBuilder().setJson(paramJson).build();

        MethodRequest request = MethodRequest.newBuilder().setSelfObject(self).setMethod(name).setParams(params)
                .setSession(session)
                .build();

        try {
            RpcObject returnValue = this.objectStub.executeMethod(request);
            logger.debug("Return value json: " + returnValue.getJson());

            return new GsonBuilder()
                    .registerTypeAdapter(CaffaObjectMethodResult.class, new CaffaObjectMethodResultAdapter(
                            this.channel, this.sessionUuid))
                    .registerTypeAdapter(CaffaField.class,
                            new CaffaFieldAdapter(this, this.channel))
                    .create()
                    .fromJson(returnValue.getJson(), CaffaObjectMethodResult.class);
        } catch (Exception e) {
            Status status = Status.fromThrowable(e);
            logger.error("Failed to execute method with error: " + status.getDescription() + " ... " + e.getMessage());
            throw new RuntimeException(status.getDescription());
        }
    }

    public String typeString() {
        return "object";
    }
}
