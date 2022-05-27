package org.caffa.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.ManagedChannel;

public class CaffaObjectMethodResult extends CaffaObject {
    public CaffaObjectMethodResult(String classKeyword, String uuid, ManagedChannel channel, String sessionUuid) {
        super(classKeyword, uuid, channel, false, sessionUuid);
    }

    @Override
    public String getJson() {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObjectMethodResult.class,
                new CaffaObjectAdapter(this.channel(), false, this.sessionUuid()));
        Gson gson = builder.create();
        return gson.toJson(this);
    }
}
