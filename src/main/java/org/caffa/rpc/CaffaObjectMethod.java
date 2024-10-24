package org.caffa.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A Caffa Object Method for remote calls on a specified "self"-object
 * Objects of this type always store values locally, and they will be serialised
 * and sent when executing the method.
 */
public class CaffaObjectMethod extends CaffaObject {
    private final CaffaObject self;
    private String resultSchema = "";

    public CaffaObjectMethod(String classKeyword, CaffaObject self, RestClient client) {
        super(classKeyword, true, client, "");
        this.self = self;
    }

    /**
     * Execute the method. This uses the RPC accessors in the method's self-object
     * since the CaffaObjectMethod itself is a purely local object.
     *
     * @return Result of executing the method.
     */
    public CaffaObjectMethodResult execute() throws CaffaConnectionError {
        return self.execute(this);
    }

    public CaffaObject getSelf() {
        return this.self;
    }

    public String getResultSchema() {        
        return this.resultSchema;
    }

    public void setResultSchema(String schema) {
        this.resultSchema = schema;
    }


    @Override
    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObjectMethod.class,
                new CaffaObjectMethodAdapter(this.self, this.keyword()));
        Gson gson = builder.serializeNulls().create();
        return gson.toJson(this);
    }

    public <T> void setParam(String keyword, T value, Class<T> type) throws Exception {
        CaffaField<?> field = this.field(keyword);
        assert field != null;
        if (field != null) {
            field.set(value, type);
        }
    }
}
