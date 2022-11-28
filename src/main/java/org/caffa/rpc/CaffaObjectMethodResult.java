package org.caffa.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CaffaObjectMethodResult extends CaffaObject {
    public CaffaObjectMethodResult(String classKeyword, String sessionUuid) {
        super(classKeyword, sessionUuid);
    }

    @Override
    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObjectMethodResult.class,
                new CaffaObjectAdapter(this.sessionUuid));
        Gson gson = builder.create();
        return gson.toJson(this);
    }
}
