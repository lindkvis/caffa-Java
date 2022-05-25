package org.caffa.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class CaffaObjectMethodResult extends CaffaObject
{
    @Expose
    public CaffaObject self;

    public CaffaObjectMethodResult(CaffaObject self) {
        super(self.channel, false);
        this.self = self;        
    }

    @Override
    public String getJson()
    {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObjectMethodResult.class,
            new CaffaObjectMethodAdapter(this.self, this.channel));
        Gson gson = builder.create();
        return gson.toJson(this);
    }
}
