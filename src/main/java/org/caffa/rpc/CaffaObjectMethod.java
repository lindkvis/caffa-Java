package org.caffa.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * An Caffa Object Method for remote calls on a specified "self"-object
 * Objects of this type always store values locally and they will be serialised
 * and sent when executing the method.
 */
public class CaffaObjectMethod extends CaffaObject {
    public final CaffaObject self;

    public CaffaObjectMethod(String classKeyword, CaffaObject self) {
        super(classKeyword, self.sessionUuid());
        this.self = self;
    }

    /**
     * Execute the method. This uses the gRPC accessors in the method's self-object
     * since the CaffaObjectMethod itself is a purely local object.
     *
     * @return
     */
    public CaffaObjectMethodResult execute() {
        return self.execute(this);
    }

    @Override
    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObjectMethod.class,
                new CaffaObjectMethodAdapter(this.self));
        Gson gson = builder.create();
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
