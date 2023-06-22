package org.caffa.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.caffa.rpc.ObjectAccessGrpc.ObjectAccessBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.Status;

public class CaffaObject {
    public final String keyword;
    public final String uuid;

    private final Map<String, CaffaField<?>> fields;

    protected final String sessionUuid;
    protected ManagedChannel channel;
    private ObjectAccessBlockingStub objectStub;

    static final long METHOD_TIMEOUT = 5000;

    private static final Logger logger = LoggerFactory.getLogger(CaffaObject.class);

    public CaffaObject(String keyword, String uuid, String sessionUuid) {
        assert !keyword.isEmpty();
        assert !uuid.isEmpty();
        assert !sessionUuid.isEmpty();

        this.keyword = keyword;
        this.uuid = uuid;
        this.sessionUuid = sessionUuid;
        this.fields = new TreeMap<String, CaffaField<?>>();
    }

    public CaffaObject(String keyword, String sessionUuid) {
        assert !keyword.isEmpty();
        assert !sessionUuid.isEmpty();

        this.keyword = keyword;
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
        result += prefix + "  keyword = " + this.keyword + "\n";
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

    public String getJson() {
        System.out.println("CaffaObject::getJson()");
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaField.class,
                new CaffaFieldAdapter(this, this.channel)).registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.channel, this.sessionUuid));
        Gson gson = builder.serializeNulls().create();
        return gson.toJson(this);
    }

    public String getAddressJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("keyword", this.keyword);
        jsonObject.addProperty("uuid", this.uuid);
        return jsonObject.toString();
    }

    public ArrayList<CaffaObjectMethod> methods() {
        ArrayList<CaffaObjectMethod> methods = new ArrayList<CaffaObjectMethod>();

        RpcObject self = RpcObject.newBuilder().setJson(getJson()).build();
        SessionMessage session = SessionMessage.newBuilder().setUuid(this.sessionUuid).build();
        ListMethodsRequest request = ListMethodsRequest.newBuilder().setSelfObject(self).setSession(session).build();

        RpcObjectList methodList = this.objectStub.withDeadlineAfter(METHOD_TIMEOUT, TimeUnit.MILLISECONDS).listMethods(request);
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
        String nameWithClass = this.keyword + "_" + name;
        for (CaffaObjectMethod myMethod : methods()) {
            if (myMethod.keyword.equals(name) || myMethod.keyword.equals(nameWithClass))
                return myMethod;
        }
        throw new RuntimeException("Failed to find method " + name);
    }

    public CaffaObjectMethodResult execute(CaffaObjectMethod method) throws Exception {
        SessionMessage session = SessionMessage.newBuilder().setUuid(this.sessionUuid).build();
        RpcObject self = RpcObject.newBuilder().setJson(getJson()).build();

        String paramJson = method.getJson();
        RpcObject params = RpcObject.newBuilder().setJson(paramJson).build();

        MethodRequest request = MethodRequest.newBuilder().setSelfObject(self).setMethod(params)
                .setSession(session)
                .build();

        try {
            RpcObject returnValue = this.objectStub.withDeadlineAfter(METHOD_TIMEOUT, TimeUnit.MILLISECONDS).executeMethod(request);
            return new CaffaObjectMethodResult(returnValue.getJson());

        } catch (Exception e) {
            Status status = Status.fromThrowable(e);
            logger.error("Failed to execute method with error: " + status.getDescription() + " ... " + e.getMessage());
            throw new RuntimeException("Failed to complete server task: " + status.getDescription());
        }
    }

    public String typeString() {
        return "object";
    }
}
