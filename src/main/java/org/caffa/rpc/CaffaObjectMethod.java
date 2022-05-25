package org.caffa.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class CaffaObjectMethod extends CaffaObject {
    @Expose
    public CaffaObject self;

    public CaffaObjectMethod(CaffaObject self) {
        super(self.channel, false);
        this.self = self;
    }

    public CaffaObjectMethodResult execute() {
        return self.execute(this);
    }

    @Override
    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObjectMethod.class,
                new CaffaObjectMethodAdapter(this.self, this.channel));
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
