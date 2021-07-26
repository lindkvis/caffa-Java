package org.caffa.rpc;

import org.caffa.rpc.CaffaField;
import org.caffa.rpc.CaffaObject;
import com.google.gson.annotations.Expose;

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
        return null;
    }
}