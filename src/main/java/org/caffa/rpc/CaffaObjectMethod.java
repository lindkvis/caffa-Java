package org.caffa.rpc;

import java.lang.reflect.Type;

import com.google.gson.annotations.Expose;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class CaffaObjectMethod extends CaffaObject
{
    @Expose
    public CaffaObject self;

    public CaffaObjectMethod(CaffaObject self) {
        super(self.channel);
        this.self = self;        
    }

    public CaffaObjectMethodResult execute()
    {
        return self.execute(this);
    }

    @Override
    public String getJson()
    {
        System.out.println("Getting object method json");
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(CaffaObjectMethod.class,
            new CaffaObjectMethodAdapter(this.self, this.channel));
        Gson gson = builder.create();
        return gson.toJson(this);
    }

    public <T> void setParam(String keyword, T value, Class<T> type)
    {
        CaffaField<?> field = this.field(keyword);
        if (field != null)
        {
            field.set(value, type);
        }
    }
}
