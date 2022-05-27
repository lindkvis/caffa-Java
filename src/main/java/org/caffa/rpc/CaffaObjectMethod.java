package org.caffa.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class CaffaObjectMethod extends CaffaObject {
    public final CaffaObject self;

    public CaffaObjectMethod(String classKeyword, String uuid, CaffaObject self) {
        super(classKeyword, uuid);
        this.self = self;
    }

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
