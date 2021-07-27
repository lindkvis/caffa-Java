package org.caffa.rpc;

import com.google.gson.annotations.Expose;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CaffaObjectMethod extends CaffaObject
{
    @Expose
    public CaffaObject self;

    public CaffaObjectMethod(CaffaObject self) {
        super(self.channel);
        this.self = self;        
    }

    public CaffaObject execute()
    {
        return self.execute(this);
    }

    @Override
    public String getJson()
    {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObjectMethod.class,
            new CaffaObjectMethodAdapter(this.self, this.channel));
        Gson gson = builder.create();
        String jsonObject = gson.toJson(this);
        return jsonObject;
    }
}