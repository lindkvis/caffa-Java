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

public class CaffaObject {
    public final String keyword;
    public String uuid = "";
    private final boolean hasLocalDataFields;

    private final Map<String, CaffaField<?>> fields;
    private final Map<String, CaffaObjectMethod> methods;

    protected RestClient client;

    private static final Logger logger = LoggerFactory.getLogger(CaffaObject.class);

    public CaffaObject(String keyword, boolean hasLocalDataFields, RestClient client, String uuid) {
        assert !keyword.isEmpty();

        this.keyword = keyword;
        this.hasLocalDataFields = hasLocalDataFields;

        this.fields = new TreeMap<>();
        this.methods = new TreeMap<>();

        this.client = client;
        this.uuid = uuid;
    }

    public boolean isLocalObject() {
        return this.hasLocalDataFields;
    }

    public RestClient getClient() {
        return this.client;
    }

    public String dump() {
        return dump("");
    }

    public String dump(String prefix) {
        String result = prefix + "{\n";
        result += prefix + "  keyword = " + this.keyword + "\n";
        result += prefix + "  local = " + this.isLocalObject() + "\n";
        result += prefix + "  uuid = " + uuid + "\n";
        result += prefix + "  fields = [\n";
        for (Map.Entry<String, CaffaField<?>> entry : this.fields.entrySet()) {
            CaffaField<?> field = entry.getValue();
            if (field == null) {
                logger.error("Field {} is null!", entry.getKey());
            } else {
                assert field != null;
                result += field.dump(prefix + "    ");
            }
        }
        result += prefix + "  ]\n";
        result += prefix + "  methods = [\n";
        for (Map.Entry<String, CaffaObjectMethod> entry : this.methods.entrySet()) {
            CaffaObjectMethod method = entry.getValue();
            if (method == null) {
                logger.error("Method {} is null!", entry.getKey());
            } else {
                assert method != null;
                result += method.dump(prefix + "    ");
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

    public void addMethod(CaffaObjectMethod method) {
        this.methods.put(method.keyword, method);
    }

    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObject.class,
                        new CaffaObjectAdapter(this.client, null, this.hasLocalDataFields));
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
        ArrayList<CaffaObjectMethod> allMethods = new ArrayList<>();
        for (Map.Entry<String, CaffaObjectMethod> entry : methods.entrySet()) {
            allMethods.add(entry.getValue());
        }
        return allMethods;
    }

    public CaffaObjectMethod method(String name) throws RuntimeException {
        if (!this.methods.containsKey(name)) {
            String errMsg = "Method does not exist: " + name;
            logger.error(errMsg);
            throw new RuntimeException(errMsg);
        }
        return this.methods.get(name);        
    }

    public CaffaObjectMethodResult execute(CaffaObjectMethod method) throws CaffaConnectionError {
        return new CaffaObjectMethodResult(this.client, this.client.execute(method), method.getResultSchema());
    }
}
